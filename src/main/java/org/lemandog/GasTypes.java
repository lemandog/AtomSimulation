package org.lemandog;

import javafx.scene.paint.Color;
public enum GasTypes {
    CHROME(51.9961,130, Color.LIGHTGRAY),
    ALLUMINIUM(26.9815386,143, Color.LIGHTSTEELBLUE);
    public double mass;
    public double diameter;
    Color particleCol;

    GasTypes(double AtomMass, double diameterPicoMeters){
        mass=AtomMass*1.660539040e-27; //масса ХРОМ, кг
        this.diameter = diameter*10e-12;
        this.particleCol = Color.color(256,256,256);
    }

    GasTypes(double AtomMass, double diameterPicoMeters, Color chosenCol){
        this.particleCol = chosenCol;
        mass=AtomMass*1.660539040e-27;
        this.diameter = diameter*10e-12;
    }

}
