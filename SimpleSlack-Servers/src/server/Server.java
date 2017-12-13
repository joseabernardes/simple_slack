/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Group;
import model.User;

/**
 *
 * @author José Bernardes
 *
 */
public class Server extends Thread {

    private final ServerSocket serverSocket;
    private List<Group> groups;
    private List<User> users;
    private final Semaphore userSemaphore;
    private final Semaphore groupSemaphore;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.groups = Collections.synchronizedList(new ArrayList<Group>());
        this.users = Collections.synchronizedList(new ArrayList<User>());
        this.userSemaphore = new Semaphore(1);
        this.groupSemaphore = new Semaphore(1);
    }

    private void readUsers() {
        try {
            userSemaphore.acquire();
            ObjectInputStream in;
            File file = new File("src/users.txt");
            in = new ObjectInputStream(new FileInputStream(file));
            users = (List<User>) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {

            users = Collections.synchronizedList(new ArrayList<User>());
            users.add(new User("alfredo", "quim"));
            users.add(new User("joel", "joel"));
        } catch (InterruptedException ex) {
            users = Collections.synchronizedList(new ArrayList<User>());
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            userSemaphore.release();
        }
        User.setID(users.size());
    }

    private void readGroups() {
        try {
            groupSemaphore.acquire();
            ObjectInputStream in;
            File file = new File("src/groups.txt");
            in = new ObjectInputStream(new FileInputStream(file));
            groups = (List<Group>) in.readObject();
        } catch (ClassNotFoundException | IOException ex) {
            groups = Collections.synchronizedList(new ArrayList<Group>());
        } catch (InterruptedException ex) {
            groups = Collections.synchronizedList(new ArrayList<Group>());
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            groupSemaphore.release();
        }
        Group.setID(groups.size());
    }

    @Override
    public void start() {
        System.out.println("SERVER IS RUNNING...");
        readUsers();
        Group group = new Group(1241, "ESTG", "127.0.0.1");
        groups.add(group);
        User alfred = null;
        for (User user : users) {
            if (user.getUsername().equals("alfredo")) {
                alfred = user;
            }

        }

        for (User user : users) {
            if (user.getUsername().equals("joel")) {
                user.addGroup(group);
                user.addPrivateChat(alfred);
            }

        }

        try {
            while (true) {
                new MultiClientThread(serverSocket.accept(), this.groups, this.users, userSemaphore, groupSemaphore).start();
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        int port = (args.length != 1) ? 7777 : Integer.valueOf(args[0]); //se não tiver argumentos, porta 7777, se tiver, lê e seleciona a porta

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            new Server(serverSocket).start();
        } catch (IOException e) {
            System.err.println("Could not listen on port: " + port + ".");
//            System.exit(-1);
        }
    }
}
