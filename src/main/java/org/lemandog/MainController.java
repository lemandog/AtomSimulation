package org.lemandog;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.lemandog.Server.ServerHandler;
import org.lemandog.Server.ServerRunner;
import org.lemandog.util.LoadConfig;
import org.lemandog.util.Output;
import org.lemandog.util.Util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Objects;

public class MainController {
    public TextField targetSizeZ;
    public TextField targetSizeX;
    public TextField genSizeZ;
    public TextField genSizeX;
    public TextField bounceWallChance;
    public TextField bounceGenChance;
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

    public static HashMap<String, ArrayDeque<SimDTO>> simQueue = new HashMap();
    public static String currentStreamKey;
    public static ArrayDeque<SimDTO> currentStream = new ArrayDeque<>();

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
    public Slider resolveSelect;
    public ScrollPane commandPane;
    public TextField resolutionField;
    public double[][] genPic;

    public void initialize(){
        resolutionField.setText(String.valueOf(resolveSelect.getValue()));
        configView.getEngine().loadContent(Util.getContent());
        commandPane.setContent(LoadConfig.constructConfigInfoFrame());
        materialChooser.setItems(FXCollections.observableArrayList(GasTypes.values()));
        materialChooser.getSelectionModel().selectedIndexProperty().addListener((ChangeListener<? super Number>) (observableValue, gasTypes, t1) -> {
            GasTypes type = materialChooser.getValue();
            atomDiam.setText(type.diameterRAW + " пм");
            atomMass.setText(type.massRAW + " аем");
        });
        writeDTO(new SimDTO());
    }
    public void selectConfig(DragEvent dragEvent) {
        Dragboard db = dragEvent.getDragboard();
        if (db.hasFiles()) {
            writeDTO(LoadConfig.select(db.getFiles().get(0)));
            configReaderStatus.setText("Файл принят");
        }
        dragEvent.consume();
    }

    public void startSim() {
        //Прочесть ввод из окна
        SimDTO run = readDTO();
        startSim(run, run.getServerAddress());
    }

    public static void startSim(SimDTO run, String address){
        Sim.index = 1;
            if (run.isDistCalc()){
                ArrayDeque<SimDTO> toSend = currentStream;
                currentStream = new ArrayDeque<>();
                ServerHandler.sendQueueToServer(toSend,address);
            } else {
                if (currentStream.isEmpty()) {
                    currentStream.add(run);
                }
                new Sim(currentStream.pop()).start();
            }
    }

    public void writeDTO(SimDTO input){   //Чтение DTO в UI
        genSizeZ.setText(String.valueOf(input.getGenSizeZ()));
        genSizeX.setText(String.valueOf(input.getGenSizeX()));
        targetSizeZ.setText(String.valueOf(input.getTarSizeZ()));
        targetSizeX.setText(String.valueOf(input.getTarSizeX()));
        bounceWallChance.setText(String.valueOf(input.getBounceWallChance()));
        bounceGenChance.setText(String.valueOf(input.getBounceGenChance()));
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
        resolveSelect.setValue(input.getResolution());


        serverAddress.setText(input.getServerAddress());
        userEmail.setText(input.getUserEmail());

        serverResponse.getEngine().loadContent("NULL");
        serverCalculate.setSelected(input.isDistCalc());

        materialChooser.setValue(input.getGas());
        numberInQueue.setText(String.valueOf(simQueue.size()));
        if (input.getGenImage() == null){
            genPic = input.getGenImage();
        }
    }
    public SimDTO readDTO() { //Запись в DTO
        try {
            SimDTO result = new SimDTO();
            result.setGenSizeZ(Double.parseDouble(genSizeZ.getText()));
            result.setGenSizeX(Double.parseDouble(genSizeX.getText()));
            result.setTarSizeZ(Double.parseDouble(targetSizeZ.getText()));
            result.setTarSizeX(Double.parseDouble(targetSizeX.getText()));
            result.setBounceWallChance(Double.parseDouble(bounceWallChance.getText()));
            result.setBounceGenChance(Double.parseDouble(bounceGenChance.getText()));
            result.setThreadCount(Integer.parseInt(threadCount.getText()));
            result.setWaitTime(Integer.parseInt(waitTime.getText()));
            result.setDimensionCount(Integer.parseInt(dimensionCount.getText()));
            result.setXFrameLen(Double.parseDouble(xFrameLen.getText()));
            result.setYFrameLen(Double.parseDouble(yFrameLen.getText()));
            result.setZFrameLen(Double.parseDouble(zFrameLen.getText()));
            result.setStepsAm(Integer.parseInt(stepsAm.getText()));
            result.setParticleAm(Integer.parseInt(particleAm.getText()));
            result.setTempAm(Double.parseDouble(tempAm.getText()));
            result.setTempSourceAm(Double.parseDouble(tempSourceAm.getText()));
            result.setPressure(Double.parseDouble(pressure.getText()));
            result.setPressurePow(Double.parseDouble(pressurePow.getText()));

            result.setOutputRAWCord(RAWCordOutput.isSelected());
            result.setOutputPicCSVPost(PicCSVOutput.isSelected());
            result.setOutputPic(PicPNGOutput.isSelected());
            result.setOutput3D(particlesDraw.isSelected());
            result.setResolution((int) resolveSelect.getValue());

            result.setOutputPath(new File(pathToOutput.getText()));
            result.setPaletteNumber((int) paletteSelect.getValue());//Вроде оно должно быть автоматически, но это на случай если не сработает
            result.setPalette((int) paletteSelect.getValue());

            result.setServerAddress(serverAddress.getText());
            result.setUserEmail(userEmail.getText());
            result.setDistCalc(serverCalculate.isSelected());

            result.setGas(materialChooser.getValue());
            result.setPlainCharacteristic(genPic != null);

            if (genPic == null){
                result.setPlainCharacteristic(true);
            }else {
                result.setPlainCharacteristic(false);
                result.setGenImage(genPic);
            }
            return result;
        }catch (NumberFormatException e ){
            System.out.println("MISMATCHED INPUT");
            System.out.println("НЕВЕРНЫЕ ЗНАЧЕНИЯ");
        }
        return null;
    }

