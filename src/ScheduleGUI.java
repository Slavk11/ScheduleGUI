import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class ScheduleGUI extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JComboBox<String> dayBox;
    private JTextField subjectField;
    private JTextField timeField;
    private Connection conn;

    public ScheduleGUI() {
        setTitle("Расписание занятий с БД");
        setSize(700, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] days = {"Понедельник", "Вторник", "Среда", "Четверг", "Пятница"};
        dayBox = new JComboBox<>(days);
        add(dayBox, BorderLayout.NORTH);

        model = new DefaultTableModel(new Object[]{"ID", "Время", "Предмет"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false; // Запрещаем редактирование
            }
        };

        table = new JTable(model);
        table.removeColumn(table.getColumnModel().getColumn(0)); // Скрыть ID
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        timeField = new JTextField(5);
        subjectField = new JTextField(10);
        JButton addButton = new JButton("Добавить");
        JButton deleteButton = new JButton("Удалить");

        inputPanel.add(new JLabel("Время:"));
        inputPanel.add(timeField);
        inputPanel.add(new JLabel("Предмет:"));
        inputPanel.add(subjectField);
        inputPanel.add(addButton);
        inputPanel.add(deleteButton);
        add(inputPanel, BorderLayout.SOUTH);

        // Обработчики
        dayBox.addActionListener(e -> loadSchedule());

        addButton.addActionListener(e -> {
            String time = timeField.getText().trim();
            String subject = subjectField.getText().trim();
            String day = (String) dayBox.getSelectedItem();
            if (!time.isEmpty() && !subject.isEmpty()) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO schedule (day, time, subject) VALUES (?, ?, ?)")) {
                    stmt.setString(1, day);
                    stmt.setString(2, time);
                    stmt.setString(3, subject);
                    stmt.executeUpdate();
                    loadSchedule();
                    timeField.setText("");
                    subjectField.setText("");
                } catch (SQLException ex) {
                    showError(ex);
                }
            }
        });

        deleteButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int modelRow = table.convertRowIndexToModel(row);
                int id = Integer.parseInt(model.getValueAt(modelRow, 0).toString());
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM schedule WHERE id = ?")) {
                    stmt.setInt(1, id);
                    stmt.executeUpdate();
                    loadSchedule();
                } catch (SQLException ex) {
                    showError(ex);
                }
            }
        });

        initDB();
        loadSchedule();
        setVisible(true);
    }

    private void initDB() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:schedule.db");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS schedule (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "day TEXT NOT NULL," +
                    "time TEXT NOT NULL," +
                    "subject TEXT NOT NULL)");
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void loadSchedule() {
        model.setRowCount(0);
        String day = (String) dayBox.getSelectedItem();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, time, subject FROM schedule WHERE day = ? ORDER BY time")) {
            stmt.setString(1, day);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("time"),
                        rs.getString("subject")
                });
            }
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void showError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Ошибка: " + ex.getMessage());
    }
}