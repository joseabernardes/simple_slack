package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import model.GroupClient;
import model.MessageClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.Protocol;
import views.main.MainController;

public class MulticastThread extends Thread {

    private final String address;
    private final String username;
    private final int port;
    private boolean receive;
    private final GroupClient group;
    private final MainController mainController;

    public MulticastThread(String address, int port, String username, GroupClient group, MainController mainController) {
        this.address = address;
        this.port = port;
        this.receive = true;
        this.username = username;
        this.group = group;
        this.mainController = mainController;
    }

    @Override
    public void run() {

        try {
            System.out.println("UDP CLIENT:" + port);
            MulticastSocket socket = new MulticastSocket(port);
            InetAddress address = InetAddress.getByName(this.address);
            socket.joinGroup(address);
            JSONObject response;
            while (receive) {
                try {
                    //receive
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    response = Protocol.parseJSONResponse(received);
                    if (response.get("command").equals(Protocol.Server.Group.LEAVE_SUCCESS)) {
                        if (response.get("data").equals(username)) {
                            Platform.runLater(() -> {
                                System.out.println(mainController);
                                System.out.println(group);
                                mainController.leaveSuccess(group);
                            });
                            receive = false;
                        }
                    } else if (response.get("command").equals(Protocol.Server.Group.LEAVE_ERROR)) {

                        Platform.runLater(() -> {
                            mainController.displaySnackBar("The group that you are trying to leave doen't exists");
                        });

                    } else if (response.get("command").equals(Protocol.Server.Group.SEND_MSG)) {

                        String x = response.get("data").toString();
                        Platform.runLater(() -> {
                            group.addMessage(MessageClient.newMessage(Protocol.parseJSONResponse(x)));
                        });

                    } else if (response.get("command").equals(Protocol.Server.Group.EDIT_SUCCESS)) {
                        JSONObject dataObj = Protocol.parseJSONResponse(response.get("data").toString());
                        Platform.runLater(() -> {
                            mainController.updateGroupName(Integer.valueOf(dataObj.get("id").toString()), dataObj.get("name").toString());
                        });
                    }
                    System.out.println(received);

                } catch (IOException ex) {
                    receive = false;
                    Logger.getLogger(MulticastThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            socket.leaveGroup(address);
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(MulticastThread.class.getName()).log(Level.SEVERE, null, ex);
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