    private double[][] pictureToAlpha(BufferedImage genPic) {
        int pixel = genPic.getRGB(0,0);
        boolean isTransparent = ((pixel>>24) == 0x00);
        int width = genPic.getWidth();
        int height = genPic.getHeight();
        double[][] alphaChannel = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                //get pixel value
                int p = genPic.getRGB(x,y);
                int value;
                if (isTransparent){
                    value = (p>>24) & 0xff;
                }else{
                    value = p;
                }
                alphaChannel[x][y] = value;
            }
        }
        double biggest = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(biggest>alphaChannel[x][y]) {biggest = alphaChannel[x][y];}
            }}
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                alphaChannel[x][y] = alphaChannel[x][y]/biggest;
            }}
        return alphaChannel;
    }

    public void genTest() {
        SimDTO result = readDTO();
        if (result != null){new Sim(readDTO()).genTest();}
    }

    public void selectPath() {
        DirectoryChooser pathfinder = new DirectoryChooser();
            File path = pathfinder.showDialog(new Stage());
            if(path == null) {
                path = new File(System.getProperty("user.home") + "/Desktop");
            }
        pathToOutput.setText(path.getAbsolutePath());
    }

    public void expungeQueue() {
        if (!currentStream.isEmpty()){currentStream.pop();}
        numberInQueue.setText(String.valueOf(currentStream.size()));
    }
    public void addToQueue() {
        currentStream.add(readDTO());
        numberInQueue.setText(String.valueOf(currentStream.size()));
    }

    public void expungeAllQueue() {
        currentStream.clear();
        numberInQueue.setText(String.valueOf(0));
    }

    public void viewPalette() {
        paletteView.setImage(new Image(Objects.requireNonNull(MainController.class.getResourceAsStream("/heatmaps/heatmap" + (int) paletteSelect.getValue() + ".png"))));
    }

    public void checkServer() {
        serverResponse.getEngine().loadContent(ServerHandler.askServerForOutput(serverAddress.getText()));
    }


    public void runServer() {
        ServerRunner.main();
    }

    public void resolveSelectDrag() {
        resolutionField.setText(String.valueOf(resolveSelect.getValue()));
    }

    public void saveConfig() {
        try {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File configPath = directoryChooser.showDialog(new Stage());
        if (configPath != null){ // Если диалог не просто закрыли
            File config = new File(configPath.getAbsolutePath() + "/config.AS");
            config.createNewFile();
            System.out.println(config.getAbsolutePath());
            FileOutputStream fout = new FileOutputStream(config);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(readDTO());
            oos.flush();
            fout.flush();
        }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void add10ToQueue() {
        for (int i = 0; i < 10; i++) {
            currentStream.add(readDTO());
        }
        numberInQueue.setText(String.valueOf(currentStream.size()));
    }
    public void add100ToQueue() {
        for (int i = 0; i < 100; i++) {
            currentStream.add(readDTO());
        }
        numberInQueue.setText(String.valueOf(currentStream.size()));
    }

    public void reloadQueueCounter() {
        numberInQueue.setText(String.valueOf(currentStream.size()));
    }

    public void SetResolution(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER){
            resolveSelect.setValue(Integer.parseInt(resolutionField.getText()));
        }
    }

    public void readAFileOutput() {
        FileChooser pathfinder = new FileChooser();
        File path = pathfinder.showOpenDialog(new Stage());
        if (path != null){
            new Output(new Sim(readDTO())).loadFromFile(path);
        } else{
            System.out.println("NO FILE SELECTED!");
        }
    }

    public void loadGen() {
        FileChooser pathfinder = new FileChooser();
        File path = pathfinder.showOpenDialog(new Stage());
        if (path != null){
            try {
                File pngInput = new File(path.getAbsolutePath());
                genPic = pictureToAlpha(ImageIO.read(pngInput));
            } catch (Exception e) {
                System.out.println("THAT FILE COULD NOT LOAD!: " + e.getMessage());
            }

        } else{
            System.out.println("NO FILE SELECTED!");
        }
    }
}
