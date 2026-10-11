/** A transaction record: one student enrolled in one subject. */
public class Enrollment {
    public static final String ENROLLED = "Enrolled";
    public static final String DROPPED = "Dropped";

    private String enrollmentId;
    private String studentId;
    private String subjectCode;
    private String date;
    private String status;

    public Enrollment(String enrollmentId, String studentId, String subjectCode, String date, String status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.date = date;
        this.status = status;
    }

    public String getEnrollmentId() { return enrollmentId; }
    public String getStudentId() { return studentId; }
    public String getSubjectCode() { return subjectCode; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isEnrolled() { return ENROLLED.equals(status); }

    public String toCsv() { return String.join(",", enrollmentId, studentId, subjectCode, date, status); }

    public static Enrollment fromCsv(String[] p) {
        return new Enrollment(p[0], p[1], p[2], p[3], p[4]);
    }
}
