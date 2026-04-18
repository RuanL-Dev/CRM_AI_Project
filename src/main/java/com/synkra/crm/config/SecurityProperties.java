package com.synkra.crm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "app.security")
public class SecurityProperties {

    private boolean bootstrapEnabled;
    private String username;
    private String password;
    private boolean devH2ConsoleEnabled;
    private final PasswordPolicy passwordPolicy = new PasswordPolicy();
    private final LoginRateLimit loginRateLimit = new LoginRateLimit();

    public boolean isBootstrapEnabled() {
        return bootstrapEnabled;
    }

    public void setBootstrapEnabled(boolean bootstrapEnabled) {
        this.bootstrapEnabled = bootstrapEnabled;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDevH2ConsoleEnabled() {
        return devH2ConsoleEnabled;
    }

    public void setDevH2ConsoleEnabled(boolean devH2ConsoleEnabled) {
        this.devH2ConsoleEnabled = devH2ConsoleEnabled;
    }

    public PasswordPolicy getPasswordPolicy() {
        return passwordPolicy;
    }

    public LoginRateLimit getLoginRateLimit() {
        return loginRateLimit;
    }

    public static class PasswordPolicy {

        private int minLength = 12;

        public int getMinLength() {
            return minLength;
        }

        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }
    }

    public static class LoginRateLimit {

        private boolean enabled = true;
        private int maxFailures = 5;
        private Duration window = Duration.ofMinutes(15);
        private Duration lockout = Duration.ofMinutes(15);

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getMaxFailures() {
            return maxFailures;
        }

        public void setMaxFailures(int maxFailures) {
            this.maxFailures = maxFailures;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }

        public Duration getLockout() {
            return lockout;
        }

        public void setLockout(Duration lockout) {
            this.lockout = lockout;
        }
    }
}
