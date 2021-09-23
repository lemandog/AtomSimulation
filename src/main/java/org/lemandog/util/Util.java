package org.lemandog.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.lemandog.App;
import org.lemandog.Sim;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Util {
    public static Label queueSize = new Label();
    public static void constructAWin() {
        Stage info = new Stage();
        VBox layout = new VBox();
        Scene mainSc = new Scene(layout,550,250);
        info.setResizable(false);
        info.setScene(mainSc);
        info.getIcons().add(new Image("/database.png"));
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
        Stage queueBuilder = new Stage();
        queueBuilder.getIcons().add(new Image("/database.png"));
        queueBuilder.setResizable(false);
        VBox layout = new VBox();

        Label mainLabel = new Label("Очереди");
        mainLabel.setAlignment(Pos.TOP_CENTER);
        mainLabel.setFont(App.mainFont);
        VBox order = new VBox();
        layout.getChildren().add(mainLabel);

        Button addToQueue = new Button("Добавить");
        addToQueue.setFont(App.mainFont);
        layout.getChildren().add(addToQueue);
        addToQueue.setOnAction(actionEvent -> {
            addToQueue();
            order.getChildren().clear();
            order.getChildren().addAll(updateQueue());
        });
        Button popToQueue = new Button("Удалить последний");
        popToQueue.setFont(App.mainFont);
        popToQueue.setOnAction(actionEvent -> {
            popToQueue();
            order.getChildren().clear();
            order.getChildren().addAll(updateQueue());
        });
        layout.getChildren().add(popToQueue);
        layout.getChildren().add(queueSize);

        order.getChildren().addAll(updateQueue());
        layout.getChildren().add(order);
        Scene mainSc = new Scene(layout,250,550);

        queueBuilder.setScene(mainSc);
        queueBuilder.show();
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

    public static void addToQueue(){
        App.simQueue.add(new Sim());
    }
}
