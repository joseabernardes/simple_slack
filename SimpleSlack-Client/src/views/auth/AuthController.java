/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.auth;

import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import utils.Protocol;

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

    private PrintWriter out;
    @FXML
    private JFXTextField registUsername;
    @FXML
    private JFXPasswordField registPassword1;
    @FXML
    private JFXPasswordField registPassword2;
    @FXML
    private JFXTextField loginUsername;
    @FXML
    private JFXPasswordField loginPassword;

    public void setController(PipedOutputStream pipedOutput) {
        out = new PrintWriter(pipedOutput, true);
    }

    @FXML
    private void loginAction(ActionEvent event) {
        String username = loginUsername.getText();
        String password = loginPassword.getText();
        out.println(Protocol.Client.Auth.LOGIN + " " + username + " " + password);

    }

    public void loginError(String error) {
        logErrors.setText(error);
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
