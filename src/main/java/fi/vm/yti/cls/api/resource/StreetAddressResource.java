package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ErrorWrapper;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Municipality;
import fi.vm.yti.cls.common.model.SpecificStreetAddress;
import fi.vm.yti.cls.common.model.StreetAddress;
import fi.vm.yti.cls.common.model.StreetNumber;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * REST resources for street addresses and street numbers.
 */
@Component
@Path("/v1/streetaddresses")
@Api(value = "streetaddresses", description = "Operations about streetaddresses.")
@Produces("text/plain")
public class StreetAddressResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(StreetAddressResource.class);
    private final Domain domain;
    private final ApiUtils apiUtils;

    @Inject
    public StreetAddressResource(final ApiUtils apiUtils,
                                 final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return streetaddresses with query parameter filters.", response = StreetAddress.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all streetaddresses in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddresses(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                       @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from")  @DefaultValue("0") final Integer from,
                                       @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                                       @ApiParam(value = "After date filtering parameter, results will be streetaddresses with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                       @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/ requested!");
        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
        final List<StreetAddress> streetAddresses = domain.getStreetAddresses(pageSize, from, name, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_STREETADDRESSES, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<StreetAddress> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(streetAddresses);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_STREETADDRESSES, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_STREETADDRESS, expand)));
        return Response.ok(wrapper).build();
    }

    @GET
    @ApiOperation(value = "Return one streetaddress using ID.", response = StreetAddress.class)
    @ApiResponse(code = 200, message = "Returns a streetaddress matching id in JSON format.")
    @Path("/streetnumber/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetNumberWithId(@ApiParam(value = "StreetNumber id.") @PathParam("id") final String id,
                                          @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/streetnumber/" + id + "/ requested!");
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_STREETNUMBER, expand)));
        final StreetNumber streetNumber = domain.getStreetNumberWithId(id);
        if (streetNumber == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.STREET_NUMBER_NOT_FOUND);
        }
        return Response.ok(streetNumber).build();
    }

    @GET
    @ApiOperation(value = "Return one streetaddress using ID.", response = StreetAddress.class)
    @ApiResponse(code = 200, message = "Returns a streetaddress matching id in JSON format.")
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddress(@ApiParam(value = "StreetAddress id.") @PathParam("id") final String id,
                                     @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/" + id + "/ requested!");
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_STREETADDRESS, expand)));
        final StreetAddress streetAddress = domain.getStreetAddressWithId(id);
        if (streetAddress == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.STREET_ADDRESS_NOT_FOUND);
        }
        return Response.ok(streetAddress).build();
    }

    @GET
    @ApiOperation(value = "Return one streetaddress using ID.", response = StreetAddress.class)
    @ApiResponse(code = 200, message = "Returns a streetaddress matching ID in JSON format.")
    @Path("/id/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddressWithId(@ApiParam(value = "StreetAddress id.") @PathParam("id") final String id,
                                           @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/id/" + id + "/ requested!");
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_STREETADDRESS, expand)));
        final StreetAddress streetAddress = domain.getStreetAddressWithId(id);
        if (streetAddress == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.STREET_ADDRESS_NOT_FOUND);
        }
        return Response.ok(streetAddress).build();
    }

    @GET
    @ApiOperation(value = "Return one streetaddress.", response = StreetAddress.class)
    @ApiResponse(code = 200, message = "Returns a streetaddress with municipality and street name in JSON format.")
    @Path("/municipality/{municipalityCode}/{streetName}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddressWithMunicipalityAndStreetName(@ApiParam(value = "Municipality code.") @PathParam("municipalityCode") final String municipalityCode,
                                                                  @ApiParam(value = "Street name.") @PathParam("streetName") final String streetName,
                                                                  @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/" + municipalityCode + "/" + streetName + "/ requested!");
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_STREETADDRESS, expand)));
        final StreetAddress streetAddress = domain.getStreetAddressWithMunicipalityAndStreetName(municipalityCode, streetName);
        if (streetAddress == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.STREET_ADDRESS_NOT_FOUND);
        }
        return Response.ok(streetAddress).build();
    }

    @GET
    @ApiOperation(value = "Return one specific street address.", response = SpecificStreetAddress.class)
    @ApiResponse(code = 200, message = "Returns a streetNumber with municipality and street name in JSON format.")
    @Path("/municipality/{municipalityCode}/{streetName}/{number}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddressWithMunicipalityAndStreetNameAndNumber(@ApiParam(value = "Municipality code.") @PathParam("municipalityCode") final String municipalityCode,
                                                                           @ApiParam(value = "Street name.") @PathParam("streetName") final String streetName,
                                                                           @ApiParam(value = "Street number.") @PathParam("number") final String number,
                                                                           @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/" + municipalityCode + "/" + streetName + "/" + number + "/ requested!");
        final List<String> filters = new ArrayList<>();
        filters.add(FILTER_NAME_STREETNUMBER);
        filters.add(FILTER_NAME_STREETADDRESS);
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(filters, expand)));
        final int numberInt = Integer.parseInt(number);
        final StreetNumber streetNumber = domain.getStreetAddressWithMunicipalityAndStreetNameAndNumber(municipalityCode, streetName, numberInt);
        if (streetNumber == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.STREET_NUMBER_NOT_FOUND);
        }
        final StreetAddress streetAddress = streetNumber.getStreetAddress();
        if (streetAddress == null) {
            return createErrorResponse(Response.Status.NOT_FOUND.getStatusCode(), ErrorWrapper.STREET_ADDRESS_NOT_FOUND);
        }
        final SpecificStreetAddress address = new SpecificStreetAddress(streetAddress);
        address.setUri(apiUtils.createResourceUrl(ApiConstants.API_PATH_STREETADDRESSES, "municipality/" + municipalityCode + "/" + streetName + "/" + number));
        address.setNumber(numberInt);
        address.setPostalCode(streetNumber.getPostalCode());
        return Response.ok(address).build();
    }

    @GET
    @ApiOperation(value = "Return a list of streetaddress matching municipality code.", response = StreetAddress.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns a list of streetaddresses with municipality in JSON format.")
    @Path("/municipality/{municipalityCode}/")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddressesWithMunicipality(@ApiParam(value = "Municipality code.") @PathParam("municipalityCode") final String municipalityCode,
                                                       @ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                       @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from")  @DefaultValue("0") final Integer from,
                                                       @ApiParam(value = "Search parameter for name, prefix style wildcard support.") @QueryParam("name") final String name,
                                                       @ApiParam(value = "After date filtering parameter, results will be streetaddresses with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                       @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/municipality/" + municipalityCode + "/ requested!");
        final Meta meta = new Meta(200, pageSize, from, after);
        final List<StreetAddress> streetAddresses = domain.getStreetAddressesWithMunicipality(pageSize, from, name, municipalityCode, meta.getAfter(), meta);
        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_STREETADDRESSES, after, pageSize, from + pageSize));
        }
        final ListResponseWrapper<StreetAddress> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(streetAddresses);
        meta.setAfterResourceUrl(apiUtils.createAfterResourceUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_STREETADDRESSES, new Date(System.currentTimeMillis())));
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_STREETADDRESS, expand)));
        return Response.ok(wrapper).build();
    }

    @GET
    @ApiOperation(value = "Return municipalities for a streetaddress.", response = Municipality.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns municipalities for a streetaddress in JSON format.")
    @Path("/{id}/municipalities")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getStreetAddressMunicipalities(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from")  @DefaultValue("0") final Integer from,
                                                   @ApiParam(value = "Search parameter for municipality code, prefix style wildcard support.") @QueryParam("codeValue") final String municipalityCode,
                                                   @ApiParam(value = "Search parameter for municipality name, prefix style wildcard support.") @QueryParam("name") final String municipalityName,
                                                   @ApiParam(value = "After date filtering parameter, results will be municipalities with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                                   @ApiParam(value = "StreetAddress id.") @PathParam("id") final String id,
                                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/streetaddresses/" + id + "/municipalities/ requested!");
        final Meta meta = new Meta(200, pageSize, from, after);
        final List<Municipality> municipalities = domain.getStreetAddressMunicipalities(pageSize, from, meta.getAfter(), id, municipalityCode, municipalityName, meta);
        final ListResponseWrapper<Municipality> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(municipalities);
        wrapper.setMeta(meta);
        ObjectWriterInjector.set(new FilterModifier(createSimpleFilterProvider(FILTER_NAME_MUNICIPALITY, expand)));
        return Response.ok(wrapper).build();
    }

}
