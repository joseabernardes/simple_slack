/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.auth;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author José Bernardes
 */
public class AuthController implements Initializable {

    @FXML
    private Label logErrors;
    @FXML
    private Label registErrors;

    @FXML
    private void loginAction(ActionEvent event) {
        logErrors.setText("try to login");
    }

    @FXML
    private void registAction(ActionEvent event) {
        registErrors.setText("try to regist");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
