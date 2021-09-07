package org.lemandog.util;

import com.opencsv.CSVWriter;
import javafx.geometry.Bounds;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.lemandog.App;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;

import static org.lemandog.App.mainFont;
import static org.lemandog.App.outputMode;
import static org.lemandog.util.SwingFXUtils.fromFXImage;

public class Output {
    static DateTimeFormatter sdfF = DateTimeFormatter.ofPattern("dd=MM=yyyy-HH=mm=ss");
    public static Image palette = new Image("heatmap2.png");
    public static int zSize = 10;
    public static int xSize = 10;
    public static DirectoryChooser directoryChooserOutputPath = new DirectoryChooser();
    public static boolean output = false;
    public static boolean outputPic = false;
    public static boolean outputPicCSV = false;
    public static boolean outputCSV = false;
    public static boolean output3D = true;
    public static CheckBox output3d;
    public static CheckBox outputAsk;
    public static CheckBox outputAskPic;
    public static CheckBox outputAskCSVHits;
    public static CheckBox outputAskGraph;
    public static Slider outputAskPicResolution;
    public static Slider outputPallete;
    public static double maxDepth;
    public static double maxWidth;
    public static int[][] picState;
    public static int lastPrintStep = 1;
    static Stage setOutput = new Stage();
    static File selectedPath = new File(System.getProperty("user.home") + "/Desktop");
    static CSVWriter global;
    static CSVWriter global2;
    public static void ConstructOutputAFrame() {
        VBox compOutput = new VBox();
        Scene setOutputSc = new Scene(compOutput,500,500);

        setOutput.setOnCloseRequest(windowEvent -> disp());


        Button dirChoBut = new Button("Выбор директории вывода");
        Label choDir = new Label();
        choDir.setFont(mainFont);
        choDir.setText(selectedPath.getAbsolutePath());
        dirChoBut.setFont(mainFont);
        dirChoBut.setOnAction(e -> {
            selectedPath = directoryChooserOutputPath.showDialog(setOutput);
            if(selectedPath == null) {
                selectedPath = new File(System.getProperty("user.home") + "/Desktop");
            }
            choDir.setText(selectedPath.getAbsolutePath());
        });
        compOutput.getChildren().add(dirChoBut);
        compOutput.getChildren().add(choDir);

        Button returnBut = new Button("Вернуться назад");
        returnBut.setFont(mainFont);
        returnBut.setOnAction(e -> Output.disp());
        Button saveResNow = new Button("Сохранить результат");
        saveResNow.setFont(mainFont);
        saveResNow.setOnAction(e -> Output.toFile());

        compOutput.getChildren().add(saveResNow);
        compOutput.getChildren().add(returnBut);

        output3d = new CheckBox();
        output3d.setText("Показывать каждый шаг перемещения частиц");
        output3d.setFont(mainFont);
        output3d.setSelected(output3D);
        output3d.setOnAction(event -> output3D = !output3D);
        compOutput.getChildren().add(output3d);

        Label textDesk = new Label("Текстовый вывод");
        textDesk.setFont(mainFont);
        textDesk.setTextFill(Color.BLUEVIOLET);
        compOutput.getChildren().add(textDesk);

        outputAskGraph = new CheckBox();
        outputAskGraph.setText("Вывести CSV");
        outputAskGraph.setFont(mainFont);
        outputAskGraph.setOnAction(event -> outputCSV = !outputCSV);
        compOutput.getChildren().add(outputAskGraph);

        Label picDesk = new Label("Плотность заселения");
        picDesk.setFont(mainFont);
        picDesk.setTextFill(Color.BLUEVIOLET);
        compOutput.getChildren().add(picDesk);

        outputAskPic = new CheckBox();
        outputAskPic.setText("Вывод плотности заселения в .PNG?");
        outputAskPic.setFont(mainFont);
        outputAskPic.setOnAction(event -> outputPic = !outputPic);
        compOutput.getChildren().add(outputAskPic);

        outputAskCSVHits = new CheckBox();
        outputAskCSVHits.setText("Вывод плотности заселения в CSV?");
        outputAskCSVHits.setFont(mainFont);
        outputAskCSVHits.setOnAction(event -> outputPicCSV = !outputPicCSV);
        compOutput.getChildren().add(outputAskCSVHits);

        outputAsk = new CheckBox();
        outputAsk.setText("Вывод плотности заселения в .TXT?");
        outputAsk.setFont(mainFont);
        outputAsk.setOnAction(event -> output = !output);
        compOutput.getChildren().add(outputAsk);

        Label resolutionWarnText = new Label("Не ставьте большое разрешение для больших подложек!");
        resolutionWarnText.setFont(mainFont);
        compOutput.getChildren().add(resolutionWarnText);

        outputAskPicResolution = new Slider();
        outputAskPicResolution.setMaxWidth(setOutputSc.getWidth()/3);
        outputAskPicResolution.setValue(3);
        Label resolutionText = new Label("Разрешение сейчас: " + String.format("%3.0f", outputAskPicResolution.getValue()));
        resolutionText.setFont(mainFont);
        outputAskPicResolution.setOnMouseReleased((event) -> {
            resolutionText.setText("Разрешение сейчас: "+ String.format("%3.0f", outputAskPicResolution.getValue())); // Три знака всего, два после запятой
        });
        outputAskPicResolution.setMin(1);
        outputAskPicResolution.setMax(10);

        compOutput.getChildren().add(resolutionText);
        compOutput.getChildren().add(outputAskPicResolution);

        outputPallete = new Slider();
        outputPallete.setMaxWidth(setOutputSc.getWidth()/3);
        outputPallete.setValue(2);
        Label paletteText = new Label("Выбор палитры");
        ImageView currPal = new ImageView("/heatmap"+(int) outputPallete.getValue()+".png");
        paletteText.setFont(mainFont);
        currPal.setScaleX(setOutputSc.getWidth()/10);
        currPal.setScaleY(10);
        outputPallete.setOnMouseReleased((event) -> currPal.setImage(new Image("/heatmap"+(int) outputPallete.getValue()+".png")));
        outputPallete.setMin(1);
        outputPallete.setMax(4);

        compOutput.getChildren().add(paletteText);
        compOutput.getChildren().add(currPal);
        compOutput.getChildren().add(outputPallete);

        setOutput.setScene(setOutputSc);
        setOutput.setResizable(false);

    }
    static public void disp(){
        if (setOutput.isShowing()){setOutput.hide();}
        else {setOutput.show();}

        if(output || outputPic || outputCSV){
            outputMode.setText("Вывод включен!");
            outputMode.setTextFill(Color.DARKOLIVEGREEN);
        }
        else{
            outputMode.setText("Вывод выключен!");
            outputMode.setTextFill(Color.INDIANRED);
        }

    }

