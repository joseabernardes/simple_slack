/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import libs.encrypt.BCrypt;

/**
 *
 * @author José Bernardes
 */
public class User implements Serializable {

    public static int ID = 0;
    private int id;
    private String username;
    private String password;
    private Socket socket;
    private final List<Group> groups;

    public User(String username, String password) {
        this.id = ++ID;
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.socket = null;
        groups = new ArrayList<Group>();
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean checkPassword(String password) {
        return BCrypt.checkpw(password, this.password);
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public boolean addGroup(Group group) {
        return this.groups.add(group);
    }

    public boolean removeGroup(Group group) {
        return this.groups.remove(group);
    }

    public List<Group> getGroups() {
        return this.groups;
    }

}
