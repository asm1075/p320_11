package src;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.conn;
import static src.PostgresSSH.st;
import static src.PostgresSSH.username;

public class createAccount {
    public static void main(String[] args) {
        createAcc();
    }

    static void createAcc() {
        try {
            st = conn.createStatement();
            Scanner scanner = new Scanner(System.in);
            System.out.print("Creating an account for the user! ");
            System.out.print("Enter your username: ");
            String username = scanner.next();
            String query = "SELECT * FROM PLAYER WHERE username = '" + username + "'";
            ResultSet rs = st.executeQuery(query);
            while(rs.next()){ // check username duplicate
                System.out.println("Username already exists. Please enter a new username:");
                username = scanner.next();
                query = "SELECT * FROM PLAYER WHERE username = '" + username + "'"; // check username duplicate
                rs = st.executeQuery(query);
            }
            System.out.print("Enter your DOB in the format year-month-day: ");
            String DOB = scanner.next();
            System.out.print("Enter your password: ");
            String pass = scanner.next();
            System.out.print("Enter your email: ");
            String email = scanner.next();

            query = "INSERT into PLAYER VALUES ('" + username + "', '" + DOB + "', '" + pass +
                    "', NOW(), NOW(), '" + email + "')";
            st.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Your account has been created " + username + "!\n");
        }
    }

}

