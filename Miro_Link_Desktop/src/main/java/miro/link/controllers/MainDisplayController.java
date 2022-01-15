package miro.link.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import miro.link.utils.Launcher;
import miro.link.utils.StatusWatchman;
import miro.link.utils.observer.Listener;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainDisplayController implements Initializable, Listener {

    @FXML
    private Button main_connect_button;
    @FXML
    private Text main_status_text;
    @FXML
    private TextArea main_log_console;
    @FXML
    private TextField main_ip;
    @FXML
    private TextField main_port;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        main_status_text.setText("Offline");
        StatusWatchman.addListener(this);
    }

    public void toggleMirroring(ActionEvent actionEvent) throws IOException {
        Launcher.toggleServer();
    }

    @Override
    public void update() {
        Runnable runnable = () -> {
            if(StatusWatchman.isServerRunning()){
                main_ip.setText(StatusWatchman.getIP());
                main_port.setText(String.valueOf(StatusWatchman.getPORT()));
                main_connect_button.setText("Disconnect");

                if(StatusWatchman.isClientConnected()){
                    main_status_text.setText("Connected");
                }
                else{
                    main_status_text.setText("Waiting for connection...");
                }
            } else{
                main_status_text.setText("Offline");
                main_connect_button.setText("Start Mirroring");
                main_ip.setText(null);
                main_port.setText(null);
            }

            main_log_console.setText("");
            main_log_console.appendText(StatusWatchman.getLogConsoleText());
        };

        //update could be called from server thread but only main thread can update gui
        Platform.runLater(runnable);
    }
}
