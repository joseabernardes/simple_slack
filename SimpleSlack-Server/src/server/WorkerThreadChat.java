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
import server.files.WriteGroups;
import server.files.WriteUsers;
import utils.GetPort;
import utils.Protocol;

public class WorkerThreadChat extends Thread {

    //syncronized hashmap?
    private final Socket socket;
    private final List<Group> groups;
    private final List<User> users;
    private User loggedUser;
    private final Semaphore userSemaphore;
    private final Semaphore groupSemaphore;
    private final DatagramSocket socketUDP;
    private String addressUDP;

    public WorkerThreadChat(Socket socket, List<Group> groups, List<User> users, Semaphore userSemaphore, Semaphore groupSemaphore) throws SocketException {
        super("WorkerThread");
        this.socket = socket;
        this.groups = groups;
        this.users = users;
        this.userSemaphore = userSemaphore;
        this.groupSemaphore = groupSemaphore;
        this.loggedUser = null;
        this.socketUDP = new DatagramSocket(GetPort.getFreeAvaliablePort(groups));
        String addressUDP = "230.0.0.1";
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
                if (inputLine.startsWith(Protocol.Client.Auth.LOGIN)) {
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
                            out.println(makeJsonResponse(Protocol.Server.Auth.LOGIN_SUCCESS, loggedUser.getUsername()).toJSONString());
                        } else {
                            out.println(makeJsonResponse(Protocol.Server.Auth.LOGIN_ERROR, Protocol.Server.Auth.Error.USER_PASS).toJSONString());
                        }
                    } else {
                        out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                    }

                } else if (inputLine.startsWith(Protocol.Client.Auth.REGIST)) {

                    String[] input = inputLine.split(" ");
                    if (input.length == 4) {
                        boolean exists = false;
                        if (input[1].matches("^[a-zA-Z0-9]{1,20}$") && input[2].matches("^[a-zA-Z0-9]{1,20}$")) {
                            synchronized (users) {
                                for (User user : users) {
                                    if (user.getUsername().equals(input[1])) {
                                        exists = true;
                                        break;
                                    }
                                }
                            }
                            if (!exists) {
                                if (input[2].equals(input[3])) {
                                    users.add(new User(input[1], input[2]));
                                    new WriteUsers(userSemaphore, users).start();
                                    out.println(makeJsonResponse(Protocol.Server.Auth.REGIST_SUCCESS, input[1]).toJSONString());
                                } else {
                                    out.println(makeJsonResponse(Protocol.Server.Auth.REGIST_ERROR, Protocol.Server.Auth.Error.PASS_MATCH).toJSONString());
                                }
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Auth.REGIST_ERROR, Protocol.Server.Auth.Error.USER_EXISTS).toJSONString());
                            }
                        } else {
                            out.println(makeJsonResponse(Protocol.Server.Auth.REGIST_ERROR, Protocol.Server.Auth.Error.REGEX).toJSONString());
                        }
                    } else {
                        out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                    }

                } else if (inputLine.equals(Protocol.Client.Auth.EXIT)) {
                    bye = true;
                    out.println(makeJsonResponse(Protocol.Server.Auth.EXIT, Protocol.Server.Auth.EXIT).toJSONString());
                    break;
                } else {
                    out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                }
            }
            if (!bye) {
                while ((inputLine = in.readLine()) != null) { //while principal
                    boolean leave = false;
                    JSONObject response;
                    if (inputLine.startsWith(Protocol.Client.Private.SEND_MSG)) {
                        String[] input = inputLine.split(" ", 3);
                        if (input.length == 3) {
                            User receiver = null;
                            synchronized (users) {
                                for (User user : users) {
                                    if (user.getId() == Integer.valueOf(input[1])) {
                                        receiver = user;
                                        break;
                                    }
                                }
                            }
                            if (receiver != null && receiver.getSocket() != null) { //se user existe e se tem socket(se tem login)
                                PrintWriter outReceiver = new PrintWriter(receiver.getSocket().getOutputStream(), true);
                                Message msg = new Message(loggedUser.getUsername(), LocalDateTime.now(), input[2]);
                                response = makeJsonResponse(Protocol.Server.Private.RECEIVE_MSG, msg.toString());
                                outReceiver.println(response.toJSONString());
                                receiver.addMessage(loggedUser, msg);
                                loggedUser.addMessage(loggedUser, msg);
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Private.SEND_ERROR, Protocol.Server.Private.Error.USER).toJSONString());
                            }

                        } else {
                            response = makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND);
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Private.SEND_FILE)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 5) {
                            User receiver = null;
                            synchronized (users) {
                                for (User user : users) {
                                    if (user.getId() == Integer.valueOf(input[1])) {
                                        receiver = user;
                                        break;
                                    }
                                }
                            }
                            if (receiver != null && receiver.getSocket() != null) { //USER EXIST E TEM LOGIN
                                int port = GetPort.getFreeAvaliablePort(groups);
                                new ReceiveFileServer(port, input[2], Integer.parseInt(input[3]), receiver, loggedUser).start();
                                JSONObject object = new JSONObject();
                                object.put("port", port);
                                object.put("address", socket.getInetAddress().getHostAddress());
                                object.put("path", input[4]);
                                out.println(makeJsonResponse(Protocol.Server.Private.SEND_FILE, object.toJSONString()));
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Private.SEND_ERROR, Protocol.Server.Private.Error.USER).toJSONString());
                            }
                        } else {
                            response = makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND);
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Group.SEND_FILE)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 5) {
                            Group group = null;
                            synchronized (loggedUser.getGroups()) {
                                for (Group x : loggedUser.getGroups()) { //ver se fez join ao grupo
                                    if (x.getId() == Integer.valueOf(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se grupo existe
                                int port = GetPort.getFreeAvaliablePort(groups);
                                new ReceiveFileServer(port, input[2], Integer.parseInt(input[3]), group, loggedUser, socketUDP).start();
                                JSONObject object = new JSONObject();
                                object.put("port", port);
                                object.put("address", socket.getInetAddress().getHostAddress());
                                object.put("path", input[4]);
                                out.println(makeJsonResponse(Protocol.Server.Group.SEND_FILE, object.toJSONString()));
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Group.SEND_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS).toJSONString());
                            }
                        } else {
                            response = makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND);
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Private.RECEIVE_FILE)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            int port = GetPort.getFreeAvaliablePort(groups);
                            String path = "files/private/" + loggedUser.getUsername() + "/" + input[1];
                            File file = new File(path);
                            if (file.exists()) {
                                new SendFileServer(port, path).start();
                                JSONObject object = new JSONObject();
                                object.put("port", port);
                                object.put("address", socket.getInetAddress().getHostAddress());
                                object.put("name", input[1]);
                                object.put("size", file.length());
                                out.println(makeJsonResponse(Protocol.Client.Private.RECEIVE_FILE, object.toJSONString()));
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Private.FILE_ERROR, Protocol.Server.Private.Error.FILE).toJSONString());
                            }
                        } else {
                            response = makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND);
                            out.println(response.toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Group.RECEIVE_FILE)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 3) {
                            int port = GetPort.getFreeAvaliablePort(groups);
                            String path = "files/groups/" + input[1] + "/" + input[2];
                            File file = new File(path);
                            if (file.exists()) {
                                new SendFileServer(port, path).start();
                                JSONObject object = new JSONObject();
                                object.put("port", port);
                                object.put("address", socket.getInetAddress().getHostAddress());
                                object.put("name", input[2]);
                                object.put("size", file.length());
                                out.println(makeJsonResponse(Protocol.Server.Group.RECEIVE_FILE, object.toJSONString()));
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Group.FILE_ERROR, Protocol.Server.Group.Error.FILE).toJSONString());
                            }
                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }

                    } else if (inputLine.startsWith(Protocol.Client.Group.ADD)) {
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

                                Group group = new Group(GetPort.getFreeAvaliablePort(groups), input[1], addressUDP);
                                groups.add(group);
                                group.setServerPort(GetPort.getFreeAvaliablePort(groups)); //para nao retornar a mesma porta que em cima //FAZER DEBUG PARA VER
                                //SE É NECESSARIO O -1 NO CONSTRUTOR
                                new WriteGroups(groupSemaphore, groups).start();
//                                new MulticastServerThread(address, group.getServerPort(), group.getPort()).start();
                                response = makeJsonResponse(Protocol.Server.Group.ADD_SUCCESS, input[1]);
                                out.println(response.toJSONString());

                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Group.ADD_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS).toJSONString());
                            }

                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }

                    } else if (inputLine.startsWith(Protocol.Client.Group.JOIN)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getId() == Integer.valueOf(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se group existe
                                loggedUser.addGroup(group);
                                group.addUser(loggedUser);

                                response = makeJsonResponse(Protocol.Server.Group.JOIN_SUCCESS, group.toString());
                                out.println(response.toJSONString());
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Group.JOIN_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS).toJSONString());
                            }
                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Group.EDIT)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 3) {
                            Group group = null;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getId() == Integer.valueOf(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se group existe
                                group.setName(input[2]);

                                response = makeJsonResponse(Protocol.Server.Group.EDIT_SUCCESS, group.toString());
                                out.println(response.toJSONString());
                            } else {
                                out.println(makeJsonResponse(Protocol.Server.Group.EDIT_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS).toJSONString());

                            }
                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Group.REMOVE)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (groups) {
                                for (Group x : groups) {
                                    if (x.getId() == Integer.valueOf(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se group existe

                                if (!group.hasUsers()) {
                                    //remover o grupo
                                    groups.remove(group);
                                } else {
                                    response = makeJsonResponse(Protocol.Server.Group.REMOVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EMPTY);
                                    out.println(response.toJSONString());
                                }
                            } else {
                                response = makeJsonResponse(Protocol.Server.Group.REMOVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EMPTY);
                                out.println(response.toJSONString());
                            }

                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }

                    } else if (inputLine.startsWith(Protocol.Client.Group.LEAVE)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (loggedUser.getGroups()) {
                                for (Group x : loggedUser.getGroups()) { //ver se fez join ao grupo
                                    if (x.getId() == Integer.valueOf(input[1])) {
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
                                String res = makeJsonResponse(Protocol.Server.Group.LEAVE_SUCCESS, loggedUser.getUsername()).toJSONString();
                                buf = res.getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                                socketUDP.send(packet);
                            } else {
                                response = makeJsonResponse(Protocol.Server.Group.LEAVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS);
                                out.println(response.toJSONString());
                            }
                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }

                    } else if (inputLine.startsWith(Protocol.Client.Group.SEND_MSG)) {
                        String[] input = inputLine.split(" ", 3);
                        if (input.length == 3) {
                            Group group = null;
                            synchronized (loggedUser.getGroups()) {
                                for (Group x : loggedUser.getGroups()) { //ver se fez join ao grupo
                                    if (x.getId() == Integer.valueOf(input[1])) {
                                        group = x;
                                        break;
                                    }
                                }
                            }
                            if (group != null) { //se grupo existe

                                byte[] buf = new byte[256];
                                Message msg = new Message(loggedUser.getUsername(), LocalDateTime.now(), input[2]);
                                String res = makeJsonResponse(Protocol.Server.Group.SEND_MSG, msg.toString()).toJSONString();
                                buf = res.getBytes();
                                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                                socketUDP.send(packet);
                                group.addMessage(msg);
                            } else {
                                response = makeJsonResponse(Protocol.Server.Group.SEND_ERROR, Protocol.Server.Group.Error.GROUP_NOT_JOINED);
                                out.println(response.toJSONString());
                            }
                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }
                    } else if (inputLine.startsWith(Protocol.Client.Group.LIST_GROUPS)) {

                        JSONArray list = new JSONArray();
                        synchronized (groups) {
                            for (Group group : groups) {
                                list.add(group.toString());
                            }

                        }
                        out.println(makeJsonResponse(Protocol.Server.Group.LIST_GROUPS, list.toJSONString()).toJSONString());

                    } else if (inputLine.startsWith(Protocol.Client.Private.LIST_LOGGED_USERS)) {

                        JSONArray list = new JSONArray();
                        synchronized (users) {
                            for (User user : users) {
                                if (user.getSocket() != null) { //se tem login
                                    list.add(user.toString());
                                }

                            }

                        }
                        out.println(makeJsonResponse(Protocol.Server.Private.LIST_LOGGED_USERS, list.toJSONString()).toJSONString());

                    } else if (inputLine.startsWith(Protocol.Client.Group.LIST_GROUP_MSGS)) {
                        String[] input = inputLine.split(" ");
                        if (input.length == 2) {
                            Group group = null;
                            synchronized (loggedUser.getGroups()) {
                                for (Group x : loggedUser.getGroups()) { //ver se fez join ao grupo
                                    if (x.getId() == Integer.valueOf(input[1])) {
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
                                response = makeJsonResponse(Protocol.Server.Group.LIST_GROUP_MSGS, list.toJSONString());
                                out.println(response.toJSONString());
                            } else {
                                response = makeJsonResponse(Protocol.Server.Group.LIST_MSGS_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS);
                                out.println(response.toJSONString());
                            }

//                            out.println("groupadded " + input[1]);
                        } else {
                            out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
                        }
                    } else if (inputLine.equals(Protocol.Client.Auth.LOGOUT)) {
                        loggedUser.setSocket(null);
                        break;
                    } else {
                        out.println(makeJsonResponse(Protocol.ERROR, Protocol.BAD_COMMAND).toJSONString());
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
