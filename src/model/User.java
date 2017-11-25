/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import encrypt.BCrypt;

/**
 *
 * @author José Bernardes
 */
public class User {

    public static int ID;
    private int id;
    private String name;
    private String username;
    private String password;

    public User(String name, String username, String password) {
        this.name = name;
        this.username = username;        
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean checkPassword(String password) {
        if(BCrypt.checkpw(password, this.password)){
            return true;
        }
        return false;
    }

    public void setPassword(String password) {
        this.password = BCrypt.hashpw(password,BCrypt.gensalt());
    }
    
    public static void main(String[] args) {
        User user = new User("nome","usernome","password");
        System.out.println(user.checkPassword("gay"));
        
    }
    
    
}
