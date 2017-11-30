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
import java.util.List;
import java.util.concurrent.Semaphore;
import model.Group;
import model.User;
import server.files.WriteUsers;
import server.utils.GetPort;

public class WorkerThreadChat extends Thread {

    //syncronized hashmap?
    private final Socket socket;
//    private final ArrayList outList;
    private final List<Group> groups;
    private final List<User> users;
    private User loggedUser;
    private final Semaphore userSemaphore;
    private final Semaphore groupSemaphore;
    private final DatagramSocket socketUDP;

    public WorkerThreadChat(Socket socket, List<Group> groups, List<User> users, Semaphore userSemaphore, Semaphore groupSemaphore) throws SocketException {
        super("WorkerThread");
        this.socket = socket;
        this.groups = groups;
        this.users = users;
        this.userSemaphore = userSemaphore;
        this.groupSemaphore = groupSemaphore;
        this.loggedUser = null;
        this.socketUDP = new DatagramSocket(GetPort.getFreeAvaliablePort(groups));
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            System.out.println("Utilizador ligou-se");
            out.println("Login Usage: login username password\nRegist Usage: regist username password password");
            //login
            boolean bye = false;
            while (loggedUser == null && (inputLine = in.readLine()) != null) {
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
                            out.println("loginsuccess " + loggedUser.getUsername());
                        } else {
                            out.println("Wrong username or password");
                        }
                    } else {
                        out.println("Login Usage: login username password");
                    }

                } else if (inputLine.startsWith("regist")) {
                    String[] input = inputLine.split(" ");
                    if (input.length == 4) {
                        boolean exists = false;
                        for (User user : users) {
                            if (user.getUsername().equals(input[1])) {
                                exists = true;
//                                break;
                            }
                        }
                        if (!exists) {
                            if (input[2].equals(input[3])) {
                                users.add(new User(input[1], input[2]));
                                new WriteUsers(userSemaphore, users).start();
                                out.println("Regist successful");
                            } else {
                                out.println("Password doesn't match");
                            }

                        } else {
                            out.println("Username already exists");
                        }
                    } else {
                        out.println("Regist Usage: regist username password password");
                    }

                } else if (inputLine.equals("Bye")) {
                    bye = true;
                    out.println("Bye");
                    break;
                } else {
                    out.println("Login Usage: login username password\nRegist Usage: regist username password password");

                }
            }
            if (!bye) {
                while ((inputLine = in.readLine()) != null) { //while principal
                    boolean leave = false;
//                    usermessage joel ola joel es lindo
                    if (inputLine.startsWith("sendmsg")) {
                        String[] input = inputLine.split(" ", 3);
                        if (input.length == 3) {
                            User receiver = null;
                            for (User user : users) {
                                if (user.getUsername().equals(input[1])) {
                                    receiver = user;
                                    break;
                                }
                            }
                            if (receiver != null && receiver.getSocket() != null) { //se user existe e se tem socket(se tem login)
                                PrintWriter outReceiver = new PrintWriter(receiver.getSocket().getOutputStream(), true);
                                inputLine = "receivemsg " + loggedUser.getUsername() + " " + input[2];
                                outReceiver.println(inputLine);
                                out.println(inputLine);
                            } else {
                                out.println("Wrong username");
                            }

                        } else {
                            out.println("Wrong command");
                        }

                    } else if (inputLine.startsWith("addgroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            //find if group name exists

                            boolean exists = false;
                            for (Group x : groups) {
                                if (x.getName().equals(input[1])) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                // create group
                                String address = "230.0.0.1";
                                Group group = new Group(GetPort.getFreeAvaliablePort(groups), input[1], address);
                                groups.add(group);
                                group.setServerPort(GetPort.getFreeAvaliablePort(groups)); //para nao retornar a mesma porta que em cima //FAZER DEBUG PARA VER
                                //SE É NECESSARIO O -1 NO CONSTRUTOR
//                            new WriteGroups(groupSemaphore, groups).start();
//                                new MulticastServerThread(address, group.getServerPort(), group.getPort()).start();
                                out.println("groupadded " + input[1]);

                            } else {
                                out.println("group exists");
                            }

                        } else {
                            out.println("Wrong command");
                        }

                    } else if (inputLine.startsWith("joingroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            for (Group x : groups) {
                                if (x.getName().equals(input[1])) {
                                    group = x;
                                    break;
                                }
                            }
                            if (group != null) { //se group existe
                                loggedUser.addGroup(group);
                                group.addUser(loggedUser);
                                out.println("joinedgroup " + group.getPort() + " " + group.getAddress());
                            } else {
                                out.println("Wrong username");
                            }

//                            out.println("groupadded " + input[1]);
                        } else {
                            out.println("Wrong command");
                        }
                    } else if (inputLine.startsWith("remgroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            for (Group x : groups) {
                                if (x.getName().equals(input[1])) {
                                    group = x;
                                    break;
                                }
                            }
                            if (group != null) { //se group existe

                                if (!group.hasUsers()) {
                                    //remover o grupo
                                    groups.remove(group);
                                    //parar o thread
//                                    byte[] buf = new byte[256];
//                                    System.out.println("UNICAST SENDER PORT:" + socketUDP.getPort() + " GROUP:" + input[1]);
//                                    String string = "shutdown";
//                                    buf = string.getBytes();
//                                    DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
//                                    socketUDP.send(packet);

                                } else {
                                    out.println("group has users");
                                }

                            } else {
                                out.println("group not exists");
                            }

                        } else {
                            out.println("Wrong command");
                        }

                    } else if (inputLine.startsWith("leavegroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            for (Group x : groups) {
                                if (x.getName().equals(input[1])) {
                                    group = x;
                                    break;
                                }
                            }
                            if (group != null) { //se group existe
                                loggedUser.removeGroup(group);
                                group.removeUser(loggedUser);
                                byte[] buf = new byte[256];
                                System.out.println("UNICAST SENDER PORT:" + socketUDP.getPort() + " GROUP:" + input[1]);
                                String string = loggedUser.getUsername() + " " + input[0];
                                buf = string.getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                                socketUDP.send(packet);
                            } else {
                                out.println("group not exists");
                            }
                        } else {
                            out.println("Wrong command");
                        }

                    } else if (inputLine.startsWith("sendgroup")) {
                        String[] input = inputLine.split(" ", 3);
                        if (input.length == 3) {
                            Group group = null;
                            for (Group x : loggedUser.getGroups()) {
                                if (x.getName().equals(input[1])) {
                                    group = x;
                                    break;
                                }
                            }
                            if (group != null) { //se group existe

                                byte[] buf = new byte[256];
                                System.out.println("UNICAST SENDER PORT:" + socketUDP.getPort() + " GROUP:" + input[1]);
                                String string = loggedUser.getUsername() + " " + input[2];
                                buf = string.getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                                socketUDP.send(packet);

//                                out.println("joinedgroup " + group.getPort() + " " + group.getAddress());
                            } else {
                                out.println("Group does not exist");
                            }

//                            out.println("groupadded " + input[1]);
                        } else {
                            out.println("Wrong command");
                        }
                    } else if (inputLine.equals("Bye")) {
                        break;

                    } else {
                        out.println("Wrong command");
                    }

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
                }

            }
            socketUDP.close();
            socket.close();
            out.close();
            in.close();
            System.out.println("Utilizador desligou-se!");

        } catch (IOException e) {

            System.out.println("Utilizador desligou-se!");
//            e.printStackTrace();
        }
    }
}
