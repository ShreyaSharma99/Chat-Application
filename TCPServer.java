import java.io.*; 
import java.net.*; 
import java.util.*;
 
class TCPServer { 

  public static HashMap<String, SocketConnection> sendSocketMap = new HashMap<>(); 
  public static HashMap<String, SocketConnection> recSocketMap = new HashMap<>(); 
  public static HashMap<String, String> keyMap = new HashMap<>();

  class SocketConnection{ 
    // Instance Variables 
    String username; 
    Socket socket;
    BufferedReader inFromClient; 
    DataOutputStream outToClient; 
    
    // Constructor Declaration of Class 
    public SocketConnection(String username, Socket socket, BufferedReader inFromClient, DataOutputStream outToClient) 
    { 
        this.username = username; 
        this.socket = socket;
        this.inFromClient = inFromClient; 
        this.outToClient = outToClient; 
    } 
  
    // method 1 
    public String getUsername() 
    { 
        return username; 
    } 
  
    // method 2 
    public BufferedReader getInFromClient() 
    { 
        return inFromClient; 
    } 
  
    // method 3 
    public DataOutputStream getOutToClient() 
    { 
        return outToClient; 
    } 
  
    // method 4 
    public Socket getSocket(){
      return socket;
    }
} 

  public String findSender(Socket connectionSocket){
    String sender="";
    for(Map.Entry<String,SocketConnection> entry: sendSocketMap.entrySet()){
      System.out.println(entry.getValue().getUsername());
      if(entry.getValue().getSocket()==connectionSocket){
          sender = (String)entry.getKey();
          break; //breaking because its one to one map
      }
    }
    return sender;    
  }

  public String findSenderR(Socket connectionSocket){
    String sender="";
    for(Map.Entry<String,SocketConnection> entry: recSocketMap.entrySet()){
      System.out.println(entry.getValue().getUsername());
      if(entry.getValue().getSocket()==connectionSocket){
          sender = (String)entry.getKey();
          break; //breaking because its one to one map
      }
    }
    return sender;    
  }

  public String createForwardMessage(String sender,String fwdMessage){
    String answer = "FORWARD "+ sender + "\n" + "Content-length: "+(fwdMessage.length())+"\n"+"\n"+fwdMessage;
    return answer;
  }

  public String[] readMessage(BufferedReader inFromClient, Socket connectionSocket, DataOutputStream outToClient) throws IOException{

    System.out.println("tosend2");
    String clientMessage = "";
    String serverReply = "";
    String fwdMessage = "";
    String errorMessage = "";
    String username = "";
    String receiver = "";
    String pubKey = "";
    try{
      System.out.println(inFromClient.readLine());
    while(inFromClient.ready()){
      clientMessage = inFromClient.readLine();
      String[] word = clientMessage.split("\\s+");
      System.out.println(word[0]);
        //when input message was for registration
        if(word[0].equals("REGISTER")){
          if(word[1].equals("TOSEND")){
            System.out.println("3");
            receiver = "send";
            username = word[2];
            if(checkUserName(username)){
              SocketConnection senderSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              sendSocketMap.put(username,senderSocket);
              clientMessage = inFromClient.readLine();
              if(clientMessage != null && clientMessage.length()==0){
                clientMessage = inFromClient.readLine();
                // if(clientMessage == null){
                  System.out.println("registed sender");
                  serverReply = "REGISTERED TOSEND "+username+"\n\n";
                  while(inFromClient.ready())
                    clientMessage = inFromClient.readLine();
                  break;
                // }
              }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              while(inFromClient.ready())
                clientMessage = inFromClient.readLine();
              break;
            }
          }
          else if(word[1].equals("TORECV")){
            receiver = "receive";
            username = word[2];
            if(checkUserName(username)){
              SocketConnection receiverSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              recSocketMap.put(username,receiverSocket);
              if(word[3].equals("Key:")){
                pubKey = word[4];
                keyMap.put(username,pubKey);
                clientMessage = inFromClient.readLine();
                if(clientMessage != null){
                  clientMessage = inFromClient.readLine();
                  if(clientMessage == null){
                    serverReply = "REGISTERED TORECV "+username+"\n\n";
                    while(inFromClient.ready())
                      clientMessage = inFromClient.readLine();
                    break;
                  }
                }
              }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              while(inFromClient.ready())
                clientMessage = inFromClient.readLine();
              break;
            }
          }
        }

        else if(word[0].equals("SEND")){
  
          username = word[1];
          if(!recSocketMap.containsKey(username)){
            serverReply = "ERROR 102 Unable to send\n\n";
            while(inFromClient.ready())
              clientMessage = inFromClient.readLine();
            break;
          }

          clientMessage = inFromClient.readLine();
          String[] word1 = clientMessage.split("\\s+");

          if(word1[0].equals("Content-length:")){
            int mssg_len = Integer.parseInt(word1[1]);
            if(inFromClient.readLine().length() == 0){
              fwdMessage = inFromClient.readLine();
              if(fwdMessage.length()!=mssg_len){
                serverReply = "ERROR - Content length and message length mismatch !";
                while(inFromClient.ready())
                  clientMessage = inFromClient.readLine();
                break;
              }
            } 
          }
        }

        else if(word[0].equals("RECEIVED")){
          while(inFromClient.ready())
            clientMessage = inFromClient.readLine();
          break;
        }

        else if(word[0].equals("DEREGISTER")){
          username = word[1];
          receiver = "deregister";
          while(inFromClient.ready())
            clientMessage = inFromClient.readLine();
          break;
        }
        else if(word[0].equals("FETCHKEY")){
          username = word[1];
          if(keyMap.containsKey(username)){
            pubKey = keyMap.get(username);
            while(inFromClient.ready())
              clientMessage = inFromClient.readLine();
            break;
          }
        }
        else if(word[0].equals("ERROR")){
          if(word[1].equals("103")){
            errorMessage = "Header incomplete";
            while(inFromClient.ready())
              clientMessage = inFromClient.readLine();
            break;
          }
        }

      }
    }catch(Exception e){
      System.out.println(e);
      while(inFromClient.ready())
        clientMessage = inFromClient.readLine();
      serverReply = "ERROR 103 Header incomplete \n\n";
    }
    String[] answer = new String[6];
    answer[0] = serverReply;
    answer[1] = fwdMessage;
    answer[2] = errorMessage;
    answer[3] = username;
    answer[4] = receiver;
    answer[5] = pubKey;
    return answer; 
  } 

