import java.beans.*;
import java.util.*;
import java.awt.Color;

/**
 * 
 * The model class holds all stored data and logic of the game.
 */
public class CheckerModel implements Model{

    /**
    * property Change Service
    */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
    * the belonging Soket
    */
    private DefaultSocket socket;
    
    /**
    * Map holding the stones of the game
    */
    private Map <Integer, Stone> stones = new HashMap();
    
    /**
    * 2 dimensional Array 8x8 gepresenting the bord squares
    */
    private Square[][] boardMatrix = new Square[8][8];
    
    
    /**
     * map with positive, neutral and negative shout messages!
     * massages are in a array.
     */
    private Map <String, String[]> shoutMessages = new HashMap();
    
    /**
    * ArrayList with the send chat messages
    */
    private List <ChatMessage> chatMessages = new LinkedList();
    
    /**
    * holding the Player object of player 1
    */
    private Player player1;
    
    /**
    * holding the Player object of player 2
    */
    private Player player2;
    
    /**
     * Holds if the player has captured a stone in a session
     */
    private boolean hasCapturedStoneInSession = false;
    
    /**
     * holds the SquareID of the last moved stone
     */
    private SquareID lastMoved = null;

    /**
     * value for debugging mode. 
     * if true debugging output will be print to console
     * and it is possible to set up a situation
     */
    private boolean debug = false;

    /**
     * Create CheckerModel object
     * @param s belonging socket
     */
    public CheckerModel(DefaultSocket s){
        socket = s;
        player1 = new Player(Color.WHITE);
        player2 = new Player(Color.BLACK);
        makeShoutMessages();
        makeSquares();
        newGame();

        if (debug) debug();
    }
    
    /**
     * debug method for setting up a test scenario
     */
    private void debug() {
        // deleting stone positions
        Set <SquareID> set = this.getStonePositions();
        for (SquareID id : set) {
                stones.remove(getSquareOfSquareID(id).getAssignedStoneNumber());
                getSquareOfSquareID(id).unAssignStoneNumber();
        }

        // creating stones for test senario
        stones.put(1, new Stone(1, Color.WHITE));
        stones.put(2, new Stone(2, Color.WHITE));
        stones.put(3, new Stone(3, Color.WHITE));
        stones.put(4, new Stone(4, Color.BLACK));
        stones.put(5, new Stone(5, Color.BLACK));

        // putting stones on squares for test senario
        getSquareOfSquareID(new SquareID(3, 3)).assignStone(stones.get(1));
        getSquareOfSquareID(new SquareID(5, 3)).assignStone(stones.get(2));
        getSquareOfSquareID(new SquareID(8, 2)).assignStone(stones.get(3));
        getSquareOfSquareID(new SquareID(8, 4)).assignStone(stones.get(4));
        getSquareOfSquareID(new SquareID(3, 5)).assignStone(stones.get(5));
    }

    /**
     * for debugging
     * prints out all squares containing stones incl. color of the stone
     */
    private void giveSquaresInConsole(){
        System.out.println("### Executing ### giveSquaresInConsole");
        int x;
        int y;
        for (x=0;x<=7;x++){
            for (y=0;y<=7;y++){
                if (boardMatrix[x][y].isStoneAssigned()){
                    System.out.println(boardMatrix[x][y].getSquareID().getColumn()+" and "+boardMatrix[x][y].getSquareID().getLine()+" Color: "+boardMatrix[x][y].getAssignedStoneColor().toString() );
                }                    
            }
        }
    }

    /**
     * Creates all squares with correct SquareID and puts them in the array.
     */        
    private void makeSquares(){
        int x;
        int y;
        
        for (y=1; y<=8; y++){
            for (x=1; x<=8; x++){
                boardMatrix[x-1][y-1] = new Square(new SquareID(x, y));
            }
        }
    }

    /**
     * Returns the Square matching to the given SquareID
     * @param theSquareID SquareID
     * @return Square matching Square
     */
    private Square getSquareOfSquareID(SquareID theSquareID){
        assert theSquareID != null : "SquareID is null";
        assert theSquareID.getColumn() > 0 && theSquareID.getColumn()<9 : "squareID out of range";
        assert theSquareID.getLine() > 0 && theSquareID.getLine()<9 : "squareID out of range";
        
        int column = theSquareID.getColumn() - 1;
        int line = theSquareID.getLine() - 1;
        
        return boardMatrix[column][line];
    }
    
    /**
     * Returns true if the Square matching to the given SquareID exists
     * @param theSquareID SquareID
     * @return boolean
     */
    public boolean existSquare(SquareID theSquareID){
        assert theSquareID != null : "SquareID is null";
        
        int x;
        int y;
        
        for (x=0; x<=7; x++) {
            for (y=0; y<=7; y++) {
                if (boardMatrix[x][y] != null &&
                    boardMatrix[x][y].getSquareID().equals(theSquareID)) 
                        return true; 
            }
        }

        return false;
}
    
    /**
     * calculate all possible destinations and returns a set with squareIDs:
     * - all destinations for the stone
     * - if a stone must cature, only the stone which needs to be captured
     * - if the selected stone can not move, all stones who can move
     * @param theID SquareID of the selected Stone
     * @return set with SquareIDs
     */
    public Set getPossibleDestinations(SquareID theID) {
        assert theID != null : "SquareID is null";
        assert theID.getColumn() > 0 && theID.getColumn()<9 : "squareID out of range";
        assert theID.getLine() > 0 && theID.getLine()<9 : "squareID out of range";
        
        if (debug) System.out.println("### Executing ### getPossibleDestinations: for ID: "+ theID.getColumn() + " " +theID.getLine());            
        if (debug) if (existSquare(theID)) System.out.println("square exists");
        if (debug) if (getSquareOfSquareID(theID).isStoneAssigned()){ System.out.println("square has stone");} else{ System.out.println("square has no stone");}
        
        Color theColor;
        theColor = getSquareOfSquareID(theID).getAssignedStoneColor();
        if (debug) System.out.println("### Executing ### getPossibleDestinations: after assigning color: "+ theColor.toString());        
    
        if (!hasCapturedStoneInSession) {
            if (debug) System.out.println("### Executing ### getPossibleDestinations: if !hasCapturedStone");
            if (canMoveStone(theID, theColor)) {
                if (debug) System.out.println("### Executing ### getPossibleDestinations: if canMoveStone");
                if (canCaptureStones(theColor)) { 
                    if (debug) System.out.println("### Executing ### getPossibleDestinations: if canCaptureStones");
                    if (canSquareCaptureStones(theID, theColor)) { 
                        if (debug) System.out.println("### Executing ### getPossibleDestinations: if canSquareCaptureStones");
                        return getCaptureableDestinations(theID, theColor);
                    }else{
                        if (debug) System.out.println("### Executing ### getPossibleDestinations: else canSquareCaptureStones");
                        return getCaptureableStones(theColor);
                    }
                }else{
                    if (debug) System.out.println("### Executing ### getPossibleDestinations: else canCaptureStone");
                    return getDestinationsWithoutCapture(theID, theColor);
                }
            }else{
                if (debug)System.out.println("### Executing ### getPossibleDestinations: else canMoveStones");       
                return getMovableStones(theColor);
            }
        }else{
            if (debug)System.out.println("### Executing ### getPossibleDestinations: else !hasCapturedStones");    
            if (hasCapturedStoneInSession && canSquareCaptureStones(lastMoved, theColor)) {
                if (debug)System.out.println("### Executing ### getPossibleDestinations: if captureStoneInSession and canCaptureStone");     
                return getCaptureableDestinations(lastMoved, theColor);
            }
        }
        
        return new HashSet<SquareID>();
    }
    
