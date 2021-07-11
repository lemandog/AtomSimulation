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

import static org.lemandog.App.mainFont;
import static org.lemandog.App.outputMode;

public class Output {
    static DirectoryChooser directoryChooserOutputPath = new DirectoryChooser();
    public static boolean output = false;
    public static CheckBox outputAsk;
    public static CheckBox outputAskPic;
    public static Slider outputAskPicResolution;
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

        compOutput.getChildren().add(choDir);
        compOutput.getChildren().add(dirChoBut);
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
}
