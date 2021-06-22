package org.lemandog;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Sphere;

import java.util.Arrays;
import java.util.Random;

import static org.lemandog.EngineDraw.*;
import static org.lemandog.Sim.*;

public class Particle{
    //Цвета состояний
    Color activeCol = Color.WHITE;
    Color wallhitCol = Color.INDIANRED;
    Color TarHitCol = Color.SKYBLUE;

    double[] coordinates;
    double[] speeds;
    double freerunLen;
    Random rand;
    Sphere obj;
    boolean isInUse;
    boolean active;
    boolean tarIsHit;
    boolean wallIsHit;
    PhongMaterial thisParticleMat;

    Particle(){ //Конструктор класса, вызывается при создании экземпляра
        this.rand = new Random();//Генератор случайных числе нужен для генерации скоростей и координат

        coordinates = generateCord();
        this.speeds = generateSpeed(1);
        this.freerunLen = calcRandLen();
        obj = new Sphere();
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

        product[0] = (rand.nextGaussian()*sv - 0.5*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv - 0.5*sv)/(rand.nextGaussian()*sv - 0.5*sv)));
        product[2] = (rand.nextGaussian()*sv - 0.5*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv - 0.5*sv)/(rand.nextGaussian()*sv - 0.5*sv)));

        if(mode == 1){product[1] = Math.abs(rand.nextGaussian()*sv - 0.5*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv - 0.5*sv)/(rand.nextGaussian()*sv - 0.5*sv)));}
        else {product[1] = (rand.nextGaussian()*sv - 0.5*sv) * Math.cos(Math.PI/2 - Math.atan((rand.nextGaussian()*sv - 0.5*sv)/(rand.nextGaussian()*sv - 0.5*sv)));}
        return product;
    }

    public Thread CreateThread() {
        return new Thread(() -> { //Лямбда-выражение с содержимым потока
            isInUse = true; //Флажок, показывающий рендеру какой атом мы считаем
            long stepsPassed = 0;

            while(active && stepsPassed<LEN){
                if(active) {
                    //Нынешнее приращение
                    double dN = freerunLen/Math.sqrt(Math.pow(speeds[0],
                            2) + Math.pow(speeds[1],2) + Math.pow(speeds[2],2));
                    //Новые координаты
                    for(int i = 0; i<3; i++) {
                        coordinates[i] = coordinates[i] + dN * speeds[i];
                    }
                    getCurrSphere();
                    active = wallCheck() && tarNotMet();
                    //Проверка стен - если столкнулось, возвращает false; Мишень - если столкновение, возвращает false
                    //Так, частица активна (active == true) только тогда, когда нет столкновения со стенами =И= нет столкновения с мишенью

                    //длина пробега
                    freerunLen = calcRandLen();
                    //скорости
                    speeds = generateSpeed(0);
                    stepsPassed++;
                } else {
                    break;}//Остановка исполнения, если не активна
            }
            nbRunning--;    //Перед завершением работы потока - отнять его от счёта запущенных
            isInUse = false;//И отметить частицу чтобы не отрисовывалась заново.
            System.out.println("PARTICLE THREAD ENDED: " + Arrays.toString(coordinates) + " # OF RUNNING THREADS " + nbRunning);
        });
    }

    private boolean tarNotMet() {
        if(target.intersects(obj.getBoundsInParent())){
            System.out.println("PARTICLE HAS HIT TARGET!");
            thisParticleMat.setDiffuseColor(TarHitCol);
            return false;
        }
        return true;
    }

    private boolean wallCheck() {
        if(chamber.intersects(obj.getBoundsInParent())){
            return true;
        }
        thisParticleMat.setDiffuseColor(wallhitCol);
        System.out.println("PARTICLE IS OUT OF BOUNDS!");
        return false;
    }

    public Sphere getCurrSphere() {
        obj.setMaterial(thisParticleMat);
        obj.setRadius(3);
        obj.setTranslateX(coordinates[0]); //Установка координат визуализации атома
        obj.setTranslateY(coordinates[1]);
        obj.setTranslateZ(coordinates[2]);
        return obj;
    }
}
