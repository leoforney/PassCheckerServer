package tk.leoforney.passcheckerserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.sql.Connection;

@Component
public class SpringUtils {

    public static ApplicationContext ctx;
    public static Connection connection;

    @Autowired
    private void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;       
    }

    @Autowired
    public void setConnection(Connection connection) {this.connection = connection;}
}