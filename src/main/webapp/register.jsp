<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inscription - Cooperative Reservation</title>
    <link rel="icon" href="${pageContext.request.contextPath}/favicon.ico" />
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <style>
        :root {
            --primary-blue: #2563eb;
            --dark-blue: #1e3a8a;
            --error-red: #ef4444;
            --error-bg: rgba(239, 68, 68, 0.15);
            --glass-bg: rgba(255, 255, 255, 0.1);
            --text-light: #f8fafc;
        }

        body, html {
            margin: 0;
            padding: 0;
            height: 100%;
            font-family: 'Segoe UI', Roboto, sans-serif;
        }

        .background {
            background: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)),
            url('https://images.unsplash.com/photo-1493238792000-8113da705763?auto=format&fit=crop&q=80&w=1920');
            background-size: cover;
            background-position: center;
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 18px;
        }

        .login-card {
            background: var(--glass-bg);
            backdrop-filter: blur(15px);
            -webkit-backdrop-filter: blur(15px);
            border: 1px solid rgba(255, 255, 255, 0.2);
            padding: 40px;
            border-radius: 24px;
            width: 100%;
            max-width: 420px;
            box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
            text-align: center;
        }

        .login-header {
            background: white;
            color: var(--dark-blue);
            padding: 8px 30px;
            border-radius: 50px;
            display: inline-block;
            font-weight: bold;
            margin-bottom: 22px;
            font-size: 1.1rem;
        }

        .sub {
            color: rgba(248, 250, 252, 0.85);
            margin: -6px 0 22px;
            font-size: .92rem;
        }

        .field-feedback {
            margin-top: -6px;
            margin-bottom: 14px;
            text-align: left;
            font-size: 0.88rem;
            font-weight: 600;
            padding-left: 2px;
        }

        .field-feedback.error {
            color: #fecaca;
        }

        .field-feedback.success {
            color: #bbf7d0;
        }

        .input-group {
            position: relative;
            margin-bottom: 16px;
        }

        .input-group i.left-icon {
            position: absolute;
            left: 15px;
            top: 50%;
            transform: translateY(-50%);
            color: rgba(255, 255, 255, 0.85);
        }

        input[type="text"], input[type="password"] {
            width: 100%;
            padding: 14px 46px 14px 45px;
            background: transparent;
            border: 1.5px solid rgba(255, 255, 255, 0.45);
            border-radius: 12px;
            color: white;
            font-size: 1rem;
            box-sizing: border-box;
            transition: all 0.3s ease;
            font-weight: 500;
        }

        input:focus {
            outline: none;
            border-color: #93c5fd;
            background: transparent;
            box-shadow: 0 0 0 4px rgba(37, 99, 235, 0.2);
        }

        /* Keep transparent style even with browser autofill */
        input:-webkit-autofill,
        input:-webkit-autofill:hover,
        input:-webkit-autofill:focus,
        input:-webkit-autofill:active {
            -webkit-text-fill-color: #ffffff;
            -webkit-box-shadow: 0 0 0 1000px transparent inset;
            box-shadow: 0 0 0 1000px transparent inset;
            transition: background-color 9999s ease-out 0s;
        }

        ::placeholder { color: rgba(255, 255, 255, 0.78); }

        .password-toggle {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            border: none;
            background: transparent;
            color: rgba(255, 255, 255, 0.9);
            width: 32px;
            height: 32px;
            border-radius: 8px;
            cursor: pointer;
        }

        .password-toggle:hover {
            background: rgba(255, 255, 255, 0.12);
        }

        .btn-login {
            width: 100%;
            padding: 14px;
            border: none;
            border-radius: 12px;
            background: white;
            color: var(--dark-blue);
            font-weight: bold;
            font-size: 1rem;
            cursor: pointer;
            transition: transform 0.2s, background 0.2s;
            margin-top: 8px;
        }

        .btn-login:hover {
            background: #f1f5f9;
            transform: translateY(-2px);
        }

        .footer-text {
            margin-top: 22px;
            color: var(--text-light);
            font-size: 0.9rem;
        }

        .footer-text a {
            color: white;
            font-weight: bold;
            text-decoration: none;
        }

        .hint {
            text-align: left;
            color: rgba(248, 250, 252, 0.75);
            font-size: .82rem;
            margin-top: -6px;
            margin-bottom: 14px;
        }
    </style>
</head>
<body>
<div class="background">
    <div class="login-card">
        <div class="login-header">Register</div>
        <div class="sub">Creer un compte pour acceder a l'application</div>

        <form method="post" action="${pageContext.request.contextPath}/register" autocomplete="off">
            <div class="input-group">
                <i class="fas fa-user left-icon"></i>
                <input type="text" name="username" placeholder="Utilisateur" value="${usernameValue}" required>
            </div>

            <div class="input-group">
                <i class="fas fa-lock left-icon"></i>
                <input id="registerPassword" type="password" name="password" placeholder="Mot de passe" required>
                <button class="password-toggle" type="button" data-target="registerPassword" aria-label="Afficher ou masquer mot de passe">
                    <i class="fa-regular fa-eye"></i>
                </button>
            </div>
            <div class="hint">Min 6 caracteres. Le mot de passe est chiffre (BCrypt) en base.</div>

            <div class="input-group">
                <i class="fas fa-lock left-icon"></i>
                <input id="registerConfirmPassword" type="password" name="confirm_password" placeholder="Confirmer mot de passe" required>
                <button class="password-toggle" type="button" data-target="registerConfirmPassword" aria-label="Afficher ou masquer confirmation mot de passe">
                    <i class="fa-regular fa-eye"></i>
                </button>
            </div>

            <c:if test="${not empty registerError}">
                <div class="field-feedback error">
                    <i class="fas fa-exclamation-circle"></i>
                    ${registerError}
                </div>
            </c:if>

            <button type="submit" class="btn-login">Creer le compte</button>
        </form>

        <div class="footer-text">
            Deja un compte ? <a href="${pageContext.request.contextPath}/login">Se connecter</a>
        </div>
    </div>
</div>
<script>
    document.querySelectorAll(".password-toggle").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var input = document.getElementById(btn.getAttribute("data-target"));
            if (!input) return;
            var icon = btn.querySelector("i");
            var hidden = input.type === "password";
            input.type = hidden ? "text" : "password";
            icon.classList.toggle("fa-eye", !hidden);
            icon.classList.toggle("fa-eye-slash", hidden);
        });
    });
</script>
</body>
</html>

