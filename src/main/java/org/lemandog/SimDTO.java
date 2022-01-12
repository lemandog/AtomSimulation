package org.lemandog;

import javafx.scene.image.Image;
import lombok.*;
import org.lemandog.util.Output;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
@AllArgsConstructor
public class SimDTO {
    final int port = 5904;
    public SimDTO(){
        pathDrawing = false;
        distCalc = false;
        output=false;
        outputPic=false;
        outputRAWCord=false;
        outputPicCSVPost=false;
        outputCSV=false;
        output3D = true;
        serverAddress = "localhost";
        userEmail = "";
        genSizeX = 0.0000000000000001; //Точечный генератор по умолчанию
        genSizeZ = 0.0000000000000001;
        tarSizeX = 1;
        tarSizeZ = 1;
        bounceGenChance = 1;
        bounceWallChance = 0.3;
        threadCount = Runtime.getRuntime().availableProcessors();
        waitTime = 0;
        dimensionCount = 3;
        xFrameLen = 1;
        yFrameLen = 1;
        zFrameLen = 1;
        particleAm = 5000;
        stepsAm = 5000;
        tempAm = 300;
        tempSourceAm = 1500;
        pressurePow = -8;
        pressure = 1;
        outputPath = FileSystemView.getFileSystemView().getHomeDirectory();
        output = false;
        outputPic = false;
        outputCSV = false;
        outputRAWCord = false;
        outputPicCSVPost = false;
        palette = new Image(Output.class.getResourceAsStream("/heatmaps/heatmap2.png"));
        paletteNumber = 2;
        resolution = 100;
        gas = GasTypes.CHROME;
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
    @Getter
    Image palette;
    public void setPalette(int paletteNumber) {
        this.palette = new Image(SimDTO.class.getResourceAsStream("/heatmaps/heatmap"+paletteNumber+".png"));
    }
    public void setPalette(){
        this.palette = new Image("/heatmaps/heatmap" + this.paletteNumber + ".png");
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