import java.net.*;
import java.util.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import javax.swing.Timer;

/**
 * Server socket that listens on a local ip:port. Is used for communicating between players.
 */
public class LocalServerSocket extends Thread implements DefaultSocket, ActionListener, PropertyChangeListener {
    /**
     * Debug modus.
     */
    private boolean networkDebug = false;
    
    /**
     * Property Change Object.
     */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    /**
     * The Game model.
     */
    private Model model;
    /**
     * The Game Frame
     */
    private GameFrame gameFrame;
    /**
     * The StartScherm
     */
    private StartWindowFrame startWindowFrame;
    /**
     * Port used.
     */
    private int port;
    /**
     * Socket bound status.
     */
    private boolean bind = false;
    /**
     * Status of connection.
     */
    private boolean connected = false;
    /**
     * Playing status.
     */
    private boolean playing = false;
    
    /**
     * Server socket used.
     */
    private ServerSocket serverSocket = null;
    /**
     * Client socket used.
     */
    private Socket clientSocket = null;
    /**
     * Socket Object out
     */
    private ObjectOutputStream objectOut = null;
    /**
     * Socket Object in.
     */
    private ObjectInputStream objectIn = null;
    
    /**
     * Ping time.
     */
    private Timer timeForPing = new Timer(5000, this);
    /**
     * Time to recieve pong.
     */
    private int receivedPong = 0;
    
    /**
     * Start the server socket at the default port. All IP's will be used.
     */
    public LocalServerSocket(StartWindowFrame thisStartWindowFrame){
        startWindowFrame = thisStartWindowFrame;
        
        //start with default port
        port = 6000;  
        start();
    }
    
    /**
     * Start the server socket at a specified port. All IP's will be used.
     * @param p Port used.
     */
    public LocalServerSocket(StartWindowFrame thisStartWindowFrame, int p){
        startWindowFrame = thisStartWindowFrame;
        
        //start with supplied port
        port = p;
        start();
    }
    
    /**
     * Returns the port that the socket is running on.
     * @return Port used.
     */
    public int getPort(){
        return port;
    }
    
    /**
     * Returns the bound state: True/false
     * @return boundState boolean
     */
    public boolean bind(){
        return bind;
    }
    
    /**
     * Returns the connection state: true/false.
     * @return isConnected status.
     */
    public boolean isConnected(){
        return connected;
    }
    
    /**
     * Returns the playing-state: True/false.
     * @return isPlaying status.
     */
    public boolean isPlaying(){
        return playing;
    }
    
    /**
     * Returns the IP of the remote player.
     * @return remoteIP in use.
     */
    public String GetRemoteIP(){
        return clientSocket.getInetAddress().toString();
    }
    

