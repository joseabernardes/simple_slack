/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author José Bernardes
 */
public class Group {

    private int port;//2222
    private String name;
    private List<User> users;

    public Group(int port, String name) {
        this.port = port;
        this.name = name;
        this.users = new ArrayList<User>();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public boolean addUser(User user) {
        return this.users.add(user);
    }

    /**
     *
     * @param id id do User
     * @return User ou null caso não exista
     */
    public User getUser(int id) {

        for (User user : users) {
            if (user.getId() == id) {
                return user;
            }
        }
        return null;
    }

}
