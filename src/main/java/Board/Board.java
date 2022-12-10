package Board;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 *
 * @author Pete Cappello
 */
public class Board
{
    // model attribute
    private int boardSize;  // number of rows & columns in the board

    // view attributes
    private int squareSize; // number of pixels in a square's edge

    /* For this use case, we may reasonably make the objects below attributes of
     * the CheckerBoard class. However, if this class is used by other classes,
     * some or all of the attributes below probably would best be thought of as
     * NOT being attributes of this class.
     */
    private Image image;
    private ImageIcon imageIcon;
    private JLabel jLabel;
    private JFrame jFrame;
    private JLabel statusLabel;

    public static int CELL_SIZE = 65;

    public Board(String boardName)
    {
        boardSize  = 8;
        squareSize = CELL_SIZE;
        int imageSize = boardSize * squareSize;
        image = new BufferedImage( imageSize, imageSize, BufferedImage.TYPE_INT_ARGB );
        imageIcon = new ImageIcon( image );
        jLabel = new JLabel( imageIcon );
        jFrame = new JFrame( boardName );
        jFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        Container container = jFrame.getContentPane();
        container.setLayout( new BorderLayout() );
        container.add( jLabel, BorderLayout.CENTER );
        jFrame.pack();
    }

//    public static void main(String[] args)
//    {
//        Board checkerBoard = new Board();
//        checkerBoard.paint(map);
//        checkerBoard.view();
//    }

    private int getInt( String question )
    {
        String intString = JOptionPane.showInputDialog( question );
        return Integer.parseInt( intString );
    }

    /**
     * Paint the checker board onto the Image.
     */
    public void paint(int[][] map)
    {
        Graphics graphics = image.getGraphics();

        graphics.setColor(Color.GREEN);
        graphics.fillRect(0, 0, CELL_SIZE*8, CELL_SIZE*8); //Board Background
        graphics.setColor(Color.BLACK);
        for (int i = 0; i < 8; ++i) // vertical grid lines
            graphics.drawLine(i*CELL_SIZE, 0, i*CELL_SIZE, 8*CELL_SIZE);
        for (int i=0; i < 8; ++i) { // horizontal grid lines
            graphics.drawLine(0, i*CELL_SIZE, 8*CELL_SIZE, i*CELL_SIZE);
        }

        for (int col = 0; col < 8; col++){
            for (int row = 0; row < 8; row++){
                if (map[row][col] == 1) {
                    graphics.setColor(Color.BLACK);
                    graphics.fillOval(col*CELL_SIZE, row*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                } else if (map[row][col] == 2) {
                    graphics.setColor(Color.WHITE);
                    graphics.fillOval(col*CELL_SIZE, row*CELL_SIZE, CELL_SIZE, CELL_SIZE);
                }
            }
        }
        jFrame.validate();
        jFrame.repaint();

//        jLabel.addMouseListener(new MouseAdapter() {
//            public void mouseClicked(MouseEvent e) {
//                System.out.println(e.getX() + " " + e.getY());
//            }
//        });
    }

    public void view() { jFrame.setVisible( true ); }
}