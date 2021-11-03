package org.lemandog;

import javafx.scene.paint.Color;
public enum GasTypes {
    ARGENTUM(107.8682,144,Color.GRAY),
    ALUMINIUM(26.9815386,143, Color.LIGHTSTEELBLUE),
    AURUM(196.966569,144, Color.GOLD),
    CHROME(51.9961,130, Color.LIGHTGRAY),
    CUPRUM(63.546,128,Color.SANDYBROWN),
    MOLYBDAENUM(95.96,139, Color.DARKGRAY),
    NICCOLUM(58.6943,124,Color.LIGHTYELLOW),
    PALLADIUM(106.421,137,Color.SILVER),
    PLATINUM(195.084,139, Color.WHITE),
    TANTALUM(180.947882,149, Color.GRAY),
    TITANIUM(47.8671,147,Color.SILVER),
    WOLFRAMIUM(183.841,137,Color.LIGHTGRAY);


    public double mass;
    public double diameter;
    public double massRAW;
    public double diameterRAW;
    public Color particleCol;

    GasTypes(double AtomMass, double diameterPicoMeters){
        mass=AtomMass*1.660539040e-27; //масса ХРОМ, кг
        this.diameter = diameterPicoMeters*10e-12;
        this.particleCol = Color.color(Math.random()*256,Math.random()*256,Math.random()*256);
        this.massRAW = AtomMass;
        this.diameterRAW = diameterPicoMeters;
    }

    GasTypes(double AtomMass, double diameterPicoMeters, Color chosenCol){
        this.particleCol = chosenCol;
        mass=AtomMass*1.660539040e-27;
        this.diameter = diameterPicoMeters*10e-12;
        this.massRAW = AtomMass;
        this.diameterRAW = diameterPicoMeters;
    }

}
