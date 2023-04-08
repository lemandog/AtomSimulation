package org.lemandog.util;

import javafx.application.Application;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Objects;

public class Console {
    @Getter
    static String ver;
    static final int CON_WIDTH = 120;

    private static int amLen;
    private static int dist;

    public static void setAm(int particleAm, int stepsAm){
        amLen = Integer.toString(particleAm).length();
        dist = Integer.toString(stepsAm).length();
    }
    @SneakyThrows
    public static void ready(){
        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model;
        if ((new File("/pom.xml")).exists())
            model = reader.read(new FileReader("/pom.xml"));
        else
            model = reader.read(
                    new InputStreamReader(
                            Objects.requireNonNull(Application.class.getResourceAsStream(
                                    "/META-INF/maven/org.lemandog/AtomSim/pom.xml"
                            ))
                    )
            );
        ver = model.getVersion();
        printLine('@');
        coolPrintout("Atom simulation v "+ver+" ");
        printLine('@');
        if (Runtime.getRuntime().maxMemory()/1048576 <= 4086) {
            coolPrintout("Currently, your machine only has " + Runtime.getRuntime().maxMemory() / 1048576 + "Mbytes of free RAM available to this instance of JVM (which is standard or below)");
            coolPrintout("You could use -Xmx<sizeInMb>m command after  \"java\" command OR in JLINK_VM_OPTIONS to allocate more memory");
            coolPrintout("For example, java -Xmx12G -jar AtomSim.jar will allocate 12GB memory to program");
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