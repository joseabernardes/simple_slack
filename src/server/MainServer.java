/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Group;
import model.User;

/**
 *
 * @author José Bernardes
 */
public class MainServer extends Thread {

    private final ServerSocket serverSocket;
    private final List<Group> groups;
    private final List<User> users;

    public MainServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.groups = new ArrayList<Group>();
        this.users = new ArrayList<User>();
    }

    @Override
    public void start() {
        System.out.println("SERVER IS RUNNING...");
//        ArrayList outList = new ArrayList();
        SynchronizedArrayList messages = new SynchronizedArrayList();
        users.add(new User("alfredo", "quim"));
        try {
            while (true) {
                new WorkerThreadChat(serverSocket.accept(), this.groups, this.users, messages).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(MainServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        int port = (args.length != 1) ? 7777 : Integer.valueOf(args[0]); //se não tiver argumentos, porta 7777, se tiver, lê e seleciona a porta

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new MainServer(serverSocket).start();
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
            System.exit(-1);
        }

    }

}
