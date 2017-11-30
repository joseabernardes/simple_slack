package client;

import java.io.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSenderTCP extends Thread {

    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;

    public ClientSenderTCP(Socket clientSocket) throws IOException, UnknownHostException {
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    private void sendGroupUDP(String input) {
        try {
            MulticastSocket socket;

            socket = new MulticastSocket(4445);

            InetAddress address = InetAddress.getByName("230.0.0.1");
            socket.joinGroup(address);
            DatagramPacket packet;

            // get a few quotes
            for (int i = 0; i < 5; i++) {

                byte[] buf = new byte[256];
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Quote of the Moment: " + received);
            }

            socket.leaveGroup(address);
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientSenderTCP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void start() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        String userInput;

        try {
            while ((userInput = stdIn.readLine()) != null) {

                out.println(userInput);

//                if (userInput.equals("Bye")) {
//                    break;
//                } else {
//                }
            }
            out.close();
            in.close();
            stdIn.close();
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientSenderTCP.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static void main(String[] args) {
        int port = (args.length != 1) ? 7777 : Integer.valueOf(args[0]); //se não tiver argumentos, porta 7777, se tiver, lê e seleciona a porta
        String host = "127.0.0.1";
        try {
            Socket clientSocket = new Socket(host, port);
            new ReceiverThread(clientSocket.getInputStream()).start();
            new ClientSenderTCP(clientSocket).start();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host:" + host + " .");
//            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to:" + host + " .");
//            System.exit(1);
        }

    }
}
