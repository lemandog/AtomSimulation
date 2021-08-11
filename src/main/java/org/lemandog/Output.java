package org.lemandog;

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
import org.lemandog.util.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

import static org.lemandog.App.mainFont;
import static org.lemandog.App.outputMode;
import static org.lemandog.util.SwingFXUtils.fromFXImage;

public class Output {
    static Image palette = new Image("heatmap2.png");
    public static int zSize = 10;
    public static int xSize = 10;
    static DirectoryChooser directoryChooserOutputPath = new DirectoryChooser();
    public static boolean output = false;
    public static boolean outputPic = false;
    public static boolean outputGraph = false;
    public static CheckBox outputAsk;
    public static CheckBox outputAskPic;
    public static CheckBox outputAskGraph;
    public static Slider outputAskPicResolution;
    public static Slider outputPallete;
    static double maxDepth;
    static double maxWidth;
    public static int[][] statesH;
    public static int[][] statesO;
    public static int[][] statesF;
    public static int[][] picState;
    public static int lastPrintStep = 1;
    static Stage setOutput = new Stage();
    static File selectedPath = new File(System.getProperty("user.home") + "/Desktop");
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

        Label textDesk = new Label("Текстовый вывод");
        textDesk.setFont(mainFont);
        textDesk.setTextFill(Color.BLUEVIOLET);
        compOutput.getChildren().add(textDesk);

        outputAsk = new CheckBox();
        outputAsk.setText("Автоматическая запись");
        outputAsk.setFont(mainFont);
        outputAsk.setOnAction(event -> output = !output);
        compOutput.getChildren().add(outputAsk);

        Label picDesk = new Label("Плотность заселения");
        picDesk.setFont(mainFont);
        picDesk.setTextFill(Color.BLUEVIOLET);
        compOutput.getChildren().add(picDesk);

        outputAskPic = new CheckBox();
        outputAskPic.setText("Вывод плотности заселения?");
        outputAskPic.setFont(mainFont);
        outputAskPic.setOnAction(event -> outputPic = !outputPic);
        compOutput.getChildren().add(outputAskPic);

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
        outputPallete.setMax(2);

        compOutput.getChildren().add(paletteText);
        compOutput.getChildren().add(currPal);
        compOutput.getChildren().add(outputPallete);

        Label graphDesk = new Label("Графопостроитель");
        graphDesk.setFont(mainFont);
        graphDesk.setTextFill(Color.BLUEVIOLET);
        compOutput.getChildren().add(graphDesk);

        outputAskGraph = new CheckBox();
        outputAskGraph.setText("Вывести график результатов");
        outputAskGraph.setFont(mainFont);
        outputAskGraph.setOnAction(event -> outputGraph = !outputGraph);
        compOutput.getChildren().add(outputAskGraph);

