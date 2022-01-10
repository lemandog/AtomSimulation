package org.lemandog.util;

import org.lemandog.App;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.lemandog.Sim.currentSim;
import static org.lemandog.util.Output.*;

public class DebugTools {
    static final boolean GLOBAL_DEBUG = false;
    public static FileWriter global2;
    synchronized public static void CSVDebug(double[] data) {
        if (GLOBAL_DEBUG){
            if (global2 == null){
                LocalDateTime main = LocalDateTime.now();
                File csv = new File( currentSim.selectedPath.getAbsolutePath()
                        + "/"+sdfF.format(main)+"1.csv");
                try {
                    global2 = new FileWriter(csv);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                for (int i = 0; i < data.length; i++) {
                    global2.write(data[i] + SEPARATOR);
                }
                global2.write(LINE_END);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
    }

    public static void close() {
        if (GLOBAL_DEBUG){
        try {
            if (global2 != null){
            global2.close();
            global2=null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }}
}
