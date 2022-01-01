package org.lemandog;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import org.lemandog.util.Console;
import org.lemandog.util.DebugTools;
import org.lemandog.util.Output;
import org.lemandog.util.Util;

import java.io.File;

public class Sim {
    public static Sim currentSim;
    static double k=1.3806485279e-23;//постоянная Больцмана, Дж/К
    double m;         //кг
    private final double d; //м
    public GasTypes thisRunMaterial;
    int T, TSource;
    double p;
    int N;
    int LEN;
    double[] CHA_SIZE; //XYZ
    public double[] TAR_SIZE; //XYZ
    double[] GEN_SIZE; //XYZ
    double lambdaN;
    Point3D center;
    int lastRunning;
    Particle[] container; //XYZ

    static int avilableStreams = Runtime.getRuntime().availableProcessors();
    public File selectedPath;
    int avilableDimensions;
    int maxDimensions = 3;
    int nbRunning = 0;
    int waitTimeMS;
    double wallBounce;
    double genBounce;
    boolean simIsAlive = false;
    boolean pathsDr;
    Thread mainContr;
    Thread[] calculator;

    public Sim(){
        selectedPath = new File(Output.choDir.getText());
        thisRunMaterial = Util.getMat();
        Console.coolPrintout("Material is: "+thisRunMaterial.name() +" "+ thisRunMaterial.diameterRAW +" "+ thisRunMaterial.massRAW);
        d = thisRunMaterial.diameter;
        m = thisRunMaterial.mass;

        p = Double.parseDouble(App.pressure.getText())*Math.pow(10,Double.parseDouble(App.pressurePow.getText())); //X*10^Y
        T = Integer.parseInt(App.tempAm.getText()); //K
        TSource = Integer.parseInt(App.tempSourceAm.getText()); //K
        N = Integer.parseInt(App.particleAm.getText());
        LEN = Integer.parseInt(App.stepsAm.getText());

        lambdaN = (k*T/(Math.sqrt(2)*p*Math.PI*Math.pow(d,2)));
        //Как сказано в Paticle, пользователь может сам ввести количество осей.
        //Конечно, я не знаю кому нужна пятимерная симуляция газа, но гибкость кода - важная часть ООП
        avilableDimensions = (int) App.dimensionCount.getValue();
        if (avilableDimensions>3){maxDimensions=avilableDimensions;}

        GEN_SIZE = new double[maxDimensions]; //XYZ
        TAR_SIZE = new double[maxDimensions]; //XYZ
        CHA_SIZE = new double[maxDimensions]; //XYZ

        CHA_SIZE[0] = Double.parseDouble(App.xFrameLen.getText());
        CHA_SIZE[1] = Double.parseDouble(App.yFrameLen.getText());
        CHA_SIZE[2] = Double.parseDouble(App.zFrameLen.getText());

        center = new Point3D(0,0,0);//Центр камеры для механики переизлучения

        TAR_SIZE[0] = CHA_SIZE[0] * App.targetSizeX.getValue();
        TAR_SIZE[1] = CHA_SIZE[1]/100;
        TAR_SIZE[2] = CHA_SIZE[2] * App.targetSizeZ.getValue();

        GEN_SIZE[0] = CHA_SIZE[0] * App.genSizeX.getValue();
        GEN_SIZE[1] = CHA_SIZE[1]/100;
        GEN_SIZE[2] = CHA_SIZE[2] * App.genSizeZ.getValue();

        waitTimeMS = (int) Math.round(App.waitTime.getValue());
        wallBounce = App.bounceWallChance.getValue();
        genBounce = App.bounceGenChance.getValue();

        lastRunning = 0;
        avilableStreams = (int) App.threadCount.getValue();

        calculator = new Thread[avilableStreams];
        pathsDr = Output.pathDrawing.isSelected();
        Console.setAm();
        //Получается так, что это невероятно огромные массивы, так что инициализировать их будем только если стоит галка.
        //Да, теперь нельзя сохранять результаты прошедшей симуляции после запуска, но Java heap space не будет ругаться.
        Output.init();
        if(Output.outputPic) {
            Output.DOTSIZE = Output.outputAskPicResolution.getValue();
            Output.palette = new Image("/heatmap" + (int) Output.outputPalette.getValue() + ".png");
        }
        //Тут компилятор ругается, но зря. Это сделано для того чтобы не словить NullPointer далее. Они все будут заменены при запуске.
        for (int i = 0; i < avilableStreams; i++) {
            calculator[i] = new Thread();
        }
        container = new Particle[N];
    }
    public static void genTest() {
    currentSim = new Sim();
    EngineDraw.reset();
    EngineDraw.eSetup();
        for (int i = 0; i < currentSim.N; i++) {
            currentSim.container[i] = new Particle(i);
        }
     EngineDraw.DrawingThreadFire(Sim.currentSim.container);
    currentSim.mainContr = new Thread(); //Иначе будет NullPointerException. То же что и выше
    if (Output.outputPic){
        for (int x = 0; x<Output.xSize;x++){
            for (int y = 0; y<Output.zSize;y++) {
                Output.picState[x][y] = (int) (Math.random() * 15);
            }}
            Output.toFile();
    }}



