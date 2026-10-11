import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/** Form 2: Dashboard - gives access to all modules and shows live statistics. */
public class Dashboard extends JFrame {
    private final User user;
    private final DataStore store = DataStore.get();
    private final JLabel lblStudents = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblActive = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblSubjects = new JLabel("0", SwingConstants.CENTER);
    private final JLabel lblEnrollments = new JLabel("0", SwingConstants.CENTER);

    private ManageStudentsForm studentsForm;
    private EnrollmentForm enrollmentForm;
    private ReportsForm reportsForm;

    public Dashboard(User user) {
        super("Student Management System - Dashboard");
        this.user = user;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(820, 520);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UI.BG);
        setLayout(new BorderLayout());

        setJMenuBar(buildMenu());

        // ----- header -----
        JPanel head = UI.header("Dashboard");
        JLabel welcome = new JLabel("   Welcome, " + user.getFullName() + "  (" + user.getRole() + ")");
        welcome.setForeground(new Color(210, 222, 245));
        head.add(welcome);
        add(head, BorderLayout.NORTH);

        // ----- center -----
        JPanel center = new JPanel(new BorderLayout(0, 25));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JPanel cards = new JPanel(new GridLayout(1, 4, 18, 0));
        cards.setOpaque(false);
        cards.add(card("Total Students", lblStudents, UI.PRIMARY));
        cards.add(card("Active Students", lblActive, UI.SUCCESS));
        cards.add(card("Subjects", lblSubjects, UI.WARNING));
        cards.add(card("Active Enrollments", lblEnrollments, UI.GRAY));
        center.add(cards, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(2, 2, 18, 18));
        menu.setOpaque(false);
        JButton b1 = UI.button("Manage Students", UI.PRIMARY);
        JButton b2 = UI.button("Enrollment (Transaction)", UI.SUCCESS);
        JButton b3 = UI.button("Reports / Summary", UI.WARNING);
        JButton b4 = UI.button("Logout", UI.DANGER);
        for (JButton b : new JButton[]{b1, b2, b3, b4}) b.setFont(new Font("SansSerif", Font.BOLD, 16));
        menu.add(b1); menu.add(b2); menu.add(b3); menu.add(b4);
        center.add(menu, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        b1.addActionListener(e -> openStudents());
        b2.addActionListener(e -> openEnrollment());
        b3.addActionListener(e -> openReports());
        b4.addActionListener(e -> logout());

        addWindowListener(new WindowAdapter() {
            @Override public void windowActivated(WindowEvent e) { refreshStats(); }
            @Override public void windowClosing(WindowEvent e) { exitApp(); }
        });
        refreshStats();
    }

    private JMenuBar buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem logout = new JMenuItem("Logout");
        JMenuItem exit = new JMenuItem("Exit");
        logout.addActionListener(e -> logout());
        exit.addActionListener(e -> exitApp());
        file.add(logout);
        file.addSeparator();
        file.add(exit);

        JMenu modules = new JMenu("Modules");
        JMenuItem m1 = new JMenuItem("Manage Students");
        JMenuItem m2 = new JMenuItem("Enrollment");
        JMenuItem m3 = new JMenuItem("Reports");
        m1.addActionListener(e -> openStudents());
        m2.addActionListener(e -> openEnrollment());
        m3.addActionListener(e -> openReports());
        modules.add(m1); modules.add(m2); modules.add(m3);

        bar.add(file);
        bar.add(modules);
        return bar;
    }

    private JPanel card(String title, JLabel value, Color color) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(color);
        p.setBorder(BorderFactory.createEmptyBorder(14, 10, 14, 10));
        value.setFont(new Font("SansSerif", Font.BOLD, 34));
        value.setForeground(Color.WHITE);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setForeground(Color.WHITE);
        t.setFont(UI.BOLD);
        p.add(value, BorderLayout.CENTER);
        p.add(t, BorderLayout.SOUTH);
        return p;
    }

    private void refreshStats() {
        int active = 0;
        for (Student s : store.getStudents()) if ("Active".equals(s.getStatus())) active++;
        int enrolled = 0;
        for (Enrollment e : store.getEnrollments()) if (e.isEnrolled()) enrolled++;
        lblStudents.setText(String.valueOf(store.getStudents().size()));
        lblActive.setText(String.valueOf(active));
        lblSubjects.setText(String.valueOf(store.getSubjects().size()));
        lblEnrollments.setText(String.valueOf(enrolled));
    }

    private void openStudents() {
        if (studentsForm == null || !studentsForm.isDisplayable()) studentsForm = new ManageStudentsForm();
        studentsForm.setVisible(true);
        studentsForm.toFront();
    }

    private void openEnrollment() {
        if (enrollmentForm == null || !enrollmentForm.isDisplayable()) enrollmentForm = new EnrollmentForm();
        else enrollmentForm.reloadAll();
        enrollmentForm.setVisible(true);
        enrollmentForm.toFront();
    }

    private void openReports() {
        if (reportsForm == null || !reportsForm.isDisplayable()) reportsForm = new ReportsForm();
        else reportsForm.refresh();
        reportsForm.setVisible(true);
        reportsForm.toFront();
    }

    private void closeChildren() {
        if (studentsForm != null) studentsForm.dispose();
        if (enrollmentForm != null) enrollmentForm.dispose();
        if (reportsForm != null) reportsForm.dispose();
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            closeChildren();
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    private void exitApp() {
        if (JOptionPane.showConfirmDialog(this, "Exit the application?", "Exit",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) System.exit(0);
    }
}
