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

public class GroupClient implements Serializable {

    private static int ID = 0;

    private int id;
    private int port;
    private int serverPort;
    private String name;
    private String address;
    private ObservableList<MessageClient> messages;
    private final List<UserClient> users;

    public GroupClient(int port, String name, String address) {
        this.id = ++ID;
        this.port = port;
        this.name = name;
        this.address = address;
        serverPort = -1;
        this.messages = FXCollections.observableArrayList(new ArrayList<MessageClient>());
        this.users = Collections.synchronizedList(new ArrayList<UserClient>());
    }

    public List<UserClient> getUsers() {
        return users;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean addUser(UserClient user) {
        return this.users.add(user);
    }

    public boolean hasUsers() {
        return !this.users.isEmpty();

    }

    public boolean removeUser(UserClient user) {
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

    public boolean addMessage(MessageClient message) {
        return messages.add(message);
    }

    public ObservableList<MessageClient> getMessages() {
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
        GroupClient.ID = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GroupClient other = (GroupClient) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
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

    public static GroupClient newGroup(JSONObject obj) {
        GroupClient group = new GroupClient(Integer.valueOf(obj.get("port").toString()), obj.get("name").toString(), obj.get("address").toString());
        group.setId(Integer.valueOf(obj.get("id").toString()));
        return group;
    }
   
}
