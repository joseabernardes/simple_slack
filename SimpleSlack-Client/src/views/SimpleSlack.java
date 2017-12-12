/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import client.ClientSenderTCP;
import client.ReceiverThread;
import com.jfoenix.controls.JFXDecorator;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import views.auth.AuthController;

/**
 *
 * @author José Bernardes
 */
public class SimpleSlack extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        PipedOutputStream pipedSenderOutput = new PipedOutputStream();
        PipedInputStream pipedSenderInput = new PipedInputStream(pipedSenderOutput);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth/Auth.fxml"));
        Parent root = loader.load();
        AuthController controller = loader.getController();
        controller.setController(pipedSenderOutput);
        int port = 7777;
        String host = "127.0.0.1";

        try {
            Socket clientSocket = new Socket(host, port);
            new ReceiverThread(clientSocket.getInputStream(), controller).start();
            new ClientSenderTCP(clientSocket, pipedSenderInput).start();

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + host + " .");
//            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to:" + host + " .");
//            System.exit(1);
        }

//        Parent root = FXMLLoader.load(getClass().getResource("main/Main.fxml"));
        stage.setTitle("Simple Slack");
        stage.getIcons().add(new Image("/views/images/Slack_Square.png"));
//        Scene scene = new Scene(root);
        JFXDecorator decorator = new JFXDecorator(stage, root);
        decorator.setCustomMaximize(true);

        Scene scene = new Scene(decorator, 500, 600);
//                Scene scene = new Scene(decorator, 1000, 640);
        String uri = getClass().getResource("main/main.css").toExternalForm();
        scene.getStylesheets().add(uri);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("Closing");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
