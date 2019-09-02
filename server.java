import java.io.*; 
import java.net.*; 

class TCPServer { 

  HashMap<String, Socket> socketMap = new HashMap<>(); 
  HashMap<String, Integer> keyMap = new HashMap<>(); 

  public static boolean parseMessage(String message, Socket clientSocket, int key){
    String line;
    String username;

    if (message.hasNextLine()) {
            line = message.nextLine();
            String word = line.next();

            if(!word.equals("SEND")){
              System.out.println("SEND token missing !");
              System.ot.println("ERROR 103 Header incomplete\n \n");
              return false;
            }

            if(!line.hasNext()){
              System.out.println("Username missing !");
              System.ot.println("ERROR 103 Header incomplete\n \n");
              return false;
            }
            
            word = line.next();

            if(!socketMap.containsKey(word)){
              System.out.println("ERROR 101 No user registered\n \n");
            }
            else {
              clientSocket = socketMap.get(word);
              key = keyMap.get(word);
            }

            if(message.hasNextLine()){
              line = message.nextLine();
              word = line.next();
              if(!word,equals("Content-length:"))
                return false;
            }

            if(!message.hasNextLine()){
              System.out.println("Wrong format! Leave one line before the starting of the message.");
              System.ot.println("ERROR 103 Header incomplete\n \n");
              return false;
            }
            else{
              line = message.nextLine();
              if(line.hasNext()){
                System.out.println("Wrong format! Leave one line before the starting of the message.");
                System.ot.println("ERROR 103 Header incomplete\n \n");
                return false;
              }
            }
    }
    else{
      System.out.println("Empty message!");
      System.ot.println("ERROR 103 Header incomplete\n \n");
      return false;
    }
     return true;
  }

  public static void main(String argv[]) throws Exception 
    { 
      String clientMessage; 
      String forwardMessage; 
      String replyFromServer;

      ServerSocket welcomeSocket = new ServerSocket(6789); 
  
      Socket connectionSocket = welcomeSocket.accept(); 

      BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream())); 

      DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream()); 

     while(true) { 
         clientMessage = inFromClient.readLine(); 

	   System.out.println(clientMessage);


           forwardMessage = clientMessage.toUpperCase() + '\n'; 

           outToClient.writeBytes(forwardMessage); 
        } 
    } 
} 
 

