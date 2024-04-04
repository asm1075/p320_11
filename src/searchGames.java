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
        Statement st2 = conn.createStatement(); // developer
        Statement st3 = conn.createStatement(); // publisher
        Statement st4 = conn.createStatement(); // dv_id
        Statement st5 = conn.createStatement(); // platform
        Statement st6 = conn.createStatement(); // start/end
        Statement st7 = conn.createStatement(); // rank
        checkLoggedIn();
        int vg_id;
        int dp_id = 0;
        Scanner scanner = new Scanner(System.in);
        System.out.println("What would you like to search by? (title, platform, release date, developers, price, and genre)");
        String search = scanner.next();
        System.out.println("Enter your search keyword(s)");
        String keyword = scanner.next();
        // can only sort by title and MAYBE release date rn
        String query = "SELECT * FROM video_game WHERE " + search + "='" + keyword + "' ORDER BY title, release_date ASC;";
        ResultSet rs = st.executeQuery(query);
        ArrayList<String> title = new ArrayList<>(); // title list
        ArrayList<String> esrb = new ArrayList<>(); // esrb rating list
        ArrayList<String> developer = new ArrayList<>(); // developer list
        ArrayList<String> publisher = new ArrayList<>(); // publisher list
        ArrayList<String> platform = new ArrayList<>(); // platform list
        ArrayList<String> start = new ArrayList<>(); // start time list
        ArrayList<String> end = new ArrayList<>(); // end time list
        ArrayList<String> rank = new ArrayList<>(); // rank list
        int choice = 1; // sort by other things
        while(choice != 2){
            while(rs.next()){
                title.add(rs.getString("title")); // add title
                esrb.add(rs.getString("esrb_rating")); // add rating
                vg_id = getVG_ID(rs.getString("title"));
                ResultSet rs2 = st2.executeQuery("SELECT dp_id FROM develops WHERE vg_id='" + vg_id + "'"); // develop
                ResultSet rs3 = st3.executeQuery("SELECT dp_id FROM publishes WHERE vg_id='" + vg_id + "'"); // publish
                if(rs2.next()) // get developer id for developers
                    dp_id = rs2.getInt("dp_id");
                ResultSet rs4 = st4.executeQuery("SELECT username FROM dev_pub WHERE dp_id='" + dp_id + "'");
                while(rs4.next()) // add all developers
                    developer.add(rs4.getString("username"));
                developer.add("1"); // buffer??
                if(rs3.next()) // get developer id for publishers
                    dp_id = rs3.getInt("dp_id");
                rs4 = st4.executeQuery("SELECT username FROM dev_pub WHERE dp_id='" + dp_id + "'");
                if(rs4.next()) // add publishers
                    publisher.add(rs4.getString("username"));
                ResultSet rs5 = st5.executeQuery("SELECT platform_name FROM hosts WHERE vg_id='" + vg_id + "'"); // platforms
                while(rs5.next()) // add platforms
                    platform.add(rs5.getString("platform_name"));
                platform.add("1"); // buffer??
                ResultSet rs6 = st6.executeQuery("SELECT start_time, end_time FROM game_play WHERE username='" + username + "'AND vg_id='" + vg_id + "'");
                while(rs6.next()){ // add all start and end times
                    start.add(rs6.getString("start_time"));
                    end.add(rs6.getString("end_time"));
                }
                start.add("1"); // buffer
                end.add("1"); // buffer
                ResultSet rs7 = st7.executeQuery("SELECT score FROM user_rating WHERE vg_id='" + vg_id + "'");
                while(rs7.next())
                    rank.add(rs7.getString("score")); // add ranks
                rank.add("1"); // buffer
            }
            int pIndex = 0; // platform index
            int dIndex = 0; // developer index
            int sIndex = 0; // start index
            int eIndex = 0; // end index
            int time = 0; // total time
            int average = 0; // average rank
            int rIndex = 0; // rank index
            for(int i = 0; i < title.size(); i++){
                System.out.println("Title: " + title.get(i));
                System.out.print("Platform(s): " + platform.get(pIndex));
                while(!platform.get(++pIndex).equals("1")) // not the buffer
                    System.out.print(", " + platform.get(pIndex));
                pIndex++; // increment for next time?
                System.out.println("\nDeveloper(s): " + developer.get(dIndex));
                while(!developer.get(++dIndex).equals("1")) // not the buffer
                    System.out.println(", " + developer.get(dIndex));
                dIndex++; // increment for next time
                System.out.println("Publisher: " + publisher.get(i));
                if(!start.isEmpty()){
                    while(!start.get(sIndex).equals("1")){
                        int spaceS = start.get(sIndex).indexOf(" "); // **get index of space, assuming playtime does not go over days**
                        int spaceE = end.get(eIndex).indexOf(" ");
                        int hourS = Integer.parseInt(start.get(sIndex).substring(spaceS+1, spaceS+3)); // hours start
                        spaceS = spaceS + 3;
                        int hourE = Integer.parseInt(start.get(eIndex).substring(spaceE+1, spaceE+3)); // hours end
                        spaceE = spaceE + 3;
                        int minS = Integer.parseInt(start.get(sIndex).substring(spaceS+1, spaceS+3)); // min start
                        int minE = Integer.parseInt(start.get(eIndex).substring(spaceE+1, spaceE+3)); // min end
                        time += 60*Math.abs(hourS - hourE); // gets hours in min and adds
                        time += Math.abs(minS - minE); // gets mins and adds
                        sIndex++; // increment start index
                        eIndex++; // increment end index
                    }
                }
                System.out.println("Playtime: " + time);
                System.out.println("ESRB Rating: " + esrb.get(i));
                int count = 0;
                if(!rank.isEmpty()){
                    while(!rank.get(rIndex).equals("1")) {
                        average += Integer.parseInt(rank.get(rIndex));
                        rIndex++; // increment
                        count++;
                    }
                    System.out.println("Star rating: " + (1.0)*(average/count));
                } else {
                    System.out.println("Game has not yet been rated");
                }
            }
            title.clear();
            esrb.clear();
            developer.clear();
            publisher.clear();
            platform.clear();
            start.clear();
            end.clear();
            rank.clear();
            System.out.println("\nWould you like to sort by something else?");
            System.out.println("1) Yes\n2)No\n>");
            choice = scanner.nextInt();
            if (choice == 1) { // repeat that shiiiii if you desire
                System.out.println("What would you like to sort by? (name, price, genre, release year)");
                String sort = scanner.next();
                System.out.println("Ascending (ASC) or descending (DSC) order?");
                String order = scanner.next();
                query = "SELECT * FROM video_game WHERE " + search + "='" + keyword + "' ORDER BY " + sort + " " + order + ";";
                rs = st.executeQuery(query);
            }
        }
        st2.close();
        st3.close();
        st4.close();
        st5.close();
        st6.close();
        st7.close();
    }
}
