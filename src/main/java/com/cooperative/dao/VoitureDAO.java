package com.cooperative.dao;

import com.cooperative.model.Voiture;
import com.cooperative.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoitureDAO {

    public void create(Voiture voiture) throws SQLException {
        String sql = "INSERT INTO voiture (idvoit, design, type, nbrplace, frais) VALUES (?, ?, ?, ?, ?)";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, voiture.getIdVoit());
            ps.setString(2, voiture.getDesign());
            ps.setString(3, voiture.getType());
            ps.setInt(4, voiture.getNbrPlace());
            ps.setInt(5, voiture.getFrais());
            ps.executeUpdate();
        }
        regeneratePlaces(voiture.getIdVoit(), voiture.getNbrPlace());
    }

    public String generateNextId() throws SQLException {
        String sql = "SELECT idvoit FROM voiture WHERE idvoit LIKE 'RES-%'";
        int max = 0;
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String id = rs.getString("idvoit");
                if (id == null) continue;
                String trimmed = id.trim();
                if (!trimmed.startsWith("RES-")) continue;
                String suffix = trimmed.substring(4);
                try {
                    int n = Integer.parseInt(suffix);
                    if (n > max) max = n;
                } catch (NumberFormatException ignored) {
                    // Ignore malformed legacy IDs.
                }
            }
        }
        return "RES-" + (max + 1);
    }

    public void update(Voiture voiture) throws SQLException {
        String sql = "UPDATE voiture SET design=?, type=?, nbrplace=?, frais=? WHERE idvoit=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, voiture.getDesign());
            ps.setString(2, voiture.getType());
            ps.setInt(3, voiture.getNbrPlace());
            ps.setInt(4, voiture.getFrais());
            ps.setString(5, voiture.getIdVoit());
            ps.executeUpdate();
        }
        regeneratePlaces(voiture.getIdVoit(), voiture.getNbrPlace());
    }

    public void delete(String idVoit) throws SQLException {
        String sql = "DELETE FROM voiture WHERE idvoit=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            ps.executeUpdate();
        }
    }

    public Voiture findById(String idVoit) throws SQLException {
        String sql = "SELECT idvoit, design, type, nbrplace, frais FROM voiture WHERE idvoit=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<Voiture> findAll() throws SQLException {
        List<Voiture> list = new ArrayList<>();
        String sql = "SELECT idvoit, design, type, nbrplace, frais FROM voiture ORDER BY idvoit DESC";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public List<Integer> findFreePlaces(String idVoit) throws SQLException {
        List<Integer> places = new ArrayList<>();
        String sql = "SELECT place FROM place WHERE idvoit=? AND occupation='Non' ORDER BY place";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    places.add(rs.getInt("place"));
                }
            }
        }
        return places;
    }

    public List<Integer> addPlaces(String idVoit, Integer startPlace, int count) throws SQLException {
        if (idVoit == null || idVoit.trim().isBlank()) {
            throw new SQLException("Voiture obligatoire.");
        }
        if (count <= 0) {
            throw new SQLException("Nombre de places a ajouter invalide.");
        }
        if (count > 200) {
            throw new SQLException("Nombre de places a ajouter trop grand.");
        }

        String getMax = "SELECT COALESCE(MAX(place),0) AS maxp FROM place WHERE idvoit=?";
        String insert = "INSERT INTO place (idvoit, place, occupation) VALUES (?, ?, 'Non')";
        String updateNbr = "UPDATE voiture SET nbrplace = ? WHERE idvoit = ?";

        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                int maxExisting = 0;
                try (PreparedStatement ps = cn.prepareStatement(getMax)) {
                    ps.setString(1, idVoit);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) maxExisting = rs.getInt("maxp");
                    }
                }

                int start = (startPlace == null || startPlace <= 0) ? (maxExisting + 1) : startPlace;
                int newMax = start + count - 1;

                try (PreparedStatement ins = cn.prepareStatement(insert)) {
                    for (int p = start; p <= newMax; p++) {
                        ins.setString(1, idVoit);
                        ins.setInt(2, p);
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }

                // Keep voiture.nbrplace consistent with the highest seat number
                try (PreparedStatement up = cn.prepareStatement(updateNbr)) {
                    up.setInt(1, Math.max(newMax, maxExisting));
                    up.setString(2, idVoit);
                    up.executeUpdate();
                }

                cn.commit();
                return findFreePlaces(idVoit);
            } catch (SQLException ex) {
                cn.rollback();
                if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("duplicate")) {
                    throw new SQLException("Doublon numero de place. Choisis un autre numero.");
                }
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    public void occupyPlace(String idVoit, int place) throws SQLException {
        String sql = "UPDATE place SET occupation='Oui' WHERE idvoit=? AND place=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            ps.setInt(2, place);
            ps.executeUpdate();
        }
    }

    public void freePlace(String idVoit, int place) throws SQLException {
        String sql = "UPDATE place SET occupation='Non' WHERE idvoit=? AND place=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, idVoit);
            ps.setInt(2, place);
            ps.executeUpdate();
        }
    }

    public void fixOrphanOccupiedPlaces() throws SQLException {
        String sql = "UPDATE place p "
                + "SET p.occupation='Non' "
                + "WHERE p.occupation='Oui' "
                + "AND NOT EXISTS ("
                + "  SELECT 1 FROM reserver r "
                + "  WHERE r.idvoit = p.idvoit AND r.place = p.place"
                + ")";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public List<Integer> deleteFreePlace(String idVoit, int placeNumber) throws SQLException {
        if (idVoit == null || idVoit.trim().isBlank()) {
            throw new SQLException("Voiture obligatoire.");
        }
        if (placeNumber <= 0) {
            throw new SQLException("Numero de place invalide.");
        }

        String checkSql = "SELECT occupation FROM place WHERE idvoit=? AND place=?";
        String deleteSql = "DELETE FROM place WHERE idvoit=? AND place=?";
        String maxSql = "SELECT COALESCE(MAX(place),0) AS maxp FROM place WHERE idvoit=?";
        String updateNbr = "UPDATE voiture SET nbrplace = ? WHERE idvoit = ?";

        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                String occupation = null;
                try (PreparedStatement check = cn.prepareStatement(checkSql)) {
                    check.setString(1, idVoit);
                    check.setInt(2, placeNumber);
                    try (ResultSet rs = check.executeQuery()) {
                        if (rs.next()) {
                            occupation = rs.getString("occupation");
                        }
                    }
                }

                if (occupation == null) {
                    throw new SQLException("Place introuvable pour cette voiture.");
                }
                if ("Oui".equalsIgnoreCase(occupation)) {
                    throw new SQLException("Impossible de supprimer une place occupee.");
                }

                try (PreparedStatement del = cn.prepareStatement(deleteSql)) {
                    del.setString(1, idVoit);
                    del.setInt(2, placeNumber);
                    del.executeUpdate();
                }

                int newMax = 0;
                try (PreparedStatement maxPs = cn.prepareStatement(maxSql)) {
                    maxPs.setString(1, idVoit);
                    try (ResultSet rs = maxPs.executeQuery()) {
                        if (rs.next()) {
                            newMax = rs.getInt("maxp");
                        }
                    }
                }

                try (PreparedStatement up = cn.prepareStatement(updateNbr)) {
                    up.setInt(1, newMax);
                    up.setString(2, idVoit);
                    up.executeUpdate();
                }

                cn.commit();
                return findFreePlaces(idVoit);
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private void regeneratePlaces(String idVoit, int nbrPlaces) throws SQLException {
        try (Connection cn = DBConnection.getConnection()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement del = cn.prepareStatement("DELETE FROM place WHERE idvoit=? AND occupation='Non'")) {
                    del.setString(1, idVoit);
                    del.executeUpdate();
                }
                String insertSql = "INSERT INTO place (idvoit, place, occupation) VALUES (?, ?, 'Non')";
                try (PreparedStatement ins = cn.prepareStatement(insertSql)) {
                    for (int i = 1; i <= nbrPlaces; i++) {
                        if (!placeExists(cn, idVoit, i)) {
                            ins.setString(1, idVoit);
                            ins.setInt(2, i);
                            ins.addBatch();
                        }
                    }
                    ins.executeBatch();
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private boolean placeExists(Connection cn, String idVoit, int place) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT 1 FROM place WHERE idvoit=? AND place=?")) {
            ps.setString(1, idVoit);
            ps.setInt(2, place);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Voiture map(ResultSet rs) throws SQLException {
        Voiture v = new Voiture();
        v.setIdVoit(rs.getString("idvoit"));
        v.setDesign(rs.getString("design"));
        v.setType(rs.getString("type"));
        v.setNbrPlace(rs.getInt("nbrplace"));
        v.setFrais(rs.getInt("frais"));
        return v;
    }
}
