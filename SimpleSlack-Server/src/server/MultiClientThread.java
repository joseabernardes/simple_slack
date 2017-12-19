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
import javafx.application.Platform;
import model.GroupServer;
import model.MessageServer;
import model.PrivateChatServer;
import model.UserServer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

                    } else if (command.equals(Protocol.Client.Private.LIST_LOGGED_USERS)) {

                        listLoggedUsers();

                    } else if (command.equals(Protocol.Client.Private.SEND_FILE)) {

                        sendPrivateFile(data);

                    } else if (command.equals(Protocol.Client.Private.REMOVE_PRIVATE_CHAT)) {

                        removePrivateChat(data);

                    } else if (inputLine.startsWith(Protocol.Client.Group.SEND_FILE)) {

                        sendGroupFile(inputLine);

                    } else if (command.equals(Protocol.Client.Private.RECEIVE_FILE)) {

                        receivePrivateFile(data);

                    } else if (inputLine.startsWith(Protocol.Client.Group.RECEIVE_FILE)) {

                        receiveGroupFile(inputLine);

                    } else if (command.equals(Protocol.Client.Group.ADD)) {

                        addGroup(data);

                    } else if (command.equals(Protocol.Client.Group.JOIN)) {

                        joinGroup(data);

                    } else if (command.equals(Protocol.Client.Group.EDIT)) {

                        editGroup(data);

                    } else if (command.equals(Protocol.Client.Group.REMOVE)) {

                        removeGroup(data);

                    } else if (command.equals(Protocol.Client.Group.LEAVE)) {

                        leaveGroup(data);

                    } else if (command.equals(Protocol.Client.Group.SEND_MSG)) {

                        sendGroupMsg(data);

                    } else if (command.equals(Protocol.Client.Group.LIST_GROUPS)) {

                        listGroups();

                    } else if (command.equals(Protocol.Client.Group.LIST_JOINED_GROUPS)) {

                        listJoinedGroups();

                    } else if (command.equals(Protocol.Client.Private.LIST_PRIVATE_CHAT)) {

                        listPrivateChats();

                    } else if (command.equals(Protocol.Client.Private.LIST_PRIVATE_MSGS)) {

                        listPrivateMsgs(data);

                    } else if (command.equals(Protocol.Client.Group.LIST_GROUP_MSGS)) {

                        listGroupMsgs(data);

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
                System.out.println("Utilizador " + loggedUser.getUsername() + " desligou-se!");

            } else {
                System.out.println("Utilizador saiu sem fazer login");
            }

        } catch (IOException e) {
            if (loggedUser != null) {
                loggedUser.setSocket(null);
                System.out.println("Utilizador " + loggedUser.getUsername() + " fez logout");
                System.out.println("Utilizador " + loggedUser.getUsername() + " desligou-se!");
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
                MessageServer msg = new MessageServer(loggedUser.getId(), loggedUser.getUsername(), LocalDateTime.now(), data.get("msg").toString(), Integer.valueOf(data.get("id").toString()));
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

    /*
    id
    file_name
    size
    path
     */
    private void sendPrivateFile(String dataString) {
        JSONObject data = Protocol.parseJSONResponse(dataString);
        UserServer receiver = null;
        synchronized (users) {
            for (UserServer user : users) {
                if (user.getId() == Integer.valueOf(data.get("id").toString())) {
                    receiver = user;
                    break;
                }
            }
        }
        if (receiver != null && receiver.getSocket() != null) { //USER EXIST E TEM LOGIN
            int port = GetPort.getFreeAvaliablePort(groups);
            new ReceiveFile(port, data.get("file_name").toString(), Integer.parseInt(data.get("size").toString()), receiver, loggedUser).start();
            JSONObject object = new JSONObject();
            object.put("port", port);
            object.put("address", socket.getInetAddress().getHostAddress());
            object.put("path", data.get("path").toString());
            out.println(Protocol.makeJSONResponse(Protocol.Server.Private.SEND_FILE, object.toJSONString()));
        } else {
            out.println(Protocol.makeJSONResponse(Protocol.Server.Private.SEND_ERROR, Protocol.Server.Private.Error.USER));
        }

    }

    /*
    file_name
    path
     */
    private void receivePrivateFile(String dataString) {
        JSONObject data = Protocol.parseJSONResponse(dataString);

        int port = GetPort.getFreeAvaliablePort(groups);
        String path = "files/private/" + loggedUser.getUsername() + "/" + data.get("file_name").toString();
        File file = new File(path);
        if (file.exists()) {
            new SendFile(port, path).start();
            JSONObject object = new JSONObject();
            object.put("port", port);
            object.put("address", socket.getInetAddress().getHostAddress());
            object.put("name", data.get("file_name").toString());
            object.put("size", file.length());
            object.put("path", data.get("path").toString());
            out.println(Protocol.makeJSONResponse(Protocol.Client.Private.RECEIVE_FILE, object.toJSONString()));
        } else {
            out.println(Protocol.makeJSONResponse(Protocol.Server.Private.FILE_ERROR, Protocol.Server.Private.Error.FILE));
        }
    }

    private void removePrivateChat(String dataString) {
        UserServer receiver = null;
        synchronized (users) {
            for (UserServer user : users) {
                if (user.getId() == Integer.valueOf(dataString)) {
                    receiver = user;
                    break;
                }
            }
        }
        if (receiver != null) {
            loggedUser.removePrivateChat(receiver);
            out.println(Protocol.makeJSONResponse(Protocol.Server.Private.REMOVE_PRIVATE_CHAT_SUCCESS, receiver.toString()));
        } else {
            out.println(Protocol.makeJSONResponse(Protocol.Server.Private.REMOVE_PRIVATE_CHAT_ERROR, Protocol.Server.Private.Error.USER));
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
                //se tem login  && se não tem já conversa privada && se não sou eu proprio
                if (user.getSocket() != null && !loggedUser.getPrivateChat().contains(new PrivateChatServer(user)) && !user.equals(loggedUser)) {
                    list.add(user.toString());
                }
            }
        }
        out.println(Protocol.makeJSONResponse(Protocol.Server.Private.LIST_LOGGED_USERS, list.toJSONString()));
    }

    private void listPrivateMsgs(String dataString) {
        JSONArray list = new JSONArray();

        synchronized (loggedUser.getPrivateChat()) {
            for (PrivateChatServer chat : loggedUser.getPrivateChat()) {
                if (chat.getUser().getId() == Integer.valueOf(dataString)) {
                    for (MessageServer msg : chat.getMessages()) {
                        list.add(msg);
                    }

                }
            }
        }
        JSONObject obj = new JSONObject();
        obj.put("id", dataString);
        obj.put("messages", list.toJSONString());
        out.println(Protocol.makeJSONResponse(Protocol.Server.Private.LIST_PRIVATE_MSGS, obj));

    }

    //GROUP
    private void sendGroupMsg(String dataString) throws IOException {;
        JSONObject data = Protocol.parseJSONResponse(dataString);
        if (data.size() == 2) {
            GroupServer group = null;
            synchronized (loggedUser.getGroups()) {
                for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
                    if (x.getId() == Integer.valueOf(data.get("id").toString())) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se grupo existe
                byte[] buf = new byte[256];
                MessageServer msg = new MessageServer(loggedUser.getId(), loggedUser.getUsername(), LocalDateTime.now(), data.get("msg").toString(), Integer.valueOf(data.get("id").toString()));
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
        boolean exists = false;
        synchronized (groups) {
            for (GroupServer x : groups) {
                if (x.getName().equals(dataString)) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            // create group

            GroupServer group = new GroupServer(GetPort.getFreeAvaliablePort(groups), dataString, addressUDP);
            groups.add(group);
//            group.setServerPort(GetPort.getFreeAvaliablePort(groups)); //para nao retornar a mesma porta que em cima //FAZER DEBUG PARA VER

//                                new MulticastServerThread(address, group.getServerPort(), group.getPort()).start();
            out.println(Protocol.makeJSONResponse(Protocol.Server.Group.ADD_SUCCESS, group.toString()));
        } else {
            out.println(Protocol.makeJSONResponse(Protocol.Server.Group.ADD_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS));
        }

    }

    private void joinGroup(String dataString) {
        GroupServer group = null;
        synchronized (groups) {
            for (GroupServer x : groups) {
                if (x.getId() == Integer.valueOf(dataString)) {
                    group = x;
                    break;
                }
            }
            if (group != null) { //se group existe
                loggedUser.addGroup(group);
                group.addUser(loggedUser);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.JOIN_SUCCESS, group.toString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.JOIN_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS));
            }
        }
    }

    private void editGroup(String dataString) throws IOException{
        JSONObject data = Protocol.parseJSONResponse(dataString);
            GroupServer group = null;
            synchronized (groups) {
                for (GroupServer x : groups) {
                    if (x.getId() == Integer.valueOf(data.get("id").toString())) {
                        group = x;
                        break;
                    }
                }
            }
            if (group != null) { //se group existe
                group.setName(data.get("nome").toString());
                byte[] buf = new byte[256];
                String res = Protocol.makeJSONResponse(Protocol.Server.Group.EDIT_SUCCESS, group.toString());
                buf = res.getBytes();
                DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
                socketUDP.send(packet);
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.EDIT_ERROR, Protocol.Server.Group.Error.GROUP_EXISTS));

            }
    }
    
    private void removeGroup(String dataString) {
        GroupServer group = null;
        synchronized (groups) {
            for (GroupServer x : groups) {
                if (x.getId() == Integer.valueOf(dataString)) {
                    group = x;
                    break;
                }
            }
        }
        if (group != null) { //se group existe

            //LEAVE GROUP
            if (group.size() == 1) {
                //remover o grupo
                groups.remove(group);
                loggedUser.removeGroup(group);
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.REMOVE_SUCESS, group.toString()));
            } else {
                out.println(Protocol.makeJSONResponse(Protocol.Server.Group.REMOVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EMPTY));
            }
        } else {
            out.println(Protocol.makeJSONResponse(Protocol.Server.Group.REMOVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS));
        }

    }

    private void leaveGroup(String dataString) throws IOException {
        GroupServer group = null;
        synchronized (loggedUser.getGroups()) {
            for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
                if (x.getId() == Integer.valueOf(dataString)) {
                    group = x;
                    break;
                }
            }
        }
        if (group != null) { //se group existe
            loggedUser.removeGroup(group);
            group.removeUser(loggedUser);
            byte[] buf = new byte[256];
            String res = Protocol.makeJSONResponse(Protocol.Server.Group.LEAVE_SUCCESS, loggedUser.getUsername());
            buf = res.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
            socketUDP.send(packet);
        } else {
            out.println(Protocol.makeJSONResponse(Protocol.Server.Group.LEAVE_ERROR, Protocol.Server.Group.Error.GROUP_NOT_EXISTS));
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
        GroupServer group = null;
        synchronized (loggedUser.getGroups()) {
            for (GroupServer x : loggedUser.getGroups()) { //ver se fez join ao grupo
                if (x.getId() == Integer.valueOf(dataString)) {
                    group = x;
                    break;
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

        }

    }
}
