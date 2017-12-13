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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import model.Group;
import model.Message;
import model.PrivateChat;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.Protocol;
import views.auth.AuthController;
import views.main.MainController;

public class ReceiverThread extends Thread {

    private final BufferedReader in;
    private String username;
    private final AuthController authController;
    private MainController mainController;

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

                response = Protocol.parseJSONResponse(inputLine);

                if (response.get("command").equals(Protocol.Server.Group.JOIN_SUCCESS)) {
                    JSONObject data = Protocol.parseJSONResponse(response.get("data").toString());
                    new MulticastThread(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), username).start();

                } else if (response.get("command").equals(Protocol.Server.Group.LIST_GROUP_MSGS)) {
                    String[] input = inputLine.split(" ", 3);
                    if (input.length == 2) {
                        username = input[1];
                    }

                } else if (response.get("command").equals(Protocol.Server.Auth.LOGIN_ERROR)) {
                    if (response.get("data").equals(Protocol.Server.Auth.Error.USER_PASS)) {
                        Platform.runLater(() -> {
                            authController.loginError("Your username or password is incorrect!");
                        });
                    }

                } else if (response.get("command").equals(Protocol.Server.Auth.LOGIN_SUCCESS)) {
                    JSONObject ob = Protocol.parseJSONResponse(response.get("data").toString());

                    username = ob.get("name").toString();
                    int id = Integer.valueOf(ob.get("id").toString());

                    Platform.runLater(() -> {
                        try {
                            mainController = authController.loginSuccess(id, username);
                        } catch (IOException ex) {
                            Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });

                } else if (response.get("command").equals(Protocol.Server.Group.LIST_JOINED_GROUPS)) {
                    JSONArray array = Protocol.parseJSONListResponse(response.get("data").toString());
                    List<Group> groups = new ArrayList<Group>();
                    for (Object object : array) {
                        JSONObject ob = Protocol.parseJSONResponse((String) object);
                        groups.add(Group.newGroup(ob));
                    }
                    Platform.runLater(() -> {
                        mainController.addJoinedGroups(groups);
                    });

                } else if (response.get("command").equals(Protocol.Server.Private.LIST_PRIVATE_CHAT)) {
                    JSONArray array = Protocol.parseJSONListResponse(response.get("data").toString());
                    List<PrivateChat> chats = new ArrayList<PrivateChat>();
                    for (Object object : array) {
                        JSONObject ob = Protocol.parseJSONResponse((String) object);
                        chats.add(PrivateChat.newPrivateChat(ob));
                    }
                    Platform.runLater(() -> {
                        mainController.addPrivateChat(chats);
                    });

                } else if (response.get("command").equals(Protocol.Server.Auth.REGIST_SUCCESS)) {
                    username = response.get("data").toString();
                    Platform.runLater(() -> {
                        authController.registSuccess(username);
                    });

                } else if (response.get("command").equals(Protocol.Server.Private.RECEIVE_MSG)) {
                    JSONObject ob = Protocol.parseJSONResponse(response.get("data").toString());
                    Message.newMessage(ob);
                   
                    Platform.runLater(() -> {
                        
                    });

                } else if (response.get("command").equals(Protocol.Server.Private.SEND_FILE)) {

                    JSONObject data = Protocol.parseJSONResponse(response.get("data").toString());
                    new SendFile(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("path").toString()).start();

                } else if (response.get("command").equals(Protocol.Server.Group.SEND_FILE)) {

                    JSONObject data = Protocol.parseJSONResponse(response.get("data").toString());
                    new SendFile(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("path").toString()).start();

                } else if (response.get("command").equals(Protocol.Server.Private.FILE_SENDED)) {

                } else if (response.get("command").equals(Protocol.Server.Private.RECEIVE_FILE)) {
                    JSONObject data = Protocol.parseJSONResponse(response.get("data").toString());
                    new ReceiveFile(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("name").toString(), Integer.valueOf(data.get("size").toString()), System.getProperty("user.home")).start();

                } else if (response.get("command").equals(Protocol.Server.Group.RECEIVE_FILE)) {
                    JSONObject data = Protocol.parseJSONResponse(response.get("data").toString());
                    new ReceiveFile(data.get("address").toString(), Integer.valueOf(data.get("port").toString()), data.get("name").toString(), Integer.valueOf(data.get("size").toString()), System.getProperty("user.home")).start();

                }
                System.out.println(response);
            }
        } catch (IOException ex) {
            System.out.println("ReceiverThread Fechado");
        }
    }

}
