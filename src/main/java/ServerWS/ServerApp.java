package ServerWS;

import Endpoint.MyClientEndpoint;
import com.google.gson.JsonObject;
import org.json.simple.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
public class ServerApp {

    private Socket clientTest;
    private DataInputStream in;
    private DataOutputStream out;
    private byte type_byte[];
    private byte data_byte[];
    public ServerApp(String host, int port) {
        int match_id = 149;
        JSONObject jsonObject1 = makeJson_match(match_id, 2, 100, 0);
//            out_server_game.write(jsonObject1.toString().getBytes());


        try {
            final MyClientEndpoint out_server_ws = new MyClientEndpoint(new URI("ws://104.194.240.16/ws/channels/"));

            // add listener
            out_server_ws.addMessageHandler(new MyClientEndpoint.MessageHandler() {
                public void handleMessage(String message) {
                    System.out.println(message);
                }
            });

            // send message to websocket
            System.out.println("befor_send");

            out_server_ws.sendMessage(jsonObject1.toString());

            // wait 5 seconds for messages from websocket
            System.out.println("hh");
            Thread.sleep(5000);

        } catch (InterruptedException ex) {
            System.err.println("InterruptedException exception: " + ex.getMessage());
        } catch (URISyntaxException ex) {
            System.err.println("URISyntaxException exception: " + ex.getMessage());
        }
//            clientTest = new Socket(host, port);
//            in = new DataInputStream(clientTest.getInputStream());
//            out = new DataOutputStream(clientTest.getOutputStream());
//            ByteBuffer before_send = ByteBuffer.allocate(12);
//            type_byte = inttobyte(4);
//            data_byte = inttobyte(8);
//            before_send.put(type_byte);
//            before_send.put(data_byte);
//            out.write(before_send.array());
//            byte[] pkt_from_server = new byte[5000];
//            in.read(pkt_from_server);
//            type_byte = getBytebyIndex(pkt_from_server, 0, 4);
//            System.out.println(byte_int(type_byte));

    }

    public static byte[] getBytebyIndex(byte[] bytes, int index1, int index2) {
        byte[] outarr = new byte[index2 - index1];
        for (int i = 0; i < index2 - index1; i++) {
            outarr[i] = bytes[i + index1];
        }
        return outarr;
    }
    static byte[] inttobyte(int i) {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.order(ByteOrder.LITTLE_ENDIAN);
        b.putInt(i);
        return b.array();
    }

    static int byte_int(byte[] bytes) {
        return ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    static byte[] Stringtobyte(String s) {
        byte b[] = s.getBytes();
        return b;
    }

    public JSONObject makeJson_match(int match_id, int status, int score_1, int score_2) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("result", 2);
        jsonObject.put("match", match_id);
        jsonObject.put("status", status);
        jsonObject.put("id1", score_1);
        jsonObject.put("id2", score_2);
        return jsonObject;
    }

    static String byteToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }



    public static String[] removeAStrByIndex(int index, String[] arrTest) {
        String [] arrTest2 = new String[arrTest.length - 1];
        for (int i = 0; i < arrTest.length; i++) {
            if (i < index) {
                arrTest2[i] = arrTest[i];
            }
            if (i > index) {
                arrTest2[i - 1] = arrTest[i];
            }
        }
        return arrTest2;
    }

    public int hashIDFromMSV(String _msv)  {
        String s_out = "";
        for (int i = 0; i < _msv.length(); i++) {
            s_out += (char)((int)_msv.charAt(i) * 5 % 10 + 49);
        }
        return Integer.valueOf(s_out);
    }


    public static void main(String[] args) {
        ServerApp sv = new ServerApp("104.194.240.16", 8881);
    }
}
