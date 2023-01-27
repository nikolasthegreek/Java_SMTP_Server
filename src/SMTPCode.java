public class SMTPCode {
    //good codes
    public static String C211(String Status){
        return "211"+Status;
    }
    public static String C214(String Help){
        return "214Help message"+Help;
    }
    public static String C220(String Domain){
        return "220"+ Domain+"Service ready";
    }
    public static String C221(String Domain){
        return "221 "+Domain+" Service closing transmission channel";
    }
    public static String C250(){
        return "250OK";
    }
    public static String C251(String Path){
        return "551User not local; will forward to"+Path;
    }
    public static String C354(){
        return "354 Start mail input; end with <CRLF>.<CRLF>";
    }

    //error codes
    public static String EC421(){
        return "450Requested action not taken";
    }
    public static String EC450(String Domain){
        return "421"+ Domain+"Service not available,closing transmission channel";
    }
    public static String EC451(){
        return "451Requested action aborted: local error in processing";
    }
    public static String EC452(){
        return "452Requested action not taken: insufficient system storage";
    }
    public static String EC500(){
        return "500Syntax error, command unrecognized";
    }
    public static String EC501(){
        return "501Syntax error in parameters or arguments";
    }
    public static String EC502(){
        return "502Command not implemented";
    }
    public static String EC503(){
        return "503Bad sequence of commands";
    }
    public static String EC504(){
        return "504Command parameter not implemented";
    }
    public static String EC550(){
        return "550Requested action not taken: mailbox unavailable";
    }
    public static String EC551(String Path){
        return "551User not local; please try "+Path;
    }
    public static String EC552(){
        return "552Requested mail action aborted: exceeded storage allocation";
    }
    public static String EC553(){
        return "553Requested action not taken: mailbox name not allowed";
    }
    public static String EC554(){
        return "554Transaction failed";
    }
}