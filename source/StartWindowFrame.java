import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.net.*;
import javax.swing.*;

/**
 * Creates frame and panels that allows the player to create a network connection.
 * Also sets up the necessary sockets.
 */
public class StartWindowFrame extends JFrame implements ActionListener, PropertyChangeListener {
    /**
     * Network debug modus.
     */
    private boolean networkDebug = false;
    
    /**
     * Local server socket used.
     */
    private LocalServerSocket localServerSocket;
    
    /**
     * Local client socket.
     */
    private LocalClientSocket localClientSocket;
    
    /**
     * JPanel holding the controllers.
     */
    private JPanel controllers;
    
    /**
     * Field for the port nummer.
     */
    private JTextField portField = new JTextField();
    
    /**
     * Label with the server status.
     */
    private JLabel serverStatus = new JLabel();
    
    /**
     * Label with the 'Request Denied' text.
     */
    private JLabel denyLabel = new JLabel("   Prev. request was denied.");
    
    /**
     * Ip Field.
     */
    private JTextField cIpField = new JTextField();
    
    /**
     * Port field.
     */
    private JTextField cPortField = new JTextField();
    
    /**
     * Label with connecting text.
     */
    private JLabel connecting = new JLabel("   Incoming request");
    
    /**
     * Accept button.
     */
    private JButton accept = new JButton("Accept");
    
    /**
     * Deny button.
     */
    private JButton deny = new JButton("Deny");
    
    /**
     * Label with the ip.
     */
    private JLabel ipLabel = new JLabel(" ");
    
    
    /**
     * Startup the host-socket, and tells to draw the graphical windows.
     */
    public StartWindowFrame() {
        try{
             localServerSocket = new LocalServerSocket(this);
             if (networkDebug) { System.out.println("Listening server socket has been setup"); }
             
             localServerSocket.addPropertyChangeListener(this);
        } catch (NullPointerException e) {}
         
        draw();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }
    
    /**
     * Resume the login frame after a request for a new game has been received. (Or network error).
     */
    // unvisible?
    public void setUnvisible(){
        
        /* close old sockets */
        if(localServerSocket != null){
            if (networkDebug) { System.out.println("Closed serversocket");}
            localServerSocket.disconnect();
            localServerSocket = null;
        }
        
        if(localClientSocket != null){
            if (networkDebug) { System.out.println("Closed clientsocket");}
            localClientSocket.disconnect();
            localClientSocket = null;
        }
        
        /* start new server socket */
        try{
            localServerSocket = new LocalServerSocket(this);
            if (networkDebug) { System.out.println("Listening server socket has been setup"); }
            localServerSocket.addPropertyChangeListener(this);
        } catch (NullPointerException e) {}
        
        /* re-check for socket status: Update server status label on panel */
        try {
            //sleep one second, so that the socket-binding can take place.
            Thread.sleep(1000);
            if(localServerSocket.bind()){
                serverStatus.setText(" Yes, port "+localServerSocket.getPort());
            }else{
                serverStatus.setText(" No, port is in use.");
            }
        } catch (InterruptedException e) {}
        
        /* re-show frame */
        this.setVisible(true);
    }
    
