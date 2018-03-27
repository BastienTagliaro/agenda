package com.tagliaro.monclin.urca.utils;

/*
    Pour add des cours dans la base de données

        DatabaseHandler db = new DatabaseHandler(this);

        db.doTable(); // Remet à 0 la base de données et recrée les tables.

        db.add(new Classes("INFO0406", "3-S26", "INFO0406 \n[enseignant]Marwane AYAIDA \n[classroom] 3-S26",
                "13-03-2018", "16h00", "19h00"));
 */
public class Classes {
    private long id;
    private String classname;
    private String classroom;
    private String description;
    private String date;
    private String startTime;
    private String endTime;

    public Classes() {
    }

    public Classes(String classname, String classroom, String description, String date, String startTime, String endTime) {
        super();
        this.classname = classname;
        this.classroom = classroom;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public Classes(long id, String classname, String classroom, String description, String date, String startTime, String endTime) {
        super();
        this.id = id;
        this.classname = classname;
        this.classroom = classroom;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getClassroom() {
        return classroom;
    }

    public void setClassroom(String classroom) {
        this.classroom = classroom;
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

    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
