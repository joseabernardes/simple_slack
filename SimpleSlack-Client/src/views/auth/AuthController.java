/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views.auth;

import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.json.simple.JSONObject;
import utils.Protocol;
import views.main.MainController;

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

    @FXML
    private JFXTabPane tab_pan;

    private JFXDecorator decorator;

    private Stage stage;

    @FXML
    private void loginAction(ActionEvent event) {
//        out.println(Protocol.Client.Auth.LOGIN + " " + username + " " + password);
        JSONObject obj = new JSONObject();
        obj.put("username", loginUsername.getText());
        obj.put("password", loginPassword.getText());
        out.println(Protocol.makeJSONResponse(Protocol.Client.Auth.LOGIN, obj));

    }

    public void loginError(String error) {
        logErrors.setText(error);
    }

    public MainController loginSuccess(int id ,String username) throws IOException {
        FXMLLoader loader = new FXMLLoader(MainController.class.getResource("Main.fxml"));
        Parent root = loader.load();

        MainController controller = loader.getController();
        controller.setController(id ,username, out);
        decorator.setContent(root);
        decorator.setStyle("-fx-decorator-color:  #3c2539");
        stage.setWidth(1000);
        stage.setHeight(640);

        //center stage
        Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
        stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);
        return controller;
    }

    @FXML
    private void registAction(ActionEvent event) {
//        out.println(Protocol.Client.Auth.REGIST + " " + registUsername.getText() + " " + registPassword1.getText() + " " + registPassword2.getText());
        JSONObject obj = new JSONObject();
        obj.put("username", registUsername.getText());
        obj.put("password1", registPassword1.getText());
        obj.put("password2", registPassword2.getText());
        out.println(Protocol.makeJSONResponse(Protocol.Client.Auth.REGIST, obj));

    }

    public void registSuccess(String username) {
        tab_pan.getSelectionModel().select(1);
        loginUsername.setText(username);
        registUsername.setText("");
        registPassword1.setText("");
        registPassword2.setText("");
    }

    public void setController(PipedOutputStream pipedOutput, JFXDecorator decorator, Stage stage) {
        out = new PrintWriter(pipedOutput, true);
        this.decorator = decorator;
        this.stage = stage;
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

}