    /**
     * Gives all SquareID's back on which are stones standing with the given stone color.
     * @param stoneColor The color of the stones, from which you want the SquareID's
     * @return Set with SquareID's on which are stones with the given color.
     */
    private Set getSquaresWithStones(Color stoneColor) {
        assert stoneColor != null : "color is null";
        assert stoneColor.equals(Color.BLACK) || stoneColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug) System.out.println("### Executing ### getSquareWithStones");

        Set<SquareID> results = new HashSet<SquareID>();
        
        for (int i=0; i<8; i++) {
            for (int j=0; j<8; j++) {
                if (boardMatrix[i][j] != null &&
                    boardMatrix[i][j].isStoneAssigned() &&
                    boardMatrix[i][j].getAssignedStoneColor().equals(stoneColor)) {
                        results.add(boardMatrix[i][j].getSquareID());
                }
            }
        }
    
        return results;
    }
    
    /**
    * Returns a set of SquareID's with stones that can move
    * @param theColor the color of the stones that can move
    * @return A Set with SquareID's of Squares on which are stones that can move
    */
    private Set getMovableStones(Color theColor) {
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        Set<SquareID> stones = getSquaresWithStones(theColor);
        Set<SquareID> results = new HashSet<SquareID>();

        for (SquareID stone : stones) {
            if (canMoveStone(stone, theColor)) {
                results.add(stone);
            }
        }
        
        return results;
    }
    
    /**
     * Checks if stone can move to another position
     * @param theID the SquareID from Square containing the stone.
     * @param theColor the color who's turn it is
     * @return Returns a boolean whether the stone can move or not.
     */
    private boolean canMoveStone(SquareID theID, Color theColor) {
        assert theID != null : "SquareID is null";
        assert theID.getColumn() > 0 && theID.getColumn()<9 : "squareID out of range";
        assert theID.getLine() > 0 && theID.getLine()<9 : "squareID out of range";
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug)System.out.println("### Executing ### canMoveStone");

        int jumper;
        int jumperback;
        Color oppositeColor;
        
        if(theColor.equals(Color.WHITE)){
            jumper = 1;
            jumperback = -1;
            oppositeColor = Color.BLACK;
        }else{
            jumper = -1;
            jumperback = 1;
            oppositeColor = Color.WHITE;
        }

        SquareID leftID = new SquareID(theID.getColumn()-1, theID.getLine()+jumper);
        SquareID rightID = new SquareID(theID.getColumn()+1, theID.getLine()+jumper);

        // check if normal stone can move
        if (!getSquareOfSquareID(theID).isStoneKing()) {
            if ((theID.getColumn() > 1 && 
                    existSquare(leftID) && 
                    !isStoneAssignedToSquare(leftID)) ||
                (theID.getColumn() < 8 && 
                    existSquare(rightID) && 
                    !isStoneAssignedToSquare(rightID))) {
                
                return true;
                
            } else {
                return canSquareCaptureStones(theID, theColor);
            }
        }else{
            SquareID leftIDBack = new SquareID(theID.getColumn()-1, theID.getLine()+jumperback);
            SquareID rightIDBack = new SquareID(theID.getColumn()+1, theID.getLine()+jumperback);
            if ((theID.getColumn() > 1 && 
                    existSquare(leftID) && 
                    !isStoneAssignedToSquare(leftID)) ||
                (theID.getColumn() > 1 && 
                    existSquare(leftIDBack) && 
                    !isStoneAssignedToSquare(leftIDBack)) ||
                (theID.getColumn() < 8 && 
                    existSquare(rightID) && 
                    !isStoneAssignedToSquare(rightID)) ||
                (theID.getColumn() < 8 && 
                    existSquare(rightIDBack) && 
                    !isStoneAssignedToSquare(rightIDBack))) {

                return true;
            } else {
                return canSquareCaptureStones(theID, theColor);                 
            }   
        }
    }

    /**
     * Checks if a square can capture an enemy stone.
     * @param theID The SquareID to check
     * @param theColor the Color who's turn it is
     * @return Boolean whether the square can capture a stone or not.
     */
    private boolean canSquareCaptureStones(SquareID theID, Color theColor) {
        assert theID != null : "SquareID is null";
        assert theID.getColumn() > 0 && theID.getColumn()<9 : "squareID out of range";
        assert theID.getLine() > 0 && theID.getLine()<9 : "squareID out of range";
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug)System.out.println("### Executing ### canSquareCaptureStones: "+theID.getColumn()+","+theID.getLine()+" color: "+ theColor.toString());
        // init vars
        int jumper; // for capturing forwards
        int jumperback; // for capturing backwards 
        Color oppositeColor;
        
        if(theColor.equals(Color.WHITE)){
            jumper = 1;
            jumperback = -1;
            oppositeColor = Color.BLACK;
        } else {
            jumper = -1;
            jumperback = 1;
            oppositeColor = Color.WHITE;
        }

        // check for normal stones
        if (!getSquareOfSquareID(theID).isStoneKing() || 
            (getSquareOfSquareID(theID).isStoneKing() && 
            hasCapturedStoneInSession)) {
            
            // SquareID's to check for capturing forwards
            SquareID leftID = new SquareID(theID.getColumn()-1, theID.getLine()+(1*jumper));
            SquareID lefterID = new SquareID(theID.getColumn()-2, theID.getLine()+(2*jumper));
            SquareID rightID = new SquareID(theID.getColumn()+1, theID.getLine()+(1*jumper));
            SquareID righterID = new SquareID(theID.getColumn()+2, theID.getLine()+(2*jumper));
            
            // SquareID's to check for captuing backwards
            SquareID leftBackID = new SquareID(theID.getColumn()-1, theID.getLine()+(1*jumperback));
            SquareID lefterBackID = new SquareID(theID.getColumn()-2, theID.getLine()+(2*jumperback));
            SquareID rightBackID = new SquareID(theID.getColumn()+1, theID.getLine()+(1*jumperback));
            SquareID righterBackID = new SquareID(theID.getColumn()+2, theID.getLine()+(2*jumperback));

            // check if there is a left stone captureable
            if (theID.getColumn() > 2 &&
                existSquare(leftID) &&
                isStoneAssignedToSquare(leftID) &&
                getSquareOfSquareID(leftID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(lefterID) &&
                !isStoneAssignedToSquare(lefterID)) {
                
                if (debug) System.out.println("Result: TRUE");
                return true;
            }

            // check if there is a right stone captureable
            if (theID.getColumn() < 7 && 
                existSquare(rightID) &&
                isStoneAssignedToSquare(rightID) &&
                getSquareOfSquareID(rightID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(righterID) &&
                !isStoneAssignedToSquare(righterID)) {
                
                if (debug)System.out.println("Result: TRUE");
                return true;
            }

            // check if there is a right stone backwards captureable
            if (hasCapturedStoneInSession &&
                theID.getColumn() < 7 && 
                existSquare(rightBackID) &&
                isStoneAssignedToSquare(rightBackID) &&
                getSquareOfSquareID(rightBackID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(righterBackID) &&
                !isStoneAssignedToSquare(righterBackID)) {
                
                if (debug)System.out.println("Result: TRUE");
                return true;
            }

            // check if there is a left stone backwards captureable
            if (hasCapturedStoneInSession &&
                theID.getColumn() < 7 && 
                existSquare(leftBackID) &&
                isStoneAssignedToSquare(leftBackID) &&
                getSquareOfSquareID(leftBackID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(lefterBackID) &&
                !isStoneAssignedToSquare(lefterBackID)) {
                
                if (debug)System.out.println("Result: TRUE");
                return true;
            }
        } else if (getSquareOfSquareID(theID).isStoneKing() && 
                    !hasCapturedStoneInSession) {

            int y;
            // check left up
            if (theID.getColumn() > 2 && theID.getLine() < 7){
                if (debug)System.out.println("## Checking left up ##");
                y = theID.getLine()+1;

                for (int x = theID.getColumn()-1; x > 1; x--) {
                    SquareID tempIDLeft = new SquareID(x, y);
                    SquareID tempIDLefter = new SquareID(x-1, y+1);

                    if (y < 8 &&
                        existSquare(tempIDLeft) &&
                        getSquareOfSquareID(tempIDLeft).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDLeft).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDLefter) &&
                        !getSquareOfSquareID(tempIDLefter).isStoneAssigned()) {

                        if (debug)System.out.println("## TRUE ##");
                        return true;
                    }
                    
                    y++;
                }
            }
            // check left down
            if (theID.getColumn() > 2 && theID.getLine() > 2) {
                if (debug)System.out.println("## Checking left down ##");
                y = theID.getLine()-1;
                
                for (int x = theID.getColumn()-1; x > 1; x--) {
                    SquareID tempIDLeftBack = new SquareID(x, y);
                    SquareID tempIDLefterBack = new SquareID(x-1, y-1);
                    if (debug)System.out.println("## Checking ## "+tempIDLeftBack.getColumn()+","+tempIDLeftBack.getLine());
                    if (y > 1 &&
                        existSquare(tempIDLeftBack) &&
                        getSquareOfSquareID(tempIDLeftBack).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDLeftBack).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDLefterBack) &&
                        !getSquareOfSquareID(tempIDLefterBack).isStoneAssigned()) {

                        if (debug)System.out.println("## TRUE ##");
                        return true;
                    }
                    y--;
                }
            }

            // check right up
            if (theID.getColumn() < 7 && theID.getLine() < 7) {
                if (debug)System.out.println("## Checking right up ##");
                y = theID.getLine()+1;
                          
                for (int x = theID.getColumn()+1; x < 8; x++){
                    SquareID tempIDRight = new SquareID(x, y);
                    SquareID tempIDRighter = new SquareID(x+1, y+1);
                    if (debug)System.out.println("## Checking ## "+tempIDRight.getColumn()+","+tempIDRight.getLine());
                    if (y < 8 &&
                        existSquare(tempIDRight) &&
                        getSquareOfSquareID(tempIDRight).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDRight).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDRighter) &&
                        !getSquareOfSquareID(tempIDRighter).isStoneAssigned()) {

                        if (debug)System.out.println("## TRUE ##");
                        return true;
                    }
                    y++;
                }
            }

            // check right down
            if (theID.getColumn() < 7 && theID.getLine() > 2) {
                if (debug)System.out.println("## Checking right down ##");
                y = theID.getLine()-1;
                          
                for (int x = theID.getColumn()+1; x < 8; x++){
                    SquareID tempIDRightBack = new SquareID(x, y);
                    SquareID tempIDRighterBack = new SquareID(x+1, y-1);
                    if (debug)System.out.println("## Checking ## "+tempIDRightBack.getColumn()+","+tempIDRightBack.getLine());
                    if (y > 1 &&
                        existSquare(tempIDRightBack) &&
                        getSquareOfSquareID(tempIDRightBack).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDRightBack).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDRighterBack) &&
                        !getSquareOfSquareID(tempIDRighterBack).isStoneAssigned()) {

                        if (debug)System.out.println("## TRUE ##");
                        return true;
                    }
                    y--;
                }
            }

        }
        if (debug)System.out.println("## FALSE ##");
        return false;        
    }
    
    /**
     * Checks if any stone can move, there is a winner when no stone can move
     * @param theColor The color of the side to check
     * @return Returns a boolean true if any stone with the given color can move.
     */
    private boolean canMoveAStone(Color theColor) {
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";    
        
        if (debug)System.out.println("### Executing ### canMoveAStone");
        Set<SquareID> squaresWithStones = getSquaresWithStones(theColor);
        for (SquareID id : squaresWithStones) {
            if (canMoveStone(id, theColor)) {
                if (debug)System.out.println("### Executing ### canMoveAStone: return true");
                return true;
            }
        }
        if (debug)System.out.println("### Executing ### canMoveAStone: return false");
        return false;
    }
    
    /**
     * Checks if any stone can capture an enemy stone
     * @param theColor The color of the side to check
     * @return Returns a boolean true if any stone of the given color can capture an enemy stone.
     */
    private boolean canCaptureStones(Color theColor) {
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug)System.out.println("### Executing ### canCaptureStones");
        
        Set<SquareID> squaresWithStones = getSquaresWithStones(theColor);
        for (SquareID id : squaresWithStones) {
            if (canSquareCaptureStones(id, theColor)) {
                return true;
            }
        }
        return false;        
    }
    
    /**
     * Gives back the possible first end destinations (temp) for all possible capture sessions
     * @param theID The SquareID of Square containing Stone that can capture
     * @param theColor the Color who's turn it is
     * @return Set with possible destinations
     */
    private Set getCaptureableDestinations(SquareID theID, Color theColor) {
        assert theID != null : "SquareID is null";
        assert theID.getColumn() > 0 && theID.getColumn()<9 : "squareID out of range";
        assert theID.getLine() > 0 && theID.getLine()<9 : "squareID out of range";
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug)System.out.println("### Executing ### getCaptureableDestinations");
        // init vars
        Set<SquareID> destinations = new HashSet<SquareID>();
        int jumper; // jumper for capturing forwards
        int jumperback; // jumper for captuing backwards
        Color oppositeColor;
        if(theColor.equals(Color.WHITE)){
            jumper = 1;
            jumperback = -1;
            oppositeColor = Color.BLACK;
        }else{
            jumper = -1;
            jumperback = 1;
            oppositeColor = Color.WHITE;
        }

        // check for normal stones
        if (!getSquareOfSquareID(theID).isStoneKing() ||
            (getSquareOfSquareID(theID).isStoneKing() && 
            hasCapturedStoneInSession)) {
            
            if (debug)System.out.println("### Executing ### getCaptureableDestinations: in if not king or king and captured stone");      
            
            // SquareId's to check for capturing fowards
            SquareID leftID = new SquareID(theID.getColumn()-1, theID.getLine()+(1*jumper));
            SquareID lefterID = new SquareID(theID.getColumn()-2, theID.getLine()+(2*jumper));
            SquareID rightID = new SquareID(theID.getColumn()+1, theID.getLine()+(1*jumper));
            SquareID righterID = new SquareID(theID.getColumn()+2, theID.getLine()+(2*jumper));
            
            // SquareId's to check for capturing fowards
            SquareID leftBackID = new SquareID(theID.getColumn()-1, theID.getLine()+(1*jumperback));
            SquareID lefterBackID = new SquareID(theID.getColumn()-2, theID.getLine()+(2*jumperback));
            SquareID rightBackID = new SquareID(theID.getColumn()+1, theID.getLine()+(1*jumperback));
            SquareID righterBackID = new SquareID(theID.getColumn()+2, theID.getLine()+(2*jumperback));
            
            // check if there is a left stone forwards captureable
            if (debug)System.out.println("### Executing ### getCaptureableDestinations: before left stone forward cap");
            if (theID.getColumn() > 2 &&
                existSquare(leftID) &&
                isStoneAssignedToSquare(leftID) &&
                getSquareOfSquareID(leftID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(lefterID) &&
                !isStoneAssignedToSquare(lefterID)) {

                if (debug)System.out.println("### Executing ### getCaptureableDestinations: left stone forward cap");
                destinations.add(lefterID);
            }

            if (debug)System.out.println("### Executing ### getCaptureableDestinations: before right stone forward cap");
            // check if there is a right stone forwards captureable
            if (theID.getColumn() < 7 &&
                existSquare(rightID) &&
                isStoneAssignedToSquare(rightID) &&
                getSquareOfSquareID(rightID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(righterID) &&
                !isStoneAssignedToSquare(righterID)) {

                if (debug)System.out.println("### Executing ### getCaptureableDestinations: right stone forward cap");
                destinations.add(righterID);
            }

            if (debug)System.out.println("### Executing ### getCaptureableDestinations: befor left stone backward cap");  
            // check if there is a left stone backwards captureable
            if (hasCapturedStoneInSession && 
                theID.getColumn() > 2 &&
                existSquare(leftBackID) &&
                isStoneAssignedToSquare(leftBackID) &&
                getSquareOfSquareID(leftBackID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(lefterBackID) &&
                !isStoneAssignedToSquare(lefterBackID)) {

                if (debug)System.out.println("### Executing ### getCaptureableDestinations: left stone backward cap");
                destinations.add(lefterBackID);
            }

            if (debug)System.out.println("### Executing ### getCaptureableDestinations: before right stone backward cap");
            // check if there is a right stone backwards captureable
            if (hasCapturedStoneInSession &&
                theID.getColumn() < 7 &&
                existSquare(rightBackID) &&
                isStoneAssignedToSquare(rightBackID) &&
                getSquareOfSquareID(rightBackID).getAssignedStoneColor().equals(oppositeColor) &&
                existSquare(righterBackID) &&
                !isStoneAssignedToSquare(righterBackID)) {

                if (debug)System.out.println("### Executing ### getCaptureableDestinations: right stone backward cap");
                destinations.add(righterBackID);
            }
        } else if (getSquareOfSquareID(theID).isStoneKing() && 
                !hasCapturedStoneInSession) {
            if (debug)System.out.println("### Executing ### getCaptureableDestinations: if king and  not captured stone"); 
            int y;

            // check left up            
            if (theID.getColumn() > 2 && theID.getLine() < 7){
                if (debug)System.out.println("# checking left up #");
                y = theID.getLine()+1;

                for (int x = theID.getColumn()-1; x > 1; x--) {
                    SquareID tempIDLeft = new SquareID(x, y);
                    SquareID tempIDLefter = new SquareID(x-1, y+1);

                    if (y < 8 &&
                        existSquare(tempIDLeft) &&
                        getSquareOfSquareID(tempIDLeft).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDLeft).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDLefter) &&
                        !getSquareOfSquareID(tempIDLefter).isStoneAssigned()) {

                        destinations.add(tempIDLefter);
                    }
                    y++;
                }
            }

            // check left down
            if (theID.getColumn() > 2 && theID.getLine() > 2) {

                if (debug)System.out.println("# checking left down #");
                y = theID.getLine()-1;

                for (int x = theID.getColumn()-1; x > 1; x--) {
                    SquareID tempIDLeftBack = new SquareID(x, y);
                    SquareID tempIDLefterBack = new SquareID(x-1, y-1);

                    if (y > 1 &&
                        existSquare(tempIDLeftBack) &&
                        getSquareOfSquareID(tempIDLeftBack).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDLeftBack).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDLefterBack) &&
                        !getSquareOfSquareID(tempIDLefterBack).isStoneAssigned()) {

                        destinations.add(tempIDLefterBack);
                    }
                    y--;
                }
            }

            // check right up
            if (theID.getColumn() < 7 && theID.getLine() < 7) {
                if (debug)System.out.println("# checking right up #");
                y = theID.getLine()+1;

                for (int x = theID.getColumn()+1; x < 8; x++){
                    SquareID tempIDRight = new SquareID(x, y);
                    SquareID tempIDRighter = new SquareID(x+1, y+1);

                    if (y < 8 &&
                        existSquare(tempIDRight) &&
                        getSquareOfSquareID(tempIDRight).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDRight).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDRighter) &&
                        !getSquareOfSquareID(tempIDRighter).isStoneAssigned()) {

                        destinations.add(tempIDRighter);
                    }
                    y++;
                }
            }

            // check right down
            if (theID.getColumn() < 7 && theID.getLine() > 2) {
                if (debug)System.out.println("# checking right down #");
                y = theID.getLine()-1;

                for (int x = theID.getColumn()+1; x < 8; x++){
                    SquareID tempIDRightBack = new SquareID(x, y);
                    SquareID tempIDRighterBack = new SquareID(x+1, y-1);
                    if (y > 1 &&
                        existSquare(tempIDRightBack) &&
                        getSquareOfSquareID(tempIDRightBack).isStoneAssigned() &&
                        getSquareOfSquareID(tempIDRightBack).getAssignedStoneColor().equals(oppositeColor) &&
                        existSquare(tempIDRighterBack) &&
                        !getSquareOfSquareID(tempIDRighterBack).isStoneAssigned()) {
                        destinations.add(tempIDRighterBack);
                    }
                    y--;
                }
            }
        }
        return destinations;
    }
    
    /**
     * Get stones with side of given color that can capture an enemy stone
     * @param theColor The color of the side to give back the captureable stones
     * @return Returns a Set with SquareID's of stones that can capture an enemy stone
     */
    private Set getCaptureableStones(Color theColor) {
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug) System.out.println("### Executing ### getCaptureableStones");
        Set<SquareID> stones = getSquaresWithStones(theColor);
        Set<SquareID> results = new HashSet<SquareID>();
        
        for (SquareID id : stones) {
            if (canSquareCaptureStones(id, theColor)) {
                results.add(id);
            }
        }
        return results;
    }
    
    /**
     * Gives possible destinations back in case there is no capture
     * @param theID The SquareID of Square Containing Stone that can move
     * @param theColor the color who's turn it is
     * @return Set with possible destinations
     */
    private Set getDestinationsWithoutCapture(SquareID theID, Color theColor) {
        assert theID != null : "SquareID is null";
        assert theID.getColumn() > 0 && theID.getColumn()<9 : "squareID out of range";
        assert theID.getLine() > 0 && theID.getLine()<9 : "squareID out of range";
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        if (debug)System.out.println("### Executing ### getDestinationsWithoutCapture");
        
        int jumper;
        Set<SquareID> destinations = new HashSet<SquareID>();
        if(theColor.equals(Color.WHITE)){
            jumper = 1; 
        }else{    
            jumper = -1; 
        }

        if (!getSquareOfSquareID(theID).isStoneKing()) {

            SquareID leftID = new SquareID(theID.getColumn()-1, theID.getLine()+jumper);             
            SquareID rightID = new SquareID(theID.getColumn()+1, theID.getLine()+jumper);

            if (theID.getColumn() > 1 && !isStoneAssignedToSquare(leftID)) destinations.add(leftID); 
            if (theID.getColumn() < 8 && !isStoneAssignedToSquare(rightID))  destinations.add(rightID);

        } else {
            int y;
            
            // check left up 
            if (theID.getColumn() > 1 && theID.getLine() < 8){
                y = theID.getLine()+1;
                if (debug)System.out.println("### Checking left up ###");
                
                for (int x = theID.getColumn()-1; x > 0; x--) {
                    SquareID tempIDLeft = new SquareID(x, y);

                    if (y < 9 &&
                        existSquare(tempIDLeft) &&
                        !getSquareOfSquareID(tempIDLeft).isStoneAssigned()) {

                        destinations.add(tempIDLeft);
                    } else {
                        break;
                    }
                    y++;
                }
            }

            // check left down
            if (theID.getColumn() > 1 && theID.getLine() > 1) {
                y = theID.getLine()-1;
                if (debug)System.out.println("### Checking left down ###");

                for (int x = theID.getColumn()-1; x > 0; x--) {
                    SquareID tempIDLeftBack = new SquareID(x, y);

                    if (y > 0 &&
                        existSquare(tempIDLeftBack) &&
                        !getSquareOfSquareID(tempIDLeftBack).isStoneAssigned()) {

                        destinations.add(tempIDLeftBack);
                    }else{
                        break;
                    }
                    y--;
                }
            }

            // check right up
            if (theID.getColumn() < 8 && theID.getLine() < 8) {
                y = theID.getLine()+1;
                if (debug)System.out.println("### Checking right up ###");

                for (int x = theID.getColumn()+1; x < 9; x++){
                    SquareID tempIDRight = new SquareID(x, y);

                    if (y < 9 &&
                        existSquare(tempIDRight) &&
                        !getSquareOfSquareID(tempIDRight).isStoneAssigned()) {

                        destinations.add(tempIDRight);
                    } else {
                        break;
                    } 
                    y++;
                }
            }

            // check right down
            if (theID.getColumn() < 8 && theID.getLine() > 1) {
                y = theID.getLine()-1;
                if (debug)System.out.println("### Checking right down ###");

                for (int x = theID.getColumn()+1; x < 9; x++){
                    SquareID tempIDRightBack = new SquareID(x, y);

                    if (y > 0 &&
                        existSquare(tempIDRightBack) &&
                        !getSquareOfSquareID(tempIDRightBack).isStoneAssigned()) {

                        destinations.add(tempIDRightBack);
                    } else {
                        break;
                    }
                    y--;
                }
            }
        }

        return destinations;
    }
    
    /**
     * gives the squareID of the stone that is captured
     * @param fromID SquareID of the origion
     * @param toID SquareID of the destination
     * @return theSquareID of the captured stone
     */
    private SquareID getCaptureableStone(SquareID fromID, SquareID toID) {
        assert fromID != null || toID != null : "SquareID is null";
        assert fromID.getColumn() > 0 && fromID.getColumn()<9 : "squareID out of range";
        assert fromID.getLine() > 0 && fromID.getLine()<9 : "squareID out of range";
        assert toID.getColumn() > 0 && toID.getColumn()<9 : "squareID out of range";
        assert toID.getLine() > 0 && toID.getLine()<9 : "squareID out of range";
        
        int column = 0;
        int line = 0;

        if (!getSquareOfSquareID(fromID).isStoneKing() || 
            (getSquareOfSquareID(fromID).isStoneKing() && 
            hasCapturedStoneInSession)) {

            // check column
            if (fromID.getColumn() - toID.getColumn() == 2) column = fromID.getColumn()-1;
            else if (fromID.getColumn() - toID.getColumn() == -2) column = fromID.getColumn()+1;

            // check line
            if (fromID.getLine() - toID.getLine() == 2) line = fromID.getLine()-1;
            else if(fromID.getLine() - toID.getLine() == -2) line = fromID.getLine()+1;
            
            if (debug)System.out.println("### Executing ### (if) getCaptureableStone result: "+column+","+line);
            if (debug)System.out.println("king: "+getSquareOfSquareID(fromID).isStoneKing() + " capturedStone: "+hasCapturedStoneInSession);

            return new SquareID(column, line);
        } else if (getSquareOfSquareID(fromID).isStoneKing() && 
                !hasCapturedStoneInSession) {
            int jumperLine;
            int jumperColumn;

            if (fromID.getColumn() - toID.getColumn() > 0) jumperColumn = -1;
            else jumperColumn = 1;

            if(fromID.getLine() - toID.getLine() > 0) jumperLine = -1;
            else jumperLine = 1;

            int y = fromID.getLine()+jumperLine;
            for (int x = fromID.getColumn()+jumperColumn; /*empty*/ ; x+=jumperColumn) {
                if (debug)System.out.println("# checking "+x+","+y);
                SquareID aSquareID = new SquareID(x, y);
                if (getSquareOfSquareID(aSquareID).isStoneAssigned()) {
                    if (debug)System.out.println("### Executing ### (if) getCaptureableStone result: "+column+","+line);
                    if (debug)System.out.println("king: "+getSquareOfSquareID(fromID).isStoneKing() + " capturedStone: "+hasCapturedStoneInSession);

                    return aSquareID;
                }
                y+=jumperLine;
            } 

        }
        return null;
    }    
   
    /**
     * returns a set with the positions of stones
     * @return set of SquareIDs of stone Positions
     */
     public Set<SquareID> getStonePositions(){
        Set<SquareID> stonePositions = new HashSet<SquareID>();
        int x;
        int y;
        
        for (x=0; x<=7; x++) {
            for (y=0; y<=7; y++) {
                if (boardMatrix[x][y] != null &&
                    boardMatrix[x][y].isStoneAssigned())
                {
                    stonePositions.add(boardMatrix[x][y].getSquareID());
                } 
            
            }
         }
         
        return stonePositions;
     }
        
    /**
     * makes the shoutmessages and fills the Map
     */          
    private void makeShoutMessages(){
        String[] goodMessagesArray = 
            {
                "Very well!",
                "Perfect!",
                "Excellent!",
                "You are THE MAN!",
                "You rock!",
                "Oh My God! Awesome!",
                "Awesome!",
                "Sure you didn't play Pro before?",
                "Competitor crushing has started...",
                "Holy cow! Very nice!",
                "Awesome! This game should be named after you!"
            };

        String[] neutralMessagesArray = 
            {    
                "What's next?",
                "Nice wheather!",
                "Next move :-)",
                "This is getting a bit boring...",
                "Show me some magic!",
                "Come on, you can do better than this!",
                "*yawn*"
            };    

        String[] negativeMessagesArray = 
            {
                "You suck!",
                "What a loser",
                "Poor boy!",
                "Is that all you can do?",
                "This is torture...",
                "Come on, you suck harder than my vacuum cleaner :(",
                "You're going down!"
            };

        shoutMessages.put("good",goodMessagesArray);
        shoutMessages.put("neutral",neutralMessagesArray);
        shoutMessages.put("negative",negativeMessagesArray);
    }
    
    /**
     * Start new game and resets the related values
     */
    private void newGame(){
        if (debug)System.out.println("### Executing ### newGame");
        stones.clear();
        
        int x;
        int y;
        
        for (y=1; y<=8; y++){
            for (x=1; x<=8; x++){
                if (boardMatrix[x-1][y-1].isStoneAssigned()) {
                    boardMatrix[x-1][y-1].unAssignStoneNumber();
                }
            }
        }
        
        addStones(Color.WHITE);
        addStones(Color.BLACK);
        
        if (debug)System.out.println("### Executing ### newGame: reset capture Stones");
        player1.resetCapturedStones();
        player2.resetCapturedStones();
        
        hasCapturedStoneInSession = false;
        lastMoved = null;
        if (debug)giveSquaresInConsole();
        
        if (debug)System.out.println("### Executing ### newGame: firePC");
        pcs.firePropertyChange("newGame", "", true);
    }
    
    /**
     * changes the turn of both players
     * @param mustSync syncronizing other model
     */
    private void changeTurn(boolean mustSync){
        if(mustSync){socket.send(13);} //sync with other player

        player1.changeTurn();
        player2.changeTurn();

        int turn = 0;
        if(player1.isTurn()){ turn = 1; }
        else {turn = 2;}
        pcs.firePropertyChange("turnChanged", "", turn); //notify all listeners
    }

    /**
     * Add the initial Stones at a newGame()
     * @param theColor the color of the stones
     */
    private void addStones(Color theColor){
        assert theColor != null : "color is null";
        assert theColor.equals(Color.BLACK) || theColor.equals(Color.WHITE) : "Color not w or b";
        
        int startrow = 0;
        int endrow=0;
        int stoneNumber = 0;
        int x;
        int y;
        if (theColor.equals(Color.WHITE)){startrow = 0; endrow=5;stoneNumber=1;}
        if (theColor.equals(Color.BLACK)){startrow = 5; endrow=0;stoneNumber=13;}

        for (y=1 + startrow ; y<=8 - endrow ; y++){
            for (x = 2-(y%2) ; x<=8 ; x = x+2){
                Stone theStone = new Stone(stoneNumber,theColor);
                stones.put(stoneNumber,theStone);
                getSquareOfSquareID(new SquareID(x,y)).assignStone(theStone);
                stoneNumber++;
            }
        }
    }

    /**
     * Adds a chatmessage to list
     * @param mustSync boolean if it needs to be syncted to the other model
     * @param playerNumber int the Playernumber who has added the message
     * @param theMessage String the message
     */
    public void addChatMessage(boolean mustSync, int playerNumber, String theMessage){
        assert playerNumber == 1 || playerNumber == 2 : "wrong playernumber";
        assert theMessage != null : "message is null";
        
        if(mustSync){socket.send(11, playerNumber, theMessage);} //sync with other player

        Player player; if(playerNumber==1){ player = player1; }else{ player = player2; }
        ChatMessage chatMessage = new ChatMessage(player, theMessage);
        chatMessages.add(chatMessage);

        pcs.firePropertyChange("newChatMessage", "", "Player"+playerNumber+": "+theMessage); //notify all listeners
    }
    
    /**
     * captures the stone on a square
     * @param mustSync boolean if it must be synced to the other model
     * @param thisSquareID SquareID of the Stone to be captured
     */
    public void captureStoneOnSquare(boolean mustSync, SquareID thisSquareID){
        assert thisSquareID != null : "SquareID is null";
        assert thisSquareID.getColumn() > 0 && thisSquareID.getColumn()<9 : "squareID out of range";
        assert thisSquareID.getLine() > 0 && thisSquareID.getLine()<9 : "squareID out of range";
        
        if(mustSync){socket.send(12, thisSquareID);} //sync with other player
        
        if (player1.isTurn()){
            player1.increaseCapturedStones();
            pcs.firePropertyChange("stoneCaptured", "", 1);
        } else {
            player2.increaseCapturedStones();
            pcs.firePropertyChange("stoneCaptured", "", 2);
        }

        int stoneNumber = getSquareOfSquareID(thisSquareID).getAssignedStoneNumber();
        getSquareOfSquareID(thisSquareID).unAssignStoneNumber();
        
        stones.remove(stoneNumber);

        if (debug)System.out.println("### Stone Captured ###");
    }

    /**
     * close the game
     * @param mustSync boolean if it must synced to the other model
     * @param theReason String the reason of closing
     */
    public void closeGame(boolean mustSync, String theReason){
        if(mustSync){socket.send(14, theReason);} //sync with other player
        pcs.firePropertyChange("closeGame", "", theReason); //notify all listeners
    }

    /**
    * Exit game. Disconnect sockets, close panels, restart connection manager.
    */
    public void exitGame(){
        socket.disconnect();
        pcs.firePropertyChange("exitGame", "", "exit");
    }
    
    /**
     * Get all captured stones by player.
     * @param thePlayerNumber the player number
     * @return int the captured stones
     */
    public int getCapturedStones(int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber ==2 : "wrong player number";
        
        Player player; if(thePlayerNumber==1){ player = player1; }else{ player = player2; }
        return player.getCapturedStones();
    }

    /**
     * Get color by player
     * @param thePlayerNumber the Player number
     * @return Color of the player
     */
    public Color getColorOfPlayer (int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber ==2 : "wrong player number";
        
        Player player; if(thePlayerNumber==1){ player = player1; }else{ player = player2; }
        return player.getColor();
    }
        
    /**
     * checks, if the player is at turn
     * @param thePlayerNumber the player number
     * @return boolean true, if the player is at turn
     */
    public boolean isPlayerAtTurn(int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber ==2 : "wrong player number";
        
        if (thePlayerNumber == 1){
            return player1.isTurn();
        } else if (thePlayerNumber == 2) {
            return player2.isTurn();
        }
        return false;
    }
    
    /**
     * Get a shoutmessage by type
     * @param theType the type of the shoutmessage : positive,neutral,negative
     * @return a random shout Message
     */
    public String getRandomShoutmessage(String theType){
        assert theType!= null : "getRandomShoutmessage: no valid type supplied.";
        
        String[] messages = shoutMessages.get(theType);

        Random r = new Random();
        int random = (r.nextInt(messages.length));
        
        return messages[random];
    }
    
    /**
     * Get the stone number that is on supplied square
     * @param theSquareID the squareID of the square which holds the stone
     * @return the stone number of the assigned stone
     */
    public int getStoneNumberOnSquare(SquareID theSquareID){
        assert theSquareID != null : "SquareID is null";
        assert theSquareID.getColumn() > 0 && theSquareID.getColumn()<9 : "squareID out of range";
        assert theSquareID.getLine() > 0 && theSquareID.getLine()<9 : "squareID out of range";

        return getSquareOfSquareID(theSquareID).getAssignedStoneNumber();
    }

    /**
     * Get the number of victories by the supplied player
     * @param thePlayerNumber teh player number
     * @return int the victories of the player
     */
    public int getVictories(int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber ==2 : "wrong player number";
        
        Player player; if(thePlayerNumber==1){ player = player1; }else{ player = player2; }
        return player.getVictories();
    }

    /**
     * Check if stone is assigned to square
     * @param theSquareID the SquareID of the Square
     * @return true, if the square has a stone assigned
     */
    public boolean isStoneAssignedToSquare(SquareID theSquareID){
        assert theSquareID != null : "SquareID is null";
        assert theSquareID.getColumn() > 0 && theSquareID.getColumn()<9 : "squareID out of range";
        assert theSquareID.getLine() > 0 && theSquareID.getLine()<9 : "squareID out of range";
        
        return getSquareOfSquareID(theSquareID).isStoneAssigned();            
    }

    /**
     * Offer a remise
     * @param mustSync boolean if it needs to sync to the other side
     * @param thePlayerNumber the player number
     */
    public void offerRemise(boolean mustSync, int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber ==2 : "wrong playernumber";
        
        if(mustSync){socket.send(15, thePlayerNumber);} //sync with other player

        pcs.firePropertyChange("remiseOffered", "", thePlayerNumber); //notify all listeners
    }

    /**
     * Reply to remise request
     * @param mustSync boolean if it needs to sync to other model
     * @param accept boolean if the offerd remise is accepted true = accepted
     */
    public void replyToRemise(boolean mustSync, boolean accept){
        if(mustSync){socket.send(18, accept);} //sync with other player

        if(accept){

            //switch colors
            if(player1.getColor()==Color.BLACK){player1.setColor(Color.WHITE);}else{player1.setColor(Color.BLACK);}
            if(player2.getColor()==Color.BLACK){player2.setColor(Color.WHITE);}else{player2.setColor(Color.BLACK);}

            //start new game
            newGame();
            pcs.firePropertyChange("remiseReply", "", true);
        }else{        
            pcs.firePropertyChange("remiseReply", "", false);
        }
    }
    
    /**
     * Set supplied stone on a supplied square
     * @param mustSync syncronize the other model
     * @param fromSquareID the SquareID of the Square from where the stone is moving
     * @param toSquareID the SquareID of the Square to where the stone is moving
     */
    public void setStoneOnSquare(boolean mustSync, SquareID fromSquareID, SquareID toSquareID){
        assert fromSquareID != null || toSquareID != null : "SquareID is null";
        assert fromSquareID.getColumn() > 0 && fromSquareID.getColumn()<9 : "squareID out of range";
        assert fromSquareID.getLine() > 0 && fromSquareID.getLine()<9 : "squareID out of range";
        assert toSquareID.getColumn() > 0 && toSquareID.getColumn()<9 : "squareID out of range";
        assert toSquareID.getLine() > 0 && toSquareID.getLine()<9 : "squareID out of range";
        
        if (debug)System.out.println("### Executing ### setStoneOnSquare");
        
        if(mustSync){socket.send(16,fromSquareID,toSquareID);} //sync with other player
        
        lastMoved = toSquareID;
        Color theColor = getSquareOfSquareID(fromSquareID).getAssignedStoneColor();
        if (canSquareCaptureStones(fromSquareID, theColor)){
            if (debug)System.out.println("### Executing ### setStoneOnSquare: in if canSquareCaptureStone");
        
            SquareID captureableStone = getCaptureableStone(fromSquareID, toSquareID);
            captureStoneOnSquare(false, captureableStone);
            if (!hasCapturedStoneInSession) {
                hasCapturedStoneInSession = !hasCapturedStoneInSession;
            }
    
        } else {
            hasCapturedStoneInSession = false;
        }
        
        Square originSquare = getSquareOfSquareID(fromSquareID);
        Square destinationSquare = getSquareOfSquareID(toSquareID);
        int theStoneNumber = originSquare.getAssignedStoneNumber();
                
        originSquare.unAssignStoneNumber();
        destinationSquare.assignStone(stones.get(theStoneNumber));
                
        // check if someone wins
        Color oppositeColor;
        if (getSquareOfSquareID(toSquareID).getAssignedStoneColor().equals(Color.WHITE)) {
            if (debug)System.out.println("### Executing ### setStoneOnSquare: opposite color: Black");
            oppositeColor = Color.BLACK;
        } else {
            if (debug)System.out.println("### Executing ### setStoneOnSquare: opposite color: White");
            oppositeColor = Color.WHITE;
        }
        
        int capturedStones;
        int playerNumber;
        if (player1.isTurn()) {
            if (debug)System.out.println("### Executing ### setStoneOnSquare: player 1 is turn");
            playerNumber = 1;
            capturedStones = getCapturedStones(1);
        } else {
            if (debug)System.out.println("### Executing ### setStoneOnSquare: player 2 is turn");
            playerNumber = 2;
            capturedStones = getCapturedStones(2);
        }

        if (capturedStones == 12 || !canMoveAStone(oppositeColor)) {
            if (debug)System.out.println("### Executing ### setStoneOnSquare: captured Stones == 12 or can not move stone");
            if (debug)System.out.println("### SETWINGAME ###");
            setWinGame(false, playerNumber);
            return;
        }

        if (!hasCapturedStoneInSession ||
            (hasCapturedStoneInSession && !canSquareCaptureStones(toSquareID, theColor))) {

            changeTurn(true);
            hasCapturedStoneInSession = false;
            pcs.firePropertyChange("stoneMoved","",true);
        } else {
            pcs.firePropertyChange("stoneMoved","",true);
            pcs.firePropertyChange("mustSecondCapture", "", toSquareID);
        }                

        if(!getSquareOfSquareID(toSquareID).isStoneKing() &&
            isMovedStoneKing(toSquareID)) {
            Stone stone = getSquareOfSquareID(toSquareID).getStone();
            stone.setToKing();

            pcs.firePropertyChange("newKing", "", stone.getStoneNumber());
        }        
        if (debug)giveSquaresInConsole();
    }
    
    /**
     * checks, if the moved stone is a king
     * @param theSquareID SuareID of the moved Stone
     * @return boolean true, if it is king
     */
    private boolean isMovedStoneKing(SquareID theSquareID) {
        assert theSquareID != null : "SquareID is null";
        assert theSquareID.getColumn() > 0 && theSquareID.getColumn()<9 : "squareID out of range";
        assert theSquareID.getLine() > 0 && theSquareID.getLine()<9 : "squareID out of range";
        
        if (getSquareOfSquareID(theSquareID).getAssignedStoneColor().equals(Color.WHITE) &&
            theSquareID.getLine() == 8) {
            
            return true;
        } else if (getSquareOfSquareID(theSquareID).getAssignedStoneColor().equals(Color.BLACK) &&
                theSquareID.getLine() == 1) {
            
            return true;
        }
        return false;
    }
    
    /**
     * Set the winning player
     * @param mustSync boolean if it needs to sync to other model
     * @param thePlayerNumber the player number who won
     */
    public void setWinGame(boolean mustSync, int thePlayerNumber){ 
        assert thePlayerNumber == 1 || thePlayerNumber ==2 : "wrong playernumber";
        
        if(mustSync){socket.send(17,thePlayerNumber);} //sync with other player
        if (debug)System.out.println("### SETWINGAME CALLED!! ###");
        
        Player player; 
        if(thePlayerNumber==1){
            player2.setColor(Color.WHITE);
            player1.setColor(Color.BLACK);
            player = player1; 
        }else{ 
            player1.setColor(Color.WHITE);
            player2.setColor(Color.BLACK);
            player = player2; 
        }

        player.increaseVictories();
        if(player.isTurn()){
            player1.changeTurn();
            player2.changeTurn();
        }
        
        newGame();
        pcs.firePropertyChange("winGame","",thePlayerNumber);
    }

    /**
     * Get the color of the supplied stone
     * @param theStoneNumber stoneNumber
     * @return Color
     */
    public Color getColorOfStone(int theStoneNumber){
        assert theStoneNumber > 0 : "getColorOfStone: faulty arguments";

        return stones.get(theStoneNumber).getColor();
    }

    /**
     * Subscribe to events sent out by the model.
     * @param a the listener
     */
    public void addPropertyChangeListener(PropertyChangeListener a){
        pcs.addPropertyChangeListener(a);
    }

    /**
     * Unscribe from events subscription
     * @param a the listener
     */
    public void removePropertyChangeListener(PropertyChangeListener a){
        pcs.removePropertyChangeListener(a);
    }
}
