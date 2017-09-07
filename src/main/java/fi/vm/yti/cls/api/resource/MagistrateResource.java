package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.Magistrate;
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
 * REST resources for magistrates.
 */
@Component
@Path("/v1/magistrates")
@Api(value = "magistrates", description = "Operations about magistrates.")
@Produces("text/plain")
public class MagistrateResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(MagistrateResource.class);
    private final Domain domain;
    private final ApiUtils apiUtils;

    @Inject
    public MagistrateResource(final ApiUtils apiUtils,
                              final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return magistrates with query parameter filters.", response = Magistrate.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all magistrates in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrates(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                   @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("codeValue") final String codeValue,
                                   @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                                   @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/magistrates/ requested!");
        final Meta meta = new Meta(200, pageSize, from, after);
        final List<Magistrate> magistrates = domain.getMagistrates(pageSize, from, codeValue, name, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_MAGISTRATES, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<Magistrate> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(magistrates);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_MAGISTRATES, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATE, expand)));
        return Response.ok(wrapper).build();
    }

    @GET
    @ApiOperation(value = "Return one magistrate.", response = Magistrate.class)
    @ApiResponse(code = 200, message = "Returns a magistrate matching code in JSON format.")
    @Path("{codeValue}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrate(@ApiParam(value = "Magistrate code.") @PathParam("codeValue") final String codeValue,
                                  @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/magistrates/" + codeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATE, expand)));
        final Magistrate magistrate = domain.getMagistrate(codeValue);
        return Response.ok(magistrate).build();
    }

    @GET
    @ApiOperation(value = "Return one magistrate.", response = Magistrate.class)
    @ApiResponse(code = 200, message = "Returns a magistrate matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrateWithId(@ApiParam(value = "Magistrate id.") @PathParam("id") final String id,
                                        @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/magistrates/id/" + id + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MAGISTRATE, expand)));
        final Magistrate magistrate = domain.getMagistrateWithId(id);
        return Response.ok(magistrate).build();
    }

    @GET
    @ApiOperation(value = "Return municipalities for magistrates.", response = Municipality.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns municipalities for a magistrate in JSON format.")
    @Path("/{resourcecode}/municipalities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getMagistrateMunicipalities(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                @ApiParam(value = "Search parameter for municipality code, prefix style wildcard support.") @QueryParam("codeValue") final String municipalityCode,
                                                @ApiParam(value = "Search parameter for municipality name, prefix style wildcard support.") @QueryParam("name") final String municipalityName,
                                                @ApiParam(value = "After date filtering parameter, results will be municipalities with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                @ApiParam(value = "MagistrateServiceUnit code.") @PathParam("resourcecode") final String resourcecode,
                                                @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        final Meta meta = new Meta(200, pageSize, from, after);
        LOG.info("/v1/magistrates/" + resourcecode + "/municipalities/ requested!");
        final List<Municipality> municipalities = domain.getMagistrateMunicipalities(pageSize, from, meta.getAfter(), resourcecode, municipalityCode, municipalityName, meta);
        final ListResponseWrapper<Municipality> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(municipalities);
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));
        return Response.ok(wrapper).build();
    }

}
