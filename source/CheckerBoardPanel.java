import java.awt.geom.Rectangle2D;
import java.beans.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.border.*;
    
/**
 * Panel with the visual representation of the checker board.
 */
public class CheckerBoardPanel extends JPanel implements PropertyChangeListener{
            
    /**
     * Set with the highlighlighted squares
     */
    private ArrayList<SquareID> highlightedSquares;
    
    /**
     * The Game Model.
     */
    private Model model;
    
    /**
     * Image of the board
     */
    private Image boardBackground;
    
    /**
     * Map with stone2Ds
     */
    private Map <Integer,Stone2D> stones;
    
    /**
     * The current player number
     */
    private int playerNumber;
    
    /**
     * The selected Stone (by clicking)
     */
    private Stone2D selectedStone;
    
    /**
     * The selected square (by clicking)
     */
    private SquareID selectedSquare;
    
     /**
     * The selected square (by clicking)
     */
    private SquareID secondCapture;
    
    /**
     * The possible destinations (by clicking)
     */
    private Set <SquareID> possibleDestination = new HashSet<SquareID>();
    
    
    /**
     * Variable, which holds true, if it is your turn.
     */
    private boolean canPlay;
    
    
    /**
     * Size value of the board
     */
    public static final int DEFAULT_SIZE = 320;
    
    
    /**
     * Constructor for CheckerBoardPanel
     * @param thisModel the belonging model
     * @param thePlayerNumber the player number
     */
    public CheckerBoardPanel(Model theModel,int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber == 2 : "wrong player number";
        assert theModel != null : "Model is null";
        
        stones = new HashMap<Integer,Stone2D>();
        highlightedSquares = new ArrayList<SquareID>();
        model = theModel;      
        playerNumber = thePlayerNumber;
        
        setCanPlay(model.isPlayerAtTurn(thePlayerNumber));
    
        setLayout(null);
        setSize(320,320);
        
        // try to load checker board background image
        try { boardBackground = ImageIO.read(this.getClass().getResource("files/checkerBoard.gif")); }
        catch (IOException e) { e.printStackTrace();  }
        
        makeStone2Ds();
        updateStonePositions();
    
        addMouseListener(new MouseHandler());
        addMouseMotionListener(new MouseMotionHandler());
        model.addPropertyChangeListener(this);
    }
    
    /**
     * Draws the board, stones and highlightings
     * @param g Graphics var
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        
        // Draw background
        g.drawImage(boardBackground, 0, 0, 320, 320, null);
        
        // Draw Highlighted Stones
        paintHighlightedSquares(g);
        
        // Draw Stones
        paintStones(g);
    }
    
    /**
     * Draws the stones
     * @param g Graphics
     */
    private void paintStones(Graphics g){
           
        for(Map.Entry <Integer,Stone2D> entry : stones.entrySet()){
            entry.getValue().paintComponent(g);
        } 
        
        if (selectedStone != null){
            selectedStone.paintComponent(g);
        }
    }
     
    /**
     * Creates initialized the Stone2D objects at a new Game.
     */
    private void makeStone2Ds(){
        stones.clear();
        Color theColor;
        int theNumber;
        for (SquareID theSquareID : model.getStonePositions()){
            theNumber = model.getStoneNumberOnSquare(theSquareID);
            theColor = model.getColorOfStone(theNumber);
            stones.put(theNumber,new Stone2D(theNumber,theColor,getPointOfSquareID(theSquareID)));
        }
        repaint();
    }
    
    /**
     * Reset the stones with the values of the Model.
     */
    private void updateStonePositions(){

        Color theColor;
        Set <Integer> modelStoneNumbers = new HashSet<Integer>();
        int theNumber;
        
        for (SquareID theSquareID : model.getStonePositions()){
            theNumber = model.getStoneNumberOnSquare(theSquareID);
            modelStoneNumbers.add(theNumber);
            theColor = model.getColorOfStone(theNumber);        
            stones.get(theNumber).setPosition(getPointOfSquareID(theSquareID));
        }
        
        Set<Integer> tempSet = new HashSet<Integer>();
        
        for (Integer localStoneNumber : stones.keySet()){
            if (!modelStoneNumbers.contains(localStoneNumber)) tempSet.add(localStoneNumber);
        }
        
        if (!tempSet.isEmpty()){
          for (Integer number : tempSet) stones.remove(number);   
        }
        
        selectedStone=null;
        selectedSquare=null;
        highlightedSquares.clear();
        repaint();
    }
     
