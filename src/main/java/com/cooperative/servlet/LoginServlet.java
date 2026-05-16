package com.cooperative.servlet;

import com.cooperative.dao.UserDAO;
import com.cooperative.model.User;
import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String username = safe(req.getParameter("username"));
        String password = safe(req.getParameter("password"));
        req.setAttribute("usernameValue", username);

        if (username.isBlank()) {
            req.setAttribute("usernameError", "Le nom d'utilisateur est obligatoire.");
            doGet(req, resp);
            return;
        }
        if (password.isBlank()) {
            req.setAttribute("passwordError", "Le mot de passe est obligatoire.");
            doGet(req, resp);
            return;
        }

        try {
            User u = userDAO.findByUsername(username);
            if (u == null) {
                req.setAttribute("usernameError", "Utilisateur introuvable.");
                doGet(req, resp);
                return;
            }

            if (!BCrypt.checkpw(password, u.getPasswordHash())) {
                req.setAttribute("passwordError", "Mot de passe incorrect.");
                doGet(req, resp);
                return;
            }

            {
                HttpSession session = req.getSession(true);
                session.setAttribute("authUser", u.getUsername());
                resp.sendRedirect(req.getContextPath() + "/dashboard");
                return;
            }
        } catch (SQLException e) {
            req.setAttribute("dbError", "Erreur base de donnees: " + e.getMessage());
            doGet(req, resp);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
