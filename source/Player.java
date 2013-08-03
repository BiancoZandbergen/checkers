import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * Player class
 */
public class Player{
    /**
     * Color of the player's stones.
     */
    private Color color;
    
    /**
     * Status of turn.
     */
    private boolean turn = false; // false by default
    
    /**
     * Number of victories.
     */
    private int victories = 0;
    
    /**
     * Number of captured stones.
     */
    private int capturedStones = 0;
    

    /**
     * Constructor for player.
     * @param theColor Players color.
     */
    public Player(Color theColor){
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "wrong Color";
        
        color = theColor;
       
        // White starts
        if(color.equals(Color.WHITE)){
            turn = true;
        }
    }
    

    /**
     * Returns the color of the player.
     * @return Color of player.
     */
    public Color getColor(){
        return color;
    }

    /**
     * Sets the player's color.
     * @param theColor Color to be set.
     */
    public void setColor(Color theColor){
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "wrong Color";
        
        color = theColor;        
    }
    
    /**
     * Returns the status of player's turn.
     * @return Players turn status.
     */
    public boolean isTurn(){
        return turn;
    }
    
    /**
     * Changes players turn status.
     */
    public void changeTurn(){
        turn = !turn;
    }
    
    /**
     * Returns the nummer of victories.
     * @return Number of victories.
     */
    public int getVictories(){
        return victories;
    }
    
    /**
     * Increases number of victories of player.
     */
    public void increaseVictories(){
        victories = victories + 1;
    }
    
    /**
     * Returns number of captured stones.
     * @return Nummer of victories.
     */
    public int getCapturedStones(){
        return capturedStones;
    }
    
    /**
     * Increase nummer of captured stones.
     */
    public void increaseCapturedStones(){
        capturedStones = capturedStones + 1;
    }
    
    /**
     * Reset nummer of captured stones.
     */
    public void resetCapturedStones(){
        capturedStones = 0;
    }
}
