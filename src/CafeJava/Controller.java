package CafeJava;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class Controller {

    private final Database db;

    public Controller() { this.db = Database.get(); }

    // Validasi
    public List<String> validateMenu(String name, String category, String priceStr, String stockStr) {
        List<String> errs = new ArrayList<>();
        if (name == null || name.trim().isEmpty()) errs.add("Nama menu wajib diisi!");
        if (!"makanan".equals(category) && !"minuman".equals(category))
            errs.add("Kategori harus 'makanan' atau 'minuman'!");

        double price = 0; int stock = 0;
        try { price = Double.parseDouble(priceStr); } catch (NumberFormatException e) { errs.add("Harga harus angka!"); }
        try { stock = Integer.parseInt(stockStr); } catch (NumberFormatException e) { errs.add("Stok harus angka!"); }

        if (price <= 0) errs.add("Harga harus > Rp 0!");
        if (stock < 0) errs.add("Stok tidak boleh negatif!");
        return errs;
    }

    // CRUD Menu
    public MenuItem addMenu(String name, String category, double price, int stock) {
        return db.menuInsert(new MenuItem(name.trim(), category, price, stock));
    }

    public boolean updateMenu(int id, String name, String category, double price, int stock) {
        MenuItem m = db.menuFindById(id);
        if (m == null) return false;
        m.setName(name.trim()); m.setCategory(category); m.setPrice(price); m.setStock(stock);
        return db.menuUpdate(m);
    }

    public boolean deleteMenu(int id) { return db.menuDelete(id); }

    public List<MenuItem> getAllMenu() { return db.menuFindAll(); }

    public List<MenuItem> searchMenu(String q) {
        return q.isEmpty() ? db.menuFindAll() : db.menuSearch(q);
    }

    // Keranjang
    public Order checkout(List<CartEntry> cart, String method, double payAmount) {
        double total = 0;
        for (CartEntry ce : cart) total += ce.item.getPrice() * ce.qty;

        String orderId = "ORD-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-" + LocalTime.now().format(DateTimeFormatter.ofPattern("HHmmss"))
            + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String today = LocalDate.now().toString();

        double actualPay = method.equals("cash") ? payAmount : total;
        double change = method.equals("cash") ? payAmount - total : 0;

        List<OrderDetail> details = new ArrayList<>();
        for (CartEntry ce : cart) {
            details.add(new OrderDetail(orderId, ce.item.getId(), ce.item.getName(),
                ce.item.getPrice(), ce.qty));
        }

        Order order = new Order(orderId, total, method, actualPay, change, today, null);
        return db.createOrder(order, details);
    }

    public static class CartEntry {
        public MenuItem item;
        public int qty;
        public CartEntry(MenuItem item, int qty) { this.item = item; this.qty = qty; }
    }

    public static String formatRupiah(double v) {
        return String.format("%,.0f", v).replace(',', '.');
    }

    // Laporan
    public List<Order> getOrdersByDate(String date) { return db.orderFindByDate(date); }
    public List<OrderDetail> getOrderDetails(String orderId) { return db.detailFindByOrderId(orderId); }
    public List<String> getOrderDates() { return db.getOrderDates(); }
    public int countOrders(String date) { return db.countOrdersByDate(date); }
    public double sumRevenue(String date) { return db.sumTotalByDate(date); }
}
