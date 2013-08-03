import java.beans.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

/**
 * Chat Panel view for chatting
 * sends a message to the other player, keeps track of messages of both sides!
 * Components: 
 * - Chat Message Area
 * - Input field
 * - Submit button
 */
public class ChatPanel extends JPanel implements PropertyChangeListener, ActionListener {
    
    /**
     * Model of the game.
     */
    private Model model;
    
    /**
     * JScollPane component holding the the Chat text area for scrolling the messages, 
     * if needed.
     */
    private JScrollPane scrollPane;
    
    /**
     * JTextArea component displaying the chat messages. placed in the scrollPane.
     */
    private JTextArea chatField;
    
    /**
     * JButton Component for sending the typed message to the model.
     */
    private JButton submitButton;
    
    /**
     * JTextField for entering the chat message to be send to other Player.
     */
    private JTextField inputField;
    
    /**
     * Holds the playernumber of the belonging player (1 or 2).
     */
    private int playerNumber;
    
    
    /**
     * Constructor of the ChatPanel.
     * sets the size, adds the needed components and registers the event handlers.
     * @param thisModel the belonging Model holding the data.
     * @param thePlayerNumber the number of the belonging player(1 or 2)
     */
    public ChatPanel(Model theModel, int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber == 2 : "wrong playernumber";
        assert theModel != null : "Model is null";
        
        playerNumber = thePlayerNumber;
        model = theModel;
       
        // sets the size of the Panel
        setSize(200, 400);
        
        // adds the needed Components
        addChatField();
        addInputField();
        addSubmitButton();

        //regiter on events
        inputField.addActionListener(this);
        submitButton.addActionListener(this);
        model.addPropertyChangeListener(this);
    }        
    
    /**
     * Adds the Chat text Area to this ChatPanel.
     * JTextArea placed in a JScrollPane.
     */
    private void addChatField(){
        //makes the chatfield
        chatField = new JTextArea();
        chatField.setEditable(false);	  
        chatField.setLineWrap(true);
        chatField.setWrapStyleWord(true);
        
        //makes the scrollPane to make scrolling of the chat field available
        scrollPane = new JScrollPane(chatField,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension(190,270));
        
        // add it to this panel
        add(scrollPane);
    }
    
    /**
     * Adds the input text field to this ChatPanel.
     */
    private void addInputField(){
        //makes the input Field
        inputField = new JTextField();
        inputField.setPreferredSize(new Dimension(190,20));
        
        //add it to this Panel
        add(inputField);
    }
    
    /**
     * Adds the submit button to this ChatPanel.
     */
    private void addSubmitButton(){
        // makes the submit button
        submitButton = new JButton("send");
        submitButton.setPreferredSize(new Dimension(190, 20));
        
        //add it to this Panel
        add(submitButton);
    }
    
    /**
     * Adds a string to the Model.
     * @param message chat message to be added to the model
     */
    public void addMessage( String message){
        assert message != null : "message is null";
        
        model.addChatMessage(true, playerNumber, message);
        inputField.setText("");
    }
    
    /**
     * Calls the addMessage method with the enterd text,
     * if submit button is pressed or in the input field is pressed return.
     * @param e action event in the panel.
     */
    public void actionPerformed(ActionEvent e) {
        // checks for the right event
        if((e.getSource().equals(inputField) || e.getSource().equals(submitButton)) ){
            //checks for the a non empty string
            if(!inputField.getText().equalsIgnoreCase("")){
                addMessage(inputField.getText());
            }
        }
    }
    
    /**
     * Listens on change events of the model. 
     * if event is "newChatMessage", 
     * it updates the chat text area with the new message.
     * @param event property Change Event of the Model
     */
    public void propertyChange (PropertyChangeEvent event){
        //checks for the right event of the model
        if(event.getPropertyName().equals("newChatMessage")){
            //add the message to the chat area
            chatField.append((String)event.getNewValue()+"\n");
            //sets the view to the last enterd line
            chatField.setCaretPosition(chatField.getText().length());
        }
    }
}
