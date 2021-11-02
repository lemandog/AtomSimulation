package org.lemandog;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.lemandog.util.Console;
import org.lemandog.util.LoadConfig;
import org.lemandog.util.Output;
import org.lemandog.util.Util;

import java.util.ArrayDeque;
import java.util.Objects;

import static org.lemandog.Sim.currentSim;

public class App extends Application {
    public static Slider targetSizeX;
    public static Slider targetSizeZ;
    public static Slider genSizeZ;
    public static Slider genSizeX;
    public static Slider bounceWallChance;
    public static Slider bounceGenChance;
    public static Slider threadCount;
    public static Slider waitTime;
    public static Slider dimensionCount;
    public static TextField xFrameLen;
    public static TextField yFrameLen;
    public static TextField zFrameLen;
    public static TextField particleAm;
    public static TextField stepsAm;
    public static TextField tempAm;
    public static TextField pressurePow;
    public static TextField pressure;

    public static Label outputMode;
    public static Label fileDropText;
    public static Button startSimButt;

    public static ArrayDeque<Sim> simQueue = new ArrayDeque<>();
    public static final Font mainFont = Font.loadFont(Objects.requireNonNull(App.class.getResource("/gost-type-a.ttf")).toExternalForm(), 24); //Подгрузка шрифта

