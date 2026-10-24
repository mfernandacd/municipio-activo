// Configuración exteriorizada de Tailwind CSS para Municipio Serranoble
window.tailwind = window.tailwind || {};
window.tailwind.config = {
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        brand: ['Cinzel', 'serif']
      },
      colors: {
        serranoble: {
          navy: '#0A2240',      // Azul noche profundo del fondo del escudo
          navyDark: '#06162B',  // Azul ultra oscuro para headers
          navyLight: '#16355C', // Azul medio para bordes/acentos
          gold: '#E5B83A',      // Dorado espiga y rayos del sol
          goldHover: '#D4A426', // Dorado para hover
          goldLight: '#FFF8E1', // Fondo crema suave con tinte dorado
          brown: '#7C4328',     // Café/Marrón de las montañas
          brownLight: '#A35D3B',
          cream: '#FAF6EE'      // Crema elegante de acentos
        }
      }
    }
  }
};
