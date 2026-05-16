package CafeJava;

class MenuItem {
    private int id;
    private String name, category;
    private double price;
    private int stock;
    private String createdAt, updatedAt;

    public MenuItem() {}
    public MenuItem(String name, String category, double price, int stock) {
        this.name = name; this.category = category;
        this.price = price; this.stock = stock;
    }
    public MenuItem(int id, String name, String category, double price, int stock,
                    String createdAt, String updatedAt) {
        this.id = id; this.name = name; this.category = category;
        this.price = price; this.stock = stock;
        this.createdAt = createdAt; this.updatedAt = updatedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return name + " (" + category + ") - Rp " + (int) price;
    }
}

class Order {
    private String id;
    private double total, paymentAmount, changeAmount;
    private String paymentMethod, orderDate, createdAt;

    public Order() {}
    public Order(String id, double total, String paymentMethod,
                 double paymentAmount, double changeAmount,
                 String orderDate, String createdAt) {
        this.id = id; this.total = total;
        this.paymentMethod = paymentMethod;
        this.paymentAmount = paymentAmount;
        this.changeAmount = changeAmount;
        this.orderDate = orderDate; this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getTotal() { return total; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getPaymentAmount() { return paymentAmount; }
    public double getChangeAmount() { return changeAmount; }
    public String getOrderDate() { return orderDate; }
    public String getCreatedAt() { return createdAt; }
}

class OrderDetail {
    private int id, menuId, quantity;
    private String orderId, menuName;
    private double price, subtotal;

    public OrderDetail() {}
    public OrderDetail(String orderId, int menuId, String menuName,
                       double price, int quantity) {
        this.orderId = orderId; this.menuId = menuId;
        this.menuName = menuName; this.price = price;
        this.quantity = quantity;
        this.subtotal = price * quantity;
    }
    public OrderDetail(int id, String orderId, int menuId, String menuName,
                       double price, int quantity, double subtotal) {
        this(orderId, menuId, menuName, price, quantity);
        this.id = id; this.subtotal = subtotal;
    }

    public int getId() { return id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public int getMenuId() { return menuId; }
    public String getMenuName() { return menuName; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; this.subtotal = price * quantity; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
