import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.regex.Pattern;

/** Form 3: Manage Students - Create, Read, Update, Delete + Search + Filter + Validation. */
public class ManageStudentsForm extends JFrame {
    private final DataStore store = DataStore.get();

    // form fields
    private final JTextField tfId = new JTextField(16);
    private final JTextField tfFirst = new JTextField(16);
    private final JTextField tfLast = new JTextField(16);
    private final JTextField tfEmail = new JTextField(16);
    private final JTextField tfContact = new JTextField(16);
    private final JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female"});
    private final JComboBox<String> cbCourse = new JComboBox<>(DataStore.COURSES);
    private final JComboBox<String> cbYear = new JComboBox<>(DataStore.YEARS);
    private final JComboBox<String> cbStatus = new JComboBox<>(DataStore.STATUSES);

    // search + filter
    private final JTextField tfSearch = new JTextField(14);
    private final JComboBox<String> cbSearchBy = new JComboBox<>(new String[]{"All", "ID", "Name", "Course"});
    private final JComboBox<String> fCourse = new JComboBox<>(withAll(DataStore.COURSES));
    private final JComboBox<String> fYear = new JComboBox<>(withAll(DataStore.YEARS));
    private final JComboBox<String> fStatus = new JComboBox<>(withAll(DataStore.STATUSES));
    private final JLabel lblCount = new JLabel();

    private final DefaultTableModel model = UI.model("Student ID", "Last Name", "First Name", "Email",
            "Contact", "Gender", "Course", "Year Level", "Status");
    private final JTable table = new JTable(model);

    private static final Pattern ID_PATTERN = Pattern.compile("\\d{4}-\\d{4}");
    private static final Pattern NAME_PATTERN = Pattern.compile("[A-Za-z][A-Za-z .'-]*");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("09\\d{9}");

    private static String[] withAll(String[] items) {
        String[] r = new String[items.length + 1];
        r[0] = "All";
        System.arraycopy(items, 0, r, 1, items.length);
        return r;
    }

