package org.lemandog;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import org.lemandog.util.Console;
import org.lemandog.util.Output;

import java.util.Arrays;
/*
---------------> X
|\
| \
|  \
|   ┘Z
↓
Y
*/

public class EngineDraw {
    static Stage draw = new Stage();
    static Group root = new Group();
    static Scene scene = new Scene(root, 600, 600);
    static double multiToFill;
    static Box chamberR;
    static Box targetR;
    static Box generatorR;

    public static Sphere engine3D(Particle currPos) {
        currPos.getCurrSphere();
        Sphere toDraw = currPos.obj;
        toDraw.setMaterial(currPos.obj.getMaterial());
        return toDraw;
    }

    public static void eSetup() { //Отрисовка камеры
        draw.setResizable(true);
        Image icon = new Image("/atomSim.png");
        draw.getIcons().add(icon);

        multiToFill = scene.getHeight()/(Arrays.stream(Sim.currentSim.CHA_SIZE).max().getAsDouble()); // Множитель для установки размера окна в зависимости от размера монитора
        scene.setFill(Color.BLACK);
        chamberR = new Box((Sim.currentSim.CHA_SIZE[0]),(Sim.currentSim.CHA_SIZE[1]),(Sim.currentSim.CHA_SIZE[2]));

        targetR = new Box((Sim.currentSim.TAR_SIZE[0]),(Sim.currentSim.TAR_SIZE[1]),(Sim.currentSim.TAR_SIZE[2]));
        targetR.setTranslateY((-Sim.currentSim.CHA_SIZE[1]/2));

        if(Output.outputPic) {
            Output.setTargetSize(chamberR.getBoundsInParent());
            Output.picState = new int[(int) Output.maxWidth][(int) Output.maxDepth];
        }
        generatorR = new Box((Sim.currentSim.GEN_SIZE[0]),(Sim.currentSim.GEN_SIZE[1]),(Sim.currentSim.GEN_SIZE[2]));
        generatorR.setTranslateY((Sim.currentSim.CHA_SIZE[1]/2));

        PerspectiveCamera main = new PerspectiveCamera();

        main.setTranslateX(-scene.getHeight()/2); //Чтобы камера в центре имела начало координат
        main.setTranslateY(-scene.getHeight()/2);
        main.setTranslateZ((Arrays.stream(Sim.currentSim.CHA_SIZE).max().getAsDouble())*multiToFill - Sim.currentSim.CHA_SIZE[2]);
        Console.coolPrintout("POS " + main.getTranslateZ() + " FILL MULTI " + multiToFill );

        main.setNearClip(0.001);
        main.setFieldOfView(51.5);
        scene.setCamera(main);
        // КОНТРОЛЬ КАМЕРЫ
        scene.setOnKeyPressed(keyEvent -> {
            if(keyEvent.getCode() == KeyCode.NUMPAD1 || keyEvent.getCode() == KeyCode.NUMPAD4){
                if(keyEvent.getCode() == KeyCode.NUMPAD1){main.setTranslateX(main.getTranslateX() + 1);} else {main.setTranslateX(main.getTranslateX() - 1);}
                System.out.println("CURRENT CAMERA X POS " + main.getTranslateX());
            }
            if(keyEvent.getCode() == KeyCode.NUMPAD2 || keyEvent.getCode() == KeyCode.NUMPAD5){
                if(keyEvent.getCode() == KeyCode.NUMPAD2){main.setTranslateY(main.getTranslateY() + 1);}else {main.setTranslateY(main.getTranslateY() - 1);}
                System.out.println("CURRENT CAMERA Y POS " + main.getTranslateY());
            }
            if(keyEvent.getCode() == KeyCode.NUMPAD3 || keyEvent.getCode() == KeyCode.NUMPAD6){
                if(keyEvent.getCode() == KeyCode.NUMPAD3){main.setTranslateZ(main.getTranslateZ() + 1);}else {main.setTranslateZ(main.getTranslateZ() - 1);}
                System.out.println("CURRENT CAMERA Z POS " + main.getTranslateZ());
            }
        });

        chamberR.setDrawMode(DrawMode.LINE);

        generatorR.setDrawMode(DrawMode.LINE);
        PhongMaterial genMat = new PhongMaterial();
        genMat.setDiffuseColor(Color.RED);
        genMat.setSpecularPower(1);
        generatorR.setMaterial(genMat);

        targetR.setDrawMode(DrawMode.LINE);
        PhongMaterial tarMat = new PhongMaterial();
        tarMat.setDiffuseColor(Color.GREEN);
        tarMat.setSpecularPower(1);
        targetR.setMaterial(tarMat);

        root.getChildren().add(targetR);
        root.getChildren().add(generatorR);
        root.getChildren().add(chamberR);

        draw.setTitle("Отрисовка");
        draw.setScene(scene);
        draw.show();
        draw.setOnCloseRequest(windowEvent -> {
            Sim.currentSim.simIsAlive = false;
            draw.hide();
        });
    }


