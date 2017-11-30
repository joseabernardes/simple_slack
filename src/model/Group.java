/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {

    private int port;
    private int serverPort;
    private String name;
    private String address;
    private final List<String> messages;
    private final List<User> users;

    public Group(int port, String name, String address) {
        this.port = port;
        this.name = name;
        this.address = address;
        serverPort = -1;
        this.messages = new ArrayList<String>();
        this.users = new ArrayList<User>();
    }

    public List<User> getUsers() {
        return users;
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

    public boolean addMessage(String message) {
        return messages.add(message);
    }

    public List<String> getMessages() {
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

}
