package org.lemandog.util;

public class Console {
    static final int CON_WIDTH = 120;
    static final int CON_HEIGHT = 60;
    public static void ready(){
        for (int i = 0; i < CON_WIDTH; i++) {
            System.out.print("@");
        }
        coolPrintout("Atom simulation v 0.2.60");
        for (int i = 0; i < CON_WIDTH; i++) {
            System.out.print("@");
        }
    }
    public static void coolPrintout(String line){
        for (int i = 0; i < CON_WIDTH/2 - line.length()/2; i++) {
            System.out.print(" ");
        }
        System.out.print(line);
        for (int i = 0; i < CON_WIDTH/2 - line.length()/2; i++) {
            System.out.print(" ");
        }
    }
}
