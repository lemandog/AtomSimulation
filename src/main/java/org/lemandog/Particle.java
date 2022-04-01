package org.lemandog;

import javafx.geometry.Point3D;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import org.lemandog.util.Console;
import org.lemandog.util.Output;

import java.util.Arrays;
import java.util.Random;

public class Particle{
    //Цвета состояний
    Color activeCol;
    Color wallHitCol = Color.ORANGERED;
    Color TarHitCol = Color.LIME;
    Color GenHitCol = Color.VIOLET;

    int ordinal;
    double dS;
    double[] coordinates; //XYZ
    double[] speeds;
    double freeRunLen;
    Random rand;
    Sphere obj;
    Sphere drawObj;
    boolean isInUse;
    boolean active;
    boolean tarIsHit;
    boolean genIsHit;
    boolean wallIsHit;
    int timesHitWall = 0;
    int timesHitGen = 0;
    PhongMaterial thisParticleMat;
    Cylinder paths = new Cylinder();
    Sim parent;

    Particle(int ordinal, Sim parent){ //Конструктор класса, вызывается при создании экземпляра
        this.parent = parent;
        activeCol = parent.thisRunMaterial.particleCol;
        this.ordinal = ordinal; //Внутреннее порядковое число частицы. Нужно только для вывода (И, возможно, кривых методов вывода частиц)
        this.rand = new Random();//Генератор случайных чисел нужен для генерации скоростей и координат

        coordinates = generateCord();
        this.speeds = generateSpeed(true);
        this.freeRunLen = calcRandLen(true);
        obj = new Sphere();     //Реальная сфера в масштабе 1 к 1
        drawObj = new Sphere(); //Скалированый obj для отрисовки
        this.isInUse = false;
        this.active = true;
        this.tarIsHit = false;
        this.wallIsHit = false;
        thisParticleMat = new PhongMaterial();
        thisParticleMat.setDiffuseColor(activeCol);
    }

    double calcRandLen(boolean isFirstStep) {
        double xMAX;
        if(isFirstStep){xMAX = 5*parent.lambdaNSource;} else{xMAX = 5*parent.lambdaN;}

        double lambda= Math.random()*xMAX; //vxR
        double awaitedNum = ((double) 4/(Math.sqrt(Math.PI)*Math.pow(parent.lambdaN,3))
                *Math.pow(lambda,2) *Math.exp(-Math.pow((lambda/parent.lambdaN),2))); //Значение функции в Х

        double possibleMax = ((double) 4/(Math.sqrt(Math.PI)*Math.pow(parent.lambdaN,3))
                *Math.pow(parent.lambdaN,2) *Math.exp(-Math.pow((lambda/parent.lambdaN),2))); //Значение функции в Х максимально возможное
        double vyR = Math.random()*possibleMax;
        while(vyR>awaitedNum){
            lambda= Math.random()*xMAX; //vxR
            vyR = Math.random()*possibleMax;
            awaitedNum = ((double) 4/(Math.sqrt(Math.PI)*Math.pow(parent.lambdaN,3))
                    *Math.pow(lambda,2) *Math.exp(-Math.pow((lambda/parent.lambdaN),2))); //Значение функции в Х
        }
        return lambda;
    }


    private double[] generateCord() {
        //Мы не знаем, что ударит в голову пользователю. Чтобы даже 100 осей были возможны, в Sim есть проверка на введённое кол-во
        double[] product = new double[parent.maxDimensions]; //XYZ
        Arrays.fill(product,0);
        // Измерений может быть меньше трёх, но дальше есть код жёстко прописанный под 3Д. (Всё что связано с визуализацией, например)
        // Обнуление неиспользуемых измерений заставляет всё работать и не вызывает Array out of bounds & NullPointerException
        for (int i = 0; i < parent.availableDimensions; i++) {
            product[i] = (parent.GEN_SIZE[i])*Math.random() - parent.GEN_SIZE[i]/2; //СЛУЧАЙНОЕ ПОЛОЖЕНИЕ ПО X ИЗ КООРДИНАТ ИЗЛУЧАТЕЛЯ
        }
        product[1] = parent.CHA_SIZE[1]/2 - parent.GEN_SIZE[1];
        return product;
    }

    private double[] generateSpeed(boolean isFirstStep) {
        double[] product = new double[parent.maxDimensions]; //XYZ
        Arrays.fill(product,0);
        double sv = Math.sqrt((Sim.k*parent.T)/ parent.m); // длина вектора
        if(isFirstStep){sv = Math.sqrt((Sim.k*parent.TSource)/ parent.m);} // длина вектора от температуры источника
        for (int i = 0; i < parent.availableDimensions; i++) {
            product[i] = rand.nextGaussian()*sv;
        }
        if(parent.availableDimensions >2 && getPosChance(product) && isFirstStep){
            for (int i = 0; i < parent.availableDimensions; i++) {
                product[i] = rand.nextGaussian()*sv;
            }
        }
        if(isFirstStep){
            product[1] = -Math.abs(product[1]);
        }
        return product;
    }

