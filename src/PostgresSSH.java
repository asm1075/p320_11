package src;
import com.jcraft.jsch.*;

import java.sql.*;
import java.text.ParseException;
import java.util.Properties;

import static src.displayMenu.menu;


public class PostgresSSH {
    static String username = "";
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

            // Display menu
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

    static int getGC_ID(String gc_name) throws SQLException, ParseException {
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

    static int getVG_ID(String vg_name) throws SQLException, ParseException {
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

    static String getVGName(int vg_id) throws SQLException, ParseException {
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

    static void logOut() {
        username = "";
        System.out.println("You're logged out! Goodbye!");
    }

    static boolean checkLoggedIn() {
        if (username.equals("")) {
            System.out.println("You are not logged in. " +
                    "Please log in or create an account before continuing.\n");
            return false;
        }
        return true;
    }
}