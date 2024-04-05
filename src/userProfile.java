package src;

import java.sql.ResultSet;
import java.sql.SQLException;

import static src.PostgresSSH.*;

public class userProfile {

    public static void main(String[] args) throws SQLException {
        displayProfile();
    }

    static void displayProfile() throws SQLException {
        if (!checkLoggedIn()) {
            return;
        }
        st = conn.createStatement();
        System.out.println("Here is " + username + "'s profile!\n");

        // num collections
        ResultSet rs;
        String query = "SELECT COUNT(gc_id) from game_collection where username = '" + username + "'";
        rs = st.executeQuery(query);
        System.out.print("Number of collections: ");
        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        // num users who follow this user
        query = "SELECT COUNT(username1) from friendship where username2 = '" + username + "'";
        rs = st.executeQuery(query);
        System.out.print("Number of users who follow you: ");
        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        // num users this user is following
        query = "SELECT COUNT(username2) from friendship where username1 = '" + username + "'";
        rs = st.executeQuery(query);
        System.out.print("Number of users you follow: ");
        while (rs.next()) {
            System.out.println(rs.getInt(1));
        }

        // top 10 games owned by highest rating
        query = "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM game_play WHERE username= '" + username + "')";
        rs = st.executeQuery(query);
        // im tired will do later



    }
}
