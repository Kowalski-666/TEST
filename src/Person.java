/**
 * Abstract base class for every person in the system.
 * Demonstrates: Encapsulation (private fields + getters/setters),
 * Abstraction and Polymorphism (abstract methods overridden by subclasses).
 */
public abstract class Person {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String contact;

    public Person(String id, String firstName, String lastName, String email, String contact) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.contact = contact;
    }

    // ---------- Getters and Setters ----------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getFullName() { return lastName + ", " + firstName; }

    // ---------- Polymorphic methods ----------
    public abstract String getRole();
    public abstract String getSummary();

    @Override
    public String toString() { return getRole() + ": " + getFullName(); }
}
