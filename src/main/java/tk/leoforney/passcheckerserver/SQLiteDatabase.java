package tk.leoforney.passcheckerserver;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tk.leoforney.passcheckerserver.Main.wd;

@Configuration
public class SQLiteDatabase {

    private static final Logger logger = Logger.getLogger(SQLiteDatabase.class.getName());

    @Bean(autowire = Autowire.BY_TYPE)
    Connection sqlConnection() {
        logger.log(Level.INFO, "Creating SQLite Connection");
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
