/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import model.GroupServer;
import model.UserServer;
import org.json.simple.JSONObject;
import utils.Protocol;

/**
 * Class responsable for Receiving a file in the Server
 */
public class ReceiveFile extends Thread {

    static final int PRIVATE = 0;
    static final int GROUP = 1;
    private ServerSocket socket;
    private String fileName;
    private int size;
    private UserServer receiver;
    private GroupServer group;
    private DatagramSocket socketUDP;
    private int type;
    private UserServer sender;

    /**
     * Constructor for Private receiver
     *
     * @param port
     * @param fileName
     * @param size
     * @param receiver
     * @param sender
     */
    public ReceiveFile(int port, String fileName, int size, UserServer receiver, UserServer sender) {
        try {
            socket = new ServerSocket(port);
            this.fileName = fileName;
            this.size = size;
            this.receiver = receiver;
            this.sender = sender;
            this.type = ReceiveFile.PRIVATE;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor for Group receiver
     *
     * @param port
     * @param fileName
     * @param size
     * @param group
     * @param socketUDP
     * @param sender
     */
    public ReceiveFile(int port, String fileName, int size, GroupServer group, UserServer sender, DatagramSocket socketUDP) {
        try {
            socket = new ServerSocket(port);
            this.fileName = fileName;
            this.size = size;
            this.sender = sender;
            this.group = group;
            this.type = ReceiveFile.GROUP;
            this.socketUDP = socketUDP;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Socket clientSock = socket.accept();

            saveFile(clientSock);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile(Socket clientSock) throws IOException {
        DataInputStream dis = new DataInputStream(clientSock.getInputStream());

        String path;
        if (type == ReceiveFile.PRIVATE) {
            path = "files/private/" + receiver.getUsername();
        } else {
            path = "files/groups/" + group.getId();
        }

        File directory = new File(path);
        if (!directory.exists()) {
            directory.mkdirs();

        }

        int i = 0;
        String folder;
        folder = path;
        path = folder + "/" + fileName;
        File file = new File(path);
        while (file.exists()) {
            if (path.indexOf(".") > 0) {
                String extension = path.substring(path.lastIndexOf("."), path.length());
                String name = fileName.substring(0, fileName.lastIndexOf("."));

                if (name.indexOf("_") > 0) {
                    String after = name.substring(0, name.lastIndexOf("_"));
                    fileName = after + "_" + i + extension;
                } else {
                    fileName = name + "_" + i + extension;
                }
                path = folder + "/" + fileName;
                file = new File(path);

                i++;
            }
        }

        FileOutputStream fos = new FileOutputStream(path);
        byte[] buffer = new byte[4096];

//        int filesize = 15123; // Send file size in separate msg
        int filesize = size;
        int read = 0;
        int totalRead = 0;
        int remaining = filesize;
        while ((read = dis.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(buffer, 0, read);
        }

        fos.close();
        dis.close();
        System.out.println("FILE RECEIVED");
        JSONObject obj = new JSONObject();
        obj.put("username", sender.getUsername());
        obj.put("date", LocalDateTime.now());
        obj.put("filename", fileName);
        obj.put("filesize", size);
        if (type == ReceiveFile.PRIVATE) {
            PrintWriter outReceiver = new PrintWriter(receiver.getSocket().getOutputStream(), true);

            outReceiver.println(makeJsonResponse(Protocol.Server.Private.FILE_SENDED, obj.toJSONString()));

        } else {
            byte[] buf = new byte[256];
            String res = makeJsonResponse(Protocol.Server.Group.RECEIVE_FILE, obj.toJSONString()).toJSONString();
            buf = res.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, InetAddress.getByName(group.getAddress()), group.getPort());
            socketUDP.send(packet);
//            group.addMessage(msg);

        }

    }

    private JSONObject makeJsonResponse(String command, String data) {
        JSONObject object = new JSONObject();
        object.put("command", command);
        object.put("data", data);
        return object;
    }

}
