import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionThread extends Thread {
    public boolean KillMe= false; //flag for thread to be terminated

    private Socket socket;
    private BufferedReader BR;
    private BufferedWriter BW;
    private Encryption ServerEncyption;
    private String MessageIN;
    private String MessageOUT;

    private boolean ThreadActive=true;
    private int WrongMessageCounter=0;
    private int MessageCheckCounter=0;
    private int MessageChecksMax;
    private int AttemtCounter=0;
    private boolean AttemtReSend=false;

    private int KeyExchangeStage=0;
    private boolean KeysExchanged=false;

    //timeout setings check server
    private int TimeoutAttemts;
    private int TimeoutAttemtsInterval;
    private int TimeoutWrongLimit;

    public void run (){
        System.out.println("~Thread started");
        FechConfig();
        ServerEncyption = new Encryption();
        ServerExchangeKeys();
        
        
        Exit();
        System.out.println("~Thread stop");
    }

    public ConnectionThread(Socket _socket,BufferedReader _BufRead,BufferedWriter _BufWrite){
        // takes the conection
        socket=_socket;
        BR=_BufRead;
        BW=_BufWrite;
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
                    
                }
            }
            //now it checks if the message that came in was "HELLO" to start the key exchange
            MessageIN=BR.readLine();
            while(!(MessageIN.equals("HELLO"))){
                WrongMessageCounter++;
                if(WrongMessageCounter>TimeoutWrongLimit){
                    TerminateConnection();
                    System.err.println("&connection timmed out");
                }
                while(WaitForMessage()){
                    AttemtCounter++;
                    
                    if(AttemtCounter>TimeoutAttemts){
                        System.err.println("&connection timmed out invalid communication");
                        TerminateConnection();
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
                }
            }
            AttemtCounter=0;

            MessageIN=ReadEncrypted();
            while(!(MessageIN.equals("DONE"))){
                WrongMessageCounter++;
                if(WrongMessageCounter>TimeoutWrongLimit){
                    TerminateConnection();
                    System.err.println("&connection timmed out");
                }
                while(WaitForMessage()){
                    AttemtCounter++;
                    MessageEncriptedSend(MessageOUT);
                    if(AttemtCounter>TimeoutAttemts){
                        System.err.println("&connection timmed out invalid communication");
                        TerminateConnection();
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
        }catch(IOException e){
            System.err.println("&failed to terminate connection"+e);
        }
        
    }

    private void Exit(){
        TerminateConnection();
        Server.ThreadFinished();
    }
}
