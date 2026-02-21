package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.example.db.Db;
import org.example.service.AuthService;
import org.example.service.StudyService;
import org.example.model.LearningProfile;
import org.example.dao.PlanDao;
import org.example.model.StudyPlan;
import java.util.List;

public class App {

    private static AuthService authService = new AuthService();
    private static StudyService studyService = new StudyService();
    private static PlanDao planDao = new PlanDao();
    private static long currentUserId;

    public static void main(String[] args) {
        Db.init();
        SwingUtilities.invokeLater(App::showLogin);
    }

    private static void showLogin() {
        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(350, 200);
        frame.setLayout(new GridLayout(4,1,5,5));

        JTextField emailField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton loginBtn = new JButton("Login");
        JButton registerBtn = new JButton("Register");

        frame.add(new JLabel("Email:"));
        frame.add(emailField);
        frame.add(new JLabel("Password:"));
        frame.add(passField);
        JPanel btnPanel = new JPanel();
        btnPanel.add(loginBtn);
        btnPanel.add(registerBtn);
        frame.add(btnPanel);

        loginBtn.addActionListener(e -> {
            try {
                var user = authService.login(emailField.getText(), new String(passField.getPassword()));
                if (user != null) {
                    currentUserId = user.id();
                    frame.dispose();
                    showDashboard();
                } else {
                    JOptionPane.showMessageDialog(frame, "Login failed");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        registerBtn.addActionListener(e -> {
            try {
                var user = authService.register(emailField.getText(), new String(passField.getPassword()));
                JOptionPane.showMessageDialog(frame, "Registered! Please login.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void showDashboard() {
        JFrame frame = new JFrame("Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800,600);
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("My Study Plans", createPlansPanel());
        tabs.addTab("Generate Study Plan", createGeneratePlanPanel());
        tabs.addTab("Revision Plan", createRevisionPlanPanel());
        tabs.addTab("Quiz Generator", createQuizPanel());

        frame.add(tabs);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JPanel createPlansPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        DefaultListModel<StudyPlan> listModel = new DefaultListModel<>();
        JList<StudyPlan> plansList = new JList<>(listModel);
        plansList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plansList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                StudyPlan p = (StudyPlan) value;
                String text = p.planType() + " - " + p.content();
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        panel.add(new JScrollPane(plansList), BorderLayout.CENTER);

        JPanel editPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        JTextField typeField = new JTextField();
        JTextArea contentArea = new JTextArea(3, 20);
        JButton addBtn = new JButton("Add Plan");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");

        editPanel.add(typeField);
        editPanel.add(contentArea);
        JPanel btnPanel = new JPanel();
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(deleteBtn);
        editPanel.add(btnPanel);

        panel.add(editPanel, BorderLayout.SOUTH);

        Runnable refresh = () -> {
            listModel.clear();
            List<StudyPlan> plans = planDao.list(currentUserId);
            for (StudyPlan p : plans) listModel.addElement(p);
        };
        refresh.run();

        addBtn.addActionListener(e -> {
            try {
                planDao.insert(currentUserId, typeField.getText(), contentArea.getText());
                refresh.run();
                typeField.setText("");
                contentArea.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        editBtn.addActionListener(e -> {
            StudyPlan selected = plansList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "Select a plan to edit.");
                return;
            }
            try {
                planDao.update(selected.id(), currentUserId, typeField.getText(), contentArea.getText());
                refresh.run();
                typeField.setText("");
                contentArea.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        deleteBtn.addActionListener(e -> {
            StudyPlan selected = plansList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(panel, "Select a plan to delete.");
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(panel,
                    "Are you sure you want to delete this plan?",
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    planDao.delete(selected.id(), currentUserId);
                    refresh.run();
                    typeField.setText("");
                    contentArea.setText("");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
                }
            }
        });

        // Pre-fill fields when selecting a plan
        plansList.addListSelectionListener(e -> {
            StudyPlan selected = plansList.getSelectedValue();
            if (selected != null) {
                typeField.setText(selected.planType());
                contentArea.setText(selected.content());
            }
        });

        return panel;
    }


    private static JPanel createGeneratePlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea output = new JTextArea();
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5,2));
        JTextField subjectField = new JTextField();
        JTextField levelField = new JTextField();
        JTextField goalField = new JTextField();
        JTextField minutesField = new JTextField();
        JButton generateBtn = new JButton("Generate Plan");

        inputPanel.add(new JLabel("Subject:")); inputPanel.add(subjectField);
        inputPanel.add(new JLabel("Level:")); inputPanel.add(levelField);
        inputPanel.add(new JLabel("Goal:")); inputPanel.add(goalField);
        inputPanel.add(new JLabel("Daily Minutes:")); inputPanel.add(minutesField);
        inputPanel.add(generateBtn);
        panel.add(inputPanel, BorderLayout.NORTH);

        generateBtn.addActionListener(e -> {
            try {
                LearningProfile p = new LearningProfile(currentUserId,
                        subjectField.getText(),
                        levelField.getText(),
                        goalField.getText(),
                        Integer.parseInt(minutesField.getText()),
                        null);
                String result = studyService.generateStudyPlan(p);
                output.setText(result);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private static JPanel createRevisionPlanPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea output = new JTextArea();
        output.setLineWrap(true); output.setWrapStyleWord(true);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(3,2));
        JTextField subjectField = new JTextField();
        JTextField daysField = new JTextField();
        JButton generateBtn = new JButton("Generate Revision");

        inputPanel.add(new JLabel("Subject:")); inputPanel.add(subjectField);
        inputPanel.add(new JLabel("Days until Exam:")); inputPanel.add(daysField);
        inputPanel.add(generateBtn);
        panel.add(inputPanel, BorderLayout.NORTH);

        generateBtn.addActionListener(e -> {
            try {
                String result = studyService.generateRevisionPlan(subjectField.getText(), Integer.parseInt(daysField.getText()));
                output.setText(result);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }

    private static JPanel createQuizPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea output = new JTextArea();
        output.setLineWrap(true); output.setWrapStyleWord(true);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(4,2));
        JTextField subjectField = new JTextField();
        JTextField diffField = new JTextField();
        JTextField questionsField = new JTextField();
        JButton generateBtn = new JButton("Generate Quiz");

        inputPanel.add(new JLabel("Subject:")); inputPanel.add(subjectField);
        inputPanel.add(new JLabel("Difficulty:")); inputPanel.add(diffField);
        inputPanel.add(new JLabel("Number of Questions:")); inputPanel.add(questionsField);
        inputPanel.add(generateBtn);
        panel.add(inputPanel, BorderLayout.NORTH);

        generateBtn.addActionListener(e -> {
            try {
                String result = studyService.generateQuiz(subjectField.getText(),
                        diffField.getText(),
                        Integer.parseInt(questionsField.getText()));
                output.setText(result);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error: " + ex.getMessage());
            }
        });

        return panel;
    }
}
