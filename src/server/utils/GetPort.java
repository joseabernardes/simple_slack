/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.utils;

import java.util.Iterator;
import java.util.List;
import libs.portFinder.AvailablePortFinder;
import model.Group;

/**
 *
 * @author José Bernardes
 */
public class GetPort {

    // 2, 3 , 4 ,1 ,5
    public static int getFreeAvaliablePort(List<Group> groups) {
        int port = AvailablePortFinder.getNextAvailable(); //1
        boolean find = true;
        while (find) {
            find = false;
            Iterator<Group> iterator = groups.iterator();
            while (iterator.hasNext() && !find) {
                Group g = iterator.next();
                if (g.getPort() == port || g.getServerPort() == port) {
                    find = true;
                }
            }
            if (find) {
                port = AvailablePortFinder.getNextAvailable(port);//2
            }
        }
        return port;
    }

}
