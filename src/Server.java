import java.util.Scanner;
public class Server {

    //Server config
    static private int Port = 8314;// change this for port
    static public  int ThreadMax = 200; // change for the max amount of client connections the sever can have
    static public int TimeoutAttemts = 3;//how many times it will re send a message before desconecting due to timeout
    static public int TimeoutAttemtsInterval = 1;//seconds between atemts
    static public int TimeoutWrongLimit=6;// after how many wrong messages will it disconect (both wrong time and uninteligable)
    static private Log ServerLog;
    static private Scanner Scann;
    public static void main(String args[]) {
        Log.LOGSINIT();
        ServerLog = new Log("Server");//starts servers log
        Encryption.GenerateKeys();
        Encryption.InitDecrypCipher();
        Accounts.AccountsINIT();
        Scann=new Scanner(System.in);
        ClientConnectionService CCS = new ClientConnectionService(Port, ThreadMax,ServerLog);
        CCS.start();
        try {
            System.out.println("~Server is waiting to accept user...");
            ServerLog.WriteLog("INIT COMPLEAT");
            while(true){
                if(Scann.nextLine().equals("END")){
                    ServerLog.WriteLog("END COMMAND GIVEN");
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
            ServerLog.WriteLog("ERROR IN MAIN LOOP: "+e);
        }

        CCS.KillConnections();
        CCS.stop();
        //the thread is stuck looking for clients
        //the problems with this method dont aply in our use
        System.out.println("~Sever stopped!");
        ServerLog.TerminateLog();
    }
}