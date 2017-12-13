/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.json.simple.JSONObject;

/**
 *
 * @author José Bernardes
 */
public class PrivateChat {

    private final User user;
    private ObservableList<Message> messages;

    public PrivateChat(User user) {
        this.user = user;
        this.messages = FXCollections.observableArrayList(new ArrayList<Message>());
    }

    public User getUser() {
        return user;
    }

    public ObservableList<Message> getMessages() {
        return messages;
    }

    public boolean addMessage(Message message) {
        return this.messages.add(message);
    }

    public static PrivateChat newPrivateChat(JSONObject obj) {
        PrivateChat chat = new PrivateChat(new User(Integer.valueOf(obj.get("id").toString()) ,obj.get("name").toString()));
        return chat;
    }

}
