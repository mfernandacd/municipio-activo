document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('tracking-form');
  const inputCodigo = document.getElementById('codigo-seguimiento');
  const btnConsultar = document.getElementById('btn-consultar');
  const loadingIndicator = document.getElementById('loading-indicator');
  const errorMessage = document.getElementById('error-message');
  const resultCard = document.getElementById('result-card');

  // Elementos de detalle dentro de la tarjeta
  const resId = document.getElementById('res-id');
  const resCategoria = document.getElementById('res-categoria');
  const resDireccion = document.getElementById('res-direccion');
  const resDescripcion = document.getElementById('res-descripcion');
  const resEstado = document.getElementById('res-estado');

  form.addEventListener('submit', async (event) => {
    event.preventDefault();
    const codigo = inputCodigo.value.trim();

    if (!codigo) return;

    // 1. Resetear estados previos de la interfaz
    errorMessage.classList.add('d-none');
    resultCard.classList.add('d-none');
    
    // 2. Activar indicador de carga y bloquear controles
    loadingIndicator.classList.remove('d-none');
    btnConsultar.disabled = true;
    inputCodigo.disabled = true;

    try {
      // 3. Petición HTTP GET al servidor
      const response = await fetch(`/api/reclamos/${encodeURIComponent(codigo)}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });

      if (!response.ok) {
        // Manejo específico si no se encuentra o hay error en el servidor
        if (response.status === 404) {
          throw new Error('No encontramos ningún reclamo asociado al código ingresado. Por favor, revisá los dígitos e intentalo nuevamente.');
        } else {
          throw new Error('No pudimos procesar tu consulta en este momento. Por favor, intentá más tarde.');
        }
      }

      const data = await response.json();

      // 4. Inyectar datos en el HTML dinámicamente
      resId.textContent = data.id || codigo;
      resCategoria.textContent = data.categoria || 'No especificada';
      resDireccion.textContent = data.direccion || 'No especificada';
      resDescripcion.textContent = data.descripcion || 'Sin descripción detallada';
      resEstado.textContent = data.estado;
      
      // Aplicar color dinámico al badge según el estado
      resEstado.className = 'badge ' + obtenerClaseEstado(data.estado);

      // Mostrar la tarjeta de resultados
      resultCard.classList.remove('d-none');

    } catch (error) {
      // 5. Presentar aviso claro al usuario omitiendo términos técnicos
      errorMessage.textContent = error.message;
      errorMessage.classList.remove('d-none');
    } finally {
      // 6. Desactivar carga y restaurar controles
      loadingIndicator.classList.add('d-none');
      btnConsultar.disabled = false;
      inputCodigo.disabled = false;
    }
  });

  // Función auxiliar para estilos visuales del estado
  function obtenerClaseEstado(estado) {
    switch (estado.toLowerCase()) {
      case 'resuelto':
        return 'bg-success';
      case 'en proceso':
        return 'bg-warning text-dark';
      case 'pendiente':
      default:
        return 'bg-secondary';
    }
  }
});