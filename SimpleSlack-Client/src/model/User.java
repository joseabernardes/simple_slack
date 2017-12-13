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

/**
 * CLASSE USER CLIENTE
 *
 */
public class User implements Serializable {

    private String username;
    private final List<Group> groups;
    private final List<PrivateChat> privateChat;

    public User(String username) {
        this.username = username;
        this.groups = Collections.synchronizedList(new ArrayList<Group>());
        this.privateChat = Collections.synchronizedList(new ArrayList<PrivateChat>());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public void removePrivateChat(User user) {
        synchronized (privateChat) {
            for (PrivateChat privateChat1 : privateChat) {
                if (privateChat1.getUser().equals(user)) {
                    privateChat.remove(privateChat1);
                    return;
                }
            }
        }
    }

}
