package org.lemandog;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.lemandog.util.Output;

import java.util.Arrays;
import java.util.Random;

import static org.lemandog.EngineDraw.*;
import static org.lemandog.Sim.*;

public class Particle{
    //Цвета состояний
    Color activeCol = Color.WHITE;
    Color wallhitCol = Color.ORANGERED;
    Color TarHitCol = Color.LIME;
    Color GenHitCol = Color.VIOLET;

    int ordinal;
    double[] coordinates;
    double[] speeds;
    double freerunLen;
    Random rand;
    Sphere obj;
    Sphere drawObj;
    boolean isInUse;
    boolean active;
    boolean tarIsHit;
    boolean genIsHit;
    boolean wallIsHit;
    PhongMaterial thisParticleMat;
    Cylinder paths = new Cylinder();

    Particle(int ordinal){ //Конструктор класса, вызывается при создании экземпляра
        this.ordinal = ordinal; //Внутреннее порядковое число частицы. Нужно только для вывода (И, возможно, кривых методов вывода частиц)
        this.rand = new Random();//Генератор случайных чисел нужен для генерации скоростей и координат

        coordinates = generateCord();
        this.speeds = generateSpeed(1);
        this.freerunLen = calcRandLen();
        obj = new Sphere();     //Реальная сфера в масштабе 1 к 1
        drawObj = new Sphere(); //Скалированый obj для отрисовки
        this.isInUse = false;
        this.active = true;
        this.tarIsHit = false;
        this.wallIsHit = false;
        thisParticleMat = new PhongMaterial();
        thisParticleMat.setDiffuseColor(activeCol);
    }

    static double calcRandLen() {
        double xMAX = 0.5/Sim.p;
        double lambda= Math.random()*xMAX; //vxR
        double awaitedNum = (((double) 4/(Math.sqrt(Math.PI)*Math.pow(lambdaN,3))*Math.pow(lambda,2) *Math.exp(-Math.pow((lambda/lambdaN),2)))); //Значение функции в Х
        double vyR = Math.random()*awaitedNum;

        return Math.sqrt(Math.pow(vyR,2) + Math.pow(lambda,2));
    }


    private double[] generateCord() {
        //Мы не знаем, что ударит в голову пользователю. Чтобы даже 100 осей были возможны, в Sim есть проверка на введённое кол-во
        double[] product = new double[Sim.maxDimensions]; //XYZ
        Arrays.fill(product,0);
        // Измерений может быть меньше трёх, но дальше есть код жёстко прописанный под 3Д. (Всё что связано с визуализацией, например)
        // Зануление неиспользуемых измерений заставляет всё работать и не вызывает Array out of bounds & NullPointerException
        for (int i = 0; i < avilableDimensions; i++) {
            product[i] = (Sim.GEN_SIZE[i])*Math.random() - Sim.GEN_SIZE[i]/2; //СЛУЧАЙНОЕ ПОЛОЖЕНИЕ ПО X ИЗ КООРДИНАТ ИЗЛУЧАТЕЛЯ
        }
        product[1] = Sim.CHA_SIZE[1]/2 - GEN_SIZE[1];
        return product;
    }

