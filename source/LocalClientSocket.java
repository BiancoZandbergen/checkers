import java.io.*;
import java.beans.*;
import java.net.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.Timer;

/**
 * Client socket that is used for communication with the opponent client.
 */
public class LocalClientSocket extends Thread implements DefaultSocket, ActionListener, PropertyChangeListener{
    /**
     * Debug modus
     */
    private boolean networkDebug = false;
    
    /**
     * Property Change Service
     */
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    /**
     * The Game model.
     */
    private Model model;
    
    /**
     * The game frame
     */
    private GameFrame gameFrame;
    
    /**
     * The StartWindow
     */
    private StartWindowFrame startWindowFrame;
    
    /**
     * Hostname of the Server.
     */
    private String hostname;
    
    /**
     * Port used.
     */
    private int port;
    
    /**
     * Connected status.
     */
    private boolean connected = false;
    
    /**
     * Shows playing status.
     */
    private boolean isPlaying = false;
    
    /**
     * Socket used.
     */
    private Socket socket = null;
    
    /**
     * Socket object out.
     */
    private ObjectOutputStream objectOut = null;
    
    /**
     * Socket object in.
     */
    private ObjectInputStream objectIn = null;
    
    /**
     * Time needed to ping.
     */
    private Timer timeForPing = new Timer(5000, this);
    
    /**
     * Status of pong received.
     */
    private int receivedPong = 0;

    
    /**
     * Setup the socket
     * @param thisStartWindowFrame The frame that started the socket.
     */
    public LocalClientSocket(StartWindowFrame thisStartWindowFrame){
        startWindowFrame = thisStartWindowFrame;
    }
    
    /**
     * Connect the client socket to a host-server.
     * @param host IP or name.
     * @param portNumber Port nummer of host server.
     */
    public void connect(String host, int portNumber){
        hostname = host;
        port = portNumber;
        
        start();
        if (networkDebug) { System.out.print("Client socket is connecting to: "+hostname+":"+port);}
    }
    
    /**
     * Checks if the socket is connected to server.
     * @return isConnected
     */
    public boolean isConnected(){
        return connected;
    }
    
    /**
     * Checks if the game has started.
     * @return isConnected
     */
    public boolean isPlaying(){
        return isPlaying;
    }

    /**
     * The thread that will do the actual communication between server and client. Listens for incoming messages.
     */
    public void run(){
        //create new client socket
        try {
            connected = true;
            pcs.firePropertyChange("connected","","1");
            
            socket = new Socket(hostname, port);
        } catch (UnknownHostException e) {
            if (networkDebug) { System.out.println("Don't know about host: "+hostname+".");}
            connected = false;
        } catch (IOException e) {
            if (networkDebug) { System.out.println("Couldn't get I/O for the connection to: "+hostname+".");}
            connected = false;
        } catch (NullPointerException e){
            if (networkDebug) { System.out.println("Unknown exception happened while connecting to server.");}
            connected = false;
        }
    
        
        try {
            objectOut = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            if (networkDebug) { System.out.println("Error - getOutputStream");}
            connected = false;
        } catch (NullPointerException e){
            if (networkDebug) { System.out.println("Unknown exception happened: getOutputStream");}
            connected = false;
        }
        
        try {
            objectIn = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            if (networkDebug) { System.out.println("Error - getInputStream");}
            connected = false;
        } catch (NullPointerException e){
            if (networkDebug) { System.out.println("Unknown exception happened: getInputStream");}
            connected = false;
        }
        

        //Listen for incoming messages
        Transporter newTransporter = null;
        
        while(connected){ 
            try{
                try{
                    newTransporter = (Transporter)objectIn.readObject();
                }catch (ClassNotFoundException e){}    

                if (networkDebug) { System.out.println("\nClient received *something* from network. ");}
                    
                /* PING PONG EVENTS FOR TIMEOUT-HANDLING */
                if (newTransporter.getType()==2){
                    //received a ping. Must reply, to prevent the other side from timeing out.
                    newTransporter.setTask(3);
                    send(newTransporter);
                }else if (newTransporter.getType()==3){
                    //received a pong, which is a reply on my previous request.
                    receivedPong = 3;
                    if (networkDebug) { System.out.println("Client Received a reply on ping request");}
                    
                /* OTHER NETWORK TRAFFIC */
                }else if(newTransporter.getType()==1){
                    //received a reply on the play-request.
                    if (networkDebug) { System.out.println("Client: Received a reply on the play-request. Sending to panel. ");}
                    
                    pcs.firePropertyChange((PropertyChangeEvent)newTransporter.getValue1());
                    
                }else{
                    //received a model update.
                    if (networkDebug) { System.out.println("Clientsocket: Received a model-update"); }
                        
                    //Let the transporter object update the local model.
                    newTransporter.execute(model);
                }
            } catch (IOException e) {}
        }
    }       
    
