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
                <p><em><strong>КОМАНДЫ:</strong></em></p>
                <table style="border-collapse: collapse; width: 100%; height: 342px;" border="1">
                <tbody>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">PARTS 1000</td>
                <td style="width: 66.836%; height: 18px;">Количество частиц в симуляции</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">STEPS 1000</td>
                <td style="width: 66.836%; height: 18px;">Количество шагов dS в симуляции</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">TEMPE 273</td>
                <td style="width: 66.836%; height: 18px;">Температура камеры в кельвинах</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">TEMPS 1500</td>
                <td style="width: 66.836%; height: 18px;">Температура испарителя в кельвинах</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">CAMSX, CAMSY, CAMSZ</td>
                <td style="width: 66.836%; height: 18px;">Размеры камеры в метрах по координатам</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">PRES^ -8</td>
                <td style="width: 66.836%; height: 18px;">Степень давления в паскалях (1*10^X)</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">PRES* 1</td>
                <td style="width: 66.836%; height: 18px;">Множитель давления в паскалях (X*10^1)</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">DRAWP</td>
                <td style="width: 66.836%; height: 18px;">Отрисовка путей частиц (ресурсоёмко!)</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">AURUM</td>
                <td style="width: 66.836%; height: 18px;">Название вещества в симуляции на латыни</td>
                </tr>
                <tr>
                <td style="width: 33.164%;">3DNOT</td>
                <td style="width: 66.836%;">Не отрисовывать 3D визуализацию</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">THREA 12</td>
                <td style="width: 66.836%; height: 18px;">Количество потоков для счёта</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">TARSX 1,TARSZ 1</td>
                <td style="width: 66.836%; height: 18px;">Размеры подложки в долях камеры (от 0 до 1)</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">GENEX 1,GENEZ 1</td>
                <td style="width: 66.836%; height: 18px;">Размеры генератора в долях камеры (от 0 до 1)</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">DIRPA C:\\Users\\User\\Desktop</td>
                <td style="width: 66.836%; height: 18px;">Путь сохранения вывода</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">PNGZA</td>
                <td style="width: 66.836%; height: 18px;">Сохранить .PNG заселения подложки</td>
                </tr>
                <tr>
                <td style="width: 33.164%;">RESOL 100</td>
                <td style="width: 66.836%;">Делений/метр в счёте заселения подложки</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">PALIT 1</td>
                <td style="width: 66.836%; height: 18px;">Палитра выводимого изображения</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">CSVZA</td>
                <td style="width: 66.836%; height: 18px;">Координаты попавших на подложку частиц в CSV</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">CSVZE</td>
                <td style="width: 66.836%; height: 18px;">Плотность заселения подложки в CSV</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">START</td>
                <td style="width: 66.836%; height: 18px;">Запустить с текущими параметрами</td>
                </tr>
                <tr style="height: 18px;">
                <td style="width: 33.164%; height: 18px;">RUNMO 20</td>
                <td style="width: 66.836%; height: 18px;">Запустить ещё N симуляций с текущими параметрами</td>
                </tr>
                <tr>
                <td style="width: 33.164%;">DIMEN 3</td>
                <td style="width: 66.836%;">Количество осей</td>
                </tr>
                <tr>
                <td style="width: 33.164%;">WAITT 5</td>
                <td style="width: 66.836%;">Задержка после каждого шага</td>
                </tr>
                <tr>
                <td style="width: 33.164%;">VERWA 0.4</td>
                <td style="width: 66.836%;">Шанс отражения от стен</td>
                </tr>
                <tr>
                <td style="width: 33.164%;">VERGE 0.7</td>
                <td style="width: 66.836%;">Шанс отражения от генератора</td>
                </tr>
                </tbody>
                </table>
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
