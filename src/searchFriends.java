package src;

import org.postgresql.util.PSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.conn;
import static src.PostgresSSH.st;
import static src.PostgresSSH.username;

import static src.PostgresSSH.*;

public class searchFriends {


    public static void main(String[] args) throws SQLException {
        friend();
    }

    static void friend() throws SQLException {
        st = conn.createStatement();
        if (!checkLoggedIn()) {
            return;
        }
        System.out.println("1) Follow \n2) Unfollow\n");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        switch (choice) {
            case 1 -> {
                try {
                    System.out.println("Enter friend's email address:");
                    String email = scanner.next();
                    String getUsername = "SELECT username FROM player WHERE email ='" + email + "'";
                    ResultSet rs = st.executeQuery(getUsername);
                    if (rs.next()) { // makes sure there is space and exists
                        String friend = rs.getString(1);
                        if(!friend.equals(username)) { // no following yourself
                            String makeFriendship = "INSERT INTO friendship VALUES ('" + username + "', '" + rs.getString(1) + "')";
                            st.executeQuery(makeFriendship);
                        }
                        else
                            System.out.println("You cannot follow yourself, silly goose!");
                    } else // no user found
                        System.out.println("There is no user associated with this account");
                }
                catch(PSQLException e){
                    System.out.println("Friendship complete!");
                }

            }
            case 2 -> {
                try {
                    System.out.println("Enter friend's email address to unfollow:");
                    String email = scanner.next();
                    String getUsername = "SELECT username FROM player WHERE email ='" + email + "'";
                    ResultSet rs = st.executeQuery(getUsername);
                    if (rs.next()) { // make sure exists
                        String removeFriendship = "DELETE FROM friendship WHERE username1='" + username + "' AND username2='" + rs.getString(1) + "'";
                        st.executeQuery(removeFriendship);
                    } else
                        System.out.println("There is no user associated with this account");
                }
                catch(PSQLException e){
                    System.out.println("Friendship removed!");
                }
            }
            default -> {
            }
        }
    }
}
