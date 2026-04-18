import "./globals.css";

export const metadata = {
  title: "AIOX Growth OS",
  description: "Workspace de campanhas, segmentos, provedores SMTP e formularios publicos integrado ao CRM Spring Boot."
};

export default function RootLayout({ children }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  );
}
