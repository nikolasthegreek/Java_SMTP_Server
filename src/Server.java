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
    static private String CMDString;

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
            ServerLog.WriteLog("INIT COMPLEAT");
            while(true){
                CMDString=Scann.nextLine();
                if(CMDString.equals("END")){
                    ServerLog.WriteLog("END COMMAND GIVEN");
                    break;
                }else if(CMDString.equals("CREATE ACCOUNT")){
                    CreateAccount();
                    System.out.println("~Account created");
                }else if(CMDString.equals("WIPE ACCOUNTS")){
                    WipeAccounts();
                    System.out.println("~Accounts wiped");
                }else{
                    System.out.println("~Did not understand command reference README");
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
    static void CreateAccount(){
        System.out.println("Write email:");
        String Email = Scann.nextLine();
        System.out.println("Password:");
        String Password = Scann.nextLine();
        Accounts.CreateAccount(Email, Password);
        ServerLog.WriteLog("ACCOUNT CREATED: "+Email);
    }
    static void WipeAccounts(){
        System.out.println("ARE YOU SURE FRFR say 'YES' to confirm");
        if(Scann.nextLine().equals("YES")){
            Accounts.WipeData();
            ServerLog.WriteLog("ACCOUNTS WIPED");
        }
        
    }

}