import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class DataStore {

    public static final String[] COURSES = {
        "BSIT", "BSCS", "BSBA", "BSED", "BSCE", "BSN"
    };
    public static final String[] YEARS = {
        "1st Year", "2nd Year", "3rd Year", "4th Year"
    };
    public static final String[] STATUSES = {
        "Active", "Inactive", "Graduated"
    };

    public static final int MAX_UNITS = 24;
    public static final double RATE_PER_UNIT = 1500.00;
    public static final double MISC_FEE = 2500.00;

    private static final String DIR = "data";
    private static final DataStore INSTANCE = new DataStore();

    public static DataStore get() {
        return INSTANCE;
    }

    private final List<User> users = new ArrayList<>();
    private final List<Student> students = new ArrayList<>();
    private final List<Subject> subjects = new ArrayList<>();
    private final List<Enrollment> enrollments = new ArrayList<>();

    private DataStore() {
        File directory = new File(DIR);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException(
                "Cannot create data directory: " +
                directory.getAbsolutePath()
            );
        }

        loadUsers();
        loadStudents();
        loadSubjects();
        loadEnrollments();
    }

    // LOGIN

    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        for (User u : users) {
            if (username.equals(u.getUsername())
                    && password.equals(u.getPassword())) {
                return u;
            }
        }

        return null;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    // STUDENTS

    public List<Student> getStudents() {
        return new ArrayList<>(students);
    }

    public Student findStudent(String id) {
        if (id == null) return null;

        for (Student s : students) {
            if (id.equals(s.getId())) return s;
        }

        return null;
    }

    public boolean emailExists(String email, String exceptId) {
        if (email == null) return false;

        for (Student s : students) {
            if (s.getEmail() != null
                    && s.getEmail().equalsIgnoreCase(email)
                    && !Objects.equals(s.getId(), exceptId)) {
                return true;
            }
        }

        return false;
    }

    public void addStudent(Student s) {
        if (s == null) {
            throw new IllegalArgumentException(
                "Student cannot be null."
            );
        }

        students.add(s);
        saveStudents();
    }

    public void updateStudent(Student updated) {
        if (updated == null) return;

        for (int i = 0; i < students.size(); i++) {
            if (Objects.equals(
                    students.get(i).getId(), updated.getId())) {
                students.set(i, updated);
                saveStudents();
                return;
            }
        }
    }

    public String deleteStudent(String id) {
        if (id == null) return "Please select a student.";

        for (Enrollment e : enrollments) {
            if (id.equals(e.getStudentId()) && e.isEnrolled()) {
                return "This student still has active enrollments.\n"
                    + "Drop the subjects first in the Enrollment module.";
            }
        }

        students.removeIf(s -> id.equals(s.getId()));
        enrollments.removeIf(e -> id.equals(e.getStudentId()));

        saveStudents();
        saveEnrollments();

        return null;
    }

    // SUBJECTS

    public List<Subject> getSubjects() {
        return new ArrayList<>(subjects);
    }

    public Subject findSubject(String code) {
        if (code == null) return null;

        for (Subject s : subjects) {
            if (code.equals(s.getCode())) return s;
        }

        return null;
    }

    // ENROLLMENTS

    public List<Enrollment> getEnrollments() {
        return new ArrayList<>(enrollments);
    }

    public int countEnrolled(String subjectCode) {
        int count = 0;

        for (Enrollment e : enrollments) {
            if (Objects.equals(e.getSubjectCode(), subjectCode)
                    && e.isEnrolled()) {
                count++;
            }
        }

        return count;
    }

    public int totalUnits(String studentId) {
        int total = 0;

        for (Enrollment e : enrollments) {
            if (Objects.equals(e.getStudentId(), studentId)
                    && e.isEnrolled()) {
                Subject subject = findSubject(e.getSubjectCode());

                if (subject != null) {
                    total += subject.getUnits();
                }
            }
        }

        return total;
    }

    public static double tuitionFor(int units) {
        return units * RATE_PER_UNIT;
    }

    public static double miscFor(int units) {
        return units > 0 ? MISC_FEE : 0;
    }

    public static double totalFeeFor(int units) {
        return tuitionFor(units) + miscFor(units);
    }

    public String enroll(String studentId, String subjectCode) {
        Student student = findStudent(studentId);

        if (student == null) return "Please select a student.";

        Subject subject = findSubject(subjectCode);

        if (subject == null) return "Please select a subject.";

        if (!"Active".equals(student.getStatus())) {
            return "Only ACTIVE students can enroll.";
        }

        for (Enrollment e : enrollments) {
            if (Objects.equals(e.getStudentId(), studentId)
                    && Objects.equals(e.getSubjectCode(), subjectCode)
                    && e.isEnrolled()) {
                return "The student is already enrolled in "
                    + subjectCode + ".";
            }
        }

        if (countEnrolled(subjectCode) >= subject.getCapacity()) {
            return "Subject " + subjectCode + " is already FULL ("
                + subject.getCapacity() + " slots).";
        }

        int newTotal = totalUnits(studentId) + subject.getUnits();

        if (newTotal > MAX_UNITS) {
            return "Unit limit exceeded. Maximum is " + MAX_UNITS
                + " units (would become " + newTotal + ").";
        }

        enrollments.add(new Enrollment(
            nextEnrollmentId(),
            studentId,
            subjectCode,
            LocalDate.now().toString(),
            Enrollment.ENROLLED
        ));

        saveEnrollments();
        return null;
    }

    public String drop(String enrollmentId) {
        if (enrollmentId == null) {
            return "Please select an enrollment.";
        }

        for (Enrollment e : enrollments) {
            if (enrollmentId.equals(e.getEnrollmentId())) {
                if (!e.isEnrolled()) {
                    return "This enrollment is already dropped.";
                }

                e.setStatus(Enrollment.DROPPED);
                saveEnrollments();
                return null;
            }
        }

        return "Enrollment record not found.";
    }

    private String nextEnrollmentId() {
        int max = 0;

        for (Enrollment e : enrollments) {
            try {
                String id = e.getEnrollmentId();

                if (id != null && id.startsWith("E-")) {
                    max = Math.max(
                        max,
                        Integer.parseInt(id.substring(2))
                    );
                }
            } catch (NumberFormatException ignored) {
                // Ignore invalid IDs.
            }
        }

        return String.format("E-%04d", max + 1);
    }

    // CSV FILE HANDLING

    private List<String[]> readCsv(String fileName) {
        File file = new File(DIR, fileName);

        if (!file.exists()) return null;

        List<String[]> rows = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file))) {

            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    rows.add(line.split(",", -1));
                }
            }

        } catch (IOException ex) {
            throw new IllegalStateException(
                "Cannot read file: " + file.getAbsolutePath(), ex
            );
        }

        return rows;
    }

    private void writeCsv(
            String fileName, String header, List<String> lines) {

        File file = new File(DIR, fileName);

        try (PrintWriter writer = new PrintWriter(
                new BufferedWriter(new FileWriter(file)))) {

            writer.println(header);

            for (String line : lines) {
                writer.println(line);
            }

            if (writer.checkError()) {
                throw new IllegalStateException(
                    "Error writing " + file.getAbsolutePath()
                );
            }

        } catch (IOException ex) {
            throw new IllegalStateException(
                "Cannot write file: " + file.getAbsolutePath(), ex
            );
        }
    }

    // LOAD USERS

    private void loadUsers() {
        List<String[]> rows = readCsv("users.csv");

        if (rows == null) {
            users.add(new User(
                "U-001", "System", "Administrator",
                "admin", "admin123", "Administrator"
            ));

            users.add(new User(
                "U-002", "Maria", "Registrar",
                "registrar", "reg123", "Registrar"
            ));

            saveUsers();
        } else {
            for (String[] row : rows) {
                users.add(User.fromCsv(row));
            }
        }
    }

    // LOAD STUDENTS

    private void loadStudents() {
        List<String[]> rows = readCsv("students.csv");

        if (rows == null) {
            seedStudents();
            saveStudents();
        } else {
            for (String[] row : rows) {
                students.add(Student.fromCsv(row));
            }
        }
    }

    // LOAD SUBJECTS

    private void loadSubjects() {
        List<String[]> rows = readCsv("subjects.csv");

        if (rows == null) {
            seedSubjects();
            saveSubjects();
        } else {
            for (String[] row : rows) {
                subjects.add(Subject.fromCsv(row));
            }
        }
    }

    // LOAD ENROLLMENTS

    private void loadEnrollments() {
        List<String[]> rows = readCsv("enrollments.csv");

        if (rows == null) {
            seedEnrollments();
            saveEnrollments();
        } else {
            for (String[] row : rows) {
                enrollments.add(Enrollment.fromCsv(row));
            }
        }
    }

    // SAVE DATA

    private void saveUsers() {
        List<String> lines = new ArrayList<>();

        for (User u : users) lines.add(u.toCsv());

        writeCsv(
            "users.csv",
            "id,firstName,lastName,username,password,role",
            lines
        );
    }

    private void saveStudents() {
        List<String> lines = new ArrayList<>();

        for (Student s : students) lines.add(s.toCsv());

        writeCsv(
            "students.csv",
            "id,firstName,lastName,email,contact,gender,course,yearLevel,status",
            lines
        );
    }

    private void saveSubjects() {
        List<String> lines = new ArrayList<>();

        for (Subject s : subjects) lines.add(s.toCsv());

        writeCsv("subjects.csv", "code,title,units,capacity", lines);
    }

    private void saveEnrollments() {
        List<String> lines = new ArrayList<>();

        for (Enrollment e : enrollments) lines.add(e.toCsv());

        writeCsv(
            "enrollments.csv",
            "enrollmentId,studentId,subjectCode,date,status",
            lines
        );
    }

    // SAMPLE STUDENTS

    private void seedStudents() {
        String[][] names = {
            {"Juan", "Dela Cruz"}, {"Maria", "Santos"},
            {"Jose", "Reyes"}, {"Ana", "Garcia"},
            {"Mark", "Villanueva"}, {"Angela", "Ramos"},
            {"Paolo", "Mendoza"}, {"Kristine", "Bautista"},
            {"Carlo", "Aquino"}, {"Jessa", "Navarro"},
            {"Miguel", "Torres"}, {"Sofia", "Castillo"},
            {"Rafael", "Domingo"}, {"Bianca", "Flores"},
            {"Daniel", "Cruz"}, {"Patricia", "Lim"},
            {"Joshua", "Pascual"}, {"Nicole", "Tan"},
            {"Gabriel", "Soriano"}, {"Camille", "Dizon"},
            {"Francis", "Morales"}, {"Isabel", "Fernandez"}
        };

        String[] gender = {
            "Male", "Female", "Male", "Female", "Male",
            "Female", "Male", "Female", "Male", "Female",
            "Male", "Female", "Male", "Female", "Male",
            "Female", "Male", "Female", "Male", "Female",
            "Male", "Female"
        };

        for (int i = 0; i < names.length; i++) {
            String first = names[i][0];
            String last = names[i][1];

            String email = (first + "." + last)
                .toLowerCase().replace(" ", "")
                + "@student.edu.ph";

            String contact = String.format(
                "0917%07d", 1234500 + i * 37
            );

            String status = "Active";

            if (i == 19) status = "Inactive";
            if (i == 20 || i == 21) status = "Graduated";

            students.add(new Student(
                String.format("2026-%04d", i + 1),
                first, last, email, contact, gender[i],
                COURSES[i % COURSES.length],
                YEARS[i % YEARS.length], status
            ));
        }
    }

    // SAMPLE SUBJECTS

    private void seedSubjects() {
        subjects.add(new Subject(
            "IT101", "Introduction to Computing", 3, 40
        ));
        subjects.add(new Subject(
            "IT102", "Computer Programming 1", 3, 35
        ));
        subjects.add(new Subject(
            "IT201", "Data Structures and Algorithms", 3, 30
        ));
        subjects.add(new Subject(
            "IT202", "Database Management Systems", 3, 30
        ));
        subjects.add(new Subject(
            "IT301", "Capstone Project 1", 3, 3
        ));
        subjects.add(new Subject(
            "MATH101", "College Algebra", 3, 40
        ));
        subjects.add(new Subject(
            "ENG101", "Purposive Communication", 3, 40
        ));
        subjects.add(new Subject(
            "BA101", "Principles of Management", 3, 35
        ));
        subjects.add(new Subject(
            "PE101", "Physical Fitness", 2, 40
        ));
        subjects.add(new Subject(
            "NSTP101", "National Service Training", 3, 50
        ));
    }

    // SAMPLE ENROLLMENTS

    private void seedEnrollments() {
        int sequence = 0;

        for (int i = 0; i < 19; i++) {
            Student student = students.get(i);

            int[] picks = {
                i % 10, (i + 3) % 10, (i + 7) % 10
            };

            for (int k = 0; k < picks.length; k++) {
                Subject subject = subjects.get(picks[k]);

                if (countEnrolled(subject.getCode())
                        >= subject.getCapacity()) {
                    continue;
                }

                String status =
                    (i % 7 == 0 && k == 1)
                    ? Enrollment.DROPPED
                    : Enrollment.ENROLLED;

                enrollments.add(new Enrollment(
                    String.format("E-%04d", ++sequence),
                    student.getId(),
                    subject.getCode(),
                    "2026-08-" + String.format(
                        "%02d", 1 + (i % 25)
                    ),
                    status
                ));
            }
        }
    }
}