package tk.leoforney.passcheckerserver;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import static tk.leoforney.passcheckerserver.PhotoManagement.uploadDir;

@Configuration
@EnableWebMvc
public class StaticResourceConfiguration implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String os = System.getProperty("os.name");
        String path = (os.toLowerCase().contains("win") ? "///" : "") + uploadDir.getAbsolutePath() + File.separator;
        registry.addResourceHandler("/submissions/**").addResourceLocations("file:" + path);
    }
}