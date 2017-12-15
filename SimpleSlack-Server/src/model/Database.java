/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author José Bernardes
 */
public class Database implements Serializable {

    private List<GroupServer> groups;
    private List<UserServer> users;

    public Database(List<GroupServer> groups, List<UserServer> users) {
        this.groups = groups;
        this.users = users;
    }

    public List<GroupServer> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupServer> groups) {
        this.groups = groups;
    }

    public List<UserServer> getUsers() {
        return users;
    }

    public void setUsers(List<UserServer> users) {
        this.users = users;
    }

}
