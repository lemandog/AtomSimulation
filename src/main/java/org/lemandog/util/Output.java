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
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.lemandog.App.mainFont;
import static org.lemandog.App.outputMode;
import static org.lemandog.Sim.currentSim;
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
    public static boolean outputPicCSVPost = false;
    public static boolean outputCSV = false;
    public static boolean output3D = true;
    public static CheckBox output3dCHK;
    public static CheckBox outputAskPic;
    public static CheckBox outputAskCSVHits;
    public static CheckBox outputAskCSVHitsPost;
    public static CheckBox outputAskGraph;
    public static Slider outputAskPicResolution;
    public static Slider outputPalette;
    public static int[][] picState;
    static Vector<Double> X = new Vector<>();
    static Vector<Double> Z = new Vector<>();
    public static int lastPrintStep = 1;
    public static double DOTSIZE;
    static Stage setOutput = new Stage();
    static FileWriter Hits, enchantedHits;
    public static CheckBox pathDrawing;
    public static Label choDir = new Label();

    static final String LINE_END = "\r\n";
    static final String SEPARATOR = ";";

    public static void ConstructOutputAFrame() {
        VBox compOutput = new VBox();
        Scene setOutputSc = new Scene(compOutput,500,500);

        setOutput.setOnCloseRequest(windowEvent -> disp());


        Button dirChoBut = new Button("Выбор директории вывода");

        choDir.setFont(mainFont);
        dirChoBut.setFont(mainFont);
        choDir.setText(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath());
        dirChoBut.setOnAction(e -> choDir.setText(directoryChooserOutputPath.showDialog(setOutput).getAbsolutePath()));
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

        outputAskCSVHitsPost = new CheckBox();
        outputAskCSVHitsPost.setText("Вывод плотности заселения в CSV с пост-обработкой?");
        outputAskCSVHitsPost.setFont(mainFont);
        outputAskCSVHitsPost.setOnAction(event -> outputPicCSVPost = !outputPicCSVPost);
        compOutput.getChildren().add(outputAskCSVHitsPost);

        Label resolutionWarnText = new Label("Не ставьте большое разрешение для больших подложек!");
        resolutionWarnText.setFont(mainFont);
        compOutput.getChildren().add(resolutionWarnText);

        outputAskPicResolution = new Slider();
        outputAskPicResolution.setMaxWidth(setOutputSc.getWidth()/3);
        outputAskPicResolution.setValue(5);
        Label resolutionText = new Label("Разрешение сейчас: " + (int)outputAskPicResolution.getValue());
        resolutionText.setFont(mainFont);
        outputAskPicResolution.setOnMouseReleased((event) -> {
            resolutionText.setText("Разрешение сейчас: "+ (int)outputAskPicResolution.getValue()); // Три знака всего, два после запятой
        });
        outputAskPicResolution.setMin(1);
        outputAskPicResolution.setMax(100);

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
        double Xmax = currentSim.TAR_SIZE[0];
        double Zmax = currentSim.TAR_SIZE[2];
        int DOTSIZE = (int) outputAskPicResolution.getValue();
        int[][] CORD = new int[(int) (Xmax * (double) DOTSIZE)+1][(int) (Zmax * (double) DOTSIZE)+1];
        if (outputPicCSV) {
            try {
                for (int i = 0; i < X.size(); i++) {
                    Hits.write(X.get(i) +SEPARATOR+ Z.get(i) + LINE_END);
                }
                Hits.close();
                System.out.println("GLOBAL STREAM Hits CLOSED");
                Hits =null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputPicCSVPost || outputPic) {
            for (int[] ints : CORD) {
                Arrays.fill(ints, 0);
            }
            int height = CORD.length;
            int width = CORD[0].length;

            for (int i = 0; i < X.size(); ++i) {

                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        if ((double) (1.0F / (float) DOTSIZE * (float) (x - 1)) - Xmax / 2.0D < X.get(i) &&
                                X.get(i) < (double) (1.0F / (float) DOTSIZE * (float) x) - Xmax / 2.0D &&
                                (double) (1.0F / (float) DOTSIZE * (float) (y - 1)) - Zmax / 2.0D < Z.get(i) &&
                                Z.get(i) < (double) (1.0F / (float) DOTSIZE * (float) y) - Zmax / 2.0D) {
                            CORD[x][y]++;
                        }

                    }
                }
                if (i%X.size() == 0){System.out.println("progress: " + ((double)i/X.size())*100 + " %");}
            }
            if(outputPicCSVPost){
                try {
                    for (int x = 0; x < width; ++x) {
                        for (int y = 0; y < height; ++y) {
                            enchantedHits.write(CORD[x][y] + SEPARATOR);
                        }
                        enchantedHits.write(LINE_END);
                    }
                    enchantedHits.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("GLOBAL DATA DEALER STREAM CLOSED");
                enchantedHits = null;
            }
        }
        if (outputPic){
            File outputFile = new File(currentSim.selectedPath.getAbsolutePath() + "/"+App.simQueue.size()+"hitsDetector.png");
            try {
                //Тут чёрт ногу сломит, но происходит конвертация из типа в тип из-за несовместимых библиотек.
                // А потом ещё раз, потому что мне нужно увеличить картинку
                Image res = toImage(CORD); //Это javafx image
                BufferedImage tmp = fromFXImage(res, null); //Это awt
                //Конвертация в awt, потому как оно почему-то возвращает awt image, а не buffered
                assert tmp != null;
                java.awt.Image resize = tmp.getScaledInstance(tmp.getWidth(null)*10, tmp.getHeight(null)*10,BufferedImage.SCALE_FAST);
                BufferedImage bImage = new BufferedImage(resize.getWidth(null),resize.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bGr = bImage.createGraphics();
                bGr.drawImage(resize, 0, 0, null);
                bGr.dispose();
                //Пишем bufferedImage стандартной библиотекой
                ImageIO.write(bImage, "png", outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("LAST MODEL CHANGE STEP "+ lastPrintStep);
        }
    public static Color colSel(int sel){        //0-9
        PixelReader randColRead = palette.getPixelReader();
        return randColRead.getColor(sel,0);}

    private static Image toImage(int[][] CORD){
        int height = CORD.length;
        int width = CORD[0].length;
        WritableImage writeHere = new WritableImage(width,height);
        PixelWriter outPix = writeHere.getPixelWriter();

        int biggest = 0;
        for (int x = 0; x<width;x++){
            for (int y = 0; y<height;y++) {
                if(CORD[x][y] > biggest){biggest = CORD[x][y];} //Ищем наибольшее
            }}


        biggest = biggest/((int)palette.getWidth()); //делим на количество цветов в палитре, так, что значения в диапазоне 0-9

        for (int x = 0; x<width;x++){
            for (int y = 0; y<height;y++) {
                CORD[x][y] = CORD[x][y]/(biggest+1); //Привод к нужным для вывода значениям
            }}

        for (int x = 0; x<width;x++){
            for (int y = 0; y<height;y++) {
                outPix.setColor(x,y,colSel(CORD[x][y]));//Вывод из палитры
            }
        }
        return writeHere;
    }
    public static void init(){
        if (outputPicCSV){
            if (Hits == null){
                LocalDateTime main = LocalDateTime.now();
                File csv = new File( currentSim.selectedPath.getAbsolutePath()
                        + "/"+App.simQueue.size()+"Hits"+sdfF.format(main)+".csv");
                try {
                    Hits = new FileWriter(csv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(outputPicCSVPost) {
            if (enchantedHits == null) {
                File csvOut = new File(currentSim.selectedPath.getAbsolutePath()
                        + "/out.csv");
                try {
                    enchantedHits = new FileWriter(csvOut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    synchronized public static void CSVStateReact(double translateX, double translateZ) {
        X.add(translateX);
        Z.add(translateZ);
    }
}
