package com.cooperative.servlet;

import com.cooperative.dao.ReservationDAO;
import com.cooperative.dao.VoitureDAO;
import com.cooperative.model.ReservationView;
import com.cooperative.model.Voiture;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final VoitureDAO voitureDAO = new VoitureDAO();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("paiementStats", reservationDAO.getPaiementStats());
            req.setAttribute("totalRecette", reservationDAO.getTotalRecette());
            req.setAttribute("totalVoyageurs", reservationDAO.getTotalVoyageurs());
            req.setAttribute("resteAPayer", reservationDAO.getResteAPayerTotal());
            List<Voiture> voitures = voitureDAO.findAll();
            req.setAttribute("voitures", voitures);

            String payCat = safe(req.getParameter("payCat"));
            List<ReservationView> travellers = reservationDAO.findByPaiementCategory(payCat);
            String payCatLabel = "";
            if ("avance".equalsIgnoreCase(payCat)) {
                payCatLabel = "Avance avec reste a payer";
            } else if ("non".equalsIgnoreCase(payCat)) {
                payCatLabel = "Pas encore paye";
            } else if ("tout".equalsIgnoreCase(payCat)) {
                payCatLabel = "Tout paye";
            }
            req.setAttribute("payCat", payCat);
            req.setAttribute("payCatLabel", payCatLabel);
            req.setAttribute("travellersCount", travellers.size());
            req.setAttribute("travellersByPaiement", travellers);

            String selectedVoiture = req.getParameter("idvoit");
            if ((selectedVoiture == null || selectedVoiture.isBlank()) && !voitures.isEmpty()) {
                selectedVoiture = voitures.get(0).getIdVoit();
            }
            req.setAttribute("selectedVoiture", selectedVoiture == null ? "" : selectedVoiture);
            if (selectedVoiture != null && !selectedVoiture.isBlank()) {
                List<Integer> placesLibres = voitureDAO.findFreePlaces(selectedVoiture);
                req.setAttribute("placesLibres", placesLibres);
                final String finalSelectedVoiture = selectedVoiture;
                Voiture selectedV = voitures.stream()
                        .filter(v -> v.getIdVoit().equals(finalSelectedVoiture))
                        .findFirst().orElse(null);
                req.setAttribute("selectedVoitureNbrPlace", selectedV != null ? selectedV.getNbrPlace() : 0);

                // String helper to test quickly if a place is free in JSP.
                List<String> parts = new ArrayList<>();
                for (Integer p : placesLibres) {
                    parts.add(String.valueOf(p));
                }
                req.setAttribute("freePlacesCsv", "," + String.join(",", parts) + ",");
            } else {
                req.setAttribute("placesLibres", Collections.emptyList());
                req.setAttribute("selectedVoitureNbrPlace", 0);
                req.setAttribute("freePlacesCsv", ",");
            }
            req.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
