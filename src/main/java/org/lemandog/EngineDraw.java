package org.lemandog;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Sphere;
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
 */import static org.lemandog.Sim.*;

public class EngineDraw {
    public static Timeline timeline;
    static Stage draw = new Stage();
    static Group root;
    static double multiToFill;
    static Box chamber;
    static Box target;
    static Sphere[] drawing;
    static TranslateTransition translateTransition;

    public static Sphere engine3D(Particle currPos) { //Отрисовываем частицы
                Sphere toDraw = new Sphere(currPos.getCurrSphere().getRadius());
                toDraw.setMaterial(currPos.getCurrSphere().getMaterial());
                toDraw.setTranslateX(getAdjustedCord(currPos.getCurrSphere().getTranslateX()));
                toDraw.setTranslateY(getAdjustedCord(currPos.getCurrSphere().getTranslateY()));
                toDraw.setTranslateZ(getAdjustedCord(currPos.getCurrSphere().getTranslateZ()));
                return toDraw;
    }

    private static double getAdjustedCord(double patient) {
        return patient * multiToFill;
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
        chamber.setTranslateX(0);
        chamber.setTranslateY(0);

        target = new Box(getAdjustedCord(TAR_SIZE[0]),getAdjustedCord(TAR_SIZE[1]),getAdjustedCord(TAR_SIZE[2]));
        target.setTranslateX(0);
        target.setTranslateY(getAdjustedCord(-CHA_SIZE[1]/2));
        target.setTranslateZ(0);
        Box generator = new Box(getAdjustedCord(GEN_SIZE[0]),getAdjustedCord(GEN_SIZE[1]),getAdjustedCord(GEN_SIZE[2]));
        generator.setTranslateX(0);
        generator.setTranslateY(getAdjustedCord(CHA_SIZE[1]/2));
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

        drawing = new Sphere[N];
        translateTransition = new TranslateTransition();
        translateTransition.setAutoReverse(false);

        root.getChildren().add(target);
        root.getChildren().add(generator);
        root.getChildren().add(chamber);

        for (int i = 0; i < Sim.N; i++) {
            App.setOutputLine("PART ADDED " + i + " X:" + Sim.container[i].obj.getTranslateX() + " Y:" +Sim.container[i].obj.getTranslateY() + " Z:" +Sim.container[i].obj.getTranslateZ());
            drawing[i] = engine3D(Sim.container[i]);
            EngineDraw.root.getChildren().add(drawing[i]);
        }
        draw.setTitle("Drawing simulation");
        draw.setScene(scene);
        draw.show();
    }

    public static Timeline DrawingThread(Particle[] objects) {
        timeline= new Timeline(new KeyFrame(Duration.millis(200), event -> {
            for(int i = 0; i<objects.length; i++){
                objects[i].getCurrSphere();
                translateTransition.setNode(drawing[i]);
                translateTransition.setToX(objects[i].coordinates[0]);
                translateTransition.setToY(objects[i].coordinates[1]);
                translateTransition.setToZ(objects[i].coordinates[2]);
                translateTransition.play();
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }
}
