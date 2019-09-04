import java.io.*; 
import java.net.*; 
import java.util.*;
import java.lang.*;
import java.security.*;
class TCPClient extends CryptographyExample { 

    static String username;
    static InetAddress serverAddress;
    static KeyPair key;
    static MessageDigest md;

    class SocketThread implements Runnable { 
     Socket connectionSocket;
     BufferedReader input;
     BufferedReader input_from_server;
     DataOutputStream output;
     DataOutputStream output_to_server;
     boolean if_receiving;

     SocketThread(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToServer, BufferedReader inputServer) {
        this.connectionSocket = connectionSocket;
        this.input = inFromClient;
        this.output = outToServer;
        this.input_from_server = inputServer;
        this.output_to_server = this.output;
        if_receiving = false;
     } 

     SocketThread(Socket connectionSocket, BufferedReader inFromServer, DataOutputStream outToClient, DataOutputStream outputServer) {
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
     public void send_message(String username, long content_length, String message, String key) throws Exception{
        byte[] recipient_key = java.util.Base64.getDecoder().decode(key);
        byte[] encrypted_data = encrypt(recipient_key, message.getBytes());
        String message_encrypted = java.util.Base64.getEncoder().encodeToString(encrypted_data);
        byte[] hash = get_digest().digest(encrypted_data);
        byte[] private_key = get_key().getPrivate().getEncoded();
        byte[] encrypt_hash = encrypt(private_key,hash);
        String hash_base64 = java.util.Base64.getEncoder().encodeToString(encrypt_hash);
        String message_to_send = "SEND " + username + "\nContent-length: " + Long.toString(content_length) + "\n\n";
        message_to_send += message_encrypted + "\n" + hash_base64 +"\n";
        this.output_to_server.writeBytes(message_to_send + '\n');
     }

     public void respond_to_server_response(String message, String username) throws IOException{
        String[] words = message.split(" ");
        if(words[0].equals("SENT"))
            System.out.println("Message Successfully sent to "+ username);
        else{
            if(words[1].equals("102"))
                System.out.println("The message was unable to send because the recipient is not registered on network.");
            else{
                System.out.println("Aww! Snap. Connection broke down. Re-establishing the connection!");
                Socket clientSocketSend_new = new Socket(get_serverAddress(),6789);
                DataOutputStream outpToServer = new DataOutputStream(clientSocketSend_new.getOutputStream());
                BufferedReader inpServer = new BufferedReader(new InputStreamReader(clientSocketSend_new.getInputStream()));
                boolean is_success = registerSocket(get_username(),false,outpToServer,inpServer);
                this.connectionSocket = clientSocketSend_new;
                this.input_from_server = inpServer;
                this.output = outpToServer;
                this.output_to_server = this.output;
                System.out.println("Connection Re-established");
            }
        }
     }
     public void request_for_deregistration() throws IOException{
        String message_to_send = "DEREGISTER " + get_username() +"\n";
        this.output_to_server.writeBytes(message_to_send);
        String response = this.input_from_server.readLine();
        System.out.println("You have been successfully deregistered");
     }
     public void read_inp_from_user(){
        while(true) { 
            try {
                String recipient_username = "";
                boolean message_started = false;
                long content_length = 0;
                while(input.ready()){
                    String message = this.input.readLine();
                    if(message.equals("UNREGISTER")){
                        request_for_deregistration();
                        System.exit(0);
                    }
                    if(message == null)
                        System.out.println("Invalid Command");
                    int counter = 0;
                    while(message.charAt(counter) != ' '){
                        if(message.charAt(counter) != '@')
                            recipient_username += Character.toString(message.charAt(counter));
                        counter++; 
                    }
                    long message_content_length = 0;
                    String message_content = "";                    
                    if(counter != message.length()){
                        message_content = message.substring(counter+1,message.length());
                        message_content_length = message_content.length();
                    }
                    String fetch_key = "FETCHKEY " + recipient_username;
                    this.output_to_server.writeBytes(fetch_key);
                    String[] key_message = this.input_from_server.readLine().split(" ");
                    String key_recipient = key_message[1];
                    send_message(recipient_username, message_content_length, message_content, key_recipient);       
                    String server_response = "";
                    String temp = "";
                    while((temp = this.input_from_server.readLine()) != null){
                        if(temp.length() ==0)
                            continue;
                        else
                            server_response = temp;
                    }
                    respond_to_server_response(server_response, recipient_username);
                }
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

     public void send_received_ack(String sender_username) throws IOException{
        String received_ack = "RECEIVED "+ sender_username+"\n";
        this.output_to_server.writeBytes(received_ack + '\n');
     }
     public void send_error_ack() throws IOException{
        String error_ack = "ERROR 103 Header Incomplete"+"\n";
        this.output_to_server.writeBytes(error_ack + '\n');
     }

     public byte[] get_key_from_server(String username) throws IOException{
        String message_to_send = "FETCHKEY " + username;
        this.output_to_server.writeBytes(message_to_send);
        String response = this.input_from_server.readLine();
        byte[] pub_key = java.util.Base64.getDecoder().decode(response);
        return pub_key;
     }
     public void read_inp_from_server(){
        while(true) { 
            try {
                String sender_username = "";
                boolean message_started = false;
                long content_length = 0;
                while(input_from_server.ready()){
                    String message = this.input_from_server.readLine();
                    if(message == null)
                        break;
                    if(message.length() == 0){
                        if(message_started == true)
                            continue;
                        message_started = true;
                        continue;
                    }
                    if(!message_started){
                        String[] words = message.split(" ");
                        if(words[0].equals("FORWARD"))
                            sender_username = words[1];
                        if(words[0].equals("Content-length:"))
                            content_length = Long.parseLong(words[1]);
                    }
                    else{
                        String hash = this.input_from_server.readLine();
                        byte[] private_key = get_key().getPrivate().getEncoded();
                        byte[] decoded_data = java.util.Base64.getDecoder().decode(message);
                        byte[] hash1 = java.util.Base64.getDecoder().decode(hash);
                        byte[] hash_digested = get_digest().digest(decoded_data);
                        byte[] public_key_sender = get_key_from_server(sender_username);
                        byte[] decrypted_hash = decrypt(public_key_sender,hash1);
                        String decrypted_data = new String(decrypt(private_key, decoded_data));
                        message = decrypted_data;
                        if(((long)message.length() == content_length) && (decrypted_hash == hash_digested )){
                            System.out.println("Message from "+ sender_username);
                            System.out.println(message);
                            send_received_ack(sender_username);
                        }
                        else
                            send_error_ack();
                    }
                } 
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

    public String get_username(){
        return username;
    }
    
    public InetAddress get_serverAddress(){
        return serverAddress;
    }


    public KeyPair get_key(){
        return key;
    }

    public MessageDigest get_digest(){
        return md;
    }

    public static void registerSocketActivity(DataOutputStream sendSocketStream1, boolean if1receive, BufferedReader input_server_1, DataOutputStream sendSocketStream2, boolean if2receive, BufferedReader input_server_2, BufferedReader input_from_user) throws IOException{
        boolean sender_registered = registerSocket(username, if1receive, sendSocketStream1, input_server_1);
        System.out.println("Send register");
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

    public static void send_register_message(String username, boolean if_to_receive, DataOutputStream out_to_server) throws IOException{
        String message = "";
        byte[] public_key = key.getPublic().getEncoded();
        String pub_key = java.util.Base64.getEncoder().encodeToString(public_key);
        if(!if_to_receive)
            message = "REGISTER TOSEND "+ username + "\nKey: " + pub_key+"\n";
        else
            message = "REGISTER TORECV "+ username + "\n";
        out_to_server.writeBytes(message + '\n');
        return;
    }

    public static boolean check_if_registered(String message) throws IOException{
        BufferedReader parser = new BufferedReader(new StringReader(message));
        String sCurrentLine = parser.readLine();
        System.out.println("Line: " + sCurrentLine);
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

    public static boolean registerSocket(String username, boolean if_to_receive, DataOutputStream out_to_server, BufferedReader input_server) throws IOException{
       send_register_message(username, if_to_receive, out_to_server);
       String server_input = "";
       while(input_server.ready()){
        String temp = input_server.readLine();
        if(temp.length() == 0 ||temp==null)
            continue;
        else
            server_input = temp;
       }
       return (check_if_registered(server_input));
    }

    public static void main(String args[]) throws Exception 
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
        System.out.println("Hurrah");
        key = generateKeyPair();
        md = MessageDigest.getInstance("SHA-256");
        Socket clientSocketReceive = new Socket(serverAddress, 6789);  

        //------------------------**USER DETAILS RETRIEVED**-----------------------------//

        //Input and Output for Socket responsible for sending messages
        DataOutputStream outToServer = new DataOutputStream(clientSocketSend.getOutputStream());
        BufferedReader inServer = new BufferedReader(new InputStreamReader(clientSocketSend.getInputStream()));

        //Input and Output for Socket responsible for receiving messages
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocketReceive.getInputStream())); 
        DataOutputStream outToClient = new DataOutputStream(System.out);
        DataOutputStream outServer = new DataOutputStream(clientSocketReceive.getOutputStream());

        registerSocketActivity(outToServer, false,inServer,outServer, true, inFromServer, inFromUser);
        SocketThread send_thread = new SocketThread(clientSocketSend, inFromUser, outToServer, inServer);
        Thread send_socket_thread = new Thread(send_thread);
        SocketThread receive_thread = new SocketThread(clientSocketReceive, inFromServer, outToClient, outServer);
        Thread receive_socket_thread = new Thread(receive_thread);
        send_socket_thread.run();
        receive_socket_thread.run();
    } 
} 

