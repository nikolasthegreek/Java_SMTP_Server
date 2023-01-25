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
        Log.LOGSINIT();
        Log ServerLog = new Log("Server");//starts servers log


        ServerLog.WriteLog("Test123123");
        ServerLog.WriteLog("zzzzzzz for 2");
        try{
            Thread.sleep(2000);
        }catch(Exception e){

        }
        ServerLog.WriteLog("gday");
        ServerLog.TerminateLog();
        







        //remove comment to generate test data
        //dont about running twice they wont double
        //these are kept here painly to be a refrence for testing login credentials
        /*
        Accounts.CreateAccount("guy@gmail.com", "dude");
        Accounts.CreateAccount("nick@yahoo.name", "ggwp");
        Accounts.CreateAccount("phil@yahoo.name", "ggwp");
        Accounts.CreateAccount("bro@mc-class.com", "OMG");
        Accounts.CreateAccount("nick@gmail.com", "ggwp");
        Accounts.SaveData();
        */
        try{
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

    static private ServerInterfaceThread StartConnectionThread(Socket _socket){
        if(ThreadCount==ThreadMax){//checks if the thread limit has been reached
            System.err.println("&Thread Limit Reached");
            return null;
        }
        try{
            //sets up the connection to be handed off the the thread
            BufferedReader is=new BufferedReader(new InputStreamReader(_socket.getInputStream()));;
            BufferedWriter os=new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
            ServerInterfaceThread Conn = new ServerInterfaceThread(_socket, is, os);
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