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
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Protocol;
import views.auth.AuthController;

/*
 *
 * @author José Bernardes
 * 
 */
public class ReceiverThread extends Thread {

    private final BufferedReader in;
    private String username;
    private final AuthController authController;

    public ReceiverThread(InputStream in, AuthController controller) {
        super("ReceiverThread");
        this.in = new BufferedReader(new InputStreamReader(in));
        username = null;
        this.authController = controller;
    }

    @Override
    public void run() {
        try {
            String inputLine;
            JSONObject response;
            while ((inputLine = in.readLine()) != null) {

                response = makeJsonResponse(inputLine);

                if (response.get("command").equals(Protocol.Server.Group.JOIN_SUCCESS)) {
                    String[] input = inputLine.split(" ");
                    JSONObject data = makeJsonResponse(response.get("data").toString());
                    new MulticastClientThread(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), username).start();

                } else if (response.get("command").equals(Protocol.Server.Group.LIST_GROUP_MSGS)) {
                    String[] input = inputLine.split(" ", 3);
                    if (input.length == 2) {
                        username = input[1];
                    }

                } else if (response.get("command").equals("error")) {
                    if (response.get("data").equals("username_or_password")) {
                        Platform.runLater(() -> {
                            authController.loginError("Your username or password is incorrect!");
                        });
                    }

                } else if (response.get("command").equals(Protocol.Server.Auth.LOGIN_SUCCESS)) {

                    username = response.get("data").toString();

                } else if (response.get("command").equals(Protocol.Server.Private.SEND_FILE)) {

                    JSONObject data = makeJsonResponse(response.get("data").toString());
                    new ClientFile(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("path").toString()).start();

                } else if (response.get("command").equals(Protocol.Server.Group.SEND_FILE)) {

                    JSONObject data = makeJsonResponse(response.get("data").toString());
                    new ClientFile(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("path").toString()).start();

                } else if (response.get("command").equals(Protocol.Server.Private.FILE_SENDED)) {

                } else if (response.get("command").equals(Protocol.Server.Private.RECEIVE_FILE)) {
                    JSONObject data = makeJsonResponse(response.get("data").toString());
                    new ReceiveFileClient(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("name").toString(), Integer.valueOf(data.get("size").toString()), System.getProperty("user.home")).start();

                } else if (response.get("command").equals(Protocol.Server.Group.RECEIVE_FILE)) {
                    JSONObject data = makeJsonResponse(response.get("data").toString());
                    new ReceiveFileClient(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("name").toString(), Integer.valueOf(data.get("size").toString()), System.getProperty("user.home")).start();

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