    /**
     * Draws the needed frames, panels and controls.
     * These controls allow the user to setup a network connection.
     */
    public void draw(){
         //init
         setTitle("Checkers network connect");
         setUndecorated(false);
         
         //controls panel
         controllers = new JPanel(new GridLayout(12,2));
         
         JLabel ipConnect = new JLabel("   Connect to: ");
         controllers.add(ipConnect);
         
         //add localhost-ip by default
         try {
             InetAddress addr = InetAddress.getLocalHost();
             cIpField.setText(addr.getHostAddress()+"");
         } catch (UnknownHostException e) {}
         
         controllers.add(cIpField);
         JLabel hostConnect = new JLabel("   Port:");
         controllers.add(hostConnect);
         cPortField.setText((localServerSocket.getPort()+""));
         controllers.add(cPortField);
         denyLabel.setVisible(false);
         controllers.add(denyLabel);
         JButton clientSubmit = new JButton("   Connect!");
         clientSubmit.addActionListener(this);
         controllers.add(clientSubmit);
         
         //empty line
         controllers.add(new JLabel(" "));
         controllers.add(new JLabel(" "));
         controllers.add(new JLabel("   Local server running:             "));
         
         if(localServerSocket.bind()){
             serverStatus.setText(" Yes, port "+localServerSocket.getPort());
          }else{
             serverStatus.setText(" No, port is in use.");
         }
         
         controllers.add(serverStatus);
         controllers.add(new JLabel("   Set listening port:"));
         portField.setText((localServerSocket.getPort()+""));
         controllers.add(portField);
         controllers.add(new JLabel(" "));
         JButton portSubmit = new JButton("   Change");
         portSubmit.addActionListener(this);
         controllers.add(portSubmit);
         
         //empty line
         controllers.add(new JLabel(" "));
         controllers.add(new JLabel(" "));  
         connecting.setEnabled(false);
         controllers.add(connecting);
         controllers.add(ipLabel);     
         accept.setEnabled(false);
         accept.addActionListener(this);
         controllers.add(accept);         
         deny.setEnabled(false);
         deny.addActionListener(this);
         controllers.add(deny);
         
         //empty line
         controllers.add(new JLabel(" "));
         controllers.add(new JLabel(" "));
         controllers.add(new JLabel(" "));
         JButton exit = new JButton(" Exit");
         exit.addActionListener(this);
         controllers.add(exit);

         //add controls to panel
         controllers.setBorder(BorderFactory.createLineBorder(new Color(128,128,128), 2));
         this.add(controllers, BorderLayout.PAGE_START);
         
         //reduce the window to minimal size.
         pack();
    
         //Set window in middle of screen
         Toolkit kit = Toolkit.getDefaultToolkit();
         Dimension screensize = kit.getScreenSize();
         int frameX = (screensize.width - controllers.getWidth()) / 2;
         int frameY = (screensize.height - controllers.getHeight()) / 2;
         setLocation(frameX, frameY);
    }

