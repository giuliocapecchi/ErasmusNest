package it.unipi.erasmusnest.controllers;

import com.dlsc.gemsfx.EmailField;
import javafx.scene.control.TextField;

public interface Validator {

    boolean isTextFieldValid(TextField textField);

    boolean isEmailFieldValid(EmailField emailField);

}
