import "./globals.css";

export const metadata = {
  title: "CRM AI Project",
  description: "Frontend Next.js do CRM AI Project consumindo a API Spring Boot."
};

export default function RootLayout({ children }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
