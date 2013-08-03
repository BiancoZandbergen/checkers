import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;

/**
 * Visual representation of Stone class.
 */ 
public class Stone2D extends JComponent {
    
    /**
     * king status, true if stone is a king.
     */
    private boolean king = false;
    
    /**
     * Holds the image for this stone.
     */
    private Image stoneImage;
    
    /**
     * Holds the path of the image location.
     */
    private String imageLocation;
    
    /**
     * Holds the color of the stone (WHITE or BLACK).
     */
    private Color color;
    
    /**
     * Holds the StoneNumber identificator.
     */
    private int stone2DNumber;
    
    /**
     * Dimension value of the Component.
     */
    public static final int COMPONENT_DIMENSION_VALUE = 40;
    /**
     * Dimension Value of the Stone Image.
     */
    public static final int STONE_DIMENSION_VALUE = 36;
    
    /**
     * Constructor for Stone2D
     * @param theColor The string to define wether the stone must be black or white.
     *        "black" and "white" are the only possible values for color.
     * @param thePosition Position object to position the Stone2D on the panel.
     * thePosition can't be null. 
     * @param theStoneNumber int of the equivalent stone number in the Model
     */
    public Stone2D(int theStoneNumber, Color theColor, Point thePosition) {
        assert theStoneNumber > 0 : "wrong stone number";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "wrong color";
        assert thePosition != null : "point is null";
        
       stone2DNumber = theStoneNumber;
        color = theColor;
        // set value for image location
        if (color.equals(Color.WHITE)) imageLocation = "files/stoneWhite.gif";
        else if (color.equals(Color.BLACK)) imageLocation = "files/stoneBlack.gif";
        // try to load image
        try { stoneImage = ImageIO.read(this.getClass().getResource(imageLocation)); }
        catch ( IOException e ) { e.printStackTrace(); }

        setLocation((int)thePosition.getX()-COMPONENT_DIMENSION_VALUE/2,
                    (int)thePosition.getY()-COMPONENT_DIMENSION_VALUE/2);
        
        setSize(COMPONENT_DIMENSION_VALUE,COMPONENT_DIMENSION_VALUE);
        setOpaque(false);
    }


    /**
     * Sets king to true. Checks which color the stone is and loads new king image as stoneImage.
     * Repaints Stone2D.
     */
    public void setToKing(){
        king = true;
        
        // set value for image location
        if (color.equals(Color.WHITE)) imageLocation = "files/stoneWhiteKing.gif";
        else if (color.equals(Color.BLACK)) imageLocation = "files/stoneBlackKing.gif";
        // try to load image
        try { stoneImage = ImageIO.read(this.getClass().getResource(imageLocation)); }
        catch ( IOException e ) { e.printStackTrace(); }
        
        repaint();
    }
    
    /**
     * Returns boolean king.
     * @return if Stone2D is king or not.
     */
    public boolean isKing() {
        return king;
    }
    
    /**
     * returns the stone number
     * @return int the stone Number
     */
    public int getStone2DNumber(){
        return stone2DNumber;
    }
    
    /**
     * returns the Stone color
     * @return Color the stone Color
     */
    public Color getStoneColor(){
        return color;
    }
    
    /**
     * Paints the component.
     * @param g Graphics object for painting
     */
    public void paintComponent(Graphics g) {
        g.drawImage(stoneImage, getX()+2, getY()+3, STONE_DIMENSION_VALUE, STONE_DIMENSION_VALUE, null);      
    }
    
    /**
     * Sets the position to the given position
     * @param p Point object containing the X and Y value corresponding to the desired position.
     */
    public void setPosition(Point p) {
        assert p != null : "point is null";
        
        setLocation((int)p.getX()-COMPONENT_DIMENSION_VALUE/2,(int)p.getY()-COMPONENT_DIMENSION_VALUE/2);
        repaint();
    }
}