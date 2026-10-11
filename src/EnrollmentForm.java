import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Form 4: Enrollment (Transaction Processing).
 * Enroll a student in a subject, drop a subject, and see the automatic
 * computation of total units, tuition, miscellaneous fee and total assessment.
 */
public class EnrollmentForm extends JFrame {
    private final DataStore store = DataStore.get();

    private final JComboBox<String> cbStudent = new JComboBox<>();
    private final JComboBox<String> cbSubject = new JComboBox<>();
    private final JLabel lblSubjectInfo = new JLabel(" ");
    private final JLabel lblUnits = new JLabel("0");
    private final JLabel lblTuition = new JLabel(UI.peso(0));
    private final JLabel lblMisc = new JLabel(UI.peso(0));
    private final JLabel lblTotal = new JLabel(UI.peso(0));

    private final JTextField tfSearch = new JTextField(14);
    private final JComboBox<String> cbStatusFilter = new JComboBox<>(new String[]{"All", "Enrolled", "Dropped"});
    private final JCheckBox chkSelectedOnly = new JCheckBox("Selected student only");
    private final JLabel lblCount = new JLabel();

    private final DefaultTableModel model = UI.model("Enroll ID", "Student ID", "Student Name", "Subject",
            "Title", "Units", "Date", "Status");
    private final JTable table = new JTable(model);
    private boolean loading = false;

