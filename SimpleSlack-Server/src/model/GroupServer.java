/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONObject;

public class GroupServer implements Serializable {

    private static int ID = 0;

    private int id;
    private int port;
//    private int serverPort;
    private String name;
    private String address;
    private final List<MessageServer> messages;
    private final List<UserServer> users;

    public GroupServer(int port, String name, String address) {
        this.id = ++ID;
        this.port = port;
        this.name = name;
        this.address = address;
//        serverPort = -1;
        this.messages = Collections.synchronizedList(new ArrayList<MessageServer>());
        this.users = Collections.synchronizedList(new ArrayList<UserServer>());
    }

    public List<UserServer> getUsers() {
        return users;
    }

    public boolean addUser(UserServer user) {
        return this.users.add(user);
    }

    public int size() {
        return this.users.size();
    }

    public boolean removeUser(UserServer user) {
        return this.users.remove(user);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    /*
    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }*/

    public boolean addMessage(MessageServer message) {
        return messages.add(message);
    }

    public List<MessageServer> getMessages() {
        return this.messages;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static void setID(int id) {
        GroupServer.ID = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("address", address);
        obj.put("port", port);
        obj.put("id", id);
        obj.put("name", name);
        return obj.toJSONString();
    }

}
