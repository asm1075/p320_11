package src;

import org.postgresql.util.PSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.conn;
import static src.PostgresSSH.st;
import static src.PostgresSSH.username;

public class logIn {
    public static void main(String[] args) throws SQLException {
        login();
    }

    static void login() throws SQLException {
        st = conn.createStatement();
        Scanner scanner = new Scanner(System.in);
        System.out.println("User exists, log into their account");
        System.out.print("Enter your username: ");
        String user = scanner.next();
        System.out.print("Enter your password: ");
        String pass = scanner.next();

        String getPassword = "SELECT password FROM player WHERE username='" + user + "'";
        ResultSet rs = st.executeQuery(getPassword);
        if(rs.next()) {
            if (pass.equals(rs.getString(1))) {
                username = user;
            } else {
                System.out.println("Incorrect password. Womp womp :/\n");
                return;
            }
        } else {
            System.out.println("Username does not exist.  Please create an account. \n");
        }

        try {
            String query = "UPDATE player set last_accessed = NOW() WHERE username = '" + username + "'";
            st.executeQuery(query);
        } catch (PSQLException e) {
            System.out.println("Welcome " + username + ". You are logged in!\n");
        }
    }


}
