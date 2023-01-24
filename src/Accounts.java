import java.io.File;

public class Accounts {
    private static Account[] accounts;//an array of all loged acounts
    private boolean Wait= false;

    public static void AccountsINIT(){
        InitFiles();
    }

    private static void InitFiles(){
        try{
            File Dir = new File(".\\Accounts");
            if(Dir.mkdir()){
                System.out.println("~Accounts folder created");
            }
        }catch(Exception  e){
            System.err.println("&failed to create directory beacuse : "+e);
        }
        try{
            File Acc = new File(".\\Accounts\\AccountsDB.txt");
            if(Acc.createNewFile()){
                System.out.println("~Accounts file created");
            }
        }catch(Exception  e){
            System.err.println("&failed to create file beacuse : "+e);
        }
    }

    private static void SaveData(){

    }
    
    public static int GenerateID(){//is used in the creation of an account
        int count= 0;
        for (Account account : accounts) {
            if(account==null){
                //accounts are kept and loaded in order of ID in an array
                //if thre is a null this means there is an open slot for an ID
                return count+1;  
            }
            count++;
        }// if no slot was found then it is apended in the end
        return count;
        
    }
    
    public static int FindUser (Email LookFor){
        for (Account account : accounts) {
            if(        account.User.Domain    ==LookFor.Domain){
                if(    account.User.MailServer==LookFor.MailServer){
                    if(account.User.UserName  ==LookFor.UserName){
                        return account.ID;
                    }
                }
            }
        }
        return -1;// -1 means it failed to find an account
    }

}
class Email{
    //  UserName@MailServer.Domain
    public String UserName;
    public String MailServer;
    public String Domain;
}
class Account{
    public Email User;
    public int ID;
    private String HashedPasword;//saves the pasword in hashed form for safty against leaks
    private String HexSalt;//the salt is saved to be able to perform the same hash to check 

    Account(Email _user,String Password){//saves user info hashed
        User = _user;
        HexSalt = Encryption.GenerateSaltHex();
        HashedPasword = Encryption.Hash(HexSalt,Password);
        ID = Accounts.GenerateID();
    }

    public boolean CheckPassword(String Password){
        if(Encryption.Hash(HexSalt,Password).equals(HashedPasword)){
            return true;
        }
        return false;
    }
}