    /**
     * Paints the highlightings.
     * @param g Graphics
     */
    private void paintHighlightedSquares(Graphics g){
        Graphics2D g2 = (Graphics2D) g;
        if (!highlightedSquares.isEmpty()){
            for (SquareID theSquareID : highlightedSquares){
                Point p = getPointOfSquareID(theSquareID);
                g2.setColor(Color.GREEN);
                g2.draw(new Rectangle2D.Double(p.getX()-20,p.getY()-20,40,40));
            }
            if (selectedSquare !=null){
                Point p = getPointOfSquareID(selectedSquare);
                g2.setColor(Color.BLUE);
                g2.draw(new Rectangle2D.Double(p.getX()-20,p.getY()-20,40,40));
            }
        }
        if (secondCapture != null){
                Point p = getPointOfSquareID(secondCapture);
                g2.setColor(Color.BLUE);
                g2.draw(new Rectangle2D.Double(p.getX()-20,p.getY()-20,40,40));
                
        }
    }
     
    /**
     * Resets the highlightings and sets the Square highlighted.
     * @param theSquareID the SquareID of the square to be highlighted
     */
    private void highlightSquare(SquareID theSquareID){
        highlightedSquares.clear();
        highlightedSquares.add(theSquareID);
        repaint();
    }
     
    /**
     * Resets the highlighting and sets a set of SquareIDs as highlighted.
     * @param setOfSquareIDs a set of squareIDs to be highlighted
     */
    private void highlightSquares(Set setOfSquareIDs){
         highlightedSquares.clear();
         highlightedSquares.addAll(setOfSquareIDs);
         repaint();
    }
    
    /**
     * Changes the canPlay variable and with it, the highlighting of the board,
     * if it is the players turn.
     * @param playable true, if player is at turn
     */
    private void setCanPlay(boolean playable){
        canPlay = playable;
        if(canPlay){    
            this.setBorder(new LineBorder(Color.green, 2));
        } else {
            this.setBorder(new LineBorder(Color.black, 1));
        }
    }
     
    /**
     * Converts a SquareID x and y position to a
     * absolute coordinate corresponding with the right
     * position for the Stone2D on the panel.
     * @param theSquareID squareID to be set to a point
     * @return Point: the middlepoint of the square
     */    
    public Point getPointOfSquareID(SquareID theSquareID) {
        assert theSquareID != null : "SquareID is null";
        assert theSquareID.getColumn() > 0 && theSquareID.getColumn()<9 : "squareID out of range";
        assert theSquareID.getLine() > 0 && theSquareID.getLine()<9 : "squareID out of range";        
        
        SquareID sid = theSquareID;
       
        //turn the board (SquareIDs), if the color is black to play from down to up
        if (model.getColorOfPlayer(playerNumber)==Color.BLACK){
            int idX = 9 - theSquareID.getColumn();
            int idY = 9 - theSquareID.getLine();
            sid = new SquareID(idX,idY);
        }
        
         int coordX = (sid.getColumn()-1)*40 + 20;
         int coordY = (DEFAULT_SIZE-1) - (sid.getLine()*40 - 20);
        
         return new Point(coordX, coordY);     
    }
     
    /**
     * Request Square ID of thePoint.
     * @return Square ID van het punt.
     * @param thePoint of wich the square ID is needed.
     */
    public SquareID getSquareIDOfPoint(Point thePoint){
        assert thePoint != null : "Point is null!";
        
        int idX = ((int)thePoint.getX() / 40 ) + 1 ;
        int idY = ((int)Math.abs(thePoint.getY()-(DEFAULT_SIZE-1)) /40) + 1;
        
        //turn the board (SquareIDs), if the color is black to play from down to up
        if (model.getColorOfPlayer(playerNumber)==Color.BLACK){
            idX = 9 - idX;
            idY = 9 - idY;
        }
        
                
        assert idX > 0 && idX <9 : "idX out of range";
        assert idY > 0 && idY <9 : "idY out of range";
        return new SquareID(idX, idY);
    }
    
