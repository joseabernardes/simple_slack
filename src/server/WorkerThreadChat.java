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
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;
import model.Group;
import model.Message;
import model.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.files.WriteUsers;
import server.utils.GetPort;

public class WorkerThreadChat extends Thread {

    //syncronized hashmap?
    private final Socket socket;
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
//            out.println("Login Usage: login username password\nRegist Usage: regist username password password");
            //login
            boolean bye = false;
            while (loggedUser == null && (inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("login")) {
                    String[] input = inputLine.split(" ");
                    if (input.length == 3) {
                        synchronized (users) {
                            for (User user : users) {
                                if (user.getUsername().equals(input[1]) && user.getSocket() == null) {
                                    if (user.checkPassword(input[2])) {
                                        loggedUser = user;
                                        loggedUser.setSocket(socket);
                                    }
                                    break;
                                }
                            }
                        }
                        if (loggedUser != null) {
                            out.println(makeJsonResponse("loginsuccess", loggedUser.getUsername()).toJSONString());
                        } else {

                            out.println(makeJsonResponse("error", "username_or_password").toJSONString());
                        }
                    } else {
                        out.println(makeJsonResponse("error", "command").toJSONString());
                    }

                } else if (inputLine.startsWith("regist")) {
                    String[] input = inputLine.split(" ");
                    if (input.length == 4) {
                        boolean exists = false;
                        synchronized (users) {
                            for (User user : users) {
                                if (user.getUsername().equals(input[1])) {
                                    exists = true;
//                                break;
                                }
                            }
                        }
                        if (!exists) {
                            if (input[2].equals(input[3])) {
                                users.add(new User(input[1], input[2]));
                                new WriteUsers(userSemaphore, users).start();
                                out.println(makeJsonResponse("registsuccessful", input[1]).toJSONString());

                            } else {
                                out.println(makeJsonResponse("error", "password_match").toJSONString());

                            }

                        } else {
                            out.println(makeJsonResponse("error", "user_already_exists").toJSONString());

                        }
                    } else {
                        out.println(makeJsonResponse("error", "command").toJSONString());

                    }

                } else if (inputLine.equals("Bye")) {
                    bye = true;
                    out.println(makeJsonResponse("bye", "bye").toJSONString());
                    break;
                } else {
                    out.println(makeJsonResponse("error", "command").toJSONString());
                }
            }
            if (!bye) {
                while ((inputLine = in.readLine()) != null) { //while principal
                    boolean leave = false;
                    JSONObject response = new JSONObject();
                    if (inputLine.startsWith("sendprivatemsg")) {
                        String[] input = inputLine.split(" ", 3);
                        if (input.length == 3) {
                            User receiver = null;
                            synchronized (users) {
                                for (User user : users) {
                                    if (user.getUsername().equals(input[1])) {
                                        receiver = user;
                                        break;
                                    }
                                }
                            }
                            if (receiver != null && receiver.getSocket() != null) { //se user existe e se tem socket(se tem login)
                                PrintWriter outReceiver = new PrintWriter(receiver.getSocket().getOutputStream(), true);
                                Message msg = new Message(loggedUser.getUsername(), LocalDateTime.now(), input[2]);
                                response = makeJsonResponse("receivemsg", msg.toString());
                                outReceiver.println(response.toJSONString());
                                receiver.addMessage(loggedUser, msg);
                                loggedUser.addMessage(loggedUser, msg);
                            } else {
                                out.println(makeJsonResponse("error", "username").toJSONString());
                            }

                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith("sendprivatefile")) {
                  

                    } else if (inputLine.startsWith("addgroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            //find if group name exists

                            boolean exists = false;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getName().equals(input[1])) {
                                        exists = true;
                                        break;
                                    }
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
                                response = makeJsonResponse("groupadded", input[1]);
                                out.println(response.toJSONString());

                            } else {
                                response = makeJsonResponse("error", "group_exists");
                                out.println(response.toJSONString());
                            }

                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }

                    } else if (inputLine.startsWith("joingroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getName().equals(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se group existe
                                loggedUser.addGroup(group);
                                group.addUser(loggedUser);

                                response = makeJsonResponse("joinedgroup", group.toString());
                                out.println(response.toJSONString());
                            } else {
                                response = makeJsonResponse("error", "group_not_exists");
                                out.println(response.toJSONString());
                            }
                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith("remgroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getName().equals(input[1])) {
                                        group = x;
                                        break;
                                    }
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
                                    response = makeJsonResponse("error", "group_not_empty");
                                    out.println(response.toJSONString());
                                }

                            } else {
                                response = makeJsonResponse("error", "group_not_exists");
                                out.println(response.toJSONString());
                            }

                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }

                    } else if (inputLine.startsWith("leavegroup")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getName().equals(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se group existe
                                loggedUser.removeGroup(group);
                                group.removeUser(loggedUser);
                                byte[] buf = new byte[256];
                                System.out.println("UNICAST SENDER PORT:" + socketUDP.getPort() + " GROUP:" + input[1]);
//                                String string = loggedUser.getUsername() + " " + input[0];
                                String res = makeJsonResponse("leave", loggedUser.getUsername()).toJSONString();
                                buf = res.getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                                socketUDP.send(packet);
                            } else {
                                response = makeJsonResponse("error", "group_not_exists");
                                out.println(response.toJSONString());
                            }
                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }

                    } else if (inputLine.startsWith("sendgroupmsg")) {
                        String[] input = inputLine.split(" ", 3);
                        if (input.length == 3) {
                            Group group = null;
                            synchronized (loggedUser.getGroups()) {
                                for (Group x : loggedUser.getGroups()) { //ver se fez join ao grupo
                                    if (x.getName().equals(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se grupo existe

                                byte[] buf = new byte[256];
                                Message msg = new Message(loggedUser.getUsername(), LocalDateTime.now(), input[2]);
                                String res = makeJsonResponse("msg", msg.toString()).toJSONString();
                                buf = res.toString().getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                                socketUDP.send(packet);
                                group.addMessage(msg);
                            } else {
                                response = makeJsonResponse("error", "group_not_joined");
                                out.println(response.toJSONString());
                            }
                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith("listgroupmsgs")) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (loggedUser.getGroups()) {
                                for (Group x : loggedUser.getGroups()) { //ver se fez join ao grupo
                                    if (x.getName().equals(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }

                            if (group != null) { //se fez join
                                List<Message> msgs = group.getMessages();
                                JSONArray list = new JSONArray();
                                synchronized (msgs) {
                                    for (Message msg : msgs) {
                                        list.add(msg.toString());
                                    }

                                }
                                response = makeJsonResponse("listgroupmsgs", list.toJSONString());
                                out.println(response.toJSONString());
                            } else {
                                response = makeJsonResponse("error", "group_not_exists");
                                out.println(response.toJSONString());
                            }

//                            out.println("groupadded " + input[1]);
                        } else {
                            response = makeJsonResponse("error", "command");
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.equals("logout")) {
                        loggedUser.setSocket(null);
                        break;
                    } else {
                        response = makeJsonResponse("error", "command");
                        out.println(response.toJSONString());
                    }
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

    private JSONObject makeJsonResponse(String command, String data) {
        JSONObject object = new JSONObject();
        object.put("command", command);
        object.put("data", data);
        return object;
    }
}
