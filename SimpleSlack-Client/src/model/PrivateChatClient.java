/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONObject;

/**
 *
 * @author José Bernardes
 */
public class PrivateChatClient implements Serializable {

    private final UserClient user;
    private ObservableList<MessageClient> messages;

    public PrivateChatClient(UserClient user) {
        this.user = user;
        this.messages = FXCollections.observableArrayList(new ArrayList<MessageClient>());
    }

    public UserClient getUser() {
        return user;
    }

    public ObservableList<MessageClient> getMessages() {
        return messages;
    }

    public boolean addMessage(MessageClient message) {
        return this.messages.add(message);
    }

    public static PrivateChatClient newPrivateChat(JSONObject obj) {
        PrivateChatClient chat = new PrivateChatClient(new UserClient(Integer.valueOf(obj.get("id").toString()), obj.get("name").toString()));
        return chat;
    }

}
