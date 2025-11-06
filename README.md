Đây là từng bước để ông chạy được dự án nhé, làm theo là được thôi!

1. Vào intelij -> new project -> đặt tên dự án là BankingRMI, chọn Build System là Maven, Tại phần Advanced setting, đặt groupID là com.banker, còn cái ArtifactID là banking-rmi-project
2. Xong có dự án rồi thì vô folder src/main/java tại đây tạo ra folder mới là Banker, trong ni tạo thêm 2 folder con lần lượt là Server và Client
3. Bắt đầu lấy hết code của tui trong 2 folder tương úng về bỏ vô đúng 2 folder vừa tạo là xong bước lấy code java, sau đó copy code của file pom thay lại vào file pom luôn là được
4. Vào trong file DatabaseConnector ở Folder Server rồi sửa lại đường dẫn SQLServer đến máy ông là được, giữ nguyên cái tên database là BankDB để tạo sau đây nè;
5. Đến đây vào SQLServer rồi tạo database, tạo bảng này kia, thêm thông tin lần lượt theo code sau:
CREATE DATABASE BankDB;
USE BankDB;

CREATE TABLE Customer (
    customer_id VARCHAR(20) PRIMARY KEY,
    name NVARCHAR(100),
    address NVARCHAR(200),
    city NVARCHAR(50)
);

CREATE TABLE Account (
    account_number INT PRIMARY KEY,
    customer_id VARCHAR(20),
    balance DECIMAL(15,2) DEFAULT 0,
    is_locked BIT DEFAULT 0,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

INSERT INTO Customer VALUES ('C001', 'Nguyen Van A', '123 Tran Phu', 'Da Nang');
INSERT INTO Customer VALUES ('C002', 'Tran Thi B', '456 Le Loi', 'Hanoi');
INSERT INTO Customer VALUES ('C003', 'Le Van Quan', '12 Dong Ho', 'Da Nang');
INSERT INTO Customer VALUES ('C004', 'Dang Nguyen Hung', '10 Ha Tay', 'Ho Chi Minh');
INSERT INTO Customer VALUES ('C005', 'Tran Viet Truong', '19 Khai Tay', 'Dak Lak');
INSERT INTO Customer VALUES ('C006', 'Tran Duc Duong', '23 Khoi Nghia', 'Tay Nguyen');
INSERT INTO Customer VALUES ('C007', 'Hoang Cong Dung', '99 Binh Nguyen', 'Quang Nam');
INSERT INTO Customer VALUES ('C008', 'Chu Ngoc Hoang', '87 Quang Dong', 'Quang Binh');
INSERT INTO Customer VALUES ('C009', 'Le Nhat Huy', '22 Linh Chi', 'Hue');

INSERT INTO Account VALUES (1001, 'C001', 5000000, 0);
INSERT INTO Account VALUES (1002, 'C002', 3000000, 0);
INSERT INTO Account VALUES (1003, 'C001', 8000000, 0);
INSERT INTO Account VALUES (1111, 'C003', 2000000, 0);
INSERT INTO Account VALUES (2222, 'C004', 9000000, 0);
INSERT INTO Account VALUES (3333, 'C005', 8000000, 0);
INSERT INTO Account VALUES (4444, 'C006', 5000000, 0);
INSERT INTO Account VALUES (5555, 'C007', 3000000, 0);
INSERT INTO Account VALUES (6666, 'C008', 1000000, 0);
INSERT INTO Account VALUES (7777, 'C009', 3000000, 0);

6. Đến đây là xong rồi, chỉ việc chạy lên như này:
   + Mở file code BankServer tại folder Server, file này có sắn hàm main rồi, chỉ việc chạy lên thôi
   + Mở file code duy nhất ở Folder Client là BankClientGUI, nó cũng có sẵn hàm main rồi, chạy lên thôi, chạy cái này lên chỉ cần nhập 1 trong mấy cái số tài khoản bảng bảng Account như 1001, 1111, 2222, này kia vào là được!
  
   + Rồi hết rồi đó, code thì tui cũng mới clone chỉnh lại giao diện với fix lại kết nối này kia cho dùng được với SQLServer chứ còn cái RMI này thì chưa coi kỹ, nhưng mà nó cũng dễ lắm, về mặt cơ bản là nó cần những cái chính như ri nè:

Bước 1: Tạo Remote Interface
public interface TinhTongRemote extends Remote {
    int tinhTong(int a, int b) throws RemoteException;
}

Bước 2: Tạo Implementation (Server)
public class TinhTongImpl extends UnicastRemoteObject implements TinhTongRemote {
    public TinhTongImpl() throws RemoteException {
        super();
    }

    @Override
    public int tinhTong(int a, int b) throws RemoteException {
        System.out.println("Server nhận: " + a + " + " + b);
        return a + b;
    }
}

Bước 3: Server khởi động registry + bind object
public class Server {
    public static void main(String[] args) {
        try {
            // Tạo registry trên cổng 1099 (mặc định)
            Registry registry = LocateRegistry.createRegistry(1099);
            
            TinhTongImpl obj = new TinhTongImpl();
            registry.rebind("TinhTongService", obj); // đăng ký tên
            
            System.out.println("Server RMI sẵn sàng...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

Bước 4: Client gọi từ xa
public class Client {
    public static void main(String[] args) {
        try {
            // Kết nối tới registry của server
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            
            // Lookup tên dịch vụ
            TinhTongRemote stub = (TinhTongRemote) registry.lookup("TinhTongService");
            
            // Gọi như hàm local
            int ketQua = stub.tinhTong(69, 420);
            System.out.println("Kết quả từ server: " + ketQua);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

-> Đó nghĩa là RMI là cho phép ae gọi phương thức từ trực tiếp từ đối tượng mà đối tượng đó cái máy của client nó không cần khởi tạo ra, mà nó đăng ký nhận đối tượng đó từ server, rồi nó gọi ra phương thức của đối tượng nớ xong nó dùng thôi
-> Ví dụ trên là nó có đối tượng Tính tổng kìa, có phương thức là tính tổng, ở server nó đăng ký 1 cái registry với cái port cố định, xong nó khởi tại cái đối tượng tính tổng lên, rồi nó bind, nghĩa là nó đăng ký là đối tượng tính tổng là thằng đối tượng vừa tạo.
-> Xong ở bên client máy khác, hén đăng ký kết nối registry tới port luôn, rồi ở đây hén gọi để tìm cái đối tượng tính tổng ở server, lúc tìm dược rồi thì hén chỉ việc gọi ra hàm tính tổng của thằng ni để tính thôi, dễ òm rứa đó, cái đồ án trên cũng tương tự rứa.

Rứa là xong cái LAB RMI thôi, hy vọng là ông cài oke, không bị gì, mà bị thì hú tui là được!
