module com.myhometutor {
    requires javafx.controls;
    requires javafx.fxml;
    
    opens com.myhometutor to javafx.fxml;
    opens com.myhometutor.controller to javafx.fxml;
    
    exports com.myhometutor;
    exports com.myhometutor.controller;
}
