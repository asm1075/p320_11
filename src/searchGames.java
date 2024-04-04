package src;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import static src.PostgresSSH.*;

public class searchGames {
    public static void main(String[] args) throws SQLException {
        search();
    }

    static void search() throws SQLException {
        st = conn.createStatement();
        checkLoggedIn();
        int choice = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("""
                What would you like to search by?
                1) title
                2) platform
                3) release date
                4) developers
                5) price
                6) genre
                >""");
        int search = scanner.nextInt();
        System.out.println("Enter your search keyword(s)");
        String keyword = scanner.next();
        keyword = "%" + keyword + "%";
        String query = switch (search) {
            case 1 -> // search by title
                    "SELECT * FROM video_game WHERE title LIKE '" + keyword + "'";
            case 2 -> // search by platform
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM hosts WHERE platform_name LIKE '" + keyword + "')";
            case 3 -> // search by release date
                    "SELECT * FROM video_game WHERE release_date LIKE '" + keyword + "'";
            case 4 -> // search by developers
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM develops WHERE dp_id IN(SELECT dp_id FROM dev_pub WHERE username LIKE '" + keyword + "'))";
            case 5 -> // search by price
                    "SELECT * FROM video_game WHERE price LIKE '" + keyword + "'";
            case 6 -> // search by genre
                    "SELECT * FROM video_game WHERE vg_id IN(SELECT vg_id FROM vg_genre WHERE g_id IN(SELECT g_id FROM genre WHERE name LIKE '" + keyword + "'))";
            default -> ""; // not an option
        };
        if(!query.equals("")) {
            ResultSet rs = st.executeQuery(query + " ORDER BY title, release_date ASC;");
            while (choice != 2) {
                while(rs.next()) {
                    display(rs); // prints things out
                    System.out.println("\n");
                }
                System.out.println("\nWould you like to sort by something else?");
                System.out.println("1) Yes\n2) No\n>");
                choice = scanner.nextInt();
                if (choice == 1) { // repeat that shiiiii if you desire
                    System.out.println("""
                    What would you like to sort by?
                    1) title
                    2) price
                    3) genre
                    4) release year
                    >""");
                    int sort = scanner.nextInt();
                    System.out.println("Ascending (ASC) or descending (DSC) order?");
                    String order = scanner.next();
                    String end = switch(sort){
                        case 1 -> // order by title
                                " ORDER BY title " + order + ";";
                        case 2 -> // order by price
                            " ORDER BY price " + order + ";";
                        case 3 -> // order by genre, ngl idk what that means so imma leave it
                            "";
                        case 4 -> // order by release year
                            " ORDER BY release_year " + order + ";";
                        default -> ""; // not an option, just sorts same as before
                    };
                    rs = st.executeQuery(query + end);
                }
            }
        }
        else // no option
            System.out.println("Not an option");
    }
    private static void display(ResultSet rs) throws SQLException{
        Statement dst = conn.createStatement();
        String title = rs.getString("title"); // get title
        String esrb = rs.getString("esrb_rating"); // get rating
        int vg_id = getVG_ID(rs.getString("title")); // get vg_id
        ArrayList<String> developers = developers(dst, vg_id); // get developers
        String publisher = publisher(dst, vg_id); // get publisher
        ArrayList<String> platforms = platforms(dst, vg_id); // get platforms
        int playtime = playtime(dst, vg_id);
        double rating = rating(dst, vg_id); // gets average rating
        System.out.println("Title: " + title); // print title
        System.out.print("Platform(s): " + platforms.get(0)); // print platform(s)
        for (int i = 1; i < platforms.size(); i++)
            System.out.print(", " + platforms.get(i));
        if(!developers.isEmpty()) // print developer(s)
            System.out.print("\nDeveloper(s): " + developers.get(0));
        else
            System.out.print("\nDeveloper(s): ");
        for(int i = 1; i < developers.size(); i++)
            System.out.print(", " + developers.get(i));
        System.out.println("\nPublisher: " + publisher); // print publisher
        System.out.println("Playtime: " + playtime + " mins"); // print playtime
        System.out.println("ESRB Rating: " + esrb); // print esrb rating
        if(rating == 0.0) // print rating
            System.out.println("Game has not yet been rated");
        else
            System.out.println("Star rating: " + rating);
        dst.close(); // close
    }
    private static ArrayList<String> developers(Statement st, int vg_id) throws SQLException {
        ArrayList<String> developers = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT username FROM dev_pub WHERE dp_id IN (SELECT dp_id FROM develops WHERE vg_id='" + vg_id + "')");
        while (rs.next()) // gets developers
            developers.add(rs.getString("username"));
        return developers;
    }
    private static String publisher(Statement st, int vg_id) throws SQLException {
        String publisher = "";
        ResultSet rs = st.executeQuery("SELECT username FROM dev_pub WHERE dp_id IN (SELECT dp_id FROM publishes WHERE vg_id='" + vg_id + "')");
        if (rs.next()) // gets publisher
            publisher = rs.getString("username");
        return publisher;
    }
    private static ArrayList<String> platforms(Statement st, int vg_id) throws SQLException {
        ArrayList<String> platforms = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT platform_name FROM hosts WHERE vg_id='" + vg_id + "'");
        while (rs.next()) // gets platforms
            platforms.add(rs.getString("platform_name"));
        return platforms;
    }
    private static double rating(Statement st, int vg_id) throws SQLException {
        ArrayList<Integer> ratings = new ArrayList<>();
        ResultSet rs = st.executeQuery("SELECT score FROM user_rating WHERE vg_id='" + vg_id + "'");
        while (rs.next()) // gets ratings
            ratings.add(rs.getInt("score"));
        double average = 0.0; // average rank
        for (Integer rating : ratings) average += rating; // add up average
        return !ratings.isEmpty() ? average / ratings.size() : 0; // avoid division by zero
    }
    private static int playtime(Statement st, int vg_id) throws SQLException {
        ArrayList<String> start = new ArrayList<>();
        ArrayList<String> end = new ArrayList<>();
        int time = 0;
        ResultSet rs = st.executeQuery("SELECT start_time, end_time FROM game_play WHERE username='" + username + "'AND vg_id='" + vg_id + "'");
        while(rs.next()){ // get start and end times
            start.add(rs.getString("start_time"));
            end.add(rs.getString("end_time"));
        }
        for(int i = 0; i < start.size(); i++){
            int spaceS = start.get(i).indexOf(" "); // **get index of space, assuming playtime does not go over days**
            int spaceE = end.get(i).indexOf(" ");
            int hourS = Integer.parseInt(start.get(i).substring(spaceS+1, spaceS+3)); // hours start
            spaceS = spaceS + 3;
            int hourE = Integer.parseInt(start.get(i).substring(spaceE+1, spaceE+3)); // hours end
            spaceE = spaceE + 3;
            int minS = Integer.parseInt(start.get(i).substring(spaceS+1, spaceS+3)); // min start
            int minE = Integer.parseInt(start.get(i).substring(spaceE+1, spaceE+3)); // min end
            time += 60*Math.abs(hourS - hourE); // gets hours in min and adds
            time += Math.abs(minS - minE); // gets mins and adds
        }
        return time;
    }
}