    /**
     * The thread that will do the actual socket setup, and listen for incoming messages.
     */
    public void run(){
        String inputLine;
        
        //setup new socket
        try {
            serverSocket = new ServerSocket(port);
            bind = true;
            if (networkDebug) { System.out.println("Socket has bound.");}
            
            pcs.firePropertyChange("bound","","1");
        } catch (IOException e) {
            bind = false;
            if (networkDebug) { System.out.println("Errorn while binding socket.");}
        }catch (NullPointerException e){
            if (networkDebug) { System.out.println("error - nullpointer");}
        }
        
        //only continue if the socket has bound
        if(bind){
            
            //Create client socket
            try {
                clientSocket = serverSocket.accept();
                connected = true;
                
                pcs.firePropertyChange("connected","","1");
                
            } catch (Exception e) {
                if (networkDebug) { System.out.println("Accept failed or canceled.");}
                connected = false;
            } 
            
            //Create the object transport streams
            try {
                objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (Exception e) {
                if (networkDebug) { System.out.println("Error - getOutputStream");}
            }
            try {
                objectIn = new ObjectInputStream(clientSocket.getInputStream());
            } catch (Exception e) {
                if (networkDebug) { System.out.println("Error - getInputStream");}
            }

            //Wait for received data. 
            while (connected) { 
                try{
                    Transporter newTransporter = null;
                    try{
                        newTransporter = (Transporter)objectIn.readObject();
                    }catch (ClassNotFoundException e){}    

                    if (networkDebug) { System.out.println("\nServer received *something* from network. ");}
                        
                    /* PING PONG EVENTS FOR TIMEOUT-HANDLING */
                    if (newTransporter.getType()==2){
                        //received a ping. Must reply, to prevent the other side from timeing out.
                        newTransporter.setTask(3);
                        send(newTransporter);
                    }else if (newTransporter.getType()==3){
                        //received a pong, which is a reply on my previous request.
                        receivedPong = 3;
                        if (networkDebug) { System.out.println("Server Received a reply on ping request");}
                        
                        /* OTHER NETWORK TRAFFIC */
                    }else if(newTransporter.getType()==1){
                        //received a request to play. Ask panel.
                        if (networkDebug) { System.out.println("LocalServerSocket Received request to play a game.");}
                        
                        pcs.firePropertyChange((PropertyChangeEvent)newTransporter.getValue1());
                    }else{
                        //received a model update.
                        if (networkDebug) { System.out.println("Serversocket: Received a model-update"); }
                        
                        //Let the transporter object update the local model.
                        newTransporter.execute(model);
                    } 
                } catch (IOException e) {}
            }
        } 
    }
    
    /**
     * Send a transport-object over the local socket to the other player.
     * @param send Object to send with transport.
     */
    public void send (Transporter send){
        if(connected){
            try{
                objectOut.reset(); //reset - hack to force sending.
                objectOut.writeObject(send);
            } catch (IOException e) {
                if (networkDebug) { System.out.println("ERROR while sending to network.");}
            }
        }
    }
    
    /**
     * Send a model-sync over the local socket to the other player.
     * @param type Type of transport, used to define method.
     */
    public void send (int type){
        Transporter transporter = new Transporter(); //create object to transport the info.
        transporter.setTask(type); //set the right task, so that the other player know what to do.
        this.send(transporter); //send through socket.
    } 
 
    /**
     * Send a model-sync over the local socket to the other player.
     * @param type Type of transport.
     * @param arg1 Object to send with transport.
     */
    public void send (int type, Object arg1){
        Transporter transporter = new Transporter(); //create object to transport the info.
        transporter.setTask(type, arg1); //set the right task, so that the other player know what to do.
        this.send(transporter); //send through socket.
    }
    
    /**
     * Send a model-sync over the local socket to the other player.
     * @param type Type of transport.
     * @param arg1 Object 1 to send with transport.
     * @param arg2 Object 2 to send with transport.
     */
    public void send (int type, Object arg1, Object arg2){
        Transporter transporter = new Transporter(); //create object to transport the info.
        transporter.setTask(type, arg1, arg2); //set the right task, so that the other player know what to do.
        this.send(transporter); //send through socket.
    }
    
    /**
     * Part of the login ("handshake") protocol. Will be called if the player decides to accept a game invite.
     * Sends out a confirmation on wanting to play a game.
     */
    public void loginConfirm(){
        //create new transporter-object
        Transporter transporter = new Transporter();
        PropertyChangeEvent event = new PropertyChangeEvent(this, "connect-confirm","","1");      
        transporter.setTask(1, event);
        
        //send request
        send(transporter);
        if (networkDebug) { System.out.println("Server: Sent confirmation on play-request");}
        
    }
    
    /**
     * Part of the login ("handshake") protocol. Will be called if the player decides to decline a game invite.
     * Sends out a deny on wanting to play a game.
     */
    public void loginDeny(){
        //create new transporter-object
        Transporter transporter = new Transporter();
        PropertyChangeEvent event = new PropertyChangeEvent(this, "connect-deny","","1");      
        transporter.setTask(1, event);
        
        //send request
        send(transporter);
        if (networkDebug) { System.out.println("Server: Sent deny on play-request");}
    }
    
    /**
     * Set the state to 'playing'. This means that the actual game will be started,
     * and the start window will be closed.
     * @param input Player to set.
     */
    public void setPlaying(boolean input){
        playing = input;
        
        //start Gameframe and Model
        if (networkDebug) { System.out.println("Server: Created model and GameFrame");}
        
        //Create model and the actual game.
        model = new CheckerModel(this);
        gameFrame = new GameFrame( model, 1);
        model.addPropertyChangeListener(gameFrame);
        model.addPropertyChangeListener(this);
        
        if (networkDebug) { System.out.println("Everything is setup..");}
        
        //start the ping-pong traffic flow (timeout-handling)
        timeForPing.start();
    }
   
       
    /**
     * Do a graceful disconnect of the socket.
     */
    public void disconnect(){
        playing = false;
        timeForPing.stop();
         
        if(bind){
            if(connected){
                if (networkDebug) { System.out.println("Disconnecting bound & connected server socket.");}
                try {
                    objectOut.close();
                    objectIn.close();
                    clientSocket.close();
                    serverSocket.close();
                } catch (SocketException e) {
                    if (networkDebug) { System.out.println("Error while disconnecting..)");    }    
                } catch (IOException e) {
                    if (networkDebug) { System.out.println("Error while disconnecting..)");}
                } catch (NullPointerException e){
                    if (networkDebug) { System.out.println("error - nullpointer while disconnecting");}
                }            
            }else{
                if (networkDebug) { System.out.println("Disconnecting bound but NOT connected server socket.");}
                try {
                    serverSocket.close(); 
                } catch (SocketException e) {
                    if (networkDebug) { System.out.println("Error while disconnecting..)");}    
                } catch (IOException e) {
                    if (networkDebug) { System.out.println("Error while disconnecting..)");}
                } catch (NullPointerException e){
                    if (networkDebug) { System.out.println("error - nullpointer while disconnecting");}
                }
            }
            
            connected = false;
        } 
    }
    
    /**
     * Actionlistener listens for ping-pong network requests (time-out handling), and time-out events.
     * @param e Event with action info.
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource().equals(timeForPing)){ 
             if(receivedPong == 0 || receivedPong == 1){
                 receivedPong++;
                 
                 if (networkDebug) { System.out.println("Client Did not receive a reply on Ping request! Trying again, re-sending.");}
                 
                 Transporter pingTransporter = new Transporter();
                 pingTransporter.setTask(2); //set ping-task
                 send(pingTransporter);
                 if (networkDebug) { System.out.println("Client sent a ping request.");}
             }
             
             if(receivedPong == 2){
                 //never received a reply on the previous request!
                 if (networkDebug) { System.out.println("Client Did not receive a reply on Ping request! Tried re-sending. Still no reply. Closing game.");}
                 model.closeGame(false, "Connection error.");
             }
             
             if(receivedPong == 3){
                 
                 //received a pong reply. 
                 // Now reset, and send a new Ping one
                 
                 receivedPong = 0;
                 
                 Transporter pingTransporter = new Transporter();
                 pingTransporter.setTask(2); //set ping-task
                 send(pingTransporter);
                 if (networkDebug) { System.out.println("Client sent a ping request.");}
             }
         }
    }

    
    /**
     * Subscribe objects to events sent out by this socket.
     * @param a Object to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener a){
        pcs.addPropertyChangeListener(a);
    }
    
    /**
     * Process received events. Events usually mean that the Model has been changed.
     * @param event An propertyChangeEvent
     */
    public void propertyChange (PropertyChangeEvent event){
        //The model has changed.
        
        //Exit the game gracefully
        if(event.getPropertyName().equalsIgnoreCase("exitGame")){
            playing = false;
            
            if (networkDebug) { System.out.println("Socket has received exitGame event");}
            gameFrame = null; //close current game, frame and panels
            model = null;
            
            //re-enable the startscherm
            startWindowFrame.setUnvisible();
        }
    }
}
