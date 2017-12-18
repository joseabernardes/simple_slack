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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import libs.portFinder.AvailablePortFinder;
import model.Database;
import model.GroupServer;
import model.PrivateChatServer;
import model.UserServer;
import server.files.WriteDatabase;
import server.files.WriteGroups;
import utils.GetPort;

/**
 *
 * @author José Bernardes
 *
 */
public class Server extends Thread {

    private final ServerSocket serverSocket;
    private List<GroupServer> groups;
    private List<UserServer> users;
    private Database database;
    private final Semaphore userSemaphore;
    private final Semaphore groupSemaphore;
    private final Semaphore databaseSemaphore;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        this.groups = Collections.synchronizedList(new ArrayList<GroupServer>());
        this.users = Collections.synchronizedList(new ArrayList<UserServer>());
        database = new Database(groups, users);
        this.userSemaphore = new Semaphore(1);
        this.groupSemaphore = new Semaphore(1);
        this.databaseSemaphore = new Semaphore(1);
    }

    public int lastGroupID() {
        List<Integer> id = new ArrayList<Integer>();
        for (GroupServer group : groups) {
            id.add(group.getId());
        }
        return (int) Collections.max(id);
    }

    public int lastUserID() {
        List<Integer> id = new ArrayList<Integer>();
        for (UserServer user : users) {
            id.add(user.getId());
        }
        return (int) Collections.max(id);
    }

    private void readDatabase() {
        try {
            databaseSemaphore.acquire();
            ObjectInputStream in;
            File file = new File("database.txt");
            in = new ObjectInputStream(new FileInputStream(file));
            database = (Database) in.readObject();
            this.groups = database.getGroups();
            this.users = database.getUsers();
            GroupServer.setID(lastGroupID());
            UserServer.setID(lastUserID());
            System.out.println("Ficheiro \"database\" lido");
        } catch (ClassNotFoundException | IOException | InterruptedException ex) {
            System.out.println("Erro a ler database");
            System.out.println("Utilizando valores default");
            groups = Collections.synchronizedList(new ArrayList<GroupServer>());
            groups.add(new GroupServer(1111, "ESTG", "230.0.0.1"));
            users = Collections.synchronizedList(new ArrayList<UserServer>());
            users.add(new UserServer("alfredo", "quim"));
            users.add(new UserServer("joel", "joel"));
            database = new Database(groups, users);
        } finally {
            databaseSemaphore.release();
        }

    }

    @Override
    public void start() {
        System.out.println("SERVER IS RUNNING...");
        readDatabase();
        /*
        GroupServer group = null;

        for (GroupServer object : groups) {
            if (object.getName().equals("ESTG")) {
                group = object;
            }
        }

        UserServer alfred = null;
        for (UserServer user : users) {
            if (user.getUsername().equals("alfredo")) {
                alfred = user;
            }
        }

        for (UserServer user : users) {
            if (user.getUsername().equals("joel")) {
                user.addGroup(group);
                alfred.addGroup(group);
                user.addPrivateChat(alfred);
//                alfred.addPrivateChat(user);

            }

        }*/

 /*
        GroupServer group = null;

       for (GroupServer object : groups) {
            if (object.getName().equals("ESTG")) {
                group = object;
            }
        }
        for (UserServer user : users) {
            if (user.getUsername().equals("joel")) {
                user.addGroup(group);
                alfred.addGroup(group);
                user.addPrivateChat(alfred);
                alfred.addPrivateChat(user);

            }

        }
         */
        new WriteDatabase(databaseSemaphore, database).start();
        users.forEach((UserServer user) -> {
            System.out.println(user);
        });

        groups.forEach((GroupServer grou) -> {
            grou.setPort(AvailablePortFinder.getNextAvailable());
            System.out.println(grou);
        });
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
        }
    }
}
