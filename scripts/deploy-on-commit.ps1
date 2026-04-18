[CmdletBinding()]
param(
    [switch]$SkipBuild,
    [switch]$SkipPublicCheck
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$deployConfigPath = Join-Path $repoRoot ".local/deploy.local.ps1"
$deployDir = Join-Path $repoRoot "deploy"
$targetDir = Join-Path $repoRoot "target"
$deployStampPath = Join-Path $repoRoot ".local/last-deployed-commit"
$tempFiles = [System.Collections.Generic.List[string]]::new()
$temporaryWorktree = $null

function Write-Step {
    param([string]$Message)
    Write-Host ("==> " + $Message)
}

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Comando obrigatorio ausente: $Name"
    }
}

function New-TempFileFromContent {
    param(
        [string]$Prefix,
        [string]$Content
    )

    $tempPath = Join-Path ([System.IO.Path]::GetTempPath()) ($Prefix + "-" + [guid]::NewGuid().ToString("N"))
    $normalizedContent = $Content -replace "`r`n", "`n"
    $utf8NoBom = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText($tempPath, $normalizedContent, $utf8NoBom)
    $tempFiles.Add($tempPath) | Out-Null
    return $tempPath
}

function Protect-PrivateKeyFile {
    param([string]$Path)

    if (-not $IsWindows) {
        return
    }

    $currentUser = [System.Security.Principal.NTAccount]::new("$env:USERDOMAIN\$env:USERNAME")
    $acl = [System.Security.AccessControl.FileSecurity]::new()
    $rule = [System.Security.AccessControl.FileSystemAccessRule]::new(
        $currentUser,
        [System.Security.AccessControl.FileSystemRights]::FullControl,
        [System.Security.AccessControl.AccessControlType]::Allow
    )
    $acl.SetOwner($currentUser)
    $acl.SetAccessRuleProtection($true, $false)
    $null = $acl.SetAccessRule($rule)
    Set-Acl -LiteralPath $Path -AclObject $acl
}

function Get-DeployConfig {
    if (-not (Test-Path -LiteralPath $deployConfigPath)) {
        throw "Arquivo local de deploy nao encontrado: $deployConfigPath. Use deploy/deploy.local.ps1.example como modelo."
    }

    $script:deployConfig = $null
    $deployConfig = $null
    . $deployConfigPath

    $resolvedConfig = if ($script:deployConfig) { $script:deployConfig } else { $deployConfig }

    if (-not $resolvedConfig) {
        throw "O arquivo $deployConfigPath deve definir a variavel `$deployConfig."
    }

    $config = @{}
    foreach ($entry in $resolvedConfig.GetEnumerator()) {
        $config[$entry.Key] = ([string]$entry.Value).Trim()
    }

    return $config
}

function Require-ConfigValue {
    param(
        [hashtable]$Config,
        [string]$Key
    )

    if (-not $Config.ContainsKey($Key) -or [string]::IsNullOrWhiteSpace($Config[$Key])) {
        throw "Configuracao obrigatoria ausente: $Key"
    }

    return $Config[$Key]
}

function Get-ConfigValueOrDefault {
    param(
        [hashtable]$Config,
        [string]$Key,
        [string]$Default
    )

    if ($Config.ContainsKey($Key) -and -not [string]::IsNullOrWhiteSpace($Config[$Key])) {
        return $Config[$Key]
    }

    return $Default
}

function Resolve-SecretFile {
    param(
        [hashtable]$Config,
        [string]$InlineKey,
        [string]$PathKey,
        [string]$Prefix,
        [switch]$PrivateKey
    )

    if ($Config.ContainsKey($PathKey) -and -not [string]::IsNullOrWhiteSpace($Config[$PathKey])) {
        $candidate = $Config[$PathKey]
        if (-not [System.IO.Path]::IsPathRooted($candidate)) {
            $candidate = Join-Path $repoRoot $candidate
        }
        if (-not (Test-Path -LiteralPath $candidate)) {
            throw "Arquivo configurado em $PathKey nao encontrado: $candidate"
        }

        if ($PrivateKey) {
            $tempKeyPath = New-TempFileFromContent -Prefix $Prefix -Content ((Get-Content -LiteralPath $candidate -Raw).Trim() + [Environment]::NewLine)
            Protect-PrivateKeyFile -Path $tempKeyPath
            return $tempKeyPath
        }

        return $candidate
    }

    $content = Require-ConfigValue -Config $Config -Key $InlineKey
    $tempPath = New-TempFileFromContent -Prefix $Prefix -Content ($content.Trim() + [Environment]::NewLine)
    if ($PrivateKey) {
        Protect-PrivateKeyFile -Path $tempPath
    }
    return $tempPath
}

