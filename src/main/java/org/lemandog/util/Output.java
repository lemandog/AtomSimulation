package org.lemandog.util;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import org.lemandog.Server.ServerRunner;
import org.lemandog.Sim;
import org.lemandog.SimDTO;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Vector;
import static org.lemandog.util.SwingFXUtils.fromFXImage;

public class Output {
    @Setter
    @Getter
    static int lastPrintStep = 0;
    static final DateTimeFormatter sdfF = DateTimeFormatter.ofPattern("dd MM yyyy HH mm");
    public static final Image palette = new Image(Output.class.getResourceAsStream("/heatmaps/heatmap2.png"));
    static public Vector<Double> X = new Vector<>();
    static public Vector<Double> Z = new Vector<>();
    FileWriter Hits, enchantedHits;
    Sim parent;
    SimDTO parentDTO;
    File csvHitsReport,csvFullOut;
    static final String LINE_END = "\r\n";
    static final String SEPARATOR = ";";

    public Output(Sim parent) { //Ставим параметры вывода
        this.parent = parent;
        parentDTO = parent.getDto();
    }
    public static void resetCords(){
        X = new Vector<>();
        Z = new Vector<>();
    }
    public void loadFromFile(File target){
        resetCords();
        try {
            Scanner sc=new Scanner(target);
            while (sc.hasNext()){
                String[] parts = sc.next().split(SEPARATOR);
                X.add(Double.valueOf(parts[0]));
                Z.add(Double.valueOf(parts[1]));
            }
            this.toFile(X,Z);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void toFile(Vector<Double> X, Vector<Double> Z) {
        double Xmax = this.parent.TAR_SIZE[0];
        double Zmax = this.parent.TAR_SIZE[2];
        int DOTSIZE = parentDTO.getResolution();
        int[][] CORD = new int[(int) (Xmax * (double) DOTSIZE)][(int) (Zmax * (double) DOTSIZE)];
        if (parentDTO.isDistCalc()){
            parentDTO.setOutputPath(new File(FileSystemView.getFileSystemView().getDefaultDirectory().getAbsolutePath() + "/AS"));
            ServerRunner.setFilesPath(parentDTO.getOutputPath());
            System.out.println(parentDTO.getOutputPath().getAbsolutePath());
            parentDTO.getOutputPath().deleteOnExit();
        }
        if (!parentDTO.getOutputPath().exists()){parentDTO.getOutputPath().mkdir();} //Создаём директории, если их нет
        if (parentDTO.isOutputRAWCord()){outputRawCord();}
        if (parentDTO.isOutputPicCSVPost()){outputPicCSVPost();}
        if (parentDTO.isOutputPicCSVPost() || parentDTO.isOutputPic()) { //Код предназначен для и картинки и вывода заселённости
            for (int[] ints : CORD) {
                Arrays.fill(ints, 0);
            }
            int height = (int) (Xmax * (double) DOTSIZE); //+2 для рамок по краю
            int width = (int) (Zmax * (double) DOTSIZE);
            double step = 1.0D/DOTSIZE;
            for (int i = 0; i < X.size(); ++i) {
                try {
                    int finCordX = (int) (((X.get(i)) / step) + (Xmax * DOTSIZE / 2));
                    int finCordZ = (int) (((Z.get(i)) / step) + (Zmax * DOTSIZE / 2));
                    CORD[finCordX][finCordZ]++;
                }catch (ArrayIndexOutOfBoundsException e){
                    Console.coolPrintout("!! PARTICLE IS OUT OF BOUNDS - IGNORING !!");
                }
                    }
            Console.coolPrintout("X-Z decomposition finished, particles calculated:"+ X.size());
            if(parentDTO.isOutputPicCSVPost()){
                try {
                    for (int x = 0; x < width; ++x) {
                        for (int y = 0; y < height; ++y) {
                            enchantedHits.write(CORD[x][y] + SEPARATOR);
                        }
                        enchantedHits.write(LINE_END);
                    }
                    enchantedHits.flush();
                    enchantedHits.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Console.printLine('@');
                Console.coolPrintout("GLOBAL DATA DEALER STREAM CLOSED");
                enchantedHits = null;
            }
        }
        if (parentDTO.isOutputPic()){
            File outputFile;
            if(parentDTO.isDistCalc()){
                outputFile = new File(ServerRunner.getFilesPath()+ "/"+ parent.thisRunIndex+"hitsDetector.png");
            } else {
                outputFile = new File(parentDTO.getOutputPath().getAbsolutePath() + "/"+ parent.thisRunIndex+"hitsDetector.png");
            }
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

    private void outputPicCSVPost() {
        if (enchantedHits == null) { //Попытка создать файл
            if(parentDTO.isDistCalc()){
                csvFullOut = new File(ServerRunner.getFilesPath()
                        + "/"+parent.thisRunIndex+"out.csv");
            } else {
                csvFullOut = new File(parentDTO.getOutputPath().getAbsolutePath()
                        + "/"+parent.thisRunIndex+"out.csv");
            }
            try {
                enchantedHits = new FileWriter(csvFullOut);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private void outputRawCord() {
        if (Hits == null){ //Попытка создать файл
            LocalDateTime main = LocalDateTime.now();
            if(parentDTO.isDistCalc()){
                csvHitsReport = new File(ServerRunner.getFilesPath()
                        + "/"+parent.thisRunIndex+"Hits"+sdfF.format(main)+".csv");
            } else{
                csvHitsReport = new File( parentDTO.getOutputPath().getAbsolutePath()
                        + "/"+parent.thisRunIndex+"Hits"+sdfF.format(main)+".csv");
            }

            try {
                Hits = new FileWriter(csvHitsReport);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try { //Запись за шаг
            for (int i = 0; i < X.size(); i++) {
                Hits.write(X.get(i) +SEPARATOR+ Z.get(i) + LINE_END);
            }
            Hits.flush();
            Hits.close();
            Console.coolPrintout("GLOBAL STREAM RAW CORD CLOSED");
            Hits =null;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static Color colSel(int sel){        //0-9 цвета в палитре
        PixelReader randColRead = palette.getPixelReader();
        return randColRead.getColor(sel,0);}

    private Image toImage(int[][] CORD){
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
    synchronized public static void CSVStateReact(double translateX, double translateZ) {
        X.add(translateX);
        Z.add(translateZ);
    }
}
