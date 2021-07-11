package org.lemandog;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.lemandog.App.mainFont;
import static org.lemandog.App.outputMode;

public class Output {

    static DirectoryChooser directoryChooserOutputPath = new DirectoryChooser();
    public static boolean output = false;
    public static CheckBox outputAsk;
    public static CheckBox outputAskPic;
    public static Slider outputAskPicResolution;
    public static int[][] statesH;
    public static int[][] statesO;
    public static int[][] statesF;
    static Stage setOutput = new Stage();
    static File selectedPath = new File(System.getProperty("user.home") + "/Desktop");
    public static void ConstructOutputAFrame() {

        VBox compOutput = new VBox();
        Scene setOutputSc = new Scene(compOutput,500,300);

        setOutput.setOnCloseRequest(windowEvent -> {
                disp();
        });

        outputAsk = new CheckBox();
        outputAsk.setText("Вывод данных");
        outputAsk.setFont(mainFont);
        outputAsk.setOnAction(event -> {
            output = !output;
        });
        compOutput.getChildren().add(outputAsk);

        outputAskPic = new CheckBox();
        outputAskPic.setText("Вывод плотности заселения?");
        outputAskPic.setFont(mainFont);
        compOutput.getChildren().add(outputAskPic);

        Label resolutionWarnText = new Label("Не ставьте большое разрешение для больших подложек!");
        resolutionWarnText.setFont(mainFont);
        compOutput.getChildren().add(resolutionWarnText);

        outputAskPicResolution = new Slider();
        outputAskPicResolution.setPrefWidth(setOutputSc.getWidth());
        Label resolutionText = new Label("Разрешение сейчас: " + 10);
        resolutionText.setFont(mainFont);
        outputAskPicResolution.setOnMouseReleased((event) -> {
            resolutionText.setText("Разрешение сейчас: "+ String.format("%3.0f", outputAskPicResolution.getValue())); // Три знака всего, два после запятой
        });
        outputAskPicResolution.setValue(10);
        outputAskPicResolution.setMin(1);
        outputAskPicResolution.setMax(100);

        compOutput.getChildren().add(resolutionText);
        compOutput.getChildren().add(outputAskPicResolution);

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
        Button returnBut = new Button("Вернуться назад");
        returnBut.setFont(mainFont);
        returnBut.setOnAction(e -> {
            Output.disp();
        });
        Button saveResNow = new Button("Сохранить результат");
        saveResNow.setFont(mainFont);
        saveResNow.setOnAction(e -> {
            Output.toFile();
        });

        compOutput.getChildren().add(choDir);
        compOutput.getChildren().add(dirChoBut);
        compOutput.getChildren().add(saveResNow);
        compOutput.getChildren().add(returnBut);

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
        try {
            PrintWriter active = new PrintWriter(selectedPath.getAbsolutePath() + "/actives.txt");
            PrintWriter tarHits = new PrintWriter(selectedPath.getAbsolutePath() + "/tarHits.txt");
            PrintWriter outOfBounds = new PrintWriter(selectedPath.getAbsolutePath() + "/outOfBounds.txt");

            int thisStepWallHitSum=0;
            int thisStepTarHitSum=0;

            for(int y=0;y<Sim.LEN;y++){
                int thisStepActiveSum=0;
                for(int x=0;x<Sim.N;x++){
                    thisStepActiveSum = thisStepActiveSum + statesF[x][y];
                    thisStepWallHitSum= thisStepWallHitSum + statesO[x][y];
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
}
