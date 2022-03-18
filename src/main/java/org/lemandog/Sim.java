package org.lemandog;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.lemandog.Server.Messenger;
import org.lemandog.Server.ServerRunner;
import org.lemandog.util.Console;
import org.lemandog.util.Output;

import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.time.LocalDateTime;

import static org.lemandog.MainController.*;

public class Sim implements Serializable {
    static final double k=1.3806485279e-23;//постоянная Больцмана, Дж/К
    @Getter
    private final SimDTO dto;
    private final Output out;
    double m;         //кг
    public GasTypes thisRunMaterial;
    double T, TSource;
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

    static int availableStreams = Runtime.getRuntime().availableProcessors();
    public File selectedPath;
    int availableDimensions;
    int maxDimensions = 3;
    int nbRunning = 0;
    int waitTimeMS;
    double wallBounce;
    double genBounce;
    boolean simIsAlive = false;
    boolean pathsDr;
    Thread mainContr;
    Thread[] calculator;
    @Getter
    EngineDraw draw;

    public static int index = 0;
    public int thisRunIndex;

    public Sim(SimDTO dto){
        index++;
        thisRunIndex = index;
        this.dto = dto;
        //DTO - значит data transfer object
        selectedPath = dto.getOutputPath();
        thisRunMaterial = dto.getGas();
        //м
        double d = thisRunMaterial.diameter;
        m = thisRunMaterial.mass;

        p = dto.getPressure()*Math.pow(10, dto.getPressurePow()); //X*10^Y
        T = dto.getTempAm(); //K
        TSource = dto.getTempSourceAm(); //K
        N = dto.getParticleAm();
        LEN = dto.getStepsAm();

        lambdaN = (k*T/(Math.sqrt(2)*p*Math.PI*Math.pow(d,2)));
        //Как сказано в Paticle, пользователь может сам ввести количество осей.
        //Конечно, я не знаю кому нужна пятимерная симуляция газа, но гибкость кода - важная часть ООП
        availableDimensions = dto.getDimensionCount();
        if (availableDimensions >3){maxDimensions= availableDimensions;}

        GEN_SIZE = new double[maxDimensions]; //XYZ
        TAR_SIZE = new double[maxDimensions]; //XYZ
        CHA_SIZE = new double[maxDimensions]; //XYZ

        CHA_SIZE[0] = dto.getXFrameLen();
        CHA_SIZE[1] = dto.getYFrameLen();
        CHA_SIZE[2] = dto.getZFrameLen();

        center = new Point3D(0,0,0);//Центр камеры для механики переизлучения
        //Размер генератора и мишени - сотая камеры, поэтому в очень низких камерах оно может не работать
        TAR_SIZE[0] = CHA_SIZE[0] * dto.getTarSizeX();
        TAR_SIZE[1] = CHA_SIZE[1]/100;
        TAR_SIZE[2] = CHA_SIZE[2] * dto.getTarSizeZ();

        GEN_SIZE[0] = CHA_SIZE[0] * dto.getGenSizeX();
        GEN_SIZE[1] = CHA_SIZE[1]/100;
        GEN_SIZE[2] = CHA_SIZE[2] * dto.getGenSizeZ();

        waitTimeMS = dto.getWaitTime();
        wallBounce = dto.getBounceWallChance();
        genBounce = dto.getBounceGenChance();

        lastRunning = 0;
        availableStreams = dto.getThreadCount();

        calculator = new Thread[availableStreams];
        pathsDr = dto.isPathDrawing();
        Console.setAm(dto.particleAm,dto.stepsAm);
        //Получается так, что это невероятно огромные массивы, так что инициализировать их будем только если стоит галка.
        //Да, теперь нельзя сохранять результаты прошедшей симуляции после запуска, но Java heap space не будет ругаться.
        out = new Output(this);
        //Тут компилятор ругается, но зря. Это сделано для того чтобы не словить NullPointer далее. Они все будут заменены при запуске.
        for (int i = 0; i < availableStreams; i++) {
            calculator[i] = new Thread();
        }
        container = new Particle[N];
    }

