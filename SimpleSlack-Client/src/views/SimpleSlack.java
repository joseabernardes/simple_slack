/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package views;

import client.SenderThread;
import client.ReceiverThread;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDecorator;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Text;
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

        //----ENDEREÇO DO SERVIDOR--------
        int port = 7777;
        String host = "127.0.0.1";
        //--------------------------------
        stage.setTitle("Simple Slack");
        stage.setTitle("Simple Slack");
        stage.getIcons().add(new Image("/views/images/Slack_Square.png"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth/Auth.fxml"));
        Parent root = loader.load();

        decorator = new JFXDecorator(stage, root, false, false, true);
        decorator.setStyle("-fx-decorator-color: #39424bbd");
        decorator.setCustomMaximize(true);

        AuthController controller = loader.getController();
        //I/O
        pipedSenderOutput = new PipedOutputStream();
        pipedSenderInput = new PipedInputStream(pipedSenderOutput);
        controller.setController(pipedSenderOutput, decorator, stage);
        Scene scene = new Scene(decorator, 500, 600);
        String uri = getClass().getResource("main/main.css").toExternalForm();
        scene.getStylesheets().add(uri);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        try {
            clientSocket = new Socket(host, port);
            new ReceiverThread(clientSocket.getInputStream(), controller).start();
            new SenderThread(clientSocket, pipedSenderInput).start();
        } catch (IOException e) {
            System.err.println("Don't know about host:" + host + " .");
            controller.closeDialog("O servidor não está ligado");
       

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
        Platform.exit();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
