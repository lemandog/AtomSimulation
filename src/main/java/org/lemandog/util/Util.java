package org.lemandog.util;

import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class Util {
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

    public static String getContent() {
        return """
                <h2 style="text-align: center;"><span style="color: #ffff99; background-color: #999999;"><em><strong>Добро пожаловать в Atom Sim</strong></em></span></h2>
                <h4><span style="color: #ff0000;">О Конфигурациях:</span></h4>
                <h4>Программа поддерживает два вида конфигураций - .TXT и .AS</h4>
                <p>Текущие параметры можно сохранить, нажав на кнопку выше - на выбор даётся два формата</p>
                <p>TXT и AS - первый можно создать или редактировать вручную, второй только загружать</p>
                <p>Команды читаются построчно, пустые строки или строки с неверными параметрами игнорируются.</p>
                <h4><span style="color: #ff0000;">Прочее:</span></h4>
                <h4>Для контроля вида на камеру используйте NUMPAD</h4>
                <h4>6-3 - Назад - вперёд (Ось Z)</h4>
                <h4>5-2 - Вверх - вниз (Ось Y)</h4>
                <h4>4-1 - Влево - вправо (Ось X)</h4>
                <h4>Если вам нужно быстро загрузить свои нестандартные параметры симуляции, используйте конфигурационный txt или .AS <br />Проверки на верность введённых данных нет<br />Чтобы остановить текущую симуляцию, нажмите ESC или закройте её окно отрисовки</h4>
                <h4><span style="color: #ff0000;">О Программе:</span></h4>
                <p>&nbsp;</p>
                <p>&nbsp;</p>
                """;
    }
}
