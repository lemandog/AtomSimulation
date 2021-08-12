package org.lemandog;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
/*
---------------> X
|\
| \
|  \
|   ┘Z
↓
Y
 */import static org.lemandog.App.*;
import static org.lemandog.Sim.*;

public class EngineDraw {
    static Stage draw = new Stage();
    static Group root;
    static double multiToFill;
    static Box chamber;
    static Box chamberR;
    static Box target;
    static Box targetR;

    public static Sphere engine3D(Particle currPos) { //Скалирование частицы для отрисовки
        currPos.getCurrSphere();
        Sphere toDraw = new Sphere(currPos.obj.getRadius());
        toDraw.setMaterial(currPos.obj.getMaterial());
        toDraw.translateXProperty().set(getAdjustedCord(currPos.obj.getTranslateX()));
        toDraw.translateYProperty().set(getAdjustedCord(currPos.obj.getTranslateY()));
        toDraw.translateZProperty().set(getAdjustedCord(currPos.obj.getTranslateZ()));
        return toDraw;
    }

    public static double getAdjustedCord(double patient) {
        return patient * multiToFill;
    }

    public static void eSetup() { //Отрисовка камеры
        root = new Group();
        draw.setResizable(true);
        Scene scene = new Scene(root, 600, 600);

        Image icon = new Image("/atomSim.png");
        draw.getIcons().add(icon);

        multiToFill = scene.getHeight()/(Arrays.stream(CHA_SIZE).max().getAsDouble() * 1.2); // Множитель для установки размера окна в зависимости от размера монитора
        scene.setFill(Color.BLACK);
        chamber = new Box(getAdjustedCord(CHA_SIZE[0]),getAdjustedCord(CHA_SIZE[1]),getAdjustedCord(CHA_SIZE[2]));
        chamberR = new Box((CHA_SIZE[0]),(CHA_SIZE[1]),(CHA_SIZE[2]));
        chamber.setTranslateX(0);
        chamber.setTranslateY(0);

        target = new Box(getAdjustedCord(TAR_SIZE[0]),getAdjustedCord(TAR_SIZE[1]),getAdjustedCord(TAR_SIZE[2]));
        targetR = new Box((TAR_SIZE[0]),(TAR_SIZE[1]),(TAR_SIZE[2]));
        target.setTranslateX(0);
        targetR.setTranslateY((-CHA_SIZE[1]/2));
        target.setTranslateY(getAdjustedCord(-CHA_SIZE[1]/2));
        target.setTranslateZ(0);

        if(Output.outputPic) {
            Output.setTargetSize(chamberR.getBoundsInParent());
            Output.picState = new int[(int) Output.maxWidth][(int) Output.maxDepth];
        }
        Box generator = new Box(getAdjustedCord(GEN_SIZE[0]),getAdjustedCord(GEN_SIZE[1]),getAdjustedCord(GEN_SIZE[2]));
        Box generatorR = new Box((GEN_SIZE[0]),(GEN_SIZE[1]),(GEN_SIZE[2]));
        generator.setTranslateX(0);
        generator.setTranslateY(getAdjustedCord(CHA_SIZE[1]/2));
        generatorR.setTranslateY((CHA_SIZE[1]/2));
        generator.setTranslateZ(0);

        PerspectiveCamera main = new PerspectiveCamera();
        scene.setCamera(main);
        main.setTranslateX(-scene.getHeight()/2); //Чтобы камера в центре имела начало координат
        main.setTranslateY(-scene.getHeight()/2);
        main.setTranslateZ(-250);
        main.setFieldOfView(70);

        chamber.setDrawMode(DrawMode.LINE);

        generator.setDrawMode(DrawMode.LINE);
        PhongMaterial genMat = new PhongMaterial();
        genMat.setDiffuseColor(Color.RED);
        genMat.setSpecularPower(1);
        generator.setMaterial(genMat);

        target.setDrawMode(DrawMode.LINE);
        PhongMaterial tarMat = new PhongMaterial();
        tarMat.setDiffuseColor(Color.GREEN);
        tarMat.setSpecularPower(1);
        target.setMaterial(tarMat);

        root.getChildren().add(target);
        root.getChildren().add(generator);
        root.getChildren().add(chamber);

        DrawingThreadFire(Sim.container);

        draw.setTitle("Отрисовка");
        draw.setScene(scene);
        draw.show();
    }


    public static void DrawingThreadFire(Particle[] containerSet) {
        Platform.runLater(()-> {
            for (Particle particle : containerSet) {
                //Теоретически, использование собственного порядкового номера частицы как указателя в массиве может вызвать исключения,
                //Но иначе тут не особо есть возможность поступить, увы.
                EngineDraw.root.getChildren().remove(particle.drawObj);
                particle.getCurrSphere();
                particle.drawObj = engine3D(particle);
                EngineDraw.root.getChildren().add(particle.drawObj);
            }
        });
    }

