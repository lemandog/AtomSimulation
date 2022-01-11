package org.lemandog.util;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import org.lemandog.App;
import org.lemandog.SimDTO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.lemandog.Sim.currentSim;
import static org.lemandog.util.SwingFXUtils.fromFXImage;

public class Output {
    @Setter
    @Getter
    static int lastPrintStep = 0;
    static DateTimeFormatter sdfF = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm");
    public static Image palette = new Image(Output.class.getResourceAsStream("/heatmaps/heatmap2.png"));
    public static int[][] picState;
    static Vector<Double> X = new Vector<>();
    static Vector<Double> Z = new Vector<>();
    static FileWriter Hits, enchantedHits;

    static final String LINE_END = "\r\n";
    static final String SEPARATOR = ";";

    public Output(SimDTO dto) { //Ставим параметры вывода

    }

    public static void ConstructOutputAFrame() {


    }

    public static void toFile() {
        double Xmax = currentSim.TAR_SIZE[0];
        double Zmax = currentSim.TAR_SIZE[2];
        int DOTSIZE = currentSim.getDto().getResolution();
        int[][] CORD = new int[(int) (Xmax * (double) DOTSIZE)+2][(int) (Zmax * (double) DOTSIZE)+2];
        if (currentSim.getDto().isOutputRAWCord()) {
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
        if ( currentSim.getDto().isOutputPicCSVPost() || currentSim.getDto().isOutputPic()) { //Код предназначен для и картинки и вывода заселённости
            for (int[] ints : CORD) {
                Arrays.fill(ints, 0);
            }
            int height = (int) (Xmax * (double) DOTSIZE)+2; //+2 для рамок по краю
            int width = (int) (Zmax * (double) DOTSIZE)+2;

            for (int i = 0; i < X.size(); ++i) {

                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        if ((double) (1.0F / (float) DOTSIZE * (float) (x - 1)) - Xmax / 2.0D <= X.get(i) &&
                                X.get(i) <= (double) (1.0F / (float) DOTSIZE * (float) x) - Xmax / 2.0D &&
                                (double) (1.0F / (float) DOTSIZE * (float) (y - 1)) - Zmax / 2.0D <= Z.get(i) &&
                                Z.get(i) <= (double) (1.0F / (float) DOTSIZE * (float) y) - Zmax / 2.0D) {
                            CORD[x][y]++;
                        }
                    }
                }
                if ((double)i%100 == 0){System.out.println("progress: " + ((double)i/X.size())*100 + " %");}
            }
            if(currentSim.getDto().isOutputPicCSVPost()){
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
        if (currentSim.getDto().isOutputPic()){
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
        }
    public static Color colSel(int sel){        //0-9 цвета в палитре
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
        if (currentSim.getDto().isOutputCSV()){
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
        if(currentSim.getDto().isOutputPicCSVPost()) {
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

    public void generateRandom() {
        for (int i = 0; i <picState.length; i++) {
            for (int j = 0; j <picState[i].length; i++){
                picState[i][j] = (int) (Math.random() * 100);
            }
        }
        toFile();
    }
}
