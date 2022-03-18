package org.lemandog.Server;

import org.lemandog.Sim;
import org.lemandog.SimDTO;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;

public class ServerHandler {
    public static void sendSimToServer(SimDTO run) {
        ArrayDeque<SimDTO> thisOne = new ArrayDeque<>(0);
        thisOne.add(run);
        sendQueueToServer(thisOne, run.getServerAddress());
    }
    public static void sendQueueToServer(ArrayDeque<SimDTO> simDTOQueue, String serverAddress) {
        try{
        Socket clientSocket1 = new Socket(serverAddress, 5904);
        clientSocket1.setSoTimeout(2000);
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocket1.getOutputStream()));

        out.writeObject(simDTOQueue);
        out.flush();
        clientSocket1.close();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    public static String askServerForOutput(String serverAddress){
        String line = " Нет ответа ";
        try {
            Socket clientSocket2 = new Socket(serverAddress, 5905);
            clientSocket2.setSoTimeout(2000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(clientSocket2.getOutputStream()));
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(clientSocket2.getInputStream()));
            line = in.readUTF();
            clientSocket2.close();
        } catch (IOException ignored) {}
        return line;
    }
}
