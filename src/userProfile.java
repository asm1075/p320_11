package src;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.TreeMap;

import static src.PostgresSSH.*;
import static src.searchGames.*;

public class userProfile {

    public static void main(String[] args) throws SQLException, ParseException {
        displayProfile();
    }

    static void displayProfile() throws SQLException, ParseException {
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
        TreeMap<Double, Integer> rate = new TreeMap<>(); // trees sort by default
        while(rs.next()){
            int vg_id = getVG_ID(rs.getString("title"));
            double r = rating(st, vg_id);
            rate.put(r, vg_id); // sort by rating
        }
        int k = 1; // counter for listing top
        System.out.println("\nTop 10 games you own!");
        for(int i = 0; i < 10 && !rate.isEmpty(); i++){ // get top 10, if there are 10. breaks if empty
            System.out.println("\n" + k++ + ".");
            query = "SELECT * FROM video_game WHERE vg_id=" + rate.get(rate.lastKey()); // get the last key, which is greatest
            rs = st.executeQuery(query);
            if (rs.next())
                display(rs); // display the info
            rate.remove(rate.lastKey()); // remove last key
        }
        System.out.println("\n"); // new line for cleanliness

    }
}
