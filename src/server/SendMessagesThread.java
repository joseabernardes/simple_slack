/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import server.SynchronizedArrayList;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author José Bernardes
 */
public class SendMessagesThread extends Thread {

    private final SynchronizedArrayList messages;
    private final ArrayList outs;

    public SendMessagesThread(SynchronizedArrayList messages, ArrayList outs) {
        this.messages = messages;
        this.outs = outs;
    }

    @Override
    public void run() {

        while (true) {

            for (int i = 0; i < messages.size(); i++) {
                for (Object out : outs) {
                    ((PrintWriter) out).println(messages.get(i));
                }
                messages.remove(messages.get(i));
            }
            try {
                System.out.println("Stop Sending");
                Thread.sleep(6000);
                System.out.println("Sending");
            } catch (InterruptedException ex) {
                Logger.getLogger(SendMessagesThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

}
