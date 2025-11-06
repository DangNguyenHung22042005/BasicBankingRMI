package Banker.Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountImp extends UnicastRemoteObject implements Account {
    private static final long serialVersionUID = 1L;
    private int accountNumber;
    private static NotificationServiceImp notificationService;

    public static void setNotificationService(NotificationServiceImp service) {
        notificationService = service;
    }

    public AccountImp(int num) throws RemoteException {
        super();
        this.accountNumber = num;
    }

    private void notify(String message) {
        if (notificationService != null) {
            notificationService.notifyClients(this.accountNumber, message);
        }
    }

    @Override
    public Boolean deposit(double amount) throws RemoteException {
        if (amount <= 0) return false;
        String sql = "UPDATE Account SET balance = balance + ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, this.accountNumber);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                notify("Tài khoản " + this.accountNumber + " nhận được + " + amount);
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi gửi tiền", e);
        }
    }

    @Override
    public Boolean withdraw(double amount) throws RemoteException {
        if (amount <= 0) return false;
        String sqlSelect = "SELECT balance FROM Account WHERE account_number = ?";
        String sqlUpdate = "UPDATE Account SET balance = balance - ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            double currentBalance = 0;
            try (PreparedStatement psSelect = conn.prepareStatement(sqlSelect)) {
                psSelect.setInt(1, this.accountNumber);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getDouble("balance");
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }
            if (currentBalance >= amount) {
                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                    psUpdate.setDouble(1, amount);
                    psUpdate.setInt(2, this.accountNumber);
                    psUpdate.executeUpdate();
                }
                conn.commit();
                notify("Tài khoản " + this.accountNumber + " đã rút - " + amount);
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi rút tiền", e);
        }
    }

    @Override
    public Boolean transfer(int destinationAccountNumber, double amount) throws RemoteException {
        if (amount <= 0 || destinationAccountNumber == this.accountNumber) {
            return false;
        }
        String sqlCheckSource = "SELECT balance FROM Account WHERE account_number = ?";
        String sqlCheckDest = "SELECT 1 FROM Account WHERE account_number = ?";
        String sqlUpdateSource = "UPDATE Account SET balance = balance - ? WHERE account_number = ?";
        String sqlUpdateDest = "UPDATE Account SET balance = balance + ? WHERE account_number = ?";
        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);
            double sourceBalance = 0;
            try (PreparedStatement ps = conn.prepareStatement(sqlCheckSource)) {
                ps.setInt(1, this.accountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        sourceBalance = rs.getDouble("balance");
                    } else {
                        conn.rollback(); return false;
                    }
                }
            }
            if (sourceBalance < amount) {
                conn.rollback(); return false;
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlCheckDest)) {
                ps.setInt(1, destinationAccountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback(); return false;
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateSource)) {
                ps.setDouble(1, amount);
                ps.setInt(2, this.accountNumber);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdateDest)) {
                ps.setDouble(1, amount);
                ps.setInt(2, destinationAccountNumber);
                ps.executeUpdate();
            }
            conn.commit();
            if (notificationService != null) {
                notificationService.notifyClients(this.accountNumber, "Chuyển khoản - " + amount + " tới TK " + destinationAccountNumber);
                notificationService.notifyClients(destinationAccountNumber, "Nhận được + " + amount + " từ TK " + this.accountNumber);
            }
            return true;
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi chuyển khoản", e);
        }
    }

    @Override
    public int getNumber() throws RemoteException {
        return this.accountNumber;
    }

    @Override
    public Customer getCustomer() throws RemoteException {
        String sql = "SELECT c.customer_id, c.name, c.address, c.city " +
                "FROM Customer c " +
                "JOIN Account a ON c.customer_id = a.customer_id " +
                "WHERE a.account_number = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString("customer_id"),
                            rs.getString("name"),
                            rs.getString("address"),
                            rs.getString("city")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi lấy khách hàng", e);
        }
        return null;
    }

    @Override
    public double getBalance() throws RemoteException {
        String sql = "SELECT balance FROM Account WHERE account_number = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi lấy số dư", e);
        }
        throw new RemoteException("Không tìm thấy tài khoản: " + this.accountNumber);
    }

    @Override
    public String toString() {
        return "AccountImp (DB-driven): " + this.accountNumber;
    }

    @Override
    public synchronized void login() throws RemoteException, LoginException {
        String sqlCheck = "SELECT is_locked FROM Account WHERE account_number = ?";
        String sqlLock = "UPDATE Account SET is_locked = 1 WHERE account_number = ? AND is_locked = 0";

        try (Connection conn = DatabaseConnector.getConnection()) {
            conn.setAutoCommit(false);

            boolean isLocked = false;
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setInt(1, this.accountNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        throw new LoginException("Tài khoản không tồn tại.");
                    }
                    isLocked = rs.getBoolean("is_locked");
                }
            }

            if (isLocked) {
                conn.rollback();
                throw new LoginException("Tài khoản đã được đăng nhập từ nơi khác.");
            }

            try (PreparedStatement ps = conn.prepareStatement(sqlLock)) {
                ps.setInt(1, this.accountNumber);
                int rowsAffected = ps.executeUpdate();

                if (rowsAffected == 0) {
                    conn.rollback();
                    throw new LoginException("Tài khoản đã được đăng nhập từ nơi khác (race condition).");
                }
            }

            conn.commit();
            System.out.println("[AccountImp " + this.accountNumber + "] Đã KHÓA (Logged in).");
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi login: " + e.getMessage(), e);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void logout() throws RemoteException {
        String sqlUnlock = "UPDATE Account SET is_locked = 0 WHERE account_number = ?";
        try (Connection conn = DatabaseConnector.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlUnlock)) {
            ps.setInt(1, this.accountNumber);
            ps.executeUpdate();
            System.out.println("[AccountImp " + this.accountNumber + "] Đã MỞ KHÓA (Logged out).");
        } catch (SQLException e) {
            throw new RemoteException("Lỗi CSDL khi logout: " + e.getMessage(), e);
        }
    }
}