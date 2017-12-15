/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import org.json.simple.JSONObject;

/**
 *
 * @author José Bernardes
 */
public class MessageServer implements Comparable<MessageServer>, Serializable {

    private final int id;
    private final String username;
    private final LocalDateTime date;
    private final String message;
    private final boolean file;
    private final int id_destiny;

    /**
     * CONSTRUTOR PARA MENSAGEM DE TEXTO
     *
     * @param id
     * @param username
     * @param date
     * @param message
     * @param id_destiny
     */
    public MessageServer(int id, String username, LocalDateTime date, String message, int id_destiny) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.message = message;
        file = false;
        this.id_destiny = id_destiny;
    }

    /**
     * CONSTRUTOR PARA ENVIO DE FICHEIRO
     *
     * @param id
     * @param username
     * @param date
     * @param message
     * @param id_destiny
     * @param file
     */
    public MessageServer(int id, String username, LocalDateTime date, String message, int id_destiny, boolean file) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.message = message;
        this.file = file;
        this.id_destiny = id_destiny;
    }

    public int getId_destiny() {
        return id_destiny;
    }

    public int getId() {
        return id;
    }

    public boolean isFile() {
        return file;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public int compareTo(MessageServer o) {
        return this.date.compareTo(o.date);
    }

    @Override
    public String toString() {
        JSONObject obj = new JSONObject();
        obj.put("id", String.valueOf(id));
        obj.put("username", username);
        obj.put("date", date.toString());
        obj.put("message", message);
        obj.put("id_destiny", String.valueOf(id_destiny));
        obj.put("file", String.valueOf(file));
        return obj.toJSONString();
    }

}
