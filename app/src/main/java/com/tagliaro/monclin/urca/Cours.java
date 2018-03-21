package com.tagliaro.monclin.urca;

/*
    Pour ajouter des cours dans la base de données

        DatabaseHandler db = new DatabaseHandler(this);

        db.doTable(); // Remet à 0 la base de données et recrée les tables.

        db.ajouter(new Cours("INFO0406", "3-S26", "INFO0406 \n[enseignant]Ayaida \n[salle] 3-S26",
                "13-03-2018", "16h00", "19h00"));
 */
public class Cours {
    private long id;
    private String nomCours;
    private String salle;
    private String description;
    private String date;
    private String heureDebut;
    private String heureFin;

    public Cours() {
    }

    public Cours(String nomCours, String salle, String description, String date, String heureDebut, String heureFin) {
        super();
        this.nomCours = nomCours;
        this.salle = salle;
        this.description = description;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }


    public Cours(long id, String nomCours, String salle, String description, String date, String heureDebut, String heureFin) {
        super();
        this.id = id;
        this.nomCours = nomCours;
        this.salle = salle;
        this.description = description;
        this.date = date;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getNomCours() {
        return nomCours;
    }

    public void setNomCours(String nomCours) {
        this.nomCours = nomCours;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String getHeureDebut() {
        return heureDebut;
    }
    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    public String getHeureFin() {
        return heureFin;
    }
    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }
}
