import java.beans.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * Main Frame containing all the panels.
 */
public class GameFrame extends JFrame implements PropertyChangeListener{

    /**
     * Variable containing the player number.
     */
    private int playerNumber;
    
    /**
     * The Game model.
     */
    private Model model;
    
    /**
     * Window width in pixels (X-axis)
     */
    public static int DEFAULT_WIDTH = 700;
    
    /**
     * Window height in pixels (Y-axis)
     */
    public static int DEFAULT_HEIGHT = 385;

    /**
     * Panel containing the board.
     */
    private CheckerBoardPanel boardPanel;

    /**
     * Panel containing the chat component.
     */
    private ChatPanel chatPanel;

    /**
     * Panel containing the game menu, status and score.
     */
    private GamePanel gamePanel;

    /**
     * Panel containing the shout messages.
     */
    private ShoutPanel shoutPanel;

    /**
     * Bogus panel filling the empty gap in the border manager.
     */
    private JPanel backPanel;
    
    /**
     * Game frame
     * @param thisModel Model, used for all game communication and storing values.
     * @param thisNumber Player number.
     * Used in the game for identification and in the Title of the frame.
     */    
    public GameFrame(Model theModel, int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber == 2 : "wrong player number";
        assert theModel != null : "Model is null";
        
        model = theModel;
        playerNumber = thePlayerNumber;
        
        // Draw frame in center of screen.
        Toolkit kit = Toolkit.getDefaultToolkit();
        Dimension screenSize = kit.getScreenSize();
        int Xas = (int)(screenSize.getWidth() - DEFAULT_WIDTH) /2;
        int Yas = (int)(screenSize.getHeight() - DEFAULT_HEIGHT)/ 2; 
                
        this.setSize(DEFAULT_WIDTH+10, DEFAULT_HEIGHT);
        this.setLocation(Xas, Yas);
        
        setTitle("Player: " + playerNumber);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5,5)); 
                
        createPanels();
        placePanels();
                
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        
    }
    
        
    /**
     * All used panels are created here.
     */
    private void createPanels(){
        
        boardPanel = new CheckerBoardPanel(model, playerNumber);
        chatPanel = new ChatPanel(model, playerNumber);
        gamePanel = new GamePanel(model, playerNumber);
        shoutPanel = new ShoutPanel(model, playerNumber);   
        backPanel = new JPanel();

    }

    /**
     * After creation, the panels will be placed on the frame.
     */
    private void placePanels(){

        gamePanel.setBounds(0,0,150,355);
        boardPanel.setBounds(165,5,320, 320);
        chatPanel.setBounds(500,0,200,355);
        shoutPanel.setBounds(0,330,DEFAULT_WIDTH,25);
        backPanel.setBounds(0,180,20,20);
        
        gamePanel.addPropertyChangeListener(this);
        
        this.add(chatPanel);
        this.add(gamePanel);
        this.add(boardPanel);
        this.add(shoutPanel);
        this.add(backPanel);
    }
       
    /**
     * Process received events. Events usually mean that the Model has been changed.
     * @param event An propertyChangeEvent
     */
    public void propertyChange (PropertyChangeEvent event){
        //The model has changed.
        
        if(event.getPropertyName().equalsIgnoreCase("exitGame")){
            this.setVisible(false);
        }
    }
}
