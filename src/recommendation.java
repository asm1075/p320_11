package src;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;
import java.util.TreeMap;

import static src.PostgresSSH.*;
import static src.searchGames.display;
import static src.searchGames.rating;

public class recommendation {
    public static void main(String[] args) throws SQLException, ParseException {
        recommend();
    }
    static void recommend() throws SQLException, ParseException{
        if (!checkLoggedIn()) {
            return;
        }
        st = conn.createStatement();
        ResultSet rs;
        int search;
        Scanner input = new Scanner(System.in);
        System.out.println("""
                What recommendations do you want?
                1) Top 20 most popular video games in the last 90 days
                2) Top 20 most popular video games among my followers
                3) Top 5 new releases of the month
                >""");
        search = input.nextInt();
        String query = switch (search){
            case 1 -> // last 90 days
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM user_rating WHERE rating_date >= CURRENT_DATE - INTERVAL '90 days')";
            case 2 -> // followers
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM game_play WHERE username IN(SELECT username2 FROM friendship WHERE username1= '" + username + "'))";
            case 3 -> // new release
                    "SELECT title, release_date FROM video_game ORDER BY release_date DESC LIMIT 5;";
            default -> // not an option
                "";
        };
        rs = st.executeQuery(query);
        if(search != 3){ // not case 3, top 20
            TreeMap<Double, Integer> rate = new TreeMap<>(); // trees sort by default
            while(rs.next()){
                int vg_id = getVG_ID(rs.getString("title"));
                double r = rating(st, vg_id);
                rate.put(r, vg_id); // sort by rating
            }
            int k = 1; // counter for listing top
            System.out.println("\nTop 20 games!");
            for(int i = 0; i < 20 && !rate.isEmpty(); i++){ // get top 20, if there are 20. breaks if empty
                System.out.println("\n" + k++ + ".");
                query = "SELECT * FROM video_game WHERE vg_id=" + rate.get(rate.lastKey()); // get the last key, which is greatest
                rs = st.executeQuery(query);
                if (rs.next())
                    display(rs); // display the info
                rate.remove(rate.lastKey()); // remove last key
            }
            System.out.println("\n"); // new line for cleanliness
        } else {
            while (rs.next()) {

                System.out.print(rs.getString(1));
                System.out.println("\t Released on: " + rs.getString(2));
            }
            System.out.println();
        }
    }
}