    private boolean getPosChance(double[] product) {
        double chance = Math.random();
        double func = Math.cos(product[1]/Math.sqrt((Math.sqrt(Math.pow(product[2], 2) + Math.pow(product[0], 2)))));
        return chance > func;
        //Новый генератор. Оставлю на всякий пожарный
        //Math.cos(product[1]/Math.sqrt((Math.sqrt(Math.pow(product[2], 2) + Math.pow(product[0], 2)))));
        //Старый генератор. Оставлю на всякий пожарный
        //Math.abs(Math.cos(Math.PI / 2 - Math.atan(product[1] / (Math.sqrt(Math.pow(product[2], 2) + Math.pow(product[0], 2))))));
    }


    public Thread createThread() {
        Thread product = new Thread(() -> { //Лямбда-выражение с содержимым потока
            isInUse = true; //Флажок, показывающий рендеру какой атом мы считаем
            int stepsPassed = 0;
            while(active && stepsPassed<parent.LEN){ //Когда симуляция запущена и частица ещё не прошла шаги
                if (parent.waitTimeMS>=0 && parent.getDto().isOutput3D()){//Ожидание?
                    try {
                        Thread.sleep(parent.waitTimeMS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }}

                parent.getDraw().drawingThreadFire(new Particle[]{this});
                //Нынешнее приращение
                dS = freeRunLen/Math.sqrt(Math.pow(speeds[0],
                        2) + Math.pow(speeds[1],2) + Math.pow(speeds[2],2));
                //Так как далее dS умножается на соответсвующую скорость по оси, считаю то что выше верным.

                //Новые координаты
                Point3D oldCord = new Point3D(coordinates[0],coordinates[1],coordinates[2]);
                for(int i = 0; i<parent.availableDimensions; i++) {
                    coordinates[i] = coordinates[i] + dS * speeds[i];
                }
                Point3D newCord = new Point3D(coordinates[0],coordinates[1],coordinates[2]);


                paths = EngineDraw.createConnection(oldCord,newCord);
                //длина пробега
                freeRunLen = calcRandLen(false);
                //скорости
                speeds = generateSpeed(false);

                //Проверка стен - если столкнулось, возвращает false; Мишень - если столкновение, возвращает false
                //Так, частица активна (active == true) только тогда, когда нет столкновения со стенами =И= нет столкновения с мишенью
                wallCheck(oldCord,newCord);
                tarNotMet(oldCord,newCord);
                genNotMet(oldCord,newCord); //Проверяем, был ли удар по источнику

                active = !wallIsHit && !tarIsHit;

                if (tarIsHit){ //Запись столкновений в CSV и или в PNG
                    getCurrSphere();
                    Output.CSVStateReact(obj.getTranslateX(),obj.getTranslateZ());
                    parent.getDraw().drawAPath(paths); //Отрисовка пути
                }
                stepsPassed++;
                paths = null; // Освобождаю память, иначе - более 2000 частиц не запустить
            }
            if(stepsPassed>Output.getLastPrintStep()){Output.setLastPrintStep(stepsPassed);}
            Console.particleOut(ordinal, timesHitWall, timesHitGen, tarIsHit, stepsPassed);
            isInUse = false;//И отметить частицу чтобы не отрисовывалась заново.
        });
        product.setPriority(Thread.MIN_PRIORITY); //Чтобы счётный поток не перебивал поток интерфейса и управления
        return product;
    }

    private void tarNotMet(Point3D oldCord, Point3D newCord) {
        if(parent.getDraw().takePointOnTarget(oldCord,newCord,this)){
            this.tarIsHit = true;
            thisParticleMat.setDiffuseColor(TarHitCol);
        }
    }
    private void genNotMet(Point3D oldCord, Point3D newCord) {
        if(parent.getDraw().takePointOnGenerator(oldCord,newCord,this) && !wallIsHit){ //Есть ли пересечение?
            if (bounceChance("GEN")){ //Испарения не происходит
            this.genIsHit = true;
            this.wallIsHit = true;
            thisParticleMat.setDiffuseColor(GenHitCol);
        }else { //Испарение происходит, Возвращаем частицу где была до удара
            timesHitGen++;
            toCenter(oldCord);
            this.genIsHit = true;
            this.wallIsHit = false;
        }}
    }

    private void wallCheck(Point3D oldCord, Point3D newCord) {
        if(parent.getDraw().takePointOnChamber(oldCord,newCord,this) && !tarIsHit){
            if (bounceChance("WALL")){
                if (!genIsHit){
                this.wallIsHit = true;
                thisParticleMat.setDiffuseColor(wallHitCol);}
            }
            else {
                timesHitWall++;
                genIsHit = false;
                toCenter(oldCord);
                this.wallIsHit = false;
        }}
    }

    private void toCenter(Point3D oldCord) {
        Point3D product = parent.center.interpolate(oldCord,(1 - Math.random()/20));//Точка между старой координатой и центром в случайной линейной пропорции
        coordinates[0] = product.getX();
        coordinates[1] = product.getY();
        coordinates[2] = product.getZ();
    }


    private boolean bounceChance(String type) {
        if (type.equals("WALL")) {
            return !(Math.random() < parent.wallBounce);
        }
        return !(Math.random() < parent.genBounce);
    }


    public void getCurrSphere() {
        obj.setMaterial(thisParticleMat);
        obj.setRadius(1/parent.getDraw().multiToFill);
        obj.setTranslateX(coordinates[0]); //Установка координат визуализации атома
        obj.setTranslateY(coordinates[1]);
        obj.setTranslateZ(coordinates[2]);
    }

}
