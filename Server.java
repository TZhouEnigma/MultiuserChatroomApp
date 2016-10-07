import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

/*
 * A chat server that delivers public and private messages.
 */
public class Server {

  // The server socket.
  private static ServerSocket serverSocket = null;
  // The user socket.
  private static Socket userSocket = null;

  // This chat server can accept up to maxUserCount users' connections.
  private static final int maxUserCount = 5;
  private static final userThread[] threads = new userThread[maxUserCount];

  public static void main(String args[]) {

    // The default port number.
    int portNumber = 8000;
    if (args.length < 1) {
      System.out
          .println("Usage: java MultiThreadChatServer <portNumber>\n"
              + "Now using port number=" + portNumber);
    } else {
      portNumber = Integer.valueOf(args[0]).intValue();
    }

    /*
     * Open a server socket on the portNumber (default 8000). Note that we can
     * not choose a port less than 1023 if we are not privileged users (root).
     */
    try {
      serverSocket = new ServerSocket(portNumber);
    } catch (IOException e) {
      System.out.println(e);
    }

    /*
     * Create a user socket for each connection and pass it to a new user
     * thread.
     */
    while (true) {
      try {
        userSocket = serverSocket.accept();
        int i = 0;
        for (i = 0; i < maxUserCount; i++) {
          if (threads[i] == null) {
            (threads[i] = new userThread(userSocket, threads)).start();
            break;
          }
        }
        if (i == maxUserCount) {
          PrintStream os = new PrintStream(userSocket.getOutputStream());
          os.println("Server too busy. Try later.");
          os.close();
          userSocket.close();
        }
      } catch (IOException e) {
        System.out.println(e);
      }
    }
  }
}

class userThread extends Thread {

  private DataInputStream is = null;
  private PrintStream os = null;
  private Socket userSocket = null;
  private final userThread[] threads;
  private int maxUserCount;
  public String temp;

  public userThread(Socket userSocket, userThread[] threads) {
    this.userSocket = userSocket;
    this.threads = threads;
    maxUserCount = threads.length;
  }

  public void run() {
    int maxUserCount = this.maxUserCount;
    userThread[] threads = this.threads;

    try {
      /*
       * Create input and output streams for this user.
       */
      is = new DataInputStream(userSocket.getInputStream());
      os = new PrintStream(userSocket.getOutputStream());
      os.println("***Enter your name. Do not start with the @ symbol***");
      String name;
      while(true){
      name = is.readLine().trim();
      if(!name.contains("@")){
        this.temp=name;
        break;
          }
         os.println("***enter again!***");
      }
      os.println("***Hello " + name
          + " to our chat room.\nTo leave enter LogOut in a new line. \nTo send a private message to somebody,use the @tag***");
      for (int i = 0; i < maxUserCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println(name
              + "has entered the chat room !!! ***");
        }
      }
      boolean flag=true;
      while (flag) {
        String line = is.readLine();
        if (line.startsWith("LogOut")) {
          flag=false;
        }
        if(line.startsWith("@")){
          for (int i = 0; i < maxUserCount; i++) {
            if(threads[i]!=null){
            int temp2=threads[i].temp.length();
            if(line.substring(1,temp2+1).equals(threads[i].temp) && !line.substring(1,temp2+1).equals(name)){
            threads[i].os.println("<" + name + "sends you a prrivate message"+"> " + line.substring(temp2+2));  
            }
            if(line.substring(1,temp2+1).equals(name)){
            this.os.println("***Cannot send a private message to yourself!***");
            }
          }
        }
        }
        else{
        for (int i = 0; i < maxUserCount; i++) {
          if (threads[i] != null) {
            threads[i].os.println("<" + name + "> " + line);
          }
        }
        }
      }
      for (int i = 0; i < maxUserCount; i++) {
        if (threads[i] != null && threads[i] != this) {
          threads[i].os.println(name
              + " is leaving the chat room !!! ***");
        }
      }
      os.println("*** Bye " + name + " ***");

      /*
       * Clean up. Set the current thread variable to null so that a new user
       * could be accepted by the server.
       */
      for (int i = 0; i < maxUserCount; i++) {
        if (threads[i] == this) {
          threads[i] = null;
        }
      }

      /*
       * Close the output stream, close the input stream, close the socket.
       */

      is.close();
      os.close();
      userSocket.close();
    } catch (IOException e) {
    }
  }
}