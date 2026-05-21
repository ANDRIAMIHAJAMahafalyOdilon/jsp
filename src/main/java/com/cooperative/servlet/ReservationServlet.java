package com.cooperative.servlet;

import com.cooperative.dao.ClientDAO;
import com.cooperative.dao.ReservationDAO;
import com.cooperative.dao.VoitureDAO;
import com.cooperative.model.Reservation;
import com.cooperative.model.ReservationView;
import com.cooperative.model.Voiture;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@WebServlet("/reservations")
public class ReservationServlet extends HttpServlet {
    private static final DateTimeFormatter DATE_RESERV_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final VoitureDAO voitureDAO = new VoitureDAO();
    private final ClientDAO clientDAO = new ClientDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String filterIdVoit = safe(req.getParameter("idvoit"));
        String placesIdVoit = safe(req.getParameter("placesIdVoit"));
        String paiement = safe(req.getParameter("paiementFilter"));
        String keyword = safe(req.getParameter("keyword"));
        String dateFromRaw = safe(req.getParameter("dateFrom"));
        String dateToRaw = safe(req.getParameter("dateTo"));
        String sort = safe(req.getParameter("sort"));
        String recentId = safe(req.getParameter("recentId"));

        try {
            if ("checkId".equals(action)) {
                String id = safe(req.getParameter("id"));
                boolean exists = !id.isBlank() && reservationDAO.findById(id) != null;
                resp.setContentType("application/json;charset=UTF-8");
                resp.getWriter().write("{\"exists\":" + exists + "}");
                return;
            }
            if ("delete".equals(action)) {
                reservationDAO.delete(req.getParameter("id"));
                setFlash(req, "success", "Reservation supprimee et place liberee.");
                resp.sendRedirect(req.getContextPath() + "/reservations");
                return;
            }

            int page = getPage(req);
            int size = 6;
            if (!recentId.isBlank()) {
                // Force "recent first" view right after create/update.
                sort = "";
            }
            LocalDate dateFrom = parseOptionalDate(dateFromRaw, "Date debut invalide.");
            LocalDate dateTo = parseOptionalDate(dateToRaw, "Date fin invalide.");
            List<ReservationView> reservations = reservationDAO.findForList(blankToNull(filterIdVoit), paiement, keyword, dateFrom, dateTo, sort);
            if (!recentId.isBlank()) {
                reservations.sort((a, b) -> {
                    boolean aRecent = Objects.equals(recentId, a.getIdReserv());
                    boolean bRecent = Objects.equals(recentId, b.getIdReserv());
                    if (aRecent == bRecent) return 0;
                    return aRecent ? -1 : 1;
                });
                page = 1;
            }
            req.setAttribute("reservations", paginate(reservations, page, size));
            req.setAttribute("currentPage", page);
            req.setAttribute("totalPages", totalPages(reservations.size(), size));
            req.setAttribute("voitures", voitureDAO.findAll());
            req.setAttribute("clients", clientDAO.findAll());
            req.setAttribute("paiementFilter", paiement);
            req.setAttribute("keyword", keyword);
            req.setAttribute("dateFrom", dateFromRaw);
            req.setAttribute("dateTo", dateToRaw);
            req.setAttribute("sort", sort);
            req.setAttribute("selectedVoitureFilter", filterIdVoit);

            String selectedPlacesVoiture = placesIdVoit;
            if (selectedPlacesVoiture.isBlank()) {
                selectedPlacesVoiture = filterIdVoit;
            }
            req.setAttribute("selectedVoiturePlaces", selectedPlacesVoiture);

            if (!selectedPlacesVoiture.isBlank()) {
                req.setAttribute("placesLibres", voitureDAO.findFreePlaces(selectedPlacesVoiture));
            }

            if ("edit".equals(action)) {
                Reservation r = reservationDAO.findById(req.getParameter("id"));
                req.setAttribute("reservationEdit", r);
                req.setAttribute("placesLibres", voitureDAO.findFreePlaces(r.getIdVoit()));
                req.setAttribute("selectedVoiturePlaces", r.getIdVoit());
            }

            req.getRequestDispatcher("/WEB-INF/views/reservations/list.jsp").forward(req, resp);
        } catch (IllegalArgumentException e) {
            setFlash(req, "danger", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/reservations");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        Reservation reservation = new Reservation();
        String idReservRaw = safe(req.getParameter("idreserv"));
        String placesRaw = safe(req.getParameter("places"));

        try {
            if ("update".equals(action)) {
                if (idReservRaw.isBlank()) {
                    throw new FieldValidationException("ID reservation obligatoire.", "idreserv");
                }
                reservation.setIdReserv(idReservRaw);
            } else {
                if (idReservRaw.isBlank()) {
                    throw new FieldValidationException("ID reservation obligatoire.", "idreserv");
                }
                if (reservationDAO.findById(idReservRaw) != null) {
                    throw new FieldValidationException("Cet ID reservation existe deja.", "idreserv");
                }
                reservation.setIdReserv(idReservRaw);
            }

            reservation.setIdVoit(requireNonBlank(req.getParameter("idvoit"), "Voiture obligatoire."));
            reservation.setIdCli(requireNonBlank(req.getParameter("idcli"), "Client obligatoire."));
            String rawPlace = req.getParameter("place");
            if (rawPlace != null && !rawPlace.trim().isBlank()) {
                reservation.setPlace(parsePositiveInt(rawPlace, "Place invalide."));
            } else {
                reservation.setPlace(1);
            }
            String rawDateReserv = req.getParameter("date_reserv");
            if ("update".equals(action)) {
                if (rawDateReserv == null || rawDateReserv.trim().isEmpty()) {
                    Reservation existing = reservationDAO.findById(reservation.getIdReserv());
                    if (existing == null || existing.getDateReserv() == null) {
                        throw new IllegalArgumentException("Date reservation introuvable pour cette modification.");
                    }
                    reservation.setDateReserv(existing.getDateReserv());
                } else {
                    reservation.setDateReserv(parseDateTime(rawDateReserv));
                }
            } else {
                // Creation: reservation timestamp is automatic if not provided by UI.
                reservation.setDateReserv(
                        (rawDateReserv == null || rawDateReserv.trim().isEmpty())
                                ? LocalDateTime.now()
                                : parseDateTime(rawDateReserv)
                );
            }
            reservation.setDateVoyage(LocalDate.parse(requireNonBlank(req.getParameter("date_voyage"), "Date voyage obligatoire.")));
            reservation.setPaiement(normalizePaiement(req.getParameter("paiement")));
            applyMontantAvanceForPaiement(reservation, req.getParameter("montant_avance"));
            if (reservation.getDateVoyage().isBefore(reservation.getDateReserv().toLocalDate())) {
                throw new IllegalArgumentException("La date voyage doit etre >= date reservation.");
            }

            if ("update".equals(action)) {
                reservation.setPlace(parsePositiveInt(req.getParameter("place"), "Place invalide."));
                String oldVoit = req.getParameter("old_idvoit");
                int oldPlace = Integer.parseInt(req.getParameter("old_place"));
                reservationDAO.update(reservation, oldVoit, oldPlace);
                setFlash(req, "success", "Reservation modifiee avec succes.");
                resp.sendRedirect(req.getContextPath() + "/reservations?recentId=" + encode(reservation.getIdReserv()));
            } else {
                List<Integer> selectedPlaces = parsePlaces(req.getParameter("places"));
                if (selectedPlaces.isEmpty()) {
                    selectedPlaces.add(parsePositiveInt(req.getParameter("place"), "Selectionne au moins une place."));
                }
                String firstCreatedId = "";
                for (int i = 0; i < selectedPlaces.size(); i++) {
                    Reservation item = new Reservation();
                    String id = selectedPlaces.size() == 1
                            ? reservation.getIdReserv()
                            : reservation.getIdReserv() + "-P" + selectedPlaces.get(i);
                    if (reservationDAO.findById(id) != null) {
                        throw new FieldValidationException("Cet ID reservation existe deja.", "idreserv");
                    }
                    item.setIdReserv(id);
                    item.setIdVoit(reservation.getIdVoit());
                    item.setIdCli(reservation.getIdCli());
                    item.setPlace(selectedPlaces.get(i));
                    item.setDateReserv(reservation.getDateReserv());
                    item.setDateVoyage(reservation.getDateVoyage());
                    item.setPaiement(reservation.getPaiement());
                    item.setMontantAvance(reservation.getMontantAvance());
                    reservationDAO.create(item);
                    if (firstCreatedId.isBlank()) {
                        firstCreatedId = id;
                    }
                }
                setFlash(req, "success", selectedPlaces.size() > 1
                        ? ("Reservations ajoutees avec succes (" + selectedPlaces.size() + " places).")
                        : "Reservation ajoutee avec succes.");
                resp.sendRedirect(req.getContextPath() + "/reservations?recentId=" + encode(firstCreatedId));
            }
        } catch (FieldValidationException e) {
            forwardWithFormError(req, resp, action, reservation, placesRaw, e.getMessage(), e.getField());
        } catch (SQLIntegrityConstraintViolationException e) {
            forwardWithFormError(req, resp, action, reservation, placesRaw, "Cet ID reservation existe deja.", "idreserv");
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if ("update".equals(action)) {
                setFlash(req, "danger", message);
                resp.sendRedirect(req.getContextPath() + "/reservations?action=edit&id=" + encode(reservation.getIdReserv()));
                return;
            }
            String field = null;
            if (message != null && (message.contains("montant") || message.contains("avance") || message.contains("paye"))) {
                field = "montant_avance";
            }
            prepareFormError(req, action, reservation, placesRaw, message, field);
            if (field == null && message != null && message.contains("date voyage")) {
                req.setAttribute("dateVoyageError", message);
            } else if (field == null && message != null && (message.contains("Date reservation") || message.contains("Format Reservation"))) {
                req.setAttribute("dateReservError", message);
            }
            doGet(req, resp);
        } catch (SQLException e) {
            String msg = safe(e.getMessage());
            String field = null;
            if (msg.contains("montant") || msg.contains("avance") || msg.contains("paye") || msg.contains("payé")) {
                field = "montant_avance";
            }
            if ("update".equals(action)) {
                setFlash(req, "danger", "Erreur base de donnees: " + msg);
                resp.sendRedirect(req.getContextPath() + "/reservations");
            } else {
                forwardWithFormError(req, resp, action, reservation, placesRaw, msg, field);
            }
        }
    }

    private void forwardWithFormError(HttpServletRequest req, HttpServletResponse resp, String action,
                                      Reservation reservation, String placesRaw, String fieldError, String fieldName)
            throws ServletException, IOException {
        prepareFormError(req, action, reservation, placesRaw, fieldError, fieldName);
        doGet(req, resp);
    }

    private void prepareFormError(HttpServletRequest req, String action, Reservation reservation,
                                  String placesRaw, String fieldError, String fieldName) {
        if ("update".equals(action)) {
            req.setAttribute("reservationEdit", reservation);
        } else {
            req.setAttribute("reservationForm", reservation);
            req.setAttribute("selectedPlacesCsv", placesRaw == null ? "" : placesRaw);
            req.setAttribute("openReservationDrawer", true);
        }
        if (fieldError != null && !fieldError.isBlank()) {
            if ("idreserv".equals(fieldName)) {
                req.setAttribute("idReservError", fieldError);
            } else if ("montant_avance".equals(fieldName)) {
                req.setAttribute("montantAvanceError", fieldError);
            } else {
                req.setAttribute("errorMessage", fieldError);
            }
        }
        req.setAttribute("formAction", action == null ? "create" : action);
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

    private List<ReservationView> paginate(List<ReservationView> data, int page, int size) {
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

    private List<Integer> parsePlaces(String raw) {
        List<Integer> out = new ArrayList<>();
        if (raw == null || raw.trim().isBlank()) {
            return out;
        }
        String[] parts = raw.split(",");
        for (String p : parts) {
            String s = p == null ? "" : p.trim();
            if (s.isBlank()) continue;
            int v = parsePositiveInt(s, "Place invalide.");
            if (!out.contains(v)) {
                out.add(v);
            }
        }
        return out;
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

    private LocalDateTime parseDateTime(String raw) {
        String value = requireNonBlank(raw, "Date reservation obligatoire.");
        String normalized = value.trim().replace("T", " ");
        if (normalized.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {
            normalized = normalized + ":00";
        }
        if (!normalized.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
            throw new IllegalArgumentException("Format Reservation attendu: yyyy-MM-dd HH:mm:ss");
        }
        try {
            return LocalDateTime.parse(normalized, DATE_RESERV_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Format Reservation attendu: yyyy-MM-dd HH:mm:ss");
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private String normalizePaiement(String raw) {
        String value = requireNonBlank(raw, "Paiement obligatoire.");
        String compact = value.trim().toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a");

        if ("sans avance".equals(compact) || compact.startsWith("sans")) return "Sans avance";
        if ("avec avance".equals(compact) || compact.startsWith("avec")) return "Avec avance";
        if ("tout paye".equals(compact) || compact.contains("tout pay")
                || compact.contains("payã©") || (compact.contains("tout") && compact.contains("pay"))) {
            return "Tout payé";
        }

        throw new IllegalArgumentException("Paiement invalide.");
    }

    private void applyMontantAvanceForPaiement(Reservation reservation, String rawMontant) throws SQLException {
        Voiture v = voitureDAO.findById(reservation.getIdVoit());
        if (v == null) {
            throw new IllegalArgumentException("Voiture introuvable.");
        }
        int frais = v.getFrais();
        String paiement = reservation.getPaiement();
        if ("Sans avance".equals(paiement)) {
            reservation.setMontantAvance(0);
        } else if ("Tout payé".equals(paiement)) {
            reservation.setMontantAvance(frais);
        } else {
            if (frais <= 1) {
                throw new IllegalArgumentException("Pour cette voiture, choisissez 'Tout paye'.");
            }
            int avance = parseNonNegativeInt(rawMontant, "Montant avance invalide.");
            if (avance <= 0 || avance >= frais) {
                throw new IllegalArgumentException(
                        "Avec 'Avec avance', le montant doit etre entre 1 et " + (frais - 1) + " Ar.");
            }
            reservation.setMontantAvance(avance);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private LocalDate parseOptionalDate(String value, String message) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(message);
        }
    }

    private void setFlash(HttpServletRequest req, String type, String message) {
        HttpSession session = req.getSession();
        session.setAttribute("flashType", type);
        session.setAttribute("flashMessage", message);
    }

    private String encode(String value) {
        if (value == null) return "";
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
