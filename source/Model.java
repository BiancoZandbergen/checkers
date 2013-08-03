import java.beans.*;
import java.util.*;
import java.awt.Color;

/**
 * Interface for the Model classes.
 */
public interface Model {

    public boolean existSquare(SquareID theSquareID);

    public Set<SquareID> getStonePositions();
    
    public Set<SquareID> getPossibleDestinations(SquareID currentSquareID);

    public void addChatMessage(boolean mustSync, int playerNumber, String theMessage);

    public void captureStoneOnSquare(boolean mustSync, SquareID thisSquareID);

    public void closeGame(boolean mustSync, String theReason);

    public void exitGame();
    
    public int getCapturedStones(int thePlayerNumber);

    public Color getColorOfPlayer (int thePlayerNumber);

    public boolean isPlayerAtTurn(int thePlayerNumber);

    public String getRandomShoutmessage(String theType);

    public int getStoneNumberOnSquare(SquareID theSquareID);

    public int getVictories(int thePlayerNumber);

    public boolean isStoneAssignedToSquare(SquareID theSquareID);

    public void offerRemise(boolean mustSync, int thePlayerNumber);

    public void replyToRemise(boolean mustSync, boolean accept);

    public void setStoneOnSquare(boolean mustSync, SquareID fromSquareID, SquareID toSquareID);

    public void setWinGame(boolean mustSync, int thePlayerNumber);

    public Color getColorOfStone(int theStoneNumber);

    public void addPropertyChangeListener(PropertyChangeListener a);

    public void removePropertyChangeListener(PropertyChangeListener a);

}
