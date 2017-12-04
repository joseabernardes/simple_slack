/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author José Bernardes
 */
public class PrivateChat {

    private final User user;
    private final List<Message> messages;

    public PrivateChat(User user) {
        this.user = user;
        this.messages = Collections.synchronizedList(new ArrayList<Message>());
    }

    public User getUser() {
        return user;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public boolean addMessage(Message message) {
        return this.messages.add(message);
    }
}
