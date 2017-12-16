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
import java.util.Objects;

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

    @Override
    public int hashCode() {
        int hash = 7;
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
        final PrivateChatServer other = (PrivateChatServer) obj;
        if (!Objects.equals(this.user, other.user)) {
            return false;
        }
        return true;
    }
}
