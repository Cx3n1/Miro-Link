package miro.link.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import miro.link.utils.Launcher;
import miro.link.utils.StatusWatchman;
import miro.link.utils.observer.Listener;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable, Listener {
    //mt - main toolbar
    @FXML
    private Button mt_start_mirroring;
    @FXML
    private Text mt_status;
    @FXML
    private Text mt_ip;
    @FXML
    private Text mt_port;
    @FXML
    private BorderPane center_display_wrapper;

    private static Node loadDisplayFromFXML(String name) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Node display;
        try (InputStream is = MainController.class.getResourceAsStream("/views/displays/" + name + "_display.fxml")) {
            display = fxmlLoader.load(is);
        }

        return display;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            displayMain(null);
        } catch (IOException e) {
            e.getStackTrace();
            //TODO log failed to initialize
        }

        mt_status.setText("Offline");
        mt_ip.setText("...");
        mt_port.setText("...");

        StatusWatchman.addListener(this);
    }

    public void displayMain(ActionEvent actionEvent) throws IOException {
        center_display_wrapper.setCenter(loadDisplayFromFXML("main"));
        StatusWatchman.updateAll();
    }

    public void displayConfiguration(ActionEvent actionEvent) throws IOException {
        center_display_wrapper.setCenter(loadDisplayFromFXML("configuration"));
    }

    public void displayCalibration(ActionEvent actionEvent) throws IOException {
        center_display_wrapper.setCenter(loadDisplayFromFXML("calibration"));
    }

    public void displaySettings(ActionEvent actionEvent) throws IOException {
        center_display_wrapper.setCenter(loadDisplayFromFXML("settings"));
    }

    public void toggleMirroring(ActionEvent actionEvent) throws IOException {
        Launcher.toggleServer();
    }

    @Override
    public void update() {
        Runnable runnable = () -> {
            if (StatusWatchman.isServerRunning()) {
                mt_ip.setText(StatusWatchman.getIP());
                mt_port.setText(String.valueOf(StatusWatchman.getPORT()));
                mt_start_mirroring.setText("Disconnect");

                if (StatusWatchman.isClientConnected())
                    mt_status.setText("Connected");
                else
                    mt_status.setText("Waiting for connection...");
            } else {
                mt_status.setText("Offline");
                mt_start_mirroring.setText("Start Mirroring");
                mt_ip.setText("...");
                mt_port.setText("...");
            }
        };

        Platform.runLater(runnable);
    }
}