    public void genTest() {
        draw = new EngineDraw(this);
        for (int i = 0; i < N; i++) {
            container[i] = new Particle(i,this);
        }
        getDraw().drawingThreadFire(container);
        mainContr = new Thread(); //Иначе будет NullPointerException. То же причины что и выше
    }



    public void start() {
        draw = new EngineDraw(this);
        simIsAlive = true;
        for (int i = 0; i < N; i++) {
            container[i] = new Particle(i,this);
        }
        //Тут условие для быстродействия. Очень большие симуляции занимают много времени для изначальной отрисовки, так что
        //Первый шаг будет отрисован только если пользователь захочет
        getDraw().drawingThreadFire(container);
        //Это делает код менее читабельным, но гораздо более быстрым.
        mainContr = new Thread(() ->{
        while(lastRunning < N && simIsAlive) {
            if(threadQuotaNotMet()>0){
                for (int i = 0; i< availableStreams; i++){
                    if (!calculator[i].isAlive()){// Найти закончившийся тред
                        try {
                        calculator[i] = null;       // Удалить
                        calculator[i] = container[lastRunning].createThread(); //Создать и запустить новый - на замену старому
                        calculator[i].start();
                        lastRunning++;
                        }catch (ArrayIndexOutOfBoundsException e){Console.coolPrintout("THREAD CREATION MISFIRE");break;}
                    }
                }
            }
        }
            try { //Просто ждём пока закончит считать. Изящнее сделать не получилось
                Thread.sleep(5000);
            } catch (InterruptedException ignore) {}
            Console.printLine('X');
            Console.coolPrintout("SIMULATION RUN IS OVER!");
            Console.coolPrintout( "Longest travel distance - " + Output.getLastPrintStep() +" jumps");
            if(getDto().isDistCalc()){ServerRunner.addLine(getDto());}
            out.toFile();
        simIsAlive = false;
        if (!currentStream.isEmpty()){
            Sim next = new Sim(currentStream.pop());
            try { //Просто ждём пока закончит считать. Изящнее сделать не получилось
                Console.coolPrintout("There are "+currentStream.size()+" more sims in current stream Queue... Starting");
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {}
            Platform.runLater(next::start); //Надо обязательно делать это на потоке JavaFX
        }else {
            Console.coolPrintout("DONE WORKING!");
            if (getDto().isDistCalc()) {
                getDto().report.append("DONE WORKING AT " + LocalDateTime.now());
                File[] attachment = ServerRunner.getAttachments();
                int i = 1;
                for (File att : attachment){
                    Messenger.send(getDto().getUserEmail(), getDto().report.toString(), att, "(Part" + i + " of " + attachment.length + ") ");
                    i++;
                }
                ServerRunner.getFilesPath().delete();
            }
            Toolkit.getDefaultToolkit().beep();
            Toolkit.getDefaultToolkit().beep();
            Toolkit.getDefaultToolkit().beep();
            if (!simQueue.isEmpty()) {
                Console.coolPrintout("ANOTHER STREAM IN GLOBAL QUEUE...");
                simQueue.remove(currentStreamKey);
                currentStreamKey = (String) simQueue.keySet().toArray()[0];
                currentStream = simQueue.get(currentStreamKey);
                Sim next = new Sim(currentStream.pop());
                Console.coolPrintout("Picked up stream of " + currentStream.size() + " sims, Starting..");
                Platform.runLater(next::start); //Надо обязательно делать это на потоке JavaFX
            }
        }
    });
        mainContr.setPriority(Thread.MAX_PRIORITY);
        mainContr.start();
    }

    private int threadQuotaNotMet() {
        int thereAreDeadThreads = 0;
        nbRunning = 0;
        try{
        for (int i = 0; i< availableStreams; i++) {
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
