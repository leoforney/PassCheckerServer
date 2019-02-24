package tk.leoforney.passcheckerserver.web;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import spark.servlet.SparkFilter;
import tk.leoforney.passcheckerserver.*;

@Configuration
public class SpringConfiguration {

    @Bean
    public FilterRegistrationBean sparkFilterRegistration() {

        SparkFilter filter = new SpringifiedSparkFilter();

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        for (String path: UserManagement.PATHS) {
            registration.addUrlPatterns(path);
        }
        for (String path: PhotoManagement.PATHS) {
            registration.addUrlPatterns(path);
        }
        for (String path: PassManagement.PATHS) {
            registration.addUrlPatterns(path);
        }
        for (String path: Runner.PATHS) {
            registration.addUrlPatterns(path);
        }
        registration.addUrlPatterns("");
        registration.addInitParameter("applicationClass", Runner.class.getCanonicalName());
        registration.setName("SparkFilter");
        registration.setOrder(1);

        return registration;
    }

}
