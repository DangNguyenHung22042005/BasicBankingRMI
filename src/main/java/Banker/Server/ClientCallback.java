package Banker.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientCallback extends Remote {
    void receiveNotification(String message) throws RemoteException;
}