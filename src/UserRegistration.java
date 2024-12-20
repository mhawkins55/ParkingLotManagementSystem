import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UserRegistration {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/ParkingLotDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MH907005"; // Replace with your actual password

    public void display() {
        Stage window = new Stage();
        window.setTitle("User Registration");

        // Labels and TextFields
        Label nameLabel = new Label("Name:");
        TextField nameInput = new TextField();

        Label emailLabel = new Label("Email:");
        TextField emailInput = new TextField();

        Label passwordLabel = new Label("Password:");
        TextField passwordInput = new TextField();

        // Button
        Button registerButton = new Button("Register");
        registerButton.setOnAction(e -> {
            String name = nameInput.getText();
            String email = emailInput.getText();
            String password = passwordInput.getText();

            if (saveUserToDatabase(name, email, password)) {
                System.out.println("User registered successfully: " + name);
                window.close();
            } else {
                System.out.println("Failed to register user.");
            }
        });

        // Layout
        VBox layout = new VBox(10); // 10px spacing between elements
        layout.getChildren().addAll(nameLabel, nameInput, emailLabel, emailInput, passwordLabel, passwordInput, registerButton);

        // Scene
        Scene scene = new Scene(layout, 300, 250);
        window.setScene(scene);
        window.show();
    }

    private boolean saveUserToDatabase(String name, String email, String password) {
        String insertQuery = "INSERT INTO Users (name, email, password) VALUES (?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
            return true;

        } catch (Exception ex) {
            System.out.println("Error saving user to database: " + ex.getMessage());
            return false;
        }
    }
}
