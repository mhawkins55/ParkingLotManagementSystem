import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class ReservationSystem {

    // Display available parking spots
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

    // Make a reservation
    public static void makeReservation(Connection conn, int userId) {
        Scanner scanner = new Scanner(System.in);
        showAvailableSpots(conn);

        System.out.print("Enter the Spot ID you'd like to reserve: ");
        int spotId = scanner.nextInt();

        System.out.print("Enter your parking start time (yyyy-MM-dd HH:mm:ss): ");
        String startTime = scanner.next(); // Accept start time input

        String insertReservation = "INSERT INTO Reservations (user_id, spot_id, start_time, is_reserved) VALUES (?, ?, ?, TRUE)";
        String updateSpotAvailability = "UPDATE ParkingSpots SET is_available = FALSE WHERE spot_id = ?";

        try (PreparedStatement reservationStmt = conn.prepareStatement(insertReservation);
             PreparedStatement updateSpotStmt = conn.prepareStatement(updateSpotAvailability)) {

            reservationStmt.setInt(1, userId);
            reservationStmt.setInt(2, spotId);
            reservationStmt.setString(3, startTime);
            int rowsAffected = reservationStmt.executeUpdate();

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

    // Finish reservation and process payment
    public static void finishReservation(Connection conn) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your Reservation ID: ");
        int reservationId = scanner.nextInt();

        String getReservationDetails = "SELECT start_time, spot_id FROM Reservations WHERE reservation_id = ?";
        String updateReservationStatus = "UPDATE Reservations SET is_reserved = FALSE, payment_status = 'Paid' WHERE reservation_id = ?";
        String releaseSpot = "UPDATE ParkingSpots SET is_available = TRUE WHERE spot_id = ?";
        double hourlyRate = 2.0;

        try (PreparedStatement getDetailsStmt = conn.prepareStatement(getReservationDetails);
             PreparedStatement updateStatusStmt = conn.prepareStatement(updateReservationStatus);
             PreparedStatement releaseSpotStmt = conn.prepareStatement(releaseSpot)) {

            getDetailsStmt.setInt(1, reservationId);
            ResultSet rs = getDetailsStmt.executeQuery();

            if (rs.next()) {
                String startTime = rs.getString("start_time");
                int spotId = rs.getInt("spot_id");

                // Calculate total parked time
                String calculateDurationQuery = "SELECT TIMESTAMPDIFF(HOUR, ?, NOW()) AS duration";
                try (PreparedStatement durationStmt = conn.prepareStatement(calculateDurationQuery)) {
                    durationStmt.setString(1, startTime);
                    ResultSet durationRs = durationStmt.executeQuery();
                    if (durationRs.next()) {
                        int duration = durationRs.getInt("duration");
                        double totalAmount = duration * hourlyRate;

                        System.out.printf("You have parked for %d hours. Total amount due: $%.2f\n", duration, totalAmount);

                        // Confirm payment
                        System.out.print("Enter 'Y' to confirm payment: ");
                        String confirm = scanner.next();
                        if (confirm.equalsIgnoreCase("Y")) {
                            // Update reservation status
                            updateStatusStmt.setInt(1, reservationId);
                            updateStatusStmt.executeUpdate();

                            // Release the parking spot
                            releaseSpotStmt.setInt(1, spotId);
                            releaseSpotStmt.executeUpdate();

                            System.out.println("Payment processed successfully. Spot is now available.");
                        } else {
                            System.out.println("Payment canceled.");
                        }
                    }
                }
            } else {
                System.out.println("Reservation not found.");
            }

        } catch (SQLException e) {
            System.out.println("Error processing payment: " + e.getMessage());
        }
    }

    // View all parking spots
    public static void displayAllParkingSpots(Connection conn) {
        String query = "SELECT spot_id, spot_number, is_available FROM ParkingSpots";
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int spotId = rs.getInt("spot_id");
                int spotNumber = rs.getInt("spot_number");
                boolean isAvailable = rs.getBoolean("is_available");
                System.out.println("Spot ID: " + spotId + ", Spot Number: " + spotNumber + ", Available: " + isAvailable);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving parking spots: " + e.getMessage());
        }
    }
}
