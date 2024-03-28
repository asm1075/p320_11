package src;
import com.jcraft.jsch.*;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;


public class PostgresSSH {
    static String username = "";
    static int gc_id = 3017;
    static int gc_id = 3016;
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
                case 1 -> createAccount();
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
            System.out.print("Creating an account for the user!");
            System.out.print("Enter your username: ");
            String username = scanner.next();
            System.out.print("Enter your DOB in the format year-month-day: ");
            String DOB = scanner.next();
            System.out.print("Enter your password: ");
            String pass = scanner.next();
            System.out.print("Enter your email: ");
            String email = scanner.next();

            String query = "INSERT into PLAYER VALUES ('" + username + "', '" + DOB + "', '" + pass +
                    "', NOW(), NOW(), '" + email + "')";
            st.executeQuery(query);
        } catch (PSQLException e) {
            // catches exception because nothing is being returned
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

    private static void viewEditCollections() throws SQLException {
        st = conn.createStatement();
        checkLoggedIn();
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
                // WORKS
                String query = "SELECT * FROM game_collection WHERE username = '" + username + "' order by name asc";
                rs = st.executeQuery(query);
                System.out.println("Your collections: ");
                System.out.println("Format: Collection Name \t # VideoGames \t Total Play Time (HH:MM)");
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
                // WORKS!!
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

                    // TODO add a complex query here to check if this videogames' platform is one of the users platform
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
                    for (int i = 0; i < vg_platform.size(); i++ ) {
                        if (user_platform.equals(vg_platform.get(i))){
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
                } else {
                    System.out.println("Not an option, womp womp.\n");
                }
                break;
            case 3:
                // WORKS!!
                String name = "";
                try {
                    System.out.print("Enter new collection name to create: ");
                    name = scanner.next();
                    gc_id++;
                    query = "INSERT INTO game_collection VALUES (" + gc_id + ", '" + username + "', '" + name + "')";
                    st.executeQuery(query);
                } catch (PSQLException e) {
                    System.out.println(name + " has been created!\n");
                }
                break;
            case 4:
                // WORKS!!
                name = "";
                try {
                    System.out.println("Which collection would you like to delete?");
                    name = scanner.next();
                    query = "DELETE FROM game_collection WHERE name = '" + name + "'";
                    st.executeQuery(query);
                } catch (PSQLException e) {
                    System.out.println(name + " has been deleted!\n");
                }
                break;
            default:
                menu();
        }
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
        checkLoggedIn();
        Scanner scanner = new Scanner(System.in);
        System.out.println("Search by (title, platform, release date, developers, price, and genre): \n");

        // search through every entry in video_games and if search word matches any attributes,
        // display them alphabetically (ascending) by video game's name and release date
        // show name, platform, developers, publisher, playtime, ratings (age and user)

        System.out.println("Sort by: \n1) Name\n2)Price\n3)Genre\n4)Released year");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                // sort list by name
                break;
            case 2:
                // sort list by price
                break;
            case 3:
                // sort list by genre
                break;
            case 4:
                // sort list by released year
                break;
            default:
                menu(); // because you can't stick to the menu options :(
        }
    }

    // WORKS!!
    private static void playGame() throws SQLException {
        st = conn.createStatement();
        checkLoggedIn();
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
        checkLoggedIn();
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
        checkLoggedIn();

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
                        String makeFriendship = "INSERT INTO friendship VALUES ('" + username + "', '" + rs.getString(1) + "')";
                        st.executeQuery(makeFriendship);
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

    private static void checkLoggedIn() throws SQLException {
        if (username.equals("")) {
            System.out.println("You are not logged in. " +
                    "Please log in or create an account before continuing.\n");
            menu();
        }
    }


}