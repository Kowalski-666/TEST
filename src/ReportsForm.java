import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/** Form 5: Reports / Summary - all values are computed from the real records. */
public class ReportsForm extends JFrame {
    private final DataStore store = DataStore.get();
    private final JTextArea overview = new JTextArea();
    private final DefaultTableModel courseModel = UI.model("Course", "Total", "Active", "Inactive", "Graduated");
    private final DefaultTableModel subjectModel = UI.model("Code", "Title", "Units", "Capacity", "Enrolled",
            "Available", "Utilization");
    private final DefaultTableModel billingModel = UI.model("Student ID", "Name", "Units", "Tuition",
            "Misc Fee", "Total Fee");
    private final JLabel lblBillingTotal = new JLabel(" ");

    public ReportsForm() {
        super("Reports / Summary");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(950, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(UI.header("Reports / Summary"), BorderLayout.NORTH);

        overview.setEditable(false);
        overview.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        overview.setMargin(new Insets(10, 12, 10, 12));

        JTable courseTable = new JTable(courseModel);
        JTable subjectTable = new JTable(subjectModel);
        JTable billingTable = new JTable(billingModel);
        UI.style(courseTable);
        UI.style(subjectTable);
        UI.style(billingTable);

        JPanel billingPanel = new JPanel(new BorderLayout());
        billingPanel.add(new JScrollPane(billingTable), BorderLayout.CENTER);
        lblBillingTotal.setFont(UI.BOLD);
        lblBillingTotal.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        billingPanel.add(lblBillingTotal, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", new JScrollPane(overview));
        tabs.addTab("Students per Course", new JScrollPane(courseTable));
        tabs.addTab("Subject Enrollment", new JScrollPane(subjectTable));
        tabs.addTab("Billing Summary", billingPanel);
        add(tabs, BorderLayout.CENTER);

        JButton btnRefresh = UI.button("Refresh", UI.PRIMARY);
        JButton btnExport = UI.button("Export Report to TXT", UI.SUCCESS);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        bottom.add(btnRefresh);
        bottom.add(btnExport);
        add(bottom, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> refresh());
        btnExport.addActionListener(e -> export());
        refresh();
    }

    public void refresh() {
        overview.setText(buildOverview());
        overview.setCaretPosition(0);
        buildCourseTable();
        buildSubjectTable();
        buildBillingTable();
    }

    private String buildOverview() {
        List<Student> students = store.getStudents();
        int active = 0, inactive = 0, graduated = 0;
        for (Student s : students) {
            switch (s.getStatus()) {
                case "Active": active++; break;
                case "Inactive": inactive++; break;
                default: graduated++;
            }
        }
        int enrolled = 0, dropped = 0;
        for (Enrollment e : store.getEnrollments()) { if (e.isEnrolled()) enrolled++; else dropped++; }

        // most popular subject
        String popular = "-";
        int best = -1;
        for (Subject s : store.getSubjects()) {
            int c = store.countEnrolled(s.getCode());
            if (c > best) { best = c; popular = s.getCode() + " - " + s.getTitle() + " (" + c + " students)"; }
        }

        double totalBilled = 0;
        for (Student s : students) totalBilled += DataStore.totalFeeFor(store.totalUnits(s.getId()));

        StringBuilder sb = new StringBuilder();
        sb.append("STUDENT MANAGEMENT SYSTEM - SUMMARY REPORT\n");
        sb.append("Generated: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append("\n");
        sb.append("==========================================================\n\n");
        sb.append("STUDENTS\n");
        sb.append(String.format("  Total students ........ %d%n", students.size()));
        sb.append(String.format("  Active ................ %d%n", active));
        sb.append(String.format("  Inactive .............. %d%n", inactive));
        sb.append(String.format("  Graduated ............. %d%n%n", graduated));

        sb.append("STUDENTS PER YEAR LEVEL\n");
        for (String y : DataStore.YEARS) {
            int c = 0;
            for (Student s : students) if (s.getYearLevel().equals(y)) c++;
            sb.append(String.format("  %-10s ............ %d%n", y, c));
        }

        sb.append("\nENROLLMENT TRANSACTIONS\n");
        sb.append(String.format("  Subjects offered ...... %d%n", store.getSubjects().size()));
        sb.append(String.format("  Active enrollments .... %d%n", enrolled));
        sb.append(String.format("  Dropped enrollments ... %d%n", dropped));
        sb.append("  Most popular subject .. ").append(popular).append("\n\n");

        sb.append("FINANCIAL SUMMARY\n");
        sb.append(String.format("  Total assessed fees ... %s%n%n", UI.peso(totalBilled)));

        // Polymorphism: a List<Person> holding different kinds of objects
        sb.append("SYSTEM USERS (polymorphism: Person.getRole() / getSummary())\n");
        List<Person> people = new ArrayList<>(store.getUsers());
        for (Person p : people) {
            sb.append("  [").append(p.getRole()).append("] ").append(p.getFullName())
                    .append(" - ").append(p.getSummary()).append("\n");
        }
        return sb.toString();
    }

    private void buildCourseTable() {
        courseModel.setRowCount(0);
        List<Student> students = store.getStudents();
        for (String c : DataStore.COURSES) {
            int total = 0, a = 0, i = 0, g = 0;
            for (Student s : students) {
                if (!s.getCourse().equals(c)) continue;
                total++;
                if ("Active".equals(s.getStatus())) a++;
                else if ("Inactive".equals(s.getStatus())) i++;
                else g++;
            }
            courseModel.addRow(new Object[]{c, total, a, i, g});
        }
    }

    private void buildSubjectTable() {
        subjectModel.setRowCount(0);
        for (Subject s : store.getSubjects()) {
            int taken = store.countEnrolled(s.getCode());
            double pct = s.getCapacity() == 0 ? 0 : taken * 100.0 / s.getCapacity();
            subjectModel.addRow(new Object[]{s.getCode(), s.getTitle(), s.getUnits(), s.getCapacity(), taken,
                    s.getCapacity() - taken, String.format("%.1f%%", pct)});
        }
    }

    private void buildBillingTable() {
        billingModel.setRowCount(0);
        double grand = 0;
        for (Student s : store.getStudents()) {
            int units = store.totalUnits(s.getId());
            if (units == 0) continue;
            double total = DataStore.totalFeeFor(units);
            grand += total;
            billingModel.addRow(new Object[]{s.getId(), s.getFullName(), units,
                    UI.peso(DataStore.tuitionFor(units)), UI.peso(DataStore.miscFor(units)), UI.peso(total)});
        }
        lblBillingTotal.setText("Grand total of assessed fees: " + UI.peso(grand));
    }

    private void export() {
        File f = new File("data", "summary_report.txt");
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.print(buildOverview());
        } catch (IOException ex) {
            UI.error(this, "Could not save the report: " + ex.getMessage());
            return;
        }
        UI.info(this, "Report saved to:\n" + f.getAbsolutePath());
    }
}
