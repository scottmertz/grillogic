(function() {
    const token = localStorage.getItem('grillogic_token');
    if (!token) {
        window.location.href = '/login';
        return;
    }

    document.getElementById('userEmail').textContent = localStorage.getItem('grillogic_email') || '';

    const role = localStorage.getItem('grillogic_role');

    const dashLink = document.getElementById('dashLink');
    if (dashLink) {
        dashLink.href = role === 'ADMIN' ? '/admin' : '/dashboard';
    }

    const adminLink = document.getElementById('adminLink');
    if (adminLink && role === 'ADMIN') {
        adminLink.style.display = 'inline';
    }

    document.getElementById('logoutBtn').addEventListener('click', function() {
        localStorage.clear();
        window.location.href = '/login';
    });
})();