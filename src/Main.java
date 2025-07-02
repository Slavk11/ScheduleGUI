import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("✅ Драйвер SQLite загружен");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ Драйвер SQLite НЕ загружен");
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(ScheduleGUI::new);
    }
}