package Banker.Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationServiceImp extends UnicastRemoteObject implements NotificationService {
    private Map<Integer, List<ClientCallback>> subscriptions;

    public NotificationServiceImp() throws RemoteException {
        super();
        subscriptions = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void registerCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException {
        subscriptions.putIfAbsent(accountNumber, new CopyOnWriteArrayList<>());

        List<ClientCallback> clients = subscriptions.get(accountNumber);
        if (!clients.contains(clientCallback)) {
            clients.add(clientCallback);
            System.out.println("[NotificationService] Client mới đăng ký theo dõi tài khoản: " + accountNumber);
        }
    }

    @Override
    public synchronized void unregisterCallback(int accountNumber, ClientCallback clientCallback) throws RemoteException {
        List<ClientCallback> clients = subscriptions.get(accountNumber);
        if (clients != null) {
            clients.remove(clientCallback);
            System.out.println("[NotificationService] Client đã hủy theo dõi tài khoản: " + accountNumber);
        }
    }

    public void notifyClients(int accountNumber, String message) {
        List<ClientCallback> clients = subscriptions.get(accountNumber);

        if (clients != null && !clients.isEmpty()) {
            System.out.println("[NotificationService] Gửi thông báo cho " + clients.size() + " client của tài khoản " + accountNumber);

            for (ClientCallback client : clients) {
                try {
                    client.receiveNotification(message);
                } catch (RemoteException e) {
                    System.err.println("Lỗi khi gửi callback, xóa client: " + e.getMessage());
                    clients.remove(client);
                }
            }
        }
    }
}
