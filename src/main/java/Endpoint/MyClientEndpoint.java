package Endpoint;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
@ClientEndpoint
public class MyClientEndpoint {

    // Chuyển byte thành int
    public static int restore(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8) |
                ((bytes[0] & 0xFF) << 0);
    }

    private Session session = null;
    public MyClientEndpoint() throws URISyntaxException, DeploymentException, IOException {
        URI uri = new URI( "ws://104.194.240.16/ws/channels/");
        ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
    }

    @OnOpen
    public void handleOpen(Session session) {
        this.session = session;
        System.out.println("Connected to Server!");
    }
    @OnMessage
    public String handleMessage(String message) {
        System.out.println("Response from Server: " + message);
        return message;
    }
    @OnMessage
    public byte[] handleMessage(byte[] message) {
        System.out.println("Response from Server: " + restore(message));
        return message;
    }
    @OnClose
    public void handleClose() {
        System.out.println("Disconnected to Server!");
    }
    @OnError
    public void handleError(Throwable t) {
        t.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    public void sendMessage(byte[] message) throws IOException {
        this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
    }

    public void disconnect() throws IOException {
        this.session.close();
    }

}