    public static void toFile() {
        if (outputPic){
            File outputfile = new File(selectedPath.getAbsolutePath() + "/hitsDetector.png");
            try {
                //Тут чёрт ногу сломит, но происходит конвертация из типа в тип из-за несовместимых библиотек.
                // А потом ещё раз, потому что мне нужно увеличить картинку
                Image res = toImage(picState); //Это javafx image
                BufferedImage tmp = fromFXImage(res, null); //Это awt
                //Конвертация в awt, потому как оно почему то возвращает awt image, а не buffered
                assert tmp != null;
                java.awt.Image res2 = tmp.getScaledInstance((int)maxWidth*10,(int)maxDepth*10,BufferedImage.SCALE_FAST);
                //Конвертация обратно в buffered
                BufferedImage bimage = new BufferedImage(res2.getWidth(null), res2.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bGr = bimage.createGraphics();
                bGr.drawImage(res2, 0, 0, null);
                bGr.dispose();
                //Пишем bufferedImage стандартной библиотекой
                ImageIO.write(bimage, "png", outputfile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputCSV) {
            try {
                global.flush();
                System.out.println("GLOBAL STREAM CLOSED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputPicCSV) {
            try {
                global2.flush();
                System.out.println("GLOBAL STREAM2 CLOSED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("LAST MODEL CHANGE STEP "+ lastPrintStep);
        }
    public static Color colSel(int sel){        //0-9
        PixelReader randColRead = palette.getPixelReader();
        return randColRead.getColor(sel,0);}

    private static Image toImage(int[][] modelRes){
        WritableImage writeHere = new WritableImage((int) maxWidth,(int) maxDepth);
        PixelWriter outPix = writeHere.getPixelWriter();

        int biggest = 0;
        for (int x = 0; x<writeHere.getWidth();x++){
            for (int y = 0; y<writeHere.getHeight();y++) {
                if(modelRes[x][y] > biggest){biggest = modelRes[x][y];} //Ищем наибольшее
            }}

        biggest = biggest/((int)palette.getWidth()); //делим на количество цветов в палитре, так, что значения в диапозоне 0-9

        for (int x = 0; x<xSize;x++){
            for (int y = 0; y<zSize;y++) {
                modelRes[x][y] = modelRes[x][y]/(biggest+1); //Привод к нужным для вывода значениям
            }}

        for (int x = 0; x<xSize;x++){
            for (int y = 0; y<zSize;y++) {
                outPix.setColor(x,y,colSel(modelRes[x][y]));//Вывод из палитры
            }}
        return writeHere;
    }
    public static void picStateReact(double xCord, double zCord){
        if (outputPic){
            picState[(int) ((maxWidth/2) + xCord)][(int) ((maxDepth/2) + zCord)] += 1;
        }
    }

    public static void setTargetSize(Bounds target) {
        maxWidth = target.getWidth();
        maxDepth = target.getDepth();
    }
    public static void insertValuesToSCV(double[] cord, int passed, int ordinal){
        if (global == null){
            CSVWriterBuild();
        }
        Vector<String> ve = new Vector<>(0);
        for (double v : cord) {
            ve.add(String.valueOf(v));
        }
        ve.add(String.valueOf(passed));
        ve.add(String.valueOf(ordinal));
        String[] output = new String[ve.size()]; //Мы не знаем, сколько там измерений
        for (int i = 0; i < ve.size(); i++) {
            output[i] = ve.get(i);
        }
        global.writeNext(output);
    }

    public static void CSVWriterBuild() {
        try {
            LocalDateTime main = LocalDateTime.now();
            File csv = new File(selectedPath.getAbsolutePath()
                    + "/ParticleStates"+sdfF.format(main)+".csv");
            global = new CSVWriter(new FileWriter(csv),
                    ';',
                    CSVWriter.NO_QUOTE_CHARACTER,
                    CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                    CSVWriter.RFC4180_LINE_END);
            Vector<String> ve = new Vector<>(0);
            for (int i = 0; i < App.dimensionCount.getValue(); i++) {
                ve.add("КООРДИНАТА "+i);
            }
            ve.add("ПРОШЕДШИЕ ШАГИ ");
            ve.add("НОМЕР ЧАСТИЦЫ ИЗ "+ App.particleAm.getText());
            ve.add("ТЕМПЕРАТУРА: "+ App.tempAm.getText());
            String[] output = new String[ve.size()]; //Мы не знаем, сколько там измерений
            for (int i = 0; i < ve.size(); i++) {
                output[i] = ve.get(i);
            }
            global.writeNext(output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void CSVStateReact(double translateX, double translateZ) {
        if (outputPicCSV){
        if (global2 == null){
            LocalDateTime main = LocalDateTime.now();
            File csv = new File(selectedPath.getAbsolutePath()
                    + "/Hits"+sdfF.format(main)+".csv");
            try {
                global2 = new CSVWriter(new FileWriter(csv),
                        ';',
                        CSVWriter.NO_QUOTE_CHARACTER,
                        CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                        CSVWriter.RFC4180_LINE_END);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        global2.writeNext(new String[]{String.valueOf(translateX),String.valueOf(translateZ)});
    }
    }
}
