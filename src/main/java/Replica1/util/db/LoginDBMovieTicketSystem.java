package Replica1.util.db;

import util.login.UserInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoginDBMovieTicketSystem {

    //db of user with username, userinfo type including username, password and type(A: Admin, C: Customer)
    public static Map<String, UserInfo> userRecords = new ConcurrentHashMap<>();

    public void addUser(UserInfo userInfo)
    {
        userRecords.put(userInfo.getUsername(),userInfo);
    }

    public boolean verifyUser(String username, String password)
    {
        System.out.println(userRecords.get(username));
        UserInfo userInfo = userRecords.get(username);
        if(userInfo!=null && userInfo.getPassword().equals(password))
            return true;
        return false;
    }

    public boolean verifyUserID(String username)
    {
        System.out.println(userRecords.get(username));
        UserInfo userInfo = userRecords.get(username);
        if(userInfo!=null)
            return true;
        return false;
    }

    public void getUsers()
    {
       for(Map.Entry<String, UserInfo> m:  userRecords.entrySet())
       {
           System.out.println(m.getKey() + ": " + m.getValue());
       }
    }

}

