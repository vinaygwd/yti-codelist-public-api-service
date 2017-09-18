package fi.vm.yti.cls.api.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;

import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.CodeRegistry;
import fi.vm.yti.cls.common.model.CodeScheme;
import fi.vm.yti.cls.common.model.Meta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;

/**
 * REST resources for registries, schemes and codes.
 */
@Component
@Path("/v1/codeschemes")
@Api(value = "codeschemes", description = "Operations about codeschemes.")
@Produces("text/plain")
public class CodeSchemeResource extends AbstractBaseResource {
    private static final Logger LOG = LoggerFactory.getLogger(CodeRegistryResource.class);
    private final Domain domain;

    @Inject
    public CodeSchemeResource(final Domain domain) {
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return list of available CodeSchemes.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all Registers in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeSchemes(@ApiParam(value = "CodeRegistry CodeValue.") @QueryParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                   @ApiParam(value = "CodeRegistry CodeValue.") @QueryParam("codeRegistryCodeValue") final String codeRegistryPrefLabel,
                                   @ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                   @ApiParam(value = "CodeScheme codeValue as string value.") @QueryParam("codeValue") final String codeSchemeCodeValue,
                                   @ApiParam(value = "CodeScheme PrefLabel as string value.") @QueryParam("prefLabel") final String codeSchemePrefLabel,
                                   @ApiParam(value = "Status enumerations in CSL format.") @QueryParam("status") @DefaultValue("VALID") final String status,
                                   @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/codeschemes/ requested!");
        final Meta meta = new Meta(200, null, null, after);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODESCHEME, expand)));
        final List<String> statusList = parseStatus(status);
        final List<CodeScheme> codeSchemesList = new ArrayList<>();
        final Set<CodeScheme> codeSchemes = domain.getCodeSchemes(pageSize, from, codeRegistryCodeValue, codeRegistryPrefLabel, codeSchemeCodeValue, codeSchemePrefLabel, statusList, meta.getAfter(), meta);
        codeSchemesList.addAll(codeSchemes);
        meta.setResultCount(codeSchemesList.size());
        final ListResponseWrapper<CodeScheme> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(codeSchemesList);
        wrapper.setMeta(meta);
        return Response.ok(wrapper).build();
    }

}
