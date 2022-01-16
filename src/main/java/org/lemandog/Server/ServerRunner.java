package org.lemandog.Server;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import org.lemandog.MainController;
import org.lemandog.Sim;
import org.lemandog.SimDTO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ServerRunner {
        public static ArrayDeque<Sim> simQueue = new ArrayDeque<>();
        static ServerSocket server;
        static ServerSocket serverStatus;
        @Getter
        @Setter
        static String email;
        @Getter
        @Setter
        static StringBuilder report = new StringBuilder();
        @Getter
        @Setter
        static File filesPath;
        public static void main() {
            Thread serverStatusReport = new Thread(()->{
                try {
                    serverStatus = new ServerSocket(5905);
                    Socket clientSocketState = serverStatus.accept();
                    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocketState.getOutputStream()));
                    out.writeUTF(statusReport());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverStatusReport.start();
            Thread serverRunner = new Thread(()->{
                try {
                    server = new ServerSocket(5904);
                    Socket clientSocket = server.accept();
                    ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                    ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                    ArrayDeque<SimDTO> accepted = (ArrayDeque<SimDTO>) in.readObject();
                        if ( accepted != null){
                            String answer;
                            if (accepted.element().getUserEmail().isBlank()){
                                answer = "NO EMAIL IS GIVEN! SIM NOT STARTED";
                                accepted = new ArrayDeque<>();
                            }
                            else{
                                answer = "ACCEPTED QUEUE OF SIM OF SIZE " + accepted.size() + " FROM " + accepted.element().getUserEmail();
                                setEmail(accepted.element().getUserEmail());
                                setReport(new StringBuilder());
                            }
                            out.writeUTF(answer);
                            System.out.println(answer);
                            out.flush();
                        }

                    for (SimDTO sim : accepted){
                        MainController.simQueue.add(new Sim(sim));
                    }
                    Platform.runLater(()->{MainController.simQueue.pop().start();});
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }

            });
            serverRunner.start();
            System.out.println("SERVER STARTED!");
        }

        private static String statusReport() {
            return "ATOM SIM SERVER REPORTING:"+ "\n" +
                    "OPERATING ON " + Runtime.getRuntime().maxMemory() / 1048576 + "MB OF RAM \n" +
                    "WITH " + simQueue.size() + " SIM IN QUEUE" +
                    "IS READY: " + !server.isClosed();

        }

    public static void addLine(Sim run){
        report.append(run.thisRunIndex + " OVER,  " + " TIMESTAMP: " + LocalDateTime.now() + "\n");
    }
    public static void addLine(String line){
        report.append(line + "\n");
    }
    public static File getAttachments() {
        File report = new File("report.zip");
        try {
            FileOutputStream fos = new FileOutputStream(report);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (File one : filesPath.listFiles()) {
            FileInputStream fis = new FileInputStream(one);
            ZipEntry zipEntry = new ZipEntry(one.getName());
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.flush();
        zipOut.close();
        fos.flush();
        fos.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
        return report;
    }
}
