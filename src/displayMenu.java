package src;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

import static src.PostgresSSH.username;

import static src.PostgresSSH.logOut;

public class displayMenu {

    public static void main(String[] args) throws SQLException, ParseException {
        menu();

    }

    static void menu() throws SQLException, ParseException {
        Scanner scanner = new Scanner(System.in);
        int choice;
        while (true) {
            System.out.println("Welcome " + username + "!");
            System.out.print("""
                    Menu:
                    1) Create an account
                    2) Log in
                    3) Display user profile
                    4) Recommendations
                    5) View/Edit collections
                    6) Search games
                    7) Play game
                    8) Rate game
                    9) Search friends
                    10) Log Out
                    11) Exit Program
                    >
                    """);
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> createAccount.createAcc();
                case 2 -> logIn.login();
                case 3 -> userProfile.displayProfile();
                case 4 -> recommendation.recommend();
                case 5 -> viewEditCollections.viewEditCollections();
                case 6 -> searchGames.search();
                case 7 -> playGame.play();
                case 8 -> rateGame.rate();
                case 9 -> searchFriends.friend();
                case 10 -> logOut();
                case 11 -> {
                    return;
                }
                default -> System.out.println("Invalid Input, please enter a number from the menu.");
            }
        }
    }


}

