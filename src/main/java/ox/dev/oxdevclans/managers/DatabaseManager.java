package ox.dev.oxdevclans.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ox.dev.oxdevclans.Oxdev_clans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final Oxdev_clans plugin;
    private HikariDataSource dataSource;

    public DatabaseManager(Oxdev_clans plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        String dbType = plugin.getPluginConfig().getString("databaseType");
        HikariConfig config = new HikariConfig();

        if (dbType.equalsIgnoreCase("SQLITE")) {
            String sqliteFile = plugin.getPluginConfig().getString("sqlite.file");
            config.setJdbcUrl("jdbc:sqlite:" + sqliteFile);
        } else if (dbType.equalsIgnoreCase("MYSQL")) {
            String host = plugin.getPluginConfig().getString("mysql.host");
            String port = plugin.getPluginConfig().getString("mysql.port");
            String database = plugin.getPluginConfig().getString("mysql.database");
            String username = plugin.getPluginConfig().getString("mysql.username");
            String password = plugin.getPluginConfig().getString("mysql.password");

            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        } else {
            throw new SQLException("Unknown database type: " + dbType);
        }

        dataSource = new HikariDataSource(config);
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            throw new SQLException("DataSource is not initialized.");
        }
    }

    public void setupDatabase() throws SQLException {
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS clans (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) UNIQUE, owner VARCHAR(50))")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS clan_members (id INT AUTO_INCREMENT PRIMARY KEY, clan_id INT, member VARCHAR(50), FOREIGN KEY (clan_id) REFERENCES clans(id))")) {
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS player_stats (id INT AUTO_INCREMENT PRIMARY KEY, player VARCHAR(50) UNIQUE, ranking INT, kills INT, deaths INT)")) {
                ps.executeUpdate();
            }
        }
    }
}
