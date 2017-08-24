package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.Meta;
import fi.vm.yti.cls.common.model.Region;
import fi.vm.yti.cls.common.model.Register;
import fi.vm.yti.cls.common.model.RegisterItem;
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
import java.util.List;
import java.util.Set;


/**
 * REST resources for registeritems.
 */
@Component
@Path("/v1/registers")
@Api(value = "registers", description = "Operations about generic registeritems.")
@Produces("text/plain")
public class RegisterResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterResource.class);

    private final ApiUtils m_apiUtils;

    private final Domain m_domain;


    @Inject
    public RegisterResource(final ApiUtils apiUtils,
                            final Domain domain) {

        m_apiUtils = apiUtils;

        m_domain = domain;

    }

    @GET
    @ApiOperation(value = "Return list of available Registers.", response = Register.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all Registers in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegisters(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                 @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                 @ApiParam(value = "Register type as string value.") @QueryParam("type") final String type,
                                 @ApiParam(value = "Register name as string value.") @QueryParam("name") final String name,
                                 @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                 @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/registers/ requested!");

        final Meta meta = new Meta(200, null, null, after);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGISTER, expand)));

        final List<Register> registerApis = new ArrayList();

        final Set<Register> registers = m_domain.getRegisters(pageSize, from, type, name, meta.getAfter(), meta);
        registerApis.addAll(registers);

        meta.setResultCount(registerApis.size());

        final ListResponseWrapper<Register> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(registerApis);

        wrapper.setMeta(meta);

        return Response.ok(wrapper).build();

    }


    @GET
    @ApiOperation(value = "Return content listing of one register.", response = Region.class)
    @ApiResponse(code = 200, message = "Returns a register matching code in JSON format.")
    @Path("{register}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegisterItem(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                    @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                    @ApiParam(value = "Register code.") @PathParam("register") final String registerCode,
                                    @ApiParam(value = "RegisterItem code.") @QueryParam("code") final String registerItemCode,
                                    @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/registers/" + registerCode + "/ requested!");

        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGISTER_ITEM, expand)));

        final List<RegisterItem> registerItems = m_domain.getRegisterItems(pageSize, from, registerCode, registerItemCode, meta.getAfter(), meta);

        if (pageSize != null && from + pageSize < meta.getTotalResults()) {
            meta.setNextPage(m_apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_REGISTERS, after, pageSize, from + pageSize));
        }

        final ListResponseWrapper<RegisterItem> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(registerItems);

        wrapper.setMeta(meta);

        return Response.ok(wrapper).build();

    }


    @GET
    @ApiOperation(value = "Return one registeritem from specific register.", response = Region.class)
    @ApiResponse(code = 200, message = "Returns one registeritem from specific register in JSON format.")
    @Path("{registerCode}/{registerItemCode}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getRegisterItem(@ApiParam(value = "Register code.") @PathParam("registerCode") final String registerCode,
                                    @ApiParam(value = "RegisterItem code.") @PathParam("registerItemCode") final String registerItemCode,
                                    @ApiParam(value = "After date filtering parameter, results will be regions with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {

        LOG.info("/v1/registers/" + registerCode + "/" + registerItemCode + "/ requested!");

        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_REGISTER_ITEM, expand)));

        final RegisterItem registerItem = m_domain.getRegisterItem(registerCode, registerItemCode);

        return Response.ok(registerItem).build();

    }

}
