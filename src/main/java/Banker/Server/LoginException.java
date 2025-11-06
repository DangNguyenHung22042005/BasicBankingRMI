package Banker.Server;

import java.rmi.RemoteException;

public class LoginException extends RemoteException {
    public LoginException(String message) {
        super(message);
    }
}
