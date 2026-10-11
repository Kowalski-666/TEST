import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;

/** Small helper class so all forms share the same look. */
public class UI {
    public static final Color PRIMARY = new Color(30, 64, 124);
    public static final Color SUCCESS = new Color(34, 139, 84);
    public static final Color WARNING = new Color(214, 137, 16);
    public static final Color DANGER = new Color(192, 57, 43);
    public static final Color GRAY = new Color(96, 108, 122);
    public static final Color BG = new Color(243, 246, 251);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 13);

    public static JButton button(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(BOLD);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(8, 14, 8, 14));
        return b;
    }

    public static JPanel header(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        p.setBackground(PRIMARY);
        JLabel l = new JLabel(title);
        l.setFont(TITLE);
        l.setForeground(Color.WHITE);
        p.add(l);
        return p;
    }

    public static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static void style(JTable t) {
        t.setRowHeight(26);
        t.setFont(new Font("SansSerif", Font.PLAIN, 13));
        t.setSelectionBackground(new Color(173, 204, 245));
        t.setSelectionForeground(Color.BLACK);
        t.setGridColor(new Color(225, 228, 234));
        t.setFillsViewportHeight(true);
        t.getTableHeader().setReorderingAllowed(false);
        t.getTableHeader().setBackground(PRIMARY);
        t.getTableHeader().setForeground(Color.WHITE);
        t.getTableHeader().setFont(BOLD);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    public static String peso(double v) { return String.format("PHP %,.2f", v); }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
}
