/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.util.ArrayList;

/**
 *
 * @author José Bernardes
 */
public class SynchronizedArrayList {

    private ArrayList list;

    public SynchronizedArrayList() {
        list = new ArrayList();

    }

    public synchronized void add(Object o) {
        list.add(o);
    }

    public synchronized Object get(int k) {
        return list.get(k);
    }

    public synchronized int size() {
        return list.size();
    }

    public synchronized void remove(Object o) {
        list.remove(o);
    }

}