    /**
     * If an event occurs, this method is called. For instance when a button has been pushed.
     * This method will take action after a control has been used.
     * It will also do the network-login. ("handshake").
     * @param event Event with the action info.
     */
    public void actionPerformed(ActionEvent event){
        //an event has been received
        
        //Exit button has been pushed
        if(event.getActionCommand()== " Exit"){
            if (networkDebug) { System.out.println("Exit button has been pressed."); }
            System.exit(-1);
        }
        
        //"change port" button has been pushed
        if(event.getActionCommand()== "   Change"){
            if (networkDebug) { 
                System.out.print("Changing local port to : ");
                System.out.print(portField.getText()+" ");
            }
            
            int port = 0;
            
            try {
                port = Integer.parseInt(portField.getText());  
            } catch (NumberFormatException e) {
                if (networkDebug) { System.out.print("Faulty port number!");}
            }
  
            //setup new host socket.
            if(port > 0){
                
                try{
                    localServerSocket.disconnect();
                }catch (NullPointerException e){}
                
                localServerSocket = new LocalServerSocket(this, port);
                localServerSocket.addPropertyChangeListener(this);
                
                if(localServerSocket.bind()){
                   serverStatus.setText(" Yes, port "+localServerSocket.getPort());
                }else{
                   serverStatus.setText(" No, port is in use."); 
                }
  
                if (networkDebug) { System.out.print("\n");}
            }
        }
        
        //"Connect" button has been pushed.
        if(event.getActionCommand()== "   Connect!"){
            if (networkDebug) { 
                System.out.print("Connecting to: ");      
                System.out.print(cIpField.getText());
                System.out.print(":"+cPortField.getText()+"\n");
            }
            
            int port = 0;
            
            try {
                port = Integer.parseInt(cPortField.getText());  
            } catch (NumberFormatException e) {
                if (networkDebug) { System.out.print("Faulty port number!");}
            }
            
            InetAddress addr = null;
            
            try {
               addr = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {}
          
            if(port > 0 && !(addr.getHostAddress().equals(cIpField.getText()) &&
               port==localServerSocket.getPort() && localServerSocket.bind())){
               
                //setup new client socket
                localClientSocket = new LocalClientSocket(this);
                localClientSocket.addPropertyChangeListener(this);
                
                if (networkDebug) { 
                    System.out.print("Connect: ");
                    System.out.print(cIpField.getText());
                    System.out.print(":"+cPortField.getText()+"\n");
                }
                
                //create new client-object
                localClientSocket.connect(cIpField.getText(),port);
               
                try {
                    //sleep one second, so that the socket-binding can take place.
                    Thread.sleep(1000);
                    if(!localClientSocket.isConnected()){
                        if (networkDebug) { System.out.println(" Error while connecting.");}
                    }
                } catch (InterruptedException e) {}
                
                //Create the connection request. ("handshake")
                Transporter loginReqTransporter = new Transporter();
                loginReqTransporter.setTask(1, new PropertyChangeEvent(this, "connect-request","","1"));
            
                //send out the request
                localClientSocket.send(loginReqTransporter);
            }
        }

        //(server) Accepted the request to play a game. Start server-game.
        if(event.getActionCommand()== "Accept"){
            if (networkDebug) { System.out.println("You accepted the request to play a game."); }
            
            //confirm to network
            localServerSocket.loginConfirm();
            
            //make frame invisible
            ipLabel.setText("");
            connecting.setEnabled(false);
            accept.setEnabled(false);
            deny.setEnabled(false);
            
            setVisible(false);
            repaint();
            
            //start rest of game everything
            localServerSocket.setPlaying(true);
        }
        
        //(server) Deny incoming request
        if(event.getActionCommand()== "Deny"){
            if (networkDebug) { System.out.println("You denied the request to play a game."); }
            
            localServerSocket.loginDeny();
            
            try {
                //clear socket for new requests
                localServerSocket.disconnect();
                localServerSocket = new LocalServerSocket(this, localServerSocket.getPort());
                localServerSocket.addPropertyChangeListener(this);
            }catch (NullPointerException e) {}
         
            ipLabel.setText("");
            connecting.setEnabled(false);
            accept.setEnabled(false);
            deny.setEnabled(false);
        }
    }

    /**
     * Listens for events sent out by the server socket. When the socket receives a request to play a game for instance.
     * @param event Event with the property changed.
     */
    public void propertyChange (PropertyChangeEvent event){
      //received an event from socket
      
      //sockets would like to say that it has bound to ip:port.
      if(event.getPropertyName().equals("bound")){
          
          //update status on panel.
          if(localServerSocket.bind()){
              serverStatus.setText(" Yes, port "+localServerSocket.getPort());
          }else{
              serverStatus.setText(" No, port is in use.");
          }
          repaint();
      }
            
      //(Server) received a request to play a game. Update panels so the user can accept/deny.
      if(event.getPropertyName().equals("connect-request")){
          if (networkDebug) {  System.out.println("You received a request to play a game.");}
          
          //make accept/deny buttons visible
          String [] ip = localServerSocket.GetRemoteIP().split("/");
          ipLabel.setText("   (From "+ip[1]+")");
          connecting.setEnabled(true);
          accept.setEnabled(true);
          deny.setEnabled(true);
          
          repaint();
      }    
            
      //(Client) sent request has been granted
      if(event.getPropertyName().equals("connect-confirm")){
          if (networkDebug) {  System.out.println("The request you sent to the server was accepted."); }
          
          //make invisible
          setVisible(false);
          repaint();

          //start rest of game
          localClientSocket.setPlaying(true);
          
          //disconnect server socket. Not needed anymore.
          localServerSocket.disconnect();
          localServerSocket = null;
      }

      //(Client) sent request has been denied
      if(event.getPropertyName().equals("connect-deny")){
          if (networkDebug) { System.out.println("The request you sent to the server was denied."); }
          
          //blink the error mssage
          boolean visible = true;
          for (int i=0; i < 12; i++){
              try {
                  Thread.sleep(500);
              } catch (InterruptedException e) {}
              denyLabel.setVisible(visible);
              visible = !visible;
          }
      }
    }
}
