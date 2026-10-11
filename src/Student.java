/** A student record. Inherits from Person (Inheritance). */
public class Student extends Person {
    private String gender;
    private String course;
    private String yearLevel;
    private String status; // Active, Inactive, Graduated

    public Student(String id, String firstName, String lastName, String email, String contact,
                   String gender, String course, String yearLevel, String status) {
        super(id, firstName, lastName, email, contact);
        this.gender = gender;
        this.course = course;
        this.yearLevel = yearLevel;
        this.status = status;
    }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public String getYearLevel() { return yearLevel; }
    public void setYearLevel(String yearLevel) { this.yearLevel = yearLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String getRole() { return "Student"; }

    @Override
    public String getSummary() { return course + ", " + yearLevel + " (" + status + ")"; }

    /** Convert to one CSV line. */
    public String toCsv() {
        return String.join(",", getId(), getFirstName(), getLastName(), getEmail(), getContact(),
                gender, course, yearLevel, status);
    }

    public static Student fromCsv(String[] p) {
        return new Student(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7], p[8]);
    }
}
