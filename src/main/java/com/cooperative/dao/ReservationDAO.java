package com.cooperative.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cooperative.model.PaiementReport;
import com.cooperative.model.Reservation;
import com.cooperative.model.ReservationReceiptData;
import com.cooperative.model.ReservationView;
import com.cooperative.model.Voiture;
import com.cooperative.util.DBConnection;
import com.cooperative.util.DateFormatUtil;

public class ReservationDAO {

    public void create(Reservation reservation) throws SQLException {
        validatePaymentRules(reservation);
        if (!isPlaceAvailable(reservation.getIdVoit(), reservation.getPlace(), null)) {
            throw new SQLException("La place selectionnee est deja occupee.");
        }
        String sql = "INSERT INTO reserver (idreserv, idvoit, idcli, place, date_reserv, date_voyage, paiement, montant_avance) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, reservation.getIdReserv());
            ps.setString(2, reservation.getIdVoit());
            ps.setInt(3, reservation.getIdCli());
            ps.setInt(4, reservation.getPlace());
            ps.setTimestamp(5, Timestamp.valueOf(reservation.getDateReserv()));
            ps.setDate(6, Date.valueOf(reservation.getDateVoyage()));
            ps.setString(7, reservation.getPaiement());
            ps.setInt(8, reservation.getMontantAvance());
            ps.executeUpdate();
        }
        new VoitureDAO().occupyPlace(reservation.getIdVoit(), reservation.getPlace());
    }

    public void update(Reservation reservation, String oldVoiture, int oldPlace) throws SQLException {
        validatePaymentRules(reservation);
        if (!isPlaceAvailable(reservation.getIdVoit(), reservation.getPlace(), reservation.getIdReserv())) {
            throw new SQLException("La place selectionnee est deja occupee.");
        }
        String sql = "UPDATE reserver SET idvoit=?, idcli=?, place=?, date_reserv=?, date_voyage=?, paiement=?, montant_avance=? WHERE idreserv=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, reservation.getIdVoit());
            ps.setInt(2, reservation.getIdCli());
            ps.setInt(3, reservation.getPlace());
            ps.setTimestamp(4, Timestamp.valueOf(reservation.getDateReserv()));
            ps.setDate(5, Date.valueOf(reservation.getDateVoyage()));
            ps.setString(6, reservation.getPaiement());
            ps.setInt(7, reservation.getMontantAvance());
            ps.setString(8, reservation.getIdReserv());
            ps.executeUpdate();
        }

        VoitureDAO voitureDAO = new VoitureDAO();
        if (!(oldVoiture.equals(reservation.getIdVoit()) && oldPlace == reservation.getPlace())) {
            voitureDAO.freePlace(oldVoiture, oldPlace);
            voitureDAO.occupyPlace(reservation.getIdVoit(), reservation.getPlace());
        }
    }

    public void delete(String idReserv) throws SQLException {
        Reservation existing = findById(idReserv);
        if (existing == null) {
            return;
        }
        String sql = "DELETE FROM reserver WHERE idreserv=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idReserv);
            ps.executeUpdate();
        }
        new VoitureDAO().freePlace(existing.getIdVoit(), existing.getPlace());
    }

    public List<Reservation> findAll() throws SQLException {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT idreserv, idvoit, idcli, place, date_reserv, date_voyage, paiement, montant_avance FROM reserver ORDER BY date_reserv DESC";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservation(rs));
            }
        }
        return list;
    }

    public List<ReservationView> findForList(String idVoit, String paiement, String keyword, LocalDate dateFrom, LocalDate dateTo, String sort) throws SQLException {
        List<ReservationView> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.idreserv, r.idvoit, v.type AS type_voiture, r.idcli, c.nom, c.numtel, r.place, r.date_reserv, r.date_voyage, "
                        + "r.paiement, r.montant_avance, v.frais "
                        + "FROM reserver r "
                        + "JOIN client c ON c.idcli = r.idcli "
                        + "JOIN voiture v ON v.idvoit = r.idvoit "
                        + "WHERE 1=1 "
        );
        List<Object> params = new ArrayList<>();

        if (idVoit != null && !idVoit.isBlank()) {
            sql.append("AND r.idvoit = ? ");
            params.add(idVoit.trim());
        }
        if (paiement != null && !paiement.isBlank()) {
            sql.append("AND r.paiement = ? ");
            params.add(paiement.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            sql.append("AND (c.nom LIKE ? OR c.numtel LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term);
            params.add(term);
        }
        if (dateFrom != null) {
            sql.append("AND r.date_voyage >= ? ");
            params.add(Date.valueOf(dateFrom));
        }
        if (dateTo != null) {
            sql.append("AND r.date_voyage <= ? ");
            params.add(Date.valueOf(dateTo));
        }

        if ("dateVoyageAsc".equals(sort)) {
            sql.append("ORDER BY r.date_voyage ASC, r.date_reserv DESC");
        } else if ("avanceDesc".equals(sort)) {
            sql.append("ORDER BY r.montant_avance DESC, r.date_reserv DESC");
        } else {
            sql.append("ORDER BY r.date_reserv DESC");
        }

        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservationView(rs));
                }
            }
        }
        return list;
    }

    public Reservation findById(String idReserv) throws SQLException {
        String sql = "SELECT idreserv, idvoit, idcli, place, date_reserv, date_voyage, paiement, montant_avance FROM reserver WHERE idreserv=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idReserv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReservation(rs);
                }
            }
        }
        return null;
    }

    public ReservationReceiptData findReceiptData(String idReserv) throws SQLException {
        String sql = "SELECT r.idreserv, r.date_reserv, r.date_voyage, c.nom, c.numtel, v.idvoit, v.type, r.place, v.frais, r.paiement, r.montant_avance "
                + "FROM reserver r "
                + "JOIN client c ON c.idcli = r.idcli "
                + "JOIN voiture v ON v.idvoit = r.idvoit "
                + "WHERE r.idreserv = ?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idReserv);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ReservationReceiptData data = new ReservationReceiptData();
                    data.setIdReserv(rs.getString("idreserv"));
                    java.time.LocalDateTime dateReserv = rs.getTimestamp("date_reserv").toLocalDateTime();
                    java.time.LocalDate dateVoyage = rs.getDate("date_voyage").toLocalDate();
                    data.setDateReserv(DateFormatUtil.formatFrenchDateTime(dateReserv));
                    data.setDateVoyage(DateFormatUtil.formatFrenchDate(dateVoyage));
                    data.setNomClient(rs.getString("nom"));
                    data.setNumTel(rs.getString("numtel"));
                    data.setIdVoiture(rs.getString("idvoit"));
                    data.setTypeVoiture(rs.getString("type"));
                    data.setPlace(rs.getInt("place"));
                    data.setFrais(rs.getInt("frais"));
                    data.setPaiement(rs.getString("paiement"));
                    data.setMontantAvance(rs.getInt("montant_avance"));
                    data.setReste(Math.max(0, data.getFrais() - data.getMontantAvance()));
                    return data;
                }
            }
        }
        return null;
    }

    public List<ReservationView> findByPaiementCategory(String payCat) throws SQLException {
        if (payCat == null || payCat.isBlank()) {
            return Collections.emptyList();
        }
        String sql = "SELECT r.idreserv, r.idvoit, v.type AS type_voiture, r.idcli, c.nom, c.numtel, r.place, "
                + "r.date_reserv, r.date_voyage, r.paiement, r.montant_avance, v.frais "
                + "FROM reserver r "
                + "JOIN client c ON c.idcli = r.idcli "
                + "JOIN voiture v ON v.idvoit = r.idvoit "
                + "WHERE "
                + categoryWhereClause(payCat)
                + " ORDER BY r.date_reserv DESC";
        List<ReservationView> list = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapReservationView(rs));
            }
        }
        return list;
    }

    private String categoryWhereClause(String payCat) {
        switch (payCat.toLowerCase()) {
            case "avance":
                return "r.paiement='Avec avance' AND r.montant_avance > 0";
            case "tout":
                return "r.paiement='Tout payé'";
            case "non":
                return "(r.paiement='Sans avance' OR (r.montant_avance = 0 AND r.paiement <> 'Tout payé'))";
            default:
                return "1=0";
        }
    }

    public List<PaiementReport> getPaiementStats() throws SQLException {
        List<PaiementReport> reports = new ArrayList<>();
        String sql = "SELECT categorie, COUNT(*) total FROM ("
                + "SELECT CASE "
                + "WHEN paiement='Tout payé' THEN 'Tout payé' "
                + "WHEN paiement='Avec avance' AND montant_avance > 0 THEN 'Avance avec reste a payer' "
                + "WHEN paiement='Sans avance' OR montant_avance = 0 THEN 'Pas encore paye' "
                + "ELSE 'Autre' END AS categorie "
                + "FROM reserver) t GROUP BY categorie";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                reports.add(new PaiementReport(rs.getString("categorie"), rs.getInt("total")));
            }
        }
        return reports;
    }

    public int getTotalRecette() throws SQLException {
        String sql = "SELECT COALESCE(SUM(montant_avance), 0) recette FROM reserver";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("recette");
            }
        }
        return 0;
    }

    public int getTotalVoyageurs() throws SQLException {
        String sql = "SELECT COUNT(*) total FROM reserver";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        }
        return 0;
    }

    public int getResteAPayerTotal() throws SQLException {
        String sql = "SELECT COALESCE(SUM(v.frais - r.montant_avance), 0) total_reste FROM reserver r JOIN voiture v ON v.idvoit = r.idvoit";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Math.max(0, rs.getInt("total_reste"));
            }
        }
        return 0;
    }

    public List<ReservationView> findLatestReservations(int limit) throws SQLException {
        List<ReservationView> list = new ArrayList<>();
        String sql = "SELECT r.idreserv, r.idvoit, v.type AS type_voiture, r.idcli, c.nom, c.numtel, r.place, r.date_reserv, r.date_voyage, "
                + "r.paiement, r.montant_avance, v.frais "
                + "FROM reserver r "
                + "JOIN client c ON c.idcli = r.idcli "
                + "JOIN voiture v ON v.idvoit = r.idvoit "
                + "ORDER BY r.date_reserv DESC, r.idreserv DESC LIMIT ?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapReservationView(rs));
                }
            }
        }
        return list;
    }

    public List<Voiture> findVoitures() throws SQLException {
        return new VoitureDAO().findAll();
    }

    private Reservation mapReservation(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setIdReserv(rs.getString("idreserv"));
        r.setIdVoit(rs.getString("idvoit"));
        r.setIdCli(rs.getInt("idcli"));
        r.setPlace(rs.getInt("place"));
        r.setDateReserv(rs.getTimestamp("date_reserv").toLocalDateTime());
        r.setDateVoyage(rs.getDate("date_voyage").toLocalDate());
        r.setPaiement(normalizePaiement(rs.getString("paiement")));
        r.setMontantAvance(rs.getInt("montant_avance"));
        return r;
    }

    private ReservationView mapReservationView(ResultSet rs) throws SQLException {
        ReservationView v = new ReservationView();
        v.setIdReserv(rs.getString("idreserv"));
        v.setIdVoit(rs.getString("idvoit"));
        v.setTypeVoiture(rs.getString("type_voiture"));
        v.setIdCli(rs.getInt("idcli"));
        v.setNomClient(rs.getString("nom"));
        v.setNumTel(rs.getString("numtel"));
        v.setPlace(rs.getInt("place"));
        v.setDateReserv(rs.getTimestamp("date_reserv").toLocalDateTime());
        v.setDateVoyage(rs.getDate("date_voyage").toLocalDate());
        v.setPaiement(normalizePaiement(rs.getString("paiement")));
        v.setMontantAvance(rs.getInt("montant_avance"));
        int frais = rs.getInt("frais");
        v.setFrais(frais);
        v.setReste(Math.max(0, frais - v.getMontantAvance()));
        return v;
    }

    private String normalizePaiement(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        String compact = value.toLowerCase()
                .replace("é", "e")
                .replace("è", "e")
                .replace("ê", "e")
                .replace("à", "a");
        if ("tout paye".equals(compact) || compact.contains("payã©")) return "Tout payé";
        if ("avec avance".equals(compact)) return "Avec avance";
        if ("sans avance".equals(compact)) return "Sans avance";
        return value;
    }

    private void validatePaymentRules(Reservation reservation) throws SQLException {
        int frais = findFraisByVoiture(reservation.getIdVoit());
        int avance = reservation.getMontantAvance();
        String paiement = reservation.getPaiement();

        if (avance < 0) {
            throw new SQLException("Le montant avance ne peut pas etre negatif.");
        }
        if (avance > frais) {
            throw new SQLException("Le montant avance (" + avance + ") ne peut pas depasser le frais (" + frais + ").");
        }
        if ("Sans avance".equals(paiement) && avance != 0) {
            throw new SQLException("Avec 'Sans avance', le montant avance doit etre 0.");
        }
        if ("Tout payé".equals(paiement) && avance != frais) {
            throw new SQLException("Avec 'Tout payé', le montant avance doit etre egal au frais (" + frais + " Ar).");
        }
        if ("Avec avance".equals(paiement) && avance <= 0) {
            throw new SQLException("Avec 'Avec avance', le montant avance doit etre superieur a 0.");
        }
    }

    private int findFraisByVoiture(String idVoit) throws SQLException {
        String sql = "SELECT frais FROM voiture WHERE idvoit=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("frais");
                }
            }
        }
        throw new SQLException("Voiture introuvable: " + idVoit);
    }

    private boolean isPlaceAvailable(String idVoit, int place, String currentReservationId) throws SQLException {
        String sql = "SELECT idreserv FROM reserver WHERE idvoit=? AND place=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            ps.setInt(2, place);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return true;
                }
                if (currentReservationId == null) {
                    return false;
                }
                return currentReservationId.equals(rs.getString("idreserv"));
            }
        }
    }
}
