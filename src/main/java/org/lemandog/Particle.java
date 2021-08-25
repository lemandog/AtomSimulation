package org.lemandog;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;

import java.util.Random;

import static org.lemandog.EngineDraw.*;
import static org.lemandog.Sim.*;

public class Particle{
    //Цвета состояний
    Color activeCol = Color.WHITE;
    Color wallhitCol = Color.ORANGERED;
    Color TarHitCol = Color.LIME;

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
    boolean wallIsHit;
    PhongMaterial thisParticleMat;
    Cylinder paths = new Cylinder();
    Cylinder pathsADJ = new Cylinder();

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
        double[] product = new double[3]; //XYZ
        product[0] = (Sim.GEN_SIZE[0])*Math.random() - Sim.GEN_SIZE[0]/2; //СЛУЧАЙНОЕ ПОЛОЖЕНИЕ ПО X ИЗ КООРДИНАТ ИЗЛУЧАТЕЛЯ
        product[1] = Sim.CHA_SIZE[1]/2 - GEN_SIZE[1];
        product[2] = (Sim.GEN_SIZE[2])*Math.random() - Sim.GEN_SIZE[2]/2; //СЛУЧАЙНОЕ ПОЛОЖЕНИЕ ПО Z ИЗ КООРДИНАТ ИЗЛУЧАТЕЛЯ
        return product;
    }
    private double[] generateSpeed(int mode) {
        double[] product = new double[3]; //XYZ
        double sv = Math.sqrt((Sim.k*Sim.T)/Sim.m);

        product[0] = (rand.nextGaussian()*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv)/(rand.nextGaussian()*sv)));
        product[2] = (rand.nextGaussian()*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv)/(rand.nextGaussian()*sv)));

        if(mode == 1){product[1] = -Math.abs(rand.nextGaussian()*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv)/(rand.nextGaussian()*sv)));}
        else {product[1] = (rand.nextGaussian()*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv)/(rand.nextGaussian()*sv)));}
        return product;
    }

    public Thread CreateThread() {
        Thread product = new Thread(() -> { //Лямбда-выражение с содержимым потока
            isInUse = true; //Флажок, показывающий рендеру какой атом мы считаем
            long stepsPassed = 0;

            while(active && stepsPassed<LEN){
                DrawingThreadFire(new Particle[]{this});
                //Нынешнее приращение
                double dN = freerunLen/Math.sqrt(Math.pow(speeds[0],
                        2) + Math.pow(speeds[1],2) + Math.pow(speeds[2],2));
                //Новые координаты
                Point3D oldCord = new Point3D(coordinates[0],coordinates[1],coordinates[2]);
                Point3D oldCordADJ = new Point3D(getAdjustedCord(coordinates[0]),getAdjustedCord(coordinates[1]),getAdjustedCord(coordinates[2]));

                for(int i = 0; i<3; i++) {
                    coordinates[i] = coordinates[i] + dN * speeds[i];
                }
                Point3D newCord = new Point3D(coordinates[0],coordinates[1],coordinates[2]);
                Point3D newCordADJ = new Point3D(getAdjustedCord(coordinates[0]),getAdjustedCord(coordinates[1]),getAdjustedCord(coordinates[2]));

                paths = EngineDraw.createConnection(oldCord,newCord);
                pathsADJ = EngineDraw.createConnection(oldCordADJ,newCordADJ);

                //длина пробега
                freerunLen = calcRandLen();
                //скорости
                speeds = generateSpeed(0);

                //Проверка стен - если столкнулось, возвращает false; Мишень - если столкновение, возвращает false
                //Так, частица активна (active == true) только тогда, когда нет столкновения со стенами =И= нет столкновения с мишенью
                tarNotMet(oldCord,newCord);
                wallCheck(oldCord,newCord);
                active = !wallIsHit && !tarIsHit;

                if (Output.output || Output.outputGraph){Output.statesF[this.ordinal][(int)stepsPassed] =  1;}
                if (!active){
                    aliveCounterI--;
                    if (Output.output || Output.outputGraph){Output.statesF[this.ordinal][(int)stepsPassed] =  0;}}
                if (wallIsHit && !tarIsHit){
                    drawAPath(pathsADJ);
                    outOfBoundsCounterI++;
                    if (Output.output || Output.outputGraph){Output.statesO[this.ordinal][(int)stepsPassed] =1;}
                } else {
                    if (Output.output || Output.outputGraph){Output.statesO[this.ordinal][(int)stepsPassed] =0;}
                }
                if (tarIsHit){
                    Output.picStateReact(obj.getTranslateX(),obj.getTranslateZ());
                    drawAPath(pathsADJ);
                    tarHitCounterI++;
                    if (Output.output || Output.outputGraph){Output.statesH[this.ordinal][(int)stepsPassed] =1;}
                } else {
                    if (Output.output || Output.outputGraph){Output.statesH[this.ordinal][(int)stepsPassed] =0;}
                }
                stepsPassed++;
                paths = null; // Освобождаю память, иначе - более 2000 частиц не запустить
                pathsADJ = null; // Освобождаю память, иначе - более 2000 частиц не запустить
            }
            if(stepsPassed>Output.lastPrintStep){Output.lastPrintStep = Math.toIntExact(stepsPassed);}
            System.out.println("PARTICLE " +ordinal + " IS DONE WALLHIT?: " + wallIsHit + " TARHIT?: " + tarIsHit +" ON STEP " + stepsPassed);
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

    private void wallCheck(Point3D oldCord, Point3D newCord) {
        if(EngineDraw.takePointOnChamber(oldCord,newCord,this) && !tarIsHit){
            this.wallIsHit = true;
            thisParticleMat.setDiffuseColor(wallhitCol);
        }
    }

    public void getCurrSphere() {
        obj.setMaterial(thisParticleMat);
        obj.setRadius(3);
        obj.setTranslateX(coordinates[0]); //Установка координат визуализации атома
        obj.setTranslateY(coordinates[1]);
        obj.setTranslateZ(coordinates[2]);
    }

}
