document.addEventListener('DOMContentLoaded', () => {
	// Referencias al formulario y a sus campos principales.
	const loginForm = document.getElementById('login-form');
	const usuarioInput = document.getElementById('usuario');
	const passwordInput = document.getElementById('password');

	// Expresión regular básica para validar el formato del correo electrónico.
	const correoRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

	// Detiene la ejecución si la página no contiene el formulario de login.
	if (!loginForm || !usuarioInput || !passwordInput) {
		return;
	}

	// Marca un campo como válido después de corregir su contenido.
	const clearFieldError = (field) => {
		field.classList.remove('is-invalid');
		field.classList.add('is-valid');
	};

	// Valida los datos y autentica al usuario contra el backend.
	loginForm.addEventListener('submit', async (event) => {
		event.preventDefault();

		// Limpia los estados anteriores antes de realizar una nueva validación.
		usuarioInput.classList.remove('is-invalid', 'is-valid');
		passwordInput.classList.remove('is-invalid', 'is-valid');

		// Obtiene los valores ingresados sin espacios al inicio o al final.
		const usuario = usuarioInput.value.trim();
		const password = passwordInput.value.trim();
		let formularioValido = true;

		// Verifica que el correo tenga un formato válido.
		if (!correoRegex.test(usuario)) {
			usuarioInput.classList.add('is-invalid');
			formularioValido = false;
		} else {
			clearFieldError(usuarioInput);
		}

		// Verifica que la contraseña haya sido ingresada.
		if (!password) {
			passwordInput.classList.add('is-invalid');
			formularioValido = false;
		} else {
			clearFieldError(passwordInput);
		}

		// Si hay algún error en el formulario, muestra una alerta con SweetAlert2.
		if (!formularioValido) {
			Swal.fire({
				icon: 'error',
				title: 'Datos incorrectos',
				text: 'Por favor, verificá que el correo tenga un formato válido y que la contraseña no esté vacía.',
				confirmButtonColor: '#0d6efd'
			});
			return;
		}

		try {
			const backendUrl = window.location.port === '8080' ? '' : 'http://localhost:8080';
			const response = await fetch(`${backendUrl}/api/auth/login`, {
				method: 'POST',
				credentials: 'include',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ email: usuario, password })
			});

			if (!response.ok) {
				throw new Error('Credenciales inválidas');
			}

			const usuarioAutenticado = await response.json();
			localStorage.setItem('usuarioMunicipio', JSON.stringify(usuarioAutenticado));
			await Swal.fire({
				icon: 'success',
				title: '¡Bienvenido!',
				text: 'Iniciando sesión...',
				showConfirmButton: false,
				timer: 1500
			});
			window.location.href = loginForm.action;
		} catch (error) {
			Swal.fire({
				icon: 'error',
				title: 'No se pudo iniciar sesión',
				text: 'Verificá tu correo y contraseña, e intentá nuevamente.',
				confirmButtonColor: '#0d6efd'
			});
		}
	});
});
