package fi.vm.yti.cls.api.configuration;

import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.resource.BusinessIdResource;
import fi.vm.yti.cls.api.resource.BusinessServiceSubRegionResource;
import fi.vm.yti.cls.api.resource.CodeRegistryResource;
import fi.vm.yti.cls.api.resource.ElectoralDistrictResource;
import fi.vm.yti.cls.api.resource.HealthCareDistrictResource;
import fi.vm.yti.cls.api.resource.HelloResource;
import fi.vm.yti.cls.api.resource.MagistrateResource;
import fi.vm.yti.cls.api.resource.MagistrateServiceUnitResource;
import fi.vm.yti.cls.api.resource.MunicipalityResource;
import fi.vm.yti.cls.api.resource.PostManagementDistrictResource;
import fi.vm.yti.cls.api.resource.PostalCodeResource;
import fi.vm.yti.cls.api.resource.RegionResource;
import fi.vm.yti.cls.api.resource.StreetAddressResource;
import fi.vm.yti.cls.api.resource.SwaggerResource;
import fi.vm.yti.cls.api.resource.VersionResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

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
    basePath = ApiConstants.API_CONTEXT_PATH + ApiConstants.API_BASE_PATH,
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

        // Generic resources.
        register(HelloResource.class);
        register(VersionResource.class);
        register(SwaggerResource.class);

        // API: Custom codelist resources.
        register(PostalCodeResource.class);
        register(PostManagementDistrictResource.class);
        register(RegionResource.class);
        register(StreetAddressResource.class);
        register(MunicipalityResource.class);
        register(MagistrateResource.class);
        register(MagistrateServiceUnitResource.class);
        register(HealthCareDistrictResource.class);
        register(ElectoralDistrictResource.class);
        register(BusinessServiceSubRegionResource.class);
        register(BusinessIdResource.class);

        // API: Generic Register resources.
        register(CodeRegistryResource.class);

    }

}
