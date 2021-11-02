package org.lemandog.util;

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
    public static double zSize = 10;
    public static double xSize = 10;
    public static DirectoryChooser directoryChooserOutputPath = new DirectoryChooser();
    public static boolean output = false;
    public static boolean outputPic = false;
    public static boolean outputPicCSV = false;
    public static boolean outputCSV = false;
    public static boolean output3D = true;
    public static CheckBox output3dCHK;
    public static CheckBox outputAskPic;
    public static CheckBox outputAskCSVHits;
    public static CheckBox outputAskGraph;
    public static Slider outputAskPicResolution;
    public static Slider outputPalette;
    public static int[][] picState;
    public static int lastPrintStep = 1;
    public static double DOTSIZE;
    static Stage setOutput = new Stage();
    static File selectedPath = new File(System.getProperty("user.home") + "/Desktop");
    static FileWriter global;
    static FileWriter global2;
    public static CheckBox pathDrawing;

    static final String LINE_END = "\r\n";
    static final String SEPARATOR = ";";

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

        pathDrawing = new CheckBox("Отрисовка путей (Только малое число частиц)");
        pathDrawing.setFont(mainFont);
        compOutput.getChildren().add(pathDrawing);

        Button returnBut = new Button("Вернуться назад");
        returnBut.setFont(mainFont);
        returnBut.setOnAction(e -> Output.disp());
        compOutput.getChildren().add(returnBut);

        output3dCHK = new CheckBox();
        output3dCHK.setText("Показывать перемещения частиц");
        output3dCHK.setFont(mainFont);
        output3dCHK.setSelected(output3D);
        output3dCHK.setOnAction(event -> output3D = output3dCHK.isSelected());
        compOutput.getChildren().add(output3dCHK);

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

        Label resolutionWarnText = new Label("Не ставьте большое разрешение для больших подложек!");
        resolutionWarnText.setFont(mainFont);
        compOutput.getChildren().add(resolutionWarnText);

        outputAskPicResolution = new Slider();
        outputAskPicResolution.setMaxWidth(setOutputSc.getWidth()/3);
        outputAskPicResolution.setValue(0.5);
        Label resolutionText = new Label("Разрешение сейчас: " + String.format("%3.2f", outputAskPicResolution.getValue()));
        resolutionText.setFont(mainFont);
        outputAskPicResolution.setOnMouseReleased((event) -> {
            resolutionText.setText("Разрешение сейчас: "+ String.format("%3.2f", outputAskPicResolution.getValue())); // Три знака всего, два после запятой
        });
        outputAskPicResolution.setMin(0.01);
        outputAskPicResolution.setMax(1);

        compOutput.getChildren().add(resolutionText);
        compOutput.getChildren().add(outputAskPicResolution);

        outputPalette = new Slider();
        outputPalette.setMaxWidth(setOutputSc.getWidth()/3);
        outputPalette.setValue(2);
        Label paletteText = new Label("Выбор палитры");
        ImageView currPal = new ImageView("/heatmap"+(int) outputPalette.getValue()+".png");
        paletteText.setFont(mainFont);
        currPal.setScaleX(setOutputSc.getWidth()/10);
        currPal.setScaleY(10);
        outputPalette.setOnMouseReleased((event) -> currPal.setImage(new Image("/heatmap"+(int) outputPalette.getValue()+".png")));
        outputPalette.setMin(1);
        outputPalette.setMax(4);

        compOutput.getChildren().add(paletteText);
        compOutput.getChildren().add(currPal);
        compOutput.getChildren().add(outputPalette);

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
            File outputFile = new File(selectedPath.getAbsolutePath() + "/hitsDetector.png");
            try {
                //Тут чёрт ногу сломит, но происходит конвертация из типа в тип из-за несовместимых библиотек.
                // А потом ещё раз, потому что мне нужно увеличить картинку
                Image res = toImage(picState); //Это javafx image
                BufferedImage tmp = fromFXImage(res, null); //Это awt
                //Конвертация в awt, потому как оно почему-то возвращает awt image, а не buffered
                assert tmp != null;
                java.awt.Image res2 = tmp.getScaledInstance(1200,1200,BufferedImage.SCALE_FAST);
                //Конвертация обратно в buffered
                BufferedImage bImage = new BufferedImage(res2.getWidth(null), res2.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bGr = bImage.createGraphics();
                bGr.drawImage(res2, 0, 0, null);
                bGr.dispose();
                //Пишем bufferedImage стандартной библиотекой
                ImageIO.write(bImage, "png", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputCSV) {
            try {
                global.close();
                System.out.println("GLOBAL STREAM ParticleStates CLOSED");
                global=null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputPicCSV) {
            try {
                global2.close();
                System.out.println("GLOBAL STREAM Hits CLOSED");
                global2=null;
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
        WritableImage writeHere = new WritableImage((int) xSize,(int) zSize);
        PixelWriter outPix = writeHere.getPixelWriter();

        int biggest = 0;
        for (int x = 0; x<(int) xSize;x++){
            for (int y = 0; y<(int) zSize;y++) {
                if(modelRes[x][y] > biggest){biggest = modelRes[x][y];} //Ищем наибольшее
            }}

        biggest = biggest/((int)palette.getWidth()); //делим на количество цветов в палитре, так, что значения в диапазоне 0-9

        for (int x = 0; x<(int)xSize;x++){
            for (int y = 0; y<(int)zSize;y++) {
                modelRes[x][y] = modelRes[x][y]/(biggest+1); //Привод к нужным для вывода значениям
            }}

        for (int x = 0; x<(int)xSize;x++){
            for (int y = 0; y<(int)zSize;y++) {
                outPix.setColor(x,y,colSel(modelRes[x][y]));//Вывод из палитры
            }}
        return writeHere;
    }
    public static void picStateReact(double xCord, double zCord){
        if (outputPic){
            fromhere:
            for(double x = 1; x< xSize; x += DOTSIZE){
                for(double y = 1; y< zSize; y += DOTSIZE){
                    if( ((1f/DOTSIZE)*(x-1) - xSize/2 <xCord/DOTSIZE && xCord/DOTSIZE<(1f/DOTSIZE)*x - xSize/2)
                            &&((1f/DOTSIZE)*(y-1) - zSize/2<zCord/DOTSIZE && zCord/DOTSIZE<(1f/DOTSIZE)*y - zSize/2)) {
                        picState[(int) (x/DOTSIZE)][(int) (y/DOTSIZE)]++;
                        break fromhere;
                    }

                }
            }

        }
    }

    synchronized public static void insertValuesToSCV(double[] cord, int passed, int ordinal){
        if (global == null){
            try {
                LocalDateTime main = LocalDateTime.now();
                File csv = new File(selectedPath.getAbsolutePath()
                        + "/"+App.simQueue.size()+"ParticleStates"+sdfF.format(main)+".csv");
                global = new FileWriter(csv);
                Vector<String> ve = new Vector<>(0);
                for (int i = 0; i < App.dimensionCount.getValue(); i++) {
                    ve.add("КООРДИНАТА "+i +SEPARATOR);
                }
                ve.add("ПРОШЕДШИЕ ШАГИ "+SEPARATOR);
                ve.add("НОМЕР ЧАСТИЦЫ ИЗ "+ App.particleAm.getText()+SEPARATOR);
                ve.add("ТЕМПЕРАТУРА: "+ App.tempAm.getText()+SEPARATOR);
                for (String s : ve) {
                    global.write(s);
                }
                global.write(LINE_END);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Vector<String> ve = new Vector<>(0);
        for (double v : cord) {
            ve.add(v + SEPARATOR);
        }
        ve.add(passed + SEPARATOR);
        ve.add(ordinal + SEPARATOR);
        String[] output = new String[ve.size()]; //Мы не знаем, сколько там измерений
        for (int i = 0; i < ve.size(); i++) {
            output[i] = ve.get(i);
        }
        try {
            for (String s : output) {
                global.write(s);
            }
            global.write(LINE_END);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    synchronized public static void CSVStateReact(double translateX, double translateZ) {
        if (outputPicCSV){
        if (global2 == null){
            LocalDateTime main = LocalDateTime.now();
            File csv = new File( selectedPath.getAbsolutePath()
                    + "/"+App.simQueue.size()+"Hits"+sdfF.format(main)+".csv");
            try {
                global2 = new FileWriter(csv);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
            try {
                global2.write(translateX +SEPARATOR+ translateZ + LINE_END);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
