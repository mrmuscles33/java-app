package fr.amr.database;

import fr.amr.utils.DateUtils;
import fr.amr.utils.StringUtils;
import fr.amr.utils.StructureUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DbMgr {

    public static final String MYSQL = "mysql";
    public static final String SQLSERVER = "sqlserver";
    public static final String ORACLE = "oracle";
    public static final String POSTGRE = "postgre";
    public static final String JAVA = "java";
    public static final String SQLITE = "sqlite";
    private static Connection conn = null;

    private DbMgr() {
        super();
    }

    /**
     * Initialize the connection to the database.
     *
     * @throws NamingException
     * @throws SQLException
     */
    public static void init() throws NamingException, SQLException {
        if (conn != null) {
            return;
        }
        InitialContext ic = new InitialContext();
        DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/db");
        conn = ds.getConnection();
    }

    /**
     * Return the connection to the database.
     *
     * @return The connection
     */
    public static Connection getConnection() {
        return conn;
    }

    /**
     * Set the connection to the database.
     * Used only for testing.
     *
     * @param connection
     */
    public static void setConnection(Connection connection) {
        conn = connection;
    }

    /**
     * Return the list of rows from the database.
     * Multiple rows and multiple columns.
     *
     * @param sql    The SQL query
     * @param params The parameters to replace the ? in the query
     * @return The list of rows
     * @throws SQLException
     */
    public static List<Map<String, String>> getRows(String sql, List<Object> params) throws SQLException {
        // List of rows
        List<Map<String, String>> rows = new ArrayList<>();

        // Create the statement
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Replace the ? with the parameters
        params = params == null ? new ArrayList<>() : params;
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) == null) {
                stmt.setNull(i + 1, Types.NULL);
                continue;
            }
            switch (params.get(i).getClass().getSimpleName()) {
                case "Integer":
                    stmt.setInt(i + 1, (Integer) params.get(i));
                    break;
                case "Double":
                    stmt.setDouble(i + 1, (Double) params.get(i));
                    break;
                default:
                    stmt.setString(i + 1, StringUtils.toString(params.get(i)));
                    break;
            }
        }
        // Execute the query
        ResultSet rs = stmt.executeQuery();
        if (rs == null) {
            return rows;
        }

        // Get the columns
        Map<String, String> colType = new HashMap<>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            colType.put(metaData.getColumnName(i), metaData.getColumnTypeName(i));
        }

        // Get the rows
        Map<String, String> row;
        while (rs.next()) {
            row = new HashMap<>();
            for (String col : colType.keySet()) {
                row.put(col, rs.getString(col));
            }
            rows.add(row);
        }

        return rows;
    }

    /**
     * Return the list of rows from the database.
     * One row and multiple columns.
     *
     * @param sql The SQL query
     * @return The list of rows
     * @throws SQLException
     */
    public static Map<String, String> getRow(String sql, List<Object> params) throws SQLException {
        List<Map<String, String>> rows = getRows(sql, params);
        if (rows.isEmpty()) {
            return new HashMap<>();
        }
        return rows.get(0);
    }

    /**
     * Return the list of values from the database.
     * One column and multiple rows.
     *
     * @param sql    The SQL query
     * @param params The parameters to replace the ? in the query
     * @return The list of values
     * @throws SQLException
     */
    public static List<String> getColumn(String sql, List<Object> params) throws SQLException {
        List<Map<String, String>> rows = getRows(sql, params);
        if (rows.isEmpty()) {
            return new ArrayList<>();
        }
        // get the first column name
        String colName = rows.get(0).keySet().iterator().next();

        // return the list of values for this column
        return rows.stream().map(row -> row.get(colName)).toList();
    }

    /**
     * Return the value from the database.
     * One column and one row.
     *
     * @param sql    The SQL query
     * @param params The parameters to replace the ? in the query
     * @return The value
     * @throws SQLException
     */
    public static String getValue(String sql, List<Object> params) throws SQLException {
        List<String> values = getColumn(sql, params);
        if (values.isEmpty()) {
            return "";
        }
        return values.get(0);
    }

    /**
     * Return true if the row exists in the database.
     *
     * @param sql    The SQL query
     * @param params The parameters to replace the ? in the query
     * @return True if the row exists, false otherwise
     * @throws SQLException
     */
    public static boolean exists(String sql, List<Object> params) throws SQLException {
        return !getRows(sql, params).isEmpty();
    }

    /**
     * Execute the SQL query (INSERT, UPDATE, DELETE).
     *
     * @param sql    The SQL query
     * @param params The parameters to replace the ? in the query
     * @throws SQLException
     */
    public static void execute(String sql, List<Object> params) throws SQLException {
        // Create the statement
        PreparedStatement stmt = conn.prepareStatement(sql);

        // Replace the ? with the parameters
        for (int i = 0; i < params.size(); i++) {
            if (params.get(i) == null) {
                stmt.setNull(i + 1, Types.NULL);
                continue;
            }
            switch (params.get(i).getClass().getSimpleName()) {
                case "Integer":
                    stmt.setInt(i + 1, (Integer) params.get(i));
                    break;
                case "Double":
                    stmt.setDouble(i + 1, (Double) params.get(i));
                    break;
                default:
                    stmt.setString(i + 1, StringUtils.toString(params.get(i)));
                    break;
            }
        }
        // Execute the query
        stmt.executeUpdate();
    }

    /**
     * Insert a simple row in the database.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The row to insert
     * @throws SQLException
     */
    public static void insert(String table, Map<String, Object> values) throws SQLException {
        insert(table, values, new HashMap<>());
    }

    /**
     * Insert a simple row in the database.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The row to insert
     * @throws SQLException
     */
    public static void insert(String table, Map<String, Object> values, Map<String, String> dateFormat) throws SQLException {
        // Sort the map by key
        Map<String, Object> sortedMap = StructureUtils.sortMap(values);

        // Create the statement
        String sql = "INSERT INTO " + table + " (" +
                String.join(",", sortedMap.keySet()) +
                ") VALUES (" +
                sortedMap
                        .keySet()
                        .stream()
                        .map(o -> dateFormat.containsKey(o) ?
                                DbMgr.toDate("?", dateFormat.get(o)) :
                                "?"
                        )
                        .collect(Collectors.joining(","))
                + ")";
        execute(sql, new ArrayList<>(sortedMap.values()));
    }

    /**
     * Insert multiple rows in the database by block of 100 rows.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The rows to insert
     * @throws SQLException
     */
    public static void insert(String table, List<Map<String, Object>> values) throws SQLException {
        insert(table, values, new HashMap<>());
    }

    /**
     * Insert multiple rows in the database by block of 100 rows.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The rows to insert
     * @throws SQLException
     */
    public static void insert(String table, List<Map<String, Object>> values, Map<String, String> dateFormat) throws SQLException {
        // All columns
        Set<String> columns = values.stream().flatMap(row -> row.keySet().stream()).collect(Collectors.toSet());

        // If some rows have missing columns, complete them with empty values
        values.forEach(row -> columns.forEach(col -> row.putIfAbsent(col, "")));

        // Split the list of rows in blocks of 100 rows
        List<List<Map<String, Object>>> blocks = new ArrayList<>();
        for (int i = 0; i < values.size(); i += 100) {
            blocks.add(values.subList(i, Math.min(i + 100, values.size())));
        }

        // INSERT INTO table (col1, col2, ...)
        String sqlInsert = "INSERT INTO " + table + " (" + String.join(",",
                columns.stream().sorted().toList()) +
                ") ";

        // SELECT ?, ?, ... FROM DUAL
        String sqlSelectTpl = "SELECT " +
                // ?, TO_DATE(?, '...'), ...
                String.join(",",
                        columns
                                .stream()
                                .map(col -> dateFormat.containsKey(col) ?
                                        DbMgr.toDate("?", dateFormat.get(col)) :
                                        "?"
                                )
                                .toList())
                + " FROM DUAL";
        String sql;
        List<Object> params;
        // Insert each block
        for (List<Map<String, Object>> block : blocks) {
            // INSERT INTO table (col1, col2, ...) SELECT ?, ?, ... FROM DUAL UNION ALL SELECT ?, ?, ... FROM DUAL
            sql = sqlInsert + String.join(" UNION ALL ", Collections.nCopies(block.size(), sqlSelectTpl));

            // Set params
            params = block.stream().map(StructureUtils::sortMap).flatMap(row -> row.values().stream()).toList();
            execute(sql, params);
        }
    }

    /**
     * Update a row in the database.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The columns and values to update
     * @param keys   The columns to identify the row
     * @throws SQLException
     */
    public static void update(String table, Map<String, Object> values, List<String> keys) throws SQLException {
        update(table, values, keys, new HashMap<>());
    }

    /**
     * Update a row in the database.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The columns and values to update
     * @param keys   The columns to identify the row
     * @throws SQLException
     */
    public static void update(String table, Map<String, Object> values, List<String> keys, Map<String, String> dateFormat) throws SQLException {
        // Sort the map by key
        Map<String, Object> sortedMap = StructureUtils.sortMap(values);

        // Create the statement
        String sql = "UPDATE " + table + " SET " +
                // col1 = ?, col2 = TO_DATE(?,'...'), ...
                String.join(", ",
                        sortedMap
                                .keySet()
                                .stream()
                                .map(col -> dateFormat.containsKey(col) ?
                                        col + " = " + DbMgr.toDate("?", dateFormat.get(col)) :
                                        col + " = ?"
                                )
                                .toList()
                ) + " WHERE " +
                // key1 = ? AND key2 = TO_DATE(?,'...') AND ...
                String.join(" AND ",
                        keys
                                .stream()
                                .sorted()
                                .map(key -> dateFormat.containsKey(key) ?
                                        key + " = " + DbMgr.toDate("?", dateFormat.get(key)) :
                                        key + " = ?"
                                )
                                .toList()
                );

        // Set params
        List<Object> params = new ArrayList<>(sortedMap.values());
        params.addAll(keys.stream().sorted().map(values::get).toList());
        execute(sql, params);
    }

    /**
     * Delete a row in the database.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The values to identify the row
     * @param keys   The columns to identify the row
     * @throws SQLException
     */
    public static void delete(String table, Map<String, Object> values, List<String> keys) throws SQLException {
        delete(table, values, keys, new HashMap<>());
    }

    /**
     * Delete a row in the database.
     * Be careful to date columns.
     *
     * @param table      The table name
     * @param values     The values to identify the row
     * @param keys       The columns to identify the row
     * @param dateFormat The date format for each column
     * @throws SQLException
     */
    public static void delete(String table, Map<String, Object> values, List<String> keys, Map<String, String> dateFormat) throws SQLException {
        // Create the statement
        String sql = "DELETE FROM " + table + " WHERE " +
                // key1 = ? AND key2 = TO_DATE(?,'...') AND ...
                String.join(" AND ", keys
                        .stream()
                        .map(key -> dateFormat.containsKey(key) ?
                                key + " = " + DbMgr.toDate("?", dateFormat.get(key)) :
                                key + " = ?"
                        )
                        .toList()
                );

        // Set params
        List<Object> params = keys.stream().map(values::get).toList();
        execute(sql, params);
    }

    /**
     * Delete multiple rows in the database by block of 100 rows.
     * Be careful to date columns.
     *
     * @param table  The table name
     * @param values The rows to delete
     * @param keys   The columns to identify the row
     * @throws SQLException
     */
    public static void delete(String table, List<Map<String, Object>> values, List<String> keys) throws SQLException {
        delete(table, values, keys, new HashMap<>());
    }

    /**
     * Delete multiple rows in the database by block of 100 rows.
     * Be careful to date columns.
     *
     * @param table      The table name
     * @param values     The rows to delete
     * @param keys       The columns to identify the row
     * @param dateFormat The date format for each column
     * @throws SQLException
     */
    public static void delete(String table, List<Map<String, Object>> values, List<String> keys, Map<String, String> dateFormat) throws SQLException {
        // Split the list of rows in blocks of 100 rows
        List<List<Map<String, Object>>> blocks = new ArrayList<>();
        for (int i = 0; i < values.size(); i += 100) {
            blocks.add(values.subList(i, Math.min(i + 100, values.size())));
        }

        // DELETE FROM table WHERE (key1, key2, ...) IN (SELECT ?, ?, ... FROM DUAL UNION ALL SELECT ?, ?, ... FROM DUAL)
        List<Object> params;
        // Delete each block
        for (List<Map<String, Object>> block : blocks) {
            String sql = "DELETE FROM " + table + " WHERE (" +
                    String.join(",", keys.stream().sorted().toList()) +
                    ") IN (" +
                    String.join(
                            " UNION ALL ",
                            Collections.nCopies(
                                    block.size(),
                                    "SELECT " + keys
                                            .stream()
                                            .sorted()
                                            .map(key -> dateFormat.containsKey(key) ?
                                                    DbMgr.toDate("?", dateFormat.get(key)) :
                                                    "?"
                                            )
                                            .collect(Collectors.joining(","))
                                            + " FROM DUAL"
                            )
                    ) + ")";
            // Set params using the keys
            params = block.stream().flatMap(row -> keys.stream().sorted().map(row::get)).toList();
            execute(sql, params);
        }
    }

    /**
     * Return the SQL command to convert a date to a string according to the database.
     *
     * @param col    The column
     * @param format The format
     * @return The SQL command
     */
    public static String toChar(String col, String format) {
        return toChar(col, format, getConnection());
    }

    /**
     * Return the SQL command to convert a date to a string according to the database.
     *
     * @param col    The column
     * @param format The format
     * @param conn   The connection
     * @return The SQL command
     */
    public static String toChar(String col, String format, Connection conn) {
        String db = getDbProductName(conn);
        return switch (db.toLowerCase()) {
            case MYSQL -> "DATE_FORMAT(" + col + ",'" + format + "')";
            case SQLSERVER -> "FORMAT(" + col + ",'" + format + "')";
            case SQLITE -> "STRFTIME('" + format + "', " + col + ")";
            default -> "TO_CHAR(" + col + ",'" + format + "')";
        };
    }

    /**
     * Return the SQL command to convert a string to a date according to the database.
     *
     * @param col    The column
     * @param format The format
     * @return The SQL command
     */
    public static String toDate(String col, String format) {
        return toDate(col, format, getConnection());
    }

    /**
     * Return the SQL command to convert a string to a date according to the database.
     *
     * @param col        The column
     * @param formatJava The format
     * @param conn       The connection
     * @return The SQL command
     */
    public static String toDate(String col, String formatJava, Connection conn) {
        String db = getDbProductName(conn);
        String newFormat = DateUtils.transcodeFormat(formatJava, JAVA, db);
        return switch (db.toLowerCase()) {
            case MYSQL -> "STR_TO_DATE(" + col + ",'" + newFormat + "')";
            case SQLSERVER -> "CAST(" + col + " AS DATETIME)";
            case SQLITE -> "DATE(" + col + ")";
            default -> "TO_DATE(" + col + ",'" + newFormat + "')";
        };
    }

    /**
     * Return the SQL command to convert a string to a date according to the database.
     *
     * @return The SQL command
     */
    public static String sysdate() {
        return sysdate(getConnection());
    }

    /**
     * Return the SQL command to get the current date according to the database.
     *
     * @param conn The connection
     * @return The SQL command
     */
    public static String sysdate(Connection conn) {
        String db = getDbProductName(conn);
        return switch (db.toLowerCase()) {
            case MYSQL -> "NOW()";
            case SQLSERVER -> "GETDATE()";
            case SQLITE -> "DATETIME('now')";
            default -> "SYSDATE";
        };
    }

    /**
     * Return the database product name.
     *
     * @return The database product name
     * @throws SQLException
     */
    public static String getDbProductName() {
        return getDbProductName(getConnection());
    }

    /**
     * Return the database product name.
     *
     * @param conn The connection
     * @return The database product name
     * @throws SQLException
     */
    public static String getDbProductName(Connection conn) {
        String db;
        try {
            db = StringUtils.toString(conn.getMetaData().getDatabaseProductName()).toLowerCase();
        } catch (SQLException e) {
            return ORACLE;
        }
        if (db.contains(MYSQL)) {
            return MYSQL;
        } else if (db.contains("sql server")) {
            return SQLSERVER;
        } else if (db.contains(ORACLE)) {
            return ORACLE;
        } else if (db.contains(POSTGRE)) {
            return POSTGRE;
        } else if (db.contains(SQLITE)) {
            return SQLITE;
        }
        return ORACLE;
    }
}