    /**
     * Send a transport-object over the local socket to the other player.
     * @param send Transport object to be send.
     */
    public void send (Transporter send){
        if(connected){        
            try{
                objectOut.reset();
                objectOut.writeObject(send);
            } catch (IOException e) {
            } catch (Exception e){}
        }
    }
    
    /**
     * Send a model-sync over the local socket to the other player.
     * @param type Contains the type of task.
     */
    public void send (int type){
        Transporter transporter = new Transporter(); //create object to transport the info.
        transporter.setTask(type); //set the right task, so that the other player know what to do.
        this.send(transporter); //send through socket.
    } 
 
    /**
     * Send a model-sync over the local socket to the other player.
     * @param type Contains the type of task.
     * @param arg1 Object to send with transport.
     */
    public void send (int type, Object arg1){
        Transporter transporter = new Transporter(); //create object to transport the info.
        transporter.setTask(type, arg1); //set the right task, so that the other player know what to do.
        this.send(transporter); //send through socket.
    }
    
    /**
     * Send a model-sync over the local socket to the other player.
     * @param type Contains the type of task.
     * @param arg1 Object 1 to send with transport.
     * @param arg2 Object 2 to send with transport.
     */
    public void send (int type, Object arg1, Object arg2){
        Transporter transporter = new Transporter(); //create object to stransport the info.
        transporter.setTask(type, arg1, arg2); //set the right task, so that the other player know what to do.
        this.send(transporter); //send through socket.
    }
    

    /**
     * Set the state to 'playing'. This means that the actual game will be started,
     * and the start window will be closed.
     * @param input Playing boolean
     */
    public void setPlaying(boolean input){
        isPlaying = input;
        
        //start Gameframe and Model
        if (networkDebug) { System.out.println("Server: Created model and GameFrame. We are now playing: "+isPlaying);}
        
        //setup actual game
        model = new CheckerModel(this);
        gameFrame = new GameFrame(model, 2);
        model.addPropertyChangeListener(gameFrame);
        model.addPropertyChangeListener(this);
        
        //start timeout handler
        timeForPing.start();
    } 
    
    /**
     * Actionlistener listens for ping-pong network requests (time-out handling), and time-out events.
     * @param e Event with Action info
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
     * Close connection
     */
    public void disconnect(){
        timeForPing.stop();
        connected = false;
        
        try{
            socket.close();
        } catch (SocketException e) {
            if (networkDebug) { System.out.println("Error while disconnecting..)");    }
        } catch (IOException e) {
            if (networkDebug) { System.out.println("Error while disconnecting..)");}
        } catch (NullPointerException e){
            if (networkDebug) { System.out.println("error - nullpointer while disconnecting");}
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
            isPlaying = false;
            
            if (networkDebug) { System.out.println("Socket has received exitGame event");}
            gameFrame = null; //close current game, frame and panels
            model = null;
            
            //re-enable the start window
            startWindowFrame.setUnvisible();
        }
    }
}
