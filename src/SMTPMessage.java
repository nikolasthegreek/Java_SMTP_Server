
public class SMTPMessage {
    private static String Commands[]={"HELO","MAIL","RCPT","DATA","VRFY","EXPN","HELP","NOOP","QUIT"};
    public String Message;
    public String Type;
    
    SMTPMessage(String _message){
        Message = _message;
        FindType();
    }
    private void FindType(){
        Type = new String(new char[]{Message.charAt(0),Message.charAt(1),Message.charAt(2),Message.charAt(3)});
        char[] _message = new char[Message.length()-3];
        for (int i = 3; i < Message.length(); i++) {
            _message[i-3]=Message.charAt(i);
        }
        Message=new String(_message);
    }
    public boolean IsValid(){
        for (String _cmd : Commands) {
            if(Type.equals(_cmd)){
                return true;
            }
        }
        return false;
    }
    public String ParseData(){//used to take the form and to path
        int i;
        boolean DataFound=false;
        for (i = 0; i < Message.length(); i++) {
            if(Message.charAt(i)==':'){
                DataFound=true;
                break;
            }
        }
        if(!DataFound){
            return null;
        }
        char[] Chars= new char[Message.length()-i-1];
        int ind=0;//index for Chars
        for (int j = i+1; j < Message.length(); j++) {//start loop after :
            Chars[ind]=Message.charAt(j);
            ind++;
        }
        return new String(Chars);
    }
}
