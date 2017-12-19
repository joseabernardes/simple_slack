package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

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
                out.println(userInput);
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
