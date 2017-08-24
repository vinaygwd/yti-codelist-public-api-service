package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.BusinessId;
import fi.vm.yti.cls.common.model.Meta;
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
 * REST resources for businessids.
 */
@Component
@Path("/v1/businessids")
@Api(value = "businessids", description = "Operations about businessids.")
@Produces("text/plain")
public class BusinessIdResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessIdResource.class);

    private final Domain m_domain;

    private final ApiUtils m_apiUtils;


    @Inject
    public BusinessIdResource(final ApiUtils apiUtils,
                              final Domain domain) {

        m_apiUtils = apiUtils;

        m_domain = domain;

    }


    @GET
    @ApiOperation(value = "Return businessids with query parameter filters.", response = BusinessId.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all businessids in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessIds(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                   @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("code") final String code,
                                   @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                                   @ApiParam(value = "After date filtering parameter, results will be businessids with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessids/ requested!");

        final Meta meta = new Meta(200, pageSize, from, after);

        final List<BusinessId> businessIds = m_domain.getBusinessIds(pageSize, from, code, name, meta.getAfter(), meta  );

        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(m_apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_BUSINESSIDS, after, pageSize, from + pageSize));
        }

        final ListResponseWrapper<BusinessId> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(businessIds);

        meta.setAfterResourceUrl(m_apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_BUSINESSIDS, new Date(System.currentTimeMillis())));

        wrapper.setMeta(meta);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSID, expand)));

        return Response.ok(wrapper).build();

    }


    @GET
    @ApiOperation(value = "Return one businessid.", response = BusinessId.class)
    @ApiResponse(code = 200, message = "Returns a businessid matching code in JSON format.")
    @Path("{code}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessId(@ApiParam(value = "BusinessId code.") @PathParam("code") final String code,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessids/" + code + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSID, expand)));

        final BusinessId businessId = m_domain.getBusinessId(code);

        return Response.ok(businessId).build();

    }


    @GET
    @ApiOperation(value = "Return one businessid.", response = BusinessId.class)
    @ApiResponse(code = 200, message = "Returns a businessid matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessIdWithId(@ApiParam(value = "BusinessId id.") @PathParam("id") final String id,
                                        @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessids/id/" + id + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSID, expand)));

        final BusinessId businessId = m_domain.getBusinessIdWithId(id);

        return Response.ok(businessId).build();

    }

}