    public static void DrawingThreadFire(Particle[] containerSet) {
        Platform.runLater(()-> {
            for (Particle particle : containerSet) {
                if (particle != null) {
                    EngineDraw.root.getChildren().remove(particle.drawObj);
                    particle.getCurrSphere();
                    particle.drawObj = engine3D(particle);
                    EngineDraw.root.getChildren().add(particle.drawObj);
                }
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
        line.setRadius(0.003);
        line.getTransforms().addAll(moveToMidpoint, rotateAroundCenter);

        return line;
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
        double optimalStep = 1/((Math.abs(mixY)+Math.abs(maxY))*10);//Шаг обратно пропорционален пути который нужно пройти

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
    static void drawAPath(Cylinder path){
        if(Sim.currentSim.pathsDr){
            EngineDraw.CylinderThread(path);
        }
    }
    public static boolean takePointOnTarget(Point3D origin, Point3D target, Particle inUse){
        Sphere product = new Sphere();
        //Это очень неэффективный и глупый метод, но он работает (в большинстве случаев)
        //Всё потому что Bounds.intersect считает неверно.
        double mixY = origin.getY();
        double maxY = target.getY();
        double optimalStep = (targetR.getBoundsInParent().getMaxY() - targetR.getBoundsInParent().getMinY())/((Math.abs(mixY)+Math.abs(maxY))*30);//Шаг обратно пропорционален пути который нужно пройти
        if (optimalStep < 1E-5){optimalStep = 1E-5;} // В предельных случаях один шаг становится ОЧЕНЬ долгим. Это вот искусственное ограничение.
        //Эта величина найдена практически и представляет собой компромисс между производительностью и точностью регистрации.
        //Да, если задать размер камеры в пару микрометров, удары не будут регистрироваться. Но никто же не вздумает вписывать настолько малые значения для практических целей
        for (double i = 0; i < 1; i+=optimalStep) {
            product.setTranslateX(origin.getX() + (target.getX() - origin.getX())*i);
            product.setTranslateY(origin.getY() + (target.getY() - origin.getY())*i);
            product.setTranslateZ(origin.getZ() + (target.getZ() - origin.getZ())*i);

            if (origin.getY() + (target.getY() - origin.getY())*i<targetR.getBoundsInParent().getMaxY()
                    && origin.getY() + (target.getY() - origin.getY())*i>targetR.getBoundsInParent().getMinY()){ //Проходит через высоту мишени

                if (product.getTranslateX()>targetR.getBoundsInParent().getMinX()
                        && product.getTranslateX()<targetR.getBoundsInParent().getMaxX()){ //Попадание по X
                    if (product.getTranslateZ()>targetR.getBoundsInParent().getMinZ()
                            && product.getTranslateZ()<targetR.getBoundsInParent().getMaxZ()){ //Попадание по Z

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

    public static boolean takePointOnGenerator(Point3D origin, Point3D target, Particle inUse){
        Sphere product = new Sphere();
        //Это очень неэффективный и глупый метод, но он работает (в большинстве случаев)
        //Всё потому что Bounds.intersect считает неверно.
        double mixY = origin.getY();
        double maxY = target.getY();
        double optimalStep = (generatorR.getBoundsInParent().getMaxY() - generatorR.getBoundsInParent().getMinY())/((Math.abs(mixY)+Math.abs(maxY))*30);//Шаг обратно пропорционален пути который нужно пройти
        if (optimalStep < 1E-5){optimalStep = 1E-5;} // В предельных случаях один шаг становится ОЧЕНЬ долгим. Это вот искусственное ограничение.
        for (double i = 0; i < 1; i+=optimalStep) {
            product.setTranslateX(origin.getX() + (target.getX() - origin.getX())*i);
            product.setTranslateY(origin.getY() + (target.getY() - origin.getY())*i);
            product.setTranslateZ(origin.getZ() + (target.getZ() - origin.getZ())*i);

            if (origin.getY() + (target.getY() - origin.getY())*i<generatorR.getBoundsInParent().getMaxY()
                    && origin.getY() + (target.getY() - origin.getY())*i>generatorR.getBoundsInParent().getMinY()){ //Проходит через высоту мишени

                if (product.getTranslateX()>generatorR.getBoundsInParent().getMinX()
                        && product.getTranslateX()<generatorR.getBoundsInParent().getMaxX()){ //Попадание по X
                    if (product.getTranslateZ()>generatorR.getBoundsInParent().getMinZ()
                            && product.getTranslateZ()<generatorR.getBoundsInParent().getMaxZ()){ //Попадание по Z
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

    public static void reset() {
        EngineDraw.root.getChildren().clear();
    }
}
