package it.unipi.erasmusnest.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class StudyFieldsLoader {

    static ArrayList<String> studyFields = new ArrayList<>();

    private static void loadStudyFields() {
        studyFields = new ArrayList<>();
        String studyField = null;
        try {
            studyField = Files.readString(Paths.get("src/main/resources/default/studyFields.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert studyField != null;
        String[] studyFieldsArray = studyField.split("\n");
        for (String s : studyFieldsArray) {
            if (!s.equals("")) {
                studyFields.add(s);
            }
        }
    }

    /* return the study fields */
    public static ArrayList<String> getStudyFields() {
        if (studyFields.isEmpty()) {
            loadStudyFields();
        }
        return studyFields;
    }

}
