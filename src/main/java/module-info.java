module AtomSimulation {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.desktop;
    requires java.sql;
    exports org.lemandog;
    exports org.lemandog.util;
    exports org.lemandog.Server;

    requires java.xml;
    requires java.scripting;
    requires javafx.fxml;
    requires javafx.web;
    requires static lombok;
    requires java.mail;
    requires org.apache.commons.io;
}