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
    private GroupClient group;
    

    public MulticastThread(String address, int port, String username,GroupClient group) {
        this.address = address;
        this.port = port;
        this.receive = true;
        this.username = username;
        this.group = group;
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
                            receive = false;
                        }
                    }
                    this.group.addMessage(MessageClient.newMessage(response));
//                    System.out.println(received);
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
