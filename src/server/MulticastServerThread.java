package server;

import java.io.*;
import java.net.*;

public class MulticastServerThread extends Thread {

    private DatagramSocket socket = null;
    private boolean deleteGroup = true;
    private final String address; //"230.0.0.1"
    private final int groupPort;
    private final int serverPort;

    public MulticastServerThread(String address, int serverPort, int groupPort) throws IOException {
        super("MulticastServerThread");
        this.groupPort = groupPort;
        this.serverPort = serverPort;
        socket = new DatagramSocket(serverPort);
        this.address = address;
    }

    @Override
    public void run() {
        System.out.println("UDP SERVER:" + serverPort + " GROUP:" + groupPort);
        while (deleteGroup) {
            try {
                byte[] buf = new byte[256];
                // receive
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                //System.out.println("Time do cliente " + received);
                if (!received.equals("shutdown")) {
                    // construct request
                    buf = received.getBytes();
                    // send
                    InetAddress group = InetAddress.getByName(address);
                    packet = new DatagramPacket(buf, buf.length, group, groupPort); //send packets to the group
                    socket.send(packet);
                } else {
                    deleteGroup = false;
                }

            } catch (IOException e) {
                e.printStackTrace();
                deleteGroup = false;
            }
        }
        socket.close();
    }
}
