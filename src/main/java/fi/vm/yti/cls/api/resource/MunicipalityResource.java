package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.BusinessServiceSubRegion;
import fi.vm.yti.cls.common.model.ElectoralDistrict;
import fi.vm.yti.cls.common.model.HealthCareDistrict;
import fi.vm.yti.cls.common.model.Magistrate;
import fi.vm.yti.cls.common.model.MagistrateServiceUnit;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Municipality;
import fi.vm.yti.cls.common.model.Region;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * REST resources for municipalities.
 */
@Component
@Path("/v1/municipalities")
@Api(value = "municipalities", description = "Operations about municipalities.")
@Produces("text/plain")
public class MunicipalityResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(MunicipalityResource.class);
    private final Domain domain;
    private final ApiUtils apiUtils;

    @Inject
    public MunicipalityResource(final ApiUtils apiUtils,
                                final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return municipalities with query parameter filters.", response = Municipality.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all municipalities in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalities(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                      @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                      @ApiParam(value = "Search parameter for codeValue, prefix style wildcard support.") @QueryParam("codeValue") final String codeValue,
                                      @ApiParam(value = "Search parameter for prefLabel, prefix style wildcard support.") @QueryParam("prefLabel") final String prefLabel,
                                      @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                      @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/ requested!");
        final Meta meta = new Meta(200, pageSize, from, after);
        final List<Municipality> municipalities = domain.getMunicipalities(pageSize, from, codeValue, prefLabel, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_MUNICIPALITIES, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<Municipality> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(municipalities);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_MUNICIPALITIES, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));
        return Response.ok(wrapper).build();
    }

    @GET
    @ApiOperation(value = "Return one municipality.", response = Municipality.class)
    @ApiResponse(code = 200, message = "Returns a municipality matching code in JSON format.")
    @Path("{codeValue}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipality(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                        @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));
        final Municipality municipality = domain.getMunicipality(codeValue);
        return Response.ok(municipality).build();
    }

    @GET
    @ApiOperation(value = "Return one municipality.", response = Municipality.class)
    @ApiResponse(code = 200, message = "Returns a municipality matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityWithId(@ApiParam(value = "Municipality id.") @PathParam("id") final String id,
                                          @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/id/" + id + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));
        final Municipality municipality = domain.getMunicipalityWithId(id);
        return Response.ok(municipality).build();
    }

    @GET
    @ApiOperation(value = "Return magistrate of the municipality.", response = Magistrate.class)
    @ApiResponse(code = 200, message = "Returns the magistrate of the municipality in JSON format.")
    @Path("/{codeValue}/magistrate/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityMagistrate(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                                @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/magistrate/ requested!");
        final Municipality municipality = domain.getMunicipality(codeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATE, expand)));
        final Magistrate magistrate = municipality.getMagistrate();
        return Response.ok(magistrate).build();
    }

    @GET
    @ApiOperation(value = "Return region of the municipality.", response = Region.class)
    @ApiResponse(code = 200, message = "Returns the region of the municipality in JSON format.")
    @Path("/{codeValue}/region/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityRegion(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                        @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/region/ requested!");
        final Municipality municipality = domain.getMunicipality(codeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGION, expand)));
        final Region region = municipality.getRegion();
        return Response.ok(region).build();
    }

    @GET
    @ApiOperation(value = "Return healthcaredistrict of the municipality.", response = HealthCareDistrict.class)
    @ApiResponse(code = 200, message = "Returns the healthcaredistrict of the municipality in JSON format.")
    @Path("/{codeValue}/healthcaredistrict/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityHealthCareDistrict(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                                      @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/healthcaredistrict/ requested!");
        final Municipality municipality = domain.getMunicipality(codeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_HEALTHCAREDISTRICT, expand)));
        final HealthCareDistrict healtCareDistrict = municipality.getHealthCareDistrict();
        return Response.ok(healtCareDistrict).build();
    }

    @GET
    @ApiOperation(value = "Return electoraldistrict of the municipality.", response = ElectoralDistrict.class)
    @ApiResponse(code = 200, message = "Returns the electoraldistrict of the municipality in JSON format.")
    @Path("/{codeValue}/electoraldistrict/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityElectoralDistrict(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                                     @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/electoraldistrict/ requested!");
        final Municipality municipality = domain.getMunicipality(codeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_ELECTORALDISTRICT, expand)));
        final ElectoralDistrict electoralDistrict = municipality.getElectoralDistrict();
        return Response.ok(electoralDistrict).build();
    }

    @GET
    @ApiOperation(value = "Return magistrateserviceunit of the municipality.", response = MagistrateServiceUnit.class)
    @ApiResponse(code = 200, message = "Returns the magistrateserviceunit of the municipality in JSON format.")
    @Path("/{codeValue}/magistrateserviceunit/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityMagistrateServiceUnit(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                                         @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/magistrateserviceunit/ requested!");
        final Municipality municipality = domain.getMunicipality(codeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATESERVICEUNIT, expand)));
        final MagistrateServiceUnit magistrateServiceUnit = municipality.getMagistrateServiceUnit();
        return Response.ok(magistrateServiceUnit).build();
    }

    @GET
    @ApiOperation(value = "Return businessservicesubregion of the municipality.", response = BusinessServiceSubRegion.class)
    @ApiResponse(code = 200, message = "Returns the businessservicesubregion of the municipality in JSON format.")
    @Path("/{codeValue}/businessservicesubregion/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMunicipalityBusinessServiceSubRegion(@ApiParam(value = "Municipality code.") @PathParam("codeValue") final String codeValue,
                                                            @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/municipalities/" + codeValue + "/businessservicesubregion/ requested!");
        final Municipality municipality = domain.getMunicipality(codeValue);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSSERVICESUBREGION, expand)));
        final BusinessServiceSubRegion businessServiceSubRegion = municipality.getBusinessServiceSubRegion();
        return Response.ok(businessServiceSubRegion).build();
    }

}
