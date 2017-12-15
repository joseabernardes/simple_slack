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
import java.util.Objects;

/**
 * CLASSE USER CLIENTE
 *
 */
public class UserClient implements Serializable {

    private int id;
    private String username;
    private final List<GroupClient> groups;
    private List<PrivateChatClient> privateChat;

    public UserClient(int id, String username) {
        this.username = username;
        this.id = id;
        this.groups = Collections.synchronizedList(new ArrayList<GroupClient>());
        this.privateChat = Collections.synchronizedList(new ArrayList<PrivateChatClient>());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrivateChat(List<PrivateChatClient> privateChat) {
        this.privateChat = privateChat;
    }

    public List<PrivateChatClient> getPrivateChat() {
        return privateChat;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean addGroup(GroupClient group) {
        return this.groups.add(group);
    }

    public boolean removeGroup(GroupClient group) {
        return this.groups.remove(group);
    }

    public List<GroupClient> getGroups() {
        return this.groups;
    }

    /**
     *
     * @param user
     * @return NULL IF NOT EXISTS
     */
    public List<MessageClient> getMessages(UserClient user) {
        synchronized (privateChat) {
            for (PrivateChatClient privateChat1 : privateChat) {
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
//    public boolean addMessage(UserClient user, MessageClient message) {
//        synchronized (privateChat) {
//            for (PrivateChatClient privateChat1 : privateChat) {
//                if (privateChat1.getUser().equals(user)) {
//                    return privateChat1.addMessage(message);
//                }
//            }
//        }
//        return false;
//    }
    /**
     *
     * @param user
     * @param message
     * @return Boolean IF PRIVATE CHAT EXISTS, OR PRIVATECHAT IF CHAT DOESNT EXIST
     */
    public Object addMessage(UserClient user, MessageClient message) {
        for (PrivateChatClient privateChat1 : privateChat) {
            if (privateChat1.getUser().equals(user)) {
                return privateChat1.addMessage(message);
            }
        }
        PrivateChatClient chat = new PrivateChatClient(user);
        privateChat.add(chat);
        chat.addMessage(message);
        return chat;
    }

    public void removePrivateChat(UserClient user) {
        synchronized (privateChat) {
            for (PrivateChatClient privateChat1 : privateChat) {
                if (privateChat1.getUser().equals(user)) {
                    privateChat.remove(privateChat1);
                    return;
                }
            }
        }
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
        final UserClient other = (UserClient) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.id;
        return hash;
    }

}
