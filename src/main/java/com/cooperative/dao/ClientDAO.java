package com.cooperative.dao;

import com.cooperative.model.Client;
import com.cooperative.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO {

    public void create(Client client) throws SQLException {
        String sql = "INSERT INTO client (idcli, nom, numtel) VALUES (?, ?, ?)";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, client.getIdCli());
            ps.setString(2, client.getNom());
            ps.setString(3, client.getNumTel());
            ps.executeUpdate();
        }
    }

    public void update(Client client) throws SQLException {
        String sql = "UPDATE client SET nom=?, numtel=? WHERE idcli=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, client.getNom());
            ps.setString(2, client.getNumTel());
            ps.setInt(3, client.getIdCli());
            ps.executeUpdate();
        }
    }

    public void delete(int idCli) throws SQLException {
        String sql = "DELETE FROM client WHERE idcli=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCli);
            ps.executeUpdate();
        }
    }

    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT idcli, nom, numtel FROM client ORDER BY idcli DESC";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clients.add(map(rs));
            }
        }
        return clients;
    }

    public boolean exists(int idCli) throws SQLException {
        return findById(idCli) != null;
    }

    public Client findById(int idCli) throws SQLException {
        String sql = "SELECT idcli, nom, numtel FROM client WHERE idcli=?";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idCli);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
            }
        }
        return null;
    }

    public List<Client> searchByNameOrPhone(String keyword) throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT idcli, nom, numtel FROM client WHERE nom LIKE ? OR numtel LIKE ? ORDER BY idcli DESC";
        try (Connection cn = DBConnection.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            String term = "%" + keyword + "%";
            ps.setString(1, term);
            ps.setString(2, term);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    clients.add(map(rs));
                }
            }
        }
        return clients;
    }

    private Client map(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setIdCli(rs.getInt("idcli"));
        c.setNom(rs.getString("nom"));
        c.setNumTel(rs.getString("numtel"));
        return c;
    }
}
