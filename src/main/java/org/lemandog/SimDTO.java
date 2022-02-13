package org.lemandog;

import javafx.scene.image.Image;
import lombok.*;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.Serializable;

@AllArgsConstructor
public class SimDTO implements Serializable {
    public SimDTO(){
        particleAm = 5000;
        stepsAm = 5000;
        tempAm = 300;
        tempSourceAm = 1500;
        gas = GasTypes.CHROME;
        xFrameLen = 1;
        yFrameLen = 1;
        zFrameLen = 1;
        pressurePow = -8;
        pressure = 1;
        pathDrawing = false;
        threadCount = Runtime.getRuntime().availableProcessors();
        tarSizeX = 1;
        tarSizeZ = 1;
        genSizeX = 0.0000000000000001; //Точечный генератор по умолчанию
        genSizeZ = 0.0000000000000001;
        outputPath = FileSystemView.getFileSystemView().getHomeDirectory();
        output = false;
        outputPic = false;
        outputCSV = false;
        outputRAWCord = false;
        outputPicCSVPost = false;
        paletteNumber = 2;
        resolution = 100;
        dimensionCount = 3;
        waitTime = 0;
        bounceWallChance = 0.3;
        bounceGenChance = 1;
        output3D = true;
        distCalc = false;
        serverAddress = "localhost";
        userEmail = "";
    }
    @Getter
    @Setter
    private boolean pathDrawing, distCalc;
    @Getter
    @Setter
    private String serverAddress, userEmail;
    @Getter
    @Setter
    private double genSizeZ, genSizeX, tarSizeZ, tarSizeX;
    @Getter
    @Setter
    private double bounceWallChance;
    @Getter
    @Setter
    private double bounceGenChance;
    @Getter
    @Setter
    int threadCount;
    @Getter
    @Setter
    int waitTime;
    @Getter
    @Setter
    int dimensionCount;
    @Getter
    @Setter
    double xFrameLen, yFrameLen, zFrameLen;
    @Getter
    @Setter
    int particleAm;
    @Getter
    @Setter
    int stepsAm;
    @Getter
    @Setter
    double tempAm, tempSourceAm;
    @Getter
    @Setter
    double pressurePow, pressure;
    @Getter
    @Setter
    File outputPath;
    @Getter
    @Setter
    boolean output, outputPic, outputRAWCord, outputPicCSVPost, outputCSV, output3D;
    public Image getPalette() {
        return new Image(SimDTO.class.getResourceAsStream("/heatmaps/heatmap"+paletteNumber+".png"));
    }
    public void setPalette(int paletteNumber) {
        this.paletteNumber = paletteNumber;
    }
    @Getter
    @Setter
    int paletteNumber;

    @Getter
    @Setter
    int resolution;
    @Getter
    @Setter
    GasTypes gas;


}