    public void start() {
        currentSim = new Sim(); //Установка выбраных параметров
        if (!selectedPath.exists()){selectedPath.mkdir();}
        currentSim.simIsAlive = true;
        EngineDraw.reset();
        EngineDraw.eSetup();
        for (int i = 0; i < N; i++) {
            container[i] = new Particle(i);
        }
        //Тут условие для быстродействия. Очень большие симуляции занимают много времени для изначальной отрисовки, так что
        //Первый шаг будет отрисован только если пользователь захочет
        EngineDraw.DrawingThreadFire(Sim.currentSim.container);
        //Это делает код менее читабельным, но гораздо более быстрым.
        mainContr = new Thread(() ->{
        while(lastRunning < N && currentSim.simIsAlive) {
            if(threadQuotaNotMet()>0){
                for (int i = 0; i< avilableStreams;i++){
                    if (!calculator[i].isAlive()){// Найти закончившийся тред
                        try {
                        calculator[i] = null;       // Удалить
                        calculator[i] = container[lastRunning].CreateThread(); //Создать и запустить новый - на замену старому
                        calculator[i].start();
                        lastRunning++;
                        }catch (ArrayIndexOutOfBoundsException e){Console.coolPrintout("THREAD CREATION MISSFIRE");break;}
                    }
                }
            }
        }
            try { //Просто ждём пока закончит считать. Изящнее сделать не получилось
                Thread.sleep(5000);
            } catch (InterruptedException ignore) {}
            Console.printLine('X');
            Console.coolPrintout("SIMULATION RUN IS OVER!");
            EngineDraw.DrawingThreadFire(container);
        Output.toFile();
        DebugTools.close();
        simIsAlive = false;
        if (!App.simQueue.isEmpty()){
            currentSim = App.simQueue.pop();
            try { //Просто ждём пока закончит считать. Изящнее сделать не получилось
                Console.coolPrintout("Another Sim will start shortly...");
                java.awt.Toolkit.getDefaultToolkit().beep();
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
            Platform.runLater(() -> currentSim.start()); //Надо обязательно делать это на потоке JavaFX
        }
        java.awt.Toolkit.getDefaultToolkit().beep();
    });
        mainContr.setPriority(Thread.MAX_PRIORITY);
        mainContr.start();
    }

    private int threadQuotaNotMet() {
        int thereAreDeadThreads = 0;
        nbRunning = 0;
        try{
        for (int i = 0; i< avilableStreams;i++) {
            if (!calculator[i].isAlive()) {
                thereAreDeadThreads++;// Есть не живые треды
            } else {
                nbRunning++;
            }
        }
        }catch(NullPointerException e){thereAreDeadThreads++;}
        return thereAreDeadThreads;
    }
}
