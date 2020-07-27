package com.gmail.specifikarma.optimumklus.networking;

import java.util.ArrayList;

public class Settings {
    private ArrayList<String> home = new ArrayList<>();
    private ArrayList<String> gallery = new ArrayList<>();

    public String getPlace() {
        return home.get(0);
    }

    public String getDesign() {
        return home.get(1);
    }

    public String getRepair() {
        return home.get(2);
    }

    public String getInstall() {
        return home.get(3);
    }

    public String getGarden() {
        return home.get(4);
    }

    public String getPaint() {
        return home.get(5);
    }

    public Settings addHomeImages(String place, String design, String repair, String install, String garden, String paint) {
        home.add(place);
        home.add(design);
        home.add(repair);
        home.add(install);
        home.add(garden);
        home.add(paint);
        return this;
    }

    public Settings addGalleryImages(String link){
        gallery.add(link);
        return this;
    }

    public ArrayList<String> getGallery() {
        return gallery;
    }

    public ArrayList<String> getHome() {
        return home;
    }
}
