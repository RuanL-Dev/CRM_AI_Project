import { cpSync, existsSync, mkdirSync, rmSync } from "node:fs";
import { dirname, join, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const scriptDir = dirname(fileURLToPath(import.meta.url));
const frontendRoot = resolve(scriptDir, "..");
const exportDir = join(frontendRoot, "out");
const targetDir = resolve(frontendRoot, "..", "src", "main", "resources", "static", "ui");

if (!existsSync(exportDir)) {
  throw new Error("Next.js export directory not found. Run the build before syncing.");
}

rmSync(targetDir, { recursive: true, force: true });
mkdirSync(targetDir, { recursive: true });
cpSync(exportDir, targetDir, { recursive: true });
