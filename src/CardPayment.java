import javax.swing.JOptionPane;

public class CardPayment implements Payment {

    public boolean pay(double amount) {
        String pin = JOptionPane.showInputDialog(null,
                "Demo card payment for Rs. " + amount +
                "\nEnter any 4-digit demo PIN:",
                "Demo Card Payment", JOptionPane.PLAIN_MESSAGE);
        if (pin != null && pin.length() == 4) {
            return true;
        }
        return false;
    }

    public String getMethodName() {
        return "DEMO CARD";
    }
}