    public ManageStudentsForm() {
        super("Manage Students");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(UI.header("Manage Students"), BorderLayout.NORTH);
        add(buildFormPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        refresh();
    }

    // ------------------------------------------------------------------ UI
    private JPanel buildFormPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 5),
                BorderFactory.createTitledBorder("Student Information")));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 6, 5, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Student ID (YYYY-NNNN):", "First Name:", "Last Name:", "Email:", "Contact (09XXXXXXXXX):",
                "Gender:", "Course:", "Year Level:", "Status:"};
        JComponent[] fields = {tfId, tfFirst, tfLast, tfEmail, tfContact, cbGender, cbCourse, cbYear, cbStatus};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            p.add(new JLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 1;
            p.add(fields[i], g);
        }

        JButton add = UI.button("Add", UI.SUCCESS);
        JButton update = UI.button("Update", UI.PRIMARY);
        JButton delete = UI.button("Delete", UI.DANGER);
        JButton clear = UI.button("Clear", UI.GRAY);
        JPanel btns = new JPanel(new GridLayout(2, 2, 8, 8));
        btns.add(add); btns.add(update); btns.add(delete); btns.add(clear);
        g.gridx = 0; g.gridy = labels.length; g.gridwidth = 2; g.insets = new Insets(16, 6, 6, 6);
        p.add(btns, g);

        // keep the form panel from stretching
        g.gridy = labels.length + 1; g.weighty = 1;
        p.add(Box.createVerticalGlue(), g);

        add.addActionListener(e -> addStudent());
        update.addActionListener(e -> updateStudent());
        delete.addActionListener(e -> deleteStudent());
        clear.addActionListener(e -> clearForm());
        return p;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        // search row
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row1.add(new JLabel("Search:"));
        row1.add(tfSearch);
        row1.add(new JLabel("by"));
        row1.add(cbSearchBy);
        JButton btnSearch = UI.button("Search", UI.PRIMARY);
        row1.add(btnSearch);

        // filter row
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        row2.add(new JLabel("Filter  Course:"));
        row2.add(fCourse);
        row2.add(new JLabel("Year:"));
        row2.add(fYear);
        row2.add(new JLabel("Status:"));
        row2.add(fStatus);
        JButton btnReset = UI.button("Reset", UI.GRAY);
        row2.add(btnReset);

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setBorder(BorderFactory.createTitledBorder("Search and Filter"));
        top.add(row1);
        top.add(row2);
        p.add(top, BorderLayout.NORTH);

        UI.style(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(lblCount, BorderLayout.SOUTH);

        // events
        btnSearch.addActionListener(e -> refresh());
        btnReset.addActionListener(e -> {
            tfSearch.setText("");
            cbSearchBy.setSelectedIndex(0);
            fCourse.setSelectedIndex(0);
            fYear.setSelectedIndex(0);
            fStatus.setSelectedIndex(0);
            refresh();
        });
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        cbSearchBy.addActionListener(e -> refresh());
        fCourse.addActionListener(e -> refresh());
        fYear.addActionListener(e -> refresh());
        fStatus.addActionListener(e -> refresh());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });
        return p;
    }

    // ------------------------------------------------------------ READ / SEARCH / FILTER
    private void refresh() {
        String q = tfSearch.getText().trim().toLowerCase();
        String by = (String) cbSearchBy.getSelectedItem();
        String course = (String) fCourse.getSelectedItem();
        String year = (String) fYear.getSelectedItem();
        String status = (String) fStatus.getSelectedItem();

        model.setRowCount(0);
        int shown = 0;
        for (Student s : store.getStudents()) {
            if (!q.isEmpty()) {
                boolean match;
                switch (by) {
                    case "ID": match = s.getId().toLowerCase().contains(q); break;
                    case "Name": match = s.getFullName().toLowerCase().contains(q)
                            || (s.getFirstName() + " " + s.getLastName()).toLowerCase().contains(q); break;
                    case "Course": match = s.getCourse().toLowerCase().contains(q); break;
                    default: match = s.getId().toLowerCase().contains(q)
                            || s.getFullName().toLowerCase().contains(q)
                            || s.getCourse().toLowerCase().contains(q)
                            || s.getEmail().toLowerCase().contains(q);
                }
                if (!match) continue;
            }
            if (!"All".equals(course) && !s.getCourse().equals(course)) continue;
            if (!"All".equals(year) && !s.getYearLevel().equals(year)) continue;
            if (!"All".equals(status) && !s.getStatus().equals(status)) continue;
            model.addRow(new Object[]{s.getId(), s.getLastName(), s.getFirstName(), s.getEmail(), s.getContact(),
                    s.getGender(), s.getCourse(), s.getYearLevel(), s.getStatus()});
            shown++;
        }
        lblCount.setText("  Records shown: " + shown + " of " + store.getStudents().size());
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        Student s = store.findStudent((String) model.getValueAt(row, 0));
        if (s == null) return;
        tfId.setText(s.getId());
        tfId.setEnabled(false); // ID cannot be changed when editing
        tfFirst.setText(s.getFirstName());
        tfLast.setText(s.getLastName());
        tfEmail.setText(s.getEmail());
        tfContact.setText(s.getContact());
        cbGender.setSelectedItem(s.getGender());
        cbCourse.setSelectedItem(s.getCourse());
        cbYear.setSelectedItem(s.getYearLevel());
        cbStatus.setSelectedItem(s.getStatus());
    }

    private void clearForm() {
        table.clearSelection();
        tfId.setText("");
        tfId.setEnabled(true);
        tfFirst.setText("");
        tfLast.setText("");
        tfEmail.setText("");
        tfContact.setText("");
        cbGender.setSelectedIndex(0);
        cbCourse.setSelectedIndex(0);
        cbYear.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        tfId.requestFocus();
    }

    // ------------------------------------------------------------ VALIDATION
    /** Validates the form. Returns a Student, or null (after showing an error) if invalid. */
    private Student readForm(boolean isNew) {
        String id = tfId.getText().trim();
        String first = tfFirst.getText().trim();
        String last = tfLast.getText().trim();
        String email = tfEmail.getText().trim();
        String contact = tfContact.getText().trim();

        if (id.isEmpty() || first.isEmpty() || last.isEmpty() || email.isEmpty() || contact.isEmpty()) {
            UI.error(this, "All fields are required. Please fill in every field.");
            return null;
        }
        if (!ID_PATTERN.matcher(id).matches()) {
            UI.error(this, "Invalid Student ID. Use the format YYYY-NNNN (example: 2026-0023).");
            return null;
        }
        if (isNew && store.findStudent(id) != null) {
            UI.error(this, "Duplicate record: Student ID " + id + " already exists.");
            return null;
        }
        if (!NAME_PATTERN.matcher(first).matches() || !NAME_PATTERN.matcher(last).matches()) {
            UI.error(this, "Names must contain letters only (no numbers or commas).");
            return null;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            UI.error(this, "Invalid email address (example: juan@student.edu.ph).");
            return null;
        }
        if (store.emailExists(email, id)) {
            UI.error(this, "Duplicate record: this email is already used by another student.");
            return null;
        }
        if (!CONTACT_PATTERN.matcher(contact).matches()) {
            UI.error(this, "Invalid contact number. Use 11 digits starting with 09 (example: 09171234567).");
            return null;
        }
        return new Student(id, first, last, email, contact, (String) cbGender.getSelectedItem(),
                (String) cbCourse.getSelectedItem(), (String) cbYear.getSelectedItem(),
                (String) cbStatus.getSelectedItem());
    }

    // ------------------------------------------------------------ CREATE / UPDATE / DELETE
    private void addStudent() {
        if (!tfId.isEnabled()) {
            UI.error(this, "A record is selected. Click 'Clear' first to add a new student.");
            return;
        }
        Student s = readForm(true);
        if (s == null) return;
        store.addStudent(s);
        refresh();
        clearForm();
        UI.info(this, "Student added successfully.");
    }

    private void updateStudent() {
        if (table.getSelectedRow() < 0) {
            UI.error(this, "Please select a student from the table to update.");
            return;
        }
        Student s = readForm(false);
        if (s == null) return;
        store.updateStudent(s);
        refresh();
        clearForm();
        UI.info(this, "Student updated successfully.");
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UI.error(this, "Please select a student from the table to delete.");
            return;
        }
        String id = (String) model.getValueAt(row, 0);
        String name = model.getValueAt(row, 2) + " " + model.getValueAt(row, 1);
        int ans = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete " + name + " (" + id + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) return;
        String err = store.deleteStudent(id);
        if (err != null) {
            UI.error(this, err);
            return;
        }
        refresh();
        clearForm();
        UI.info(this, "Student deleted successfully.");
    }
}
