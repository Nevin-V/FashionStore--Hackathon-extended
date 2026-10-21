import java.sql.*;
import java.util.*;

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
        return DriverManager.getConnection(URL, USER, PASSWORD);
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
        return getProducts("ALL");
    }

    public static List<Product> getProducts(String category) throws SQLException {
        List<Product> list = new ArrayList<>();
        Connection con = connect();
        Statement stmt = con.createStatement();

        String sql;
        if (category != null && !category.equals("ALL")) {
            sql = "SELECT * FROM products WHERE category='" + category.toUpperCase() + "' ORDER BY id DESC";
        } else {
            sql = "SELECT * FROM products ORDER BY id DESC";
        }

        ResultSet rs = stmt.executeQuery(sql);
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
                    rs.getString("created_at")));
        }

        rs.close();
        stmt.close();
        con.close();
        return list;
    }

    // order place chyyan

    public static int placeOrder(int uid, Map<Integer, Integer> cart, String paymethod)
            throws SQLException, InsufficientStockException {

        Connection con = connect();
        con.setAutoCommit(false);

        try {
            double total = 0;
            Map<Integer, Product> pmap = new HashMap<>();
            Statement stmt = con.createStatement();

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                int pid = entry.getKey();
                int qty = entry.getValue();

                ResultSet rs = stmt.executeQuery("SELECT * FROM products WHERE id=" + pid);
                if (rs.next()) {
                    Product p = new Product(rs.getInt("id"), rs.getString("name"), rs.getString("category"),
                            rs.getString("gender"), rs.getString("size"), rs.getDouble("price"), rs.getInt("stock"));

                    if (qty > p.getStock()) {
                        throw new InsufficientStockException(p.getName(), qty, p.getStock());
                    }
                    pmap.put(pid, p);
                    total += p.getPrice() * qty;
                }
                rs.close();
            }

            stmt.executeUpdate("INSERT INTO orders (user_id, total, status, payment_method) VALUES (" +
                    uid + ", " + total + ", 'PLACED', '" + paymethod + "')",
                    Statement.RETURN_GENERATED_KEYS);
            ResultSet keys = stmt.getGeneratedKeys();
            keys.next();
            int orderid = keys.getInt(1);
            keys.close();

            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                Product p = pmap.get(entry.getKey());
                int qty = entry.getValue();

                stmt.executeUpdate(
                        "INSERT INTO order_items (order_id, product_id, product_name, quantity, unit_price) VALUES (" +
                                orderid + ", " + p.getId() + ", '" + p.getName() + "', " + qty + ", " + p.getPrice()
                                + ")");

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

    // order status update chyyan

    public static void updateOrderStatus(int orderid, String status) throws SQLException {
        Connection con = connect();
        Statement stmt = con.createStatement();
        stmt.executeUpdate("UPDATE orders SET status='" + status + "' WHERE id=" + orderid);
        stmt.close();
        con.close();
    }
}
