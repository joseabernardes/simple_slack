/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author José Bernardes
 */
import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.List;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import model.Group;
import model.User;
import server.utils.GetPort;

public class WorkerThreadChat extends Thread {

    private final Socket socket;
//    private final ArrayList outList;
    private final List<Group> groups;
    private final List<User> users;
    private final SynchronizedArrayList messages;
    private User loggedUser;

    public WorkerThreadChat(Socket socket, List<Group> groups, List<User> users, SynchronizedArrayList messages) {
        super("WorkerThread");
        this.socket = socket;
        this.groups = groups;
        this.messages = messages;
        this.users = users;
        this.loggedUser = null;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            System.out.println("Utilizador ligou-se");
            out.println("Login Usage: login username password");
            out.println("Regist Usage: regist username password password");
            //login
            boolean bye = false;
            while ((inputLine = in.readLine()) != null && loggedUser == null) {

                if (inputLine.startsWith("login")) {

                    String[] input = inputLine.split(" ");

                    if (input.length == 3) {

                        for (User user : users) {
                            if (user.getUsername().equals(input[1])) {
                                if (user.checkPassword(input[2])) {
                                    loggedUser = user;
                                    loggedUser.setSocket(socket);
                                }
                                break;
                            }
                        }
                        if (loggedUser != null) {
                            out.println("Login success");
                        } else {
                            out.println("Wrong username or password");
                        }
                    } else {
                        out.println("Login Usage: login username password");
                    }

                } else if (inputLine.startsWith("regist ")) {
                    Calendar now = Calendar.getInstance();
                    String outString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + " > " + inputLine;

//                messages.add(outString);
                    out.println(outString);
                } else if (inputLine.equals("Bye")) {
                    bye = true;
                    break;
                } else {
                    out.println("Login Usage: login username password");
                    out.println("Regist Usage: regist username password password");

                }
            }
            if (!bye) {
                while ((inputLine = in.readLine()) != null) { //while principal

//                if (inputLine.startsWith("groupadd ")) {
//
//                    inputLine = inputLine.replaceFirst("groupadd ", "");
//                    if (!inputLine.equals("") && !inputLine.equals(" ")) {
//                        //create group
//                        Group group = new Group(GetPort.getFreeAvaliablePort(groups), inputLine);
//
//                        String outString = "Adicionar grupo " + inputLine;
//                        out.println(outString);
//
//                    } else {
//
//                        //Invalid name
//                    }
//
//                } else {
//                    Calendar now = Calendar.getInstance();
//                    String outString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + " > " + inputLine;
//
////                messages.add(outString);
//                    out.println(outString);
//                }
                    if (inputLine.equals("Bye")) {
                        break;
                    }
                }

            }

            out.close();
            in.close();
            System.out.println("Utilizador desligou-se!");

        } catch (IOException e) {

            System.out.println("Utilizador desligou-se!");
//            e.printStackTrace();
        }
    }
}
