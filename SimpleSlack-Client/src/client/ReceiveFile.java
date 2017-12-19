/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.application.Platform;
import model.UserClient;
import views.main.MainController;

public class ReceiveFile extends Thread {

    private Socket socket;
    private String fileName;
    private int size;
    private UserClient sender;
    private String destinationPath;
    private MainController mainController;

    public ReceiveFile(String host, int port, String fileName, int size, String destinationPath, MainController mainController) {
        try {
            socket = new Socket(host, port);
            this.fileName = fileName;
            this.size = size;
            this.destinationPath = destinationPath;
            this.mainController = mainController;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            saveFile();
            Platform.runLater(() -> {
                mainController.displaySnackBar("Ficheiro '" + fileName + "' recebido com sucesso");
            });
        } catch (IOException e) {
            Platform.runLater(() -> {
                mainController.displaySnackBar("Falha ao receber o ficheiro '" + fileName + "'");
            });
        }
    }

    private void saveFile() throws IOException {
        String path = destinationPath + "/" + fileName;
        int i = 0;
        File file = new File(path);
        while (file.exists()) {
            if (path.indexOf(".") > 0) {
                String extension = path.substring(path.lastIndexOf("."), path.length());
                String name = fileName.substring(0, fileName.lastIndexOf("."));
                if (name.indexOf("_") > 0) {
                    String after = name.substring(0, name.lastIndexOf("_"));
                    fileName = after + "_" + i + extension;
                } else {
                    fileName = name + "_" + i + extension;
                }

                path = destinationPath + "/" + fileName;
                file = new File(path);
                i++;
            }
        }

        DataInputStream dis = new DataInputStream(socket.getInputStream());

        FileOutputStream fos = new FileOutputStream(path);
        byte[] buffer = new byte[4096];

//        int filesize = 15123; // Send file size in separate msg
        int filesize = size;
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }

        fos.close();
        dis.close();
        System.out.println("FILE RECEIVED");

    }

}
