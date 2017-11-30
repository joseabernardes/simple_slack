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

/*
 *
 * @author José Bernardes
 * 
 */
public class ReceiverThread extends Thread {

    private final BufferedReader in;
    private String username;

    public ReceiverThread(InputStream in) {
        this.in = new BufferedReader(new InputStreamReader(in));
        username = null;
    }

    @Override
    public void run() {
        try {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("joinedgroup")) {
                    String[] input = inputLine.split(" ");
                    if (input.length == 3) {
                        new MulticastClientThread(input[2], Integer.valueOf(input[1]), username).start();
                    }
                } else if (inputLine.startsWith("loginsuccess")) {
                    String[] input = inputLine.split(" ");
                    if (input.length == 2) {
                        username = input[1];
                    }
                }

                System.out.println(inputLine);
            }
        } catch (IOException ex) {
//            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Servidor desligado");
//            System.exit(-1);
        }
    }

}
