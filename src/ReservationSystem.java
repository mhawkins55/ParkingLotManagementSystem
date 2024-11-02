import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ReservationSystem {

    // Function to display available spots
    public static void showAvailableSpots(Connection conn) {
        String query = "SELECT spot_id, spot_number FROM ParkingSpots WHERE is_available = TRUE";

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            System.out.println("Available Parking Spots:");
            while (rs.next()) {
                System.out.println("Spot ID: " + rs.getInt("spot_id") + " - Spot Number: " + rs.getInt("spot_number"));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching available spots: " + e.getMessage());
        }
    }

    // Function to make a reservation
    public static void makeReservation(Connection conn) {
        // Display available spots first
        showAvailableSpots(conn);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the Spot ID you'd like to reserve: ");
        int spotId = scanner.nextInt();  // User selects a spot

        System.out.print("Enter your User ID: ");
        int userId = scanner.nextInt();  // User provides their user ID

        // Insert the reservation into the Reservations table
        String insertReservation = "INSERT INTO Reservations (user_id, spot_id, reservation_time) VALUES (?, ?, NOW())";
        String updateSpotAvailability = "UPDATE ParkingSpots SET is_available = FALSE WHERE spot_id = ?";

        try (PreparedStatement reservationStmt = conn.prepareStatement(insertReservation);
             PreparedStatement updateSpotStmt = conn.prepareStatement(updateSpotAvailability)) {

            // Insert reservation
            reservationStmt.setInt(1, userId);
            reservationStmt.setInt(2, spotId);
            int rowsAffected = reservationStmt.executeUpdate();

            // Mark the spot as unavailable
            if (rowsAffected > 0) {
                updateSpotStmt.setInt(1, spotId);
                updateSpotStmt.executeUpdate();
                System.out.println("Reservation successful for Spot ID: " + spotId);
            } else {
                System.out.println("Failed to make reservation.");
            }

        } catch (SQLException e) {
            System.out.println("Error making reservation: " + e.getMessage());
        }
    }
}
