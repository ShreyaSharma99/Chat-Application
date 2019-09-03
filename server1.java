import java.io.*; 
import java.net.*; 


public static HashMap<String, Socket> socketMap = new HashMap<>(); 
public static HashMap<String, Integer> keyMap = new HashMap<>(); 

class TCPServer { 

  public String[] readMessage(String clientMessage, BufferedReader inFromClient, Socket connectionSocket){

    String serverReply = "";
    String fwdMessage = "";
    String errorMessage = "";
    String username = "";
    while(inFromClient.ready()){
      clientMessage = inFromClient.readLine();
      String[] word = clientMessage.split("\\s+");
      try{
        //when input message was for registration
        if(word[0].equals("REGISTER")){
          if(word[1].equals("TOSEND")){
            username = word[2];
            if(checkUserName(username)){
              socketMap.put(username,connectionSocket);
              clientMessage = inFromClient.readLine();
              if(clientMessage != null){
                clientMessage = inFromClient.readLine();
                if(clientMessage == null){
                  serverReply = "REGISTERED TOSEND "+username+"\n\n";
                  break;
                }
              }
            }
            else{
              serverReply = "ERROR 100 Malformed "+username+"\n\n";
              break;
            }
          }
          else if(word[1].equals("TORECV")){
            username = word[2];
            if(checkUserName(username)){
              socketMap.put(username,connectionSocket);
              clientMessage = inFromClient.readLine();
              if(clientMessage != null){
                clientMessage = inFromClient.readLine();
                if(clientMessage == null){
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
          if(!socketMap.containsKey(username)){
            serverReply = "ERROR 102 Unable to send\n";
            break;
          }

          clientMessage = inFromClient.readLine();
          String[] word1 = clientMessage.split("\\s+");

          if(word1[0].equals("Content-length:")){
            int mssg_len = Integer.parseInt(word[1]);
            if(inFromClient.readLine().length() == 0){
              String fwdMessage = inFromClient.readLine();
              if(fwdMessage.length()!=mssg_len){
                serverReply = "ERROR - Content length and message length mismatch !";
                break;
              }
            } 
          }
        }

        else if(word[0].equals("RECEIVED")){
          break;
        }

        else if(word[0].equals("ERROR")){
          if(word[1].equals("103")){
            errorMessage = "Header incomplete";
            break;
          }
        }

      }catch(Exception e){

      }
    }
    String[4] answer;
    answer[0] = serverReply;
    answer[1] = fwdMessage;
    answer[2] = errorMessage;
    answer[3] = username;
    return answer; 
  } 

  public boolean checkUserName (String un ){
    int len = un.length();
    if(un.charAt(0) != '@') return false;
    for(int i=1; i<len; i++){
      if!((((int)un.charAt(i)>47)&&((int)un.charAt(i)<58)) || (((int)un.charAt(i)>64)&&((int)un.charAt(i)<91)) || (((int)un.charAt(i)>96)&&((int)un.charAt(i)<123)))
        return false;
    }
    return true;
  }

  public static void main(String argv[]) throws Exception 
    { 

      ServerSocket welcomeSocket = new ServerSocket(6789); 
      
      while(true) { 
        Socket connectionSocket = welcomeSocket.accept(); 

        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 
        DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 

        SocketThread socketThread = new SocketThread(connectionSocket, inFromClient, outToClient);

        Thread thread = new Thread(socketThread);
        thread.run();  

      }
    }
} 
 
class SocketThread implements Runnable extends TCPServer {
  String clientMessage; 
  String serverReply;
  String fwdMessage; 
  String errorMessage;
  String username;
  Socket connectionSocket;
  BufferedReader inFromClient;
  DataOutputStream outToClient;
   
  SocketThread (Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient) {
    this.connectionSocket = connectionSocket;
    this.inFromClient = inFromClient;
    this.outToClient = outToClient;
  } 

  public void run() {
    while(true) { 
	   try {
      // clientMessage = inFromClient.readLine(); 
      // System.out.println(clientMessage);

      String[4] str = readMessage(clientMessage, inFromClient, connectionSocket);
      serverReply = str[0];
      fwdMessage = str[1];
      errorMessage = str[2];
      username = str[3];
      outToClient.writeBytes(serverReply); 

	   }catch(Exception e) {
		    try {
			   connectionSocket.close();
		    } catch(Exception ee) { }
		    break;
	   }
    } 
  }


  
}







