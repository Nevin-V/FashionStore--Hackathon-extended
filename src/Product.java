public class Product {
    private int id;
    private String name;
    private String category;
    private String gender;
    private String size;
    private double price;
    private int stock;

    public Product(int id, String name, String category, String gender, String size, double price, int stock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.gender = gender;
        this.size = size;
        this.price = price;
        this.stock = stock;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getGender() {
        return gender;
    }

    public String getSize() {
        return size;
    }

    public double getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }
}
