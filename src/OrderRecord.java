public class OrderRecord {
    private int id;
    private String customer;
    private double total;
    private String status;
    private String paymentmethod;
    private String paymentStatus;
    private String date;

    public OrderRecord(int id, String customer, double total, String status, String paymentmethod, String paymentStatus, String date) {
        this.id = id;
        this.customer = customer;
        this.total = total;
        this.status = status;
        this.paymentmethod = paymentmethod;
        this.paymentStatus = (paymentStatus != null && !paymentStatus.trim().isEmpty()) ? paymentStatus : "PAID";
        this.date = date;
    }

    public OrderRecord(int id, String customer, double total, String status, String paymentmethod, String date) {
        this(id, customer, total, status, paymentmethod, "PAID", date);
    }

    public int getId() {
        return id;
    }

    public String getCustomer() {
        return customer;
    }

    public double getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public String getPaymentMethod() {
        return paymentmethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getCreatedAt() {
        return date;
    }
}
