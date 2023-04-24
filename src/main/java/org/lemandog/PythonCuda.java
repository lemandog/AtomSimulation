package org.lemandog;

import org.lemandog.util.Console;
import org.lemandog.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Objects;

import com.squareup.okhttp.*;

public class PythonCuda {
    static OkHttpClient client = new OkHttpClient();
    static String mainExecScript = Objects.requireNonNull(Util.getResourceAsFile("python/serverMainExec.py")).getPath();
    static final String[] commands = {"cmd.exe", "start cmd.exe /k python " + mainExecScript};
    static Process pyServExec;
    static String serverAddress = "http://127.0.0.1:5000";
    private static final String PYTHON_DOWNLOAD_URL = "https://www.python.org/ftp/python/3.10.0/python-3.10.0-amd64.exe";
    private static final String PYTHON_INSTALLER_FILENAME = "python-installer";

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
                Console.coolPrintout("Python is installed already");
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
            File tempFile = File.createTempFile(PYTHON_INSTALLER_FILENAME, ".exe");
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
        String response;
        if (checkServerRunning()) {
            try {
                System.out.println("server is running");
                Request formBody = new Request.Builder()
                        .url(serverAddress + "/data")
                        .build();
                response = client.newCall(formBody).execute().body().string();
            } catch (IOException e) {
                Console.coolPrintout("Ошибка запроса:" + e.getMessage());
                return "Ошибка при отсылке запроса";
            }
        } else {
            System.out.println("server is not running");
            startServer();
            response = "Сервер не запущен";
        }
        return response.toString();
    }

    private static boolean checkServerRunning() {
        if (pyServExec != null){
            return pyServExec.isAlive();
        }
        return false;

    }

    public static void startServer() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(commands);
            pyServExec = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void endServer() {
        Console.coolPrintout("Stopping python server...");
        pyServExec.destroy();
    }
}
