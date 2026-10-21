public class PaymentFactory {

    public static Payment createPayment(int choice) {
        if (choice == 0) {
            return new CODPayment();
        } else if (choice == 1) {
            return new CardPayment();
        } else {
            return new CODPayment();
        }
    }
}
