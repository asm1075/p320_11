package src;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

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
                4) For you
                >""");
        search = input.nextInt();
        String query = switch (search){
            case 1 -> // last 90 days
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM user_rating WHERE rating_date >= CURRENT_DATE - INTERVAL '90 days')";
            case 2 -> // followers
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM game_play WHERE username IN(SELECT username2 FROM friendship WHERE username1= '" + username + "'))";
            case 3 -> // new release
                    "SELECT title, release_date FROM video_game ORDER BY release_date DESC LIMIT 5;";
            case 4 -> // for you
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM user_rating WHERE rating_date >= CURRENT_DATE - INTERVAL '90 days')";
            default -> // not an option
                "";
        };
        rs = st.executeQuery(query);
        if(search != 3 && search != 4){ // not case 3 or 4, top 20
            TreeMap<Double, List<Integer>> rate = new TreeMap<>();
            while(rs.next()) {
                int vg_id = getVG_ID(rs.getString("title"));
                double r = rating(st, vg_id);
                if (!rate.containsKey(r))
                    rate.put(r, new ArrayList<>());
                rate.get(r).add(vg_id); // add vg_id to the list associated with the rating
            }
            int k = 1; // counter for listing top
            System.out.println("\nTop 20 games!");
            for (Map.Entry<Double, List<Integer>> entry : rate.descendingMap().entrySet())
                for (int vg_id : entry.getValue()) {
                    if(k > 20)
                        break;
                    System.out.println("\n" + k++ + ".");
                    query = "SELECT * FROM video_game WHERE vg_id=" + vg_id;
                    rs = st.executeQuery(query);
                    if (rs.next())
                        display(rs); // display
                }
            System.out.println("\n"); // new line for cleanliness
        }
        else if (search == 3) {
            int i = 1;
            System.out.println("\nTop 5 releases of the month:");
            while (rs.next()) {
                System.out.print(i++ + ": ");
                System.out.print(rs.getString(1));
                System.out.println("\t Released on: " + rs.getString(2));
            }
            System.out.println();
        }
        else if (search == 4) {
            TreeMap<Double, Integer> rate = new TreeMap<>(); // trees sort by default
            while(rs.next()){
                int vg_id = getVG_ID(rs.getString("title"));
                double r = rating(st, vg_id);
                rate.put(r, vg_id); // sort by rating
            }

            int k = 1; // counter for listing top
            System.out.println("\nGames recommended for you!");
            for(int i = 0; i < 5 && !rate.isEmpty(); i++) {
                System.out.println("\n" + k++ + ".");
                query = "SELECT * FROM video_game WHERE vg_id=" + rate.get(rate.lastKey()); // get the last key, which is greatest
                rs = st.executeQuery(query);
                if (rs.next())
                    display(rs); // display the info
                rate.remove(rate.lastKey()); // remove last key
            }

            System.out.println();
        }
        else {
            System.out.println("Invalid Choice");
        }
    }
}
