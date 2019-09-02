import java.io.*; 
import java.net.*; 
class TCPClient { 

    static String username;
    static InetAddress serverAddress;

    public String get_username(){
        return username;
    }
    public String registerSocket(Socket socket_to_register, String username, boolean if_to_receive){

    }
    public static void main(String argv[]) throws Exception 
    { 
        String sentence, modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in)); 
        
        //-----------------------**START TAKING USER DETAILS**---------------------------//
        System.out.println("Enter your username:");
         //Get Username from User
        String username = inFromUser.readLine();

        System.out.println("Enter the Server IP Address:");
     
        serverAddress = InetAddress.getByName(inFromUser.readLine());

        Socket clientSocketSend = new Socket(serverAddress, 6789);
        Socket clientSocketReceive = new Socket(serverAddress, 6789);  

        //------------------------**USER DETAILS RETRIEVED**-----------------------------//

        //Input and Output for Socket responsible for sending messages
        DataOutputStream outToServer = new DataOutputStream(clientSocketSend.getOutputStream());
        BufferedReader inServer = new BufferedReader(new InputStreamReader(clientSocketSend.getInputStream()))

        //Input and Output for Socket responsible for receiving messages
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocketReceive.getInputStream())); 
        DataOutputStream outToClient = new DataOutputStream(System.out);
        DataOutputStream outServer = new DataOutputStream(clientSocketReceive.getOutputStream());

        SocketThread send_thread = new SocketThread(clientSocketSend, inFromUser, outToServer, inServer);
        Thread send_socket_thread = new Thread(send_thread);
        SocketThread receive_thread = new SocketThread(clientSocketReceive, inFromServer, outToClient, outServer);
        Thread receive_socket_thread = new Thread(receive_thread);
        send_socket_thread.run();
        receive_socket_thread.run();
    } 
} 

class SocketThread implements Runnable extends TCPClient { 
     Socket connectionSocket;
     BufferedReader input;
     BufferedReader input_from_server;
     DataOutputStream output;
     DataOutputStream output_to_server;
     boolean if_receiving;
   
     SocketThread (Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient, BufferedReader inputServer) {
        this.connectionSocket = connectionSocket;
        this.input = inFromClient;
        this.output = outToServer;
        this.input_from_server = inputServer;
        if_receiving = false;
     } 

     SocketThread (Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient, DataOutputStream outputServer) {
        this.connectionSocket = connectionSocket;
        this.input = inFromClient;
        this.output = outToClient;
        this.output_to_server = outputServer;
        if_receiving = true;
     }

     public void run() {

       while(true) { 
        try {
            registerSocket(this.connectionSocket,get_username(),if_receiving);
            clientSentence = inFromUser.readLine();
            capitalizedSentence = clientSentence.toUpperCase() + '\n'; 
            outToClient.writeBytes(capitalizedSentence); 
        } 
        catch(Exception e) {
            try {
              connectionSocket.close();
            } 
            catch(Exception ee) { }
            break;
             }
        } 
    }
}

