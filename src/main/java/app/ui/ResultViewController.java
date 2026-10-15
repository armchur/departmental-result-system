package app.ui;

import app.model.Course;
import app.model.ResultEntry;
import app.model.Student;
import app.service.ResultService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;

public class ResultViewController {

    @FXML private ComboBox<StudentItem> studentBox;
    @FXML private ComboBox<CourseItem> courseBox;
    @FXML private TextField sessionField;
    @FXML private TextField scoreField;
    @FXML private Button recordButton;

    @FXML private TableView<ResultEntry> resultsTable;
    @FXML private TableColumn<ResultEntry, String> codeCol;
    @FXML private TableColumn<ResultEntry, String> titleCol;
    @FXML private TableColumn<ResultEntry, Integer> unitCol;
    @FXML private TableColumn<ResultEntry, Double> scoreCol;
    @FXML private TableColumn<ResultEntry, String> gradeCol;
    @FXML private TableColumn<ResultEntry, Integer> pointCol;
    @FXML private TableColumn<ResultEntry, Integer> weightedCol;

    @FXML private Label gpaLabel;
    @FXML private Label statusLabel;

    private ResultService service;
    private final ObservableList<ResultEntry> tableData = FXCollections.observableArrayList();

    public record StudentItem(int id, String label) {
        @Override public String toString() { return label; }
    }

    public record CourseItem(int id, String label) {
        @Override public String toString() { return label; }
    }

    @FXML
    public void initialize() {
        // Set up table column bindings
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("courseTitle"));
        unitCol.setCellValueFactory(new PropertyValueFactory<>("creditUnit"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        pointCol.setCellValueFactory(new PropertyValueFactory<>("gradePoint"));
        weightedCol.setCellValueFactory(new PropertyValueFactory<>("weightedPoint"));

        resultsTable.setItems(tableData);

        // Listen for student changes to reload results
        studentBox.setOnAction(e -> refreshResults());
        sessionField.textProperty().addListener((obs, oldVal, newVal) -> refreshResults());
    }

    public void initData(ResultService service) {
        this.service = service;
        loadDropdowns();
        refreshResults();
    }

    private void loadDropdowns() {
        try {
            studentBox.getItems().clear();
            for (Student s : service.getStudents()) {
                studentBox.getItems().add(new StudentItem(s.getId(), s.getMatricNo() + " - " + s.getFullName()));
            }
            if (!studentBox.getItems().isEmpty()) {
                studentBox.setValue(studentBox.getItems().get(0));
            }

            courseBox.getItems().clear();
            for (Course c : service.getCourses()) {
                courseBox.getItems().add(new CourseItem(c.getId(), c.getCode() + " (" + c.getCreditUnit() + " units)"));
            }
            if (!courseBox.getItems().isEmpty()) {
                courseBox.setValue(courseBox.getItems().get(0));
            }
        } catch (SQLException ex) {
            showError("Database Error", "Failed to load dropdown data: " + ex.getMessage());
        }
    }

    private void refreshResults() {
        if (service == null) return;
        StudentItem selectedStudent = studentBox.getValue();
        String session = sessionField.getText() == null ? "" : sessionField.getText().trim();

        if (selectedStudent == null || session.isEmpty()) {
            tableData.clear();
            gpaLabel.setText("Calculated GPA: 0.00");
            return;
        }

        try {
            List<ResultEntry> results = service.getStudentResults(selectedStudent.id(), session);
            tableData.setAll(results);
            double gpa = service.calculateGPA(results);
            gpaLabel.setText(String.format("Calculated GPA: %.2f", gpa));
            statusLabel.setText(String.format("Loaded %d course result(s) for %s (%s)", 
                    results.size(), selectedStudent.label(), session));
        } catch (SQLException ex) {
            showError("Query Error", "Failed to load student results: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRecordScore() {
        StudentItem student = studentBox.getValue();
        CourseItem course = courseBox.getValue();
        String session = sessionField.getText() == null ? "" : sessionField.getText().trim();
        String scoreText = scoreField.getText() == null ? "" : scoreField.getText().trim();

        if (student == null || course == null) {
            showWarning("Input Missing", "Please select both a student and a course.");
            return;
        }

        if (session.isEmpty()) {
            showWarning("Input Missing", "Session cannot be blank.");
            return;
        }

        double score;
        try {
            score = Double.parseDouble(scoreText);
        } catch (NumberFormatException e) {
            showWarning("Invalid Input", "Please enter a valid numeric score (0 - 100).");
            return;
        }

        try {
            service.recordScore(student.id(), course.id(), session, score);
            scoreField.clear();
            refreshResults();
            statusLabel.setText("Result saved successfully.");
        } catch (IllegalArgumentException e) {
            showWarning("Validation Error", e.getMessage());
        } catch (SQLException e) {
            showError("Database Error", "Failed to save score: " + e.getMessage());
        }
    }

    private void showWarning(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
