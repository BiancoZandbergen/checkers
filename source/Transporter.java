import java.beans.PropertyChangeEvent;
import java.io.*;
import java.util.*;

/**
 * Wrapper class that can be transported over the network.
 * Holds objects and tasks for the remote side to execute.
 */
public class Transporter implements Serializable {    
    /**
     * Network debug modus.
     */
    private boolean networkDebug = false;
    
    /*
     * type
     * 1 = network handshake: event
     * 2 = ping (timeout-handling)
     * 3 = pong (timeout-handling)
     * 
     * 10 = newGame()
     * 11 = addChatMessage()
     * 12 = captureStoneonSquare()
     * 13 = changeTurn()
     * 14 = closeGame()
     * 15 = offerRemise()
     * 16 = setStoneOnSquare()
     * 17 = setWinGame
     * 18 = replyOnRemise
     * 
     */
     
    /**
     * Type of transport.
     */
    private int type;
    
    
    /**
     * First transport value.
     */
    private Object value1 = null;
    
    /**
     * Second transport value.
     */
    private Object value2 = null;
    

    /**
     * Sets task for transport.
     * @param task Task to set.
     */
    public void setTask(int task){
        assert (task >= 1 && task <= 3) || (task >= 10 && task <=18) : "wrong task number";
        
        type = task;
    }
    
    /**
     * Sets task for transport.
     * @param task Task to set.
     * @param object Object parameter for method.
     */
    public void setTask(int task, Object object){
        assert (task >= 1 && task <= 3) || (task >= 10 && task <=18) : "wrong task nummer";
        assert object != null : "object is null";
        
        type = task;
        value1 = object;
    }
    
    /**
     * Sets task for transport.
     * @param task Task to set.
     * @param object Object parameter for method.
     * @param object2 Object parameter for method.
     */
    public void setTask(int task, Object object, Object object2){
            assert (task >= 1 && task <= 3) || (task >= 10 && task <=18) : "wrong task nummer";
            assert object != null : "object is null";
            assert object2 != null : "object2 is null";
            
        type = task;
        value1 = object;
        value2 = object2;
    }
    
    /**
     * Execute tasks. Model will be updated.
     * @param model The Game model.
     */
    public void execute(Model model){
        assert model != null : "Model is null";
            
        if (networkDebug) { System.out.println("Executing transport-object on remote model."); }
        
        switch(type){
            case 11: model.addChatMessage           (false, (Integer)value1, (String)value2);      break;
            case 12: model.captureStoneOnSquare     (false, (SquareID)value1);                     break;
            case 14: model.closeGame                (false, (String)value1);                       break;
            case 15: model.offerRemise              (false, (Integer)value1);                      break;
            case 16: model.setStoneOnSquare         (false, (SquareID)value1,(SquareID)value2);    break;
            case 17: model.setWinGame               (false, (Integer)value1);                      break;
            case 18: model.replyToRemise            (false, (Boolean)value1) ;                     break;
        }
    }

    /**
     * Get object 1
     * @return Object parameter of transport
     */
    public Object getValue1() {
        return value1;
    }

    /**
     * Get object 2
     * @return Object parameter of transport
     */
    public Object getValue2() {
        return value2;
    }
    
    /**
     * Get task type
     * @return Type of transport.
     */
    public int getType() {
        return type;
    }
}
