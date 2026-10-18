document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('tracking-form');
  const inputCodigo = document.getElementById('codigo-seguimiento');
  const btnConsultar = document.getElementById('btn-consultar');
  const loadingIndicator = document.getElementById('loading-indicator');
  const errorMessage = document.getElementById('error-message');
  const resultCard = document.getElementById('result-card');

  const resId = document.getElementById('res-id');
  const resCategoria = document.getElementById('res-categoria');
  const resEstado = document.getElementById('res-estado');
  const resFecha = document.getElementById('res-fecha');

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
      const response = await fetch(`/api/reclamos/seguimiento/${encodeURIComponent(codigo)}`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        }
      });

      if (!response.ok) {
        if (response.status === 404) {
          throw new Error('No encontramos ningún reclamo asociado al código ingresado. Por favor, revisá los dígitos e intentalo nuevamente.');
        } else {
          throw new Error('No pudimos procesar tu consulta en este momento. Por favor, intentá más tarde.');
        }
      }

      const data = await response.json();

      resId.textContent = data.codigoSeguimiento || codigo;
      resCategoria.textContent = data.categoria || 'No especificada';
      resEstado.textContent = formatearEstado(data.estado);
      resEstado.className = `badge ${obtenerClaseEstado(data.estado)}`;
      resFecha.textContent = data.fechaCreacion
        ? new Date(data.fechaCreacion).toLocaleString('es-AR')
        : 'No disponible';

      resultCard.classList.remove('d-none');

    } catch (error) {
      errorMessage.textContent = error instanceof TypeError
        ? 'No pudimos conectarnos con el servicio. Revisá tu conexión e intentá nuevamente.'
        : error.message;
      errorMessage.classList.remove('d-none');
    } finally {
      loadingIndicator.classList.add('d-none');
      btnConsultar.disabled = false;
      inputCodigo.disabled = false;
    }
  });

  function obtenerClaseEstado(estado) {
    switch (estado?.toUpperCase()) {
      case 'RESUELTO':
        return 'bg-success';
      case 'EN_PROCESO':
        return 'bg-warning text-dark';
      case 'PENDIENTE':
      default:
        return 'bg-secondary';
    }
  }

  function formatearEstado(estado) {
    return estado === 'EN_PROCESO' ? 'En proceso' : estado?.toLowerCase().replace(/^./, letra => letra.toUpperCase());
  }
});