function Invoke-Native {
    param(
        [string]$FilePath,
        [string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Falha ao executar: $FilePath $($Arguments -join ' ')"
    }
}

function Get-BuiltJar {
    param([string]$SearchDir)

    $jars = Get-ChildItem -LiteralPath $SearchDir -Filter "*.jar" -File |
        Where-Object { $_.Name -notlike "*.jar.original" -and $_.Name -notlike "*-sources.jar" -and $_.Name -notlike "*-javadoc.jar" } |
        Sort-Object LastWriteTime -Descending

    if (-not $jars) {
        throw "Nenhum jar de aplicacao foi encontrado em $SearchDir"
    }

    return $jars[0]
}

function Test-PublicLogin {
    param([string]$Url)

    for ($attempt = 1; $attempt -le 24; $attempt++) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -MaximumRedirection 5 -TimeoutSec 15
            if ($response.Content -match "Entrar \| Painel Comercial") {
                return
            }
        } catch {
            Start-Sleep -Seconds 5
            continue
        }

        Start-Sleep -Seconds 5
    }

    throw "O endpoint publico nao respondeu como esperado: $Url"
}

Require-Command -Name "git"
Require-Command -Name "mvn"
Require-Command -Name "ssh"
Require-Command -Name "scp"

$config = Get-DeployConfig
$deployHost = Require-ConfigValue -Config $config -Key "CRM_DEPLOY_HOST"
$deployUser = Require-ConfigValue -Config $config -Key "CRM_DEPLOY_USER"
$deployPath = Require-ConfigValue -Config $config -Key "CRM_DEPLOY_PATH"
$deployPort = Get-ConfigValueOrDefault -Config $config -Key "CRM_DEPLOY_PORT" -Default "22"
$publicLoginUrl = Require-ConfigValue -Config $config -Key "CRM_PUBLIC_LOGIN_URL"
$sshKeyFile = Resolve-SecretFile -Config $config -InlineKey "CRM_DEPLOY_SSH_PRIVATE_KEY" -PathKey "CRM_DEPLOY_SSH_KEY_PATH" -Prefix "crm-deploy-key" -PrivateKey
$knownHostsFile = Resolve-SecretFile -Config $config -InlineKey "CRM_DEPLOY_KNOWN_HOSTS" -PathKey "CRM_DEPLOY_KNOWN_HOSTS_PATH" -Prefix "crm-known-hosts"

$envLines = @(
    "CRM_POSTGRES_DB=$(Require-ConfigValue -Config $config -Key "CRM_POSTGRES_DB")"
    "CRM_POSTGRES_USER=$(Require-ConfigValue -Config $config -Key "CRM_POSTGRES_USER")"
    "CRM_POSTGRES_PASSWORD=$(Require-ConfigValue -Config $config -Key "CRM_POSTGRES_PASSWORD")"
    "CRM_APP_PROFILE=$(Require-ConfigValue -Config $config -Key "CRM_APP_PROFILE")"
    "CRM_APP_USERNAME=$(Require-ConfigValue -Config $config -Key "CRM_APP_USERNAME")"
    "CRM_APP_PASSWORD=$(Require-ConfigValue -Config $config -Key "CRM_APP_PASSWORD")"
    "CRM_BOOTSTRAP_ENABLED=$(Get-ConfigValueOrDefault -Config $config -Key "CRM_BOOTSTRAP_ENABLED" -Default "true")"
    "N8N_WEBHOOK_URL=$(Get-ConfigValueOrDefault -Config $config -Key "N8N_WEBHOOK_URL" -Default '')"
    "N8N_CONNECT_TIMEOUT_MS=$(Get-ConfigValueOrDefault -Config $config -Key "N8N_CONNECT_TIMEOUT_MS" -Default "2000")"
    "N8N_READ_TIMEOUT_MS=$(Get-ConfigValueOrDefault -Config $config -Key "N8N_READ_TIMEOUT_MS" -Default "5000")"
    "N8N_RETRY_DELAY_MS=$(Get-ConfigValueOrDefault -Config $config -Key "N8N_RETRY_DELAY_MS" -Default "30000")"
    "N8N_RETRY_SCHEDULER_DELAY_MS=$(Get-ConfigValueOrDefault -Config $config -Key "N8N_RETRY_SCHEDULER_DELAY_MS" -Default "30000")"
    "N8N_MAX_ATTEMPTS=$(Get-ConfigValueOrDefault -Config $config -Key "N8N_MAX_ATTEMPTS" -Default "5")"
)

$deployEnvFile = Join-Path $deployDir ".env.production"
($envLines -join [Environment]::NewLine) + [Environment]::NewLine | Set-Content -LiteralPath $deployEnvFile -NoNewline

$sshSharedArgs = @(
    "-i", $sshKeyFile,
    "-o", "BatchMode=yes",
    "-o", "StrictHostKeyChecking=yes",
    "-o", "UserKnownHostsFile=$knownHostsFile"
)

$sshArgs = $sshSharedArgs + @(
    "-p", $deployPort
)

$scpArgs = $sshSharedArgs + @(
    "-P", $deployPort
)

