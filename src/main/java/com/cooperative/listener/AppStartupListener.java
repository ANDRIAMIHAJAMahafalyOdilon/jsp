package com.cooperative.listener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.cooperative.dao.VoitureDAO;
import com.cooperative.util.DBConnection;

/**
 * Au démarrage :
 * 1. Migre idcli de INT vers VARCHAR(30) si nécessaire
 * 2. Corrige les places orphelines
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        migrateClientIdToVarchar(sce);
        fixOrphanPlaces(sce);
    }

    private void migrateClientIdToVarchar(ServletContextEvent sce) {
        try (Connection cn = DBConnection.getConnection()) {
            // Vérifier le type actuel de idcli dans la table client
            String checkSql = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'client' AND COLUMN_NAME = 'idcli'";
            String dataType = null;
            try (PreparedStatement ps = cn.prepareStatement(checkSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) dataType = rs.getString("DATA_TYPE");
            }

            if ("int".equalsIgnoreCase(dataType)) {
                sce.getServletContext().log("[Migration] idcli est INT, migration vers VARCHAR(30)...");

                // 1. Supprimer la FK dans reserver
                try (PreparedStatement ps = cn.prepareStatement(
                        "ALTER TABLE reserver DROP FOREIGN KEY fk_reserver_client")) {
                    ps.executeUpdate();
                } catch (Exception ignored) { /* FK peut ne pas exister */ }

                // 2. Modifier idcli dans client (INT → VARCHAR)
                try (PreparedStatement ps = cn.prepareStatement(
                        "ALTER TABLE client MODIFY idcli VARCHAR(30) NOT NULL")) {
                    ps.executeUpdate();
                }

                // 3. Modifier idcli dans reserver
                try (PreparedStatement ps = cn.prepareStatement(
                        "ALTER TABLE reserver MODIFY idcli VARCHAR(30) NOT NULL")) {
                    ps.executeUpdate();
                }

                // 4. Recréer la FK
                try (PreparedStatement ps = cn.prepareStatement(
                        "ALTER TABLE reserver ADD CONSTRAINT fk_reserver_client " +
                        "FOREIGN KEY (idcli) REFERENCES client(idcli)")) {
                    ps.executeUpdate();
                } catch (Exception ignored) { /* FK peut déjà exister */ }

                sce.getServletContext().log("[Migration] idcli migre vers VARCHAR(30) avec succes.");
            } else {
                sce.getServletContext().log("[Migration] idcli est deja VARCHAR, aucune migration necessaire.");
            }
        } catch (Exception e) {
            sce.getServletContext().log("[Migration] Erreur migration idcli: " + e.getMessage());
        }
    }

    private void fixOrphanPlaces(ServletContextEvent sce) {
        try {
            new VoitureDAO().fixOrphanOccupiedPlaces();
            sce.getServletContext().log("[AppStartup] Places orphelines corrigees.");
        } catch (Exception e) {
            sce.getServletContext().log("[AppStartup] Erreur fixOrphan: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
