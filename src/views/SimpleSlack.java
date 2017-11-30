/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author José Bernardes
 */
public class SimpleSlack extends Application {

    @Override
    public void start(Stage stage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("auth/Auth.fxml"));
        Parent root = FXMLLoader.load(getClass().getResource("main/Main.fxml"));
        stage.setTitle("Simple Slack");
        stage.getIcons().add(new Image("/views/images/Slack_Square.png"));
        Scene scene = new Scene(root);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
