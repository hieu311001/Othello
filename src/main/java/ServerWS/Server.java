package ServerWS;

import Board.Board;
import org.json.simple.JSONValue;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Server extends Thread {
    Socket socket = null;

    public Server(Socket socket) {
        this.socket = socket;
    }

    // Kiểm tra nếu kết nối web đã được lập
    private static boolean webConnect = false;

    // Connection cho web
    private static ConnectionHandler webConnection;
    public static Board board = new Board("Server");

    public static int blackScore = 0;
    public static int whiteScore = 0;
    public static int blackID;
    public static int whiteID;
    public static int nextID = blackID;
    public static int matchID = 0;
    public static int winID = 0;
    public static int numPlayer = 0;
    public static List<Integer> point = new ArrayList<Integer>();
    public static List<ConnectionHandler> clients = new ArrayList<>();
    public static Object lock = new Object();
    public static String turn = "BLACK";
    public static int map[][] =
            {{0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,1,2,0,0,0},
                    {0,0,0,2,1,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}};
    private static boolean canMove(int row, int col, int rowDir, int colDir, int opponent) {
        int currentRow = row + rowDir;
        int currentCol = col + colDir;

        // Nếu vượt ra phạm vi bàn cờ thì trả về
        if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
        {
            return false;
        }

        // Nếu là quân cờ cùng màu hoặc không có quân cờ nào thì trả về
        if ((map[currentRow][currentCol] != opponent && map[currentRow][currentCol] != 0) || map[currentRow][currentCol] == 0) {
            return false;
        }

        while (map[currentRow][currentCol] == opponent) {
            currentRow = currentRow + rowDir;
            currentCol = currentCol + colDir;

            if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
            {
                return false;
            }

            if (map[currentRow][currentCol] != opponent && map[currentRow][currentCol] != 0) {
                return true;
            }


        }

        return false;
    }

    // Kiểm tra nước đi hợp lệ
    private static boolean validMove(int x, int y) {
        int opponent = 2; // Đối thủ là trắng
        if (turn == "WHITE") {
            opponent = 1; // Đối thủ là đen
        }

        if (map[x][y] == 0) {
            // Kiểm tra bên phải
            if (canMove(x, y, 0, 1, opponent)) {
                return true;
            }
            // Kiểm tra bên trái
            else if (canMove(x, y, 0, -1, opponent)) {
                return true;
            }
            // Kiểm tra bên dưới
            else if (canMove(x, y, 1, 0, opponent)) {
                return true;
            }
            // Kiểm tra bên trên
            else if (canMove(x, y, -1, 0, opponent)) {
                return true;
            }
            // Kiểm tra góc phải dưới
            else if (canMove(x, y, 1, 1, opponent)) {
                return true;
            }
            // Kiểm tra góc phải trên
            else if (canMove(x, y, -1, 1, opponent)) {
                return true;
            }
            // Kiểm tra góc trái trên
            else if (canMove(x, y, -1, -1, opponent)) {
                return true;
            }
            // Kiểm tra góc trái dưới
            else if (canMove(x, y, 1, -1, opponent)) {
                return true;
            }
        }
        return false;
    }

    // Logic của 1 lượt đi
    private static void getTurn(int turn, int row, int col) {
        // Đặt nước đi là quân cờ
        map[row][col] = turn;

        // Kiểm tra và lật cờ
        // Kiểm tra trên và dưới
        direction(row, col, turn, 0, -1);
        direction(row, col, turn, 0, 1);

        // Kiểm tra phải và trái
        direction(row, col, turn, 1,0);
        direction(row, col, turn, -1, 0);

        // Kiểm tra các góc
        direction(row, col, turn, 1,1);
        direction(row, col, turn, 1,-1);
        direction(row, col, turn, -1,1);
        direction(row, col, turn, -1,-1);
    }

    // Lật quân cờ theo hướng nhất định
    private static void direction (int row, int col, int turn, int colDir, int rowDir) {
        int currentRow= row + rowDir;
        int currentCol = col + colDir;

        // Nếu vượt ra phạm vi bàn cờ thì trả về
        if (currentRow==8 || currentRow<0 || currentCol==8 || currentCol<0)
        {
            return;
        }

        // Nếu tại vị trí đang xét là 1 quân cờ
        while (map[currentRow][currentCol]==1 || map[currentRow][currentCol]==2)
        {
            // Nếu vị trí cuối cùng là quân cờ cùng màu với lượt
            // thì thực hiện đổi màu tất cả các quân cờ nằm ở hướng ngược lại hướng đã chọn
            if (map[currentRow][currentCol]==turn)
            {
                while(!(row==currentRow && col==currentCol))
                {
                    map[currentRow][currentCol]=turn;
                    currentRow=currentRow-rowDir;
                    currentCol=currentCol-colDir;
                }
                break;
            }
            // Tìm vị trí cuối cùng theo hướng đã chọn
            else
            {
                currentRow=currentRow + rowDir;
                currentCol=currentCol + colDir;
            }

            // Nếu vượt ra phạm vi bàn cờ thì trả về
            if (currentRow<0 || currentCol<0 || currentRow==8 || currentCol==8)
            {
                break;
            }
        }
    }

    // Xét trường hợp kết thúc game đấu
    private static boolean gameOver() {
        // Nếu bàn cờ đã hết quân
        if (blackScore + whiteScore == 64) {
            return true;
        }
        // Nếu bàn cờ chỉ có quân trắng
        if (blackScore == 0) {
            return true;
        }
        // Nếu bàn cờ chỉ có quân đen
        if (whiteScore == 0) {
            return true;
        }

        if (turn == "BLACK") {
            turn = "WHITE";
        }
        else {
            turn = "BLACK";
        }

        // Nếu bàn cờ không còn nước đi
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (validMove(i, j)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void gameResult() {
        if (blackScore > whiteScore) {
            JOptionPane.showMessageDialog(null, "Ván đấu kết thúc. Đen thắng!", "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (blackScore < whiteScore) {
            JOptionPane.showMessageDialog(null, "Ván đấu kết thúc. Trắng thắng!", "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
        }
        else {
            JOptionPane.showMessageDialog(null, "Ván đấu kết thúc. Hai bên hòa!", "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Lấy tọa độ các ô
    public static List<Integer> coordinates(int map[][]) {
        List<Integer> a = new ArrayList<Integer>();
        for (int i = 1; i <= map.length; i++) {
            for (int j = 1; j <= map[i-1].length; j++) {
                if (map[i-1][j-1] != 0)
                {
                    a.add(i*10+j);
                }
            }
        }
        return a;
    }

    // Lấy tọa dộ nước đi
    public static int[] next(int move) {
        if (move < 0) {
            move = move*-1;
        }
        int b[] = {0, 0};
        b[0] = move/10;
        b[1] = move%10;
        return b;
    }

    // Thực hiện bước đi
    public static boolean getMap(int[] a) {
        int x = a[0] - 1;
        int y = a[1] - 1;
        if (validMove(x, y) && turn == "BLACK") {
            getTurn(1, x, y);
        } else if (validMove(x, y) && turn == "WHITE") {
            getTurn(2, x, y);
        }
        else if (!validMove(x, y)) {
            return false;
        }
        return true;
    }

//    public static byte[] convert_map(int[][] map) {
//        byte[] bytes = {};
//        for (int i = 0; i < map.length; i++) {
//            for (int j = 0; j < map[i].length; j++) {
//                if (map[i][j] != 0) {
//                    bytes += convert_data((i+1)*10+j+1);
//                }
//            }
//        }
//        return bytes;
//    }

    // Print Map
    public static void printMap(int[][] map){
        point.clear();
        int scoreBlack = 0;
        int scoreWhite = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
                if (map[i][j] == 1) {
                    scoreBlack++;
                    point.add((i+1)*10+j+1);
                } else if (map[i][j] == 2) {
                    point.add(-((i+1)*10+j+1));
                    scoreWhite++;
                }
            }
            System.out.println("");
        }
        System.out.println("-------------------------------------");
        blackScore = scoreBlack;
        whiteScore = scoreWhite;

        if (winID != 0) {
            board.paint(map);
        }
    }

    // Chuyển int thành byte
    public static byte[] convert_data(int data)
    {
        byte[] b = new byte[4];
        b[0] = (byte)data;
        b[1] = (byte)((data >> 8) & 0xFF);
        b[2] = (byte)((data >> 16) & 0xFF);
        b[3] = (byte)((data >> 24) & 0xFF);
        return b;
    }

    // Chuyển byte thành int
    public static int restoreInt(byte[] bytes) {
        return ((bytes[3] & 0xFF) << 24) |
                ((bytes[2] & 0xFF) << 16) |
                ((bytes[1] & 0xFF) << 8) |
                ((bytes[0] & 0xFF) << 0);
    }


    // Khởi tạo gói tin gửi đi
    public static byte[] set_pkt(int type, int len, byte[] data) {
        byte[] out = new byte[8 + len];
        byte[] type_ = convert_data(type);
        byte[] len_ = convert_data(len);
        for (int i = 0; i < 4; i++)         out[i] = type_[i];
        for (int i = 4; i < 8; i++)         out[i] = len_[i - 4];
        for (int i = 8; i < (8 + len); i++) out[i] = data[i - 8];

        return out;
    }

    // Tạo chuỗi byte gửi đi ở gói tin 3
    public static byte[] pkt_map(int blackScore, int whiteScore, int id, List<Integer> point){
        int len = 12 + point.size()*4;
        byte[] out = new byte[len];
        byte[] black = convert_data(blackScore);
        byte[] white = convert_data(whiteScore);
        // Mã hóa 2 điểm số
        for (int i = 0; i < 4 ; i++) {
            out[i] = black[i];
        }
        for (int i = 4; i < 8 ; i++) {
            out[i] = white[i-4];
        }
        // Mã hóa id
        for (int i = 8; i < 12; i++) {
            out[i] = convert_data(id)[i-8];
        }
        // Mã hóa map gửi đi
        for (int i = 0; i < point.size(); i++) {
            for (int j = 0; j < 4; j++) {
                byte[] bt = convert_data(point.get(i));
                out[12 +  j + i*4] = bt[j];
            }
        }
        return out;
    }

    public void run() {
        //Board board = new Board("Server");
        board.view();

        byte[] input = new byte[4];

        int type = -1;
        int len = -1;
        try {

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();

            if (!webConnect) {
                webConnect = true;

                // Khởi tạo game trên web socket
                byte[] req = new byte[1000];
                socket.getInputStream().read(req);
                String reqJson
                        = new String(req,
                        StandardCharsets.UTF_8);

                System.out.println(reqJson);
                JsonReader jsonReader = Json.createReader(new StringReader(reqJson));
                JsonObject object;
                try {
                    object = jsonReader.readObject();
                    blackID = object.getInt("id1");
                    whiteID = object.getInt("id2");
                    matchID = object.getInt("match");
                }
                catch (JsonParsingException e) {

                }
                jsonReader.close();

                if(reqJson != "") {
                    Map res = new HashMap();
                    res.put("result", 1);
                    res.put("ip", "0.tcp.ap.ngrok.io");
                    res.put("port", 11800);
                    res.put("path", "apollozz");
                    String jsonText = JSONValue.toJSONString(res);

                    socket.getOutputStream().write(jsonText.getBytes("utf-8"));
                } else {
                    Map res = new HashMap();
                    res.put("result", 0);
                    String jsonText = JSONValue.toJSONString(res);

                    socket.getOutputStream().write(jsonText.getBytes("utf-8"));
                }
            }

            // Trao đổi gói tin giữa server và player
            while (true) {
                is.read(input);
                type = restoreInt(input);
                is.read(input);
                len = restoreInt(input);
                if (len <= 0) {
                    continue;
                }

                if (type == 0) {
                    // Gửi gói tin xác nhân kết nối thành công
                    byte[] point = new byte[5];
                    is.read(point);
                    String nextPoint = new String(point, "utf-8");
                    if (nextPoint.equals("BLACK")) {
                        nextID = blackID;
                    } else if (nextPoint.equals("WHITE")) {
                        nextID = whiteID;
                    }

                    if (!clients.isEmpty()) {
                        clients.get(numPlayer).sendData(set_pkt(1, 4, convert_data(nextID)));
                    }
                }
                else if (type == 2) {
                    is.read(input); int id = restoreInt(input);
                    System.out.println("ID người chơi: " + id);
                    numPlayer++;
                    board.paint(map);
                    if (numPlayer == 2) {
                        printMap(map);
                        int nextID = blackID;
                        int length = 12 + point.size()*4;

                        byte[] out = pkt_map(blackScore, whiteScore, nextID, point);
                        for (ConnectionHandler client : clients) {
                            client.sendData(set_pkt(3, length, out));
                        }
                    }
                }
                else if (type == 4) {
                    is.read(input);  int id = restoreInt(input);
                    is.read(input);  int points = restoreInt(input);

                    // Dựa vào id để xác định turn này là quân nào đi
                    if (id == blackID) {
                        turn = "BLACK";
                    } else {
                        turn = "WHITE";
                    }

                    int[] move = next(points);
                    boolean checkPoint = getMap(move);
                    printMap(map);
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    }
                    catch(InterruptedException e) {

                    }
                    board.paint(map);
                    if (!checkPoint) {
                        int length = 12 + point.size()*4;
                        byte[] out = pkt_map(blackScore, whiteScore, id, point);
                        os.write(set_pkt(5, length, out));
                    } else {
                        if (id == blackID) {
                            nextID = whiteID;
                        }
                        else {
                            nextID = blackID;
                        }
                    }
                    if(gameOver()) {
                        if (blackScore > whiteScore) {
                            id = blackID;
                        }
                        else if (blackScore < whiteScore) {
                            id = whiteID;
                        }
                        else {
                            id = 0;
                        }
                        for (ConnectionHandler client : clients) {
                            int length = 12 + point.size()*4;
                            byte[] out = pkt_map(blackScore, whiteScore, nextID, point);
                            client.sendData(set_pkt(3, length, out));
                            client.sendData(set_pkt(6, 4, convert_data(id)));
                        }
                        gameResult();
                    }
                    else {
                        int length = 12 + point.size()*4;
                        byte[] out = pkt_map(blackScore, whiteScore, nextID, point);
                        for (ConnectionHandler client : clients) {
                            client.sendData(set_pkt(3, length, out));
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Kết nối hỏng");
        }
    }

    public static void main(String[] args) {
        int serverIndex = 0;
        try {
            ServerSocket sk=new ServerSocket(8889);

            System.out.println("Server is connecting....");
            boolean listening=true;
            while(listening){
                serverIndex++;
                Socket socket = sk.accept();
                ConnectionHandler client = new ConnectionHandler(sk, socket);
                synchronized (lock) {
                    if (serverIndex > 1){
                        clients.add(client);
                    }
                }

                new Server(socket).start();

                System.out.println("Client " + serverIndex + " is connect");
            }
        } catch (IOException e) {
            System.out.print(e);
        }
    }
}