import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
//================================

public class Server {

    //Server config
    static private int Port = 8314;// change this for port
    static public  int ThreadMax = 200; // change for the max amount of client connections the sever can have
    static public int TimeoutAttemts = 3;//how many times it will re send a message before desconecting due to timeout
    static public int TimeoutAttemtsInterval = 1;//seconds between atemts
    static public int TimeoutWrongLimit=6;// after how many wrong messages will it disconect (both wrong time and uninteligable)
    static private int ThreadCount=0;

    public static void main(String args[]) {
        ServerSocket listener = null;
        Socket socketOfServer = null;
        //initialising the encription
        Encryption.GenerateKeys();
        Encryption.InitDecrypCipher();
        Accounts.AccountsINIT();
        try {
            listener = new ServerSocket(Port);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }

        try {
            System.out.println("~Server is waiting to accept user...");
            while(true){

                StartConnectionThread(listener.accept());
                System.out.println("~Accept a client!, creating thread");
            }
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }
        System.out.println("~Sever stopped!");
    }

    static private ConnectionThread StartConnectionThread(Socket _socket){
        if(ThreadCount==ThreadMax){//checks if the thread limit has been reached
            System.err.println("&Thread Limit Reached");
            return null;
        }
        try{
            //sets up the connection to be handed off the the thread
            BufferedReader is=new BufferedReader(new InputStreamReader(_socket.getInputStream()));;
            BufferedWriter os=new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
            ConnectionThread Conn = new ConnectionThread(_socket, is, os);
            Conn.start();
            return Conn;
        }catch (Exception e) {
            System.err.println("&Failed to create thread"+e);
            e.printStackTrace();
            return null;
        }
    }
    static public void ThreadFinished(){
        //is called by thread to infrom the main thread that they are done
        ThreadCount -=1;
    }
}