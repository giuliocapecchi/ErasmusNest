package it.unipi.erasmusnest.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class CityFieldsLoader { //ciao

    // static ArrayList<String> studyFields = new ArrayList<>();
    static ArrayList<String> cityFields = new ArrayList<>();

    private static void loadCityFields() {
        // studyFields = new ArrayList<>();
        cityFields = new ArrayList<>();
        String cittaField = null;
        try {
            cittaField = Files.readString(Paths.get("src/main/resources/default/studyFields.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert cittaField != null;
        String[] cityFieldsArray = cittaField.split("\n");
        for (String s : cityFieldsArray) {
            if (!s.equals("")) {
                cityFields.add(s);
            }
        }
    }

    /* return the study fields */
    public static ArrayList<String> getCityFields() {
        if (cityFields.isEmpty()) {
            loadCityFields();
        }
        return cityFields;
    }

    public static void printCityFields() {
        if (cityFields.isEmpty()) {
            loadCityFields();
        }
        for (String s : cityFields) {
            System.out.println(s);
        }
    }

}