    public static void CylinderThread(Cylinder path) {
        Platform.runLater(()-> EngineDraw.root.getChildren().add(path));
    }

    public static Cylinder createConnection(Point3D origin, Point3D target) {
        Point3D yAxis = new Point3D(0, 1, 0);
        Point3D diff = target.subtract(origin);
        double height = diff.magnitude();
        Point3D mid = target.midpoint(origin);
        Translate moveToMidpoint = new Translate(mid.getX(), mid.getY(), mid.getZ());

        Point3D axisOfRotation = diff.crossProduct(yAxis);
        double angle = Math.acos(diff.normalize().dotProduct(yAxis));
        Rotate rotateAroundCenter = new Rotate(-Math.toDegrees(angle), axisOfRotation);

        Cylinder line = new Cylinder(1, height);
        line.setRadius(0.1);
        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
    }

    public static boolean takePointOnTarget(Point3D origin, Point3D target, Particle inUse){
        Sphere product = new Sphere();
        //Это очень неэффективный и глупый метод, но он работает (в большинстве случаев)
        //Всё потому что Bounds.intersect считает неверно.
        double mixY = origin.getY();
        double maxY = target.getY();
        double optimalStep = 1/((Math.abs(mixY)+Math.abs(maxY))*100);//Шаг обратно пропорционален пути который нужно пройти
        for (double i = 0; i < 1; i+=optimalStep) {
            product.setTranslateX(origin.getX() + (target.getX() - origin.getX())*i);
            product.setTranslateY(origin.getY() + (target.getY() - origin.getY())*i);
            product.setTranslateZ(origin.getZ() + (target.getZ() - origin.getZ())*i);

            if (origin.getY() + (target.getY() - origin.getY())*i<targetR.getBoundsInParent().getMaxY()
                    && origin.getY() + (target.getY() - origin.getY())*i>targetR.getBoundsInParent().getMinY()){ //Проходит через высоту мишени

                if (product.getBoundsInParent().getMinX()>targetR.getBoundsInParent().getMinX()
                        && product.getBoundsInParent().getMaxX()<targetR.getBoundsInParent().getMaxX()){ //Попадание по X
                    if (product.getBoundsInParent().getMinZ()>targetR.getBoundsInParent().getMinZ()
                            && product.getBoundsInParent().getMaxZ()<targetR.getBoundsInParent().getMaxZ()){ //Попадание по Z

                        inUse.coordinates[0] = product.getTranslateX();
                        inUse.coordinates[1] = product.getTranslateY();
                        inUse.coordinates[2] = product.getTranslateZ();
                        inUse.obj = product;
                        return true;
                    }
                }}
        }
        return false;
    }
    public static boolean takePointOnChamber(Point3D origin, Point3D target, Particle inUse){
        Sphere product = new Sphere();
        //Так как мишень перпендикулярна оси Y, логично искать точку пересечения, естественно, от Y
        //XYZ уравнение прямой
        double chamberMinX = chamberR.getBoundsInParent().getMinX();
        double chamberMaxX = chamberR.getBoundsInParent().getMaxX();
        double chamberMinY = chamberR.getBoundsInParent().getMinY();
        double chamberMaxY = chamberR.getBoundsInParent().getMaxY();
        double chamberMinZ = chamberR.getBoundsInParent().getMinZ();
        double chamberMaxZ = chamberR.getBoundsInParent().getMaxZ();
        double mixY = origin.getY();
        double maxY = target.getY();
        double optimalStep = 1/((Math.abs(mixY)+Math.abs(maxY))*100);//Шаг обратно пропорционален пути который нужно пройти

        for (double i = 0; i < 1; i+=optimalStep) {
            product.setTranslateX(origin.getX() + (target.getX() - origin.getX())*i);
            product.setTranslateY(origin.getY() + (target.getY() - origin.getY())*i);
            product.setTranslateZ(origin.getZ() + (target.getZ() - origin.getZ())*i);
            if (chamberMinX>product.getBoundsInParent().getCenterX() || chamberMaxX<product.getBoundsInParent().getCenterX() ||
                chamberMinY>product.getBoundsInParent().getCenterY() || chamberMaxY<product.getBoundsInParent().getCenterY() ||
                chamberMinZ>product.getBoundsInParent().getCenterZ() || chamberMaxZ<product.getBoundsInParent().getCenterZ()
            ){
                        inUse.coordinates[0] = product.getTranslateX();
                        inUse.coordinates[1] = product.getTranslateY();
                        inUse.coordinates[2] = product.getTranslateZ();
                        inUse.obj = product;
                        return true;
            }
        }
        return false;
    }
    static void drawAPath(Cylinder pathADJ){
        if(pathsDr){
            EngineDraw.CylinderThread(pathADJ);
        }
    }
}
