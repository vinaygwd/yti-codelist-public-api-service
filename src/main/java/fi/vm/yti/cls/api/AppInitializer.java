package fi.vm.yti.cls.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.configuration.PublicApiServiceProperties;
import fi.vm.yti.cls.api.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
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

@Component
public class AppInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(AppInitializer.class);

    public static final String LOCAL_SWAGGER_DATA_DIR = "/data/cls/cls-api/swagger/";

    private final ApiUtils m_apiUtils;

    private final PublicApiServiceProperties m_publicApiServiceProperties;


    @Inject
    public AppInitializer(final ApiUtils apiUtils,
                          final PublicApiServiceProperties publicApiServiceProperties) {

        m_apiUtils = apiUtils;

        m_publicApiServiceProperties = publicApiServiceProperties;

    }


    /**
     * Initialize the application, load data for services.
     */
    public void initialize() {

        updateSwaggerHost();

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

        try {

            final InputStream inputStream = FileUtils.loadFileFromClassPath("/swagger/swagger.json");
            final ObjectNode jsonObject = (ObjectNode) mapper.readTree(new InputStreamReader(inputStream, "UTF-8"));

            final String hostname = m_apiUtils.getPublicApiServiceHostname();
            jsonObject.put("host", hostname);

            // TODO: Remove this hack once Swagger UI cyclic dependency / Maximum call stack size exceeded issue gets fixed: https://github.com/astaxie/beego/issues/2694
            final ObjectNode definitions = (ObjectNode) jsonObject.get("definitions");
            final JsonNode streetNumber = definitions.get("StreetNumber");
            final ObjectNode streetNumberProperties = (ObjectNode) streetNumber.get("properties");
            streetNumberProperties.remove("streetAddress");

            final String scheme = m_publicApiServiceProperties.getScheme();
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
