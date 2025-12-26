module com.myhometutor {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;
    requires org.xerial.sqlitejdbc;
    requires java.mail;

    opens com.myhometutor to javafx.fxml;
    opens com.myhometutor.controller to javafx.fxml;
    
    exports com.myhometutor;
    exports com.myhometutor.controller;
    exports com.myhometutor.database;
    exports com.myhometutor.model;
    exports com.myhometutor.util;
}
