package com.cooperative.model;

import com.cooperative.util.DateFormatUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReservationView {
    private String idReserv;
    private String idVoit;
    private String typeVoiture;
    private int idCli;
    private String nomClient;
    private String numTel;
    private int place;
    private LocalDateTime dateReserv;
    private LocalDate dateVoyage;
    private String paiement;
    private int montantAvance;
    private int frais;
    private int reste;

    public String getIdReserv() { return idReserv; }
    public void setIdReserv(String idReserv) { this.idReserv = idReserv; }
    public String getIdVoit() { return idVoit; }
    public void setIdVoit(String idVoit) { this.idVoit = idVoit; }
    public String getTypeVoiture() { return typeVoiture; }
    public void setTypeVoiture(String typeVoiture) { this.typeVoiture = typeVoiture; }
    public int getIdCli() { return idCli; }
    public void setIdCli(int idCli) { this.idCli = idCli; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }
    public int getPlace() { return place; }
    public void setPlace(int place) { this.place = place; }
    public LocalDateTime getDateReserv() { return dateReserv; }
    public void setDateReserv(LocalDateTime dateReserv) { this.dateReserv = dateReserv; }
    public LocalDate getDateVoyage() { return dateVoyage; }
    public void setDateVoyage(LocalDate dateVoyage) { this.dateVoyage = dateVoyage; }
    public String getDateVoyageLabel() { return DateFormatUtil.formatFrenchDate(dateVoyage); }
    public String getPaiement() { return paiement; }
    public void setPaiement(String paiement) { this.paiement = paiement; }
    public int getMontantAvance() { return montantAvance; }
    public void setMontantAvance(int montantAvance) { this.montantAvance = montantAvance; }
    public int getFrais() { return frais; }
    public void setFrais(int frais) { this.frais = frais; }
    public int getReste() { return reste; }
    public void setReste(int reste) { this.reste = reste; }
}
