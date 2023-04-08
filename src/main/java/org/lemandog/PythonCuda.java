package org.lemandog;

import org.lemandog.util.Console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

public class PythonCuda {
    static String serverAddress = "localhost";
    private static final String PYTHON_DOWNLOAD_URL = "https://www.python.org/ftp/python/3.10.0/python-3.10.0-amd64.exe";
    private static final String PYTHON_INSTALLER_FILENAME = "python-installer.exe";

    public static void checkAndInstall() {
        if (!checkIfPythonInstalled()) {
            downloadAndInstallPython();
        }
    }

    private static boolean checkIfPythonInstalled() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "--version");
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null && line.contains("Python")) {
                return true;
            }
        } catch (IOException e) {
            // Ignore exceptions, assume that Python is not installed
        }

        return false;
    }

    private static void downloadAndInstallPython() {
        try {
            URL downloadUrl = new URL(PYTHON_DOWNLOAD_URL);
            Console.coolPrintout("Downloading python!");
            File tempFile = File.createTempFile("python", ".exe");
            Files.copy(downloadUrl.openStream(), tempFile.toPath());
            Console.coolPrintout("Installing python!");
            ProcessBuilder processBuilder = new ProcessBuilder(tempFile.getAbsolutePath(), "/quiet", "/norestart");
            Process process = processBuilder.start();
            process.waitFor();
            tempFile.delete();
            Console.coolPrintout("Installed python and deleted tempfiles");
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to download and install Python: " + e.getMessage());
        }
    }

    PythonCuda() {

    }

    public static String getCUDAData() {
        StringBuilder response;
        if (checkServerRunning()){
            try {
                URL url = new URL(serverAddress+"/data");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            startServer();
            response = new StringBuilder("Не могу связаться с сервером");
        }
        return response.toString();
    }

    private static boolean checkServerRunning() {
        try {
        URL url = new URL(serverAddress+"/health");
        HttpURLConnection conn = null;
        conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
            return Integer.valueOf(conn.getResponseCode()).equals(201);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void startServer() {
    }
    public static void endServer() {
    }
}
