import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

public class ServerInterfaceThread extends Thread {
    private ClientConnectionService CCS;//the "dispacher"
    private Log SITLog;
    private Socket socket;
    private BufferedReader BR;
    private BufferedWriter BW;
    private Encryption ServerEncyption;
    private String MessageIN;
    private String MessageOUT;

    private int WrongMessageCounter=0;
    private int MessageCheckCounter=0;
    private int MessageChecksMax;
    private int AttemtCounter=0;
    private boolean AttemtReSend=false;

    //timeout setings check server
    private int TimeoutAttemts;
    private int TimeoutAttemtsInterval;
    private int TimeoutWrongLimit;

    public void run (){
        SITLog=new Log("SIT");
        FechConfig();
        ServerEncyption = new Encryption();
        SITLog.WriteLog("SIT INIT SUCCESSFUL");
        ServerExchangeKeys();
        SITLog.WriteLog("HEY EXCHANGE SUCCESFUL");


        Exit();
    }

    public ServerInterfaceThread(Socket _socket,BufferedReader _BufRead,BufferedWriter _BufWrite ,ClientConnectionService _ccs){
        // takes the conection
        socket=_socket;
        BR=_BufRead;
        BW=_BufWrite;
        CCS=_ccs;
    }
    
    private void FechConfig(){//copies setings from server, this is done so all the server setings are in one spot
        TimeoutAttemts=Server.TimeoutAttemts;
        TimeoutAttemtsInterval=Server.TimeoutAttemtsInterval;
        TimeoutWrongLimit=Server.TimeoutWrongLimit;
        MessageChecksMax = TimeoutAttemtsInterval*10;// checks are done 10 every second
    }

    private void ServerExchangeKeys(){
        //public key exchange
        //C:HELLO
        //S:HELLO
        //C:<PUBLIC_KEY>
        //S:<PUBLIC_KEY>
        //C:DONE*encrypted
        //S:DONE*encrypted
        try{
            //waits for client to start the interaction
            while(WaitForMessage()){
                AttemtCounter++;
                if(AttemtCounter>TimeoutAttemts){
                    TerminateConnection();
                    System.out.println("&connection timmed out");
                    SITLog.WriteLog("TIMEOUT");
                }
            }
            //now it checks if the message that came in was "HELLO" to start the key exchange
            MessageIN=BR.readLine();
            while(!(MessageIN.equals("HELLO"))){
                WrongMessageCounter++;
                if(WrongMessageCounter>TimeoutWrongLimit){
                    TerminateConnection();
                    System.err.println("&connection timmed out");
                    SITLog.WriteLog("TIMEOUT");
                }
                while(WaitForMessage()){
                    AttemtCounter++;
                    
                    if(AttemtCounter>TimeoutAttemts){
                        System.err.println("&connection timmed out invalid communication");
                        TerminateConnection();
                        SITLog.WriteLog("TIMEOUT or INVALID");
                    }
                    AttemtCounter=0;
                }
                MessageIN=BR.readLine();
            }
            
            MessageOUT="HELLO";
            MessageSend(MessageOUT);
            while(WaitForMessage()){
                AttemtCounter++;
                if(AttemtCounter>TimeoutAttemts){
                    TerminateConnection();
                    System.out.println("&connection timmed out");
                    SITLog.WriteLog("TIMEOUT");
                }
            }
            AttemtCounter=0;

            MessageIN=BR.readLine();
            //saves clients public key
            ServerEncyption.InitEncryptCipher(MessageIN);
            //sends servers public key
            MessageOUT=Encryption.GetPublicKey();
            MessageSend(MessageOUT);
            while(WaitForMessage()){
                AttemtCounter++;
                if(AttemtCounter>TimeoutAttemts){
                    TerminateConnection();
                    System.out.println("&connection timmed out");
                    SITLog.WriteLog("TIMEOUT");
                }
            }
            AttemtCounter=0;

            MessageIN=ReadEncrypted();
            while(!(MessageIN.equals("DONE"))){
                WrongMessageCounter++;
                if(WrongMessageCounter>TimeoutWrongLimit){
                    TerminateConnection();
                    System.err.println("&connection timmed out");
                    SITLog.WriteLog("TIMEOUT");
                }
                while(WaitForMessage()){
                    AttemtCounter++;
                    MessageEncriptedSend(MessageOUT);
                    if(AttemtCounter>TimeoutAttemts){
                        System.err.println("&connection timmed out invalid communication");
                        TerminateConnection();
                        SITLog.WriteLog("TIMEOUT or INVALID");
                    }
                }
                AttemtCounter=0;
                MessageIN=ReadEncrypted();
            }
            System.err.println("~Transfer compleat");
            MessageOUT="DONE";
            MessageEncriptedSend(MessageOUT);
            //keys have been exchanged
        }catch(Exception e){
            System.err.println("&failed key exchange :"+e);
            SITLog.WriteLog("FAILED KEY EXCHANGE: "+e);
        }
    }

    private Boolean WaitForMessage(){
        try{
            //checks if a message has arived
            MessageCheckCounter=0;
            while(!BR.ready()){try{
                if(MessageCheckCounter>MessageChecksMax){
                    return true;
                }else{
                    MessageCheckCounter++;
                }
                sleep(100);

                }catch(Exception e){
                    System.err.println("&Thread can't sleep"+e);
                }
            }
        }catch(Exception e){
            System.err.println(e);
        }
        return false;
    }
    
    private void MessageSend(String Message){
        try{
            BW.write(Message);
            BW.newLine();
            BW.flush();
        }catch(Exception e){
            System.err.println("&Message failed to be sent "+e);
        }
    }
    private void MessageEncriptedSend(String Message){
        try{
            BW.write(ServerEncyption.Encript(Message));
            BW.newLine();
            BW.flush();
        }catch(Exception e){
            System.err.println("&Message failed to be sent "+e);
        }
    }
    private String ReadEncrypted(){
        try{
            return Encryption.DeCrypt(BR.readLine());
        }catch(Exception e){
            System.err.println("&Message failed to be sent "+e);
            return null;
        }
        
    }

    private void TerminateConnection(){
        try{
            BW.close();
            BR.close();
            socket.close();
            SITLog.WriteLog("TERMINATION OF CONNECTION SUCCESSFUL");
        }catch(IOException e){
            System.err.println("&failed to terminate connection"+e);
        }
        
    }
    private void Exit(){
        TerminateConnection();
        CCS.ThreadFinished();
        SITLog.WriteLog("EXIT SUCCESSFUL");
        SITLog.TerminateLog();
    }
    public void TerminateThread(){
        SITLog.WriteLog("COMMAND FROM ABOUT TO TERMINATE");
        Exit();
    }
}
