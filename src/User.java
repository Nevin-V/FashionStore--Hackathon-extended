public abstract class User {
    private int id;
    private String username;
    private String fullname;

    public User(int id, String username, String fullname) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullname;
    }

    public abstract String getRole();

    public abstract String getRoleTitle();

    public boolean isAdmin() {
        return getRole().equals("ADMIN");
    }
}
