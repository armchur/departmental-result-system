package app.ui;

import app.dao.DatabaseInitializer;
import app.dao.SqliteResultDao;
import app.model.Course;
import app.model.ResultEntry;
import app.model.Student;
import app.service.ResultService;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    private ResultService service;
    private final ObservableList<ResultEntry> tableData = FXCollections.observableArrayList();
    private final Label gpaLabel = new Label("GPA: 0.00");

    @Override
    public void start(Stage primaryStage) {
        // Initialize DB and Service
        DatabaseInitializer.initialize();
        service = new ResultService(new SqliteResultDao());

        primaryStage.setTitle("Departmental Result System - CSC 508");

        // Top Controls: Student, Course, Session, Score
        ComboBox<StudentItem> studentBox = new ComboBox<>();
        ComboBox<CourseItem> courseBox = new ComboBox<>();
        TextField sessionField = new TextField("2025/2026");
        sessionField.setPrefWidth(110);
        TextField scoreField = new TextField();
        scoreField.setPromptText("Score (0-100)");
        scoreField.setPrefWidth(100);

        Button saveButton = new Button("Record Score");
        saveButton.setStyle("-fx-background-color: #1e40af; -fx-text-fill: white; -fx-font-weight: bold;");

        // Populate dropdowns from Service
        try {
            for (Student s : service.getStudents()) {
                studentBox.getItems().add(new StudentItem(s.getId(), s.getMatricNo() + " - " + s.getFullName()));
            }
            if (!studentBox.getItems().isEmpty()) studentBox.setValue(studentBox.getItems().get(0));

            for (Course c : service.getCourses()) {
                courseBox.getItems().add(new CourseItem(c.getId(), c.getCode() + " (" + c.getCreditUnit() + " units)"));
            }
            if (!courseBox.getItems().isEmpty()) courseBox.setValue(courseBox.getItems().get(0));
        } catch (Exception e) {
            e.printStackTrace();
        }

        HBox topBox = new HBox(10,
            new Label("Student:"), studentBox,
            new Label("Course:"), courseBox,
            new Label("Score:"), scoreField,
            saveButton
        );
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setPadding(new Insets(15));
        topBox.setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 1 0;");

        // Table
        TableView<ResultEntry> table = new TableView<>(tableData);
        TableColumn<ResultEntry, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        codeCol.setPrefWidth(120);

        TableColumn<ResultEntry, String> titleCol = new TableColumn<>("Course Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("courseTitle"));
        titleCol.setPrefWidth(220);

        TableColumn<ResultEntry, Integer> unitCol = new TableColumn<>("Units");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("creditUnit"));
        unitCol.setPrefWidth(80);

        TableColumn<ResultEntry, Double> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        scoreCol.setPrefWidth(80);

        TableColumn<ResultEntry, String> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
        gradeCol.setPrefWidth(80);

        TableColumn<ResultEntry, Integer> ptCol = new TableColumn<>("Points");
        ptCol.setCellValueFactory(new PropertyValueFactory<>("gradePoint"));
        ptCol.setPrefWidth(80);

        table.getColumns().addAll(codeCol, titleCol, unitCol, scoreCol, gradeCol, ptCol);

        // Bottom Bar with GPA
        gpaLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        HBox bottomBox = new HBox(gpaLabel);
        bottomBox.setAlignment(Pos.CENTER_RIGHT);
        bottomBox.setPadding(new Insets(15));
        bottomBox.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-width: 1 0 0 0;");

        // Refresh Results Handler
        Runnable refreshResults = () -> {
            StudentItem selStudent = studentBox.getValue();
            if (selStudent != null) {
                try {
                    List<ResultEntry> results = service.getStudentResults(selStudent.id(), sessionField.getText().trim());
                    tableData.setAll(results);
                    double gpa = service.calculateGPA(results);
                    gpaLabel.setText(String.format("Calculated GPA: %.2f", gpa));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };

        studentBox.setOnAction(e -> refreshResults.run());

        saveButton.setOnAction(e -> {
            try {
                StudentItem s = studentBox.getValue();
                CourseItem c = courseBox.getValue();
                double score = Double.parseDouble(scoreField.getText().trim());
                service.recordScore(s.id(), c.id(), sessionField.getText().trim(), score);
                scoreField.clear();
                refreshResults.run();
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Please enter a valid numeric score.").showAndWait();
            } catch (IllegalArgumentException ex) {
                new Alert(Alert.AlertType.WARNING, ex.getMessage()).showAndWait();
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Database error: " + ex.getMessage()).showAndWait();
            }
        });

        // Initial Load
        refreshResults.run();

        BorderPane root = new BorderPane();
        root.setTop(topBox);
        root.setCenter(table);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 750, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    record StudentItem(int id, String label) {
        @Override public String toString() { return label; }
    }
    record CourseItem(int id, String label) {
        @Override public String toString() { return label; }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
