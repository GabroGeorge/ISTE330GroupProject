// Student name: Gabro, George
// Practical Exam 01
// 10-13-2023

// Java program that connects to a database (Using StudentDB)

import java.sql.*; 
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;     

public class DataLayer{

   // Attributes
	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	private String sql;
    private boolean connection;
    private boolean editPerm; //Determines if user has edit permissions.
    public int col;


   //Sets up default driver and basis for the SQL database
	final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";    
    static String url = "jdbc:mysql://localhost/";
    
   //Connect to db, Takes username password and databasename
    public boolean connect(String user, String password, String database){
      //Nulls connection to avoid any issues
      //Sets up path to db
    url = url + database;
      //trys Connection
    try {
        Class.forName(DEFAULT_DRIVER);                                                             
        conn = DriverManager.getConnection(url, user, password);   
        System.out.println("\nCreated Connection! - Gabro, George\n");
        connection = true;
        
		} // end of try      
	catch(ClassNotFoundException cnfe) {
		System.out.print("ERROR IN CLASS, CONNECTION FAILED \n" + cnfe);
        connection = false;
    }catch(SQLException se) {
		System.out.print("ERROR SQLException, CONNECTION FAILED \n" + se);
        connection = false;
		}//end of catch
    
    return connection;
    }

   //Closes all connections after checking if connection is true.
    public boolean close(){
        try {
        if (connection) {
            rs.close();
            stmt.close();
            conn.close();
            System.out.println("SQL Connection Closed");
            return true;
        }else{
            System.out.println("SQL Connection was already closed");
            return true;
        }
    }
    catch(SQLException sqle){
        System.out.println("ERROR IN METHOD close()");
        System.out.println("ERROR MESSAGE -> "+sqle);
        return false;
    }
    }

    //Return true if user has edit permissions, otherwise false. Determines if user can log in.
    public boolean login(String username, String password) {
        
        try {
            String query = "SELECT * FROM userlogin WHERE username = ? AND password = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(query);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);

            try (ResultSet userIdResultSet = preparedStatement.executeQuery()) {
                if (userIdResultSet.next()) {
                    int userId = userIdResultSet.getInt("userID");

                    // Second query to get type_id based on user_id
                    String userTypeQuery = "SELECT typeID FROM users WHERE userID = ?";
                    try (PreparedStatement userTypeStatement = conn.prepareStatement(userTypeQuery)) {
                        userTypeStatement.setInt(1, userId);

                        try (ResultSet userTypeResultSet = userTypeStatement.executeQuery()) {
                            if (userTypeResultSet.next()) {
                                String userType = userTypeResultSet.getString("typeID");
                                if ("F".equals(userType)) {
                                    System.out.println("Login successful! User is faculty.");
                                    return true; // User is faculty
                                } else if("S".equals(userType)){
                                    System.out.println("Login successful! User is student.");
                                    return false; // User is not faculty
                                } else{
                                    System.out.println("Login successful! User is guest.");
                                    return false; // User is not faculty
                                }
                            } else {
                                System.out.println("Login failed. No matching user in faculty table.");
                                return false; // No matching user in faculty table
                            }
                        }
                    }
                } else {
                    System.out.println("Login failed. No matching user in userlogin table.");
                    return false; // No matching user in userlogin table
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Login failed due to an error.");
            return false;
        }
    }

    public void createOrUpdateAccount(String userType, int id, String firstName, String lastName,
                                   String email, String phoneNumber, String location,
                                   String username, String password) {
    try {
        String query;

        if ("Faculty".equalsIgnoreCase(userType)) {
            // Create or update Faculty account
            query = "INSERT INTO faculty (facultyid, fname, lname) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE fname = ?, lname = ?";

            try (PreparedStatement facultyStatement = conn.prepareStatement(query)) {
                facultyStatement.setInt(1, id);
                facultyStatement.setString(2, firstName);
                facultyStatement.setString(3, lastName);

                // Set parameters for ON DUPLICATE KEY UPDATE clause
                facultyStatement.setString(4, firstName);
                facultyStatement.setString(5, lastName);

                facultyStatement.executeUpdate();
            }

            // Create or update Faculty contact information
            query = "INSERT INTO facultycontact (facultyid, email, phonenumber, location) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE email = ?, phonenumber = ?, location = ?";
        } else if ("Student".equalsIgnoreCase(userType)) {
            // Create or update Student account
            query = "INSERT INTO student (studentid, fname, lname) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE fname = ?, lname = ?";

            try (PreparedStatement studentStatement = conn.prepareStatement(query)) {
                studentStatement.setInt(1, id);
                studentStatement.setString(2, firstName);
                studentStatement.setString(3, lastName);

                // Set parameters for ON DUPLICATE KEY UPDATE clause
                studentStatement.setString(4, firstName);
                studentStatement.setString(5, lastName);

                studentStatement.executeUpdate();
            }

            // Create or update Student contact information
            query = "INSERT INTO studentcontact (studentid, email, phonenumber) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE email = ?, phonenumber = ?";
        } else {
            System.out.println("Invalid user type.");
            return;
        }

        try (PreparedStatement contactStatement = conn.prepareStatement(query)) {
            contactStatement.setInt(1, id);
            
            if ("Faculty".equalsIgnoreCase(userType)) {
                contactStatement.setString(2, email);
                contactStatement.setString(3, phoneNumber);
                contactStatement.setString(4, location);

                // Set parameters for ON DUPLICATE KEY UPDATE clause
                contactStatement.setString(5, email);
                contactStatement.setString(6, phoneNumber);
                contactStatement.setString(7, location);
            } else if ("Student".equalsIgnoreCase(userType)) {
                contactStatement.setString(2, email);
                contactStatement.setString(3, phoneNumber);

                // Set parameters for ON DUPLICATE KEY UPDATE clause
                contactStatement.setString(4, email);
                contactStatement.setString(5, phoneNumber);
            }

            contactStatement.executeUpdate();

            // Create or update User Login
            query = "INSERT INTO userlogin (id, username, password) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE password = ?";

            try (PreparedStatement loginStatement = conn.prepareStatement(query)) {
                loginStatement.setInt(1, id);
                loginStatement.setString(2, username);
                loginStatement.setString(3, password);

                // Set parameter for ON DUPLICATE KEY UPDATE clause
                loginStatement.setString(4, password);

                loginStatement.executeUpdate();

                System.out.println("Account created/updated successfully.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
        System.out.println("Error creating/updating account.");
    }
}


    public static void main(String[] args) {
        DataLayer dataLayer = new DataLayer();
        String username = "root";
        String password = "student";
        String database = "project";

        boolean isConnected = dataLayer.connect(username, password, database);

        username = "jmd4173";
        password = "StudentPass";

        dataLayer.login(username, password);

        username = "JimHab";
        password = "FacultyPass";

        dataLayer.login(username, password);

        username = "Wegmans";
        password = "GuestPass";

        dataLayer.login(username, password);
        
        
        dataLayer.createOrUpdateAccount("Faculty",4, "John", "Doe", "john.doe@example.com", "1234567890", "New Location", "faculty123", "password123");

        // Example: Creating or updating a Student account
        dataLayer.createOrUpdateAccount("Student", 5,"Jane", "Doe", "jane.doe@example.com", "9876543210", null, "student123", "password456");

    }

}
