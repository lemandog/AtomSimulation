package org.lemandog.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.lemandog.*;

public class LoadConfig {
    public static VBox constructConfigInfoFrame(){
        VBox layout = new VBox();
        layout.getChildren().add(new Label("Инструкция: как использовать конфигурационный файл?"));
        layout.getChildren().add(new Label("Создайте новый txt и запишите команды. Одна строка - одна команда"));
        layout.getChildren().add(new Label("PARTS 500 / Количество частиц"));
        layout.getChildren().add(new Label("STEPS 5000 / Количество максимальное шагов"));
        layout.getChildren().add(new Label("TEMPE 273 / Температура, в кельвинах (>0)"));
        layout.getChildren().add(new Label("TEMPS 1200 / Температура источника, в кельвинах (>0)"));
        layout.getChildren().add(new Label("AURUM / Латинское название вещества из списка по нажатию \"Вещество в симуляции\""));
        layout.getChildren().add(new Label("CAMSX 5 (Лево/право)/ Размеры камеры по осям. Могут быть дробными."));
        layout.getChildren().add(new Label("CAMSY 5 (Вверх/вниз)"));
        layout.getChildren().add(new Label("CAMSZ 5 (Дальше/ближе)"));
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
        layout.getChildren().add(new Label("PNGZA / Плотность заселения (PNG)"));
        layout.getChildren().add(new Label("CSVZA / Плотность заселения (CSV)"));
        layout.getChildren().add(new Label("CSVZE / Плотность заселения (CSV) на подложке"));
        layout.getChildren().add(new Label("CSVOU / Вывод в CSV"));
        layout.getChildren().add(new Label("PALIT 1 / Выбор палитры"));
        layout.getChildren().add(new Label("RESOL 20 / Делений на метр в выводе (для csv и png)"));
        layout.getChildren().add(new Label("START / Запустить симуляцию (После всех прочих команд!)"));
        layout.getChildren().add(new Label("DIMEN 3 / Количество осей"));
        layout.getChildren().add(new Label("WAITT 5 / Задержка между шагами"));
        layout.getChildren().add(new Label("VERWA 0 / Вероятность отражения от стен"));
        layout.getChildren().add(new Label("VERGE 0.9 / Вероятность отражения от генератора"));
        layout.getChildren().add(new Label("3DNOT / Не отрисовывать 3Д. (Отключает и задержку!)"));
        layout.getChildren().add(new Label("DISTC / Считать на сервере"));
        layout.getChildren().add(new Label("ADDRE 127.0.0.1 / Адрес сервера"));
        layout.getChildren().add(new Label("EMAIL example@exaple.com / Почта для отсылки результатов"));
        layout.getChildren().add(new Label("Очереди:"));
        layout.getChildren().add(new Label("RUNAN - Добавить одну симуляцию. "));
        layout.getChildren().add(new Label("RUNMO 10 -Добавить ещё 10 симуляций с текущими настройками в очередь. "));
        layout.getChildren().add(new Label("Не обязательно указывать все команды. Конфигурационный файл"));
        layout.getChildren().add(new Label("может быть как и в одну команду, так и в двадцать."));
        layout.getChildren().add(new Label("Перетащите получившийся файл в бирюзовую панель."));
        layout.getChildren().add(new Label("Строки без ключевых слов будут проигнорированы."));
        return layout;
    }
    public static SimDTO select(File file) {
        if (file.getName().contains(".txt")){
            Console.printLine('S');
            Console.coolPrintout("File accepted!");
            return load(file);
        } else
        if (file.getName().contains(".AS")){
            SimDTO result = null;
            try {
                result  = (SimDTO) new ObjectInputStream(new FileInputStream(file)).readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            return result;
        }
        else{
            Console.printLine('F');
            Console.coolPrintout("Wrong file format - .txt only!");
            return new SimDTO();
        }
    }

    private static SimDTO load(File file) {
        SimDTO result = new SimDTO();
        try {
            for (String line : Files.readAllLines(file.toPath())){
                if (line.contains("PARTS")){result.setParticleAm(Integer.parseInt(line.trim().replaceAll("PARTS ","")));} //int
                if (line.contains("STEPS")){result.setStepsAm(Integer.parseInt(line.trim().replaceAll("STEPS ","")));} //int
                if (line.contains("TEMPE")){result.setTempAm(Double.parseDouble(line.trim().replaceAll("TEMPE ","")));} //double
                if (line.contains("TEMPS")){result.setTempSourceAm(Double.parseDouble(line.trim().replaceAll("TEMPS ","")));} //double
                for (int i = 0; i < GasTypes.values().length; i++) {
                    if(line.contains(GasTypes.values()[i].name())){
                        result.setGas(GasTypes.values()[i]);
                    }
                }
                if (line.contains("CAMSX")){result.setXFrameLen(Double.parseDouble(line.trim().replaceAll("CAMSX ","")));} //double
                if (line.contains("CAMSY")){result.setYFrameLen(Double.parseDouble(line.trim().replaceAll("CAMSY ","")));} //double
                if (line.contains("CAMSZ")){result.setZFrameLen(Double.parseDouble(line.trim().replaceAll("CAMSZ ","")));} //double
                if (line.contains("PRES^")){result.setPressurePow(Double.parseDouble(line.trim().replaceAll("PRES\\^ ","")));} //double
                if (line.contains("PRES*")){result.setPressure(Double.parseDouble(line.trim().replaceAll("PRES\\* ","")));} //double
                if (line.contains("DRAWP")){result.setPathDrawing(true);} //bool
                if (line.contains("THREA")){result.setThreadCount(Integer.parseInt(line.trim().replaceAll("THREA ","")));} //int
                if (line.contains("XTARS")){result.setTarSizeX(Double.parseDouble(line.trim().replaceAll("XTARS ","")));} //double
                if (line.contains("ZTARS")){result.setTarSizeZ(Double.parseDouble(line.trim().replaceAll("ZTARS ","")));} //double
                if (line.contains("XGENE")){result.setGenSizeX(Double.parseDouble(line.trim().replaceAll("XGENE ","")));} //double
                if (line.contains("ZGENE")){result.setGenSizeZ(Double.parseDouble(line.trim().replaceAll("ZGENE ","")));} //double
                if (line.contains("DIRPA")){result.setOutputPath(new File(line.trim().replaceAll("DIRPA ","")));} //str
                if (line.contains("TEXTW")){result.setOutput(true);} //bool
                if (line.contains("PNGZA")){result.setOutputPic(true);} //bool
                if (line.contains("CSVZA")){result.setOutputRAWCord(true);} //bool
                if (line.contains("CSVZE")){result.setOutputPicCSVPost(true);} //bool
                if (line.contains("CSVOU")){result.setOutputCSV(true);}
                if (line.contains("PALIT")){result.setPalette(Integer.parseInt(line.trim().replaceAll("PALIT ","")));} //int
                if (line.contains("RESOL")){result.setResolution(Integer.parseInt(line.trim().replaceAll("RESOL ","")));}
                if (line.contains("START")){MainController.startSim(result,result.getServerAddress());} //bool
                if (line.contains("DIMEN")){result.setDimensionCount(Integer.parseInt(line.trim().replaceAll("DIMEN ","")));} //int
                if (line.contains("WAITT")){result.setWaitTime(Integer.parseInt(line.trim().replaceAll("WAITT ","")));} //int
                if (line.contains("VERWA")){result.setBounceWallChance(Double.parseDouble(line.trim().replaceAll("VERWA ","")));} //double
                if (line.contains("VERGE")){result.setBounceGenChance(Double.parseDouble(line.trim().replaceAll("VERGE ","")));} //double
                if (line.contains("3DNOT")){result.setOutput3D(false);}
                if (line.contains("DISTC")){result.setDistCalc(true);}
                if (line.contains("ADDRE")){result.setServerAddress(line.trim().replaceAll("ADDRE ",""));}
                if (line.contains("EMAIL")){result.setUserEmail(line.trim().replaceAll("EMAIL ",""));}
                if (line.contains("RUNAN")){MainController.currentStream.add(result);} //double
                if (line.contains("RUNMO")){
                    int more = Integer.parseInt(line.trim().replaceAll("RUNMO ", ""));
                    for (int i = 0; i < more; i++) {
                        MainController.currentStream.add(result);
                    }}
            }
    } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
