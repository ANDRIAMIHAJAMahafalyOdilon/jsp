package com.cooperative.servlet;

import com.cooperative.dao.UserDAO;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String username = safe(req.getParameter("username"));
        String password = safe(req.getParameter("password"));
        String confirm = safe(req.getParameter("confirm_password"));

        try {
            String error = validate(username, password, confirm);
            if (error != null) {
                req.setAttribute("registerError", error);
                req.setAttribute("usernameValue", username);
                doGet(req, resp);
                return;
            }

            if (userDAO.existsByUsername(username)) {
                req.setAttribute("registerError", "Ce nom d'utilisateur existe deja.");
                req.setAttribute("usernameValue", username);
                doGet(req, resp);
                return;
            }

            String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
            userDAO.create(username, hash);
            resp.sendRedirect(req.getContextPath() + "/login?registered=1");
        } catch (SQLException e) {
            req.setAttribute("registerError", "Erreur base de donnees: " + e.getMessage());
            req.setAttribute("usernameValue", username);
            doGet(req, resp);
        }
    }

    private String validate(String username, String password, String confirm) {
        if (username.isBlank()) return "Utilisateur obligatoire.";
        if (username.length() < 3) return "Utilisateur trop court (min 3 caracteres).";
        if (username.length() > 60) return "Utilisateur trop long (max 60 caracteres).";
        if (password.isBlank()) return "Mot de passe obligatoire.";
        if (password.length() < 6) return "Mot de passe trop court (min 6 caracteres).";
        if (!password.equals(confirm)) return "Les mots de passe ne correspondent pas.";
        return null;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

