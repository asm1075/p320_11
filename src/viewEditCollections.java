package src;

import org.postgresql.util.PSQLException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

import static src.PostgresSSH.*;

public class viewEditCollections {

    public static void main(String[] args) throws SQLException, ParseException {
        viewEditCollections();
    }

    static void viewEditCollections() throws SQLException, ParseException {
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
                query = "SELECT MAX(gc_id) FROM game_collection";
                rs = st.executeQuery(query);
                int gc_id = 0;
                if(rs.next()) // increment the thing
                    gc_id = rs.getInt(1) + 1;
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
}
