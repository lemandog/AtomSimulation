package org.lemandog;

import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.lemandog.util.LoadConfig;
import org.lemandog.util.Util;

import java.io.File;
import java.util.ArrayDeque;

import static org.lemandog.Sim.currentSim;

public class MainController {
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

    public CheckBox RAWCordOutput;
    public CheckBox PicCSVOutput;
    public CheckBox PicPNGOutput;
    public CheckBox particlesDraw;
    public TextField pathToOutput;
    public Slider paletteSelect;
    public ImageView paletteView;

    public TextField serverAddress;
    public TextField userEmail;
    public WebView serverResponse;
    public CheckBox serverCalculate;

    public ChoiceBox<GasTypes> materialChooser;
    public Text atomDiam;
    public Text atomMass;

    public Text numberInQueue;

    public WebView configView;
    public Text configReaderStatus;


    public void initialize(){
        readDTO(new SimDTO());
    }
    public void selectConfig(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            readDTO(LoadConfig.select(db.getFiles().get(0)));
            configReaderStatus.setText("Файл принят");
        }
        dragEvent.consume();
    }

    public void startSim() {
        //Прочесть ввод из окна
        SimDTO run = writeDTO();
        startSim(run);
    }

    public static void startSim(SimDTO run){
        if (simQueue.isEmpty()){simQueue.add(new Sim(run));} // Если Пользователь не использует очередь,
        if (run.isDistCalc()){
            ServerHandler.sendQueueToServer(simQueue);
        }
        else{
            currentSim = simQueue.pop();
            currentSim.start();
        }
    }
    public void readDTO(SimDTO input){   //Чтение DTO в UI
        genSizeZ.setValue(input.getGenSizeZ());
        genSizeX.setValue(input.getGenSizeX());
        targetSizeZ.setValue(input.getTarSizeZ());
        targetSizeX.setValue(input.getTarSizeX());
        bounceWallChance.setValue(input.getBounceWallChance());
        bounceGenChance.setValue(input.getBounceGenChance());
        threadCount.setText(String.valueOf(input.getThreadCount()));
        waitTime.setText(String.valueOf(input.getWaitTime()));
        dimensionCount.setText(String.valueOf(input.getDimensionCount()));
        xFrameLen.setText(String.valueOf(input.getXFrameLen()));
        yFrameLen.setText(String.valueOf(input.getYFrameLen()));
        zFrameLen.setText(String.valueOf(input.getZFrameLen()));
        particleAm.setText(String.valueOf(input.getParticleAm()));
        stepsAm.setText(String.valueOf(input.getStepsAm()));
        tempAm.setText(String.valueOf(input.getTempAm()));
        tempSourceAm.setText(String.valueOf(input.getTempSourceAm()));
        pressurePow.setText(String.valueOf(input.getPressurePow()));
        pressure.setText(String.valueOf(input.getPressure()));

        RAWCordOutput.setSelected(input.isOutputRAWCord());
        PicCSVOutput.setSelected(input.isOutputPicCSVPost());
        PicPNGOutput.setSelected(input.isOutputPic());
        particlesDraw.setSelected(input.isOutput3D());
        pathToOutput.setText(input.outputPath.getAbsolutePath());
        paletteSelect.setValue(input.getPaletteNumber());
        paletteView.setImage(input.getPalette());


        serverAddress.setText(input.getServerAddress());
        userEmail.setText(input.getUserEmail());

        serverResponse.getEngine().loadContent("NULL");
        serverCalculate.setSelected(input.isDistCalc());

        materialChooser.setValue(input.getGas());
        atomDiam.setText(input.getGas().diameterRAW + " пм");
        atomMass.setText(input.getGas().massRAW + " аем");

        numberInQueue.setText(String.valueOf(simQueue.size()));

        configView.getEngine().loadContent(Util.getContent());

    }
    public SimDTO writeDTO() { //Запись в DTO
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

    public void genTest(ActionEvent actionEvent) {
        new Sim(writeDTO()).genTest();

    }

    public void selectPath() {
        DirectoryChooser pathfinder = new DirectoryChooser();
            File path = pathfinder.showDialog(new Stage());
            if(path == null) {
                path = new File(System.getProperty("user.home") + "/Desktop");
            }
        pathToOutput.setText(path.getAbsolutePath());
    }

    public void expungeToQueue() {
        if (!simQueue.isEmpty()){simQueue.pop();}
        numberInQueue.setText(String.valueOf(simQueue.size()));
    }
    public void addToQueue() {
        simQueue.add(new Sim(writeDTO()));
        numberInQueue.setText(String.valueOf(simQueue.size()));
    }

    public void add10ToQueue() {
        for (int i = 0; i < 10; i++) {
            simQueue.add(new Sim(writeDTO()));
        }
        numberInQueue.setText(String.valueOf(simQueue.size()));
    }

    public void expungeAllQueue() {
        simQueue.clear();
        numberInQueue.setText(String.valueOf(0));
    }

    public void viewPalette() {
        paletteView.setImage(new Image(MainController.class.getResourceAsStream("/heatmaps/heatmap"+(int)paletteSelect.getValue()+".png")));
    }

    public void checkServer() {

    }



}