    private double[] generateSpeed(int mode) {
        double[] product = new double[Sim.maxDimensions]; //XYZ
        //В общем-то планируется механика переиспарения и добавление сюда жёсткого ограничения может пока вызвать проблемы
        Arrays.fill(product,0);
        double sv = Math.sqrt((Sim.k*Sim.T)/Sim.m);
        for (int i = 0; i < avilableDimensions; i++) {
            product[i] = (rand.nextGaussian()*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv)/(rand.nextGaussian()*sv)));
        }

        if(mode == 1 && avilableDimensions>2){
            product[1] = -Math.abs(rand.nextGaussian()*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv)/(rand.nextGaussian()*sv)));}

        return product;
    }

    public Thread CreateThread() {
        Thread product = new Thread(() -> { //Лямбда-выражение с содержимым потока
            isInUse = true; //Флажок, показывающий рендеру какой атом мы считаем
            int stepsPassed = 0;
            while(active && stepsPassed<LEN){

                if (waitTimeMS>=0 && Output.output3D){
                    try {
                        Thread.sleep(waitTimeMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }}

                if (Output.output3D){DrawingThreadFire(new Particle[]{this});}
                //Нынешнее приращение
                double dN = freerunLen/Math.sqrt(Math.pow(speeds[0],
                        2) + Math.pow(speeds[1],2) + Math.pow(speeds[2],2));
                //Новые координаты
                Point3D oldCord = new Point3D(coordinates[0],coordinates[1],coordinates[2]);

                for(int i = 0; i<avilableDimensions; i++) {
                    coordinates[i] = coordinates[i] + dN * speeds[i];
                }
                Point3D newCord = new Point3D(coordinates[0],coordinates[1],coordinates[2]);

                paths = EngineDraw.createConnection(oldCord,newCord);
                //длина пробега
                freerunLen = calcRandLen();
                //скорости
                speeds = generateSpeed(0);

                //Проверка стен - если столкнулось, возвращает false; Мишень - если столкновение, возвращает false
                //Так, частица активна (active == true) только тогда, когда нет столкновения со стенами =И= нет столкновения с мишенью
                tarNotMet(oldCord,newCord);
                wallCheck(oldCord,newCord);
                genNotMet(oldCord,newCord); //Проверяем, был ли удар по источнику
                active = !wallIsHit && !tarIsHit;

                if (tarIsHit){
                    Output.CSVStateReact(obj.getTranslateX(),obj.getTranslateZ());
                    Output.picStateReact(obj.getTranslateX(),obj.getTranslateZ());
                    drawAPath(paths);
                    tarHitCounterI++;
                }
                stepsPassed++;
                paths = null; // Освобождаю память, иначе - более 2000 частиц не запустить
                if (Output.outputCSV){
                    Output.insertValuesToSCV(coordinates,stepsPassed,ordinal);
                }
            }
            if(stepsPassed>Output.lastPrintStep){Output.lastPrintStep = Math.toIntExact(stepsPassed);}
            System.out.println("PARTICLE " +ordinal + " IS DONE WALLHIT?: " + wallIsHit + " TARHIT?: " + tarIsHit +" GENHIT?: " + genIsHit +" ON STEP " + stepsPassed);
            isInUse = false;//И отметить частицу чтобы не отрисовывалась заново.
        });
        product.setPriority(Thread.MIN_PRIORITY);
        return product;
    }

    private void tarNotMet(Point3D oldCord, Point3D newCord) {
        if(EngineDraw.takePointOnTarget(oldCord,newCord,this)){
            this.tarIsHit = true;
            thisParticleMat.setDiffuseColor(TarHitCol);
        }
    }
    private void genNotMet(Point3D oldCord, Point3D newCord) {
        if(EngineDraw.takePointOnGenerator(oldCord,newCord,this) && !wallIsHit){ //Есть ли пересечение?
            if (bounceChance("GEN")){ //Испарения не происходит
            this.genIsHit = true;
            this.wallIsHit = true;
            System.out.println("PARTICLE " + ordinal + " HIT GENERATOR AND STAYED!");
            thisParticleMat.setDiffuseColor(GenHitCol);
        }else { //Испарение происходит, Возвращаем частицу где была до удара
            System.out.println("PARTICLE " + ordinal + " HAS HIT GENERATOR AND FLEW AWAY");
            coordinates[0] = oldCord.getX();
            coordinates[1] = oldCord.getY();
            coordinates[2] = oldCord.getZ();
            this.genIsHit = true;
            this.wallIsHit = false;
        }}
    }

    private void wallCheck(Point3D oldCord, Point3D newCord) {
        if(EngineDraw.takePointOnChamber(oldCord,newCord,this) && !tarIsHit){
            if (bounceChance("WALL") && !genIsHit){
                System.out.println("PARTICLE " + ordinal + " HIT WALL AND STAYED!");
                this.wallIsHit = true;
                thisParticleMat.setDiffuseColor(wallhitCol);}
            else {
                System.out.println("PARTICLE " + ordinal + " HAS HIT WALL AND FLEW AWAY");
                genIsHit = false;
                coordinates[0] = oldCord.getX();
                coordinates[1] = oldCord.getY();
                coordinates[2] = oldCord.getZ();
                this.wallIsHit = false;
        }}
    }

    private boolean bounceChance(String type) {
        if (type.equals("WALL")) {
            return !(Math.random() < App.bounceWallChance.getValue());
        }
        return !(Math.random() < App.bounceGenChance.getValue());
    }

    public void getCurrSphere() {
        obj.setMaterial(thisParticleMat);
        obj.setRadius(1/multiToFill);
        obj.setTranslateX(coordinates[0]); //Установка координат визуализации атома
        obj.setTranslateY(coordinates[1]);
        obj.setTranslateZ(coordinates[2]);
    }

}
