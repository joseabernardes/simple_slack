/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/**
 *
 * @author PC
 */
public class MainAplication extends Application implements EventHandler<ActionEvent> {

    private Button sign, registar;
    private TabPane tp;
    private Tab login, regist;
    private TextField username, usernameR;
    private PasswordField pwd, pwdR, confirmpwd;
    private Label user, pass, userR, passR, confirm;
    private GridPane gpLogin, gpRegistar;
    private Scene scene;
  

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Simple Slack");
        stage.getIcons().add(new Image("/images/Slack_Square.png"));

        Label label = new Label();
        label.setText("Welcome to Simple Slack");
        ImageView iv = new ImageView("/images/icon.png");
        iv.setFitHeight(30);
        iv.setFitWidth(30);
        label.setGraphic(iv);

        //String str = "Welcome To " + iv + "imple" + iv + "lack";
        //
        
        
        label.setFont(new Font("Arial", 30));
        label.setTextFill(Color.web("#0076a3"));
        label.setTextAlignment(TextAlignment.JUSTIFY);

        BorderPane bp = new BorderPane();

        tp = new TabPane();
        //tp.setLayoutY(100);

        regist = new Tab("Registar");
        login = new Tab("Log In");
        regist.setClosable(false);
        login.setClosable(false);

        
        tp.getTabs().add(login);
        tp.getTabs().add(regist);
        
        
        if (tp.getSelectionModel().getSelectedItem().equals(login)) {
            gpLogin = new GridPane();
            user = new Label("Username: ");
            gpLogin.add(user, 0, 0);
            username = new TextField();
            username.setPromptText("username");
            gpLogin.add(username, 1, 0);

            pass = new Label("Password: ");
            gpLogin.add(pass, 0, 1);

            pwd = new PasswordField();
            pwd.setPromptText("Password");
            gpLogin.add(pwd, 1, 1);

            sign = new Button("Log in");
            gpLogin.add(sign, 1, 2);

            login.setContent(gpLogin);
        } else if (tp.getSelectionModel().getSelectedItem().equals(regist)) {
            gpRegistar = new GridPane();

            userR = new Label("Username: ");
            gpRegistar.add(userR, 0, 0);
            usernameR = new TextField();
            usernameR.setPromptText("username");
            gpRegistar.add(usernameR, 1, 0);
            passR = new Label("Password: ");
            gpRegistar.add(passR, 0, 1);
            pwdR = new PasswordField();
            pwdR.setPromptText("Password");
            gpRegistar.add(pwdR, 1, 1);
            confirm = new Label("Password");
            gpRegistar.add(confirm, 0, 2);
            confirmpwd = new PasswordField();
            confirmpwd.setPromptText("Confirm password");
            gpRegistar.add(confirmpwd, 1, 2);
            registar = new Button("Regist");
            gpRegistar.add(registar, 1, 3);
            regist.setContent(gpRegistar);
        }

        bp.setTop(label);
        bp.setCenter(tp);
        scene = new Scene(bp, 700, 500);
        scene.getStylesheets().add(MainAplication.class.getResource("style.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void handle(ActionEvent t) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
////        BorderPane p = new BorderPane();
//        sign = new Button();
//        sign.setText("Sign In");
////        p.getChildren().add(button);
