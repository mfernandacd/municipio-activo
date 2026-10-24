document.addEventListener('DOMContentLoaded', () => {
  // Seleccionamos todos los selectores de estado en el dashboard
  const statusSelectors = document.querySelectorAll('.status-selector');

  statusSelectors.forEach(selectElement => {
    // Guardamos el estado inicial por si es necesario revertirlo
    selectElement.dataset.previousValue = selectElement.value;

    selectElement.addEventListener('change', async (event) => {
      const select = event.target;
      const newStatus = select.value;
      
      // Encontramos la fila contenedora para extraer el ID del reclamo
      const row = select.closest('tr');
      const claimId = row ? row.getAttribute('data-claim-id') : null;

      if (!claimId) {
        console.error('No se pudo identificar el ID del reclamo.');
        return;
      }

      const previousStatus = select.dataset.previousValue;

      try {
        // Deshabilitamos el select temporalmente para evitar doble envío
        select.disabled = true;

        // Petición HTTP PUT al servidor
        const endpoint = window.municipioApiUrl(`/api/reclamos/${claimId}/estado`);
        const response = await window.municipioFetch(endpoint, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ estado: newStatus })
        });

        if (!response.ok) {
          throw new Error('Error en la respuesta del servidor al actualizar el estado.');
        }

        // Si la petición es exitosa, actualizamos el valor previo de referencia
        select.dataset.previousValue = newStatus;
        
        // Notificación visual de éxito (opcional: puedes usar un componente Toast o alerta sutil)
        console.log(`Reclamo #${claimId} actualizado a "${newStatus}" con éxito.`);

      } catch (error) {
        console.error('Fallo al actualizar el reclamo:', error);
        
        // Revertimos al valor anterior en el select si ocurre un error
        select.value = previousStatus;
        alert('No se pudo actualizar el estado del reclamo. Por favor, inténtalo de nuevo.');

      } finally {
        // Rehabilitamos el control
        select.disabled = false;
      }
    });
  });
});
