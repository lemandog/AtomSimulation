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
    public static Timeline timeline;
    static Stage draw = new Stage();
    static Group root;
    static double multiToFill;
    static Box chamber;
    static Box chamberR;
    static Box target;
    static Box targetR;
    static Sphere[] drawing;

    public static Sphere engine3D(Particle currPos) { //Отрисовываем частицы
                Sphere toDraw = new Sphere(currPos.getCurrSphere().getRadius());
                toDraw.setMaterial(currPos.getCurrSphere().getMaterial());
                toDraw.translateXProperty().set(getAdjustedCord(currPos.getCurrSphere().getTranslateX()));
                toDraw.translateYProperty().set(getAdjustedCord(currPos.getCurrSphere().getTranslateY()));
                toDraw.translateZProperty().set(getAdjustedCord(currPos.getCurrSphere().getTranslateZ()));
                return toDraw;
    }

    public static double getAdjustedCord(double patient) {
        return patient * multiToFill;
    }
    public static double getDeAdjustedCord(double patient) {
        return patient / multiToFill;
    }

    public static void esetup() { //Отрисовка камеры
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

        drawing = new Sphere[N]; //Это сферы именно для отрисовки. Чтобы не было путаницы с ещё одними экземплярами сфер внутри частиц

        root.getChildren().add(target);
        root.getChildren().add(generator);
        root.getChildren().add(chamber);

        for (int i = 0; i < Sim.N; i++) {
            drawing[i] = engine3D(Sim.container[i]); //Сферы к месту для отрисовки
            EngineDraw.root.getChildren().add(drawing[i]); //Добавляем в сцену
        }
        draw.setTitle("Drawing simulation");
        draw.setScene(scene);
        draw.show();
    }

    public static Timeline TextUpdate() {
        App.timelineT= new Timeline(new KeyFrame(Duration.millis(50), event -> {
            if (!mainContr.isAlive()){partStatusRunning.setTextFill(Color.RED);} else {
                partStatusRunning.setTextFill(Color.BLACK);
            }
            partStatusRunning.setText(" Частиц в работе: " + nbRunning);
            partStatusReady.setText(" Частиц в очереди: " + (N - lastRunning));
            partStatusDone.setText(" Частиц готово: " + lastRunning);
            targetHitCounter.setText(" Упало на мишень: " + tarHitCounterI);
            outOfBoundsCounter.setText(" Упало на стены: " + outOfBoundsCounterI);
        }));
        timelineT.setCycleCount(Animation.INDEFINITE);
        return timelineT;
    }
    public static Timeline DrawingThreadFire() {
        timeline = new Timeline(new KeyFrame(Duration.millis(50), event -> {
        Platform.runLater(()-> {
            for(int i = 0; i<drawing.length; i++){
                EngineDraw.root.getChildren().remove(drawing[i]);
                drawing[i] = engine3D(Sim.container[i]);
                EngineDraw.root.getChildren().add(drawing[i]);
            }});
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    public static Timeline CylinderThread(Cylinder path) {
        Timeline cylTr = new Timeline(new KeyFrame(Duration.millis(100), event -> EngineDraw.root.getChildren().add(path)));
        cylTr.setCycleCount(1);
        return cylTr;
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

}
