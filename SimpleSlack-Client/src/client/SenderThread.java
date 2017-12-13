package client;

import java.io.*;
import java.net.*;
import utils.Protocol;

public class SenderThread extends Thread {

    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    private final PipedInputStream pipedInput;

    public SenderThread(Socket clientSocket, PipedInputStream pipedInput) throws IOException, UnknownHostException {
        super("SenderThread");
        this.clientSocket = clientSocket;
        this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        this.pipedInput = pipedInput;
    }

    @Override
    public void run() {
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(pipedInput));

        String userInput;

        try {
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.startsWith(Protocol.Client.Private.SEND_FILE)) {
                    String[] split = userInput.split(" ");
                    if (split.length == 3) {
                        String user = split[1];
                        String filePath = split[2];
                        File file = new File(filePath);
                        if (file.exists()) {
                            int size = (int) file.length();
                            out.println(Protocol.Client.Private.SEND_FILE + " " + user + " " + file.getName() + " " + size + " " + file.getAbsolutePath());
                        }
                    }
                } else if (userInput.startsWith(Protocol.Client.Group.SEND_FILE)) {
                    String[] split = userInput.split(" ");
                    if (split.length == 3) {
                        String group = split[1];
                        String filePath = split[2];
                        File file = new File(filePath);
                        if (file.exists()) {
                            int size = (int) file.length();
                            out.println(Protocol.Client.Group.SEND_FILE + " " + group + " " + file.getName() + " " + size + " " + file.getAbsolutePath());
                        }
                    }

                } else {
                    out.println(userInput);
                }
            }
            out.close();
            in.close();
            stdIn.close();
            clientSocket.close();
        } catch (IOException ex) {
            System.out.println("SenderThread Fechado");
        }

    }

}
