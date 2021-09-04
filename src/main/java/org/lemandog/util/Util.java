package org.lemandog.util;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Util {
    public static void constructAWin() {
        Stage info = new Stage();
        VBox layout = new VBox();
        Scene mainSc = new Scene(layout,400,400);

        info.setScene(mainSc);
        info.getIcons().add(new Image("/database.png"));
        info.setTitle("О программе");

        info.show();


    }
}
