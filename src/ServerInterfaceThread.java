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
    private Account User;
    private String Mail="";//temprary storage for incoming mail

    private int WrongMessageCounter=0;
    private int MessageCheckCounter=0;
    private int MessageChecksMax;
    private int AttemtCounter=0;

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
        SITLog.WriteLog("KEY EXCHANGE SUCCESFUL");
        User=LogIn();
        MessageOUT=SMTPCode.C220("SERVER");
        MessageEncriptedSend(MessageOUT);
        
        while(true){// main loop for reacting to SMTP commands
            try{
                while(WaitForMessage()){};
                MessageIN= ReadEncrypted();
                System.out.println(MessageIN);
                SMTPMessage SMTPMSG= new SMTPMessage(MessageIN);
                //if statement to determine reply
                if(SMTPMSG.Type.equals("HELO")){//NOOP reply
                    MessageOUT=SMTPCode.C250();
                    MessageEncriptedSend(MessageOUT);
                    SITLog.WriteLog("RECIEVED HELLO");
                }else if(SMTPMSG.Type.equals("QUIT")){
                    MessageOUT =SMTPCode.C221("Service closing transmission channel");
                    MessageEncriptedSend(MessageOUT);//sends out the reply to quit
                    SITLog.WriteLog("RECIEVED QUIT COMMAND");
                    break;
                }else if(SMTPMSG.Type.equals("NOOP")){//NOOP reply
                    MessageOUT =SMTPCode.C250();
                    MessageEncriptedSend(MessageOUT);
                }else if(SMTPMSG.Type.equals("HELP")){//Help reply
                    MessageOUT =SMTPCode.C214("Bla bla bla I am helping you know");
                    MessageEncriptedSend(MessageOUT);
                }else if(SMTPMSG.Type.equals("MAIL")){//MAIL INTERACTION
                    String Sender = SMTPMSG.ParseData();
                    System.out.println(Sender);//test---------------------------------------------------
                    if(Sender.equals(User.Email)){//checks if it is the user
                        MessageOUT =SMTPCode.C250();
                        MessageEncriptedSend(MessageOUT);

                        try {Thread.sleep(100);} catch (Exception e) {}//to avoid race horse condition
                        while(WaitForMessage()){};//waits for RCPT command
                        MessageIN= ReadEncrypted();
                        System.out.println(MessageIN);
                        SMTPMSG= new SMTPMessage(MessageIN);
                        if(SMTPMSG.Type.equals("RCPT")){

                            String Reciver =SMTPMSG.ParseData();
                            System.out.println(Reciver);//test--------------------------------------
                            
                            if(Accounts.FindUser(Reciver)!=null){//checks if user exists
                                System.out.println("passed");
                                MessageOUT =SMTPCode.C250();
                                MessageEncriptedSend(MessageOUT);

                                try {Thread.sleep(100);} catch (Exception e) {}//to avoid race horse condition
                                while(WaitForMessage()){};//waits for DATA command
                                MessageIN= ReadEncrypted();
                                System.out.println(MessageIN);
                                SMTPMSG= new SMTPMessage(MessageIN);
                                if(SMTPMSG.Type.equals("DATA")){
                                    MessageOUT =SMTPCode.C354();//start sending data code
                                    MessageEncriptedSend(MessageOUT);

                                    while(true){//checks if it is the . command
                                        try {Thread.sleep(1000);} catch (Exception e) {}//to avoid race horse condition
                                        WaitMail();
                                        System.out.println(MessageIN);
                                        if(MessageIN==null){continue;}
                                        if(MessageIN.equals("RSET")){//for case of RSET
                                            MessageOUT =SMTPCode.C250();
                                            MessageEncriptedSend(MessageOUT);
                                            break;//stops and does not save
                                        }else if(MessageIN.equals(".")){//reacts to end of transmition
                                            MessageOUT =SMTPCode.C250();
                                            MessageEncriptedSend(MessageOUT);
                                            //saves mail
                                            Server.Mailusers.add(Sender);
                                            Server.MailRCPT.add(Reciver);
                                            Server.Mail.add(Mail);
                                            Mail="";
                                            break;
                                        }
                                        Mail = Mail + '\n' + MessageIN;
                                    }




                                }else if(SMTPMSG.Type.equals("RSET")){//checks for RSET command
                                    MessageOUT =SMTPCode.C250();
                                    MessageEncriptedSend(MessageOUT);
                                }else if(SMTPMSG.IsValid()){//if it is a valid command but not what was expected
                                    MessageOUT =SMTPCode.EC503();//incorect squence command
                                    MessageEncriptedSend(MessageOUT);
                                }else{//command not valid
                                    MessageOUT =SMTPCode.EC500();//command not recognized code
                                    MessageEncriptedSend(MessageOUT);
                                }

                            }else{
                                MessageOUT =SMTPCode.EC551("IDK");//user is uknow(not in accounts)
                                MessageEncriptedSend(MessageOUT);
                            }

                        }else if(SMTPMSG.Type.equals("RSET")){//checks for RSET command
                            MessageOUT =SMTPCode.C250();
                            MessageEncriptedSend(MessageOUT);
                        }else if(SMTPMSG.IsValid()){//if it is a valid command but not what was expected
                            MessageOUT =SMTPCode.EC503();//incorect squence command
                            MessageEncriptedSend(MessageOUT);
                        }else{//command not valid
                            MessageOUT =SMTPCode.EC500();//command not recognized code
                            MessageEncriptedSend(MessageOUT);
                        }

                    }else{
                        MessageOUT =SMTPCode.EC451();//abort code
                        MessageEncriptedSend(MessageOUT);
                    }
                }else if(SMTPMSG.Type.equals("RSET")){//checks for RSET command
                    MessageOUT =SMTPCode.C250();
                MessageEncriptedSend(MessageOUT);
                }else if(SMTPMSG.IsValid()){//if it is a valid command but not what was expected
                    MessageOUT =SMTPCode.EC503();//incorect squence command
                    MessageEncriptedSend(MessageOUT);
                }else{//command not valid
                    MessageOUT =SMTPCode.EC500();//command not recognized code
                    MessageEncriptedSend(MessageOUT);
                }


            }catch(Exception e){
                System.err.println("&Error in main SIT loop");
                SITLog.WriteLog("EXEPTION I MAIN SIT LOOP");
                e.printStackTrace();
                break;
            }
        }



        Exit();
    }

    private Account LogIn(){
        Account Acc;
        while(true){//geting the email/username stage
            while(WaitForMessage()){}
            MessageIN= ReadEncrypted();
            if(MessageIN.equals("EXIT")){
                SITLog.WriteLog("FAILED LOGIN NO USERNAME");
                return null;
            }
            Acc=Accounts.FindUser(MessageIN);
            if(Acc!=null){
                break;
            }
            MessageOUT="WRONGEMAIL";
        MessageEncriptedSend(MessageOUT);
        }
        MessageOUT="GOODEMAIL";
        MessageEncriptedSend(MessageOUT);
        int count =3;
        while(count>0){//pasword login with attemts
            count--;
            while(WaitForMessage()){}
            MessageIN= ReadEncrypted();
            if(Acc.CheckPassword(MessageIN)){
                SITLog.WriteLog("SUCCESSFUL LOGIN: "+Acc.Email);
                MessageOUT="SUCCESSFUL LOGIN";
                MessageEncriptedSend(MessageOUT);
                return Acc;
            }
            MessageOUT="PASSWORDWRONG";
            MessageEncriptedSend(MessageOUT);
        }
        MessageOUT="PASSWORDFAIL";
        MessageEncriptedSend(MessageOUT);
        SITLog.WriteLog("FAILED LOGIN WRONG PASSWORD");
        return null;
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
                MessageSend(MessageOUT);
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
                    MessageSend(MessageOUT);
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
                MessageSend(MessageOUT);
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
    private void WaitMail(){
        while(true){
            while(WaitForMessage()){}
            try{
                MessageIN=BR.readLine();
            }catch(IOException e){
                System.err.println("mail fuckup");
            }
            
            if(MessageIN!=null){
                MessageIN= Encryption.DeCrypt(MessageIN);
            }
        }

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
