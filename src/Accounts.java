import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.Scanner;

public class Accounts {
    private static LinkedList<Account> _accounts;//a list of all loged acounts
    private static File DB;

    public static void AccountsINIT(){
        _accounts= new LinkedList<Account>();
        InitFiles();
        DB = new File(".\\Accounts\\AccountsDB.txt");
        LoadAccounts();
    }
    
    private static void InitFiles(){
        try{
            File Dir = new File(".\\Accounts");
            if(Dir.mkdir()){
                System.out.println("~Accounts folder created");
            }
        }catch(Exception  e){
            System.err.println("&failed to create Accounts directory beacuse : "+e);
        }
        try{
            File Acc = new File(".\\Accounts\\AccountsDB.txt");
            if(Acc.createNewFile()){
                System.out.println("~Accounts file created");
            }
        }catch(Exception e){
            System.err.println("&failed to create file Accounts beacuse : "+e);
        }
    }

    //Accounts are saved across many lines in the txt file
    //Email
    //Hashedpasword
    //HexSalt

    private static void LoadAccounts(){
        Account Data;
        try{
            Data = new Account();
            Scanner Reader = new Scanner(DB);
            while (Reader.hasNextLine()) {
                Data =new Account();
                Data.Email=Reader.nextLine();//            loads Email
                Data.HashedPasword=Reader.nextLine();//    loads hashed password
                Data.HexSalt=Reader.nextLine();//          loads HexSalt
                _accounts.add(Data);//                     Saves Data
            }
            Reader.close();
        }catch(Exception e){
            System.err.println("&failed to load data: "+e);
        }
        

    }

    public static void SaveData(){
        String Data = new String();
        Account Acc;
        try{
            FileWriter Writer = new FileWriter(".\\Accounts\\AccountsDB.txt");
            for (int i = 0; i < _accounts.size(); i++) {
                Acc=_accounts.get(i);
                Data= Data+Acc.Email+"\n";//        loads email
                Data= Data+Acc.HashedPasword+"\n";//loads hashed password
                Data= Data+Acc.HexSalt+"\n";//      loads salt
            }
            Writer.write(Data);
            Writer.close();
        }catch(Exception e){
            System.err.println("&failed to save accounts: "+e);
        }
    }
    
    public static Account FindUser (String _email){
        for (int i = 0; i < _accounts.size(); i++) {
            if(_accounts.get(i).Email.equals(_email)){
                return _accounts.get(i);
            }
        }
        return null;// null means it failed to find an account
    }
    public static boolean FindUserBool (String _email){
        for (int i = 0; i < _accounts.size(); i++) {
            if(_accounts.get(i).Email.equals(_email)){
                System.out.println("found user");
                return true;
            }
        }
        return false;
    }

    public static boolean CreateAccount(String email, String Password){
        if(FindUser(email)!=null){return false;}//this returns false to denote the account exists
        _accounts.add(new Account(email,Password));//loads new account(will get saved in the DB later unless interupted)
        return true;//succesfully saved(still voletile)
    }

    public static void WipeData(){
        for (int i = 0; i < _accounts.size(); i++) {
            _accounts.remove();
        }
    }
}
class Account{
    public String Email;
    public String HashedPasword;//saves the pasword in hashed form for safty against leaks
    public String HexSalt;//the salt is saved to be able to perform the same hash to check 

    Account(){}// default constructor for when loading from DB

    Account(String email,String Password){//saves user info hashed
        Email = email;
        HexSalt = Encryption.GenerateSaltHex();
        HashedPasword = Encryption.Hash(HexSalt,Password);
    }

    public boolean CheckPassword(String Password){
        if(Encryption.Hash(HexSalt,Password).equals(HashedPasword)){
            return true;
        }
        return false;
    }
}
