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
      //System.out.println("find: "+entry.getValue().getUsername());
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
      if(entry.getValue().getSocket()==connectionSocket){
          sender = (String)entry.getKey();
          break; //breaking because its one to one map
      }
    }
    return sender;    
  }

  public String createForwardMessage_withHash(String sender,String fwdMessage,String contLen, String hash){
    String answer = "FORWARD "+ sender + "\n" + "Content-length: "+contLen+"\n\n"+fwdMessage+"\n" + hash +"\n\n";
    return answer;
  }

  public String createForwardMessage_withoutHash(String sender,String fwdMessage,String contLen, String hash){
    String answer = "FORWARD "+ sender + "\n" + "Content-length: "+contLen+"\n\n"+fwdMessage+"\n\n";
    return answer;
  }

  public String[] readMessage_mode1(BufferedReader inFromClient, Socket connectionSocket, DataOutputStream outToClient) throws IOException{
    String clientMessage = "";
    String serverReply = "";
    String fwdMessage = "";
    String errorMessage = "";
    String username = "";
    String receiver = "";
    String pubKey = "";
    String contLen = "";
    String hash = "";
    try{
    while(inFromClient.ready()){
      clientMessage = inFromClient.readLine();
      String[] word = clientMessage.split("\\s+");
        //when input message was for registration
        if(word[0].equals("REGISTER")){
          if(word[1].equals("TOSEND")){
            receiver = "send";
            username = word[2];
            if(checkUserName(username)){
              if(!check_if_already_there(username,1)){
                serverReply = "ERROR USERNAME IS ALREADY TAKEN!";
                break;
              }
              SocketConnection senderSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              sendSocketMap.put(username,senderSocket);
              clientMessage = inFromClient.readLine();

              if(clientMessage != null && clientMessage.length()==0){
                  serverReply = "REGISTERED TOSEND "+username+"\n\n";
                  break;
                }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
          else if(word[1].equals("TORECV")){
            receiver = "receive";
            username = word[2];
            if(checkUserName(username)){
              if(!check_if_already_there(username,2)){
                serverReply = "ERROR USERNAME IS ALREADY TAKEN!";
                break;
              }
              SocketConnection receiverSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              recSocketMap.put(username,receiverSocket);
              clientMessage = inFromClient.readLine();
              if(clientMessage != null){
                  serverReply = "REGISTERED TORECV "+username+"\n\n";
                  break;
              }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
        }

        else if(word[0].equals("SEND")){
  
          username = word[1];
          if(!recSocketMap.containsKey(username)){
            serverReply = "ERROR 102 Unable to send\n\n";
            break;
          }
          clientMessage = inFromClient.readLine();
          String[] word1 = clientMessage.split("\\s+");
          if(word1[0].equals("Content-length:")){
            int mssg_len = Integer.parseInt(word1[1]);
            contLen = word1[1];
            if(inFromClient.readLine().length() == 0){
              fwdMessage = inFromClient.readLine();
              String velliLine = inFromClient.readLine();
              break;
            } 
          }
        }

        else if(word[0].equals("RECEIVED")){
          break;
        }

        else if(word[0].equals("DEREGISTER")){
          username = word[1];
          receiver = "deregister";
          break;
        }
        else if(word[0].equals("ERROR")){
          if(word[1].equals("103")){
            errorMessage = "Header incomplete";
            break;
          }
        }
      }
    }catch(Exception e){
      while(inFromClient.ready())
        clientMessage = inFromClient.readLine();
      serverReply = "ERROR 103 Header incomplete \n\n";
    }
    String[] answer = new String[8];
    answer[0] = serverReply;
    answer[1] = fwdMessage;
    answer[2] = errorMessage;
    answer[3] = username;
    answer[4] = receiver;
    answer[5] = pubKey;
    answer[6] = contLen;
    answer[7] = hash;
    return answer; 
  } 

  public String[] readMessage_mode2(BufferedReader inFromClient, Socket connectionSocket, DataOutputStream outToClient) throws IOException{
    String clientMessage = "";
    String serverReply = "";
    String fwdMessage = "";
    String errorMessage = "";
    String username = "";
    String receiver = "";
    String pubKey = "";
    String contLen = "";
    String hash = "";
    try{
    while(inFromClient.ready()){
      clientMessage = inFromClient.readLine();
      String[] word = clientMessage.split("\\s+");
        if(word[0].equals("REGISTER")){
          if(word[1].equals("TOSEND")){
            receiver = "send";

            username = word[2];
            if(checkUserName(username)){
              if(!check_if_already_there(username,1)){
                serverReply = "ERROR USERNAME IS ALREADY TAKEN!";
                break;
              }
              SocketConnection senderSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              sendSocketMap.put(username,senderSocket);
              clientMessage = inFromClient.readLine();

              if(clientMessage != null && clientMessage.length()==0){
                  serverReply = "REGISTERED TOSEND "+username+"\n\n";
                  break;
                }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
          else if(word[1].equals("TORECV")){
            receiver = "receive";
            username = word[2];
            if(checkUserName(username)){
              if(!check_if_already_there(username,2)){
                serverReply = "ERROR USERNAME IS ALREADY TAKEN!";
                break;
              }
              SocketConnection receiverSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              recSocketMap.put(username,receiverSocket);
              if(word[3].equals("Key:")){
                pubKey = word[4];
                keyMap.put(username,pubKey);
                clientMessage = inFromClient.readLine();
                if(clientMessage != null){
                    serverReply = "REGISTERED TORECV "+username+"\n\n";
                    break;
                }
              }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
        }

        else if(word[0].equals("SEND")){
          username = word[1];
          if(!recSocketMap.containsKey(username)){
            serverReply = "ERROR 102 Unable to send\n\n";
            break;
          }

          clientMessage = inFromClient.readLine();
          String[] word1 = clientMessage.split("\\s+");

          if(word1[0].equals("Content-length:")){
            int mssg_len = Integer.parseInt(word1[1]);
            contLen = word1[1];
            if(inFromClient.readLine().length() == 0){
              fwdMessage = inFromClient.readLine();
              String velliLine = inFromClient.readLine();
              break;
            } 
          }
        }

        else if(word[0].equals("RECEIVED")){
          break;
        }

        else if(word[0].equals("DEREGISTER")){
          username = word[1];
          receiver = "deregister";
          break;
        }
        else if(word[0].equals("FETCHKEY")){
          username = word[1];
          if(keyMap.containsKey(username)){
            pubKey = keyMap.get(username);
            break;
          }
          else{
            serverReply = "ERROR 102 Unable to send\n\n";
            break;
          }
        }
        else if(word[0].equals("ERROR")){
          if(word[1].equals("103")){
            errorMessage = "Header incomplete";
            break;
          }
        }
      }
    }catch(Exception e){
      while(inFromClient.ready())
        clientMessage = inFromClient.readLine();
      serverReply = "ERROR 103 Header incomplete \n\n";
    }
    String[] answer = new String[8];
    answer[0] = serverReply;
    answer[1] = fwdMessage;
    answer[2] = errorMessage;
    answer[3] = username;
    answer[4] = receiver;
    answer[5] = pubKey;
    answer[6] = contLen;
    answer[7] = hash;
    return answer; 
  } 

  public String[] readMessage_mode3(BufferedReader inFromClient, Socket connectionSocket, DataOutputStream outToClient) throws IOException{
    String clientMessage = "";
    String serverReply = "";
    String fwdMessage = "";
    String errorMessage = "";
    String username = "";
    String receiver = "";
    String pubKey = "";
    String contLen = "";
    String hash = "";
    try{
    while(inFromClient.ready()){
      clientMessage = inFromClient.readLine();
      String[] word = clientMessage.split("\\s+");
        if(word[0].equals("REGISTER")){
          if(word[1].equals("TOSEND")){
            receiver = "send";
            username = word[2];
            if(checkUserName(username)){
              if(!check_if_already_there(username,1)){
                serverReply = "ERROR USERNAME IS ALREADY TAKEN!";
                break;
              }
              SocketConnection senderSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              sendSocketMap.put(username,senderSocket);
              clientMessage = inFromClient.readLine();

              if(clientMessage != null && clientMessage.length()==0){
                  serverReply = "REGISTERED TOSEND "+username+"\n\n";
                  break;
                }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
          else if(word[1].equals("TORECV")){
            receiver = "receive";
            username = word[2];
            if(checkUserName(username)){
              if(!check_if_already_there(username,2)){
                serverReply = "ERROR USERNAME IS ALREADY TAKEN!";
                break;
              }
              SocketConnection receiverSocket = new SocketConnection(username, connectionSocket, inFromClient, outToClient);
              recSocketMap.put(username,receiverSocket);
              if(word[3].equals("Key:")){
                pubKey = word[4];
                keyMap.put(username,pubKey);
                clientMessage = inFromClient.readLine();
                if(clientMessage != null){
                    serverReply = "REGISTERED TORECV "+username+"\n\n";
                    break;
                }
              }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
        }

        else if(word[0].equals("SEND")){
          username = word[1];
          if(!recSocketMap.containsKey(username)){
            serverReply = "ERROR 102 Unable to send\n\n";
            break;
          }
          clientMessage = inFromClient.readLine();
          String[] word1 = clientMessage.split("\\s+");
          if(word1[0].equals("Content-length:")){
            int mssg_len = Integer.parseInt(word1[1]);
            contLen = word1[1];
            if(inFromClient.readLine().length() == 0){
              fwdMessage = inFromClient.readLine();
              hash = inFromClient.readLine();
              String velliLine = inFromClient.readLine();
                break;
            } 
          }
        }
        else if(word[0].equals("RECEIVED")){
          break;
        }
        else if(word[0].equals("DEREGISTER")){
          username = word[1];
          receiver = "deregister";
          break;
        }
        else if(word[0].equals("FETCHKEY")){
          username = word[1];
          if(keyMap.containsKey(username)){
            pubKey = keyMap.get(username);
            break;
          }
          else{
            serverReply = "ERROR 102 Unable to send\n\n";
            break;
          }
        }
        else if(word[0].equals("ERROR")){
          if(word[1].equals("103")){
            errorMessage = "Header incomplete";
            break;
          }
        }
      }
    }catch(Exception e){
      while(inFromClient.ready())
        clientMessage = inFromClient.readLine();
      serverReply = "ERROR 103 Header incomplete \n\n";
    }
    String[] answer = new String[8];
    answer[0] = serverReply;
    answer[1] = fwdMessage;
    answer[2] = errorMessage;
    answer[3] = username;
    answer[4] = receiver;
    answer[5] = pubKey;
    answer[6] = contLen;
    answer[7] = hash;
    return answer; 
  }

  public boolean checkUserName (String un){
    int len = un.length();
    for(int i=0; i<len; i++){
      if(!((((int)(un.charAt(i))>47)&&((int)(un.charAt(i))<58)) || (((int)(un.charAt(i))>64)&&((int)(un.charAt(i))<91)) || (((int)(un.charAt(i))>96)&&((int)(un.charAt(i))<123))))
        return false;
    }
    return true;
  }

  public check_if_already_there(String un, int numb){
    if(numb==1){
      for(Map.Entry<String,SocketConnection> entry: sendSocketMap.entrySet()){
        if(entry.getValue().getUsername().equals(un))
            return false; //breaking because its one to one map
      }
    }
    else{
      for(Map.Entry<String,SocketConnection> entry: recSocketMap.entrySet()){
        if(entry.getValue().getUsername().equals(un))
            return false; //breaking because its one to one map
      }
    }
    return true;
  }

  public static void main(String argv[]) throws Exception{  
      ServerSocket welcomeSocket = new ServerSocket(6789); 

      while(true) { 
        Socket connectionSocket = new Socket();
        connectionSocket = welcomeSocket.accept(); 
        System.out.println(connectionSocket.getPort()+" Welcome");
       
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

        int mode = Integer.parseInt(argv[0]);

        if(mode == 1)
          SocketThread_mode1 socketThread = new SocketThread_mode1(connectionSocket, inFromClient, outToClient);
        else if(mode == 2)
          SocketThread_mode2 socketThread = new SocketThread_mode2(connectionSocket, inFromClient, outToClient);
        else
          SocketThread_mode3 socketThread = new SocketThread_mode3(connectionSocket, inFromClient, outToClient);
        Thread thread = new Thread(socketThread);
        thread.start();
      }
    }
} 

class SocketThread_mode1 extends TCPServer implements Runnable {
  String serverReply;
  String fwdMessage;
  String errorMessage; 
  String username;
  String receiver;
  String pubKey;
  String contLen;
  String hash;
  Socket connectionSocket = null;
  BufferedReader inFromClient1;
  DataOutputStream outToClient1;  //client 1 is the calling client
   

  public SocketThread_mode1(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient) throws IOException{
    this.connectionSocket = connectionSocket;
    this.inFromClient1 = inFromClient;
    this.outToClient1 =  outToClient; 
  } 

  public void run() {
    while(true) { 
     try {
      String[] str = new String[8];
      str = readMessage_mode1(this.inFromClient1, this.connectionSocket, this.outToClient1);
      serverReply = str[0];
      fwdMessage = str[1];
      errorMessage = str[2];
      username = str[3];
      receiver = str[4];
      pubKey = str[5];
      contLen = str[6];
      hash = str[7];
      if(serverReply.length()!=0)
        this.outToClient1.writeBytes(serverReply);
      //check when server reply is an error message
      if(pubKey.length()!=0){
        String output = "Key: "+ pubKey + "\n";
        this.outToClient1.writeBytes(output);
      }
      else if(receiver.equals("deregister")){
        if(findSender(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          sendSocketMap.remove(username);
          recSocketMap.remove(username);
          keyMap.remove(username);
          connectionSocket.close();
        }
        else if(findSenderR(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          recSocketMap.remove(username);
          connectionSocket.close();
        }
        else
          this.outToClient1.writeBytes("FAIL: User was not registered  \n");
      }
      boolean is_error_possible = true;
      while(fwdMessage.length()!=0 && username.length()!=0 && is_error_possible){
        //forwrad message to receiving client
        SocketConnection forwardSocket = recSocketMap.get(username);
        String sender = findSender(connectionSocket);
        String forwardMessage = createForwardMessage_withoutHash(sender,fwdMessage,contLen, hash);
        forwardSocket.getOutToClient().writeBytes(forwardMessage);
        boolean done = false;
        String[] reply = new String[8];
        if(!done){
          reply = readMessage_mode1(forwardSocket.getInFromClient(), forwardSocket.getSocket(), forwardSocket.getOutToClient());
          if(reply[5].length() != 0){
              String output1 = "Key: "+ reply[5] + "\n";
              forwardSocket.getOutToClient().writeBytes(output1);
              done = true;
          }
        }
        reply = readMessage_mode1(forwardSocket.getInFromClient(), forwardSocket.getSocket(), forwardSocket.getOutToClient());
        if(reply[2].length()==0) is_error_possible = false;
        //check when server reply is an error message
      }

      if(!is_error_possible){
        serverReply = "SENT " + username + "\n\n";
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

class SocketThread_mode2 extends TCPServer implements Runnable {
  String serverReply;
  String fwdMessage;
  String errorMessage; 
  String username;
  String receiver;
  String pubKey;
  String contLen;
  String hash;
  Socket connectionSocket = null;
  BufferedReader inFromClient1;
  DataOutputStream outToClient1;  //client 1 is the calling client
   

  public SocketThread_mode2(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient) throws IOException{
    this.connectionSocket = connectionSocket;
    this.inFromClient1 = inFromClient;
    this.outToClient1 =  outToClient; 
  } 

  public void run() {
    while(true) { 
     try {
      String[] str = new String[8];
      str = readMessage_mode2(this.inFromClient1, this.connectionSocket, this.outToClient1);
      serverReply = str[0];
      fwdMessage = str[1];
      errorMessage = str[2];
      username = str[3];
      receiver = str[4];
      pubKey = str[5];
      contLen = str[6];
      hash = str[7];
      if(serverReply.length()!=0)
        this.outToClient1.writeBytes(serverReply);

      //check when server reply is an error message
      if(pubKey.length()!=0){
        String output = "Key: "+ pubKey + "\n";
        this.outToClient1.writeBytes(output);
      }
      else if(receiver.equals("deregister")){
        if(findSender(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          sendSocketMap.remove(username);
          recSocketMap.remove(username);
          keyMap.remove(username);
          connectionSocket.close();
        }
        else if(findSenderR(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          recSocketMap.remove(username);
          connectionSocket.close();
        }
        else
          this.outToClient1.writeBytes("FAIL: User was not registered  \n");
      }

      boolean is_error_possible = true;
      while(fwdMessage.length()!=0 && username.length()!=0 && is_error_possible){
        //forwrad message to receiving client
        SocketConnection forwardSocket = recSocketMap.get(username);
        String sender = findSender(connectionSocket);
        String forwardMessage = createForwardMessage_withoutHash(sender,fwdMessage,contLen, hash);
        forwardSocket.getOutToClient().writeBytes(forwardMessage);
        reply = readMessage_mode2(forwardSocket.getInFromClient(), forwardSocket.getSocket(), forwardSocket.getOutToClient());
        if(reply[2].length()==0) is_error_possible = false;
        //check when server reply is an error message
      }

      if(!is_error_possible){
        serverReply = "SENT " + username + "\n\n";
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

class SocketThread_mode3 extends TCPServer implements Runnable {
  String serverReply;
  String fwdMessage;
  String errorMessage; 
  String username;
  String receiver;
  String pubKey;
  String contLen;
  String hash;
  Socket connectionSocket = null;
  BufferedReader inFromClient1;
  DataOutputStream outToClient1;  //client 1 is the calling client
   

  public SocketThread_mode3(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient) throws IOException{
    this.connectionSocket = connectionSocket;
    this.inFromClient1 = inFromClient;
    this.outToClient1 =  outToClient; 
  } 

  public void run() {
    while(true) { 
     try {
      String[] str = new String[8];
      str = readMessage_mode3(this.inFromClient1, this.connectionSocket, this.outToClient1);

      serverReply = str[0];
      fwdMessage = str[1];
      errorMessage = str[2];
      username = str[3];
      receiver = str[4];
      pubKey = str[5];
      contLen = str[6];
      hash = str[7];
      if(serverReply.length()!=0)
        this.outToClient1.writeBytes(serverReply);

      //check when server reply is an error message
      if(pubKey.length()!=0){
        String output = "Key: "+ pubKey + "\n";
        this.outToClient1.writeBytes(output);
      }
      else if(receiver.equals("deregister")){
        if(findSender(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          sendSocketMap.remove(username);
          recSocketMap.remove(username);
          keyMap.remove(username);
          connectionSocket.close();
        }
        else if(findSenderR(connectionSocket).length()!=0){
          this.outToClient1.writeBytes("SUCCESS \n");
          recSocketMap.remove(username);
          connectionSocket.close();
        }
        else
          this.outToClient1.writeBytes("FAIL: User was not registered  \n");
      }

      boolean is_error_possible = true;
      while(fwdMessage.length()!=0 && username.length()!=0 && is_error_possible){
        //forwrad message to receiving client
        SocketConnection forwardSocket = recSocketMap.get(username);
        String sender = findSender(connectionSocket);
        String forwardMessage = createForwardMessage_withHash(sender,fwdMessage,contLen, hash);
        forwardSocket.getOutToClient().writeBytes(forwardMessage);
        boolean done = false;
        String[] reply = new String[8];
        if(!done){
          reply = readMessage_mode3(forwardSocket.getInFromClient(), forwardSocket.getSocket(), forwardSocket.getOutToClient());
          if(reply[5].length() != 0){
              String output1 = "Key: "+ reply[5] + "\n";
              forwardSocket.getOutToClient().writeBytes(output1);
              done = true;
          }
        }
        reply = readMessage_mode3(forwardSocket.getInFromClient(), forwardSocket.getSocket(), forwardSocket.getOutToClient());
        if(reply[2].length()==0) is_error_possible = false;
        //check when server reply is an error message
      }

      if(!is_error_possible){
        serverReply = "SENT " + username + "\n\n";
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
