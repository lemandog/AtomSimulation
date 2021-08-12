package org.lemandog;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    public static Slider targetSizeX;
    public static Slider targetSizeZ;
    public static Slider genSizeZ;
    public static Slider genSizeX;
    public static Slider threadCount;
    public static TextField xFrameLen;
    public static TextField yFrameLen;
    public static TextField zFrameLen;
    public static TextField particleAm;
    public static TextField stepsAm;
    public static TextField tempAm;
    public static TextField pressurePow;
    public static TextField pressure;
    public static CheckBox pathDrawing;
    public static Label outputMode;

    public static final Font mainFont = Font.loadFont(Objects.requireNonNull(App.class.getResource("/gost-type-a.ttf")).toExternalForm(), 24); //Подгрузка шрифта

    @Override
    public void start(Stage stage){
        Stage userControlWindows = new Stage();
        userControlWindows.setResizable(false);

        FlowPane userControlPane = new FlowPane(); //Компоновка окна
        Image icon = new Image("/atomSim.png");
        userControlWindows.getIcons().add(icon);
        userControlWindows.setTitle("Контроль");

        userControlWindows.setOnCloseRequest(event -> System.exit(0));

        Scene userControl = new Scene(userControlPane,500,630); //Новое окно с компоновкой
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

        xFrameLen = new TextField("80");
        Label xFrameLenText = new Label("Размеры камеры по оси X");
        xFrameLenText.setLabelFor(xFrameLen);
        xFrameLenText.setFont(mainFont);
        userControlPane.getChildren().add(xFrameLen);
        userControlPane.getChildren().add(xFrameLenText);

        yFrameLen = new TextField("80");
        Label yFrameLenText = new Label("Размеры камеры по оси Y");
        yFrameLenText.setLabelFor(yFrameLen);
        yFrameLenText.setFont(mainFont);
        userControlPane.getChildren().add(yFrameLen);
        userControlPane.getChildren().add(yFrameLenText);

        zFrameLen = new TextField("80");
        Label zFrameLenText = new Label("Размеры камеры по оси Z");
        zFrameLenText.setLabelFor(zFrameLen);
        zFrameLenText.setFont(mainFont);
        userControlPane.getChildren().add(zFrameLen);
        userControlPane.getChildren().add(zFrameLenText);

        pressurePow = new TextField("-18");
        Label pressurePowText = new Label("Давление - степень десятки");
        pressurePowText.setLabelFor(pressurePow);
        pressurePowText.setFont(mainFont);
        userControlPane.getChildren().add(pressurePow);
        userControlPane.getChildren().add(pressurePowText);

        pressure = new TextField("1");
        Label pressureText = new Label("Давление - число перед десяткой (Па)");
        pressureText.setLabelFor(pressurePow);
        pressureText.setFont(mainFont);
        userControlPane.getChildren().add(pressure);
        userControlPane.getChildren().add(pressureText);

        pathDrawing = new CheckBox("Отрисовка путей (Только малое число частиц)");
        pathDrawing.setFont(mainFont);
        userControlPane.getChildren().add(pathDrawing);

        threadCount = new Slider();
        threadCount.setMin(1);
        threadCount.setMax(20);
        threadCount.setValue(Sim.avilableStreams);
        threadCount.setShowTickMarks(true);
        threadCount.setShowTickLabels(true);
        threadCount.setPrefWidth(userControl.getWidth());
        Label threadCountText = new Label("Количество потоков для счёта: " + Sim.avilableStreams);
        threadCountText.setFont(mainFont);
        threadCount.setOnMouseReleased((event) ->
                threadCountText.setText("Количество потоков для счёта: "+ Math.round(threadCount.getValue())));
        Label threadCountTextAv = new Label("Всего найдено доступных потоков: " + Sim.avilableStreams);
        threadCountTextAv.setFont(mainFont);
        userControlPane.getChildren().add(threadCountText);
        userControlPane.getChildren().add(threadCountTextAv);
        userControlPane.getChildren().add(threadCount);


        targetSizeX = new Slider();
        targetSizeX.setMin(0);
        targetSizeX.setMax(1);
        targetSizeX.setValue(0.8);
        targetSizeX.setPrefWidth(userControl.getWidth());
        Label targetSizeXText = new Label("Размер подложки по X (доли от камеры): 0,80");
        targetSizeXText.setFont(mainFont);
        targetSizeX.setOnMouseReleased((event) -> {
            targetSizeXText.setText("Размер подложки по X (доли от камеры): "+ String.format("%3.2f", targetSizeX.getValue())); // Три знака всего, два после запятой
        });
        userControlPane.getChildren().add(targetSizeXText);
        userControlPane.getChildren().add(targetSizeX);


        targetSizeZ = new Slider();
        targetSizeZ.setMin(0);
        targetSizeZ.setMax(1);
        targetSizeZ.setValue(0.8);
        targetSizeZ.setPrefWidth(userControl.getWidth());
        Label targetSizeYText = new Label("Размер подложки по Z (доли от камеры): 0,80");
        targetSizeYText.setFont(mainFont);
        targetSizeZ.setOnMouseReleased((event) -> {
            targetSizeYText.setText("Размер подложки по Z (доли от камеры): "+ String.format("%3.2f", targetSizeZ.getValue())); // Три знака всего, два после запятой
        });
        userControlPane.getChildren().add(targetSizeYText);
        userControlPane.getChildren().add(targetSizeZ);

        genSizeX = new Slider();
        genSizeX.setMin(0);
        genSizeX.setMax(1);
        genSizeX.setValue(0.8);
        genSizeX.setPrefWidth(userControl.getWidth());
        Label genSizeXText = new Label("Размер генератора по X (доли от камеры): 0,80");
        genSizeXText.setFont(mainFont);
        genSizeX.setOnMouseReleased((event) -> {
            genSizeXText.setText("Размер генератора по X (доли от камеры): "+ String.format("%3.2f", genSizeX.getValue())); // Три знака всего, два после запятой
        });
        userControlPane.getChildren().add(genSizeXText);
        userControlPane.getChildren().add(genSizeX);

        genSizeZ = new Slider();
        genSizeZ.setMin(0);
        genSizeZ.setMax(1);
        genSizeZ.setValue(0.8);
        genSizeZ.setPrefWidth(userControl.getWidth());
        Label genSizeYText = new Label("Размер генератора по Z (доли от камеры): 0,80");
        genSizeYText.setFont(mainFont);
        genSizeZ.setOnMouseReleased((event) -> {
            genSizeYText.setText("Размер генератора по Z (доли от камеры): "+ String.format("%3.2f", genSizeZ.getValue())); // Три знака всего, два после запятой
        });
        userControlPane.getChildren().add(genSizeYText);
        userControlPane.getChildren().add(genSizeZ);

        Output.ConstructOutputAFrame();


        Button startSimButt = new Button("Старт симуляции");
        startSimButt.setFont(mainFont);
        startSimButt.setOnAction(event -> Sim.start());
        Button genTest = new Button("Тест генератора частиц");
        genTest.setFont(mainFont);
        genTest.setOnAction(event -> Sim.genTest());
        Button outputOption = new Button("Парамеры вывода");
        outputOption.setFont(mainFont);
        outputOption.setOnAction(event -> Output.disp());

        userControlPane.getChildren().add(startSimButt);
        userControlPane.getChildren().add(genTest);
        userControlPane.getChildren().add(outputOption);


        outputMode = new Label("Вывод выключен!");
        outputMode.setTextFill(Color.INDIANRED);
        outputMode.setFont(mainFont);
        userControlPane.getChildren().add(outputMode);

        userControlWindows.show();
    }
}
