package org.lemandog;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.lemandog.util.*;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Objects;
import java.util.ResourceBundle;

import static org.lemandog.Sim.currentSim;

public class App extends Application {
    public Slider targetSizeZ;
    public Slider targetSizeX;
    public Slider genSizeZ;
    public Slider genSizeX;
    public Slider bounceWallChance;
    public Slider bounceGenChance;
    public TextField threadCount;
    public TextField waitTime;
    public TextField dimensionCount;
    public TextField xFrameLen;
    public TextField yFrameLen;
    public TextField zFrameLen;
    public TextField particleAm;
    public TextField stepsAm;
    public TextField tempAm;
    public TextField tempSourceAm;
    public TextField pressurePow;
    public TextField pressure;
    public static Scene scene;

    public static ArrayDeque<Sim> simQueue = new ArrayDeque<>();
    public static final Font mainFont = Font.loadFont(Objects.requireNonNull(App.class.getResource("/gost-type-a.ttf")).toExternalForm(), 20); //Подгрузка шрифта
    public static final Font mainFont2 = Font.loadFont(Objects.requireNonNull(App.class.getResource("/gost-type-a.ttf")).toExternalForm(), 16); //Подгрузка шрифта

    public CheckBox RAWCordOutput;
    public CheckBox PicCSVOutput;
    public CheckBox PicPNGOutput;
    public CheckBox particlesDraw;
    public TextField pathToOutput;
    public Slider paletteSelect;
    public ImageView paletteView;

    public TextField ServerAddress;
    public TextField userEmail;
    public WebView serverResponce;
    public CheckBox serverCalculate;

    public ChoiceBox<GasTypes> materialChooser;
    public Text atomRad;
    public Text atomMass;

    public Text numberInQueue;

    public WebView configView;

    @FXML
    void initialize(){
        System.out.println("hello");
    }
    @Override
    public void start(Stage stage){
        Console.ready();
        try {
        Stage userControlWindows = new Stage();
        userControlWindows.setResizable(false);
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/UI/main.fxml"));
        Parent root = loader.load();
        scene = new Scene(root);
        userControlWindows.setScene(scene);
        Image icon = new Image(App.class.getResourceAsStream("/icons/atomSim.png"));
        userControlWindows.getIcons().add(icon);
        userControlWindows.setTitle("Контроль");
        userControlWindows.setOnCloseRequest(event -> System.exit(0));
        userControlWindows.show();
        } catch (IOException e) {
            System.out.println("CANNOT LOAD RESOURCES FROM- CHECK INTEGRITY! - IO EXCEPTION");
            e.printStackTrace();
        }
        Output.ConstructOutputAFrame();
    }

    public static void main(String[] args) {
            launch();
    }

    public void selectConfig(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            setDTO(LoadConfig.select(db.getFiles().get(0)));
        }
        dragEvent.consume();
    }

    public void startSim() {
        //Прочесть ввод из окна
        SimDTO run = readDTO();
        startSim(run);
    }

    public static void startSim(SimDTO run){
        if (simQueue.isEmpty()){simQueue.add(new Sim(run));} // Если Пользователь не использует очередь,
        if (run.distCalc){
            ServerHandler.sendQueueToServer(simQueue);
        }
        else{
            currentSim = simQueue.pop();
            currentSim.start();
        }
    }
    public void setDTO(SimDTO input){
        genSizeZ.setValue(input.genSizeZ);
        genSizeX.setValue(input.genSizeX);
        targetSizeZ.setValue(input.tarSizeZ);
        targetSizeX.setValue(input.tarSizeX);
    }
    public SimDTO readDTO() {
        SimDTO result = new SimDTO();
        result.setGenSizeZ(genSizeZ.getValue());
        result.setGenSizeX(genSizeX.getValue());
        result.setTarSizeZ(targetSizeZ.getValue());
        result.setTarSizeX(targetSizeX.getValue());
        result.setBounceWallChance(bounceWallChance.getValue());
        result.setBounceGenChance(bounceGenChance.getValue());
        result.setThreadCount(Integer.parseInt(threadCount.getText()));
        result.setWaitTime(Integer.parseInt(waitTime.getText()));
        result.setDimensionCount(Integer.parseInt(dimensionCount.getText()));
        result.setXFrameLen(Double.parseDouble(xFrameLen.getText()));
        result.setYFrameLen(Double.parseDouble(yFrameLen.getText()));
        result.setZFrameLen(Double.parseDouble(zFrameLen.getText()));
        result.setStepsAm(Integer.parseInt(stepsAm.getText()));
        result.setTempAm(Double.parseDouble(tempAm.getText()));
        result.setTempSourceAm(Double.parseDouble(tempSourceAm.getText()));
        result.setPressure(Double.parseDouble(pressure.getText()));
        result.setPressurePow(Double.parseDouble(pressurePow.getText()));


        return result;
    }

    public void viewAndOutput(ActionEvent actionEvent) {
    }

    public void server(ActionEvent actionEvent) {

    }

    public void matterSelect(ActionEvent actionEvent) {
        Util.constructAWinMatterChooser();
    }

    public void queueManage(ActionEvent actionEvent) {
        Util.constructAWinQueue();
    }

    public void about(ActionEvent actionEvent) {
        Util.constructAWin();
        LoadConfig.constructConfigInfoFrame();
    }

    public void genTest(ActionEvent actionEvent) {
        new Sim(readDTO()).genTest();
    }

    public void selectPath(ActionEvent actionEvent) {

    }

    public void expungeToQueue(ActionEvent actionEvent) {

    }

    public void viewPalette(DragEvent dragEvent) {

    }

    public void checkServer(ActionEvent actionEvent) {

    }

    public void addToQueue(ActionEvent actionEvent) {

    }
}
