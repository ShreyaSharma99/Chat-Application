import java.io.*; 
import java.net.*; 
import java.util.*;
import java.lang.*;
class TCPClient { 

    static String username;
    static InetAddress serverAddress;

    public String get_username(){
        return username;
    }
    
    public void registerSocketActivity(DataOutputStream sendSocketStream1, boolean if1receive, BufferedReader input_server_1, DataOutputStream sendSocketStream2, boolean if2receive, BufferedReader input_server_2, BufferedReader input_from_user){
        boolean sender_registered = registerSocket(username, if1receive, sendSocketStream1, input_server_1);
        boolean receiver_registered = registerSocket(username, if2receive, sendSocketStream2,input_server_2);
        if(sender_registered && receiver_registered)
            return;
        else{
            System.out.println("Do you wish to continue anyway or Exit? Y or N or E");
            String response = input_from_user.readLine();
            if(response.equals("Y"))
                return;
            else{
                if(response.equals("N"))
                    registerSocketActivity(sendSocketStream1, if1receive, input_server_1,sendSocketStream2, if2receive, input_server_2, input_from_user);
                else
                    System.exit(1);
            }
        }
        return;
    }

    public void send_register_message(String username, boolean if_to_receive, DataOutputStream out_to_server){
        String message = "";
        if(!if_to_receive)
            message = "REGISTER TOSEND "+ username + "\n";
        else
            message = "REGISTER TORECV "+ username + "\n";
        out_to_server.writeBytes(message + '\n');
        return;
    }
    public boolean registerSocket(String username, boolean if_to_receive, DataOutputStream out_to_server, BufferedReader input_server){
       send_register_message(username, if_to_receive, out_to_server);
       String server_input = "";
       server_input = input_server.readLine();
       return (check_if_registered(server_input));
    }

    public boolean check_if_registered(String message){
        BufferedReader parser = new BufferedReader(new StringReader(message));
        String sCurrentLine = parser.readLine();
        String[] words = sCurrentLine.split(" ");
            if(words[0].equals("ERROR")){
                if(words[1].equals("100"))
                    System.out.println("Registration Failed. Username entered is invalid.");
                else
                    System.out.println("Registration Failed. You are not registered yet.");
                return false;    
            }
            return true;
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

        registerSocketActivity(outToServer, false,inServer, outServer,true, inFromServer, inFromUser);
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
        this.output_to_server = this.output;
        if_receiving = false;
     } 

     SocketThread (Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient, DataOutputStream outputServer) {
        this.connectionSocket = connectionSocket;
        this.input = inFromServer;
        this.output = outToClient;
        this.output_to_server = outputServer;
        this.input_from_server = this.input;
        if_receiving = true;
     }


     public void run() {
        if(!this.if_receiving)
            read_inp_from_user();
        else
            read_inp_from_server();      
    }
    public void read_inp_from_user(){
        while(true) { 
            try {
                if(if_receiving){
                    
                }
                else{
                    server_input = this.input_from_server.readLine();

                    user_input = this.input.readLine();
                    
                }
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
    public void read_inp_from_server(){
        while(true) { 
            try {
                String message = this.
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

