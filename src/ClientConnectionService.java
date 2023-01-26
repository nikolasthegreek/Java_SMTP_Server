import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
/*
 * this thread acts like the dispacher of the server so the MAIN thread can focus on managing the server
 */




public class ClientConnectionService extends Thread{
    private Log ServerLog;
    private int Port=0;
    private int ThreadCount=0;
    private int ThreadMax;
    private LinkedList<ServerInterfaceThread> Connections;

    ClientConnectionService(int _port,int _threadmax,Log _serverlog){
        ThreadMax = _threadmax;
        Port = _port;
        ServerLog= _serverlog;
    }

    public void run (){
        ServerSocket listener = null;
        Connections=new LinkedList<>();
        try{
            listener = new ServerSocket(Port);
        } catch (Exception e) {
            System.err.println("&failed to init lisener");
            ServerLog.WriteLog("CCS: FAILED TO START LISENER: "+e);
        }
        try {
            System.out.println("~Server is waiting to accept user...");
            ServerLog.WriteLog("CCS INIT COMPLEAT: READY FOR CLIENTS");
            while(true){
                Connections.add(StartConnectionThread(listener.accept()));
                System.out.println("~Accept a client!, created thread");
                
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            ServerLog.WriteLog("ERROR IN LISENER LOOP: "+e);
        }
        
    }
    private ServerInterfaceThread StartConnectionThread(Socket _socket){
        ServerLog.WriteLog("CLIENT CONNECTING IP:"+_socket.getRemoteSocketAddress().toString());
        if(ThreadCount==ThreadMax){//checks if the thread limit has been reached
            System.err.println("&Thread Limit Reached");
            ServerLog.WriteLog("FAILED TO START THREAD: TOO MANY ACTIVE THREADS");
            return null;
        }
        try{
            //sets up the connection to be handed off the the thread
            BufferedReader is=new BufferedReader(new InputStreamReader(_socket.getInputStream()));;
            BufferedWriter os=new BufferedWriter(new OutputStreamWriter(_socket.getOutputStream()));
            ServerInterfaceThread Conn = new ServerInterfaceThread(_socket, is, os,this);
            Conn.start();
            ServerLog.WriteLog("CLIENT THREAD STARTED");
            return Conn;
        }catch (Exception e) {
            System.err.println("&Failed to create thread"+e);
            e.printStackTrace();
            ServerLog.WriteLog("ERROR OCURED AT THREAD STARTUP: " +e);
            return null;
        }
    }
    public void ThreadFinished(){
        //is called by thread to infrom the main thread that they are done
        ThreadCount -=1;
        ServerLog.WriteLog("THREAD FINISHED");
    }
    public void KillConnections(){
        for (int i = 0; i < Connections.size(); i++) {
            Connections.get(i).TerminateThread();
        }
    }
}
