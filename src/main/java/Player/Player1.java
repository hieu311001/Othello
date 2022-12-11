package Player;

import Board.Board;

import javax.swing.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class Player1 {

    public static int ID;
    public static int myID;
    public static int lenMap;
    public static String IP = "localhost";
    public static final int PORT = 8889;
    public static final String myPoint = "BLACK";
    public static int blackScore = 0;
    public static int whiteScore = 0;
    public static int[] point = new int[64];
    public static int map[][] =
            {{0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,1,2,0,0,0},
                    {0,0,0,2,1,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0}};

    // Chuyển nước đi thành tọa độ
    // VD: 45 -> ô map[3][4]
    public static int[] getPoint(int point) {
        int[] arr = {0, 0};
        point = Math.abs(point);
        arr[0] = point / 10;
        arr[1] = point % 10;
        return arr;
    }

    // Lấy tọa độ các ô đã đánh khi nhận được thông tin tử server
    public static void printPoint(int[] point) {
        for (int i = 0; i < lenMap; i++) {
            int[] arr = getPoint(point[i]);

            if (point[i] > 0){
                map[arr[0]-1][arr[1]-1] = 1;
            } else {
                map[arr[0]-1][arr[1]-1] = 2;
            }
        }
    }

    // Print Map
    public static void printMap(int[][] map){
        printPoint(point);
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println("");
        }
        System.out.println("-------------------------------------");
    }

    // Chuyển int thành byte
    public static byte[] convert_data(int data) {
        byte[] b = new byte[4];
        b[0] = (byte) data;
        b[1] = (byte) ((data >> 8) & 0xFF);
        b[2] = (byte) ((data >> 16) & 0xFF);
        b[3] = (byte) ((data >> 24) & 0xFF);
        return b;
    }

    // Chuyển byte thành int
    public static int restore(byte[] bytes) {
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
        for (int i = 0; i < 4; i++) out[i] = type_[i];
        for (int i = 4; i < 8; i++) out[i] = len_[i - 4];
        for (int i = 8; i < (8 + len); i++) out[i] = data[i - 8];

        return out;
    }

    // Mã hóa gói tin gửi nước đi (type = 4)
    public static byte[] pkt_turn(int id, int x, int y){
        byte[] out = new byte[8];

        for (int i = 0; i < 4; i++){
            out[i] = convert_data(id)[i];
        }
        for (int i = 4; i < 8; i++) {
            out[i] = convert_data(x*10+y)[i-4];
        }

        return out;
    }

    // Giải mã gói tin type = 3
    public static void restore_pkt(byte[] data) {
        lenMap = (data.length - 12)/4;
        byte[] black = Arrays.copyOfRange(data, 0, 4);  blackScore = restore(black);
        byte[] white = Arrays.copyOfRange(data, 4, 8);  whiteScore = restore(white);
        byte[] id = Arrays.copyOfRange(data, 8, 12);    ID = restore(id);
        for (int i = 0; i < (data.length - 12)/4; i++) {
            byte[] x = Arrays.copyOfRange(data, 12+i*4, 12+(i+1)*4);
            point[i] = restore(x);
        }
    }

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

    private static int[] autoMove() {
        int[] move = {-1, -1};
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (validMove(i, j)) {
                    move[0] = i;
                    move[1] = j;
                    return move;
                }
            }
        }
        return move;
    }

    public static void main(String[] args) {
        // Khởi tạo bộ đọc đầu vào từ bàn phím
        Scanner myObj = new Scanner(System.in);

        Board board = new Board("Player 1");
        board.view();
        int type = 0;
        int len = 0;
        int result;
        Socket skt = null;
        try {
            System.out.println("Client is Connecting....");
            // Lấy ip của máy tĩnh
            IP = InetAddress.getLocalHost().getHostAddress();

            skt = new Socket(IP, PORT);
            System.out.println(skt);
            System.out.println("Client is Connect");
            InputStream is = skt.getInputStream();
            OutputStream os = skt.getOutputStream();

            os.write(set_pkt(0, myPoint.length(), myPoint.getBytes()));

            while (true) {
                byte[] input = new byte[4];

                is.read(input);
                type = restore(input);
                is.read(input);
                len = restore(input);

                if (type == 1) {
                    is.read(input);
                    myID = restore(input);
                    System.out.println("ID: " + myID);
                    board.paint(map);
                    if (myID != 0) {
                        os.write(set_pkt(2, 4, convert_data(myID)));
                        JOptionPane.showMessageDialog(null, "Bạn là Đen!", "Xác nhận người chơi", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
                else if (type == 3) {
                    // Lấy phần data còn lại sau khi lấy ra type và len
                    byte[] out = new byte[len];
                    is.read(out);
                    // Giải mã để lấy các thông tin cần thiết
                    restore_pkt(out);
                    // Vẽ map sau khi nhận được thông tin
                    printMap(map);
                    board.paint(map);
                    if(ID == myID) {
//                        System.out.println("Nhập tọa độ x: ");
//                        int x = myObj.nextInt();
//                        System.out.println("Nhập tọa độ y: ");
//                        int y = myObj.nextInt();
                        int[] move = autoMove();
                        // Gửi gói tin chứa thông tin nước đi
                        os.write(set_pkt(4, 8, pkt_turn(myID, move[0]+1, move[1]+1)));
                    }
                }
                else if (type == 5) {
                    System.out.println("Nước đi của bạn không họp lệ!");

                    // Lấy phần data còn lại sau khi lấy ra type và len
                    byte[] out = new byte[len];
                    is.read(out);
                    // Giải mã để lấy các thông tin cần thiết
                    restore_pkt(out);
                    // Vẽ map sau khi nhận được thông tin
                    printMap(map);
                    board.paint(map);

                    if(ID == myID) {
//                        System.out.println("Nhập tọa độ x: ");
//                        int x = myObj.nextInt();
//                        System.out.println("Nhập tọa độ y: ");
//                        int y = myObj.nextInt();
                        int[] move = autoMove();
                        // Gửi gói tin chứa thông tin nước đi
                        os.write(set_pkt(4, 8, pkt_turn(myID, move[0]+1, move[1]+1)));
                    }
                }
                else if (type == 6) {
                    is.read(input); int id = restore(input);

                    if(id == myID) {
                        JOptionPane.showMessageDialog(null, "Bạn đã giành chiến thắng!", "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (id != myID && id != 0) {
                        JOptionPane.showMessageDialog(null, "Bạn đã thua!", "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else if (id == 0){
                        JOptionPane.showMessageDialog(null, "Hai bên hòa!", "Kết quả trận đấu", JOptionPane.INFORMATION_MESSAGE);
                    }
                }

//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                }
//                catch(InterruptedException e) {
//
//                }
            }
        } catch (IOException e) {
            System.out.print("Kết nối hỏng");
        }
    }
}