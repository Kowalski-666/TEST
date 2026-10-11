/** A system user (admin / registrar) used for login. Inherits from Person. */
public class User extends Person {
    private String username;
    private String password;
    private String role;

    public User(String id, String firstName, String lastName, String username, String password, String role) {
        super(id, firstName, lastName, "", "");
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String getRole() { return role; }

    @Override
    public String getSummary() { return "Username: " + username; }

    public String toCsv() {
        return String.join(",", getId(), getFirstName(), getLastName(), username, password, role);
    }

    public static User fromCsv(String[] p) {
        return new User(p[0], p[1], p[2], p[3], p[4], p[5]);
    }
}
