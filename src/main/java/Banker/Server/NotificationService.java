package Banker.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotificationService extends Remote {
    void registerCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException;
    void unregisterCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException;
}