package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MulticastClientThread extends Thread {

    private final String address;
    private final String username;
    private final int port;
    private boolean receive;

    public MulticastClientThread(String address, int port, String username) {
        this.address = address;
        this.port = port;
        this.receive = true;
        this.username = username;
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
                    response = makeJsonResponse(received);
                    if (response.get("command").equals("leave")) {
                        if (response.get("data").equals(username)) {
                            receive = false;
                        }
                    }
                    System.out.println(received);
                } catch (IOException ex) {
                    receive = false;
                    Logger.getLogger(MulticastClientThread.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            socket.leaveGroup(address);
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(MulticastClientThread.class.getName()).log(Level.SEVERE, null, ex);
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
