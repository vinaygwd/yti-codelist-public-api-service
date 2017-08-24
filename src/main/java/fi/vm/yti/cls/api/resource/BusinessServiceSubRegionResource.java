package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.BusinessServiceSubRegion;
import fi.vm.yti.cls.common.model.ElectoralDistrict;
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
 * REST resources for electoral districts.
 */
@Component
@Path("/v1/businessservicesubregions")
@Api(value = "businessservicesubregions", description = "Operations about businessservicesubregions.")
@Produces("text/plain")
public class BusinessServiceSubRegionResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessServiceSubRegionResource.class);

    private final Domain m_domain;

    private final ApiUtils m_apiUtils;


    @Inject
    public BusinessServiceSubRegionResource(final ApiUtils apiUtils,
                                            final Domain domain) {

        m_apiUtils = apiUtils;

        m_domain = domain;

    }



    @GET
    @ApiOperation(value = "Return businessservicesubregions with query parameter filters.", response = String.class)
    @ApiResponse(code = 200, message = "Returns all businessservicesubregions in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessServiceSubRegions(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                 @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                 @ApiParam(value = "Search parameter for code, prefix style wildcard support.") @QueryParam("code") final String code,
                                                 @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                                                 @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                 @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessservicesubregions/ requested!");

        final Meta meta = new Meta(200, pageSize, from, after);

        final List<BusinessServiceSubRegion> businessServiceSubRegions = m_domain.getBusinessServiceSubRegions(pageSize, from, code, name, meta.getAfter(), meta);

        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(m_apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_BUSINESSSERVICESUBREGIONS, after, pageSize, from + pageSize));
        }

        final ListResponseWrapper<BusinessServiceSubRegion> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(businessServiceSubRegions);

        meta.setAfterResourceUrl(m_apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_BUSINESSSERVICESUBREGIONS, new Date(System.currentTimeMillis())));

        wrapper.setMeta(meta);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSSERVICESUBREGION, expand)));

        return Response.ok(wrapper).build();

    }


    @GET
    @ApiOperation(value = "Return one businessservicesubregion.", response = ElectoralDistrict.class)
    @ApiResponse(code = 200, message = "Returns a businessservicesubregion matching code in JSON format.")
    @Path("{code}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessServiceSubRegion(@ApiParam(value = "BusinessServiceSubRegion code.") @PathParam("code") final String code,
                                                @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessservicesubregions/" + code + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSSERVICESUBREGION, expand)));

        final BusinessServiceSubRegion businessServiceSubRegion = m_domain.getBusinessServiceSubRegion(code);

        return Response.ok(businessServiceSubRegion).build();

    }


    @GET
    @ApiOperation(value = "Return one businessservicesubregion.", response = ElectoralDistrict.class)
    @ApiResponse(code = 200, message = "Returns a businessservicesubregion matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessServiceSubRegionWithId(@ApiParam(value = "BusinessServiceSubRegion id.") @PathParam("id") final String id,
                                                      @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessservicesubregions/id/" + id + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_BUSINESSSERVICESUBREGION, expand)));

        final BusinessServiceSubRegion businessServiceSubRegion = m_domain.getBusinessServiceSubRegionWithId(id);

        return Response.ok(businessServiceSubRegion).build();

    }


    @GET
    @ApiOperation(value = "Return municipalities for businessservicesubregion.", response = Municipality.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns municipalities for a businessservicesubregion in JSON format.")
    @Path("/{resourcecode}/municipalities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getBusinessServiceSubRegionMunicipalities(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                              @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                                              @ApiParam(value = "Search parameter for municipality code, prefix style wildcard support.") @QueryParam("code") final String municipalityCode,
                                                              @ApiParam(value = "Search parameter for municipality name, prefix style wildcard support.") @QueryParam("name") final String municipalityName,
                                                              @ApiParam(value = "After date filtering parameter, results will be municipalities with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                              @ApiParam(value = "BusinessServiceSubRegion code.") @PathParam("resourcecode") final String resourcecode,
                                                              @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/businessservicesubregions/" + resourcecode + "/municipalities/ requested!");

        final Meta meta = new Meta(200, pageSize, from, after);

        final List<Municipality> municipalities = m_domain.getBusinessServiceSubRegionMunicipalities(pageSize, from, meta.getAfter(), resourcecode, municipalityCode, municipalityName, meta);

        final ListResponseWrapper<Municipality> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(municipalities);

        wrapper.setMeta(meta);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));

        return Response.ok(wrapper).build();

    }

}