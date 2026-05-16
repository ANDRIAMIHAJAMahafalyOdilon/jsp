package com.cooperative.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Reservation {
    private String idReserv;
    private String idVoit;
    private int idCli;
    private int place;
    private LocalDateTime dateReserv;
    private LocalDate dateVoyage;
    private String paiement;
    private int montantAvance;

    public String getIdReserv() { return idReserv; }
    public void setIdReserv(String idReserv) { this.idReserv = idReserv; }
    public String getIdVoit() { return idVoit; }
    public void setIdVoit(String idVoit) { this.idVoit = idVoit; }
    public int getIdCli() { return idCli; }
    public void setIdCli(int idCli) { this.idCli = idCli; }
    public int getPlace() { return place; }
    public void setPlace(int place) { this.place = place; }
    public LocalDateTime getDateReserv() { return dateReserv; }
    public void setDateReserv(LocalDateTime dateReserv) { this.dateReserv = dateReserv; }
    public LocalDate getDateVoyage() { return dateVoyage; }
    public void setDateVoyage(LocalDate dateVoyage) { this.dateVoyage = dateVoyage; }
    public String getPaiement() { return paiement; }
    public void setPaiement(String paiement) { this.paiement = paiement; }
    public int getMontantAvance() { return montantAvance; }
    public void setMontantAvance(int montantAvance) { this.montantAvance = montantAvance; }
}
