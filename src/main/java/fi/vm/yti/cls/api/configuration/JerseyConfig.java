package fi.vm.yti.cls.api.configuration;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import fi.vm.yti.cls.api.resource.CodeRegistryResource;
import fi.vm.yti.cls.api.resource.CodeSchemeResource;
import fi.vm.yti.cls.api.resource.SwaggerResource;
import fi.vm.yti.cls.api.resource.VersionResource;
import fi.vm.yti.cls.common.constants.ApiConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

@Component
@SwaggerDefinition(
    info = @Info(
        description = "Code List Service - Public API Service - Spring Boot microservice.",
        version = ApiConstants.API_VERSION,
        title = "Code List Service - Public API Service",
        termsOfService = "https://opensource.org/licenses/EUPL-1.1",
        contact = @Contact(
            name = "Code List Service by the Population Register Center of Finland",
            url = "http://vm.fi/yhteinen-tiedon-hallinta"
        ),
        license = @License(
                name = "EUPL-1.2",
                url = "https://opensource.org/licenses/EUPL-1.1"
        )
    ),
    host = "localhost:9600",
    basePath = ApiConstants.API_CONTEXT_PATH_RESTAPI + ApiConstants.API_BASE_PATH,
    consumes = {"application/json", "application/xml"},
    produces = {"application/json", "application/xml"},
    schemes = {SwaggerDefinition.Scheme.HTTPS}
)
@Api(value = ApiConstants.API_BASE_PATH, description = "Code List Service - Public API Service")
@ApplicationPath(ApiConstants.API_BASE_PATH)
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        final JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());

        // CORS filtering.
        register(CorsFilter.class);

        // Generic resources.
        register(VersionResource.class);
        register(SwaggerResource.class);

        // API: Generic Register resources.
        register(CodeRegistryResource.class);
        register(CodeSchemeResource.class);
    }

}
