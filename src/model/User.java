/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import libs.encrypt.BCrypt;

/**
 *
 * @author José Bernardes
 */
public class User implements Serializable {

    private String username;
    private String password;
    private Socket socket;
    private final List<Group> groups;
    private final List<PrivateChat> privateChat;

    public User(String username, String password) {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.socket = null;
        this.groups = Collections.synchronizedList(new ArrayList<Group>());
        this.privateChat = Collections.synchronizedList(new ArrayList<PrivateChat>());
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
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

    /**
     *
     * @param user
     * @return NULL IF NOT EXISTS
     */
    public List<Message> getMessages(User user) {
        synchronized (privateChat) {
            for (PrivateChat privateChat1 : privateChat) {
                if (privateChat1.getUser().equals(user)) {
                    return privateChat1.getMessages();
                }
            }
        }
        return null;
    }

    /**
     *
     * @param user
     * @param message
     * @return FALSE IF NOT ADD
     */
    public boolean addMessage(User user, Message message) {
        synchronized (privateChat) {
            for (PrivateChat privateChat1 : privateChat) {
                if (privateChat1.getUser().equals(user)) {
                    return privateChat1.addMessage(message);
                }
            }
        }
        return false;
    }

}
