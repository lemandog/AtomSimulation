package org.lemandog.Server;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.lemandog.MainController;
import org.lemandog.Sim;
import org.lemandog.SimDTO;
import org.apache.commons.io.FileUtils;
import org.lemandog.util.Console;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static org.lemandog.MainController.currentStream;

public class ServerRunner {
        static ServerSocket server;
        static ServerSocket serverStatus;
        static LocalDateTime startup = LocalDateTime.now();
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
                    while (true) {
                        Socket clientSocketState = serverStatus.accept();
                        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocketState.getOutputStream()));
                        out.writeUTF(statusReport());
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            serverStatusReport.start();
            Thread serverRunner = new Thread(()->{
                try {
                    server = new ServerSocket(5904);
                    while (true) {
                        Socket clientSocket = server.accept();
                        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket.getInputStream()));
                        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
                        ArrayDeque<SimDTO> accepted = (ArrayDeque<SimDTO>) in.readObject();
                        HashMap<String, ArrayDeque<SimDTO>> set = new HashMap();
                        if (accepted != null) {
                            StringBuilder answer = new StringBuilder();
                            for (SimDTO sim : accepted) {
                                sim.setOutput3D(false); //На сервере отрисовка не нужна
                                ArrayDeque<SimDTO> currentUserList;
                                if (!sim.getUserEmail().isBlank()){
                                    if(set.containsKey(sim.getUserEmail())){
                                        currentUserList = set.get(sim.getUserEmail());
                                    }else{
                                        currentUserList = new ArrayDeque<>(0);
                                    }
                                    currentUserList.add(sim);
                                    set.put(sim.getUserEmail(),currentUserList);
                                    answer.append("\n ACCEPTED SIM FROM " + accepted.element().getUserEmail() + "\n CURRENT SIZE - " + currentUserList + "\n");
                                } else{
                                    answer.append("\n NO EMAIL IS GIVEN! SIM NOT CREATED \n ");
                                }
                                setEmail(accepted.element().getUserEmail());
                                setReport(new StringBuilder());

                            }
                                out.writeUTF(answer.toString());
                                System.out.println(answer);
                                out.flush();
                        }
                        MainController.simQueue.putAll(set);
                        if (currentStream.isEmpty()){
                        Platform.runLater(() -> {
                            String key = (String) MainController.simQueue.keySet().toArray()[0];
                            currentStream = MainController.simQueue.get(key);
                            new Sim(currentStream.pop()).start();
                        });
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });
            serverRunner.start();
            System.out.println("SERVER STARTED!");
            System.out.print(statusReport());
        }

        private static String statusReport() {
            StringBuilder ipv4 = new StringBuilder();
            try {
                ipv4.append(InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return "<p>ATOMSIM "+ Console.getVer()+" Server reporting:</p>"+
                   "<p>Operating on "+Runtime.getRuntime().maxMemory() / 1048576 +"  of RAM</p>"+
                   "<p>With "+ currentStream.size()+" simulations in current stream</p>"+
                   "<p>With "+MainController.simQueue.size()+" streams in queue</p>"+
                   "<p>Ready:"+!server.isClosed()+"</p>"+
                   "<p>Uptime: "+ LocalDateTime.ofEpochSecond(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startup.toEpochSecond(ZoneOffset.UTC),0,ZoneOffset.UTC)+ "</p>"+
                   "<p>"+ipv4+"</p>";
        }


    public static void addLine(Sim run){
        report.append(run.thisRunIndex + " OVER,  " + " TIMESTAMP: " + LocalDateTime.now() + "\n");
    }
    public static void addLine(String line){
        report.append(line + "\n");
    }

    public static File[] getAttachments() {
        File dir = new File("/report");
        dir.delete();
        dir.mkdir();
        ZipFile zipFile = new ZipFile("/report/report.zip");
        ArrayList<File> files = new ArrayList<File>();
        Collections.addAll(files, filesPath.listFiles());
        ZipParameters parameters = new ZipParameters();
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.MAXIMUM);
        try {
            zipFile.createSplitZipFile(files, parameters, true, 22214400);
            zipFile.getSplitZipFiles();
        } catch (ZipException e) {
            e.printStackTrace();
        }
        try {
            FileUtils.deleteDirectory(filesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] result = null;
        try {
            result = new File[zipFile.getSplitZipFiles().size()];
            for (int i=0;i<zipFile.getSplitZipFiles().size();i++){
                result[i] = zipFile.getSplitZipFiles().get(i);
            }
        } catch (ZipException e) {
            e.printStackTrace();
        }
        return result;
    }

}
