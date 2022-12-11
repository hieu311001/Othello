package ServerWS;

import Endpoint.MyClientEndpoint;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.DeploymentException;

public class ServerApp {
    public static void main(String[] args) throws URISyntaxException, DeploymentException, IOException {
        MyClientEndpoint client = new MyClientEndpoint();
        int serverIndex = 0;
        try {
            // Gói tin bắt đầu trận đấu
            Map res1 = new HashMap();
            res1.put("result", 1);
            res1.put("match", 79);
            String res1Text = JSONValue.toJSONString(res1);

            // Gói tin kết thúc trận đấu
            Map res3 = new HashMap();
            res3.put("result", 3);
            res3.put("match", 79);
            String res3Text = JSONValue.toJSONString(res3);

            // Gói tin cập nhật trận đấu
            Map res2 = new HashMap();
            res2.put("result", 2);
            res2.put("match", 269);
            res2.put("status", 1);
            res2.put("id1", 100);
            res2.put("id2", 50);
            String res2Text = JSONValue.toJSONString(res2);

            System.out.println(res2Text);

            client.sendMessage(res2Text.toString().getBytes("utf-8"));
        } catch (IOException e) {
            System.out.print(e);
        }
    }
}