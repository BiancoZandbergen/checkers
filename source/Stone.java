import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * Stone Class
 */
public class Stone{
    /**
     * Stone's number.
     */
    private int stoneNumber;
    
    /**
     * Stone's color.
     */
    private Color color;
    
    /**
     * King status.
     */
    private boolean king = false;
    
    
    /**
     * Constructor.
     * @param theStoneNumber Stone's nummer.
     * @param theColor Stone's color.
     */
    public Stone(int theStoneNumber, Color theColor){
        assert theStoneNumber > 0 : "wrong stone number";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "wrong color";
        
        stoneNumber = theStoneNumber;
        color = theColor;
    }
    
    /**
     * Returns stone's nummer.
     * @return stone's nummer.
     */
    public int getStoneNumber(){
        return stoneNumber;
    }
    
    /**
     * Returns stone's color.
     * @return Stone's color.
     */
    public Color getColor(){
        return color;
    }
    
    /**
     * Returns stone's king status.
     * @return Stone's king status.
     */
    public boolean isKing(){
        return king;
    }
    
    /**
     * Sets stone's king status to true.
     */
    public void setToKing(){
        assert (!king) : "Is already a king!";
        
        king = true;
    }
}