    /**
     * Action to perform when a action is performed, menu.
     * @param event Event received.
     */
    public void propertyChange (PropertyChangeEvent event){
        //checks for the right event of the model
        if(event.getPropertyName().equals("newGame")){
            if (model.getColorOfPlayer(playerNumber).equals(Color.WHITE)){
                setCanPlay(true);
                
            } else {
                setCanPlay(false);                
            }
            //updates stones
            makeStone2Ds();
            
        }
        
        if(event.getPropertyName().equals("stoneMoved")){
            
            //updates stones
            updateStonePositions();
            
        }
        
        if(event.getPropertyName().equals("remiseOffered")){
            setCanPlay(false);          
        }
        
        if(event.getPropertyName().equals("remiseReply")){
            if(model.isPlayerAtTurn(playerNumber)){
                setCanPlay(true);    
            }else{
                setCanPlay(false);  
            }
        }
        
        if(event.getPropertyName().equals("turnChanged")){
           if ((Integer)event.getNewValue() == playerNumber){
               setCanPlay(true);
           } else {
               setCanPlay(false);             
           }
            
        }
        
        if(event.getPropertyName().equals("mustSecondCapture")&& canPlay){
            SquareID theSquareID = (SquareID)event.getNewValue();
            
            secondCapture = theSquareID;
            highlightSquares(possibleDestination);
        }
        
        if(event.getPropertyName().equals("newKing")){
            int theStoneNumber = (Integer)event.getNewValue();
            stones.get(theStoneNumber).setToKing();
            repaint();
        }
    }
    
    /**
     * Innerclass of CheckerBoardPanel to handle mouse click events.
     */
    private class MouseHandler extends MouseAdapter{
        
        /**
         * Actions to perform when mouse button pressed.
         * @param event Event containing mouse info.
         */
        public void mousePressed(MouseEvent event){
            if (!canPlay) return;
            
            SquareID theSquareID = getSquareIDOfPoint(event.getPoint());
            if (!model.existSquare(theSquareID)) return;
            if (secondCapture!=null && !secondCapture.equals(theSquareID))return;
            if (model.isStoneAssignedToSquare(theSquareID)){
                                
                selectedSquare = theSquareID;
                
                selectedStone = stones.get(model.getStoneNumberOnSquare(theSquareID));
                
                if (!selectedStone.getStoneColor().equals(model.getColorOfPlayer(playerNumber))){
                    
                    selectedSquare= null;
                    selectedStone = null;
                    highlightedSquares.clear();
                    possibleDestination.clear();
                    repaint();
                    return;
                }
                
                possibleDestination = model.getPossibleDestinations(theSquareID);
                highlightSquares(possibleDestination);
            }
            
        }

        /**
         * Actions to perform when mouse button released.
         * @param event Event containing mouse info.
         */
        public void mouseReleased(MouseEvent event){
            if (!canPlay) return;
            SquareID theSquareID = getSquareIDOfPoint(event.getPoint());
           
            if (model.existSquare(theSquareID)){
                if (!model.isStoneAssignedToSquare(theSquareID)){
                    if (selectedStone!=null){
                        for(SquareID id : possibleDestination){
                            if(id.equals(theSquareID)){
                                secondCapture = null;
                                model.setStoneOnSquare(true,selectedSquare,theSquareID);
                                
                                selectedSquare= null;
                                selectedStone = null; 
                                break;                                
                                // Break after the set is made empty or it will except.
                                
                            }
                        }
                        possibleDestination.clear();
                    } 
                } else {
                    if (selectedStone!=null && selectedSquare.equals(theSquareID)){
                        selectedStone.setPosition(getPointOfSquareID(selectedSquare));
                        repaint();
                        return;
                    }
                }
            } 
            
            if (selectedStone != null){
                selectedStone.setPosition(getPointOfSquareID(selectedSquare));
                
                selectedSquare= null;
                selectedStone = null;
                possibleDestination.clear();
            }
            
            possibleDestination.clear();
            highlightedSquares.clear();
            repaint();
        }
    }

    /**
     * Innerclass of CheckerBoardPanel to handle mouse motion events.
     */
    private class MouseMotionHandler implements MouseMotionListener{
        
        /**
         * Action to perform when mouse moved.
         * @param event Event containing mouse info.
         */
        public void mouseMoved(MouseEvent event){}

        /**
         * Action to perform when mouse dragged.
         * @param event Event containing mouse info.
         */
        public void mouseDragged(MouseEvent event){
            if (selectedStone != null){
                int x = event.getX();
                int y = event.getY();
           
                selectedStone.setLocation(x-20,y-20);
                repaint();
            }
        }
    }    
}
