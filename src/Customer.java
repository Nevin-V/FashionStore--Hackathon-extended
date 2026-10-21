public class Customer extends User {

    public Customer(int id, String username, String fullname) {
        super(id, username, fullname);
    }

    public String getRole() {
        return "CUSTOMER";
    }

    public String getRoleTitle() {
        return "Customer";
    }
}