        setOutput.setScene(setOutputSc);
        setOutput.setResizable(false);

    }
    static public void disp(){
        if (setOutput.isShowing()){setOutput.hide();}
        else {setOutput.show();}

        if(output){
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
                //Тут чёрт ногу сломит, но происходит конвертация из типа в тип из за несовместимых библиотек.
                // А потом ещё раз, потому что мне нужно увеличить картинку
                Image res = toImage(picState); //Это javafx image
                BufferedImage tmp = fromFXImage(res, null); //Это awt
                //Конвертация в awt, потому как оно почему то возвращает awt image, а не buffered
                java.awt.Image res2 = tmp.getScaledInstance((int)maxWidth*10,(int)maxDepth*10,BufferedImage.SCALE_AREA_AVERAGING);
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
        if (output) {
            try {
                PrintWriter active = new PrintWriter(selectedPath.getAbsolutePath() + "/actives.txt");
                PrintWriter tarHits = new PrintWriter(selectedPath.getAbsolutePath() + "/tarHits.txt");
                PrintWriter outOfBounds = new PrintWriter(selectedPath.getAbsolutePath() + "/outOfBounds.txt");

                int thisStepWallHitSum = 0;
                int thisStepTarHitSum = 0;

                for (int y = 0; y < Sim.LEN; y++) {
                    int thisStepActiveSum = 0;
                    for (int x = 0; x < Sim.N; x++) {
                        thisStepActiveSum = thisStepActiveSum + statesF[x][y];
                        thisStepWallHitSum = thisStepWallHitSum + statesO[x][y];
                        thisStepTarHitSum = thisStepTarHitSum + statesH[x][y];
                    }
                    active.println(thisStepActiveSum);
                    outOfBounds.println(thisStepWallHitSum);
                    tarHits.println(thisStepTarHitSum);

                }

                active.close();
                tarHits.close();
                outOfBounds.close();
                System.out.println("RESULTS ARE SAVED AT " + selectedPath.getAbsolutePath());
            } catch (FileNotFoundException e) {
                System.out.println("IT SEEMS, THAT DIRECTORY TO WHICH YOU WANT TO SAVE RESULTS IS READ ONLY OR UNAVAILABLE." +
                        "TRY TO SAVE AGAIN, BY ENTERING DIFFERENT DIRECTORY IN OUTPUT OPTIONS AND PRESSING SAVE RESULTS");
                e.printStackTrace();
            }
        }
        if(outputGraph){
            File outputfile = new File(selectedPath.getAbsolutePath() + "/stateGraph.png");
            try {
                ImageIO.write(Objects.requireNonNull(fromFXImage(toGraph(), null)), "png", outputfile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        System.out.println("LAST MODEL CHANGE STEP "+ lastPrintStep);
        }
    public static Color colSel(int sel){        //0-9
        PixelReader randColRead = palette.getPixelReader();
        return randColRead.getColor(sel,0);}

    private static Image toGraph(){
        WritableImage writeHere = new WritableImage(lastPrintStep,Sim.N+1);
        PixelWriter pen = writeHere.getPixelWriter();
        int thisStepWallHitSum = 0;
        int thisStepTarHitSum = 0;
        for (int x = 0; x < lastPrintStep; x++) {
            int thisStepActiveSum = 0;
            for (int y = 0; y < Sim.N; y++) {
                pen.setColor(x,y,Color.WHITE);
            }
            for (int y = 0; y < Sim.N; y++) {
                //Х и У тут поменялись местами - дело в том, что изначально эта матрица была "повёрнута на 90 гр"
                thisStepActiveSum = thisStepActiveSum + statesF[y][x];
                thisStepWallHitSum = thisStepWallHitSum + statesO[y][x];
                thisStepTarHitSum = thisStepTarHitSum + statesH[y][x];
            }
            pen.setColor(x,Sim.N - thisStepActiveSum,Color.BLACK);
            pen.setColor(x,Sim.N - thisStepWallHitSum,Color.RED);
            pen.setColor(x,Sim.N - thisStepTarHitSum,Color.BLUE);
    }

        return writeHere;
}

    private static Image toImage(int[][] modelRes){
        WritableImage writeHere = new WritableImage((int) maxWidth,(int) maxDepth);
        PixelWriter outPix = writeHere.getPixelWriter();

        int biggest = 0;
        for (int x = 0; x<writeHere.getWidth();x++){
            for (int y = 0; y<writeHere.getHeight();y++) {
                if(modelRes[x][y] > biggest){biggest = modelRes[x][y];} //Ищем наибольшее
            }}

        biggest = biggest/((int)palette.getWidth() + 1); //делим на количество цветов в палитре, так, что значения в диапозоне 0-9

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
            picState[(int) ((int) (maxWidth/2) + xCord)][(int) ((int) (maxDepth/2) + zCord)] += 1;
        }
    }

    public static void setTargetSize(Bounds target) {
        maxWidth = target.getWidth();
        maxDepth = target.getDepth();
    }
}
