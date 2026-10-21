public class InsufficientStockException extends Exception {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String name, int requested, int available) {
        super("Not enough stock for '" + name + "'. Requested: " + requested + ", Available: " + available);
    }
}
