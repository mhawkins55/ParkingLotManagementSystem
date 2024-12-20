import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Parking Lot Management System");

        // Buttons for the main menu
        Button btnUserRegistration = createStyledButton("User Registration");
        Button btnUserLogin = createStyledButton("User Login");
        Button btnAdminLogin = createStyledButton("Admin Login");
        Button btnExit = createStyledButton("Exit");

        // Set button actions
        btnUserRegistration.setOnAction(e -> openUserRegistration());
        btnUserLogin.setOnAction(e -> openUserLogin());
        btnAdminLogin.setOnAction(e -> openAdminLogin(primaryStage)); // Updated for Admin Login
        btnExit.setOnAction(e -> primaryStage.close());

        // Layout
        VBox layout = new VBox(20); // 20px spacing between buttons
        layout.setStyle("-fx-background-color: #1E3A8A; -fx-padding: 20px; -fx-alignment: center;"); // Styling for deep blue background
        layout.getChildren().addAll(btnUserRegistration, btnUserLogin, btnAdminLogin, btnExit);

        // Scene
        Scene scene = new Scene(layout, 390, 844); // iPhone-like dimensions
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setStyle(
                "-fx-background-color: #2563EB;" + // Secondary blue color
                        "-fx-text-fill: white;" +          // White text
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-radius: 30;" +        // Rounded corners
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 5, 0.3, 2, 2);"
        );

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #3B82F6;" + // Lighter blue on hover
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-radius: 30;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.3), 5, 0.3, 2, 2);"
        ));

        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #2563EB;" + // Revert to default style
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-radius: 30;" +
                        "-fx-background-radius: 30;" +
                        "-fx-padding: 10 20;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 5, 0.3, 2, 2);"
        ));

        return button;
    }

    private void openUserRegistration() {
        UserRegistration userRegistration = new UserRegistration();
        userRegistration.display();
    }

    private void openUserLogin() {
        UserLogin userLogin = new UserLogin();
        userLogin.display();
    }

    private void openAdminLogin(Stage primaryStage) {
        Stage adminLoginStage = new Stage();
        adminLoginStage.setTitle("Admin Login");

        // Admin login fields
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        Label passwordLabel = new Label("Password:");
        TextField passwordField = new TextField();
        Button loginButton = createStyledButton("Login");
        Label feedbackLabel = new Label();

        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if ("admin".equalsIgnoreCase(username) && "password".equals(password)) {
                feedbackLabel.setText("Login successful!");
                adminLoginStage.close();
                openAdminDashboard(); // Navigate to Admin Dashboard
            } else {
                feedbackLabel.setText("Invalid credentials. Please try again.");
            }
        });

        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding: 20px; -fx-alignment: center;"); // Center alignment
        layout.getChildren().addAll(
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                loginButton, feedbackLabel
        );

        Scene scene = new Scene(layout, 390, 844); // Adjust dimensions
        adminLoginStage.setScene(scene);
        adminLoginStage.show();
    }

    private void openAdminDashboard() {
        AdminDashboard adminDashboard = new AdminDashboard();
        adminDashboard.display();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
