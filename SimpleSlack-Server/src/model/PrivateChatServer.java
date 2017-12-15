/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author José Bernardes
 */
public class PrivateChatServer implements Serializable {

    private final UserServer user;
    private final List<MessageServer> messages;

    public PrivateChatServer(UserServer user) {
        this.user = user;
        this.messages = Collections.synchronizedList(new ArrayList<MessageServer>());
    }

    public UserServer getUser() {
        return user;
    }

    public List<MessageServer> getMessages() {
        return messages;
    }

    public boolean addMessage(MessageServer message) {
        return this.messages.add(message);
    }
}
