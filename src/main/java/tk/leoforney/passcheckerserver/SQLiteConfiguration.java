package tk.leoforney.passcheckerserver;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static tk.leoforney.passcheckerserver.Main.wd;

@Configuration
public class SQLiteConfiguration {

    @Bean
    public Connection sqliteConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + wd + File.separator + "PassCheckerDatabase.db");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (connection != null) {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connection;
    }

}
