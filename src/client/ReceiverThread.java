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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
            JSONObject response;
            while ((inputLine = in.readLine()) != null) {
                response = makeJsonResponse(inputLine);

                if (response.get("command").equals("joinedgroup")) {
                    String[] input = inputLine.split(" ");
                    JSONObject data = makeJsonResponse(response.get("data").toString());
                    if (data.size() == 2) {
                        new MulticastClientThread(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), username).start();
                    }

                } else if (response.get("command").equals("listgroupmsgs")) {
                    String[] input = inputLine.split(" ", 3);
                    if (input.length == 2) {
                        username = input[1];
                    }

                } else if (response.get("command").equals("loginsuccess")) {
                    if (response.size() == 2) {
                        username = response.get("data").toString();
                    }
                }

                System.out.println(response);
            }
        } catch (IOException ex) {
//            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Servidor desligado");
//            System.exit(-1);
        }
    }

    private JSONObject makeJsonResponse(String input) {
        JSONParser parser = new JSONParser();
        JSONObject object = null;
        try {
            object = (JSONObject) parser.parse(input);
        } catch (ParseException ex) {
            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }
}
