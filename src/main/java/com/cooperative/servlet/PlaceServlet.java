package com.cooperative.servlet;

import com.cooperative.dao.VoitureDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/places")
public class PlaceServlet extends HttpServlet {

    private final VoitureDAO voitureDAO = new VoitureDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String action = safe(req.getParameter("action"));
        if ("add".equals(action)) {
            handleAdd(req, resp);
            return;
        }
        if ("delete".equals(action)) {
            handleDelete(req, resp);
            return;
        }
        writeJson(resp, 400, "{\"ok\":false,\"message\":\"Action invalide.\"}");
    }

    private void handleAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idVoit = safe(req.getParameter("idvoit"));
        Integer startPlace = parseOptionalPositiveInt(req.getParameter("startPlace"));
        int count;
        try {
            count = parsePositiveInt(req.getParameter("count"));
        } catch (IOException ex) {
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"Nombre de places invalide.\"}");
            return;
        }

        try {
            List<Integer> freePlaces = voitureDAO.addPlaces(idVoit, startPlace, count);
            String json = "{\"ok\":true,\"message\":\"Places ajoutees avec succes.\",\"freePlaces\":" + toJsonArray(freePlaces) + "}";
            writeJson(resp, 200, json);
        } catch (SQLException e) {
            String msg = escapeJson(e.getMessage() == null ? "Erreur base de donnees." : e.getMessage());
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"" + msg + "\"}");
        }
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String idVoit = safe(req.getParameter("idvoit"));
        int place;
        try {
            place = parsePositiveInt(req.getParameter("place"));
        } catch (IOException ex) {
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"Numero de place invalide.\"}");
            return;
        }

        try {
            List<Integer> freePlaces = voitureDAO.deleteFreePlace(idVoit, place);
            String json = "{\"ok\":true,\"message\":\"Place supprimee avec succes.\",\"freePlaces\":" + toJsonArray(freePlaces) + "}";
            writeJson(resp, 200, json);
        } catch (SQLException e) {
            String msg = escapeJson(e.getMessage() == null ? "Erreur base de donnees." : e.getMessage());
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"" + msg + "\"}");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = safe(req.getParameter("action"));
        if (!"list".equals(action)) {
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"Action invalide.\"}");
            return;
        }

        String idVoit = safe(req.getParameter("idvoit"));
        if (idVoit.isBlank()) {
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"Voiture obligatoire.\"}");
            return;
        }
        try {
            List<Integer> freePlaces = voitureDAO.findFreePlaces(idVoit);
            String json = "{\"ok\":true,\"freePlaces\":" + toJsonArray(freePlaces) + "}";
            writeJson(resp, 200, json);
        } catch (SQLException e) {
            String msg = escapeJson(e.getMessage() == null ? "Erreur base de donnees." : e.getMessage());
            writeJson(resp, 400, "{\"ok\":false,\"message\":\"" + msg + "\"}");
        }
    }

    private int parsePositiveInt(String raw) throws IOException {
        try {
            int v = Integer.parseInt(raw);
            if (v <= 0) throw new NumberFormatException();
            return v;
        } catch (Exception e) {
            throw new IOException("Nombre invalide.");
        }
    }

    private Integer parseOptionalPositiveInt(String raw) {
        try {
            if (raw == null || raw.trim().isEmpty()) return null;
            int v = Integer.parseInt(raw.trim());
            if (v <= 0) return null;
            return v;
        } catch (Exception e) {
            return null;
        }
    }

    private String safe(String v) {
        return v == null ? "" : v.trim();
    }

    private void writeJson(HttpServletResponse resp, int status, String json) throws IOException {
        resp.setStatus(status);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(json);
    }

    private String toJsonArray(List<Integer> nums) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < nums.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(nums.get(i));
        }
        sb.append(']');
        return sb.toString();
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}

