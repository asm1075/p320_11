package src;
import com.jcraft.jsch.*;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.util.Properties;
import java.util.Scanner;


public class PostgresSSH {
    static String username = "";
    static int gc_id = 3002;
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
        int choice = 0;
        while (choice != 8) {
            System.out.print("""
                    Menu:
                    1) Create an account
                    2) Log in
                    3) View/Edit collections
                         a) edit collection
                           - edit collection name
                            - delete game
                         b) create collection
                         c) delete collection
                    4) Search games
                         a) sort by
                         b) add game to collection
                         c) play game - make sure player owns platform
                         d) rate game
                    5) Play game
                         a) play random game
                         b) search game --> (aka goes to menu option 4)\s
                    6) Search friends
                         a) follow
                         b) unfollow
                    7) Log Out
                    8) Exit Program
                    > 
                    """);
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> createAccount();
                case 2 -> logIn();
                case 3 -> viewEditCollections();
                case 4 -> searchGames();
                case 5 -> playGame();
                case 6 -> searchFriends();
                case 7 -> logOut();
                case 8 -> {
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
        username = scanner.next();
        System.out.print("Enter your password: ");
        String pass = scanner.next();

        String getPassword = "SELECT password FROM player WHERE username ='" + username + "'";
        try {
            ResultSet rs = st.executeQuery(getPassword);
            if (pass.equals(rs.getString(3))) {
                System.out.println("Welcome " + username + ". You are logged in!\n");
            } else {
                System.out.println("Incorrect password. Womp womp :/");
            }
        } catch (PSQLException e) {

        }

        try {
            String query = "UPDATE player set last_accessed = NOW() WHERE username = '" + username + "'";
            st.executeQuery(query);
        } catch (PSQLException e) {

        }



    }

    private static void viewEditCollections() throws SQLException {
        st = conn.createStatement();
        if (username.equals("")) {
            System.out.println("You are not logged in");
            return;
        }


        Scanner scanner = new Scanner(System.in);
        System.out.println("Your Collections: \n");
        int choice;
        System.out.println("1) View collection\n 2) Edit Collection\n 3) Create Collection" +
                "\n 4) Delete Collection\n");
        choice = scanner.nextInt();
        switch (choice) {
            case 1:
                // start SQL codeblock here
                String query = "SELECT * FROM game_collection WHERE username =" + username + " order by username asc";
                ResultSet rs = st.executeQuery(query);
                while (rs.next()) {
                    System.out.print("Column 1 returned ");
                    System.out.println(rs.getString(1));
                }
                // end SQL codeblock here

                //TODO display number of games in collection and total play time in hours: minutes
                break;
            case 2:
                System.out.println("1) Edit Collection Name\n 2) Delete Game\n");
                choice = scanner.nextInt();
                if (choice == 1) {
                    try {
                        System.out.println("Which collection name would you like to change?");
                        String collection = scanner.next();
                        System.out.println("What would you like to change it to?");
                        String updated = scanner.next();
                        query = "UPDATE game_collection set name ='" + updated + "' WHERE name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e) {

                    }
                } else if (choice == 2) {
                    try {
                        System.out.println("Which video game would you like to remove (video game ID)?");
                        int vg_id = scanner.nextInt();
                        System.out.println("Which collection would you like to remove this video game from?");
                        String collection = scanner.next();
                        query = "DELETE FROM game_collection WHERE vg_id = " + vg_id + " AND name = '" + collection + "'";
                        st.executeQuery(query);
                    } catch (PSQLException e) {

                    }
                } else {
                    System.out.println("Not an option, womp womp.");
                }
                break;
            case 3:
                try {
                    System.out.println("Enter new collection name to create: ");
                    String collectionName = scanner.next();
                    query = "INSERT INTO game_collection VALUES (" + gc_id++ + ", '" + username + "', '" + collectionName + "' , 0";
                    st.executeQuery(query);
                } catch (PSQLException e) {

                }
                break;
            case 4:
                try {
                    System.out.println("Which collection would you like to delete?");
                    String name = scanner.next();
                    query = "DELETE FROM game_collection WHERE name = '" + name + "'";
                    st.executeQuery(query);
                } catch (PSQLException e) {

                }
                break;
            default:
                // go back to main menu because you suck for not entering something correctly
                return;
        }
    }

    private static void searchGames() throws SQLException {
        st = conn.createStatement();
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
                return; // because you can't stick to the menu options :(
        }

        System.out.println("Select game (type in primary key for video game pls):\n");
        String videoGamePrimaryKey = scanner.next();
        System.out.println("1) Add game to collection\n2) Play game\n3)Rate Game\n");
        choice = scanner.nextInt();
        switch (choice) {
            case 1:
                System.out.println("Collection to add game to: ");
                // search for PK with collection name
                // add videoGamePrimaryKey to collection
                break;
            case 2:
                // uh just mark game as played??
                // idrk how to do this one so do we jsut add a random time to playtime?
                break;
            case 3:
                // fill out all fields of a rating from the rating table
                // add entry with those inputs
                break;
            default:
                return;
        }
    }

    private static void playGame() throws SQLException {
        st = conn.createStatement();
        Scanner scanner = new Scanner(System.in);
        System.out.println("1) Play Game\n2) Play Random Game!\n");
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                searchGames();
                break;
            case 2:
                // random seed and choose game?
                System.out.println("Game you're playing: "); // chosen game
                // mark that game as played and add play time
            default:
                return;
        }

    }

    private static void searchFriends() throws SQLException {
        System.out.println("1) Follow \n 2) Unfollow\n");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        Connection conn = null; // change when we get into postgresSQL
        Statement st = conn.createStatement();

        switch (choice) {
            case 1 -> {
                System.out.println("Enter friend's email address:");
                String email = scanner.next();
                // start SQL codeblock here
                String getUsername = "SELECT username FROM player WHERE email ='" + email + "'";
                ResultSet rs = st.executeQuery(getUsername);
                String makeFriendship = "INSERT INTO friendship VALUES ('" + username + "', '" + rs.getString(1) + "'";
                st.executeQuery(makeFriendship);
                rs.close();
                st.close();
                // end SQL codeblock here

                // if email doesn't exist
                //System.out.println("There is no user associated with this account");
            }
            case 2 -> {
                System.out.println("Enter friend's email address to unfollow:");
                String email = scanner.next();
                // start SQL codeblock here
                String getUsername = "SELECT username FROM player WHERE email ='" + email + "'";
                ResultSet rs = st.executeQuery(getUsername);
                String removeFriendship = "DELETE FROM friendship WHERE username1 = '" + username + "' AND username2 = '" + rs.getString(1) + "'";
                st.executeQuery(removeFriendship);
                rs.close();
                st.close();
                // end SQL codeblock here

                // if email doesn't exist
                //System.out.println("There is no user associated with this account");
            }
            default -> {
                return;
            }
        }
    }

    private static void logOut() {
        username = "";
        System.out.println("You're logged out! Byeeee");
    }


}