    public EnrollmentForm() {
        super("Enrollment - Transaction Processing");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1200, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(UI.header("Enrollment (Transaction Processing)"), BorderLayout.NORTH);
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildTablePanel(), BorderLayout.CENTER);
        reloadAll();
    }

    // ------------------------------------------------------------------ UI
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 5));

        // --- new enrollment ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("New Enrollment"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.weightx = 0; form.add(new JLabel("Student:"), g);
        g.gridx = 1; g.weightx = 1; form.add(cbStudent, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0; form.add(new JLabel("Subject:"), g);
        g.gridx = 1; g.weightx = 1; form.add(cbSubject, g);
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2; form.add(lblSubjectInfo, g);
        JButton btnEnroll = UI.button("Enroll Student", UI.SUCCESS);
        g.gridy = 3; form.add(btnEnroll, g);
        cbStudent.setPrototypeDisplayValue("2026-0000 - Aaaaaaaaaaaa, Aaaaaaaaaa");
        cbSubject.setPrototypeDisplayValue("MATH101 - Aaaaaaaaaaaaaaaaaaaaaaaaaaa");
        left.add(form);
        left.add(Box.createVerticalStrut(12));

        // --- assessment (automatic calculation) ---
        JPanel assess = new JPanel(new GridBagLayout());
        assess.setBorder(BorderFactory.createTitledBorder("Assessment (auto-computed)"));
        GridBagConstraints a = new GridBagConstraints();
        a.insets = new Insets(5, 6, 5, 6);
        a.anchor = GridBagConstraints.WEST;
        String[] names = {"Total Units:", "Tuition (" + UI.peso(DataStore.RATE_PER_UNIT) + "/unit):",
                "Miscellaneous Fee:", "TOTAL FEE:"};
        JLabel[] vals = {lblUnits, lblTuition, lblMisc, lblTotal};
        for (int i = 0; i < names.length; i++) {
            a.gridx = 0; a.gridy = i; a.weightx = 1;
            assess.add(new JLabel(names[i]), a);
            a.gridx = 1; a.weightx = 0; a.anchor = GridBagConstraints.EAST;
            assess.add(vals[i], a);
            a.anchor = GridBagConstraints.WEST;
        }
        lblTotal.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblTotal.setForeground(UI.SUCCESS);
        lblUnits.setFont(UI.BOLD);
        left.add(assess);
        left.add(Box.createVerticalGlue());

        JLabel rule = new JLabel("<html><i>Rules: only Active students; no duplicate subject;<br>"
                + "subject must have free slots; max " + DataStore.MAX_UNITS + " units.</i></html>");
        rule.setForeground(Color.GRAY);
        left.add(rule);

        btnEnroll.addActionListener(e -> doEnroll());
        cbStudent.addActionListener(e -> {
            if (loading) return;
            updateAssessment();
            refreshTable();
        });
        cbSubject.addActionListener(e -> {
            if (!loading) updateSubjectInfo();
        });
        left.setPreferredSize(new Dimension(380, 100));
        return left;
    }

    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        top.setBorder(BorderFactory.createTitledBorder("Search and Filter Enrollments"));
        top.add(new JLabel("Search (student / subject):"));
        top.add(tfSearch);
        top.add(new JLabel("Status:"));
        top.add(cbStatusFilter);
        top.add(chkSelectedOnly);
        p.add(top, BorderLayout.NORTH);

        UI.style(table);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JButton btnDrop = UI.button("Drop Selected Enrollment", UI.DANGER);
        JPanel bp = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bp.add(btnDrop);
        bottom.add(bp, BorderLayout.WEST);
        bottom.add(lblCount, BorderLayout.EAST);
        p.add(bottom, BorderLayout.SOUTH);

        btnDrop.addActionListener(e -> doDrop());
        tfSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });
        cbStatusFilter.addActionListener(e -> refreshTable());
        chkSelectedOnly.addActionListener(e -> refreshTable());
        return p;
    }

    // ------------------------------------------------------------------ DATA LOADING
    /** Reload combo boxes, assessment and table from the data store. */
    public void reloadAll() {
        loading = true;
        String oldStudent = selectedStudentId();
        String oldSubject = selectedSubjectCode();

        cbStudent.removeAllItems();
        for (Student s : store.getStudents()) {
            if ("Active".equals(s.getStatus())) cbStudent.addItem(s.getId() + " - " + s.getFullName());
        }
        selectById(cbStudent, oldStudent);

        cbSubject.removeAllItems();
        for (Subject s : store.getSubjects()) cbSubject.addItem(s.getCode() + " - " + s.getTitle());
        selectById(cbSubject, oldSubject);
        loading = false;

        updateSubjectInfo();
        updateAssessment();
        refreshTable();
    }

    private void selectById(JComboBox<String> cb, String id) {
        if (id == null) return;
        for (int i = 0; i < cb.getItemCount(); i++) {
            if (cb.getItemAt(i).startsWith(id + " - ")) { cb.setSelectedIndex(i); return; }
        }
    }

    private String selectedStudentId() {
        String item = (String) cbStudent.getSelectedItem();
        return item == null ? null : item.split(" - ")[0];
    }

    private String selectedSubjectCode() {
        String item = (String) cbSubject.getSelectedItem();
        return item == null ? null : item.split(" - ")[0];
    }

    private void updateSubjectInfo() {
        Subject s = store.findSubject(selectedSubjectCode());
        if (s == null) { lblSubjectInfo.setText(" "); return; }
        int taken = store.countEnrolled(s.getCode());
        lblSubjectInfo.setText("Units: " + s.getUnits() + "   |   Slots: " + taken + "/" + s.getCapacity()
                + "   |   Available: " + (s.getCapacity() - taken));
    }

    /** AUTOMATIC CALCULATION of units and fees. */
    private void updateAssessment() {
        String id = selectedStudentId();
        int units = id == null ? 0 : store.totalUnits(id);
        lblUnits.setText(units + " / " + DataStore.MAX_UNITS);
        lblTuition.setText(UI.peso(DataStore.tuitionFor(units)));
        lblMisc.setText(UI.peso(DataStore.miscFor(units)));
        lblTotal.setText(UI.peso(DataStore.totalFeeFor(units)));
    }

    private void refreshTable() {
        String q = tfSearch.getText().trim().toLowerCase();
        String status = (String) cbStatusFilter.getSelectedItem();
        String only = chkSelectedOnly.isSelected() ? selectedStudentId() : null;

        model.setRowCount(0);
        int shown = 0;
        for (Enrollment e : store.getEnrollments()) {
            Student st = store.findStudent(e.getStudentId());
            Subject sub = store.findSubject(e.getSubjectCode());
            String stName = st == null ? "(deleted)" : st.getFullName();
            String title = sub == null ? "" : sub.getTitle();
            if (only != null && !e.getStudentId().equals(only)) continue;
            if (!"All".equals(status) && !e.getStatus().equals(status)) continue;
            if (!q.isEmpty()) {
                String hay = (e.getStudentId() + " " + stName + " " + e.getSubjectCode() + " " + title).toLowerCase();
                if (!hay.contains(q)) continue;
            }
            model.addRow(new Object[]{e.getEnrollmentId(), e.getStudentId(), stName, e.getSubjectCode(), title,
                    sub == null ? 0 : sub.getUnits(), e.getDate(), e.getStatus()});
            shown++;
        }
        lblCount.setText("Records shown: " + shown + "   ");
    }

    // ------------------------------------------------------------------ TRANSACTIONS
    private void doEnroll() {
        String sid = selectedStudentId();
        String code = selectedSubjectCode();
        if (sid == null) { UI.error(this, "No active student available to enroll."); return; }
        if (code == null) { UI.error(this, "Please select a subject."); return; }
        String err = store.enroll(sid, code);
        if (err != null) {
            UI.error(this, err);
            return;
        }
        updateSubjectInfo();
        updateAssessment();
        refreshTable();
        UI.info(this, "Enrollment successful!\n" + sid + " is now enrolled in " + code + ".");
    }

    private void doDrop() {
        int row = table.getSelectedRow();
        if (row < 0) { UI.error(this, "Please select an enrollment record to drop."); return; }
        String eid = (String) model.getValueAt(row, 0);
        String subj = (String) model.getValueAt(row, 3);
        int ans = JOptionPane.showConfirmDialog(this,
                "Drop " + subj + " for " + model.getValueAt(row, 2) + "?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ans != JOptionPane.YES_OPTION) return;
        String err = store.drop(eid);
        if (err != null) { UI.error(this, err); return; }
        updateSubjectInfo();
        updateAssessment();
        refreshTable();
        UI.info(this, "Subject dropped. Assessment has been recomputed.");
    }
}
