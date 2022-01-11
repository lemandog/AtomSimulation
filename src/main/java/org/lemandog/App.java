package org.lemandog;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.lemandog.util.Console;

import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage){
        Console.ready();
        try {
        Stage userControlWindows = new Stage();
        userControlWindows.setResizable(false);
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/UI/main.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        userControlWindows.setScene(scene);
        Image icon = new Image(App.class.getResourceAsStream("/icons/atomSim.png"));
        userControlWindows.getIcons().add(icon);
        userControlWindows.setTitle("Контроль");
        userControlWindows.setOnCloseRequest(event -> System.exit(0));
        userControlWindows.show();
        } catch (IOException e) {
            System.out.println("CANNOT LOAD RESOURCES FROM- CHECK INTEGRITY! - IO EXCEPTION");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
            launch();
    }

}
