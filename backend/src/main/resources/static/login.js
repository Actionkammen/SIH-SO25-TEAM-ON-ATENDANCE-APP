document.addEventListener('DOMContentLoaded', () => {
  const form = document.querySelector('.login-form');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = form.email.value;
    const password = form.password.value;

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ email, password })
      });

      if (response.ok) {
        const data = await response.json();
        alert('Login successful! Welcome ' + data.name);
        // Store JWT token in localStorage or cookie as needed
        localStorage.setItem('token', data.token);
        // Redirect or load dashboard page here if available
      } else {
        const errorText = await response.text();
        alert('Login failed: ' + errorText);
      }
    } catch (error) {
      alert('Error connecting to server: ' + error.message);
    }
  });

  // Toggle password visibility
  const togglePassword = document.querySelector('.toggle-password');
  const passwordInput = form.password;
  togglePassword.addEventListener('click', () => {
    if (passwordInput.type === 'password') {
      passwordInput.type = 'text';
      togglePassword.classList.remove('fa-eye');
      togglePassword.classList.add('fa-eye-slash');
    } else {
      passwordInput.type = 'password';
      togglePassword.classList.remove('fa-eye-slash');
      togglePassword.classList.add('fa-eye');
    }
  });
});
