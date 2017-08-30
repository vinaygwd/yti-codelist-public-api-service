package fi.vm.yti.cls.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;


@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class })
@ComponentScan("fi.vm.yti.cls.api")
public class PublicApiServiceApplication {

    private static final Logger LOG = LoggerFactory.getLogger(PublicApiServiceApplication.class);


    public PublicApiServiceApplication() {

    }


    public static void main(final String[] args) {

        final ApplicationContext context = SpringApplication.run(PublicApiServiceApplication.class, args);

        final AppInitializer serviceInitializer = (AppInitializer) context.getBean(AppInitializer.class);
        serviceInitializer.initialize();

    }

}
