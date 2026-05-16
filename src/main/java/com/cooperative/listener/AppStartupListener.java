package com.cooperative.listener;

import com.cooperative.dao.VoitureDAO;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Au démarrage : corrige les places orphelines (occupation='Oui' sans réservation).
 */
@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
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
