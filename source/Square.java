import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;


/**
 * Class representing a Square on the checker board
 */

public class Square{
    /**
     * ID of the square.
     */
    private SquareID squareID;
    
    /**
     * Stone in this square.
     */
    private Stone stone;
    
    /**
     * Constructor when only the squareID is given.
     * @param theSquareID SquareID of the Square
     */
    public Square(SquareID theSquareID){
        assert theSquareID != null : "SquareID is null";
        assert theSquareID.getColumn() > 0 && theSquareID.getColumn()<9 : "squareID out of range";
        assert theSquareID.getLine() > 0 && theSquareID.getLine()<9 : "squareID out of range";
        
        squareID = theSquareID;
    }
    
    /**
     * Returns the SquareID.
     * @return The SquareID of the object.
     */
    public SquareID getSquareID(){
        return squareID;
    }
    /**
     * Returns true when the stone is assigned.
     * @return Returns the assigned status of the stone.
     */
    public boolean isStoneAssigned(){
        return stone != null;
    }
    
    /**
     * Returns the status of the stone, king or not.
     * @return True when king, false when not.
     */
    public boolean isStoneKing(){
        return stone.isKing();
    }
    /**
     * Assigns a stone to the object.
     * @param theStone Stone to be assigned.
     */
    public void assignStone(Stone theStone){
            assert theStone != null : "stone is null";
            
            stone = theStone;
    }
    /**
     * Returns the stonenumber of the assigned stone.
     * @return Stone of assigned stone.
     */
    public int getAssignedStoneNumber(){
        return stone.getStoneNumber();
    }
        
    /**
     * Returns assigned stone's color.
     * @return Stones color.
     */
    public Color getAssignedStoneColor(){
        return stone.getColor();
    }
    
    /**
     * Sets the stone to null (unavailable).
     */
    public void unAssignStoneNumber(){
        stone = null;
    }
    
    /**
     * Returns assigned stone
     * @return The currently assigned stone
     */
    public Stone getStone() {
        return stone;
    }
}
