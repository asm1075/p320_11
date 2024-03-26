package src;
import java.util.Scanner;

import static javafx.application.Platform.exit;

/**
 * ResultSet resultSet;
 * Statement statement = connection.createStatement()) {
 *
 *             // Create and execute a SELECT SQL statement.
 *             String selectSql = "SELECT TOP 10 Title, FirstName, LastName from SalesLT.Customer";
 *             resultSet = statement.executeQuery(selectSql);
 *
 *             // Print results from select statement
 *             while (resultSet.next()) {
 *                 System.out.println(resultSet.getString(2) + " " + resultSet.getString(3));
 */

public class menu {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        while (choice != 8) {
            System.out.println("Menu:\n" +
                    "1) Create an account\n" +
                    "2) Log in\n" +
                    "3) View/Edit collections\n" +
                    "     a) edit collection\n" +
                    "       - edit collection name\n" +
                    "        - delete game\n" +
                    "     b) create collection\n" +
                    "     c) delete collection\n" +
                    "4) Search games\n" +
                    "     a) sort by\n" +
                    "     b) add game to collection\n" +
                    "     c) play game - make sure player owns platform\n" +
                    "     d) rate game\n" +
                    "5) Play game\n" +
                    "     a) play random game\n" +
                    "     b) search game --> (aka goes to menu option 4) \n" +
                    "6) Search friends\n" +
                    "     a) follow\n" +
                    "     b) unfollow\n" +
                    "7) Log Out\n" +
                    "8) Exit Program\n");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    createAccount();
                    break;
                case 2:
                    logIn();
                    break;
                case 3:
                    viewEditCollections();
                    break;
                case 4:
                    searchGames();
                    break;
                case 5:
                    playGame();
                    break;
                case 6:
                    searchFriends();
                    break;
                case 7:
                    logOut();
                    break;
                case 8:
                    exit();
                    break;
                default:
                    System.out.println("Invalid Input, please enter a number from the menu.");
            }
        }
    }

    private static void createAccount() {
        System.out.println("Creating an account for the user!");
        // prompt for all info required for player (besides date accessed and date created)
        // check if primary key exists in table and if so, make user choose a new username
        // insert new entry into player table
            // update date created and date accessed with current datetime
    }

    private static void logIn() {
        System.out.println("User exists, log into their account");
        // store user info in tuple and search entry by primary key
        // set static user variable with player's primary key (so program knows which player this is)
        // update date accessed with current datetime
        System.out.println("Welcome" + player.name + ". You are logged in!");
    }

    private static void viewEditCollections() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Your Collections: \n");
        int choice = 0;
        System.out.println("1) View collection\n 2) Edit Collection\n 3) Create Collection" +
                "\n 4) Delete Collection\n");
        choice = scanner.nextInt();
        switch (choice) {
            case 1:
                // check static user variable and print all collections related with it
                // printed in ascending order by name
                // display number of games in collection and total play time in hours: minutes
                break;
            case 2:
                System.out.println("1) Edit Collection Name\n 2) Delete Game\n");
                choice = scanner.nextInt();
                if (choice == 1) {
                    // edit collection table entry name
                } else if (choice == 2) {
                    // prompt for which game by primary key of game
                    // delete that game from the collection
                } else {
                    System.out.println("Not an option, womp womp.");
                }
                break;
            case 3:
                System.out.println("Enter collection name: ");
                String collectionName = scanner.next();
                // make collection with this name and make it empty (no games)
                break;
            case 4:
                System.out.println("Which collection would you like to delete?");
                // prompt for which game by primary key of collection
                // delete that collection
                break;
            default:
                // go back to main menu because you suck for not entering something correctly
                return;
        }
    }

    private static void searchGames() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Search: \n");
        String searchWord = scanner.next();

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

    private static void playGame() {
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

    private static void searchFriends() {
        System.out.println("1) Follow \n 2) Unfollow\n");
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        switch (choice) {
            case 1:
                System.out.println("Enter friend's email address:");
                // if email exists and player associated
                    // add to friend of static user
                // if email doesn't exist
                System.out.println("There is no user associated with this account");
                break;
            case 2:
                System.out.println("Enter friend's email address to unfollow:");
                // if email exists and player associated
                    // delete friend of static user
                // if email doesn't exist
                System.out.println("There is no user associated with this account");
                break;
            default:
                return;
        }
    }

    private static void logOut() {
        // set static user variable back to null
        System.out.println("You're logged out! Byeeee");
    }


}

