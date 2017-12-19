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
    private final int fileSize;

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
        this.file = false;
        this.fileSize = -1;
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
     * @param fileSize
     */
    public MessageServer(int id, String username, LocalDateTime date, String message, int id_destiny, int fileSize) {
        this.id = id;
        this.username = username;
        this.date = date;
        this.message = message;
        this.file = true;
        this.id_destiny = id_destiny;
        this.fileSize = fileSize;
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

    public int getFileSize() {
        return fileSize;
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
        obj.put("file_size", String.valueOf(fileSize));

        return obj.toJSONString();
    }

}
