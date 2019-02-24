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

    /*
    @Override
    protected SparkApplication[] getApplications(FilterConfig filterConfig) throws ServletException {
        SparkApplication[] applications = new SparkApplication[1];
        System.out.println("Spring application fetched");
        //ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        String e = filterConfig.getInitParameter("applicationClass");
        applications[0] = (SparkApplication) Main.context.getBean(e);
        return applications;
    }
    /*

    @Override
    protected SparkApplication getApplication(FilterConfig filterConfig) throws ServletException {
        System.out.println("Spring application fetched");
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
        String e = filterConfig.getInitParameter("applicationClass");
        return (SparkApplication) context.getBean(e);
    }*/
}