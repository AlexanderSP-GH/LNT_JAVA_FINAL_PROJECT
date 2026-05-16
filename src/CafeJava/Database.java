package CafeJava;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Database {

    private static Database instance;
    private Connection conn;
    private static final String DB_HOST = "localhost";
    private static final String DB_PORT = "3306";
    private static final String DB_NAME = "cafe_java";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    private Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String url = "jdbc:mysql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME
                       + "?useSSL=false&serverTimezone=Asia/Jakarta&allowPublicKeyRetrieval=true";
            conn = DriverManager.getConnection(url, DB_USER, DB_PASS);
            conn.setAutoCommit(true);
            initTables();
            System.out.println("[DB] Koneksi MySQL berhasil: " + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
        } catch (Exception e) {
            System.err.println("[DB] Gagal koneksi ke MySQL!");
            System.err.println("[DB] Pastikan: (1) MySQL sudah running, (2) database '" + DB_NAME + "' sudah dibuat");
            e.printStackTrace();
        }
    }

    public static Database get() {
        if (instance == null) instance = new Database();
        return instance;
    }

    public Connection conn() { return conn; }

    public void close() {
        try { if (conn != null && !conn.isClosed()) conn.close(); } catch (SQLException e) {}
    }

    // Init Dummy Data
    private void initTables() {
        String sql = """
            CREATE TABLE IF NOT EXISTS menu (
                id          INT AUTO_INCREMENT PRIMARY KEY,
                name        VARCHAR(100)    NOT NULL,
                category    ENUM('makanan', 'minuman') NOT NULL,
                price       DECIMAL(10,2)   NOT NULL,
                stock       INT             NOT NULL DEFAULT 0,
                created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
                updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS orders (
                id              VARCHAR(30)     PRIMARY KEY,
                total           DECIMAL(10,2)   NOT NULL,
                payment_method  ENUM('cash', 'qris', 'debit') NOT NULL,
                payment_amount  DECIMAL(10,2)   NOT NULL,
                change_amount   DECIMAL(10,2)   NOT NULL DEFAULT 0,
                order_date      DATE            NOT NULL,
                created_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
            );
            CREATE TABLE IF NOT EXISTS order_details (
                id          INT AUTO_INCREMENT PRIMARY KEY,
                order_id    VARCHAR(30)     NOT NULL,
                menu_id     INT             DEFAULT NULL,
                menu_name   VARCHAR(100)    NOT NULL,
                price       DECIMAL(10,2)   NOT NULL,
                quantity    INT             NOT NULL,
                subtotal    DECIMAL(10,2)   NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (menu_id) REFERENCES menu(id) ON DELETE SET NULL
            );
            """;
        String seed = """
            INSERT IGNORE INTO menu (id, name, category, price, stock) VALUES
                (1,'Nasi Goreng Spesial','makanan',25000,50),
                (2,'Mie Goreng Jawa','makanan',20000,40),
                (3,'Ayam Penyet','makanan',28000,30),
                (4,'Sate Ayam (10 tusuk)','makanan',30000,25),
                (5,'Kentang Goreng','makanan',15000,60),
                (6,'Es Kopi Susu','minuman',18000,100),
                (7,'Es Teh Manis','minuman',8000,150),
                (8,'Jus Alpukat','minuman',20000,40),
                (9,'Matcha Latte','minuman',22000,35),
                (10,'Air Mineral','minuman',5000,200);
            """;
        try (Statement stmt = conn.createStatement()) {
            for (String s : sql.split(";")) { String t = s.trim(); if (!t.isEmpty()) stmt.execute(t); }
            stmt.executeUpdate(seed);
            System.out.println("[DB] Tabel & seed siap.");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // Menu
    public List<MenuItem> menuFindAll() {
        List<MenuItem> list = new ArrayList<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM menu ORDER BY category, name")) {
            while (rs.next()) list.add(mapMenuItem(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public MenuItem menuFindById(int id) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM menu WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapMenuItem(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<MenuItem> menuSearch(String q) {
        List<MenuItem> list = new ArrayList<>();
        String sql = "SELECT * FROM menu WHERE LOWER(name) LIKE ? OR LOWER(category) LIKE ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + q.toLowerCase() + "%";
            ps.setString(1, like); ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapMenuItem(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public MenuItem menuInsert(MenuItem item) {
        String sql = "INSERT INTO menu (name, category, price, stock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, item.getName()); ps.setString(2, item.getCategory());
            ps.setDouble(3, item.getPrice()); ps.setInt(4, item.getStock());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) item.setId(rs.getInt(1)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return item;
    }

    public boolean menuUpdate(MenuItem item) {
        String sql = "UPDATE menu SET name=?, category=?, price=?, stock=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.getName()); ps.setString(2, item.getCategory());
            ps.setDouble(3, item.getPrice()); ps.setInt(4, item.getStock());
            ps.setInt(5, item.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean menuDelete(int id) {
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE order_details SET menu_name = CONCAT(menu_name, ' (dihapus)') WHERE menu_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE order_details SET menu_id = NULL WHERE menu_id = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM menu WHERE id = ?")) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                conn.commit();
                return rows > 0;
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    // Order
    public Order createOrder(Order order, List<OrderDetail> details) {
        String sqlO = "INSERT INTO orders (id, total, payment_method, payment_amount, change_amount, order_date) VALUES (?,?,?,?,?,?)";
        String sqlD = "INSERT INTO order_details (order_id, menu_id, menu_name, price, quantity, subtotal) VALUES (?,?,?,?,?,?)";
        String sqlS = "UPDATE menu SET stock = stock - ? WHERE id = ?";
        try {
            conn.setAutoCommit(false);
            try (PreparedStatement psO = conn.prepareStatement(sqlO)) {
                psO.setString(1, order.getId()); psO.setDouble(2, order.getTotal());
                psO.setString(3, order.getPaymentMethod()); psO.setDouble(4, order.getPaymentAmount());
                psO.setDouble(5, order.getChangeAmount()); psO.setString(6, order.getOrderDate());
                psO.executeUpdate();
            }
            try (PreparedStatement psD = conn.prepareStatement(sqlD);
                 PreparedStatement psS = conn.prepareStatement(sqlS)) {
                for (OrderDetail d : details) {
                    psD.setString(1, d.getOrderId()); psD.setInt(2, d.getMenuId());
                    psD.setString(3, d.getMenuName()); psD.setDouble(4, d.getPrice());
                    psD.setInt(5, d.getQuantity()); psD.setDouble(6, d.getSubtotal());
                    psD.executeUpdate();
                    psS.setInt(1, d.getQuantity()); psS.setInt(2, d.getMenuId());
                    psS.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
        return order;
    }

    public List<Order> orderFindByDate(String date) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE order_date = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrder(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<OrderDetail> detailFindByOrderId(String orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM order_details WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapDetail(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<String> getOrderDates() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT order_date FROM orders ORDER BY order_date DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(rs.getString("order_date"));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int countOrdersByDate(String date) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM orders WHERE order_date = ?")) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public double sumTotalByDate(String date) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COALESCE(SUM(total),0) FROM orders WHERE order_date = ?")) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getDouble(1); }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    private MenuItem mapMenuItem(ResultSet rs) throws SQLException {
        return new MenuItem(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
            rs.getDouble("price"), rs.getInt("stock"),
            rs.getString("created_at"), rs.getString("updated_at"));
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        return new Order(rs.getString("id"), rs.getDouble("total"),
            rs.getString("payment_method"), rs.getDouble("payment_amount"),
            rs.getDouble("change_amount"), rs.getString("order_date"), rs.getString("created_at"));
    }

    private OrderDetail mapDetail(ResultSet rs) throws SQLException {
        return new OrderDetail(rs.getInt("id"), rs.getString("order_id"), rs.getInt("menu_id"),
            rs.getString("menu_name"), rs.getDouble("price"), rs.getInt("quantity"), rs.getDouble("subtotal"));
    }
}
