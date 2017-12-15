/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Semaphore;
import model.GroupServer;
import model.MessageServer;
import model.PrivateChatServer;
import model.UserServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.files.WriteGroups;
import server.files.WriteUsers;
import utils.GetPort;
import utils.Protocol;

public class MultiClientThread extends Thread {

    private final Socket socket;
    private final List<GroupServer> groups;
    private final List<UserServer> users;
    private UserServer loggedUser;
    private final Semaphore userSemaphore;
    private final Semaphore groupSemaphore;
    private final DatagramSocket socketUDP;
    private final String addressUDP;
    private final PrintWriter out;

    public MultiClientThread(Socket socket, List<GroupServer> groups, List<UserServer> users, Semaphore userSemaphore, Semaphore groupSemaphore) throws IOException {
        super("MultiClientThread");
        this.socket = socket;
        this.groups = groups;
        this.users = users;
        this.userSemaphore = userSemaphore;
        this.groupSemaphore = groupSemaphore;
        this.loggedUser = null;
        this.socketUDP = new DatagramSocket(GetPort.getFreeAvaliablePort(groups));
        addressUDP = "230.0.0.1";
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            System.out.println("Utilizador tentou autenticar");
            boolean bye = false;
            while (loggedUser == null && !bye && (inputLine = in.readLine()) != null) {
                JSONObject response = Protocol.parseJSONResponse(inputLine);
                String command = response.get(Protocol.COMMAND).toString();
                String data = response.get(Protocol.DATA).toString();
                switch (command) {

                    case Protocol.Client.Auth.LOGIN:
                        login(data);
                        break;

                    case Protocol.Client.Auth.REGIST:
                        regist(data);
                        break;

                    case Protocol.Client.Auth.EXIT:
                        bye = true;
                        out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.EXIT, Protocol.Server.Auth.EXIT));
                        break;
                    default:
                        badCommand();

                }
            }
            if (!bye && loggedUser != null) { //se fez login
                System.out.println("Utilizador " + loggedUser.getUsername() + " autenticado");
                while ((inputLine = in.readLine()) != null) { //while principal

                    JSONObject response = Protocol.parseJSONResponse(inputLine);
                    String command = response.get(Protocol.COMMAND).toString();
                    String data = response.get(Protocol.DATA).toString();

                    if (command.equals(Protocol.Client.Private.SEND_MSG)) {
                        sendPrivateMsg(data);

                    } else if (inputLine.startsWith(Protocol.Client.Private.SEND_FILE)) {

                        sendPrivateFile(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Private.REMOVE_PRIVATE_CHAT)) {

                        removePrivateChat(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.SEND_FILE)) {

                        sendGroupFile(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Private.RECEIVE_FILE)) {

                        receivePrivateFile(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.RECEIVE_FILE)) {

                        receiveGroupFile(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.ADD)) {

                        addGroup(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.JOIN)) {

                        joinGroup(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.EDIT)) {

                        editGroup(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.REMOVE)) {

                        removeGroup(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.LEAVE)) {

                        leaveGroup(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.SEND_MSG)) {

                        sendGroupMsg(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.LIST_GROUPS)) {

                        listGroups();

                    } else if (inputLine.startsWith(Protocol.Client.Private.LIST_LOGGED_USERS)) {

                        listLoggedUsers();

                    } else if (command.equals(Protocol.Client.Group.LIST_JOINED_GROUPS)) {

                        listJoinedGroups();

                    } else if (command.equals(Protocol.Client.Private.LIST_PRIVATE_CHAT)) {

                        listPrivateChats();

                    } else if (inputLine.startsWith(Protocol.Client.Private.LIST_PRIVATE_MSGS)) {

                        listPrivateMsgs(inputLine);

                    } else if (inputLine.startsWith(Protocol.Client.Group.LIST_GROUP_MSGS)) {

                        listGroupMsgs(inputLine);

                    } else if (inputLine.equals(Protocol.Client.Auth.LOGOUT)) {
                        loggedUser.setSocket(null);
                        System.out.println("Utilizador fez logout");
                        break;
                    } else {
                        badCommand();
                    }
                }

            }

            if (loggedUser != null) {
                loggedUser.setSocket(null);

                System.out.println("Utilizador " + loggedUser.getUsername() + " fez logout");
                socketUDP.close();
                socket.close();
                out.close();
                in.close();
                new WriteUsers(userSemaphore, users).start();
                System.out.println("Utilizador " + loggedUser.getUsername() + " desligou-se!");

            } else {
                System.out.println("Utilizador saiu sem fazer login");
            }

        } catch (IOException e) {
            if (loggedUser != null) {
                loggedUser.setSocket(null);
                System.out.println("Utilizador " + loggedUser.getUsername() + " fez logout");
                System.out.println("Utilizador " + loggedUser.getUsername() + " desligou-se!");
                new WriteUsers(userSemaphore, users).start();
            } else {
                System.out.println("Utilizador saiu sem fazer login");
            }

        }
    }

    //AUTH
    private void login(String dataString) {
        JSONObject data = Protocol.parseJSONResponse(dataString);
        if (data.size() == 2) {
            synchronized (users) {
                for (UserServer user : users) {
                    if (user.getUsername().equals(data.get("username").toString()) && user.getSocket() == null) {
                        if (user.checkPassword(data.get("password").toString())) {
                            loggedUser = user;
                            loggedUser.setSocket(socket);
                        }
                        break;
                    }
                }
            }

            if (loggedUser != null) {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.LOGIN_SUCCESS, loggedUser.toString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.LOGIN_ERROR, Protocol.Server.Auth.Error.USER_PASS));
            }
        } else {
            badCommand();
        }

    }

    private void regist(String dataString) {
        JSONObject data = Protocol.parseJSONResponse(dataString);
        if (data.size() == 3) {
            boolean exists = false;
            if (data.get("username").toString().matches("^[a-zA-Z0-9]{1,20}$") && data.get("password1").toString().matches("^[a-zA-Z0-9]{1,20}$")) {
                synchronized (users) {
                    for (UserServer user : users) {
                        if (user.getUsername().equals(data.get("username").toString())) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    if (data.get("password1").toString().equals(data.get("password2").toString())) {
                        users.add(new UserServer(data.get("username").toString(), data.get("password1").toString()));
                        new WriteUsers(userSemaphore, users).start();
                        out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.REGIST_SUCCESS, data.get("username").toString()));
                    } else {
                        out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.REGIST_ERROR, Protocol.Server.Auth.Error.PASS_MATCH));
                    }
                } else {
                    out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.REGIST_ERROR, Protocol.Server.Auth.Error.USER_EXISTS));
                }
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Auth.REGIST_ERROR, Protocol.Server.Auth.Error.REGEX));
            }
        } else {
            badCommand();
        }
    }

    private void badCommand() {
        out.println(Protocol.makeJSONResponse(Protocol.ERROR, Protocol.BAD_COMMAND));
    }

    //PRIVATE
    private void sendPrivateMsg(String dataString) throws IOException {
        JSONObject data = Protocol.parseJSONResponse(dataString);
        if (data.size() == 2) {
            UserServer receiver = null;
            synchronized (users) {
                for (UserServer user : users) {
                    if (user.getId() == Integer.valueOf(data.get("id").toString())) {
                        receiver = user;
                        break;
                    }
                }
            }
            if (receiver != null && receiver.getSocket() != null) { //se user existe e se tem socket(se tem login)
                PrintWriter outReceiver = new PrintWriter(receiver.getSocket().getOutputStream(), true);
                MessageServer msg = new MessageServer(loggedUser.getId(), loggedUser.getUsername(), LocalDateTime.now(), data.get("msg").toString(), Integer.valueOf(data.get("id").toString()), false);
                outReceiver.println(Protocol.makeJSONResponse(Protocol.Server.Private.RECEIVE_MSG, msg.toString()));
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.RECEIVE_MSG, msg.toString()));
                receiver.addMessage(loggedUser, msg);
                loggedUser.addMessage(receiver, msg);
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.SEND_ERROR, Protocol.Server.Private.Error.USER));
            }
        } else {
            badCommand();
        }
    }

    private void sendPrivateFile(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 5) {
            UserServer receiver = null;
            synchronized (users) {
                for (UserServer user : users) {
                    if (user.getId() == Integer.valueOf(input[1])) {
                        receiver = user;
                        break;
                    }
                }
            }
            if (receiver != null && receiver.getSocket() != null) { //USER EXIST E TEM LOGIN
                int port = GetPort.getFreeAvaliablePort(groups);
                new ReceiveFile(port, input[2], Integer.parseInt(input[3]), receiver, loggedUser).start();
                JSONObject object = new JSONObject();
                object.put("port", port);
                object.put("address", socket.getInetAddress().getHostAddress());
                object.put("path", input[4]);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.SEND_FILE, object.toJSONString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.SEND_ERROR, Protocol.Server.Private.Error.USER));
            }
        } else {
            badCommand();
        }

    }

    private void receivePrivateFile(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            int port = GetPort.getFreeAvaliablePort(groups);
            String path = "files/private/" + loggedUser.getUsername() + "/" + input[1];
            File file = new File(path);
            if (file.exists()) {
                new SendFile(port, path).start();
                JSONObject object = new JSONObject();
                object.put("port", port);
                object.put("address", socket.getInetAddress().getHostAddress());
                object.put("name", input[1]);
                object.put("size", file.length());
                out.println(Protocol.makeJSONResponse(Protocol.Client.Private.RECEIVE_FILE, object.toJSONString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.FILE_ERROR, Protocol.Server.Private.Error.FILE));
            }
        } else {
            badCommand();
        }
    }

    private void removePrivateChat(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            UserServer receiver = null;
            synchronized (users) {
                for (UserServer user : users) {
                    if (user.getId() == Integer.valueOf(input[1])) {
                        receiver = user;
                        break;
                    }
                }
            }
            if (receiver != null) {
                loggedUser.removePrivateChat(receiver);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.REMOVE_PRIVATE_CHAT_SUCCESS, receiver.getUsername()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Private.REMOVE_PRIVATE_CHAT_ERROR, Protocol.Server.Private.Error.USER));
            }
        } else {
            badCommand();
        }

    }

    private void listPrivateChats() {
        JSONArray list = new JSONArray();
        synchronized (loggedUser.getPrivateChat()) {
            for (PrivateChatServer chat : loggedUser.getPrivateChat()) {
                list.add(chat.getUser().toString());
            }
        }
        out.println(Protocol.makeJSONResponse(Protocol.Server.Private.LIST_PRIVATE_CHAT, list.toJSONString()));

    }

    private void listLoggedUsers() {
        JSONArray list = new JSONArray();
        synchronized (users) {
            for (UserServer user : users) {
                if (user.getSocket() != null) { //se tem login
                    list.add(user.toString());
                }
            }
        }
        out.println(Protocol.makeJSONResponse(Protocol.Server.Private.LIST_LOGGED_USERS, list.toJSONString()));
    }

    private void listPrivateMsgs(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            JSONArray list = new JSONArray();
            synchronized (loggedUser.getPrivateChat()) {
                for (PrivateChatServer chat : loggedUser.getPrivateChat()) {
                    if (chat.getUser().getId() == Integer.valueOf(input[1])) {
                        for (MessageServer msg : chat.getMessages()) {
                            list.add(msg);
                            break;
                        }

                    }
                }
            }
            out.println(Protocol.makeJSONResponse(Protocol.Server.Private.LIST_PRIVATE_MSGS, list.toJSONString()));
        } else {
            badCommand();
        }
    }

    //GROUP
    private void sendGroupMsg(String dataString) throws IOException {
        String[] input = dataString.split(" ", 3);
        if (input.length == 3) {
            GroupServer group = null;
            synchronized (loggedUser.getGroups()) {
                for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
                    if (x.getId() == Integer.valueOf(input[1])) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se grupo existe

                byte[] buf = new byte[256];
                MessageServer msg = new MessageServer(loggedUser.getId(), loggedUser.getUsername(), LocalDateTime.now(), input[2], Integer.valueOf(input[1]), false);
                String res = Protocol.makeJSONResponse(Protocol.Server.Group.SEND_MSG, msg.toString());
                buf = res.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                socketUDP.send(packet);
                group.addMessage(msg);
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.SEND_ERROR, Protocol.Server.Group.Error.GROUP_NOT_JOINED));
            }
        } else {
            badCommand();
        }

    }

    private void sendGroupFile(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 5) {
            GroupServer group = null;
            synchronized (loggedUser.getGroups()) {
                for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
                    if (x.getId() == Integer.valueOf(input[1])) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se grupo existe
                int port = GetPort.getFreeAvaliablePort(groups);
                new ReceiveFile(port, input[2], Integer.parseInt(input[3]), group, loggedUser, socketUDP).start();
                JSONObject object = new JSONObject();
                object.put("port", port);
                object.put("address", socket.getInetAddress().getHostAddress());
                object.put("path", input[4]);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.SEND_FILE, object.toJSONString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.SEND_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS));
            }
        } else {
            badCommand();
        }

    }

    private void receiveGroupFile(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 3) {
            int port = GetPort.getFreeAvaliablePort(groups);
            String path = "files/groups/" + input[1] + "/" + input[2];
            File file = new File(path);
            if (file.exists()) {
                new SendFile(port, path).start();
                JSONObject object = new JSONObject();
                object.put("port", port);
                object.put("address", socket.getInetAddress().getHostAddress());
                object.put("name", input[2]);
                object.put("size", file.length());
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.RECEIVE_FILE, object.toJSONString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.FILE_ERROR, Protocol.Server.Group.Error.FILE));
            }
        } else {
            badCommand();
        }

    }

    private void addGroup(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            //find if group name exists
            boolean exists = false;
            synchronized (groups) {
                for (GroupServer x : groups) {
                    if (x.getName().equals(input[1])) {
                        exists = true;
                        break;
                    }
                }
            }
            if (!exists) {
                // create group

                GroupServer group = new GroupServer(GetPort.getFreeAvaliablePort(groups), input[1], addressUDP);
                groups.add(group);
                group.setServerPort(GetPort.getFreeAvaliablePort(groups)); //para nao retornar a mesma porta que em cima //FAZER DEBUG PARA VER
                //SE É NECESSARIO O -1 NO CONSTRUTOR
                new WriteGroups(groupSemaphore, groups).start();
//                                new MulticastServerThread(address, group.getServerPort(), group.getPort()).start();

                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.ADD_SUCCESS, input[1]));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.ADD_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS));
            }
        } else {
            badCommand();
        }

    }

    private void joinGroup(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            GroupServer group = null;
            synchronized (groups) {
                for (GroupServer x : groups) {
                    if (x.getId() == Integer.valueOf(input[1])) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se group existe
                loggedUser.addGroup(group);
                group.addUser(loggedUser);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.JOIN_SUCCESS, group.toString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.JOIN_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS));
            }
        } else {
            badCommand();
        }
    }

    private void editGroup(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 3) {
            GroupServer group = null;
            synchronized (groups) {
                for (GroupServer x : groups) {
                    if (x.getId() == Integer.valueOf(input[1])) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se group existe
                group.setName(input[2]);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.EDIT_SUCCESS, group.toString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.EDIT_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS));

            }
        } else {
            badCommand();
        }
    }

    private void removeGroup(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            GroupServer group = null;
            synchronized (groups) {
                for (GroupServer x : groups) {
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
                    out.println(Protocol.makeJSONResponse(Protocol.Server.Group.REMOVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EMPTY));
                }
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.REMOVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EMPTY));
            }

        } else {
            badCommand();
        }

    }

    private void leaveGroup(String dataString) throws IOException {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            GroupServer group = null;
            synchronized (loggedUser.getGroups()) {
                for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
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
                String res = Protocol.makeJSONResponse(Protocol.Server.Group.LEAVE_SUCCESS, loggedUser.getUsername());
                buf = res.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                socketUDP.send(packet);
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.LEAVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS));
            }
        } else {
            badCommand();
        }

    }

    private void listGroups() {
        JSONArray list = new JSONArray();
        synchronized (groups) {
            for (GroupServer group : groups) {
                if (!group.getUsers().contains(loggedUser)) {
                    list.add(group.toString());
                }
            }
        }
        out.println(Protocol.makeJSONResponse(Protocol.Server.Group.LIST_GROUPS, list.toJSONString()));
    }

    private void listJoinedGroups() {
        JSONArray list = new JSONArray();
        synchronized (loggedUser.getGroups()) {
            for (GroupServer group : loggedUser.getGroups()) {
                list.add(group.toString());
            }

        }
        out.println(Protocol.makeJSONResponse(Protocol.Server.Group.LIST_JOINED_GROUPS, list.toJSONString()));

    }

    private void listGroupMsgs(String dataString) {
        String[] input = dataString.split(" ");
        if (input.length == 2) {
            GroupServer group = null;
            synchronized (loggedUser.getGroups()) {
                for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
                    if (x.getId() == Integer.valueOf(input[1])) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se fez join
                List<MessageServer> msgs = group.getMessages();
                JSONArray list = new JSONArray();
                synchronized (msgs) {
                    for (MessageServer msg : msgs) {
                        list.add(msg.toString());
                    }
                }
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.LIST_GROUP_MSGS, list.toJSONString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.LIST_MSGS_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS));
            }

        } else {
            badCommand();
        }

    }

}
