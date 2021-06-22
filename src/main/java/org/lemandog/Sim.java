package org.lemandog;


import javafx.animation.Timeline;
import static org.lemandog.EngineDraw.DrawingThread;

public class Sim {
    static double k=1.3806485279e-23;//постоянная Больцмана, Дж/К
    private static final double m_Cr=51.9961; //масса ХРОМА, а.е.м.
    static double m=m_Cr*1.660539040e-27; //масса ХРОМ, кг
    private static final double d = 130*10e-12;//Диаметр хрома (м)
    private static final double R=8.31;
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

    public static int avilableStreams = Runtime.getRuntime().availableProcessors();
    public static int nbRunning = 0;
    public static boolean simIsAlive = false;
    static Thread mainContr;
    static Thread[] calculator;

    public static void setup(){
        outOfBoundsCounterI = 0; //Обнуление счётчиков с предыдущего запуска
        tarHitCounterI = 0;
        EngineDraw.root = null;
        p = Math.pow(Double.parseDouble(App.pressure.getText()),Double.parseDouble(App.pressurePow.getText()));
        T = Integer.parseInt(App.tempAm.getText());
        N = Integer.parseInt(App.particleAm.getText());
        LEN = Integer.parseInt(App.stepsAm.getText());
        lambdaN = (k*T/(Math.sqrt(2)*p*Math.PI*Math.pow(d,2)));
        CHA_SIZE[0] = Integer.parseInt(App.xFrameLen.getText());
        CHA_SIZE[1] = Integer.parseInt(App.yFrameLen.getText());
        CHA_SIZE[2] = Integer.parseInt(App.zFrameLen.getText());

        TAR_SIZE[0] = CHA_SIZE[0] * App.targetSizeX.getValue();
        TAR_SIZE[1] = 1;
        TAR_SIZE[2] = CHA_SIZE[2] * App.targetSizeZ.getValue();

        GEN_SIZE[0] = CHA_SIZE[0] * App.genSizeX.getValue();
        GEN_SIZE[1] = 1;
        GEN_SIZE[2] = CHA_SIZE[2] * App.genSizeZ.getValue();
        lastRunning = 0;
        avilableStreams = (int) App.threadCount.getValue();
        calculator = new Thread[avilableStreams];
        for (int i = 0; i < avilableStreams; i++) {
            calculator[i] = new Thread();
        }

        container = new Particle[N];
        for (int i = 0; i < N; i++) {
            container[i] = new Particle();
        }
    }
    public static void genTest() {
    setup();
    EngineDraw.esetup();
    mainContr = new Thread(); //Иначе будет NullPointerException
    EngineDraw.DrawingThread(container).playFromStart();
    }



    public static void start() {
        setup(); //Установка выбраных параметров
        simIsAlive = true;
        EngineDraw.esetup();

        Timeline mainanim = DrawingThread(container);
        mainanim.play();

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
                        System.out.println("PARTICLE THREAD RUNNING # " + lastRunning);
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
            mainanim.stop();
        System.out.println("SIMULATION RUN ENDED");
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
