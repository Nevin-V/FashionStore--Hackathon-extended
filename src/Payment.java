public interface Payment {
    boolean pay(double amount);
    String getMethodName();
}
