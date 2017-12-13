/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import client.SenderThread;
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

    private PipedOutputStream pipedSenderOutput;
    private PipedInputStream pipedSenderInput;
    private Socket clientSocket;
    private JFXDecorator decorator;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Simple Slack");
        stage.getIcons().add(new Image("/views/images/Slack_Square.png"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth/Auth.fxml"));
        Parent root = loader.load();

        decorator = new JFXDecorator(stage, root, false, false, false);
        decorator.setStyle("-fx-decorator-color: #39424bbd");
        decorator.setCustomMaximize(true);

        AuthController controller = loader.getController();
        //I/O
        pipedSenderOutput = new PipedOutputStream();
        pipedSenderInput = new PipedInputStream(pipedSenderOutput);
        controller.setController(pipedSenderOutput, decorator, stage);
        int port = 7777;
        String host = "127.0.0.1";

        try {
            clientSocket = new Socket(host, port);
            new ReceiverThread(clientSocket.getInputStream(), controller).start();
            new SenderThread(clientSocket, pipedSenderInput).start();
            Scene scene = new Scene(decorator, 500, 600);
            String uri = getClass().getResource("main/main.css").toExternalForm();
            scene.getStylesheets().add(uri);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + host + " .");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to:" + host + " .");
        }

    }

    @Override
    public void stop() throws Exception {
        System.out.println("Closing");
        pipedSenderOutput.close();
        pipedSenderInput.close();
        if (clientSocket != null) {
            clientSocket.close();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
