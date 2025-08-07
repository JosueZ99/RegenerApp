package com.regenerarestudio.regenerapp.model;

public class Project {
    private int id;
    private String name;
    private String client;
    private String location;
    private String type;
    private String status;
    private boolean isSelected;

    public Project(int id, String name, String client, String location, String type, String status) {
        this.id = id;
        this.name = name;
        this.client = client;
        this.location = location;
        this.type = type;
        this.status = status;
        this.isSelected = false;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public String getClient() { return client; }
    public String getLocation() { return location; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public boolean isSelected() { return isSelected; }

    // Setters
    public void setSelected(boolean selected) { this.isSelected = selected; }

    @Override
    public String toString() {
        return name;
    }
}