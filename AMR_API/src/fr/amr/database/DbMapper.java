package fr.amr.database;

import fr.amr.utils.DateUtils;
import fr.amr.utils.StringUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DbMapper {

    private final List<String> columns = getColumns();
    private final List<String> keys = getKeys();
    private final List<String> dateColumns = getDateColumns();

    protected abstract String getTable();

    protected abstract List<String> getKeys();

    protected abstract List<Object> getKeysValues();

    protected abstract List<String> getColumns();

    protected abstract List<Object> getColumnsValues();

    public boolean setValue(String pName, Object pValue) {
        return false;
    }

    /**
     * Return the clause where to identify this item
     * col1 = ? AND col2 = TO_DATE(?,'dd/MM/yyyy') AND col3 = ? AND ...
     *
     * @return
     */
    public String getWhere() {
        return keys.stream().map(key -> key + " = " + (dateColumns.contains(key) ? "TO_DATE(?, '" + this.getDateFormat(key) + "')" : "?")).collect(Collectors.joining(" AND "));
    }

    /**
     * Return the clause where to identify this item with the values
     * col1 = ? AND col2 = TO_DATE(?,'dd/MM/yyyy') AND col3 = ? AND ...
     *
     * @param params The list of values
     * @return The clause where
     */
    public final String getWhere(List<Object> params) {
        params.addAll(this.getKeysValues());
        return this.getWhere();
    }

    /**
     * Return the clause insert
     * ?, TO_DATE(?,'dd/MM/yyyy'), ?, ...
     *
     * @return The clause insert
     */
    public String getInsert() {
        return columns.stream().map(col -> (dateColumns.contains(col) ? "TO_DATE(?,'" + this.getDateFormat(col) + "')" : "?")).collect(Collectors.joining(","));
    }

    /**
     * Return the clause insert with the values
     *
     * @param params The list of values
     * @return The clause insert
     */
    public String getInsert(List<Object> params) {
        params.addAll(this.getColumnsValues());
        return this.getInsert();
    }

    /**
     * Return the clause update
     * col1 = ?, col2 = TO_DATE(?,'dd/MM/yyyy'), col3 = ?, ...
     *
     * @return The clause update
     */
    public String getUpdate() {
        return columns.stream().map(col -> col + " = " + (dateColumns.contains(col) ? "TO_DATE(?,'" + this.getDateFormat(col) + "')" : "?")).collect(Collectors.joining(","));
    }

    /**
     * Return the clause update with the values
     * col1 = ?, col2 = TO_DATE(?,'dd/MM/yyyy'), col3 = ?, ...
     *
     * @param params
     * @return
     */
    public String getUpdate(List<Object> params) {
        params.addAll(this.getColumnsValues());
        return this.getUpdate();
    }

    /**
     * Return the clause select
     * col1, TO_CHAR(col2,'dd/MM/yyyy') AS col2, col3, ...
     *
     * @return The clause select
     */
    public String getSelect() {
        return columns.stream().map(col -> dateColumns.contains(col) ? "TO_CHAR(" + col + ",'" + this.getDateFormat(col) + "') AS " + col : col).collect(Collectors.joining(","));
    }

    /**
     * List of date columns
     *
     * @return The list of date columns
     */
    protected List<String> getDateColumns() {
        return new ArrayList<>();
    }

    /**
     * Return the date format for the given column
     *
     * @param column The column
     * @return The date format
     */
    protected String getDateFormat(String column) {
        return !StringUtils.isEmpty(column) ? DateUtils.ORA_D_M_Y_H_M_S : "";
    }

    /**
     * Return the date format for all date columns
     *
     * @return The dates format
     */
    private Map<String, String> getDateFormat() {
        return dateColumns.stream().collect(Collectors.toMap(Function.identity(), this::getDateFormat));
    }

    /**
     * Fill the object from the map
     *
     * @param map The map
     */
    protected abstract void fromMap(Map<String, ?> map);

    /**
     * Create a map from the object
     *
     * @return The map
     */
    protected abstract Map<String, Object> toMap();

    /**
     * Load the object from the database
     *
     * @throws SQLException
     */
    public void load() throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT " + this.getSelect() + " FROM " + this.getTable() + " WHERE " + this.getWhere(params);
        Map<String, String> row = DbMgr.getRow(sql, params);
        this.fromMap(row);
    }

    /**
     * Insert the object in the database
     *
     * @throws SQLException
     */
    public void insert() throws SQLException {
        DbMgr.insert(this.getTable(), this.toMap(), this.getDateFormat());
    }

    /**
     * Update the object in the database
     *
     * @throws SQLException
     */
    public void update() throws SQLException {
        DbMgr.update(this.getTable(), this.toMap(), this.getKeys(), this.getDateFormat());
    }

    /**
     * Delete the object in the database
     *
     * @throws SQLException
     */
    public void delete() throws SQLException {
        DbMgr.delete(this.getTable(), this.toMap(), this.getKeys(), this.getDateFormat());
    }

    /**
     * Return the object as a string
     *
     * @return The object as a string
     */
    public String toString() {
        return this.toMap().toString();
    }
}



