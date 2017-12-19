/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SendFile extends Thread {

    private ServerSocket socket;
    private String filePath;

    public SendFile(int port, String path) {
        this.filePath = path;
        try {
            socket = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void run() {
        try {
            Socket client = socket.accept();
            sendFile(client);
        } catch (IOException ex) {
            Logger.getLogger(SendFile.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void sendFile(Socket client) throws IOException {
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());

        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[4096];

        while (fis.read(buffer) > 0) {
            dos.write(buffer);
        }

        fis.close();
        dos.close();
        System.out.println("Ficheiro '" + filePath + "' enviado");
    }

}
