import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.awt.font.*;

// Managers
import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;

import javax.swing.BoxLayout;

/**
 * Panel for menu, status and score.
 */
public class GamePanel extends JPanel implements PropertyChangeListener, ActionListener{
    
    /**
     * The Game model.
     */
    private Model model;
    
    /**
     * Label holding the game status.
     */
    private JTextArea statusLabel = new JTextArea("Welcome to Checkers!",3,10);
    
    /**
     * Label holding the score.
     */
    private JLabel scoreLabel = new JLabel("0-0",JLabel.CENTER);
    
    /**
     * Label holding the score.
     */
    private JLabel victoriesLabel = new JLabel("0-0",JLabel.CENTER);
    
    /**
     * Button for requesting a remise.
     */
    private JButton remiseAskButton = new JButton("Ask for remise");
    
    /**
     * Button to deny a remise request.
     */
    private JButton remiseDenyButton = new JButton("Deny remise");
    
    /**
     * Button to request a give up.
     */
    private JButton giveUp = new JButton("Give up");
    
    /**
     * Button to exit the game.
     */
    private JButton exitGame = new JButton("Exit game");
    
    /**
     * Label showing a stone with the player's color.
     */
    private JLabel showStoneLabel = new JLabel();
    
    /**
     * Panel where the status messages are shown.
     */
    private JPanel statusPanel;
    
    /**
     * Player nummer.
     */
    private int playerNumber;
    
    /**
     * Variable containing the timer for paintComponent.
     */
    private Timer timer = new Timer(1000, this);
    
    /**
     * Nr of seconds before closing.
     */
    private int closingSecs = 6;
    
    /**
     * Message received in the status panel after user exit.
     */
    private String closingMessage;
    
    /**
     * Game Panel
     * @param thisModel Model, used for all game communication and storing values.
     * @param thePlayerNumber Playnumber
     */
    public GamePanel(Model theModel, int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber == 2 : "wrong player number";
        assert theModel != null : "Model is null";
        
        model = theModel;
        playerNumber = thePlayerNumber;
       
        this.addComponentsToPanels();
        this.addListener();
        this.setPanelsOptions();
    }
    
    /**
     * Sets the options for the panels.
     */
    private void setPanelsOptions(){
         // Layout manager 
        GridLayout statusLayout = new GridLayout(1,1);
        statusLayout.setVgap(5);
        statusPanel.setSize(50,200);  
        statusLabel.setEditable(false);
        statusLabel.setWrapStyleWord(true);
        
        // Set background same as panel.
        statusLabel.setBackground(this.getBackground());
        scoreLabel.setBackground(this.getBackground());
        victoriesLabel.setBackground(this.getBackground());  
        
         // set fonts
        Font font = new Font("Castellar", Font.ITALIC, 20);
        Font ledFont;
        try{
            ledFont = Font.createFont(Font.TRUETYPE_FONT, new File("files/ledfont.ttf"));
            ledFont = ledFont.deriveFont( 25f ); 
        }catch (Exception e){
            ledFont = font; //use default font in case of loading error.
        }
        
        scoreLabel.setForeground(Color.RED);
        victoriesLabel.setForeground(Color.RED);
        
        // Line break
        statusLabel.setLineWrap(true);
        remiseDenyButton.setEnabled(false);
        
        statusLabel.setFont(new Font("sansserif", Font.BOLD,10));
        scoreLabel.setFont(ledFont);
        victoriesLabel.setFont(ledFont);
        
    }
    
    /**
     * Adds all the components to the panels.
     */
    private void addComponentsToPanels(){
        // Panel for the status text areas
        statusPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        GridLayout layout = new GridLayout(12,1);
        statusPanel.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        statusPanel.add(statusLabel);
        this.add(statusPanel);
        
        // Label for text above current state
        JLabel victories = new JLabel("Victories",JLabel.CENTER);
        JLabel score = new JLabel(" Captured stones",JLabel.CENTER);
        
        //set stone to show
        setStoneShow();

        // 5 pixels distance between buttons
        layout.setVgap(6);  
        
        // Add layoutmanager
        buttonPanel.setLayout(layout);  
        buttonPanel.add(score);
        buttonPanel.add(scoreLabel); 
        buttonPanel.add(victories);
        buttonPanel.add(victoriesLabel);
        buttonPanel.add(remiseAskButton);
        buttonPanel.add(remiseDenyButton);
        buttonPanel.add(giveUp);
        buttonPanel.add(exitGame); 
        buttonPanel.add(showStoneLabel);  
        this.add(buttonPanel);
    }
    
