package org.lemandog.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.lemandog.App;

public class LoadConfig {
    public static void constructConfigInfoFrame(){
        Stage info = new Stage();
        VBox layout = new VBox();
        Scene infoPane = new Scene(layout);
        info.setScene(infoPane);
        layout.getChildren().add(new Label("Инструкция: как использовать конфигурационный файл?"));
        layout.getChildren().add(new Label("Создайте новый txt и запишите команды. Одна строка - одна команда"));
        layout.getChildren().add(new Label("PARTS 500 / Количество частиц"));
        layout.getChildren().add(new Label("STEPS 5000 / Количество максимальное шагов"));
        layout.getChildren().add(new Label("TEMPE 273 / Температура, в кельвинах (>0)"));
        layout.getChildren().add(new Label("CAMSX 5  / Размеры камеры по осям. Могут быть дробными."));
        layout.getChildren().add(new Label("CAMSY 5"));
        layout.getChildren().add(new Label("CAMSZ 5"));
        layout.getChildren().add(new Label("PRES^ -5  / Степень 1*10^X в паскалях"));
        layout.getChildren().add(new Label("PRES* 1   / Степень X*10^-5 в паскалях"));
        layout.getChildren().add(new Label("DRAWP     / Рисовать пути частиц (Осторожно, ресурсоёмко!)"));
        layout.getChildren().add(new Label("THREA 12  / Количество потоков для счёта"));
        layout.getChildren().add(new Label("XTARS 0.5 / Размер подложки в долях от камеры"));
        layout.getChildren().add(new Label("ZTARS 0.5"));
        layout.getChildren().add(new Label("XGENE 0.8 / Размер генератора в долях от камеры"));
        layout.getChildren().add(new Label("ZGENE 0.8  / Количество потоков для счёта"));
        layout.getChildren().add(new Label("DIRPA C:\\Users\\User\\Desktop  / Директория для сохранения результатов"));
        layout.getChildren().add(new Label("TEXTW / Текстовый вывод"));
        layout.getChildren().add(new Label("PNGZA / Плотность заселения"));
        layout.getChildren().add(new Label("GRAPH / Встроенный графопостроитель"));
        layout.getChildren().add(new Label("PALIT 1 / Выбор палитры"));
        layout.getChildren().add(new Label("START / Запустить симуляцию (После всех прочих команд!)"));
        layout.getChildren().add(new Label("DIMEN 3 / Количество осей"));
        layout.getChildren().add(new Label("WAITT 5 / Задержка между шагами"));
        layout.getChildren().add(new Label("VERWA 0 / Вероятность отражения от стен"));
        layout.getChildren().add(new Label("VERGE 0.9 / Вероятность отражения от генератора"));
        info.show();
    }
    public static void select(File file) {
        if (file.getName().contains(".txt")){
            App.fileDropText.setTextFill(Color.DARKGREEN);
            App.fileDropText.setText("Файл принят");
            load(file);
        } else {
            App.fileDropText.setTextFill(Color.DARKRED);
            App.fileDropText.setText("Файл получен, но он не .txt");
        }
    }

    private static void load(File file) {
        try {
            for (String line : Files.readAllLines(file.toPath())){
                if (line.contains("PARTS")){App.particleAm.setText(line.trim().replaceAll("PARTS ",""));} //int
                if (line.contains("STEPS")){App.stepsAm.setText(line.trim().replaceAll("STEPS ",""));} //int
                if (line.contains("TEMPE")){App.tempAm.setText(line.trim().replaceAll("TEMPE ",""));} //int
                if (line.contains("CAMSX")){App.xFrameLen.setText(line.trim().replaceAll("CAMSX ",""));} //double
                if (line.contains("CAMSY")){App.yFrameLen.setText(line.trim().replaceAll("CAMSY ",""));} //double
                if (line.contains("CAMSZ")){App.zFrameLen.setText(line.trim().replaceAll("CAMSZ ",""));} //double
                if (line.contains("PRES^")){App.pressurePow.setText(line.trim().replaceAll("PRES\\^ ",""));} //double
                if (line.contains("PRES*")){App.pressure.setText(line.trim().replaceAll("PRES\\* ",""));} //double
                if (line.contains("DRAWP")){App.pathDrawing.setSelected(true);} //bool
                if (line.contains("THREA")){App.threadCount.setValue(Integer.parseInt(line.trim().replaceAll("THREA ","")));} //int
                if (line.contains("DIMEN")){App.dimensionCount.setValue(Integer.parseInt(line.trim().replaceAll("DIMEN ","")));} //int
                if (line.contains("XTARS")){App.targetSizeX.setValue(Double.parseDouble(line.trim().replaceAll("XTARS ","")));} //double
                if (line.contains("ZTARS")){App.targetSizeZ.setValue(Double.parseDouble(line.trim().replaceAll("ZTARS ","")));} //double
                if (line.contains("XGENE")){App.genSizeX.setValue(Double.parseDouble(line.trim().replaceAll("XGENE ","")));} //double
                if (line.contains("ZGENE")){App.genSizeZ.setValue(Double.parseDouble(line.trim().replaceAll("ZGENE ","")));} //double
                if (line.contains("DIRPA")){Output.directoryChooserOutputPath.setInitialDirectory(new File((line.trim().replaceAll("DIRPA ",""))));} //str
                if (line.contains("TEXTW")){Output.output = true;} //bool
                if (line.contains("PNGZA")){Output.outputPic = true;} //bool
                if (line.contains("GRAPH")){Output.outputCSV = true;}
                if (line.contains("PALIT")){Output.outputPallete.setValue(Integer.parseInt(line.trim().replaceAll("PALIT ","")));} //int
                if (line.contains("START")){App.startSimButt.fire();} //bool
                if (line.contains("WAITT")){App.waitTime.setValue(Integer.parseInt(line.trim().replaceAll("WAITT ","")));} //int
                if (line.contains("VERWA")){App.bounceWallChance.setValue(Double.parseDouble(line.trim().replaceAll("VERWA ","")));} //double
                if (line.contains("VERGE")){App.bounceGenChance.setValue(Double.parseDouble(line.trim().replaceAll("VERGE ","")));} //double
            }
    } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
