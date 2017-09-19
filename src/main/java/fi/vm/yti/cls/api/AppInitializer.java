package fi.vm.yti.cls.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.configuration.PublicApiServiceProperties;
import fi.vm.yti.cls.api.configuration.VersionInformation;
import fi.vm.yti.cls.api.util.FileUtils;

@Component
public class AppInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AppInitializer.class);
    public static final String LOCAL_SWAGGER_DATA_DIR = "/data/cls/cls-api/swagger/";
    private final ApiUtils apiUtils;
    private final PublicApiServiceProperties publicApiServiceProperties;
    private final VersionInformation versionInformation;


    @Inject
    public AppInitializer(final VersionInformation versionInformation,
                          final ApiUtils apiUtils,
                          final PublicApiServiceProperties publicApiServiceProperties) {
        this.versionInformation = versionInformation;
        this.apiUtils = apiUtils;
        this.publicApiServiceProperties = publicApiServiceProperties;
    }

    /**
     * Initialize the application, load data for services.
     */
    public void initialize() {
        printLogo();
        updateSwaggerHost();
    }

    /**
     * Application logo printout to log.
     */
    private void printLogo() {
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
        LOG.info("                --- Version " + versionInformation.getVersion() + " starting up. --- ");
        LOG.info("");
    }

    /**
     * Updates the compile time generated swagger.json with the hostname of the current environment.
     *
     * The file is stored in the {@value #LOCAL_SWAGGER_DATA_DIR} folder, where it will be served from the SwaggerResource.
     */
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    private void updateSwaggerHost() {
        final ObjectMapper mapper = new ObjectMapper();
        FileOutputStream fos = null;
        try (final InputStream inputStream = FileUtils.loadFileFromClassPath("/swagger/swagger.json")) {
            final ObjectNode jsonObject = (ObjectNode) mapper.readTree(new InputStreamReader(inputStream, "UTF-8"));
            final String hostname = apiUtils.getPublicApiServiceHostname();
            jsonObject.put("host", hostname);
            final String scheme = publicApiServiceProperties.getScheme();
            final List<String> schemes = new ArrayList<>();
            schemes.add(scheme);
            final ArrayNode schemeArray = mapper.valueToTree(schemes);
            jsonObject.putArray("schemes").addAll(schemeArray);
            final File file = new File(LOCAL_SWAGGER_DATA_DIR + "swagger.json");
            Files.createDirectories(Paths.get(file.getParentFile().getPath()));
            final String fileLocation = file.toString();
            LOG.info("Storing modified swagger.json description with hostname: " + hostname + " to: " + fileLocation);
            fos = new FileOutputStream(fileLocation, false);
            mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
            fos.write(mapper.writeValueAsString(jsonObject).getBytes(StandardCharsets.UTF_8));
            fos.close();
        } catch (IOException e) {
            LOG.error("Swagger JSON parsing failed: " + e.getMessage());
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    LOG.error("Closing output stream failed: " + e.getMessage());
                }
            }
        }
    }

}
