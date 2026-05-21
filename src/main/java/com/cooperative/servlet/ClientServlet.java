package com.cooperative.servlet;

import com.cooperative.dao.ClientDAO;
import com.cooperative.model.Client;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/clients")
public class ClientServlet extends HttpServlet {
    private static final Pattern ID_CLIENT_PATTERN = Pattern.compile("[A-Za-z0-9\\-_]{1,30}");
    private final ClientDAO clientDAO = new ClientDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String search = req.getParameter("search");

        try {
            if ("checkId".equals(action)) {
                String id = safe(req.getParameter("id"));
                boolean exists = !id.isBlank() && clientDAO.exists(id);
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"exists\":" + exists + "}");
                return;
            }
            if ("delete".equals(action)) {
                clientDAO.delete(requireNonBlank(req.getParameter("id"), "ID client manquant."));
                setFlash(req, "success", "Client supprime avec succes.");
                resp.sendRedirect(req.getContextPath() + "/clients");
                return;
            }
            if ("edit".equals(action)) {
                req.setAttribute("clientEdit", clientDAO.findById(requireNonBlank(req.getParameter("id"), "ID client manquant.")));
            }
            forwardList(req, resp, search);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        Client c = new Client();
        c.setNom(safe(req.getParameter("nom")));
        c.setNumTel(safe(req.getParameter("numtel")));

        try {
            if (c.getNom().isBlank() || c.getNumTel().isBlank()) {
                throw new FieldValidationException("Nom et telephone sont obligatoires.", null);
            }
            if (!c.getNom().matches("[A-Za-zÀ-ÖØ-öø-ÿ\\s'\\-]{2,50}")) {
                throw new FieldValidationException("Nom invalide (lettres uniquement).", null);
            }
            if (!c.getNumTel().matches("[0-9]{6,20}")) {
                throw new FieldValidationException("Telephone invalide (chiffres uniquement).", null);
            }
            String idCli = requireNonBlank(req.getParameter("idcli"), "ID client obligatoire.");
            if (!ID_CLIENT_PATTERN.matcher(idCli).matches()) {
                throw new FieldValidationException(
                        "ID client invalide (lettres, chiffres, tiret ou underscore, 1 a 30 caracteres).", "idcli");
            }
            if ("update".equals(action)) {
                c.setIdCli(idCli);
                clientDAO.update(c);
                setFlash(req, "success", "Client modifie avec succes.");
                resp.sendRedirect(req.getContextPath() + "/clients");
            } else {
                c.setIdCli(idCli);
                if (clientDAO.exists(c.getIdCli())) {
                    throw new FieldValidationException("Cet ID client existe deja.", "idcli");
                }
                clientDAO.create(c);
                setFlash(req, "success", "Client ajoute avec succes.");
                resp.sendRedirect(req.getContextPath() + "/clients");
            }
        } catch (FieldValidationException e) {
            if ("update".equals(action)) {
                setFlash(req, "danger", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/clients?action=edit&id=" + encode(c.getIdCli()));
            } else {
                req.setAttribute("clientForm", c);
                req.setAttribute("idCliError", e.getMessage());
                req.setAttribute("idCliErrorField", e.getField());
                req.setAttribute("openClientDrawer", true);
                try {
                    forwardList(req, resp, null);
                } catch (SQLException ex) {
                    throw new ServletException(ex);
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            req.setAttribute("clientForm", c);
            req.setAttribute("idCliError", "Cet ID client existe deja.");
            req.setAttribute("idCliErrorField", "idcli");
            req.setAttribute("openClientDrawer", true);
            try {
                forwardList(req, resp, null);
            } catch (SQLException ex) {
                throw new ServletException(ex);
            }
        } catch (IllegalArgumentException e) {
            if ("update".equals(action)) {
                setFlash(req, "danger", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/clients");
            } else {
                req.setAttribute("clientForm", c);
                req.setAttribute("openClientDrawer", true);
                try {
                    forwardList(req, resp, null);
                } catch (SQLException ex) {
                    throw new ServletException(ex);
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private void forwardList(HttpServletRequest req, HttpServletResponse resp, String search) throws SQLException, ServletException, IOException {
        int page = getPage(req);
        int size = 5;
        List<Client> clients = (search != null && !search.isBlank())
                ? clientDAO.searchByNameOrPhone(search)
                : clientDAO.findAll();

        req.setAttribute("search", search == null ? "" : search);
        req.setAttribute("clients", paginate(clients, page, size));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages(clients.size(), size));
        req.getRequestDispatcher("/WEB-INF/views/clients/list.jsp").forward(req, resp);
    }

    private static class FieldValidationException extends Exception {
        private final String field;

        FieldValidationException(String message, String field) {
            super(message);
            this.field = field;
        }

        String getField() {
            return field;
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String encode(String value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private List<Client> paginate(List<Client> data, int page, int size) {
        if (data.isEmpty()) {
            return Collections.emptyList();
        }
        int from = Math.max(0, (page - 1) * size);
        int to = Math.min(data.size(), from + size);
        if (from >= to) {
            from = Math.max(0, (totalPages(data.size(), size) - 1) * size);
            to = Math.min(data.size(), from + size);
        }
        return data.subList(from, to);
    }

    private int totalPages(int totalItems, int size) {
        return Math.max(1, (int) Math.ceil((double) totalItems / size));
    }

    private int getPage(HttpServletRequest req) {
        try {
            return Math.max(1, Integer.parseInt(req.getParameter("page")));
        } catch (Exception e) {
            return 1;
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private void setFlash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}
