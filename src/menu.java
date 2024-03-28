package src;
import java.util.Scanner;

public class menu {
    /**
     * Statement st = conn.createStatement();
     * String query = "SELECT * FROM mytable WHERE columnfoo = 500";
     * ResultSet rs = st.executeQuery(query);
     * while (rs.next()) {
     *     System.out.print("Column 1 returned ");
     *     System.out.println(rs.getString(1));
     * }
     * rs.close();
     * st.close();
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice = 0;
        while (choice != 8) {
            System.out.println("""
                    Menu:
                    1) Create an account
                    2) Log in
                    3) View/Edit collections
                         a) edit collection
                           - edit collection name
                           - delete game
                           - add game
                         b) create collection
                         c) delete collection
                    4) Search games
                    5) Play game
                         a) play random game
                         b) play specific game - make sure player owns platform
                    6) Search friends
                         a) follow
                         b) unfollow
                    7) Log Out
                    8) Exit Program
                    >
                    """);
            choice = scanner.nextInt();
            }
        }
    }