try {
    $commitSha = (& git -C $repoRoot rev-parse --short HEAD).Trim()
    $commitRef = (& git -C $repoRoot rev-parse HEAD).Trim()
    $buildRoot = $repoRoot
    $buildTargetDir = $targetDir
    $buildDeployDir = $deployDir

    if (-not $SkipBuild) {
        $temporaryWorktree = Join-Path ([System.IO.Path]::GetTempPath()) ("crm-deploy-worktree-" + [guid]::NewGuid().ToString("N"))
        Write-Step "Criando worktree temporario do commit $commitSha"
        Invoke-Native -FilePath "git" -Arguments @("-C", $repoRoot, "worktree", "add", "--detach", $temporaryWorktree, $commitRef)

        $buildRoot = $temporaryWorktree
        $buildTargetDir = Join-Path $buildRoot "target"
        $buildDeployDir = Join-Path $buildRoot "deploy"

        Write-Step "Executando mvn verify no worktree temporario"
        Push-Location $buildRoot
        try {
            Invoke-Native -FilePath "mvn" -Arguments @("verify")
        } finally {
            Pop-Location
        }
    } else {
        Write-Step "Build ignorado por parametro"
    }

    $jar = Get-BuiltJar -SearchDir $buildTargetDir
    $sha256File = Join-Path $buildTargetDir ($jar.Name + ".sha256")
    $hash = Get-FileHash -LiteralPath $jar.FullName -Algorithm SHA256
    "{0} *{1}" -f $hash.Hash.ToLowerInvariant(), $jar.Name | Set-Content -LiteralPath $sha256File -NoNewline

    $remote = "$deployUser@$deployHost"

    Write-Step "Preparando diretorio remoto"
    Invoke-Native -FilePath "ssh" -Arguments ($sshArgs + @($remote, "mkdir -p '$deployPath'"))

    Write-Step "Enviando artefatos para $deployHost"
    Invoke-Native -FilePath "scp" -Arguments ($scpArgs + @($jar.FullName, "${remote}:$deployPath/$($jar.Name)"))
    Invoke-Native -FilePath "scp" -Arguments ($scpArgs + @($sha256File, "${remote}:$deployPath/$($jar.Name).sha256"))
    Invoke-Native -FilePath "scp" -Arguments ($scpArgs + @((Join-Path $buildDeployDir "docker-compose.yml"), "${remote}:$deployPath/docker-compose.yml"))
    Invoke-Native -FilePath "scp" -Arguments ($scpArgs + @((Join-Path $buildDeployDir "Caddyfile"), "${remote}:$deployPath/Caddyfile"))
    Invoke-Native -FilePath "scp" -Arguments ($scpArgs + @((Join-Path $buildDeployDir "README.md"), "${remote}:$deployPath/README.md"))
    Invoke-Native -FilePath "scp" -Arguments ($scpArgs + @($deployEnvFile, "${remote}:$deployPath/.env"))

    Write-Step "Reiniciando stack remota"
    $restartScript = @'
cd '__DEPLOY_PATH__' &&
chmod 600 '.env' &&
sha256sum -c '__JAR_NAME__.sha256' &&
if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD='docker compose'
else
  COMPOSE_CMD='docker-compose'
fi &&
$COMPOSE_CMD up -d --force-recreate crm-postgres crm-app &&
$COMPOSE_CMD ps
'@
    $restartScript = $restartScript.Replace("__DEPLOY_PATH__", $deployPath).Replace("__JAR_NAME__", $jar.Name)
    Invoke-Native -FilePath "ssh" -Arguments ($sshArgs + @($remote, $restartScript))

    Write-Step "Validando healthcheck interno"
    $healthScript = @'
if docker compose version >/dev/null 2>&1; then
  COMPOSE_CMD='docker compose'
else
  COMPOSE_CMD='docker-compose'
fi
cd '__DEPLOY_PATH__'
for attempt in $(seq 1 48); do
  if curl --fail --silent http://127.0.0.1:8081/healthz | grep -q '\"status\":\"ok\"'; then
    exit 0
  fi
  sleep 5
done
$COMPOSE_CMD logs --tail=200 crm-postgres crm-app >&2
exit 1
'@
    $healthScript = $healthScript.Replace("__DEPLOY_PATH__", $deployPath)
    Invoke-Native -FilePath "ssh" -Arguments ($sshArgs + @($remote, $healthScript))

    if (-not $SkipPublicCheck) {
        Write-Step "Validando endpoint publico"
        Test-PublicLogin -Url $publicLoginUrl
    }

    if (-not (Test-Path -LiteralPath (Split-Path -Parent $deployStampPath))) {
        New-Item -ItemType Directory -Path (Split-Path -Parent $deployStampPath) | Out-Null
    }
    Set-Content -LiteralPath $deployStampPath -Value $commitSha -NoNewline

    Write-Step "Deploy concluido no commit $commitSha"
} finally {
    if (Test-Path -LiteralPath $deployEnvFile) {
        Remove-Item -LiteralPath $deployEnvFile -Force
    }

    if ($temporaryWorktree -and (Test-Path -LiteralPath $temporaryWorktree)) {
        Invoke-Native -FilePath "git" -Arguments @("-C", $repoRoot, "worktree", "remove", "--force", $temporaryWorktree)
    }

    foreach ($tempFile in $tempFiles) {
        if (Test-Path -LiteralPath $tempFile) {
            Remove-Item -LiteralPath $tempFile -Force
        }
    }
}
