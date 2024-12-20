import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

public class Main {
    private static final String URL = "jdbc:mysql://localhost:3306/ParkingLotDB";
    private static final String USER = "root";
    private static final String PASSWORD = "MH907005";

    private static boolean isAdmin = false;

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Connected to the database!");
            Scanner scanner = new Scanner(System.in);

            while (true) {
                showMainMenu();
                int choice = scanner.nextInt();
                handleMainChoice(conn, choice, scanner);
            }
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }

    private static void showMainMenu() {
        System.out.println("1. User Registration");
        System.out.println("2. User Login");
        System.out.println("3. Admin Login");
        System.out.println("4. Exit");
    }

    private static void handleMainChoice(Connection conn, int choice, Scanner scanner) throws SQLException {
        switch (choice) {
            case 1:
                registerUser(conn, scanner);
                break;
            case 2:
                loginUser(conn, scanner);
                break;
            case 3:
                loginAdmin(scanner);
                break;
            case 4:
                System.exit(0);
            default:
                System.out.println("Invalid choice! Please try again.");
        }
    }

    private static void loginAdmin(Scanner scanner) {
        System.out.print("Enter admin username (admin): ");
        String username = scanner.next();
        System.out.print("Enter admin password (password): ");
        String password = scanner.next();

        if ("admin".equalsIgnoreCase(username) && "password".equals(password)) {
            isAdmin = true;
            System.out.println("Logged in as admin.");
            adminMenu();
        } else {
            System.out.println("Invalid admin credentials.");
        }
    }

    private static void adminMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean inAdminMenu = true;

        while (inAdminMenu) {
            System.out.println("Admin Menu:");
            System.out.println("1. Release a reservation");
            System.out.println("2. Add a parking spot");
            System.out.println("3. Remove a parking spot");
            System.out.println("4. Back");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    releaseReservation(scanner);
                    break;
                case 2:
                    addParkingSpot(scanner);
                    break;
                case 3:
                    removeParkingSpot(scanner);
                    break;
                case 4:
                    inAdminMenu = false; // Go back to main menu
                    System.out.println("Returning to main menu.");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void registerUser(Connection conn, Scanner scanner) {
        System.out.print("Enter your name: ");
        String name = scanner.next();
        System.out.print("Enter your email: ");
        String email = scanner.next();
        System.out.print("Enter your password: ");
        String password = scanner.next();

        String insertQuery = "INSERT INTO Users (name, email, password, user_type) VALUES (?, ?, ?, 'user')";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.executeUpdate();
            System.out.println("User registered successfully.");
        } catch (SQLException e) {
            System.out.println("Error registering user: " + e.getMessage());
        }
    }

    private static void loginUser(Connection conn, Scanner scanner) {
        System.out.print("Enter your email: ");
        String email = scanner.next();
        System.out.print("Enter your password: ");
        String password = scanner.next();

        String query = "SELECT user_type FROM Users WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && "user".equalsIgnoreCase(rs.getString("user_type"))) {
                System.out.println("Logged in as user.");
                userMenu(conn, scanner, email); // Pass email to user menu
            } else {
                System.out.println("Invalid email or password.");
            }
        } catch (SQLException e) {
            System.out.println("Error during user login: " + e.getMessage());
        }
    }

    private static void userMenu(Connection conn, Scanner scanner, String email) throws SQLException {
        boolean inUserMenu = true;

        while (inUserMenu) {
            System.out.println("User Menu:");
            System.out.println("1. View available parking spots");
            System.out.println("2. Make a reservation");
            System.out.println("3. Finish reservation and pay");
            System.out.println("4. Back");
            int userChoice = scanner.nextInt();

            switch (userChoice) {
                case 1:
                    viewAvailableSpots(conn);
                    break;
                case 2:
                    makeReservation(conn, scanner, email);
                    break;
                case 3:
                    finishReservation(conn, scanner, email);
                    break;
                case 4:
                    inUserMenu = false;
                    System.out.println("Returning to main menu.");
                    break;
                default:
                    System.out.println("Invalid choice! Please try again.");
            }
        }
    }

    private static void viewAvailableSpots(Connection conn) {
        String query = "SELECT spot_id, spot_number FROM ParkingSpots WHERE is_reserved = 0";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            System.out.println("Available parking spots:");
            while (rs.next()) {
                int spotId = rs.getInt("spot_id");
                int spotNumber = rs.getInt("spot_number");
                System.out.println("Spot ID: " + spotId + ", Spot Number: " + spotNumber);
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving available spots: " + e.getMessage());
        }
    }

    private static void makeReservation(Connection conn, Scanner scanner, String email) {
        viewAvailableSpots(conn);

        System.out.print("Enter the Spot ID to reserve: ");
        int spotId = scanner.nextInt();

        // Automatically fetch current date
        String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        // Prompt user for time only
        System.out.print("Enter your parking start time (hh:mm): ");
        String time = scanner.next();
        System.out.print("Is it AM or PM? (Enter AM/PM): ");
        String amPm = scanner.next().toUpperCase();

        // Combine date and time
        String startTimeInput = currentDate + " " + time + " " + amPm;

        // Convert to 24-hour format for the database
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime;
        try {
            Date parsedDate = inputFormat.parse(startTimeInput);
            startTime = outputFormat.format(parsedDate);
        } catch (ParseException e) {
            System.out.println("Invalid time format. Please try again.");
            return;
        }

        String reservationQuery = "INSERT INTO Reservations (user_id, spot_id, start_time, is_reserved) VALUES ((SELECT user_id FROM Users WHERE email = ?), ?, ?, TRUE)";
        String updateSpotQuery = "UPDATE ParkingSpots SET is_reserved = 1 WHERE spot_id = ?";
        try (PreparedStatement reservationStmt = conn.prepareStatement(reservationQuery);
             PreparedStatement updateSpotStmt = conn.prepareStatement(updateSpotQuery)) {

            reservationStmt.setString(1, email);
            reservationStmt.setInt(2, spotId);
            reservationStmt.setString(3, startTime);
            reservationStmt.executeUpdate();

            updateSpotStmt.setInt(1, spotId);
            updateSpotStmt.executeUpdate();

            System.out.println("Reservation successful for Spot ID: " + spotId);
        } catch (SQLException e) {
            System.out.println("Error making reservation: " + e.getMessage());
        }
    }

    private static void finishReservation(Connection conn, Scanner scanner, String email) {
        String getReservationsQuery = "SELECT reservation_id, spot_id, start_time FROM Reservations WHERE user_id = (SELECT user_id FROM Users WHERE email = ?) AND is_reserved = TRUE";
        try (PreparedStatement getReservationsStmt = conn.prepareStatement(getReservationsQuery)) {
            getReservationsStmt.setString(1, email);
            ResultSet rs = getReservationsStmt.executeQuery();

            System.out.println("Your active reservations:");
            boolean hasReservations = false;
            while (rs.next()) {
                hasReservations = true;
                int reservationId = rs.getInt("reservation_id");
                int spotId = rs.getInt("spot_id");
                String startTime = rs.getString("start_time");
                System.out.printf("Reservation ID: %d | Spot ID: %d | Start Time: %s\n", reservationId, spotId, startTime);
            }

            if (!hasReservations) {
                System.out.println("No active reservations found.");
                return;
            }

            System.out.print("Enter the Reservation ID to finish: ");
            int reservationId = scanner.nextInt();

            processReservation(conn, scanner, email, reservationId);
        } catch (SQLException e) {
            System.out.println("Error fetching reservations: " + e.getMessage());
        }
    }

    private static void processReservation(Connection conn, Scanner scanner, String email, int reservationId) {
        String getDetailsQuery = "SELECT start_time, spot_id FROM Reservations WHERE reservation_id = ? AND user_id = (SELECT user_id FROM Users WHERE email = ?)";
        String updateReservationQuery = "UPDATE Reservations SET is_reserved = FALSE, payment_status = 'Paid' WHERE reservation_id = ?";
        String releaseSpotQuery = "UPDATE ParkingSpots SET is_reserved = 0 WHERE spot_id = ?";
        try (PreparedStatement getDetailsStmt = conn.prepareStatement(getDetailsQuery);
             PreparedStatement updateReservationStmt = conn.prepareStatement(updateReservationQuery);
             PreparedStatement releaseSpotStmt = conn.prepareStatement(releaseSpotQuery)) {

            getDetailsStmt.setInt(1, reservationId);
            getDetailsStmt.setString(2, email);
            ResultSet rs = getDetailsStmt.executeQuery();

            if (rs.next()) {
                String startTime = rs.getString("start_time");
                int spotId = rs.getInt("spot_id");

                String calculateDurationQuery = "SELECT TIMESTAMPDIFF(HOUR, ?, NOW()) AS duration";
                try (PreparedStatement durationStmt = conn.prepareStatement(calculateDurationQuery)) {
                    durationStmt.setString(1, startTime);
                    ResultSet durationRs = durationStmt.executeQuery();
                    if (durationRs.next()) {
                        int duration = durationRs.getInt("duration");
                        double totalAmount = duration * 2.0;

                        System.out.printf("You parked for %d hours. Total amount: $%.2f\n", duration, totalAmount);

                        System.out.print("Confirm payment (Y/N): ");
                        String confirmation = scanner.next();
                        if (confirmation.equalsIgnoreCase("Y")) {
                            updateReservationStmt.setInt(1, reservationId);
                            updateReservationStmt.executeUpdate();

                            releaseSpotStmt.setInt(1, spotId);
                            releaseSpotStmt.executeUpdate();

                            System.out.println("Payment successful. Spot is now available.");
                        } else {
                            System.out.println("Payment canceled.");
                        }
                    }
                }
            } else {
                System.out.println("Reservation not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error completing reservation: " + e.getMessage());
        }
    }

    private static void releaseReservation(Scanner scanner) {
        System.out.println("Reserved parking spots:");
        String reservedSpotsQuery = "SELECT spot_id, spot_number FROM ParkingSpots WHERE is_reserved = 1";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(reservedSpotsQuery);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int spotId = rs.getInt("spot_id");
                int spotNumber = rs.getInt("spot_number");
                System.out.println("Spot ID: " + spotId + ", Spot Number: " + spotNumber);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving reserved spots: " + e.getMessage());
            return;
        }

        System.out.print("Enter the Spot ID to release: ");
        int spotId = scanner.nextInt();

        String updateQuery = "UPDATE ParkingSpots SET is_reserved = 0 WHERE spot_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(updateQuery)) {

            stmt.setInt(1, spotId);
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Reservation released for Spot ID: " + spotId);
            } else {
                System.out.println("Error: Spot ID not found or already available.");
            }
        } catch (SQLException e) {
            System.out.println("Error releasing reservation: " + e.getMessage());
        }
    }

    private static void addParkingSpot(Scanner scanner) {
        System.out.println("Current parking spots:");
        displayAllParkingSpots();

        System.out.print("Enter Spot ID to add: ");
        int spotId = scanner.nextInt();
        System.out.print("Enter Spot Number: ");
        int spotNumber = scanner.nextInt();

        String insertQuery = "INSERT INTO ParkingSpots (spot_id, spot_number, is_reserved) VALUES (?, ?, 0)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertQuery)) {

            stmt.setInt(1, spotId);
            stmt.setInt(2, spotNumber);
            stmt.executeUpdate();
            System.out.println("Parking spot added: Spot ID " + spotId + ", Spot Number " + spotNumber);
        } catch (SQLException e) {
            System.out.println("Error adding parking spot: " + e.getMessage());
        }
    }

    private static void removeParkingSpot(Scanner scanner) {
        System.out.println("Current parking spots:");
        displayAllParkingSpots();

        System.out.print("Enter Spot ID to remove: ");
        int spotId = scanner.nextInt();

        String deleteQuery = "DELETE FROM ParkingSpots WHERE spot_id = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setInt(1, spotId);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Parking spot removed: Spot ID " + spotId);
            } else {
                System.out.println("Error: Spot ID not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error removing parking spot: " + e.getMessage());
        }
    }

    private static void displayAllParkingSpots() {
        String query = "SELECT spot_id, spot_number, is_reserved FROM ParkingSpots";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int spotId = rs.getInt("spot_id");
                int spotNumber = rs.getInt("spot_number");
                boolean isReserved = rs.getBoolean("is_reserved");
                System.out.println("Spot ID: " + spotId + ", Spot Number: " + spotNumber + ", Reserved: " + isReserved);
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving all parking spots: " + e.getMessage());
        }
    }
}



