package com.cooperative.model;

public class PaiementReport {
    private String categorie;
    private int total;

    public PaiementReport(String categorie, int total) {
        this.categorie = categorie;
        this.total = total;
    }

    public String getCategorie() { return categorie; }
    public int getTotal() { return total; }
}
