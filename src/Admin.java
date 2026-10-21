public class Admin extends User {

    public Admin(int id, String username, String fullname) {
        super(id, username, fullname);
    }

    public String getRole() {
        return "ADMIN";
    }

    public String getRoleTitle() {
        return "Store Administrator";
    }
}
