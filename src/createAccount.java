package src;

import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.conn;
import static src.PostgresSSH.st;
import static src.PostgresSSH.username;

public class createAccount {
    public static void main(String[] args) throws SQLException {
        createAcc();
    }

    static void createAcc() {
        try {
            st = conn.createStatement();
            Scanner scanner = new Scanner(System.in);
            System.out.print("Creating an account for the user!");
            System.out.print("Enter your username: ");
            String username = scanner.next();
            System.out.print("Enter your DOB in the format year-month-day: ");
            String DOB = scanner.next();
            System.out.print("Enter your password: ");
            String pass = scanner.next();
            System.out.print("Enter your email: ");
            String email = scanner.next();

            String query = "INSERT into PLAYER VALUES ('" + username + "', '" + DOB + "', '" + pass +
                    "', NOW(), NOW(), '" + email + "')";
            st.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Your account has been created " + username + "!\n");
        }
    }

}

