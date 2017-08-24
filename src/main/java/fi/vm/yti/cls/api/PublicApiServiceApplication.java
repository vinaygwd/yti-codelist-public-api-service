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

    public static final String APP_VERSION = PublicApiServiceApplication.class.getPackage().getImplementationVersion();


    public PublicApiServiceApplication() {

    }


    public static void main(final String[] args) {

        final ApplicationContext context = SpringApplication.run(PublicApiServiceApplication.class, args);

        printLogo();

        final AppInitializer serviceInitializer = (AppInitializer) context.getBean(AppInitializer.class);
        serviceInitializer.initialize();

    }


    /**
     * Application logo printout to log.
     */
    private static void printLogo() {

        LOG.info("");
        LOG.info("       .__                                 ___.   .__  .__        ");
        LOG.info("  ____ |  |   ______           ______  __ _\\_ |__ |  | |__| ____  ");
        LOG.info("_/ ___\\|  |  /  ___/   ______  \\____ \\|  |  \\ __ \\|  | |  |/ ___\\ ");
        LOG.info("\\  \\___|  |__\\___ \\   /_____/  |  |_> >  |  / \\_\\ \\  |_|  \\  \\___ ");
        LOG.info(" \\___  >____/____  >           |   __/|____/|___  /____/__|\\___  >");
        LOG.info("     \\/          \\/            |__|             \\/             \\/ ");
        LOG.info("              .__                            .__              ");
        LOG.info("_____  ______ |__|   ______ ______________  _|__| ____  ____  ");
        LOG.info("\\__  \\ \\____ \\|  |  /  ___// __ \\_  __ \\  \\/ /  |/ ___\\/ __ \\ ");
        LOG.info(" / __ \\|  |_> >  |  \\___ \\\\  ___/|  | \\/\\   /|  \\  \\__\\  ___/ ");
        LOG.info("(____  /   __/|__| /____  >\\___  >__|    \\_/ |__|\\___  >___  >");
        LOG.info("     \\/|__|             \\/     \\/                    \\/    \\/ ");
        LOG.info("");
        LOG.info("                --- Version " + APP_VERSION + " starting up. --- ");
        LOG.info("");

    }

}
