import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDashboard {

    private String userEmail; // Logged-in user's email

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ParkingLotDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MH907005"; // Replace with your actual password

    public UserDashboard(String userEmail) {
        this.userEmail = userEmail;
    }

    public void display() {
        Stage window = new Stage();
        window.setTitle("User Dashboard");

        // Welcome message
        Label welcomeLabel = new Label("Welcome, " + userEmail);
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");

        // Buttons for dashboard actions
        Button btnViewSpots = new Button("View Available Parking Spots");
        Button btnMakeReservation = new Button("Make a Reservation");
        Button btnFinishReservation = new Button("Finish and Pay for Reservation");
        Button btnLogout = new Button("Log Out");

        // Apply styles to buttons
        styleButton(btnViewSpots);
        styleButton(btnMakeReservation);
        styleButton(btnFinishReservation);
        styleButton(btnLogout);

        // Set button actions
        btnViewSpots.setOnAction(e -> viewAvailableParkingSpots());
        btnMakeReservation.setOnAction(e -> makeReservation());
        btnFinishReservation.setOnAction(e -> finishReservation());
        btnLogout.setOnAction(e -> {
            System.out.println("Logging out...");
            window.close();
        });

        // Layout
        VBox layout = new VBox(10); // 10px spacing between elements
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #001f3f; -fx-padding: 20;");
        layout.getChildren().addAll(welcomeLabel, btnViewSpots, btnMakeReservation, btnFinishReservation, btnLogout);

        // Scene
        Scene scene = new Scene(layout, 390, 844);
        window.setScene(scene);
        window.show();
    }

    private void viewAvailableParkingSpots() {
        Stage spotWindow = new Stage();
        spotWindow.setTitle("Available Parking Spots");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #001f3f; -fx-padding: 20;");

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT spot_number FROM ParkingSpots WHERE is_reserved = 0");
             ResultSet rs = stmt.executeQuery()) {

            Label header = new Label("Available Parking Spots:");
            header.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
            layout.getChildren().add(header);

            boolean hasSpots = false;
            while (rs.next()) {
                hasSpots = true;
                int spotNumber = rs.getInt("spot_number");
                Label spotLabel = new Label("Spot Number: " + spotNumber);
                spotLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                layout.getChildren().add(spotLabel);
            }

            if (!hasSpots) {
                Label noSpotsLabel = new Label("No available spots at the moment.");
                noSpotsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                layout.getChildren().add(noSpotsLabel);
            }

        } catch (Exception ex) {
            Label errorLabel = new Label("Error fetching parking spots: " + ex.getMessage());
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
            layout.getChildren().add(errorLabel);
        }

        Scene scene = new Scene(layout, 390, 844);
        spotWindow.setScene(scene);
        spotWindow.show();
    }

    private void makeReservation() {
        Stage reservationWindow = new Stage();
        reservationWindow.setTitle("Make a Reservation");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #001f3f; -fx-padding: 20;");

        // Header
        Label header = new Label("Available Parking Spots:");
        header.setStyle("-fx-font-size: 18px; -fx-text-fill: white;");
        layout.getChildren().add(header);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT spot_number FROM ParkingSpots WHERE is_reserved = 0");
             ResultSet rs = stmt.executeQuery()) {

            boolean hasSpots = false;
            while (rs.next()) {
                hasSpots = true;
                int spotNumber = rs.getInt("spot_number");
                Label spotLabel = new Label("Spot Number: " + spotNumber);
                spotLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                layout.getChildren().add(spotLabel);
            }

            if (!hasSpots) {
                Label noSpotsLabel = new Label("No available spots at the moment.");
                noSpotsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                layout.getChildren().add(noSpotsLabel);
            }

        } catch (Exception ex) {
            Label errorLabel = new Label("Error fetching parking spots: " + ex.getMessage());
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
            layout.getChildren().add(errorLabel);
        }

        // Input field for Spot Number
        Label spotLabel = new Label("Enter Spot Number to Reserve:");
        spotLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        TextField spotInput = new TextField();

        // Input field for Start Time
        Label timeLabel = new Label("Enter Start Time (HH:mm):");
        timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        TextField timeInput = new TextField();

        // AM/PM Selection
        ToggleGroup amPmGroup = new ToggleGroup();
        RadioButton amButton = new RadioButton("AM");
        RadioButton pmButton = new RadioButton("PM");
        amButton.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        pmButton.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
        amButton.setToggleGroup(amPmGroup);
        pmButton.setToggleGroup(amPmGroup);
        amButton.setSelected(true);

        // Button to reserve
        Button reserveButton = new Button("Reserve");
        styleButton(reserveButton);
        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        reserveButton.setOnAction(e -> {
            String spotNumber = spotInput.getText();
            String inputTime = timeInput.getText();
            RadioButton selectedAmPm = (RadioButton) amPmGroup.getSelectedToggle();

            if (spotNumber.isEmpty() || inputTime.isEmpty()) {
                feedbackLabel.setText("Please fill in all fields.");
                return;
            }

            try {
                String currentDate = java.time.LocalDate.now().toString();
                String fullTime = inputTime + " " + selectedAmPm.getText();

                java.time.format.DateTimeFormatter inputFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a");
                java.time.LocalTime parsedTime = java.time.LocalTime.parse(fullTime, inputFormatter);
                String startTime = currentDate + " " + parsedTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));

                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                     PreparedStatement reserveStmt = conn.prepareStatement(
                             "UPDATE ParkingSpots SET is_reserved = 1 WHERE spot_number = ? AND is_reserved = 0");
                     PreparedStatement insertReservationStmt = conn.prepareStatement(
                             "INSERT INTO Reservations (user_id, spot_id, start_time, is_reserved) " +
                                     "VALUES ((SELECT user_id FROM Users WHERE email = ?), " +
                                     "(SELECT spot_id FROM ParkingSpots WHERE spot_number = ?), ?, 1)")) {

                    reserveStmt.setString(1, spotNumber);
                    int rowsUpdated = reserveStmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        insertReservationStmt.setString(1, userEmail);
                        insertReservationStmt.setString(2, spotNumber);
                        insertReservationStmt.setString(3, startTime);
                        insertReservationStmt.executeUpdate();

                        feedbackLabel.setText("Reservation successful for Spot Number: " + spotNumber);
                    } else {
                        feedbackLabel.setText("Error: Spot is already reserved or does not exist.");
                    }
                }
            } catch (Exception ex) {
                feedbackLabel.setText("Error making reservation: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(
                spotLabel, spotInput, timeLabel, timeInput, amButton, pmButton, reserveButton, feedbackLabel
        );

        Scene scene = new Scene(layout, 390, 844);
        reservationWindow.setScene(scene);
        reservationWindow.show();
    }

    private void styleButton(Button button) {
        button.setStyle("-fx-font-size: 16px; -fx-background-color: #0074D9; -fx-text-fill: white; -fx-padding: 10; -fx-background-radius: 20;");
    }

    private void finishReservation() {
        Stage finishWindow = new Stage();
        finishWindow.setTitle("Finish Reservation and Pay");

        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #001f3f; -fx-padding: 20;");

        Label feedbackLabel = new Label();
        feedbackLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        ListView<String> reservationList = new ListView<>();
        Button payButton = new Button("Pay and Release Spot");
        styleButton(payButton);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement fetchReservationsStmt = conn.prepareStatement(
                     "SELECT r.reservation_id, p.spot_number, r.start_time " +
                             "FROM Reservations r " +
                             "JOIN ParkingSpots p ON r.spot_id = p.spot_id " +
                             "WHERE r.user_id = (SELECT user_id FROM Users WHERE email = ?) AND r.is_reserved = 1")) {

            fetchReservationsStmt.setString(1, userEmail);
            ResultSet rs = fetchReservationsStmt.executeQuery();

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int spotNumber = rs.getInt("spot_number");
                String startTime = rs.getString("start_time");

                reservationList.getItems().add(
                        String.format("Reservation ID: %d | Spot: %d | Start Time: %s", reservationId, spotNumber, startTime)
                );
            }

            if (reservationList.getItems().isEmpty()) {
                Label noReservationsLabel = new Label("No active reservations found.");
                noReservationsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");
                layout.getChildren().add(noReservationsLabel);
            } else {
                reservationList.getSelectionModel().selectFirst();
                layout.getChildren().addAll(new Label("Your Active Reservations:"), reservationList);
            }

        } catch (Exception ex) {
            Label errorLabel = new Label("Error fetching reservations: " + ex.getMessage());
            errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
            layout.getChildren().add(errorLabel);
        }

        payButton.setOnAction(e -> {
            String selectedReservation = reservationList.getSelectionModel().getSelectedItem();
            if (selectedReservation == null) {
                feedbackLabel.setText("Please select a reservation.");
                return;
            }

            try {
                // Parse reservation details
                String[] parts = selectedReservation.split("\\|");
                int reservationId = Integer.parseInt(parts[0].split(":")[1].trim());
                int spotNumber = Integer.parseInt(parts[1].split(":")[1].trim());

                // Begin transaction
                try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
                    conn.setAutoCommit(false);

                    try (PreparedStatement releaseSpotStmt = conn.prepareStatement(
                            "UPDATE ParkingSpots SET is_reserved = 0 WHERE spot_number = ?");
                         PreparedStatement updateReservationStmt = conn.prepareStatement(
                                 "UPDATE Reservations SET is_reserved = 0, payment_status = 'Paid', payment_amount = 20 WHERE reservation_id = ?")) {

                        // Execute updates
                        releaseSpotStmt.setInt(1, spotNumber);
                        updateReservationStmt.setInt(1, reservationId);

                        releaseSpotStmt.executeUpdate();
                        updateReservationStmt.executeUpdate();

                        conn.commit(); // Commit transaction
                        feedbackLabel.setText("Payment successful! Spot released.");
                        reservationList.getItems().remove(selectedReservation);
                    } catch (Exception ex) {
                        conn.rollback(); // Rollback transaction on error
                        feedbackLabel.setText("Error processing payment: " + ex.getMessage());
                    }
                }
            } catch (Exception ex) {
                feedbackLabel.setText("Error parsing reservation details: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(payButton, feedbackLabel);
        Scene scene = new Scene(layout, 390, 844);
        finishWindow.setScene(scene);
        finishWindow.show();
    }
}
