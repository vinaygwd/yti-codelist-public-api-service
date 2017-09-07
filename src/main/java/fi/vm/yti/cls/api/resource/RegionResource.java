package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
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
 * REST resources for regions.
 */
@Component
@Path("/v1/regions")
@Api(value = "regions", description = "Operations about regions.")
@Produces("text/plain")
public class RegionResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(RegionResource.class);

    private final Domain m_domain;

    private final ApiUtils m_apiUtils;


    @Inject
    public RegionResource(final ApiUtils apiUtils,
                          final Domain domain) {

        m_apiUtils = apiUtils;

        m_domain = domain;

    }


    @GET
    @ApiOperation(value = "Return regions with query parameter filters.", response = Region.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all regions in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegions(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                               @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from")  @DefaultValue("0") final Integer from,
                               @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("codeValue") final String codeValue,
                               @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                               @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                               @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/regions/ requested!");

        final Meta meta = new Meta(200, pageSize, from, after);

        final List<Region> regions = m_domain.getRegions(pageSize, from, codeValue, name, meta.getAfter(), meta);

        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(m_apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_REGIONS, after, pageSize, from + pageSize));
        }

        final ListResponseWrapper<Region> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(regions);

        meta.setAfterResourceUrl(m_apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_REGIONS, new Date(System.currentTimeMillis())));

        wrapper.setMeta(meta);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGION, expand)));

        return Response.ok(wrapper).build();

    }


    @GET
    @ApiOperation(value = "Return one region.", response = Region.class)
    @ApiResponse(code = 200, message = "Returns a region matching code in JSON format.")
    @Path("{codeValue}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegion(@ApiParam(value = "Region code.") @PathParam("codeValue") final String codeValue,
                              @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/regions/" + codeValue + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGION, expand)));

        final Region region = m_domain.getRegion(codeValue);

        return Response.ok(region).build();

    }


    @GET
    @ApiOperation(value = "Return one region.", response = Region.class)
    @ApiResponse(code = 200, message = "Returns a region matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegionWithId(@ApiParam(value = "Region id.") @PathParam("id") final String id,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/regions/id/" + id + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGION, expand)));

        final Region region = m_domain.getRegionWithId(id);

        return Response.ok(region).build();

    }


    @GET
    @ApiOperation(value = "Return municipalities for region.", response = Municipality.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns municipalities for a region in JSON format.")
    @Path("/{resourcecode}/municipalities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegionMunicipalities(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                            @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from")  @DefaultValue("0") final Integer from,
                                            @ApiParam(value = "Search parameter for municipality code, prefix style wildcard support.") @QueryParam("codeValue") final String municipalityCode,
                                            @ApiParam(value = "Search parameter for municipality name, prefix style wildcard support.") @QueryParam("name") final String municipalityName,
                                            @ApiParam(value = "After date filtering parameter, results will be municipalities with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                            @ApiParam(value = "Region code.") @PathParam("resourcecode") final String resourcecode,
                                            @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/regions/" + resourcecode + "/municipalities/ requested!");

        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);

        final List<Municipality> municipalities = m_domain.getRegionMunicipalities(pageSize, from, meta.getAfter(), resourcecode, municipalityCode, municipalityName, meta);

        final ListResponseWrapper<Municipality> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(municipalities);

        wrapper.setMeta(meta);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));

        return Response.ok(wrapper).build();

    }


}
