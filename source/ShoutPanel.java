import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

public class ShoutPanel extends JPanel implements PropertyChangeListener{
    
    private Model model;
    private int playerNumber;
    private boolean capturedStoneEvent = false;
    
    private JLabel shoutTextLabel = new JLabel();
    
    public ShoutPanel(Model theModel, int thePlayerNumber){
        assert thePlayerNumber == 1 || thePlayerNumber == 2 : "wrong playernumber";
        assert theModel != null : "Model is null";
        
        model = theModel;
        playerNumber = thePlayerNumber;
        
        this.setSize(320,180);
        this.add(shoutTextLabel);
        shoutTextLabel.setText(model.getRandomShoutmessage("good"));
        model.addPropertyChangeListener(this);
    }
    
    /*
     * property change event handler
    */
    public void propertyChange (PropertyChangeEvent event){

          //remise: neutral
          if(event.getPropertyName().equalsIgnoreCase("remiseReply")){
              shoutTextLabel.setText(model.getRandomShoutmessage("neutral"));
          }
          
          //stone has moved: neutral
          if(event.getPropertyName().equalsIgnoreCase("stoneMoved")){
              if (!capturedStoneEvent){
              shoutTextLabel.setText(model.getRandomShoutmessage("neutral"));
              }
              capturedStoneEvent = false;
          }
          
          //stone has been captured 
          if(event.getPropertyName().equalsIgnoreCase("stoneCaptured")){
              capturedStoneEvent = true;
              if((Integer)event.getNewValue()==playerNumber){
                  shoutTextLabel.setText(model.getRandomShoutmessage("good"));
              }else{
                  shoutTextLabel.setText(model.getRandomShoutmessage("negative"));
              }
          }
          
          //stone has been captured
          if(event.getPropertyName().equalsIgnoreCase("winGame")){
              if((Integer)event.getNewValue()==playerNumber){
                  shoutTextLabel.setText(model.getRandomShoutmessage("good"));
              }else{
                  shoutTextLabel.setText(model.getRandomShoutmessage("negative"));
              }
          }
    }
}
