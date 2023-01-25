import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;
//====================================
//       CCT Thread
// The point of the CCT is to handle Timeout of the connection 
//in periods of prolonged inactivity, where it will every now and then
//send a message to make sure the client didnt disconect, it would be
//too confusing and complicated to have the SIT and CCT thread in
//the same script. So for a very slight expence of cpu reasorces
// it is worth it as it makes it easier to read/write the code
//=====================================

public class ClientCommunicationThread extends Thread{
    private Thread Parent;
    private Socket socket;
    private BufferedReader BR;
    private BufferedWriter BW;
    private Encryption ServerEncyption;
    private String MessageIN;
    private String MessageOUT;

    private int MessageCheckCounter=0;
    private int MessageChecksPerSec=4;//config here for long wait
    private int MessageChecksMax=10;//  config here for short wait

    public void run(){

    }

    public void CCTinit(Socket _socket,BufferedReader _BufRead,BufferedWriter _BufWrite,Thread mom){
        socket=_socket;
        BR=_BufRead;
        BW=_BufWrite;
        Parent = mom;
    }

    private Boolean WaitForMessageLong(){
        try{
            //checks if a message has arived
            MessageCheckCounter=0;
            while(!BR.ready()){try{
                if(MessageCheckCounter>MessageChecksPerSec*5){
                    return true;
                }else{
                    MessageCheckCounter++;
                }
                sleep(1000/MessageChecksPerSec);

                }catch(Exception e){
                    System.err.println("&Thread can't sleep"+e);
                }
            }
        }catch(Exception e){
            System.err.println(e);
        }
        return false;
    }
    private Boolean WaitForMessage(){
        try{
            //checks if a message has arived
            MessageCheckCounter=0;
            while(!BR.ready()){try{
                if(MessageCheckCounter>MessageChecksMax*5){
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
    

    public void MessageOUT(String Message){
        try{
            BW.write(Message);
            BW.newLine();
            BW.flush();
        }catch(Exception e){
            System.err.println("&Message failed to be sent "+e);
        }
    }
}
