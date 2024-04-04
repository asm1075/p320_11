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
                case 2 -> logIn.login();
                case 3 -> src.viewEditCollections.viewEditCollections();
                case 4 -> src.searchGames.search();
                case 5 -> src.playGame.play();
                case 6 -> src.rateGame.rate();
                case 7 -> src.searchFriends.friend();
                case 8 -> logOut();
                case 9 -> {
                    return;
                }
                default -> System.out.println("Invalid Input, please enter a number from the menu.");
            }
        }
    }


}

