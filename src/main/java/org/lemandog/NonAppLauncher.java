package org.lemandog;

import org.lemandog.Server.ServerRunner;

import java.util.Arrays;

//Этот класс предназначен для запуска fatjar
public class NonAppLauncher {
    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        if(!(Arrays.stream(args).toList()).contains("server")) {
            App.main(args);
        } else{ServerRunner.main();}
    }
}
