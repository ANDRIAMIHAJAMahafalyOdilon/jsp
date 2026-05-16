package com.cooperative.model;

public class ReservationReceiptData {
    private String idReserv;
    private String dateReserv;
    private String dateVoyage;
    private String nomClient;
    private String numTel;
    private String idVoiture;
    private String typeVoiture;
    private int place;
    private int frais;
    private String paiement;
    private int montantAvance;
    private int reste;

    public String getIdReserv() { return idReserv; }
    public void setIdReserv(String idReserv) { this.idReserv = idReserv; }
    public String getDateReserv() { return dateReserv; }
    public void setDateReserv(String dateReserv) { this.dateReserv = dateReserv; }
    public String getDateVoyage() { return dateVoyage; }
    public void setDateVoyage(String dateVoyage) { this.dateVoyage = dateVoyage; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }
    public String getNumTel() { return numTel; }
    public void setNumTel(String numTel) { this.numTel = numTel; }
    public String getIdVoiture() { return idVoiture; }
    public void setIdVoiture(String idVoiture) { this.idVoiture = idVoiture; }
    public String getTypeVoiture() { return typeVoiture; }
    public void setTypeVoiture(String typeVoiture) { this.typeVoiture = typeVoiture; }
    public int getPlace() { return place; }
    public void setPlace(int place) { this.place = place; }
    public int getFrais() { return frais; }
    public void setFrais(int frais) { this.frais = frais; }
    public String getPaiement() { return paiement; }
    public void setPaiement(String paiement) { this.paiement = paiement; }
    public int getMontantAvance() { return montantAvance; }
    public void setMontantAvance(int montantAvance) { this.montantAvance = montantAvance; }
    public int getReste() { return reste; }
    public void setReste(int reste) { this.reste = reste; }
}
