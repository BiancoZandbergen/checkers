
/**
 * Object containing a chat message.
 */
public class ChatMessage {
    
    /**
     * The chat message.
     */
    private String chatMessage;
    
    /**
     * Owner of the chat message.
     */
    private Player owner;
    
    
    /**
     * Constructor for ChatMessage.
     * @param theOwner Owner of the message.
     * @param theMessage The message.
     */
    public ChatMessage(Player theOwner, String theMessage){
        assert(theMessage.length() >= 1) : "Message length too short!";
        
        owner = theOwner;
        chatMessage = theMessage;
    }
    
    /**
     * Gives the message.
     * @return The message
     */
    public String getChatMessage(){
        return chatMessage;
    }
    
    /**
     * Gives the owner.
     * @return Owner of the message.
     */
    public Player getOwner(){
        return owner;
    }
}
