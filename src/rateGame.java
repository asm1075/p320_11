package src;

import org.postgresql.util.PSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.*;

public class rateGame {


    public static void main(String[] args) throws SQLException {
        rate();
    }

    static void rate() throws SQLException {
        st = conn.createStatement();
        if (!checkLoggedIn()) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        int vg_id = 0;
        System.out.println("What game would you like to rate?");
        String vg_name = scanner.next();

        String getVGID = "SELECT vg_id FROM video_game WHERE title ='" + vg_name + "'";
        ResultSet rs = st.executeQuery(getVGID);
        if (rs.next()) {
            vg_id = rs.getInt(1);
        }

        if (vg_id == 0) {
            System.out.println("Video game doesn't exist.\n");
            return;
        }

        System.out.println("What score would you like to give it (0-5)?");
        int score = scanner.nextInt();
        System.out.println("Any comments? (Up to 320 characters)");
        String comment = scanner.next();

        try {
            String query = "INSERT INTO user_rating VALUES ('" + username + "', " + vg_id + ", " + score + ", '" + comment + "', NOW())";
            st.executeQuery(query);
        } catch (PSQLException e) {
            System.out.println(vg_name + "has been rated with a score of " + score + " and the comment \"" + comment + "\"!\n");
        }

    }
}
