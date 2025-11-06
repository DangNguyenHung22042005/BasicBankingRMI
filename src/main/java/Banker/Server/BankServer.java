package Banker.Server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankServer {
    public static void main(String[] args) {
        System.out.println("Đang khởi động BankServer (kết nối CSDL)...");

        List<Integer> accountNumbers;
        try {
            accountNumbers = getAccountNumbersFromDB();
        } catch (SQLException e) {
            System.err.println("Không thể lấy danh sách tài khoản từ CSDL. Server tắt.");
            e.printStackTrace();
            return;
        }

        Registry registry;
        try {
            registry = LocateRegistry.createRegistry(2204);
            System.out.println("Đã tạo RMI Registry trên cổng 2204.");
        } catch (RemoteException e1) {
            System.out.println("Không thể tạo RMI Registry: " + e1.getMessage());
            return;
        }

        try {
            NotificationServiceImp notificationService = new NotificationServiceImp();

            registry.rebind("notification", notificationService);
            System.out.println("Đã đăng ký 'notification' (Notification Service).");

            AccountImp.setNotificationService(notificationService);
        } catch (Exception ex) {
            System.out.println("Lỗi khi đăng ký 'notification': " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        try {
            AccountListImp list = new AccountListImp();
            registry.rebind("accounts", list);
            System.out.println("Đã đăng ký 'accounts' (danh sách tài khoản).");
        } catch (Exception ex) {
            System.out.println("Lỗi khi đăng ký 'accounts': " + ex.getMessage());
            ex.printStackTrace();
            return;
        }

        int boundCount = 0;
        for (int accNum : accountNumbers) {
            try {
                AccountImp account = new AccountImp(accNum);
                registry.rebind("act" + accNum, account);
                boundCount++;
            } catch (Exception e) {
                System.err.println("Lỗi khi tạo hoặc đăng ký tài khoản " + accNum);
                e.printStackTrace();
            }
        }

        System.out.println("==================================================");
        System.out.println("ĐÃ ĐĂNG KÝ THÀNH CÔNG " + boundCount + " TÀI KHOẢN.");
        System.out.println("BankServer đã sẵn sàng!");
        System.out.println("==================================================");
    }

    private static List<Integer> getAccountNumbersFromDB() throws SQLException {
        List<Integer> numbers = new ArrayList<>();
        String sql = "SELECT account_number FROM Account";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                numbers.add(rs.getInt("account_number"));
            }
        }
        return numbers;
    }
}