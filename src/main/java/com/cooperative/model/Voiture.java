package com.cooperative.model;

public class Voiture {
    private String idVoit;
    private String design;
    private String type;
    private int nbrPlace;
    private int frais;

    public String getIdVoit() { return idVoit; }
    public void setIdVoit(String idVoit) { this.idVoit = idVoit; }
    public String getDesign() { return design; }
    public void setDesign(String design) { this.design = design; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getNbrPlace() { return nbrPlace; }
    public void setNbrPlace(int nbrPlace) { this.nbrPlace = nbrPlace; }
    public int getFrais() { return frais; }
    public void setFrais(int frais) { this.frais = frais; }
}
