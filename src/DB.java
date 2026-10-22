import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DB {
    private static DB instance;
    private static final String URL = "jdbc:mysql://localhost:3306/fashion_store";
    private static final String USER = "root";
    private static final String PASSWORD = "admin123";

    // Singleton Pattern: private constructor prevents external instantiation
    private DB() {
    }

    // Singleton Pattern: single globally accessible instance
    public static DB getInstance() {
        if (instance == null) {
            instance = new DB();
        }
        return instance;
    }

    public static Connection connect() throws SQLException {
        Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
        try (Statement s = con.createStatement()) {
            s.executeUpdate("ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_status VARCHAR(50) DEFAULT 'PAID'");
        } catch (Exception ignored) {
        }
        return con;
    }

    // login chyyan

    public static User login(String username, String password) throws SQLException {
        Connection con = connect();

        Statement stmt = con.createStatement();

        String sql = "SELECT id, username, full_name, role FROM users WHERE username='" + username + "' AND password='"
                + password + "'";

        ResultSet rs = stmt.executeQuery(sql);

        if (rs.next()) {
            int id = rs.getInt("id");
            String uname = rs.getString("username");
            String name = rs.getString("full_name");
            String role = rs.getString("role");

            rs.close();
            stmt.close();
            con.close();

            if (role.equals("ADMIN")) {
                return new Admin(id, uname, name);
            } else {
                return new Customer(id, uname, name);
            }
        }

        rs.close();
        stmt.close();
        con.close();
        return null;
    }

    // register chyyan

    public static void register(String fullname, String username, String password) throws SQLException {

        Connection con = connect();
        Statement stmt = con.createStatement();

        String sql = "INSERT INTO users (username, password, full_name, role) VALUES ('" +
                username.trim() + "', '" + password + "', '" + fullname.trim() + "', 'CUSTOMER')";

        stmt.executeUpdate(sql);
        stmt.close();
        con.close();
    }

    // product filter chyyan

    public static List<Product> getProducts() throws SQLException {
        return getProducts("ALL", "ALL", "ALL");
    }

    public static List<Product> getProducts(String category, String gender, String size) throws SQLException {
        List<Product> list = new ArrayList<>();
        Connection con = connect();
        Statement stmt = con.createStatement();

        StringBuilder sql = new StringBuilder("SELECT * FROM products");
        List<String> conditions = new ArrayList<>();

        if (category != null && !category.equals("ALL"))
            conditions.add("category='" + category.toUpperCase() + "'");
        if (gender != null && !gender.equals("ALL"))
            conditions.add("gender='" + gender.toUpperCase() + "'");
        if (size != null && !size.equals("ALL"))
            conditions.add("size='" + size.toUpperCase() + "'");

        if (!conditions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < conditions.size(); i++) {
                if (i > 0)
                    sql.append(" AND ");
                sql.append(conditions.get(i));
            }
        }
        sql.append(" ORDER BY id DESC");

        ResultSet rs = stmt.executeQuery(sql.toString());
        while (rs.next()) {
            list.add(new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("gender"),
                    rs.getString("size"),
                    rs.getDouble("price"),
                    rs.getInt("stock")));
        }

        rs.close();
        stmt.close();
        con.close();
        return list;
    }

    // product save chyyan

    public static void saveProduct(int id, String name, String category, String gender, String size, double price,
            int stock) throws SQLException {

        Connection con = connect();
        Statement stmt = con.createStatement();

        String sql;
        if (id == 0) {
            sql = "INSERT INTO products (name, category, gender, size, price, stock) VALUES ('" +
                    name.trim() + "', '" + category.trim().toUpperCase() + "', '" + gender.trim().toUpperCase() +
                    "', '" + size.trim().toUpperCase() + "', " + price + ", " + stock + ")";
        } else {
            sql = "UPDATE products SET name='" + name.trim() + "', category='" + category.trim().toUpperCase() +
                    "', gender='" + gender.trim().toUpperCase() + "', size='" + size.trim().toUpperCase() +
                    "', price=" + price + ", stock=" + stock + " WHERE id=" + id;
        }

        stmt.executeUpdate(sql);
        stmt.close();
        con.close();
    }

    // product delete chyyan

    public static void deleteProduct(int id) throws SQLException {
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.executeUpdate("DELETE FROM products WHERE id=" + id);
        stmt.close();
        con.close();
    }

    // product order list chyyan

    public static List<OrderRecord> getOrders(Integer uid) throws SQLException {
        List<OrderRecord> list = new ArrayList<>();

        Connection con = connect();
        Statement stmt = con.createStatement();

        String sql = "SELECT o.id, u.username, o.total, o.status, o.payment_method, " +
                "COALESCE(o.payment_status, 'PAID') AS payment_status, " +
                "DATE_FORMAT(o.created_at, '%Y-%m-%d %H:%i') AS created_at " +
                "FROM orders o JOIN users u ON o.user_id = u.id ";

        if (uid != null) {
            sql += "WHERE o.user_id=" + uid + " ";
        }
        sql += "ORDER BY o.id DESC";

        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            list.add(new OrderRecord(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getDouble("total"),
                    rs.getString("status"),
                    rs.getString("payment_method"),
                    rs.getString("payment_status"),
                    rs.getString("created_at")));
        }

        rs.close();
        stmt.close();
        con.close();
        return list;
    }

    // order place chyyan

    public static int placeOrder(int uid, List<CartItem> cart, Payment payment)
            throws SQLException, InsufficientStockException {
        String method = payment.getMethodName();
        String payStatus = "PAID";
        if (payment instanceof BNPLPayment) {
            payStatus = ((BNPLPayment) payment).getInitialPaymentStatus();
        } else if (method.toUpperCase().contains("CASH")) {
            payStatus = "PENDING (COD)";
        }
        return placeOrder(uid, cart, method, payStatus);
    }

    public static int placeOrder(int uid, List<CartItem> cart, String paymethod)
            throws SQLException, InsufficientStockException {
        String payStatus = (paymethod != null && paymethod.toUpperCase().contains("CASH")) ? "PENDING (COD)" : "PAID";
        return placeOrder(uid, cart, paymethod, payStatus);
    }

    public static int placeOrder(int uid, List<CartItem> cart, String paymethod, String payStatus)
            throws SQLException, InsufficientStockException {

        Connection con = connect();
        con.setAutoCommit(false);

        try {
            double total = 0;
            Statement stmt = con.createStatement();

            // validate stock and compute total
            for (CartItem item : cart) {
                int pid = item.getProduct().getId();
                int qty = item.getQuantity();

                ResultSet rs = stmt.executeQuery("SELECT stock FROM products WHERE id=" + pid);
                if (rs.next()) {
                    int stock = rs.getInt("stock");
                    if (qty > stock) {
                        throw new InsufficientStockException(item.getProduct().getName(), qty, stock);
                    }
                    total += item.getProduct().getPrice() * qty;
                }
                rs.close();
            }

            PreparedStatement psOrder = con.prepareStatement(
                    "INSERT INTO orders (user_id, total, status, payment_method, payment_status) VALUES (?, ?, 'PLACED', ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, uid);
            psOrder.setDouble(2, total);
            psOrder.setString(3, paymethod);
            psOrder.setString(4, payStatus);
            psOrder.executeUpdate();

            ResultSet keys = psOrder.getGeneratedKeys();
            keys.next();
            int orderid = keys.getInt(1);
            keys.close();
            psOrder.close();

            for (CartItem item : cart) {
                Product p = item.getProduct();
                int qty = item.getQuantity();

                PreparedStatement psItem = con.prepareStatement(
                        "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price) VALUES (?, ?, ?, ?, ?)");
                psItem.setInt(1, orderid);
                psItem.setInt(2, p.getId());
                psItem.setString(3, p.getName());
                psItem.setInt(4, qty);
                psItem.setDouble(5, p.getPrice());
                psItem.executeUpdate();
                psItem.close();

                stmt.executeUpdate("UPDATE products SET stock = stock - " + qty + " WHERE id = " + p.getId());
            }

            stmt.close();
            con.commit();
            con.close();
            return orderid;

        } catch (Exception e) {
            con.rollback();
            con.close();
            if (e instanceof InsufficientStockException)
                throw (InsufficientStockException) e;
            if (e instanceof SQLException)
                throw (SQLException) e;
            throw new SQLException("Order failed: " + e.getMessage());
        }
    }

    // cart save chyyan (logout time)

    public static void saveCart(int uid, List<CartItem> cart) throws SQLException {
        Connection con = connect();
        Statement stmt = con.createStatement();
        // clear existing saved cart for this user
        stmt.executeUpdate("DELETE FROM cart_items WHERE user_id=" + uid);
        for (CartItem item : cart) {
            stmt.executeUpdate("INSERT INTO cart_items (user_id, product_id, quantity) VALUES (" +
                    uid + ", " + item.getProduct().getId() + ", " + item.getQuantity() + ")");
        }
        stmt.close();
        con.close();
    }

    // cart load chyyan (login time)

    public static List<CartItem> loadCart(int uid) throws SQLException {
        List<CartItem> cart = new ArrayList<>();
        Connection con = connect();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT p.id, p.name, p.category, p.gender, p.size, p.price, p.stock, ci.quantity " +
                        "FROM cart_items ci " +
                        "JOIN products p ON ci.product_id = p.id " +
                        "WHERE ci.user_id=" + uid);
        while (rs.next()) {
            Product p = new Product(
                    rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                    rs.getString("gender"), rs.getString("size"),
                    rs.getDouble("price"), rs.getInt("stock"));
            int qty = rs.getInt("quantity");
            cart.add(new CartItem(p, qty));
        }
        rs.close();
        stmt.close();
        con.close();
        return cart;
    }

    // order status update chyyan

    public static void updateOrderStatus(int orderid, String status) throws SQLException {
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.executeUpdate("UPDATE orders SET status='" + status + "' WHERE id=" + orderid);
        stmt.close();
        con.close();
    }

    // payment status update chyyan

    public static void updatePaymentStatus(int orderid, String status) throws SQLException {
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.executeUpdate("UPDATE orders SET payment_status='" + status.replace("'", "''") + "' WHERE id=" + orderid);
        stmt.close();
        con.close();
    }
}
