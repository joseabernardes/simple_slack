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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONObject;

public class Group implements Serializable {

    private static int ID = 0;

    private int id;
    private int port;
    private int serverPort;
    private String name;
    private String address;
    private ObservableList<Message> messages;
    private final List<User> users;

    public Group(int port, String name, String address) {
        this.id = ++ID;
        this.port = port;
        this.name = name;
        this.address = address;
        serverPort = -1;
        this.messages = FXCollections.observableArrayList(new ArrayList<Message>());
        this.users = Collections.synchronizedList(new ArrayList<User>());
    }

    public List<User> getUsers() {
        return users;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean addUser(User user) {
        return this.users.add(user);
    }

    public boolean hasUsers() {
        return !this.users.isEmpty();

    }

    public boolean removeUser(User user) {
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

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean addMessage(Message message) {
        return messages.add(message);
    }

    public ObservableList<Message> getMessages() {
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
        Group.ID = id;
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

    public static Group newGroup(JSONObject obj) {
        Group group = new Group(Integer.valueOf(obj.get("port").toString()), obj.get("name").toString(), obj.get("address").toString());
        group.setId(Integer.valueOf(obj.get("id").toString()));
        return group;
    }

}
