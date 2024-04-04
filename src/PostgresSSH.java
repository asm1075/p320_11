package src;
import src.createAccount;
import com.jcraft.jsch.*;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;


public class PostgresSSH {
    static String username = "";
    static int gc_id = 3017;
    static Connection conn = null;
    static Statement st;

    public static void main(String[] args) throws SQLException {
        int lport = 5432;
        String rhost = "starbug.cs.rit.edu";
        int rport = 5432;
        String user = System.getenv("USER"); //change to your username
        String password = System.getenv("PASS"); //change to your password
        String databaseName = "p320_11"; //change to your database name

        String driverName = "org.postgresql.Driver";
        //Connection conn = null;
        Session session = null;
        try {
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            JSch jsch = new JSch();
            session = jsch.getSession(user, rhost, 22);
            session.setPassword(password);
            session.setConfig(config);
            session.setConfig("PreferredAuthentications","publickey,keyboard-interactive,password");
            session.connect();
            System.out.println("Connected");
            int assigned_port = session.setPortForwardingL(lport, "127.0.0.1", rport);
            System.out.println("Port Forwarded");

            // Assigned port could be different from 5432 but rarely happens
            String url = "jdbc:postgresql://127.0.0.1:"+ assigned_port + "/" + databaseName;

            System.out.println("database Url: " + url);
            Properties props = new Properties();
            props.put("user", user);
            props.put("password", password);

            Class.forName(driverName);
            conn = DriverManager.getConnection(url, props);
            System.out.println("Database connection established");

            // Do something with the database....

            menu();



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Closing Database Connection");
                conn.close();
            }
            if (session != null && session.isConnected()) {
                System.out.println("Closing SSH Connection");
                session.disconnect();
            }
        }
    }


    public static void menu() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int choice;
        while (true) {
            System.out.println("Welcome " + username + "!");
            System.out.print("""
                    Menu:
                    1) Create an account
                    2) Log in
                    3) View/Edit collections
                    4) Search games
                    5) Play game
                    6) Rate game
                    7) Search friends
                    8) Log Out
                    9) Exit Program
                    >
                    """);
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> src.createAccount.createAcc();
                case 2 -> logIn();
                case 3 -> viewEditCollections();
                case 4 -> searchGames();
                case 5 -> playGame();
                case 6 -> rateGame();
                case 7 -> searchFriends();
                case 8 -> logOut();
                case 9 -> {
                    return;
                }
                default -> System.out.println("Invalid Input, please enter a number from the menu.");
            }
        }
    }

    // WORKS!
    private static void createAccount() throws SQLException {
        try {
            st = conn.createStatement();
            Scanner scanner = new Scanner(System.in);
            System.out.print("Creating an account for the user! ");
            System.out.print("Enter your username: ");
            String username = scanner.next();
            String query = "SELECT * FROM PLAYER WHERE username = '" + username + "'";
            ResultSet rs = st.executeQuery(query);
            while(rs.next()){ // check username duplicate
                System.out.println("Username already exists. Please enter a new username:");
                username = scanner.next();
                query = "SELECT * FROM PLAYER WHERE username = '" + username + "'"; // check username duplicate
                rs = st.executeQuery(query);
            }
            System.out.print("Enter your DOB in the format year-month-day: ");
            String DOB = scanner.next();
            System.out.print("Enter your password: ");
            String pass = scanner.next();
            System.out.print("Enter your email: ");
            String email = scanner.next();

            query = "INSERT into PLAYER VALUES ('" + username + "', '" + DOB + "', '" + pass +
                    "', NOW(), NOW(), '" + email + "')";
            st.executeQuery(query);
        } catch (PSQLException e) {
            System.out.println("Your account has been created " + username + "!\n");
        }
    }


    // WORKS!!
    private static void logIn() throws SQLException {
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

    // WORKS!!
    private static void viewEditCollections() throws SQLException {
        st = conn.createStatement();
        if (!checkLoggedIn()) {
            return;
        }
        Scanner scanner = new Scanner(System.in);
        ResultSet rs;
        System.out.println("Your Collections: \n");
        int choice;
        System.out.println("""
                1) View Collections
                2) Edit Collection
                3) Create Collection
                4) Delete Collection
                >\s""");
        choice = scanner.nextInt();
        switch (choice) {
            case 1:
                String query = "SELECT * FROM game_collection WHERE username = '" + username + "' order by name asc";
                rs = st.executeQuery(query);
                System.out.println("Your collections: ");
                System.out.println("Format: Collection Name, # VideoGames, Total Play Time (HH:MM)");
                while (rs.next()) {
                    int time = Integer.parseInt(rs.getString(5));
                    int hours   = time / 60;
                    int minutes = time % 60;
                    System.out.print(rs.getString(3) + "\t" + rs.getString(4) + "\t");
                    System.out.printf("%d:%02d\n", hours, minutes);
                }
                System.out.println();
                break;
            case 2:
                System.out.print("1) Edit Collection Name\n2) Add Game\n3) Delete Game\n>");
                choice = scanner.nextInt();
                System.out.println("Which collection would you like to edit?");
                String collection = scanner.next();

                int gamecoll_id = 0;
                int vg_id;
                String getCID = "SELECT gc_id FROM game_collection WHERE name='" + collection + "' AND username = '" + username + "'";
                rs = st.executeQuery(getCID);
                if (rs.next()) {
                    gamecoll_id = rs.getInt(1);
                }
                if (gamecoll_id == 0) {
                    System.out.println("Not a valid collection.\n");
                    return;
                }

                if (choice == 1) {
                    String updated = "";
                    try {
                        System.out.println("What would you like to change it to?");
                        updated = scanner.next();
                        query = "UPDATE game_collection set name ='" + updated + "' WHERE name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e) {
                        System.out.println(collection + " has been changed to " + updated + "!\n");
                    }

                } else if (choice == 2) {
                    System.out.println("Which video game would you like to add to " + collection + "?");
                    String vg_name = scanner.next();
                    vg_id = getVG_ID(vg_name);

                    String get_platform = "SELECT platform_name FROM hosts WHERE vg_id = '" + vg_id + "'";
                    rs = st.executeQuery(get_platform);
                    ArrayList<String> vg_platform = new ArrayList<>();
                    String user_platform = "";
                    while (rs.next()){
                        vg_platform.add(rs.getString(1));
                    }
                    String check_compatibility = "SELECT platform_name FROM user_platform WHERE username = '" + username + "'";
                    rs = st.executeQuery(check_compatibility);
                    if (rs.next()) {
                        user_platform = rs.getString(1);
                    }
                    boolean compatible = false;
                    for (String s : vg_platform) {
                        if (user_platform.equals(s)) {
                            compatible = true;
                            break;
                        }
                    }
                    if (!compatible){
                        System.out.println("Warning: the platform you own is not compatible with this game.");
                    }

                    try {
                        query = "INSERT into vg_collection VALUES (" + vg_id + ", " + gamecoll_id + ")";
                        st.executeQuery(query);
                    } catch (PSQLException e) {
                        System.out.println("Added " + vg_name + " to " + collection + "!\n");
                    }

                    try {
                        query = "UPDATE game_collection set num_games = num_games + 1 WHERE name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e){
                        System.out.println("Collection game count has been updated.");
                    }

                    // TODO don't have this hardcoded for next phase - asm
                    try {
                        query = "UPDATE game_collection set sum_gameplay_time = sum_gameplay_time + 10 WHERE name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e){
                        System.out.println("Collection total gameplay time  has been updated.");
                    }

                } else if (choice == 3) {
                    System.out.println("Which video game would you like to remove?");
                    String vg_name = scanner.next();
                    vg_id = getVG_ID(vg_name);

                    try {
                        query = "DELETE FROM vg_collection WHERE vg_id = " + vg_id + " AND gc_id = '" + gamecoll_id + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e) {
                        System.out.println("Deleted " + vg_name + " from " + collection + "!\n");
                    }

                    try {
                        query = "UPDATE game_collection set num_games = num_games - 1 WHERE name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e){
                        System.out.println("Collection game count has been updated.");
                    }

                    // TODO don't have this hardcoded for next phase - asm
                    try {
                        query = "UPDATE game_collection set sum_gameplay_time = sum_gameplay_time - 10 WHERE name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e){
                        System.out.println("Collection total gameplay time has been updated.");
                    }

                } else {
                    System.out.println("Not an option, womp womp.\n");
                }
                break;
            case 3:
                String name = "";
                try {
                    System.out.print("Enter new collection name to create: ");
                    name = scanner.next();
                    gc_id++;
                    query = "INSERT INTO game_collection VALUES (" + gc_id + ", '" + username + "', '" + name + "', 0, 0)";
                    st.executeQuery(query);
                } catch (PSQLException e) {
                    System.out.println(name + " has been created!\n");
                }
                break;
            case 4:
                name = "";
                try {
                    System.out.println("Which collection would you like to delete?");
                    name = scanner.next();
                    query = "DELETE FROM vg_collection where gc_id =" + getGC_ID(name);
                    st.executeQuery(query);
                } catch (PSQLException e) {
                    System.out.println("Games have been removed from collection.");
                }

                try {

                    query = "DELETE FROM game_collection WHERE name = '" + name + "'";
                    st.executeQuery(query);
                } catch (PSQLException e) {
                    System.out.println(name + " has been deleted!\n");
                }
                break;
            default:
                break;
        }
    }

    private static int getGC_ID(String gc_name) throws SQLException {
        st = conn.createStatement();
        int thisgc_id = 0;
        String getGCID = "SELECT gc_id FROM game_collection WHERE name='" + gc_name + "' AND username = '" + username + "'";

        ResultSet rs = st.executeQuery(getGCID);
        if (rs.next()) {
            thisgc_id = rs.getInt(1);
        }

        if (thisgc_id == 0) {
            System.out.println("You don't have a collection by this name.\n");
            menu();
        }
        return thisgc_id;
    }

    private static int getVG_ID(String vg_name) throws SQLException {
        st = conn.createStatement();
        int vg_id = 0;
        String getVGID = "SELECT vg_id FROM video_game WHERE title='" + vg_name + "'";

        ResultSet rs = st.executeQuery(getVGID);
        if (rs.next()) {
            vg_id = rs.getInt(1);
        }

        if (vg_id == 0) {
            System.out.println("Not a valid video game.\n");
            menu();
        }
        return vg_id;
    }

    private static String getVGName(int vg_id) throws SQLException {
        st = conn.createStatement();
        String title = "";
        String getVGID = "SELECT title FROM video_game WHERE vg_id='" + vg_id + "'";

        ResultSet rs = st.executeQuery(getVGID);
        if (rs.next()) {
            title = rs.getString(1);
        }

        if (title.equals("")) {
            System.out.println("Not a valid video game.\n");
            menu();
        }
        return title;
    }

    private static void searchGames() throws SQLException {
        st = conn.createStatement();
        Statement st2 = conn.createStatement(); // developer
        Statement st3 = conn.createStatement(); // publisher
        Statement st4 = conn.createStatement(); // dv_id
        Statement st5 = conn.createStatement(); // platform
        Statement st6 = conn.createStatement(); // start/end
        Statement st7 = conn.createStatement(); // rank
        checkLoggedIn();
        int vg_id = 0;
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

    // WORKS!!
    private static void playGame() throws SQLException {
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

    private static void playSpecificGame(int vg_id) throws SQLException {
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

    // WORKS!!
    private static void rateGame() throws SQLException {
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
            String query = "INSERT INTO user_rating VALUES ('" + username + "', " + vg_id + ", " + score + ", '" + comment + "')";
            st.executeQuery(query);
        } catch (PSQLException e) {
            System.out.println(vg_name + "has been rated with a score of " + score + " and the comment \"" + comment + "\"!\n");
        }

    }

    // WORKS!!
    private static void searchFriends() throws SQLException {
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

    private static void logOut() {
        username = "";
        System.out.println("You're logged out! Goodbye!");
    }

    private static boolean checkLoggedIn() {
        if (username.equals("")) {
            System.out.println("You are not logged in. " +
                    "Please log in or create an account before continuing.\n");
            return false;
        }
        return true;
    }


}