    @Override
    public void start(Stage stage){
        Console.ready();
        Stage userControlWindows = new Stage();
        userControlWindows.setResizable(false);

        FlowPane userControlPane = new FlowPane(); //Компоновка окна
        Image icon = new Image("/atomSim.png");
        userControlWindows.getIcons().add(icon);
        userControlWindows.setTitle("Контроль");

        userControlWindows.setOnCloseRequest(event -> System.exit(0));

        Scene userControl = new Scene(userControlPane,500,730); //Новое окно с компоновкой
        userControlWindows.setScene(userControl);
        //Добавляем органы управления
        particleAm = new TextField("500"); //Поле для ввода кол-ва частиц
        Label particleAmText = new Label("Количество частиц в симуляции");
        particleAmText.setLabelFor(particleAm);
        particleAmText.setFont(mainFont);
        userControlPane.getChildren().add(particleAm);
        userControlPane.getChildren().add(particleAmText);

        stepsAm = new TextField("5000"); //Поле для ввода кол-ва шагов
        Label stepsAmText = new Label("Количество шагов в симуляции");
        stepsAmText.setLabelFor(stepsAm);
        stepsAmText.setFont(mainFont);
        userControlPane.getChildren().add(stepsAm);
        userControlPane.getChildren().add(stepsAmText);

        tempAm = new TextField("273"); //Поле для ввода температуры
        Label tempAmText = new Label("Температура окружения (в кельвинах)");
        tempAmText.setLabelFor(tempAm);
        tempAmText.setFont(mainFont);
        userControlPane.getChildren().add(tempAm);
        userControlPane.getChildren().add(tempAmText);

        xFrameLen = new TextField("10");
        Label xFrameLenText = new Label("Размеры камеры по оси X");
        xFrameLenText.setLabelFor(xFrameLen);
        xFrameLenText.setFont(mainFont);
        userControlPane.getChildren().add(xFrameLen);
        userControlPane.getChildren().add(xFrameLenText);

        yFrameLen = new TextField("10");
        Label yFrameLenText = new Label("Размеры камеры по оси Y");
        yFrameLenText.setLabelFor(yFrameLen);
        yFrameLenText.setFont(mainFont);
        userControlPane.getChildren().add(yFrameLen);
        userControlPane.getChildren().add(yFrameLenText);

        zFrameLen = new TextField("10");
        Label zFrameLenText = new Label("Размеры камеры по оси Z");
        zFrameLenText.setLabelFor(zFrameLen);
        zFrameLenText.setFont(mainFont);
        userControlPane.getChildren().add(zFrameLen);
        userControlPane.getChildren().add(zFrameLenText);

        pressurePow = new TextField("-8");
        Label pressurePowText = new Label("Давление - степень десятки");
        pressurePowText.setLabelFor(pressurePow);
        pressurePowText.setFont(mainFont);
        userControlPane.getChildren().add(pressurePow);
        userControlPane.getChildren().add(pressurePowText);

        pressure = new TextField("10");
        Label pressureText = new Label("Давление - число, возводимое в степень (Па)");
        pressureText.setLabelFor(pressurePow);
        pressureText.setFont(mainFont);
        userControlPane.getChildren().add(pressure);
        userControlPane.getChildren().add(pressureText);

        HBox queuePanel = new HBox();
        Button queueWinBuilder = new Button("Очередь симуляции");
        queueWinBuilder.setFont(mainFont);
        queueWinBuilder.setOnAction(actionEvent -> Util.constructAWinQueue());
        queuePanel.getChildren().add(queueWinBuilder);
        userControlPane.getChildren().add(queuePanel);
        threadCount = new Slider();
        threadCount.setMin(1);
        threadCount.setMax(20);
        threadCount.setValue(Sim.avilableStreams);
        threadCount.setShowTickMarks(true);
        threadCount.setShowTickLabels(true);
        threadCount.setPrefWidth(userControl.getWidth());
        Label threadCountTextAv = new Label("Потоков у вашего CPU: " + Sim.avilableStreams + " Выбрано: " + Math.round(threadCount.getValue()));
        threadCount.setOnMouseReleased((event) ->
                threadCountTextAv.setText("Потоков у вашего CPU: " + Sim.avilableStreams + " Выбрано: " + Math.round(threadCount.getValue())));
        threadCountTextAv.setFont(mainFont);
        userControlPane.getChildren().add(threadCountTextAv);
        userControlPane.getChildren().add(threadCount);

        HBox waitTimebox = new HBox();
        waitTime = new Slider();
        waitTime.setMin(0);
        waitTime.setMax(200);
        waitTime.setValue(0);
        waitTime.setPrefWidth(userControl.getWidth()/2);
        Label waitTimeText = new Label("Ожидание между шагами: " + Math.round(waitTime.getValue()));
        waitTimeText.setFont(mainFont);
        waitTime.setOnMouseReleased((event) ->
                waitTimeText.setText("Ожидание между шагами: " + Math.round(waitTime.getValue())));
        waitTimeText.setFont(mainFont);
        waitTimeText.setPrefWidth(userControl.getWidth()/2);
        waitTimebox.getChildren().addAll(waitTimeText,waitTime);
        userControlPane.getChildren().add(waitTimebox);

        HBox bounceChanceW = new HBox();
        bounceWallChance = new Slider();
        bounceWallChance.setMin(0);
        bounceWallChance.setMax(1);
        bounceWallChance.setValue(0.2);
        bounceWallChance.setPrefWidth(userControl.getWidth()/2);
        Label bounceWallChanceText = new Label("Отрыв от стен "+ String.format("%3.2f", bounceWallChance.getValue()));
        bounceWallChanceText.setPrefWidth(userControl.getWidth()/2);
        bounceWallChanceText.setFont(mainFont);
        bounceWallChance.setOnMouseReleased((event) -> {
            bounceWallChanceText.setText("Отрыв от стен "+ String.format("%3.2f", bounceWallChance.getValue())); // Три знака всего, два после запятой
        });
        bounceChanceW.getChildren().addAll(bounceWallChanceText,bounceWallChance);
        userControlPane.getChildren().add(bounceChanceW);

        HBox bounceChanceG = new HBox();
        bounceGenChance = new Slider();
        bounceGenChance.setMin(0);
        bounceGenChance.setMax(1);
        bounceGenChance.setValue(0.8);
        bounceGenChance.setPrefWidth(userControl.getWidth()/2);
        Label bounceGenChanceText = new Label("Отрыв от ген. "+ String.format("%3.2f", bounceGenChance.getValue()));
        bounceGenChanceText.setPrefWidth(userControl.getWidth()/2);
        bounceGenChanceText.setFont(mainFont);
        bounceGenChance.setOnMouseReleased((event) -> {
            bounceGenChanceText.setText("Отрыв от ген. "+ String.format("%3.2f", bounceGenChance.getValue())); // Три знака всего, два после запятой
        });
        bounceChanceG.getChildren().addAll(bounceGenChanceText,bounceGenChance);
        userControlPane.getChildren().add(bounceChanceG);

        HBox dimenParX = new HBox();
        dimensionCount = new Slider();
        dimensionCount.setMin(1);
        dimensionCount.setMax(3);
        dimensionCount.setValue(3);
        dimensionCount.setShowTickMarks(true);
        dimensionCount.setShowTickLabels(true);
        dimensionCount.setPrefWidth(userControl.getWidth()/2);
        Label dimensionCountText = new Label("Количество осей: " + 3);
        dimensionCountText.setPrefWidth(userControl.getWidth()/2);
        dimensionCountText.setFont(mainFont);
        dimensionCount.setOnMouseReleased((event) ->
                dimensionCountText.setText("Количество осей: "+ Math.round(dimensionCount.getValue())));
        dimenParX.getChildren().addAll(dimensionCountText,dimensionCount);
        userControlPane.getChildren().add(dimenParX);

        HBox tarParX = new HBox();
        targetSizeX = new Slider();
        targetSizeX.setMin(0);
        targetSizeX.setMax(1);
        targetSizeX.setValue(0.8);
        targetSizeX.setPrefWidth(userControl.getWidth()/2);
        Label targetSizeXText = new Label("Размер подложки по X "+ String.format("%3.2f", targetSizeX.getValue()));
        targetSizeXText.setPrefWidth(userControl.getWidth()/2);
        targetSizeXText.setFont(mainFont);
        targetSizeX.setOnMouseReleased((event) -> {
            targetSizeXText.setText("Размер подложки по X "+ String.format("%3.2f", targetSizeX.getValue())); // Три знака всего, два после запятой
        });
        tarParX.getChildren().addAll(targetSizeXText,targetSizeX);
        userControlPane.getChildren().add(tarParX);

        HBox tarParZ = new HBox();
        tarParZ.setPrefWidth(userControl.getWidth());
        targetSizeZ = new Slider();
        targetSizeZ.setMin(0);
        targetSizeZ.setMax(1);
        targetSizeZ.setValue(0.8);
        targetSizeZ.setPrefWidth(userControl.getWidth()/2);
        Label targetSizeZText = new Label("Размер подложки по Z "+ String.format("%3.2f", targetSizeZ.getValue()));
        targetSizeZText.setPrefWidth(userControl.getWidth()/2);
        targetSizeZText.setFont(mainFont);
        targetSizeZ.setOnMouseReleased((event) -> {
            targetSizeZText.setText("Размер подложки по Z "+ String.format("%3.2f", targetSizeZ.getValue())); // Три знака всего, два после запятой
        });
        tarParZ.getChildren().addAll(targetSizeZText,targetSizeZ);
        userControlPane.getChildren().add(tarParZ);

        HBox generatorParX = new HBox();
        generatorParX.setPrefWidth(userControl.getWidth());
        genSizeX = new Slider();
        genSizeX.setMin(0);
        genSizeX.setMax(1);
        genSizeX.setValue(0.8);
        genSizeX.setPrefWidth(userControl.getWidth()/2);
        Label genSizeXText = new Label("Размер генератора X  "+ String.format("%3.2f", genSizeX.getValue()));
        genSizeXText.setPrefWidth(userControl.getWidth()/2);
        genSizeXText.setFont(mainFont);
        genSizeX.setOnMouseReleased((event) -> {
            genSizeXText.setText("Размер генератора X  "+ String.format("%3.2f", genSizeX.getValue())); // Три знака всего, два после запятой
        });
        generatorParX.getChildren().addAll(genSizeXText,genSizeX);
        userControlPane.getChildren().add(generatorParX);

        HBox generatorParZ = new HBox();
        generatorParZ.setPrefWidth(userControl.getWidth());
        genSizeZ = new Slider();
        genSizeZ.setMin(0);
        genSizeZ.setMax(1);
        genSizeZ.setValue(0.8);
        genSizeZ.setPrefWidth(userControl.getWidth()/2);
        Label genSizeZText = new Label("Размер генератора Z  "+ String.format("%3.2f", genSizeZ.getValue()));
        genSizeZText.setPrefWidth(userControl.getWidth()/2);
        genSizeZText.setFont(mainFont);
        genSizeZ.setOnMouseReleased((event) -> {
            genSizeZText.setText("Размер генератора Z  "+ String.format("%3.2f", genSizeZ.getValue())); // Три знака всего, два после запятой
        });
        generatorParZ.getChildren().addAll(genSizeZText,genSizeZ);
        userControlPane.getChildren().add(generatorParZ);
        Output.ConstructOutputAFrame();

        startSimButt = new Button("Старт симуляции");
        startSimButt.setFont(mainFont);
        startSimButt.setOnAction(event -> {
            if (simQueue.isEmpty()){simQueue.add(new Sim());} // Если Пользователь не использует очередь,
            currentSim = simQueue.pop();
            currentSim.start();
        });
        Button genTest = new Button("Тест генератора");
        genTest.setFont(mainFont);
        genTest.setOnAction(event -> Sim.genTest());
        Button outputOption = new Button("Вывод");
        outputOption.setFont(mainFont);
        outputOption.setOnAction(event -> Output.disp());
        outputOption.setPrefWidth(userControl.getWidth()/4);

        HBox buttonPanel = new HBox();
        buttonPanel.setPrefWidth(userControl.getWidth());
        buttonPanel.getChildren().addAll(startSimButt,genTest,outputOption);
        userControlPane.getChildren().add(buttonPanel);
        buttonPanel.setSpacing(0);
        HBox dragTarget = new HBox();
        dragTarget.setPrefHeight(userControl.getWidth()/14);
        dragTarget.setBackground(new Background(new BackgroundFill(Color.SKYBLUE, null, null)));
        dragTarget.setOnDragEntered(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {

                LoadConfig.select(db.getFiles().get(0));
            }
            /* let the source know whether the string was successfully
             * transferred and used */
            event.consume();
        });
        dragTarget.setAlignment(Pos.CENTER);
        fileDropText = new Label("Конфигурационный .txt переместите сюда");
        fileDropText.setFont(mainFont);
        dragTarget.getChildren().add(fileDropText);

        dragTarget.setMinSize(userControl.getWidth(),30);
        userControlPane.getChildren().add(dragTarget);

        HBox buttonPanelConfig = new HBox();
        Button confInfo = new Button("О конфигурациях");
        confInfo.setFont(mainFont);
        confInfo.setOnAction(event -> LoadConfig.constructConfigInfoFrame());


        Button resetDatabase = new Button("О программе");
        resetDatabase.setFont(mainFont);
        resetDatabase.setOnAction(event -> Util.constructAWin());


        outputMode = new Label("Вывод выключен!");
        outputMode.setTextFill(Color.INDIANRED);
        outputMode.setFont(mainFont);
        buttonPanelConfig.getChildren().addAll(confInfo,resetDatabase,outputMode);
        userControlPane.getChildren().add(buttonPanelConfig);
        userControlWindows.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
