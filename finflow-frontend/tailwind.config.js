/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        display: ['Outfit', 'Inter', 'sans-serif'],
      },
      colors: {
        dark: {
          900: '#0a0e1a',
          800: '#111827',
          700: '#1e293b',
          600: '#334155',
          500: '#475569',
        },
        accent: {
          blue: '#3b82f6',
          indigo: '#6366f1',
          violet: '#8b5cf6',
          emerald: '#10b981',
          amber: '#f59e0b',
          rose: '#f43f5e',
          cyan: '#06b6d4',
        },
        bank: {
          bg: '#F5F3EE',
          white: '#FFFFFF',
          navy: '#121C2D',
          'gray-600': '#4B5563',
          'gray-500': '#6B7280',
          'gray-400': '#9CA3AF',
          border: '#E5E0D8',
          emerald: '#10B981',
          rose: '#EF4444',
          amber: '#F59E0B',
        }
      },
    },
  },
  plugins: [],
}
