package phasecommit.sqlitelib;

import java.io.File;
import java.sql.*;

public class SqliteDB {
    /**
     * Database home directory.
     */
    private static final String dbPath = "db/";

    /**
     * Database name.
     */
    private String dbName;

    /**
     * Default constructor.
     * @param dbName - Database name
     */
    public SqliteDB(String dbName) {
        this.dbName = dbName;
        createDB();
    }

    /**
     * Connects to database db and returns the connection.
     *
     * @return Connection - Connection to dbName
     */
    private Connection connect() {
        String url = "jdbc:sqlite:" + dbPath + dbName;
        Connection con = null;
        try {
            con = DriverManager.getConnection(url);
        }
        catch (SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
        }

        return con;
    }

    /**
     * Create database dbName.
     *
     * @return int - 0 - Success
     *               1 - Failure
     */
    public int createDB() {
        int retval = 0;

        File dbFile = new File(dbPath + dbName);
        if (!dbFile.exists()) {
            System.out.println("Database does not exist. Creating one.");
            try(Connection con = this.connect()) {
                if (con != null) {
                    DatabaseMetaData meta = con.getMetaData();
                    System.out.println("A new database has been created");
                }
            }
            catch (SQLException e) {
                System.out.println("ERROR: " + e.getMessage());
                retval = 1;
            }
        }
        else {
            System.out.println("Database " + dbName + " found and exists.");
        }

        String sql = "CREATE TABLE IF NOT EXISTS data_map (\n"
                + " data_key integer PRIMARY KEY, \n"
                + " data_val text \n"
                + " );";
        try (Connection con = this.connect();
             Statement stmt = con.createStatement()) {
            stmt.execute(sql);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
            retval = 1;
        }

        return retval;
    }

    /**
     * Stores the given key and value in database.
     *
     * @param key - Integer key
     * @param value - Value corresponding to key
     * @return int - >=0 - Success
     *               -1 - Failure
     */
    public int put(int key, String value) {
        int retval = 0;

        String sql = "INSERT INTO data_map(data_key, data_val) VALUES(?, ?)";

        try (Connection con = this.connect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, key);
            pstmt.setString(2, value);
            retval = pstmt.executeUpdate();
        }
        catch (SQLException e) {
            retval = -1;
        }

        return retval;
    }

    /**
     * Deletes the key-value pair associated with given
     * key.
     *
     * @param key - Key to be deleted
     * @return int - >=0 - Success
     *               -1 - Failure
     */
    public int del(int key) {
        int retval = 0;

        String sql = "DELETE FROM data_map WHERE data_key = ?";

        try (Connection con = this.connect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, key);
            retval = pstmt.executeUpdate();
        }
        catch (SQLException e) {
            retval = -1;
        }

        return retval;
    }

    /**
     * Returns the value corresponding to given key.
     *
     * @param key - Key of value to be returned
     * @return String - Value corresponding to given key
     */
    public String get(int key) {
        String sql = "SELECT data_val FROM data_map WHERE data_key = ?";

        String value = null;
        try (Connection con = this.connect();
             PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, key);
            ResultSet rs = pstmt.executeQuery();

            if (!rs.isClosed()) {
                value = rs.getString("data_val");
                rs.close();
            }
        }
        catch (SQLException e) {
            value = null;
        }

        return value;
    }

    /**
     * Delete associated database (dbName).
     */
    public void deleteDB() {
        File dbFile = new File(dbPath + dbName);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

}
