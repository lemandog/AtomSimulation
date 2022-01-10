package org.lemandog.util;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.lemandog.App;
import org.lemandog.GasTypes;
import org.lemandog.Sim;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Util {
    static GasTypes chosen = GasTypes.CHROME;
    public static Label queueSize = new Label();
    public static void constructAWin() {
        Stage info = new Stage();
        VBox layout = new VBox();
        Scene mainSc = new Scene(layout,550,250);
        info.setResizable(false);
        info.setScene(mainSc);
        info.getIcons().add(new Image("/icons/database.png"));
        info.setTitle("О программе");

        layout.getChildren().add(new Label("Для контроля вида на камеру используйте NUMPAD"));
        layout.getChildren().add(new Label("6-3 - Назад - вперёд (Ось Z)"));
        layout.getChildren().add(new Label("5-2 - Вверх - вниз   (Ось Y)"));
        layout.getChildren().add(new Label("4-1 - Влево - вправо (Ось X)"));
        layout.getChildren().add(new Label("Если вам нужны нестандартные параметры симуляции, используйте конфигурационный txt"));
        layout.getChildren().add(new Label("Проверки на адекватность введённых данных там нет и не планируется"));
        layout.getChildren().add(new Label("Чтобы остановить текущую симуляцию, закройте окно отрисовки"));
        layout.getChildren().add(new Label());
        layout.getChildren().add(new Label("Скачивайте последнюю версию программы по ссылке:"));
        Hyperlink myLink = new Hyperlink("https://github.com/lemandog/AtomSimulation/releases");
        myLink.setOnAction(actionEvent -> {
            try {
                Desktop.getDesktop().browse(new URL(myLink.getText()).toURI());
            } catch (IOException | URISyntaxException e) {e.printStackTrace();}
        });
        layout.getChildren().add(myLink);
        layout.getChildren().add(new Label());
        layout.getChildren().add(new Label("Данная программа написана для научной статьи в 2021"));
        layout.getChildren().add(new Label("Пишите свои замечания и пожелания на адрес:"));
        Hyperlink myLink2 = new Hyperlink("alexo98@yandex.ru");
        myLink2.setOnAction(actionEvent -> {
            try {
                Desktop.getDesktop().browse(new URL("mailto:alexo98@yandex.ru?subject=AtomSim%20feedback").toURI());
            } catch (IOException | URISyntaxException e) {e.printStackTrace();}
        });
        layout.getChildren().add(myLink2);
        info.show();
    }
    public static void constructAWinQueue(){
        try {
            Stage queueBuilder = new Stage();
            queueBuilder.setResizable(false);
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/queueWindow.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            queueBuilder.setScene(scene);
            queueBuilder.getIcons().add(new Image("/icons/database.png"));
            queueBuilder.setResizable(false);
            queueBuilder.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void popToQueue() {
        if (!App.simQueue.isEmpty()){
            App.simQueue.remove();
        }
    }
    private static Node[] updateQueue(){
        Label[] result = new Label[App.simQueue.size()];
        for (int i = 0; i < App.simQueue.size(); i++) {
            result[i] = new Label("SIM #" + i);
            result[i].setFont(App.mainFont);
        }
        queueSize.setText("Размер очереди: " + App.simQueue.size());
        if (result.length > 0){
            return result;
        }else{
            return new Label[]{new Label("ПУСТО")};
        }}

    public static void constructAWinMatterChooser() {
        Stage matterCh = new Stage();
        matterCh.setTitle("Вещество в симуляции");
        matterCh.getIcons().add(new Image("/icons/matterChooser.png"));
        matterCh.setResizable(false);
        VBox layout = new VBox();

        Label mainLabel = new Label("Вещества (Выбор по нажатию). Выбран сейчас: " + chosen.name());
        mainLabel.setAlignment(Pos.TOP_CENTER);
        mainLabel.setFont(App.mainFont2);
        VBox order = new VBox();
        layout.getChildren().add(mainLabel);

        for (int i = 0; i < GasTypes.values().length; i++) {
            HBox matter = new HBox();
            matter.setPrefHeight(50);
            final GasTypes fin = GasTypes.values()[i];
            matter.setOnMouseClicked(actionEvent -> {
                chosen = fin;
                mainLabel.setText("Вещества. Выбран сейчас: " + chosen.name());
            });
            matter.getChildren().add(new Label(GasTypes.values()[i].name()));
            matter.getChildren().add(new Label(" А.Е.М. "+GasTypes.values()[i].massRAW));
            matter.getChildren().add(new Label(" Диаметр(пм) "+GasTypes.values()[i].diameterRAW));
            matter.setBackground(new Background(
                    new BackgroundFill(
                            new LinearGradient(1, 0, 0.4, 1, true,
                                    CycleMethod.NO_CYCLE,
                                    new Stop(0, GasTypes.values()[i].particleCol),
                                    new Stop(1, Color.WHITESMOKE)
                            ), CornerRadii.EMPTY, Insets.EMPTY),
                    new BackgroundFill(
                            new RadialGradient(
                                    0.9, 0.9, 0.5, 0.5, 0.5, true,
                                    CycleMethod.NO_CYCLE,
                                    new Stop(0, GasTypes.values()[i].particleCol),
                                    new Stop(1, Color.WHITESMOKE)),
                            CornerRadii.EMPTY, Insets.EMPTY)
                    ));
            layout.getChildren().add(matter);
        }

        layout.getChildren().add(order);
        Scene mainSc = new Scene(layout,450,350);

        matterCh.setScene(mainSc);
        matterCh.show();

    }

    public static GasTypes getMat() {
        return chosen;
    }
}
