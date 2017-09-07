package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.MagistrateServiceUnit;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Municipality;
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
 * REST resources for magistrate service units.
 */
@Component
@Path("/v1/magistrateserviceunits")
@Api(value = "magistrateserviceunits", description = "Operations about magistrate service units.")
@Produces("text/plain")
public class MagistrateServiceUnitResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(MagistrateServiceUnitResource.class);
    private final Domain domain;
    private final ApiUtils apiUtils;

    @Inject
    public MagistrateServiceUnitResource(final ApiUtils apiUtils,
                                         final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return magistrateserviceunits with query parameter filters.", response = MagistrateServiceUnit.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all magistrateserviceunits in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public ListResponseWrapper<MagistrateServiceUnit> getMagistrateServiceUnits(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                                                @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                                                @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("codeValue") final String codeValue,
                                                                                @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                                                                                @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                                                @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/magistrateserviceunits/ requested!");
        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
        final List<MagistrateServiceUnit> magistrateServiceUnits = domain.getMagistrateServiceUnits(pageSize, from, codeValue, name, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_MAGISTRATESERVICEUNITS, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<MagistrateServiceUnit> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(magistrateServiceUnits);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_MAGISTRATESERVICEUNITS, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATESERVICEUNIT, expand)));
        return wrapper;
    }

    @GET
    @ApiOperation(value = "Return one magistrateserviceunit.", response = Municipality.class)
    @ApiResponse(code = 200, message = "Returns a magistrateserviceunit matching code in JSON format.")
    @Path("{codeValue}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrateServiceUnit(@ApiParam(value = "MagistrateServiceUnit code.") @PathParam("codeValue") final String codeValue,
                                             @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/magistrateserviceunits/" + codeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATESERVICEUNIT, expand)));
        final MagistrateServiceUnit magistrateServiceUnit = domain.getMagistrateServiceUnit(codeValue);
        return Response.ok(magistrateServiceUnit).build();
    }

    @GET
    @ApiOperation(value = "Return one magistrateserviceunit.", response = Municipality.class)
    @ApiResponse(code = 200, message = "Returns a magistrateserviceunit matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrateServiceUnitWithId(@ApiParam(value = "MagistrateServiceUnit id.") @PathParam("id") final String id,
                                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/magistrateserviceunits/id/" + id + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATESERVICEUNIT, expand)));
        final MagistrateServiceUnit magistrateServiceUnit = domain.getMagistrateServiceUnitWithId(id);
        return Response.ok(magistrateServiceUnit).build();
    }

    @GET
    @ApiOperation(value = "Return municipalities for magistrateserviceunit.", response = Municipality.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns municipalities for a magistrateserviceunit in JSON format.")
    @Path("/{resourcecode}/municipalities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrateServiceUnitMunicipalities(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                           @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                           @ApiParam(value = "Search parameter for municipality code, prefix style wildcard support.") @QueryParam("codeValue") final String municipalityCode,
                                                           @ApiParam(value = "Search parameter for municipality name, prefix style wildcard support.") @QueryParam("name") final String municipalityName,
                                                           @ApiParam(value = "After date filtering parameter, results will be municipalities with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                           @ApiParam(value = "MagistrateServiceUnit code.") @PathParam("resourcecode") final String resourcecode,
                                                           @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
        LOG.info("/v1/magistrateserviceunits/" + resourcecode + "/municipalities/ requested!");
        final List<Municipality> municipalities = domain.getMagistrateServiceUnitMunicipalities(pageSize, from, meta.getAfter(), resourcecode, municipalityCode, municipalityName, meta);
        final ListResponseWrapper<Municipality> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(municipalities);
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));
        return Response.ok(wrapper).build();
    }

}