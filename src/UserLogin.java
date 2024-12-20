import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserLogin {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/ParkingLotDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MH907005"; // Replace with your actual password

    public void display() {
        Stage window = new Stage();
        window.setTitle("User Login");

        // Labels and Input Fields
        Label emailLabel = new Label("Email:");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        TextField emailInput = new TextField();
        emailInput.setStyle("-fx-pref-width: 200px; -fx-padding: 5px;");

        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setStyle("-fx-pref-width: 200px; -fx-padding: 5px;");

        Label errorLabel = new Label(); // To display errors if login fails
        errorLabel.setStyle("-fx-text-fill: red;");

        // Button
        Button loginButton = new Button("Login");
        loginButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-padding: 8px 15px; -fx-border-radius: 15px; -fx-background-radius: 15px;"
        );
        loginButton.setOnMouseEntered(e -> loginButton.setStyle(
                "-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-padding: 8px 15px; -fx-border-radius: 15px; -fx-background-radius: 15px;"
        ));
        loginButton.setOnMouseExited(e -> loginButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; " +
                        "-fx-padding: 8px 15px; -fx-border-radius: 15px; -fx-background-radius: 15px;"
        ));

        loginButton.setOnAction(e -> {
            String email = emailInput.getText();
            String password = passwordInput.getText();

            if (validateLogin(email, password)) {
                System.out.println("Login successful for: " + email);
                window.close(); // Close the login window after successful login
            } else {
                errorLabel.setText("Invalid email or password. Please try again.");
            }
        });

        // Layout
        VBox layout = new VBox(10); // 10px spacing between elements
        layout.setStyle("-fx-background-color: #001F3F; -fx-padding: 20px; -fx-alignment: center;");
        layout.getChildren().addAll(emailLabel, emailInput, passwordLabel, passwordInput, loginButton, errorLabel);

        // Scene
        Scene scene = new Scene(layout, 390, 844);
        window.setScene(scene);
        window.show();
    }

    private boolean validateLogin(String email, String password) {
        String query = "SELECT * FROM Users WHERE email = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // If login is successful, open the User Dashboard
                UserDashboard dashboard = new UserDashboard(email);
                dashboard.display();
                return true;
            } else {
                return false;
            }

        } catch (Exception ex) {
            System.out.println("Error during login validation: " + ex.getMessage());
            return false;
        }
    }
}
