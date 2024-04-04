package src;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.*;

public class playGame {

    public static void main(String[] args) throws SQLException {
        play();
    }

    static void play() throws SQLException {
        st = conn.createStatement();
        if (!checkLoggedIn()) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("1) Play Game\n2) Play Random Game!\n>");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1 -> {
                System.out.println("Which game would you like to play?");
                String name = scanner.next();
                int vg_id = getVG_ID(name);
                playSpecificGame(vg_id);
            }
            case 2 -> {
                int vg_id;
                System.out.println("Random game!");
                String query = """
                        SELECT vg_id FROM video_game\s
                        OFFSET floor(random() * (SELECT COUNT(*)\s
                        FROM video_game)) LIMIT 1;""";
                ResultSet rs = st.executeQuery(query);
                if (rs.next()) {
                    vg_id = rs.getInt(1);
                    System.out.println(vg_id + " Game you're playing: " + getVGName(vg_id));
                    playSpecificGame(vg_id);
                }
            }
            default -> {
            }
        }
    }

    static void playSpecificGame(int vg_id) throws SQLException {
        st = conn.createStatement();
        Scanner scanner = new Scanner(System.in);
        System.out.println("When did you start playing? (Put in format YYYY-MM-DD HH:MM)");
        String start_time = scanner.nextLine() + ":00.000000";
        System.out.println("When did you end playing? (Put in format YYYY-MM-DD HH:MM)");
        String end_time = scanner.nextLine() + ":00.000000";
        try {
            String query = "INSERT into game_play VALUES ('" + username + "', " + vg_id +
                    ", '" + start_time + "', '" + end_time + "')";
            st.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("\nPlayed " + getVGName(vg_id) + "!\n");
        }

//        // TODO add this code to add game to collection/delete game from collection
//        // check if it's in a collection and then update the sum_gameplay time of that collection
//        String subQuery = "Select gc_id from vg_collection where vg_id = " + vg_id + ")"; // gets gc_ids
//        String query = "Update game_collection set sum_gameplay_time - sum_gameplay_time + 45 where gc_id = (" + subQuery;
//
//        try {
//            ResultSet rs = st.executeQuery(query);
//        } catch (PSQLException e) {
//            System.out.println("Updated gameplay time in relevant collections.");
//        }
    }
}
