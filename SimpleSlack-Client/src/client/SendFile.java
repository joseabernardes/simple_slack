/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

/**
 *
 * @author José Bernardes
 */
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import model.MessageClient;
import views.main.MainController;

public class SendFile extends Thread {

    private Socket s;
    private String filePath;
    private MainController mainController;

    public SendFile(String host, int port, String file, MainController mainController) {
        this.filePath = file;
        this.mainController = mainController;

        try {
            s = new Socket(host, port);
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        try {
            sendFile(filePath);
            Platform.runLater(() -> {
                mainController.displaySnackBar("Ficheiro enviando com sucesso");
            });

        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
            Platform.runLater(() -> {
                mainController.displaySnackBar("Erro ao enviar o ficheiro");
            });
        }
    }

    public void sendFile(String file) throws IOException {
        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }

        fis.close();
        dos.close();
    }

}
