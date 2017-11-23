/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author José Bernardes
 */
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;

public class WorkerThreadChat extends Thread {

    private final Socket socket;
    private final ArrayList outList;
    private final SynchronizedArrayList messages;

    public WorkerThreadChat(Socket socket, ArrayList outList, SynchronizedArrayList messages) {
        super("WorkerThread");
        this.socket = socket;
        this.outList = outList;
        this.messages = messages;
    }

    @Override
    public void run() {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            System.out.println("Utilizador ligou-se");

            while ((inputLine = in.readLine()) != null) {

                if (inputLine.startsWith("groupadd ")) {
                    inputLine = inputLine.replaceFirst("groupadd ", "");
                    //create group
                    String outString = "Adicionar grupo " + inputLine;
                    out.println(outString);

                } else {
                    Calendar now = Calendar.getInstance();
                    String outString = now.get(Calendar.HOUR_OF_DAY) + ":" + now.get(Calendar.MINUTE) + ":" + now.get(Calendar.SECOND) + " > " + inputLine;

//                messages.add(outString);
                    out.println(outString);
                }

                if (inputLine.equals("Bye")) {
                    break;
                }
            }

            out.close();
            in.close();
            System.out.println("Utilizador desligou-se!");

        } catch (IOException e) {

            System.out.println("Utilizador desligou-se!");
//            e.printStackTrace();
        }
    }
}
