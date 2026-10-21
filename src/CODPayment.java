public class CODPayment implements Payment {

    public boolean pay(double amount) {
        return amount > 0;
    }

    public String getMethodName() {
        return "CASH ON DELIVERY";
    }
}
