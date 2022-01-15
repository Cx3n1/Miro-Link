package miro.link;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;


import java.io.IOException;
import java.io.InputStream;


@Slf4j
public class MiroLink extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent rootNode;
        try(InputStream is = MiroLink.class.getResourceAsStream("/views/main_view.fxml")){
            rootNode = fxmlLoader.load(is);
        }
        Scene scene = new Scene(rootNode, 750, 500);

        try(InputStream is = MiroLink.class.getResourceAsStream("/images/icon.png")) {
            Image image = new Image(is);
            System.out.println("Trying to load icon" + image);
            stage.getIcons().add(image);
        } catch (Exception e) {
            log.warn("Couldn't load icon", e);
        }

        stage.setTitle("Miro-Link");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}