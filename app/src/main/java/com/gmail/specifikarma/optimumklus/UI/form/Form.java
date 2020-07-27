package com.gmail.specifikarma.optimumklus.UI.form;

import java.io.File;
import java.util.ArrayList;

public class Form {
    private ArrayList<String> namePhone = new ArrayList<>();
    private ArrayList<String> services = new ArrayList<>();
    private ArrayList<File> files = new ArrayList<>();

    public void addName(String name) {
        namePhone.add(0,name.trim());
    }

    public void addPhone(String phone) {
        namePhone.add(1, phone.trim());
    }

    public void addFiles(File path){
        files.add(path);
    }

    public void addServices(String path){
        services.add(path);
    }

    public String getName() {
        return namePhone.get(0);
    }

    public String getPhone() {
        return namePhone.get(1);
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public ArrayList<String> getServices() {
        return services;
    }
}
