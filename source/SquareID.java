import java.io.*;

/**
 * Class storing the SquareID info.
 */

/**
 * A SquareID is representing the square coordinates of the checker board.
 * colums = x-Axis
 * lines  = y-Axis
 */
public class SquareID implements Serializable {
    /**
     * Squares's column on bord.
     */
    private int column;
    
    /**
     * Square's line on bord.
     */
    private int line;
    
    
    /**
     * Constructor used when column and line is known.
     * @param theColumn Square's column
     * @param theLine Square's line.
     */
    public SquareID(int theColumn, int theLine){
        line = theLine;
        column = theColumn;
    }
    
    /**
     * Return column of square
     * @return column of square
     */
    public int getColumn(){
        return column;
    }
    
    /**
     * Return line of square
     * @return line of square
     */
    public int getLine(){
        return line;
    }
    
    /**
     * Set the column and line of a square
     */
    public void setLocation(int theColumn, int theLine){
            line = theLine;
            column = theColumn;
                
    }
    /**
     * Return self
     */
    public SquareID getLocation(){
            return this;
    }
        
   /**
    * Check if two SquareID objects are equal.
    */
    public boolean equals(Object obj) {
        assert obj != null : "object is null";
            
        if (obj instanceof SquareID) {
            SquareID id = (SquareID)obj;
            return (column == id.getColumn()) && (line == id.getLine());                
        }
        return false;
    }
}