package org.lemandog;

import javafx.scene.image.Image;

import static org.lemandog.EngineDraw.*;

public class Sim {
    static double k=1.3806485279e-23;//постоянная Больцмана, Дж/К
    private static final double m_Cr=51.9961; //масса ХРОМА, а.е.м.
    static double m=m_Cr*1.660539040e-27; //масса ХРОМ, кг
    private static final double d = 130*10e-12;//Диаметр хрома (м)
    public static int T;
    public static double p;
    static int N;
    public static int LEN;
    public static double[] CHA_SIZE = new double[3]; //XYZ
    public static double[] TAR_SIZE = new double[3]; //XYZ
    public static double[] GEN_SIZE = new double[3]; //XYZ
    public static double lambdaN;
    static int lastRunning;
    static Particle[] container; //XYZ

    public static int tarHitCounterI = 0;
    public static int outOfBoundsCounterI = 0;
    public static int aliveCounterI = 0;

    public static int avilableStreams = Runtime.getRuntime().availableProcessors();
    public static int nbRunning = 0;
    public static boolean simIsAlive = false;
    public static boolean pathsDr = false;
    static Thread mainContr;
    static Thread[] calculator;

    public static void setup(){
        outOfBoundsCounterI = 0; //Обнуление счётчиков с предыдущего запуска
        tarHitCounterI = 0;
        EngineDraw.root = null;
        p = Math.pow(Double.parseDouble(App.pressure.getText()),Double.parseDouble(App.pressurePow.getText()));
        T = Integer.parseInt(App.tempAm.getText());
        N = Integer.parseInt(App.particleAm.getText());
        aliveCounterI = N;
        LEN = Integer.parseInt(App.stepsAm.getText());
        lambdaN = (k*T/(Math.sqrt(2)*p*Math.PI*Math.pow(d,2)));

        CHA_SIZE[0] = Integer.parseInt(App.xFrameLen.getText());
        CHA_SIZE[1] = Integer.parseInt(App.yFrameLen.getText());
        CHA_SIZE[2] = Integer.parseInt(App.zFrameLen.getText());

        TAR_SIZE[0] = CHA_SIZE[0] * App.targetSizeX.getValue();
        TAR_SIZE[1] = CHA_SIZE[1]/100;
        TAR_SIZE[2] = CHA_SIZE[2] * App.targetSizeZ.getValue();

        GEN_SIZE[0] = CHA_SIZE[0] * App.genSizeX.getValue();
        GEN_SIZE[1] = CHA_SIZE[1]/100;
        GEN_SIZE[2] = CHA_SIZE[2] * App.genSizeZ.getValue();

        lastRunning = 0;
        avilableStreams = (int) App.threadCount.getValue();
        calculator = new Thread[avilableStreams];
        pathsDr = App.pathDrawing.isSelected();

        Output.statesH = new int[N][LEN]; // Для переписи приземлившихся промахнувшихся и живых частиц
        Output.statesO = new int[N][LEN];
        Output.statesF = new int[N][LEN];
        Output.xSize = (int) TAR_SIZE[0];
        Output.zSize = (int) TAR_SIZE[2];
        Output.picState = new int[Output.xSize+2][Output.zSize+2]; //+2 - это для запаса.
        // Иногда координата немного выходит за пределы из за перевода в целочисленное значение
        Output.palette = new Image("/heatmap"+(int) Output.outputPallete.getValue()+".png");

        for (int i = 0; i < avilableStreams; i++) {
            calculator[i] = new Thread();
        }

        container = new Particle[N];
        for (int i = 0; i < N; i++) {
            container[i] = new Particle(i);
        }
    }
    public static void genTest() {
    setup();
    EngineDraw.esetup();
    mainContr = new Thread(); //Иначе будет NullPointerException
    DrawingThreadFire();
        for (int x = 0; x<Output.xSize;x++){
            for (int y = 0; y<Output.zSize;y++) {
                Output.picState[x][y] = (int) (Math.random() * 15);
            }}
            Output.toFile();
    }



    public static void start() {
        setup(); //Установка выбраных параметров
        simIsAlive = true;
        EngineDraw.esetup();

        DrawingThreadFire();//Поток на отрисовку в EngineDraw (EngineDraw.timeline)
        TextUpdate(); //На отрисовку текста - отдельный тред в App (App.timelineT)
        //Это делает код менее читабельным, но гораздо более быстрым.
        timeline.play();
        App.timelineT.play();

        mainContr = new Thread(() ->{
        while(lastRunning < N) {
            if(threadQuotaNotMet()){
                for (int i = 0; i< avilableStreams;i++){
                    if (!calculator[i].isAlive()){// Найти закончившийся тред
                        try {
                        calculator[i] = null;       // Удалить
                        calculator[i] = container[lastRunning].CreateThread(); //Создать и запустить новый - на замену старому
                        calculator[i].start();
                        lastRunning++;
                        }catch (ArrayIndexOutOfBoundsException e){System.out.println("THREAD CREATION MISSFIRE");break;}
                    }

                }
            }
        }
            try {
                Thread.sleep(3000); //Ожидание, чтобы анимация успела поместить все частицы. TODO: Исправить на что то более логичное
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            timeline.stop();
            App.timelineT.stop();
        System.out.println("SIMULATION RUN ENDED");
        Output.toFile();
        simIsAlive = false;
    });
        mainContr.setPriority(Thread.MAX_PRIORITY);
        mainContr.start();
    }

    private static boolean threadQuotaNotMet() {
        boolean thereAreDeadThreads = false;
        nbRunning = 0;
        for (int i = 0; i< avilableStreams;i++){
            if (!calculator[i].isAlive()){
                thereAreDeadThreads = true;// Есть не живые треды
            } else {
                nbRunning++;
            }
        }
        return thereAreDeadThreads;
    }
}
