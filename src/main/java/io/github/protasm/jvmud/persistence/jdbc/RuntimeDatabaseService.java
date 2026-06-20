package io.github.protasm.jvmud.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JDBC-backed database handle manager for JVMud-native database efuns.
 *
 * <p>The service intentionally exposes JVMud operations such as {@code jvmud_db_exec} rather than
 * legacy driver names. Mudlib compatibility objects can translate spellings like {@code db_exec}
 * onto these methods while JVMud owns only the generic runtime contract: handles identify open
 * connections, one result cursor is retained per handle, SQL errors are queryable, and fetched rows
 * are returned as LPC arrays.</p>
 */
public final class RuntimeDatabaseService {
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private int nextHandle = 1;
    private final Map<Integer, DatabaseHandle> handles = new LinkedHashMap<>();

    /** Configures the JDBC connection settings used by subsequent database connections. */
    public void configure(String jdbcUrl, String jdbcUser, String jdbcPassword) {
        this.jdbcUrl = normalize(jdbcUrl);
        this.jdbcUser = normalize(jdbcUser);
        this.jdbcPassword = jdbcPassword != null ? jdbcPassword : "";
    }

    /** Opens a connection and returns its positive handle, or {@code 0} when no JDBC URL is configured. */
    public int connect(String databaseName, String user, String password) {
        if (jdbcUrl == null) {
            return 0;
        }

        String effectiveUser = normalize(user) != null ? normalize(user) : jdbcUser;
        String effectivePassword = normalize(user) != null ? (password != null ? password : "") : jdbcPassword;
        try {
            Connection connection = effectiveUser != null
                    ? DriverManager.getConnection(jdbcUrl, effectiveUser, effectivePassword)
                    : DriverManager.getConnection(jdbcUrl);
            int handle = nextHandle++;
            handles.put(handle, new DatabaseHandle(connection));
            return handle;
        } catch (SQLException e) {
            return 0;
        }
    }

    /** Executes one SQL statement against an open handle and returns that handle on success. */
    public int execute(int handle, String sql) {
        DatabaseHandle databaseHandle = handles.get(handle);
        if (databaseHandle == null) {
            return 0;
        }

        databaseHandle.closeCursor();
        databaseHandle.lastError = null;
        try {
            Statement statement = databaseHandle.connection.createStatement();
            boolean hasResultSet = statement.execute(sql);
            if (hasResultSet) {
                databaseHandle.statement = statement;
                databaseHandle.resultSet = statement.getResultSet();
            } else {
                statement.close();
            }
            return handle;
        } catch (SQLException e) {
            databaseHandle.lastError = e.getMessage();
            return handle;
        }
    }

    /** Fetches the next row from the handle's current result cursor, or LPC false when exhausted. */
    public Object fetch(int handle) {
        DatabaseHandle databaseHandle = handles.get(handle);
        if (databaseHandle == null || databaseHandle.resultSet == null) {
            return 0;
        }

        try {
            if (!databaseHandle.resultSet.next()) {
                databaseHandle.closeCursor();
                return 0;
            }

            ResultSetMetaData metadata = databaseHandle.resultSet.getMetaData();
            List<Object> row = new ArrayList<>(metadata.getColumnCount());
            for (int column = 1; column <= metadata.getColumnCount(); column++) {
                row.add(toLpcValue(databaseHandle.resultSet.getObject(column)));
            }
            return row;
        } catch (SQLException e) {
            databaseHandle.lastError = e.getMessage();
            databaseHandle.closeCursor();
            return 0;
        }
    }

    /** Returns the last SQL error for an open handle, or LPC false when no error is pending. */
    public Object error(int handle) {
        DatabaseHandle databaseHandle = handles.get(handle);
        if (databaseHandle == null || databaseHandle.lastError == null || databaseHandle.lastError.isBlank()) {
            return 0;
        }
        return databaseHandle.lastError;
    }

    /** Closes an open handle and returns LPC status. */
    public int close(int handle) {
        DatabaseHandle databaseHandle = handles.remove(handle);
        if (databaseHandle == null) {
            return 0;
        }
        databaseHandle.close();
        return 1;
    }

    /** Returns the currently open database handles as an LPC array. */
    public List<Integer> handles() {
        return new ArrayList<>(handles.keySet());
    }

    /** Escapes a string for interpolation into Realms-style SQL text. */
    public String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("'", "\\'");
    }

    private static Object toLpcValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue ? 1 : 0;
        }
        return value;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static final class DatabaseHandle {
        private final Connection connection;
        private Statement statement;
        private ResultSet resultSet;
        private String lastError;

        private DatabaseHandle(Connection connection) {
            this.connection = Objects.requireNonNull(connection, "connection");
        }

        private void closeCursor() {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
            } catch (SQLException ignored) {
                // Closing a stale cursor is best-effort cleanup.
            } finally {
                resultSet = null;
            }

            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException ignored) {
                // Closing a stale statement is best-effort cleanup.
            } finally {
                statement = null;
            }
        }

        private void close() {
            closeCursor();
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Closing a stale connection is best-effort cleanup.
            }
        }
    }
}
