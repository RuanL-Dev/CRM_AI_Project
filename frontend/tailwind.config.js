/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./app/**/*.{js,jsx}",
    "./components/**/*.{js,jsx}"
  ],
  theme: {
    extend: {
      fontFamily: {
        display: ["Trebuchet MS", "Segoe UI Semibold", "sans-serif"],
        body: ["Segoe UI", "Helvetica Neue", "Arial", "sans-serif"]
      },
      colors: {
        ink: "#0f172a",
        cyanbrand: "#0f766e",
        amberbrand: "#d97706"
      },
      boxShadow: {
        panel: "0 24px 70px rgba(15, 23, 42, 0.16)"
      }
    }
  },
  plugins: []
};