  public boolean checkUserName (String un ){
    int len = un.length();
    //if(un.charAt(0) != '@') return false;
    for(int i=0; i<len; i++){
      if(!((((int)(un.charAt(i))>47)&&((int)(un.charAt(i))<58)) || (((int)(un.charAt(i))>64)&&((int)(un.charAt(i))<91)) || (((int)(un.charAt(i))>96)&&((int)(un.charAt(i))<123))))
        return false;
    }
    return true;
  }

  public static void main(String args[]) throws Exception 
    { 

      ServerSocket welcomeSocket = new ServerSocket(6789); 
      
      while(true) { 
        Socket connectionSocket = welcomeSocket.accept(); 

        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 
        System.out.println("Welcome");
        SocketThread socketThread = new SocketThread(connectionSocket, inFromClient, outToClient);
        System.out.println("Registration done");
        Thread thread = new Thread(socketThread);
        thread.run();  

      }
    }
} 

class SocketThread extends TCPServer implements Runnable {
  String serverReply;
  String fwdMessage; 
  String errorMessage;
  String username;
  String receiver;
  String pubKey;
  Socket connectionSocket = null;
  BufferedReader inFromClient1;
  DataOutputStream outToClient1;  //client 1 is the calling client
   

  public SocketThread(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient){
    System.out.println("I was here");
    this.connectionSocket = connectionSocket;
    this.inFromClient1 = inFromClient;
    this.outToClient1 = outToClient;
    // System.out.println("entered");
  } 

  public void run() {
    while(true) { 
     try {
      String[] str = new String[5];
      System.out.println("1");
       System.out.println("1st" + inFromClient1.ready());
      str = readMessage(this.inFromClient1, this.connectionSocket, this.outToClient1);
      System.out.println("2");
      serverReply = str[0];
      fwdMessage = str[1];
      errorMessage = str[2];
      username = str[3];
      receiver = str[4];
      pubKey = str[5];
      if(serverReply.length()!=0){
        this.outToClient1.writeBytes(serverReply);
      } 
      //check when server reply is an error message
      if(pubKey.length()!=0){
        this.outToClient1.writeBytes(pubKey);
        break;
      }
      else if(receiver.equals("deregister")){
        if(findSender(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          sendSocketMap.remove(username);
          connectionSocket.close();
          break;
        }
        else if(findSenderR(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          recSocketMap.remove(username);
          connectionSocket.close();
          break;
        }
        else{
          this.outToClient1.writeBytes("FAIL: User was not registered  \n");
          break;
        }
      }

      if(receiver.equals("receive")){
        break;
      }

      boolean is_error_possible = true;
      while(fwdMessage.length()!=0 && username.length()!=0 && is_error_possible){
        //forwrad message to receiving client
        SocketConnection forwardSocket = recSocketMap.get(username);
        String sender = findSender(connectionSocket);
        String forwardMessage = createForwardMessage(sender,fwdMessage);
        forwardSocket.getOutToClient().writeBytes(forwardMessage);
        // BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        // DataOutputStream outToClient2 = new DataOutputStream(forwardSocket.getOutputStream());
        // outToClient2.writeBytes(fwdMessage);
        // forwardSocket.getInFromClient() = new BufferedReader(new InputStreamReader(forwardSocket.getSocket().getInputStream()));
        String[] reply = new String[5];
        reply = readMessage(forwardSocket.getInFromClient(), forwardSocket.getSocket(), forwardSocket.getOutToClient()); 
        if(reply[2].length()==0) is_error_possible = false;
        //check when server reply is an error message
      }

      if(!is_error_possible){
        serverReply = "SENT " + username + "\n \n";
        this.outToClient1.writeBytes(serverReply);
      }

     }catch(Exception e) {
        try {
         connectionSocket.close();
        } catch(Exception ee) { }
        break;
     }
    } 
  }  
}
