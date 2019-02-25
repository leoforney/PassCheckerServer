package tk.leoforney.passcheckerserver;

import spark.servlet.SparkApplication;
import spark.servlet.SparkFilter;

import javax.servlet.ServletException;

public class SpringifiedSparkFilter extends SparkFilter {

    @Override
    protected SparkApplication getApplication(String applicationClassName) throws ServletException {
        try {
            Class<?> applicationClass = Class.forName(applicationClassName);
            return (SparkApplication) applicationClass.newInstance();
        } catch (Exception exc) {
            throw new ServletException(exc);
        }
    }

}