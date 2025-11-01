
import java.sql.*;
import java.util.Scanner;

public class HotelBookingSystem {

    public static void main(String[] args) {

        String url = "jdbc:mysql://localhost:3306/hotel";
        String user = "root";
        String pass = "root123";

        Scanner sc = new Scanner(System.in);

        try {
            // Load Driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to Database
            Connection con = DriverManager.getConnection(url, user, pass);
            System.out.println("Connected to Hotel Database Successfully!");

            while (true) {
                System.out.println("\n=== HOTEL ROOM BOOKING SYSTEM ===");
                System.out.println("1. View Available Rooms");
                System.out.println("2. Book Room");
                System.out.println("3. View All Customers");
                System.out.println("4. Checkout Customer");
                System.out.println("5. Exit");
                System.out.print("Enter your choice: ");
                int ch = sc.nextInt();

                switch (ch) {

                    case 1:
                        // Show Available Rooms
                        String query1 = "SELECT * FROM rooms WHERE status='Available'";
                        Statement stmt1 = con.createStatement();
                        ResultSet rs1 = stmt1.executeQuery(query1);

                        System.out.println("\nRoom No\tType\t\tPrice\tStatus");
                        System.out.println("----------------------------------------");
                        while (rs1.next()) {
                            System.out.printf("%d\t%-10s\t%.2f\t%s\n",
                                    rs1.getInt("room_no"),
                                    rs1.getString("type"),
                                    rs1.getDouble("price"),
                                    rs1.getString("status"));
                        }
                        rs1.close();
                        stmt1.close();
                        break;

                    case 2:
                        // Book Room
                        System.out.print("Enter Customer ID: ");
                        int cid = sc.nextInt();
                        sc.nextLine();
                        System.out.print("Enter Customer Name: ");
                        String cname = sc.nextLine();
                        System.out.print("Enter Room Number to Book: ");
                        int rno = sc.nextInt();
                        System.out.print("Enter Number of Days: ");
                        int days = sc.nextInt();

                        // Get room price
                        String priceQuery = "SELECT price, status FROM rooms WHERE room_no=?";
                        PreparedStatement psCheck = con.prepareStatement(priceQuery);
                        psCheck.setInt(1, rno);
                        ResultSet rsCheck = psCheck.executeQuery();

                        if (rsCheck.next()) {
                            String status = rsCheck.getString("status");
                            double price = rsCheck.getDouble("price");

                            if (status.equalsIgnoreCase("Available")) {
                                double total = price * days;

                                String insertCust = "INSERT INTO customers VALUES (?, ?, ?, ?, ?)";
                                PreparedStatement psInsert = con.prepareStatement(insertCust);
                                psInsert.setInt(1, cid);
                                psInsert.setString(2, cname);
                                psInsert.setInt(3, rno);
                                psInsert.setInt(4, days);
                                psInsert.setDouble(5, total);
                                psInsert.executeUpdate();

                                // Update room status
                                String updateRoom = "UPDATE rooms SET status='Booked' WHERE room_no=?";
                                PreparedStatement psUpdate = con.prepareStatement(updateRoom);
                                psUpdate.setInt(1, rno);
                                psUpdate.executeUpdate();

                                System.out.println("Room booked successfully for " + cname + "!");
                                System.out.println("Total cost: ₹" + total);

                                psInsert.close();
                                psUpdate.close();
                            } else {
                                System.out.println("Room is already booked!");
                            }
                        } else {
                            System.out.println(" Room not found!");
                        }
                        rsCheck.close();
                        psCheck.close();
                        break;

                    case 3:
                        // View All Customers
                        String query3 = "SELECT * FROM customers";
                        Statement stmt3 = con.createStatement();
                        ResultSet rs3 = stmt3.executeQuery(query3);

                        System.out.println("\nCID\tName\t\tRoom No\tDays\tTotal Cost");
                        System.out.println("------------------------------------------------");
                        while (rs3.next()) {
                            System.out.printf("%d\t%-10s\t%d\t%d\t₹%.2f\n",
                                    rs3.getInt("cid"),
                                    rs3.getString("name"),
                                    rs3.getInt("room_no"),
                                    rs3.getInt("days"),
                                    rs3.getDouble("total_cost"));
                        }
                        rs3.close();
                        stmt3.close();
                        break;

                    case 4:
                        // Checkout
                        System.out.print("Enter Customer ID to Checkout: ");
                        int checkId = sc.nextInt();

                        // Find customer room
                        String getRoom = "SELECT room_no FROM customers WHERE cid=?";
                        PreparedStatement psGet = con.prepareStatement(getRoom);
                        psGet.setInt(1, checkId);
                        ResultSet rsGet = psGet.executeQuery();

                        if (rsGet.next()) {
                            int roomNo = rsGet.getInt("room_no");

                            // Delete customer
                            String delCust = "DELETE FROM customers WHERE cid=?";
                            PreparedStatement psDel = con.prepareStatement(delCust);
                            psDel.setInt(1, checkId);
                            psDel.executeUpdate();

                            // Make room available
                            String makeAvailable = "UPDATE rooms SET status='Available' WHERE room_no=?";
                            PreparedStatement psAvail = con.prepareStatement(makeAvailable);
                            psAvail.setInt(1, roomNo);
                            psAvail.executeUpdate();

                            System.out.println(" Customer checked out. Room " + roomNo + " is now available.");
                            psDel.close();
                            psAvail.close();
                        } else {
                            System.out.println(" Customer not found!");
                        }

                        rsGet.close();
                        psGet.close();
                        break;

                    case 5:
                        System.out.println("Exiting... Thank you for using the system!");
                        con.close();
                        sc.close();
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice! Try again.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
