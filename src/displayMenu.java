package src;
import java.sql.SQLException;
import java.util.Scanner;

import static src.PostgresSSH.username;

import static src.PostgresSSH.logOut;

public class displayMenu {

    public static void main(String[] args) throws SQLException {
        menu();

    }

    static void menu() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        int choice;
        while (true) {
            System.out.println("Welcome " + username + "!");
            System.out.print("""
                    Menu:
                    1) Create an account
                    2) Log in
                    3) Display user profile
                    4) View/Edit collections
                    5) Search games
                    6) Play game
                    7) Rate game
                    8) Search friends
                    9) Log Out
                    10) Exit Program
                    >
                    """);
            choice = scanner.nextInt();

            switch (choice) {
                case 1 -> createAccount.createAcc();
                case 2 -> logIn.login();
                case 3 -> userProfile.displayProfile();
                case 4 -> viewEditCollections.viewEditCollections();
                case 5 -> searchGames.search();
                case 6 -> playGame.play();
                case 7 -> rateGame.rate();
                case 8 -> searchFriends.friend();
                case 9 -> logOut();
                case 10 -> {
                    return;
                }
                default -> System.out.println("Invalid Input, please enter a number from the menu.");
            }
        }
    }


}

