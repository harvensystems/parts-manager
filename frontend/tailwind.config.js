/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      colors: {
        workshop: {
          50: '#f0fdf4',
          500: '#22c55e',
          600: '#16a34a',
          900: '#0f172a',
          950: '#090d16',
        }
      }
    },
  },
  plugins: [],
}
