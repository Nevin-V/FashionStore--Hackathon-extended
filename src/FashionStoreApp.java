import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class FashionStoreApp {
    private JFrame frame;

    private User user;

    private Map<Integer, CartItem> cart = new LinkedHashMap<>();

    // pmodel-product model
    // cmodel-customer model

    // apmodel-admin product model
    // aomodel-admin orders model

    private DefaultTableModel pmodel, cmodel, ordersmodel;
    private DefaultTableModel apmodel, aomodel;

    private JTextField searchtxt;

    private JComboBox<String> catfilter;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
            }
            new FashionStoreApp().showLogin();
        });
    }
    // aadhyathe login screen

    private void showLogin() {
        JFrame f = new JFrame("Fashion Store - Login");

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(400, 300);
        f.setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel title = new JLabel("FASHION STORE", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));

        JTextField userfield = new JTextField();
        JPasswordField passfield = new JPasswordField();

        JButton loginbtn = new JButton("Login");

        JButton regbtn = new JButton("Create New Account");

        panel.add(title);
        panel.add(new JLabel("Username:"));
        panel.add(userfield);

        panel.add(new JLabel("Password:"));
        panel.add(passfield);

        JPanel bpanel = new JPanel(new FlowLayout());
        bpanel.add(loginbtn);
        bpanel.add(regbtn);

        JPanel container = new JPanel(new BorderLayout(10, 10));
        container.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        container.add(panel, BorderLayout.CENTER);
        container.add(bpanel, BorderLayout.SOUTH);
        f.add(container);

        // login button nte action

        loginbtn.addActionListener(e -> {
            try {
                User u = DB.login(userfield.getText(), new String(passfield.getPassword()));
                if (u == null) {
                    JOptionPane.showMessageDialog(f, "Invalid username or password.");
                    return;
                }

                user = u;
                f.dispose();
                showMainDashboard();

            } catch (SQLException ex) {
                showError("Database error: " + ex.getMessage());
            }
        });

        regbtn.addActionListener(e -> showRegisterDialog(f));
        passfield.addActionListener(e -> loginbtn.doClick());

        f.setVisible(true);
    }

    // register chyyan

    private void showRegisterDialog(Component parent) {
        JTextField nametxt = new JTextField();
        JTextField usertxt = new JTextField();
        JPasswordField passtxt = new JPasswordField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));

        panel.add(new JLabel("Full Name:"));
        panel.add(nametxt);
        panel.add(new JLabel("Username:"));
        panel.add(usertxt);

        panel.add(new JLabel("Password:"));
        panel.add(passtxt);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Create Customer Account",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                DB.register(nametxt.getText(), usertxt.getText(), new String(passtxt.getPassword()));

                JOptionPane.showMessageDialog(parent, "Account registered! You can now log in.");

            } catch (SQLException ex) {

                showError("Registration failed: " + ex.getMessage());
            }
        }
    }

    // main dashboard

    private void showMainDashboard() {

        frame = new JFrame("Fashion Store | " + user.getFullName() + " (" + user.getRoleTitle() + ")");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 650);
        frame.setLocationRelativeTo(null);

        JPanel toppanel = new JPanel(new BorderLayout());
        toppanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel welcomelbl = new JLabel("Logged in as: " + user.getFullName() + " [" + user.getRoleTitle() + "]");
        welcomelbl.setFont(new Font("SansSerif", Font.BOLD, 14));

        JButton logoutbtn = new JButton("Logout");
        logoutbtn.addActionListener(e -> {
            frame.dispose();
            cart.clear();
            showLogin();
        });
        toppanel.add(welcomelbl, BorderLayout.WEST);

        toppanel.add(logoutbtn, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        if (user.isAdmin()) {

            tabs.addTab("Manage Products", createAdminProductsPanel());
            tabs.addTab("Manage Orders", createAdminOrdersPanel());
        } else {

            tabs.addTab("Catalogue", createCataloguePanel());
            tabs.addTab("Shopping Cart", createCartPanel());
            tabs.addTab("My Orders", createCustomerOrdersPanel());
        }

        frame.add(toppanel, BorderLayout.NORTH);
        frame.add(tabs, BorderLayout.CENTER);
        frame.setVisible(true);

        if (user.isAdmin()) {

            refreshAdminProducts();
            refreshAdminOrders();
        } else {

            refreshCatalogue();
            refreshCart();
            refreshCustomerOrders();
        }
    }

    // product display chyyan olla panel

    private JPanel createCataloguePanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel fbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchtxt = new JTextField(15);
        catfilter = new JComboBox<>(new String[] { "ALL", "TOPWEAR", "BOTTOMWEAR", "DRESSES", "FOOTWEAR" });
        JButton searchbtn = new JButton("Filter");
        JButton resetbtn = new JButton("Reset");
        JButton cartbtn = new JButton("Add Selected to Cart");

        fbar.add(new JLabel("Search:"));
        fbar.add(searchtxt);

        fbar.add(new JLabel("Category:"));
        fbar.add(catfilter);

        fbar.add(searchbtn);
        fbar.add(resetbtn);

        pmodel = createTableModel("ID", "Product Name", "Category", "Gender", "Size", "Price (Rs.)", "Stock");
        JTable table = new JTable(pmodel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        searchbtn.addActionListener(e -> refreshCatalogue());
        resetbtn.addActionListener(e -> {

            searchtxt.setText("");
            catfilter.setSelectedIndex(0);
            refreshCatalogue();
        });
        cartbtn.addActionListener(e -> addToCart(table));

        JPanel bottombar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottombar.add(cartbtn);

        panel.add(fbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bottombar, BorderLayout.SOUTH);
        return panel;
    }

    // cart items kanan olla panel

    private JPanel createCartPanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        cmodel = createTableModel("Product ID", "Product Name", "Size", "Price (Rs.)", "Quantity", "Subtotal (Rs.)");
        JTable table = new JTable(cmodel);

        JButton removebtn = new JButton("Remove Selected");
        JButton checkoutbtn = new JButton("Proceed to Checkout");

        removebtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0 || row >= cart.size()) {
                showError("Select an item to remove.");
                return;
            }
            int pid = Integer.parseInt(cmodel.getValueAt(row, 0).toString());
            cart.remove(pid);
            refreshCart();
        });

        checkoutbtn.addActionListener(e -> checkout());

        JPanel bbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bbar.add(removebtn);
        bbar.add(checkoutbtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bbar, BorderLayout.SOUTH);
        return panel;
    }

    // customer order kanan olla panel

    private JPanel createCustomerOrdersPanel() {

        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        ordersmodel = createTableModel("Order ID", "Total (Rs.)", "Status", "Payment Method", "Date Placed");
        JTable table = new JTable(ordersmodel);

        JButton refreshbtn = new JButton("Refresh Orders");
        refreshbtn.addActionListener(e -> refreshCustomerOrders());

        JPanel bbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bbar.add(refreshbtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bbar, BorderLayout.SOUTH);
        return panel;
    }

    // admin nte product panel

    private JPanel createAdminProductsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        apmodel = createTableModel("ID", "Name", "Category", "Gender", "Size", "Price", "Stock");
        JTable table = new JTable(apmodel);

        JButton addbtn = new JButton("Add Product");

        JButton editbtn = new JButton("Edit Selected");
        JButton deletebtn = new JButton("Delete Selected");
        JButton refreshbtn = new JButton("Refresh");

        addbtn.addActionListener(e -> openProductEditor(null));
        editbtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select a product to edit.");
                return;
            }
            int id = Integer.parseInt(apmodel.getValueAt(row, 0).toString());
            Product selected = findProductById(id);
            if (selected != null)
                openProductEditor(selected);
        });
        deletebtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select a product to delete.");
                return;
            }
            int id = Integer.parseInt(apmodel.getValueAt(row, 0).toString());
            int confirm = JOptionPane.showConfirmDialog(frame, "Delete product #" + id + "?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    DB.deleteProduct(id);
                    refreshAdminProducts();
                } catch (SQLException ex) {
                    showError("Could not delete: " + ex.getMessage());
                }
            }
        });
        refreshbtn.addActionListener(e -> refreshAdminProducts());

        JPanel bbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bbar.add(addbtn);
        bbar.add(editbtn);
        bbar.add(deletebtn);
        bbar.add(refreshbtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bbar, BorderLayout.SOUTH);
        return panel;
    }

    // adminte order panel

    private JPanel createAdminOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        aomodel = createTableModel("Order ID", "Customer", "Total", "Status", "Payment", "Date");
        JTable table = new JTable(aomodel);

        JComboBox<String> scombo = new JComboBox<>(
                new String[] { "PLACED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED" });
        JButton updatebtn = new JButton("Update Status");
        JButton refreshbtn = new JButton("Refresh");

        updatebtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Select an order to update.");
                return;
            }
            int orderid = Integer.parseInt(aomodel.getValueAt(row, 0).toString());
            String newStatus = String.valueOf(scombo.getSelectedItem());
            try {
                DB.updateOrderStatus(orderid, newStatus);
                refreshAdminOrders();
                JOptionPane.showMessageDialog(frame, "Order #" + orderid + " updated to " + newStatus);
            } catch (SQLException ex) {
                showError("Update failed: " + ex.getMessage());
            }
        });
        refreshbtn.addActionListener(e -> refreshAdminOrders());

        JPanel bbar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bbar.add(new JLabel("Set Status:"));
        bbar.add(scombo);
        bbar.add(updatebtn);
        bbar.add(refreshbtn);

        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(bbar, BorderLayout.SOUTH);
        return panel;
    }

    // product cart il add chyyan

    private void addToCart(JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a product from the table first.");
            return;
        }

        int id = Integer.parseInt(pmodel.getValueAt(row, 0).toString());
        Product product = findProductById(id);

        if (product == null) {
            showError("Product unavailable.");
            return;
        }

        String input = JOptionPane.showInputDialog(frame,
                "Stock Available: " + product.getStock() + "\nEnter Quantity:", "1");
        if (input == null)
            return;

        try {
            int qty = Integer.parseInt(input.trim());
            if (qty <= 0) {
                showError("Quantity must be greater than zero.");
                return;
            }

            CartItem existing = cart.get(id);
            int totalqty = qty + (existing == null ? 0 : existing.getQuantity());

            if (totalqty > product.getStock()) {
                throw new InsufficientStockException(product.getName(), totalqty, product.getStock());
            }

            if (existing == null) {
                cart.put(id, new CartItem(product, qty));
            } else {
                existing.setQuantity(totalqty);
            }

            refreshCart();
            JOptionPane.showMessageDialog(frame, "Item added to cart!");
        } catch (NumberFormatException ex) {
            showError("Quantity must be a valid number.");
        } catch (InsufficientStockException ex) {
            showError(ex.getMessage());
        }
    }

    // product buy chyyan

    private void checkout() {
        if (cart.isEmpty()) {
            showError("Your cart is empty.");
            return;
        }

        double total = 0;
        for (CartItem item : cart.values())
            total += item.getSubtotal();

        Object[] options = { "Cash on Delivery", "Demo Card", "Cancel" };
        int choice = JOptionPane.showOptionDialog(frame,
                "Total Amount: Rs. " + total + "\nSelect Payment Method:",
                "Checkout", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (choice < 0 || choice == 2)
            return;

        Payment payment = PaymentFactory.createPayment(choice);
        if (!payment.pay(total)) {
            showError("Payment cancelled.");
            return;
        }

        Map<Integer, Integer> quantities = new LinkedHashMap<>();
        for (Map.Entry<Integer, CartItem> entry : cart.entrySet()) {
            quantities.put(entry.getKey(), entry.getValue().getQuantity());
        }

        try {
            int orderid = DB.placeOrder(user.getId(), quantities, payment.getMethodName());
            cart.clear();
            refreshCart();
            refreshCatalogue();
            refreshCustomerOrders();

            JOptionPane.showMessageDialog(frame, "Order #" + orderid + " Placed!\n\n" +
                    "Amount: Rs. " + total + "\n" +
                    "Payment: " + payment.getMethodName());
        } catch (InsufficientStockException ex) {
            showError(ex.getMessage());
            refreshCatalogue();
        } catch (Exception ex) {
            showError("Order failed: " + ex.getMessage());
        }
    }

    // product edit chyyan

    private void openProductEditor(Product old) {
        JTextField nametxt = new JTextField(old == null ? "" : old.getName());
        JTextField cattxt = new JTextField(old == null ? "TOPWEAR" : old.getCategory());
        JComboBox<String> gcombo = new JComboBox<>(new String[] { "MEN", "WOMEN", "UNISEX" });
        JTextField sizetxt = new JTextField(old == null ? "M" : old.getSize());
        JTextField pricetxt = new JTextField(old == null ? "599" : String.valueOf(old.getPrice()));
        JTextField stocktxt = new JTextField(old == null ? "10" : String.valueOf(old.getStock()));

        if (old != null)
            gcombo.setSelectedItem(old.getGender());

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Product Name:"));
        p.add(nametxt);
        p.add(new JLabel("Category:"));
        p.add(cattxt);
        p.add(new JLabel("Gender:"));
        p.add(gcombo);
        p.add(new JLabel("Size (S/M/L/XL):"));
        p.add(sizetxt);
        p.add(new JLabel("Price (Rs.):"));
        p.add(pricetxt);
        p.add(new JLabel("Stock Quantity:"));
        p.add(stocktxt);

        int res = JOptionPane.showConfirmDialog(frame, p, old == null ? "Add Product" : "Edit Product",
                JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                double price = Double.parseDouble(pricetxt.getText().trim());
                int stock = Integer.parseInt(stocktxt.getText().trim());
                DB.saveProduct(old == null ? 0 : old.getId(), nametxt.getText(), cattxt.getText(),
                        String.valueOf(gcombo.getSelectedItem()), sizetxt.getText(), price, stock);
                refreshAdminProducts();
            } catch (NumberFormatException ex) {
                showError("Price and stock must be numbers.");
            } catch (SQLException ex) {
                showError(ex.getMessage());
            }
        }
    }

    // ellam refresh chyyan

    private void refreshCatalogue() {
        if (pmodel == null)
            return;
        pmodel.setRowCount(0);
        try {
            String search = searchtxt.getText().trim().toLowerCase();
            String cat = String.valueOf(catfilter.getSelectedItem());

            List<Product> list = cat.equals("ALL") ? DB.getProducts() : DB.getProducts(cat);
            for (Product p : list) {
                if (p.getName().toLowerCase().contains(search)) {
                    pmodel.addRow(new Object[] { p.getId(), p.getName(), p.getCategory(), p.getGender(),
                            p.getSize(), p.getPrice(), p.getStock() });
                }
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshCart() {
        if (cmodel == null)
            return;
        cmodel.setRowCount(0);
        double total = 0;
        for (CartItem item : cart.values()) {
            total += item.getSubtotal();
            cmodel.addRow(new Object[] { item.getProduct().getId(), item.getProduct().getName(),
                    item.getProduct().getSize(), item.getProduct().getPrice(),
                    item.getQuantity(), item.getSubtotal() });
        }
        cmodel.addRow(new Object[] { "", "", "", "", "TOTAL:", "Rs. " + total });
    }

    private void refreshCustomerOrders() {
        if (ordersmodel == null)
            return;
        ordersmodel.setRowCount(0);
        try {
            for (OrderRecord o : DB.getOrders(user.getId())) {
                ordersmodel.addRow(new Object[] { o.getId(), o.getTotal(), o.getStatus(), o.getPaymentMethod(),
                        o.getCreatedAt() });
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAdminProducts() {
        if (apmodel == null)
            return;
        apmodel.setRowCount(0);
        try {
            for (Product p : DB.getProducts()) {
                apmodel.addRow(new Object[] { p.getId(), p.getName(), p.getCategory(), p.getGender(),
                        p.getSize(), p.getPrice(), p.getStock() });
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAdminOrders() {
        if (aomodel == null)
            return;
        aomodel.setRowCount(0);
        try {
            for (OrderRecord o : DB.getOrders(null)) {
                aomodel.addRow(new Object[] { o.getId(), o.getCustomer(), o.getTotal(), o.getStatus(),
                        o.getPaymentMethod(), o.getCreatedAt() });
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private DefaultTableModel createTableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private Product findProductById(int id) {
        try {
            for (Product p : DB.getProducts()) {
                if (p.getId() == id)
                    return p;
            }
        } catch (SQLException e) {
        }
        return null;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Fashion Store", JOptionPane.ERROR_MESSAGE);
    }
}
