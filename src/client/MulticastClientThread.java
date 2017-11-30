package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            while (receive) {
                try {
                    //receive
                    byte[] buf = new byte[256];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    if (received.startsWith(username + " leavegroup")) {
                        receive = false;
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
}
