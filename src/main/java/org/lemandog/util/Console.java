package org.lemandog.util;

import org.lemandog.App;
public class Console {
    static final int CON_WIDTH = 120;

    private static int amLen;
    private static int dist;

    public static void setAm(int particleAm, int stepsAm){
        amLen = Integer.toString(particleAm).length();
        dist = Integer.toString(stepsAm).length();
    }
    public static void ready(){
        printLine('@');
        coolPrintout("Atom simulation v 0.4.4 ");
        printLine('@');
        if (Runtime.getRuntime().maxMemory()/1048576 <= 4086) {
            coolPrintout("Currently, your machine only has " + Runtime.getRuntime().maxMemory() / 1048576 + "Mbytes of free RAM available to this instance of JVM (which is standard or below)");
            coolPrintout("You could use -Xmx<sizeInMb>m command after  \"java\" command OR in JLINK_VM_OPTIONS to allocate more memory");
        }

    }

    public static void printLine(char target) {
        for (int i = 0; i < CON_WIDTH; i++) {
            System.out.print(target);
        }
    }
    public static void coolPrintout(String line){
        for (int i = 0; i < CON_WIDTH/2 - line.length()/2; i++) {
            System.out.print(" ");
        }
        System.out.println(line);
    }
    public static void particleOut(int ordinal, int timesHitWall, int timesHitGen, boolean tarIsHit, int stepsPassed){
        System.out.println(String.format("#%0" +amLen+ "d",ordinal) + "; "+"On step: "+String.format("#%0" +dist+ "d",stepsPassed)
                +"; Wall / Gen bounces = " +timesHitWall + " / " + timesHitGen + " Met with target?: " + tarIsHit);
    }
}