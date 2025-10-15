import java.sql.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StudentGradeTracker extends JFrame {
    // ---------- Data Section ----------
    private ArrayList<Student> students = new ArrayList<>();
    private Connection conn;

    // ---------- GUI Section ----------
    private JTextField nameField, rollField, scoreField;
    private JTextArea reportArea;

    // ---------- Constructor ----------
    public StudentGradeTracker() {
        // Setup Window
        setTitle("Student Grade Tracker");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel (Inputs)
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Roll No:"));
        rollField = new JTextField();
        inputPanel.add(rollField);

        inputPanel.add(new JLabel("Score:"));
        scoreField = new JTextField();
        inputPanel.add(scoreField);

        JButton addButton = new JButton("Add Student");
        JButton saveButton = new JButton("Save to DB");
        JButton reportButton = new JButton("Show Report");

        inputPanel.add(addButton);
        inputPanel.add(saveButton);

        add(inputPanel, BorderLayout.NORTH);

        // Center Panel (Report)
        reportArea = new JTextArea();
        add(new JScrollPane(reportArea), BorderLayout.CENTER);

        // Bottom Button
        add(reportButton, BorderLayout.SOUTH);

        // Action Listeners
        addButton.addActionListener(e -> addStudent());
        saveButton.addActionListener(e -> saveStudentsToDB());
        reportButton.addActionListener(e -> showReport());

        // Connect to DB
        connectDB();
    }

    // ---------- Student Class ----------
    class Student {
        private String name;
        private int rollNo;
        private double score;

        public Student(String name, int rollNo, double score) {
            this.name = name;
            this.rollNo = rollNo;
            this.score = score;
        }

        public String getName() { return name; }
        public int getRollNo() { return rollNo; }
        public double getScore() { return score; }

        public String toString() {
            return "Name: " + name + ", Roll No: " + rollNo + ", Score: " + score;
        }
    }

    // ---------- Core Functions ----------
    private void addStudent() {
        try {
            String name = nameField.getText();
            int roll = Integer.parseInt(rollField.getText());
            double score = Double.parseDouble(scoreField.getText());
            students.add(new Student(name, roll, score));
            JOptionPane.showMessageDialog(this, "Student Added!");
            nameField.setText(""); rollField.setText(""); scoreField.setText("");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Input!");
        }
    }

    private void showReport() {
        if (students.isEmpty()) {
            reportArea.setText("No students yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        double total = 0, highest = students.get(0).getScore(), lowest = students.get(0).getScore();

        for (Student s : students) {
            sb.append(s.toString()).append("\n");
            total += s.getScore();
            if (s.getScore() > highest) highest = s.getScore();
            if (s.getScore() < lowest) lowest = s.getScore();
        }

        double avg = total / students.size();
        sb.append("\nAverage Score: ").append(avg);
        sb.append("\nHighest Score: ").append(highest);
        sb.append("\nLowest Score: ").append(lowest);

        reportArea.setText(sb.toString());
    }

   private void connectDB() {
    try {
        // Load MySQL JDBC driver
        Class.forName("com.mysql.cj.jdbc.Driver");

        // Establish connection
        conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/studentdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
            "root",               // 🔹 your MySQL username
            "password"    // 🔹 your MySQL password
        );

        System.out.println("✅ Connected to Database Successfully");

        // Wrap getMetaData() inside its own try-catch
        try {
            System.out.println("Connected as: " + conn.getMetaData().getUserName());
        } catch (SQLException se) {
            System.out.println("Unable to fetch user info: " + se.getMessage());
        }

    } catch (ClassNotFoundException e) {
        System.out.println("❌ JDBC Driver not found!");
        e.printStackTrace();
    } catch (SQLException e) {
        System.out.println("❌ Database Connection Failed!");
        e.printStackTrace();
    }
}


    private void saveStudentsToDB() {
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Database not connected!");
            return;
        }

        try {
            String sql = "INSERT INTO students(name, rollNo, score) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            for (Student s : students) {
                stmt.setString(1, s.getName());
                stmt.setInt(2, s.getRollNo());
                stmt.setDouble(3, s.getScore());
                stmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "All Students Saved to DB!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------- Main ----------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new StudentGradeTracker().setVisible(true));
    }
}
