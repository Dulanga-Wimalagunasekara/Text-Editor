package controller;

import javafx.event.ActionEvent;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;




public class AboutFormController{
    public AnchorPane aboutpane;

    public void btnCloseOnAction(ActionEvent actionEvent) {
       Stage stage = (Stage) aboutpane.getScene().getWindow();
       stage.close();


    }
}
