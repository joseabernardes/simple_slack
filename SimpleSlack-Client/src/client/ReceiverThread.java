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
import model.GroupClient;
import model.MessageClient;
import model.PrivateChatClient;
import model.UserClient;
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
            while ((inputLine = in.readLine()) != null) {
                JSONObject response = Protocol.parseJSONResponse(inputLine);
                String command = response.get(Protocol.COMMAND).toString();
                String dataString = response.get(Protocol.DATA).toString();//JSONObject or pure String
                JSONObject dataObj;
                JSONArray dataArray;
                switch (command) {
                    /**
                     * AUTH
                     */
                    case Protocol.Server.Auth.LOGIN_SUCCESS:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        username = dataObj.get("name").toString();
                        int id = Integer.valueOf(dataObj.get("id").toString());

                        Platform.runLater(() -> {
                            try {
                                mainController = authController.loginSuccess(id, username);
                            } catch (IOException ex) {
                                Logger.getLogger(ReceiverThread.class.getName()).log(Level.SEVERE, null, ex); //error
                            }
                        });
                        break;
                    case Protocol.Server.Auth.LOGIN_ERROR:

                        loginErrors(dataString);

                        break;
                    case Protocol.Server.Auth.REGIST_SUCCESS:
                        Platform.runLater(() -> {
                            authController.registError("");
                            authController.registSuccess(dataString);
                        });
                        break;

                    case Protocol.Server.Auth.REGIST_ERROR:

                        registErrors(dataString);
                        break;

                    /**
                     * PRIVATE CHAT
                     */
                    case Protocol.Server.Private.LIST_PRIVATE_CHAT:
                        dataArray = Protocol.parseJSONListResponse(dataString);
                        List<PrivateChatClient> chats = new ArrayList<PrivateChatClient>();
                        for (Object object : dataArray) {
                            JSONObject obj = Protocol.parseJSONResponse(object.toString());
                            chats.add(PrivateChatClient.newPrivateChat(obj));
                        }
                        Platform.runLater(() -> {
                            mainController.addPrivateChats(chats);
                        });
                        break;

                    case Protocol.Server.Private.LIST_LOGGED_USERS:
                        dataArray = Protocol.parseJSONListResponse(dataString);
                        List<PrivateChatClient> loggedUsers = new ArrayList<PrivateChatClient>();
                        for (Object object : dataArray) {
                            JSONObject obj = Protocol.parseJSONResponse(object.toString());
                            loggedUsers.add(PrivateChatClient.newPrivateChat(obj));
                        }
                        Platform.runLater(() -> {
                            mainController.openAddPrivate(loggedUsers);
                        });

                        break;

                    case Protocol.Server.Private.SEND_ERROR:

                        if (dataString.equals(Protocol.Server.Private.Error.USER)) {
                            Platform.runLater(() -> {
                                mainController.displayError("Cannot send message, user not logged in");
                            });
                        }

                        break;

                    case Protocol.Server.Private.RECEIVE_MSG:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        Platform.runLater(() -> {
                            mainController.addMessageToPrivateChat(MessageClient.newMessage(dataObj));
                        });
                        break;
                    case Protocol.Server.Private.SEND_FILE:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        new SendFile(dataObj.get("address").toString(), Integer.valueOf(dataObj.get("port").toString()), dataObj.get("path").toString()).start();
                        break;
                    case Protocol.Server.Private.FILE_SENDED:
                        //TODO
                        break;
                    case Protocol.Server.Private.RECEIVE_FILE:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        new ReceiveFile(dataObj.get("address").toString(), Integer.valueOf(dataObj.get("port").toString()), dataObj.get("name").toString(), Integer.valueOf(dataObj.get("size").toString()), System.getProperty("user.home")).start();
                        break;
                    /**
                     * GROUP CHAT
                     */
                    case Protocol.Server.Group.JOIN_SUCCESS:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        GroupClient group = new GroupClient(Integer.valueOf(dataObj.get("port").toString()), dataObj.get("name").toString(), dataObj.get("address").toString());
                        group.setId(Integer.valueOf(dataObj.get("id").toString()));
                        new MulticastThread(dataObj.get("address").toString(), Integer.valueOf(dataObj.get("port").toString()), username, group).start();
                        Platform.runLater(() -> {
                            this.mainController.addGroupToClientUser(group);
                        });

                        break;
                    case Protocol.Server.Group.LIST_GROUP_MSGS:
                        //TODO
                        break;

                    case Protocol.Server.Group.LIST_JOINED_GROUPS:
                        dataArray = Protocol.parseJSONListResponse(dataString);
                        List<GroupClient> groups = new ArrayList<GroupClient>();
                        for (Object object : dataArray) {
                            JSONObject ob = Protocol.parseJSONResponse(object.toString());
                            groups.add(GroupClient.newGroup(ob));
                        }
                        Platform.runLater(() -> {
                            mainController.addJoinedGroups(groups);
                        });
                        break;

                    case Protocol.Server.Group.SEND_FILE:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        new SendFile(dataObj.get("address").toString(), Integer.valueOf(dataObj.get("port").toString()), dataObj.get("path").toString()).start();
                        break;
                    case Protocol.Server.Group.RECEIVE_FILE:
                        dataObj = Protocol.parseJSONResponse(dataString);
                        new ReceiveFile(dataObj.get("address").toString(), Integer.valueOf(dataObj.get("port").toString()), dataObj.get("name").toString(), Integer.valueOf(dataObj.get("size").toString()), System.getProperty("user.home")).start();
                        break;
                }
                System.out.println(response);
            }
        } catch (IOException ex) {
            System.out.println("ReceiverThread Fechado");
        }
    }

    private void loginErrors(String dataString) {
        String result;
        switch (dataString) {
            case Protocol.Server.Auth.Error.USER_PASS:
                result = "Your username or password is incorrect";
                break;
            default:
                result = "Something went wrong, contact the admin";
        }
        Platform.runLater(() -> {
            authController.loginError(result);
        });
    }

    private void registErrors(String dataString) {
        String result;
        switch (dataString) {
            case Protocol.Server.Auth.Error.PASS_MATCH:
                result = "Password doesn't match";
                break;
            case Protocol.Server.Auth.Error.USER_EXISTS:
                result = "Username already exists";
                break;
            case Protocol.Server.Auth.Error.REGEX:
                result = "Use only word charaters and numbers | {1-20} characters";
                break;
            default:
                result = "Something went wrong, contact the admin";
        }
        Platform.runLater(() -> {
            authController.registError(result);
        });
    }
}
