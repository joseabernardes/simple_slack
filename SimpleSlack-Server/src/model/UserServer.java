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
import org.json.simple.JSONObject;
import utils.Protocol;

/**
 *
 * @author José Bernardes
 */
public class UserServer implements Serializable {

    private static int ID = 0;
    private final int id;
    private String username;
    private String password;
    private transient Socket socket;
    private final List<GroupServer> groups;
    private List<PrivateChatServer> privateChat;

    public UserServer(String username, String password) {
        this.id = ++ID;
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.socket = null;
        this.groups = Collections.synchronizedList(new ArrayList<GroupServer>());
        this.privateChat = Collections.synchronizedList(new ArrayList<PrivateChatServer>());
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

    public boolean addGroup(GroupServer group) {
        return this.groups.add(group);
    }

    public boolean removeGroup(GroupServer group) {
        return this.groups.remove(group);
    }

    public List<GroupServer> getGroups() {
        return this.groups;
    }

    /**
     *
     * @param user
     * @return NULL IF NOT EXISTS
     */
    public List<MessageServer> getMessages(UserServer user) {
        synchronized (privateChat) {
            for (PrivateChatServer privateChat1 : privateChat) {
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
    public boolean addMessage(UserServer user, MessageServer message) {
        synchronized (privateChat) {
            for (PrivateChatServer privateChat1 : privateChat) {
                if (privateChat1.getUser().equals(user)) {
                    return privateChat1.addMessage(message);
                }
            }
            PrivateChatServer chat = new PrivateChatServer(user);
            privateChat.add(chat);
            return chat.addMessage(message);
        }
    }

    public void removePrivateChat(UserServer user) {
        synchronized (privateChat) {
            for (PrivateChatServer privateChat1 : privateChat) {
                if (privateChat1.getUser().equals(user)) {
                    privateChat.remove(privateChat1);
                    return;
                }
            }
        }
    }

    public List<PrivateChatServer> getPrivateChat() {
        return privateChat;
    }

    public void setPrivateChat(List<PrivateChatServer> privateChat) {
        this.privateChat = privateChat;
    }

    public int getId() {
        return id;
    }

    public static void setID(int id) {
        UserServer.ID = id;
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("name", username);
        obj.put("id", String.valueOf(id));
        return obj.toJSONString();
    }

    //RTEMOVER
    public void addPrivateChat(UserServer user) {
        privateChat.add(new PrivateChatServer(user));
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
        final UserServer other = (UserServer) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }
}
