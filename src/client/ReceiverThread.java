/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author José Bernardes
 */
public class ReceiverThread extends Thread {

    private final BufferedReader in;

    public ReceiverThread(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));

    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println(inputLine);
            }
        } catch (IOException ex) {
//            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Servidor desligado");
            System.exit(-1);
        }
    }

}
