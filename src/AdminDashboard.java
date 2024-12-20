import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboard {

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ParkingLotDB";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "MH907005"; // Replace with your actual password

    public void display() {
        Stage window = new Stage();
        window.setTitle("Admin Dashboard");

        // Buttons for admin actions
        Button btnViewReservations = new Button("View Active Reservations");
        Button btnAddSpot = new Button("Add Parking Spot");
        Button btnRemoveSpot = new Button("Remove Parking Spot");
        Button btnReleaseReservation = new Button("Release a Reservation");
        Button btnReleaseAllReservations = new Button("Release All Reservations"); // New button
        Button btnLogout = new Button("Log Out");

        // Set button actions
        btnViewReservations.setOnAction(e -> viewActiveReservations());
        btnAddSpot.setOnAction(e -> addParkingSpot());
        btnRemoveSpot.setOnAction(e -> removeParkingSpot());
        btnReleaseReservation.setOnAction(e -> releaseReservation());
        btnReleaseAllReservations.setOnAction(e -> releaseAllReservations()); // New action
        btnLogout.setOnAction(e -> {
            System.out.println("Logging out...");
            window.close();
        });

        // Layout
        VBox layout = new VBox(10); // 10px spacing between elements
        layout.getChildren().addAll(
                btnViewReservations,
                btnAddSpot,
                btnRemoveSpot,
                btnReleaseReservation,
                btnReleaseAllReservations, // Add new button to layout
                btnLogout
        );

        // Scene
        Scene scene = new Scene(layout, 400, 300);
        window.setScene(scene);
        window.show();
    }

    private void viewActiveReservations() {
        Stage reservationWindow = new Stage();
        reservationWindow.setTitle("Active Reservations");

        VBox layout = new VBox(10);

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.reservation_id, u.email, p.spot_number, r.start_time " +
                             "FROM Reservations r " +
                             "JOIN Users u ON r.user_id = u.user_id " +
                             "JOIN ParkingSpots p ON r.spot_id = p.spot_id " +
                             "WHERE r.is_reserved = 1");
             ResultSet rs = stmt.executeQuery()) {

            Label header = new Label("Active Reservations:");
            layout.getChildren().add(header);

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                String userEmail = rs.getString("email");
                int spotNumber = rs.getInt("spot_number");
                String startTime = rs.getString("start_time");

                layout.getChildren().add(new Label(
                        String.format("ID: %d | User: %s | Spot: %d | Start: %s",
                                reservationId, userEmail, spotNumber, startTime)));
            }

        } catch (Exception ex) {
            layout.getChildren().add(new Label("Error fetching active reservations: " + ex.getMessage()));
        }

        Scene scene = new Scene(layout, 500, 400);
        reservationWindow.setScene(scene);
        reservationWindow.show();
    }

    private void addParkingSpot() {
        Stage addSpotWindow = new Stage();
        addSpotWindow.setTitle("Add Parking Spot");

        VBox layout = new VBox(10);

        Label header = new Label("Existing Parking Spots:");
        layout.getChildren().add(header);

        // Fetch and display existing parking spots
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement("SELECT spot_number FROM ParkingSpots");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int spotNumber = rs.getInt("spot_number");
                layout.getChildren().add(new Label("Spot Number: " + spotNumber));
            }

        } catch (Exception ex) {
            layout.getChildren().add(new Label("Error fetching parking spots: " + ex.getMessage()));
        }

        Label spotNumberLabel = new Label("Enter New Spot Number:");
        TextField spotNumberInput = new TextField();
        Button addButton = new Button("Add Spot");
        Label feedbackLabel = new Label();

        addButton.setOnAction(e -> {
            String spotNumber = spotNumberInput.getText();

            if (spotNumber.isEmpty()) {
                feedbackLabel.setText("Spot number cannot be empty.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "INSERT INTO ParkingSpots (spot_number, is_reserved) VALUES (?, 0)")) {

                stmt.setString(1, spotNumber);
                stmt.executeUpdate();
                feedbackLabel.setText("Spot added successfully!");

            } catch (Exception ex) {
                feedbackLabel.setText("Error adding spot: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(spotNumberLabel, spotNumberInput, addButton, feedbackLabel);

        Scene scene = new Scene(layout, 400, 400);
        addSpotWindow.setScene(scene);
        addSpotWindow.show();
    }

    private void releaseReservation() {
        Stage releaseWindow = new Stage();
        releaseWindow.setTitle("Release Reservation");

        VBox layout = new VBox(10);

        Label header = new Label("Active Reservations:");
        layout.getChildren().add(header);

        // Fetch and display active reservations
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT r.reservation_id, p.spot_number, r.start_time " +
                             "FROM Reservations r " +
                             "JOIN ParkingSpots p ON r.spot_id = p.spot_id " +
                             "WHERE r.is_reserved = 1");
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int reservationId = rs.getInt("reservation_id");
                int spotNumber = rs.getInt("spot_number");
                String startTime = rs.getString("start_time");

                layout.getChildren().add(new Label(
                        String.format("ID: %d | Spot: %d | Start: %s",
                                reservationId, spotNumber, startTime)));
            }

        } catch (Exception ex) {
            layout.getChildren().add(new Label("Error fetching active reservations: " + ex.getMessage()));
        }

        Label reservationIdLabel = new Label("Enter Reservation ID to Release:");
        TextField reservationIdInput = new TextField();
        Button releaseButton = new Button("Release Reservation");
        Label feedbackLabel = new Label();

        releaseButton.setOnAction(e -> {
            String reservationId = reservationIdInput.getText();

            if (reservationId.isEmpty()) {
                feedbackLabel.setText("Reservation ID cannot be empty.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement updateReservationStmt = conn.prepareStatement(
                         "UPDATE Reservations SET is_reserved = 0 WHERE reservation_id = ?");
                 PreparedStatement releaseSpotStmt = conn.prepareStatement(
                         "UPDATE ParkingSpots SET is_reserved = 0 WHERE spot_id = (SELECT spot_id FROM Reservations WHERE reservation_id = ?)")) {

                updateReservationStmt.setString(1, reservationId);
                releaseSpotStmt.setString(1, reservationId);

                updateReservationStmt.executeUpdate();
                releaseSpotStmt.executeUpdate();

                feedbackLabel.setText("Reservation released successfully!");

            } catch (Exception ex) {
                feedbackLabel.setText("Error releasing reservation: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(reservationIdLabel, reservationIdInput, releaseButton, feedbackLabel);

        Scene scene = new Scene(layout, 400, 400);
        releaseWindow.setScene(scene);
        releaseWindow.show();
    }

    private void releaseAllReservations() {
        Stage releaseAllWindow = new Stage();
        releaseAllWindow.setTitle("Release All Reservations");

        VBox layout = new VBox(10);
        Label feedbackLabel = new Label();

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement updateReservationsStmt = conn.prepareStatement(
                     "UPDATE Reservations SET is_reserved = 0 WHERE is_reserved = 1");
             PreparedStatement releaseSpotsStmt = conn.prepareStatement(
                     "UPDATE ParkingSpots SET is_reserved = 0 WHERE is_reserved = 1")) {

            updateReservationsStmt.executeUpdate();
            releaseSpotsStmt.executeUpdate();
            feedbackLabel.setText("All active reservations have been released!");

        } catch (Exception ex) {
            feedbackLabel.setText("Error releasing all reservations: " + ex.getMessage());
        }

        layout.getChildren().add(feedbackLabel);

        Scene scene = new Scene(layout, 300, 150);
        releaseAllWindow.setScene(scene);
        releaseAllWindow.show();
    }

    private void removeParkingSpot() {
        Stage removeSpotWindow = new Stage();
        removeSpotWindow.setTitle("Remove Parking Spot");

        VBox layout = new VBox(10);

        Label spotNumberLabel = new Label("Enter Spot Number to Remove:");
        TextField spotNumberInput = new TextField();
        Button removeButton = new Button("Remove Spot");
        Label feedbackLabel = new Label();

        removeButton.setOnAction(e -> {
            String spotNumber = spotNumberInput.getText();

            if (spotNumber.isEmpty()) {
                feedbackLabel.setText("Spot number cannot be empty.");
                return;
            }

            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM ParkingSpots WHERE spot_number = ?")) {

                stmt.setString(1, spotNumber);
                int rowsDeleted = stmt.executeUpdate();

                if (rowsDeleted > 0) {
                    feedbackLabel.setText("Spot removed successfully!");
                } else {
                    feedbackLabel.setText("Spot not found.");
                }

            } catch (Exception ex) {
                feedbackLabel.setText("Error removing spot: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(spotNumberLabel, spotNumberInput, removeButton, feedbackLabel);

        Scene scene = new Scene(layout, 400, 200);
        removeSpotWindow.setScene(scene);
        removeSpotWindow.show();
    }
}
