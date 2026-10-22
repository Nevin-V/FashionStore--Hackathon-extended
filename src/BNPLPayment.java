import javax.swing.*;
import java.awt.*;

public class BNPLPayment implements Payment {
    private int tenure = 1;
    private double installment = 0;
    private String plan = "Full Payment";

    @Override
    public boolean pay(double amount) {
        String[] options = {
            "Full Payment (1 Month) - Rs. " + String.format("%.2f", amount),
            "3 Months EMI - Rs. " + String.format("%.2f", amount / 3) + " / month",
            "6 Months EMI - Rs. " + String.format("%.2f", amount / 6) + " / month",
            "12 Months EMI - Rs. " + String.format("%.2f", amount / 12) + " / month"
        };

        JComboBox<String> planCombo = new JComboBox<>(options);
        JLabel emiLabel = new JLabel("Instalment Amount: Rs. " + String.format("%.2f", amount) + " (Due in 30 Days)");
        emiLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        emiLabel.setForeground(new Color(0, 128, 64));

        planCombo.addActionListener(e -> {
            int idx = planCombo.getSelectedIndex();
            int t = (idx == 0) ? 1 : (idx == 1 ? 3 : (idx == 2 ? 6 : 12));
            double inst = Math.round((amount / t) * 100.0) / 100.0;
            if (t == 1) {
                emiLabel.setText("Instalment Amount: Rs. " + String.format("%.2f", amount) + " (Due in 30 Days)");
            } else {
                emiLabel.setText("Monthly Instalment: Rs. " + String.format("%.2f", inst) + " / month (" + t + " Months)");
            }
        });

        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.add(new JLabel("Total Order Amount: Rs. " + String.format("%.2f", amount)));
        panel.add(new JLabel("Choose Plan (Full Payment or Monthly Instalments):"));
        panel.add(planCombo);
        panel.add(emiLabel);

        int res = JOptionPane.showConfirmDialog(null, panel, "Buy Now, Pay Later (EMI)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (res == JOptionPane.OK_OPTION) {
            int idx = planCombo.getSelectedIndex();
            if (idx == 0) {
                tenure = 1;
                plan = "Full Payment";
                installment = amount;
            } else {
                tenure = (idx == 1 ? 3 : (idx == 2 ? 6 : 12));
                plan = tenure + "M EMI";
                installment = Math.round((amount / tenure) * 100.0) / 100.0;
            }
            return true;
        }
        return false;
    }

    @Override
    public String getMethodName() {
        return "BNPL (" + plan + ")";
    }

    public int getTenure() {
        return tenure;
    }

    public double getInstallment() {
        return installment;
    }

    public String getInitialPaymentStatus() {
        return (tenure == 1) ? "PENDING (Pay in 30 Days)" : "PENDING (0/" + tenure + " Paid)";
    }
}