    /**
     * Method displaying the players stone.
     */
    private void setStoneShow(){
        //Image icon to show the color
        ImageIcon showStone = null;
        if(model.getColorOfPlayer(playerNumber).equals(Color.WHITE)) {
            //showStone = new ImageIcon("files/stoneWhiteSmall.gif");
            try { showStone = new ImageIcon(this.getClass().getResource("files/stoneWhiteSmall.gif")); }
            catch (Exception e) { System.out.println("Error: failed to load files/stoneWhiteSmall.gif");}
            
        }else { 
            //showStone = new ImageIcon("files/stoneBlackSmall.gif");
            try { showStone = new ImageIcon(this.getClass().getResource("files/stoneBlackSmall.gif")); }
            catch (Exception e) { System.out.println("Error: failed to load files/stoneBlackSmall.gif");}
        }
        
        showStoneLabel.setIcon(showStone);
        showStoneLabel.setText("= your color");
    }
    
    /**
     * Adds listener. to panels
     */
    private void addListener(){
        remiseAskButton.addActionListener(this);
        remiseDenyButton.addActionListener(this);
        giveUp.addActionListener(this);
        exitGame.addActionListener(this);
        model.addPropertyChangeListener(this);
    }
    
    /**
     * Checks the source of the button click and takes action.
     * @param e Event fired when a bottom is clicked.
     */
     public void actionPerformed(ActionEvent e) {

        if(e.getSource().equals(remiseAskButton)){
            if(remiseAskButton.getText().equalsIgnoreCase("Ask for remise")) {
                model.offerRemise(true, playerNumber);
                remiseAskButton.setEnabled(false);
            }else{
                model.replyToRemise(true,true);
                remiseAskButton.setEnabled(true);
                remiseAskButton.setText("Ask for remise");
            }
        } else if(e.getSource().equals(remiseDenyButton)){
            model.replyToRemise(true,false);
        } else if(e.getSource().equals(giveUp)){
            if(playerNumber == 1){
                model.setWinGame(true, 2);
            } else {
                model.setWinGame(true, 1);
            }
        } else if(e.getSource().equals(exitGame)){
            model.closeGame(true," Player " + playerNumber + " has exit the game.");
        }
        
        //update from timer
        if(e.getSource().equals(timer)){
            statusLabel.setText("Exit in "+closingSecs+" seconds: "+closingMessage);
            closingSecs--;
            
            if(closingSecs==-1){
                timer.stop();
                model.exitGame();
                
            }
        }
     } 
     
    /**
     * Paints the components on the panel.
     * @param g Graphic
     */
    public void paintComponent(Graphics g){
        super.paintComponent(g);     
      
        //Graphics2D g2 = (Graphics2D)g;  
     }
 
    /**
     * Handles the incoming property change events.
     * @param event propertyChanged event
     */
    public void propertyChange (PropertyChangeEvent event){
        // remise offered
        if(event.getPropertyName().equalsIgnoreCase("remiseOffered")){
            if((Integer)event.getNewValue() != playerNumber){
                remiseAskButton.setText("Accept remise");
                remiseDenyButton.setEnabled(true);
                // can't give up when remise is offered
                giveUp.setEnabled(false);
            }
        }
        // remise denied
        if(event.getPropertyName().equalsIgnoreCase("remiseReply")){
            remiseAskButton.setText("Ask for remise");
            remiseDenyButton.setEnabled(false);
            remiseAskButton.setEnabled(true);
            giveUp.setEnabled(true);
        }

        // closing game due to exit button / network disconnect
        if(event.getPropertyName().equalsIgnoreCase("closeGame")){ 
            closingMessage = (String)event.getNewValue();
            timer.start();
        }

        // A stone captured
        if(event.getPropertyName().equalsIgnoreCase("stoneCaptured")){
            if(playerNumber == 1){
                scoreLabel.setText("" + model.getCapturedStones(1) + " - " + model.getCapturedStones(2));
            } else {
                scoreLabel.setText("" + model.getCapturedStones(2) + " - " + model.getCapturedStones(1));
            }
        }
        
        // One of the player won
        if(event.getPropertyName().equalsIgnoreCase("newGame")){
            if(playerNumber == 1){
                victoriesLabel.setText("" + model.getVictories(1) + " - " + model.getVictories(2));
            } else {
                victoriesLabel.setText("" + model.getVictories(2) + " - " + model.getVictories(1));
            }
              
            if(playerNumber == 1){
                scoreLabel.setText("" + model.getCapturedStones(1) + " - " + model.getCapturedStones(2));
            } else {
                scoreLabel.setText("" + model.getCapturedStones(2) + " - " + model.getCapturedStones(1));
            }

            remiseAskButton.setText("Ask for remise");
            remiseDenyButton.setEnabled(false);
            remiseAskButton.setEnabled(true);
            giveUp.setEnabled(true);
            
            //refresh stone that shows color
            setStoneShow();
            showStoneLabel.repaint();
        }
    }
}
