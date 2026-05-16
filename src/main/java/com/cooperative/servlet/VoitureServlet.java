package com.cooperative.servlet;

import com.cooperative.dao.VoitureDAO;
import com.cooperative.model.Voiture;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@WebServlet("/voitures")
public class VoitureServlet extends HttpServlet {
    private final VoitureDAO voitureDAO = new VoitureDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String recentId = safe(req.getParameter("recentId"));
        try {
            if ("checkId".equals(action)) {
                String id = safe(req.getParameter("id"));
                boolean exists = !id.isBlank() && voitureDAO.findById(id) != null;
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"exists\":" + exists + "}");
                return;
            }
            if ("delete".equals(action)) {
                voitureDAO.delete(req.getParameter("id"));
                setFlash(req, "success", "Voiture supprimee avec succes.");
                resp.sendRedirect(req.getContextPath() + "/voitures");
                return;
            }
            if ("edit".equals(action)) {
                req.setAttribute("voitureEdit", voitureDAO.findById(req.getParameter("id")));
            }
            forwardList(req, resp, recentId);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        Voiture v = new Voiture();
        v.setIdVoit(safe(req.getParameter("idvoit")));
        v.setDesign(safe(req.getParameter("design")));
        v.setType(safe(req.getParameter("type")));
        try {
            v.setNbrPlace(parsePositiveInt(req.getParameter("nbrplace"), "Nombre de places invalide."));
            v.setFrais(parseNonNegativeInt(req.getParameter("frais"), "Frais invalide."));
            if (v.getDesign().isBlank()) {
                throw new FieldValidationException("Design voiture obligatoire.", null);
            }
            if (!"Simple".equals(v.getType()) && !"Premium".equals(v.getType()) && !"VIP".equals(v.getType())) {
                throw new FieldValidationException("Type voiture invalide.", null);
            }
            if ("update".equals(action)) {
                if (v.getIdVoit().isBlank()) {
                    throw new FieldValidationException("ID voiture manquant pour la modification.", null);
                }
                voitureDAO.update(v);
                setFlash(req, "success", "Voiture modifiee avec succes.");
                resp.sendRedirect(req.getContextPath() + "/voitures?recentId=" + encode(v.getIdVoit()));
            } else {
                if (v.getIdVoit().isBlank()) {
                    throw new FieldValidationException("ID voiture obligatoire.", "idvoit");
                }
                if (voitureDAO.findById(v.getIdVoit()) != null) {
                    throw new FieldValidationException("Cet ID voiture existe deja.", "idvoit");
                }
                voitureDAO.create(v);
                setFlash(req, "success", "Voiture ajoutee avec succes.");
                resp.sendRedirect(req.getContextPath() + "/voitures?recentId=" + encode(v.getIdVoit()));
            }
        } catch (FieldValidationException e) {
            if ("update".equals(action)) {
                setFlash(req, "danger", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/voitures?action=edit&id=" + encode(v.getIdVoit()));
            } else {
                req.setAttribute("voitureForm", v);
                req.setAttribute("idVoitError", e.getMessage());
                req.setAttribute("idVoitErrorField", e.getField());
                req.setAttribute("openVoitureDrawer", true);
                try {
                    forwardList(req, resp, "");
                } catch (SQLException ex) {
                    throw new ServletException(ex);
                }
            }
        } catch (SQLIntegrityConstraintViolationException e) {
            req.setAttribute("voitureForm", v);
            req.setAttribute("idVoitError", "Cet ID voiture existe deja.");
            req.setAttribute("idVoitErrorField", "idvoit");
            req.setAttribute("openVoitureDrawer", true);
            try {
                forwardList(req, resp, "");
            } catch (SQLException ex) {
                throw new ServletException(ex);
            }
        } catch (IllegalArgumentException e) {
            if ("update".equals(action)) {
                setFlash(req, "danger", e.getMessage());
                resp.sendRedirect(req.getContextPath() + "/voitures");
            } else {
                req.setAttribute("voitureForm", v);
                req.setAttribute("openVoitureDrawer", true);
                try {
                    forwardList(req, resp, "");
                } catch (SQLException ex) {
                    throw new ServletException(ex);
                }
            }
        } catch (SQLException e) {
            if ("update".equals(action)) {
                setFlash(req, "danger", "Erreur base de donnees: " + safe(e.getMessage()));
                resp.sendRedirect(req.getContextPath() + "/voitures");
            } else {
                req.setAttribute("voitureForm", v);
                req.setAttribute("openVoitureDrawer", true);
                try {
                    forwardList(req, resp, "");
                } catch (SQLException ex) {
                    throw new ServletException(ex);
                }
            }
        }
    }

    private void forwardList(HttpServletRequest req, HttpServletResponse resp, String recentId) throws SQLException, ServletException, IOException {
        int page = getPage(req);
        int size = 5;
        List<Voiture> voitures = voitureDAO.findAll();
        if (!recentId.isBlank()) {
            voitures.sort((a, b) -> {
                boolean aRecent = Objects.equals(recentId, a.getIdVoit());
                boolean bRecent = Objects.equals(recentId, b.getIdVoit());
                if (aRecent == bRecent) return 0;
                return aRecent ? -1 : 1;
            });
            page = 1;
        }
        req.setAttribute("voitures", paginate(voitures, page, size));
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages(voitures.size(), size));
        req.getRequestDispatcher("/WEB-INF/views/voitures/list.jsp").forward(req, resp);
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

    private List<Voiture> paginate(List<Voiture> data, int page, int size) {
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

    private int parsePositiveInt(String raw, String message) {
        String v = raw == null ? "" : raw.trim();
        if (v.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        int value;
        try {
            value = Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private int parseNonNegativeInt(String raw, String message) {
        String v = raw == null ? "" : raw.trim();
        if (v.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        int value;
        try {
            value = Integer.parseInt(v);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(message);
        }
        if (value < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        if (value == null) return "";
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void setFlash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }
}
