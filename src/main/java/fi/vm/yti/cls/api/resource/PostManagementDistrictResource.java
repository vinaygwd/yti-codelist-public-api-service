package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.PostManagementDistrict;
import fi.vm.yti.cls.common.model.PostalCode;
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
 * REST resources for post management districts.
 */
@Component
@Path("/v1/postmanagementdistricts")
@Api(value = "postmanagementdistricts", description = "Operations about post management districts.")
@Produces("text/plain")
public class PostManagementDistrictResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(PostManagementDistrictResource.class);
    private final Domain domain;
    private final ApiUtils apiUtils;

    @Inject
    public PostManagementDistrictResource(final ApiUtils apiUtils,
                                          final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return postmanagementdistricts with query parameter filters.", response = PostManagementDistrict.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns postmanagementdistricts and metadata in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostManagementDistricts(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                               @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                               @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("codeValue") final String codeValue,
                                               @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("prefLabel") final String prefLabel,
                                               @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                               @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postmanagementdistricts/ requested with code: " + codeValue + ", name: " + prefLabel);
        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
        final List<PostManagementDistrict> postManagementDistricts = domain.getPostManagementDistricts(pageSize, from, codeValue, prefLabel, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_POSTMANAGEMENTDISTRICTS, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<PostManagementDistrict> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(postManagementDistricts);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_POSTMANAGEMENTDISTRICTS, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTMANAGEMENTDISTRICT, expand)));
        return Response.ok(wrapper).build();
    }

    @GET
    @ApiOperation(value = "Return one postmanagementdistrict.", response = PostManagementDistrict.class)
    @ApiResponse(code = 200, message = "Returns a postmanagementdistrict macthing code in JSON format.")
    @Path("{codeValue}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostManagementDistrict(@ApiParam(value = "PostManagementDistrict code.") @PathParam("codeValue") final String codeValue,
                                              @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postmanagementdistricts/" + codeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTMANAGEMENTDISTRICT, expand)));
        final PostManagementDistrict postManagementDistrict = domain.getPostManagementDistrict(codeValue);
        return Response.ok(postManagementDistrict).build();
    }

    @GET
    @ApiOperation(value = "Return one postmanagementdistrict.", response = PostManagementDistrict.class)
    @ApiResponse(code = 200, message = "Returns a postmanagementdistrict macthing ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostManagementDistrictWithId(@ApiParam(value = "PostManagementDistrict id.") @PathParam("id") final String id,
                                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postmanagementdistricts/id/" + id + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTMANAGEMENTDISTRICT, expand)));
        final PostManagementDistrict postManagementDistrict = domain.getPostManagementDistrictWithId(id);
        return Response.ok(postManagementDistrict).build();
    }

    @GET
    @ApiOperation(value = "Return postalcodes for postmanagementdistrict.", response = PostalCode.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns postal codes for matching postmanagementdistrict in JSON format.")
    @Path("{codeValue}/postalcodes")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getPostManagementDistrictPostalCodes(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                         @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                         @ApiParam(value = "After date filtering parameter, results will be postalcodes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                         @ApiParam(value = "PostalCode code.") @PathParam("codeValue") final String codeValue,
                                                         @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/postmanagementdistrict/" + codeValue + "/postalcodes/ requested!");
        final Meta meta = new Meta(200, pageSize, from, after);
        final List<PostalCode> postalCodes =  domain.getPostManagementDistrictPostalCodes(pageSize, from, meta.getAfter(), codeValue, meta);
        final ListResponseWrapper<PostalCode> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(postalCodes);
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_POSTMANAGEMENTDISTRICT, expand)));
        return Response.ok(wrapper).build();
    }

}
