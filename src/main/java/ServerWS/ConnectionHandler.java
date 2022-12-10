package ServerWS;

import java.net.*;
import java.util.*;
import java.io.*;

public class ConnectionHandler {
    public Socket socket;
    private InputStream in;
    private OutputStream out;
    private ServerSocket server;

    public ConnectionHandler(ServerSocket serverSocket, Socket socket) {
        try {
            this.server = serverSocket;
            this.socket = socket;
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
        }
        catch (IOException e) {

        }
    }

    public void sendData(byte[] pkt) {
        try {
            this.out.write(pkt);
        }
        catch (IOException e) {

        }
    }
}
