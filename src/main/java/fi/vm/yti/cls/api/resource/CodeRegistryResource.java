package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;
import fi.vm.yti.cls.api.api.ApiConstants;
import fi.vm.yti.cls.api.api.ApiUtils;
import fi.vm.yti.cls.api.api.ListResponseWrapper;
import fi.vm.yti.cls.api.domain.Domain;
import fi.vm.yti.cls.common.model.Code;
import fi.vm.yti.cls.common.model.CodeRegistry;
import fi.vm.yti.cls.common.model.CodeScheme;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * REST resources for registries, schemes and codes.
 */
@Component
@Path("/v1/coderegistries")
@Api(value = "coderegistries", description = "Operations about coderegistries, codeschemes and codes, .")
@Produces("text/plain")
public class CodeRegistryResource extends AbstractBaseResource {
    private static final Logger LOG = LoggerFactory.getLogger(CodeRegistryResource.class);
    private final ApiUtils apiUtils;
    private final Domain domain;

    @Inject
    public CodeRegistryResource(final ApiUtils apiUtils,
                                final Domain domain) {
        this.apiUtils = apiUtils;
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return list of available CodeRegistries.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all Registers in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistries(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                      @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                      @ApiParam(value = "CodeRegistry CodeValue as string value.") @QueryParam("codeValue") final String codeRegistryCodeValue,
                                      @ApiParam(value = "CodeRegistry name as string value.") @QueryParam("name") final String name,
                                      @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                      @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/coderegistries/ requested!");
        final Meta meta = new Meta(200, null, null, after);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODEREGISTRY, expand)));
        final List<CodeRegistry> registryList = new ArrayList<>();
        final Set<CodeRegistry> codeRegistries = domain.getCodeRegistries(pageSize, from, codeRegistryCodeValue, name, meta.getAfter(), meta);
        registryList.addAll(codeRegistries);
        meta.setResultCount(registryList.size());
        final ListResponseWrapper<CodeRegistry> wrapper = new ListResponseWrapper<>();
        wrapper.setResults(registryList);
        wrapper.setMeta(meta);
        return Response.ok(wrapper).build();
    }

    @GET
    @Path("{codeRegistryCodeValue}")
    @ApiOperation(value = "Return one specific CodeRegistry.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns one specific CodeRegistry in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistry(@ApiParam(value = "CodeRegistry CodeValue.") @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand,
                                    @ApiParam(value = "Value by id") @QueryParam("useId") @DefaultValue("false") final Boolean useId) {
        LOG.info("/v1/coderegistries/ " + codeRegistryCodeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODEREGISTRY, expand)));
        final CodeRegistry codeRegistry = domain.getCodeRegistry(codeRegistryCodeValue, useId);
        if (codeRegistry != null) {
            return Response.ok(codeRegistry).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes")
    @ApiOperation(value = "Return list of available CodeRegistries.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all Registers in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeSchemes(@ApiParam(value = "CodeRegistry CodeValue.") @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                   @ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                   @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                   @ApiParam(value = "CodeSchemeType name as string value.") @QueryParam("type") final String codeSchemeType,
                                   @ApiParam(value = "CodeScheme codeValue as string value.") @QueryParam("codeValue") final String codeSchemeCodeValue,
                                   @ApiParam(value = "CodeScheme PrefLabel as string value.") @QueryParam("prefLabel") final String codeSchemePrefLabel,
                                   @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                   @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/coderegistries/" + codeRegistryCodeValue + "/codeschemes/ requested!");
        final Meta meta = new Meta(200, null, null, after);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODESCHEME, expand)));
        final CodeRegistry codeRegistry = domain.getCodeRegistry(codeRegistryCodeValue, false);
        if (codeRegistry != null) {
            final List<CodeScheme> codeSchemesList = new ArrayList<>();
            final Set<CodeScheme> codeSchemes = domain.getCodeSchemes(pageSize, from, codeRegistryCodeValue, codeSchemeCodeValue, codeSchemePrefLabel, codeSchemeType, meta.getAfter(), meta);
            codeSchemesList.addAll(codeSchemes);
            meta.setResultCount(codeSchemesList.size());
            final ListResponseWrapper<CodeScheme> wrapper = new ListResponseWrapper<>();
            wrapper.setResults(codeSchemesList);
            wrapper.setMeta(meta);
            return Response.ok(wrapper).build();
        } else {
            meta.setCode(404);
            meta.setMessage("No such resource.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes/{codeSchemeCodeValue}")
    @ApiOperation(value = "Return one specific CodeScheme.", response = CodeScheme.class)
    @ApiResponse(code = 200, message = "Returns one specific CodeScheme in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeScheme(@ApiParam(value = "CodeRegistry CodeValue.") @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                                  @ApiParam(value = "CodeScheme CodeValue.") @PathParam("codeSchemeCodeValue") final String codeSchemeCodeValue,
                                  @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand,
                                  @ApiParam(value = "Value by id") @QueryParam("useId") @DefaultValue("false") final Boolean useId) {
        LOG.info("/v1/coderegistries/" + codeRegistryCodeValue + "/codeschemes/" + codeSchemeCodeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODESCHEME, expand)));
        final CodeRegistry codeRegistry = domain.getCodeRegistry(codeRegistryCodeValue, false);
        if (codeRegistry != null) {
            final CodeScheme codeScheme = domain.getCodeScheme(codeRegistryCodeValue, codeSchemeCodeValue, useId);
            return Response.ok(codeScheme).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @GET
    @Path("{codeRegistryCodeValue}/codeschemes/{codeSchemeCodeValue}/codes")
    @ApiOperation(value = "Return content listing of one register.", response = Code.class)
    @ApiResponse(code = 200, message = "Returns a register matching code in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodes(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                             @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                             @ApiParam(value = "CodeRegistry CodeValue.") @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                             @ApiParam(value = "CodeScheme CodeValue.") @PathParam("codeSchemeCodeValue") final String codeSchemeCodeValue,
                             @ApiParam(value = "Code code.") @QueryParam("codeValue") final String codeCodeValue,
                             @ApiParam(value = "Code PrefLabel.") @QueryParam("prefLabel") final String prefLabel,
                             @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                             @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        LOG.info("/v1/coderegistries/" + codeRegistryCodeValue + "/codeschemes/" + codeSchemeCodeValue + "/codes/ requested!");
        final Meta meta = new Meta(Response.Status.OK.getStatusCode(), pageSize, from, after);
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODE, expand)));
        final CodeScheme codeScheme = domain.getCodeScheme(codeRegistryCodeValue, codeSchemeCodeValue, false);
        if (codeScheme != null) {
            final Set<Code> codes = domain.getCodes(pageSize, from, codeRegistryCodeValue, codeSchemeCodeValue, codeCodeValue, prefLabel, meta.getAfter(), meta);
            if (pageSize != null && from + pageSize < meta.getTotalResults()) {
                meta.setNextPage(apiUtils.createNextPageUrl(ApiConstants.API_VERSION, ApiConstants.API_PATH_CODEREGISTRIES, after, pageSize, from + pageSize));
            }
            final ListResponseWrapper<Code> wrapper = new ListResponseWrapper<>();
            wrapper.setMeta(meta);
            if (codes == null) {
                meta.setCode(404);
                meta.setMessage("No such resource.");
                return Response.status(Response.Status.NOT_FOUND).entity(wrapper).build();
            }
            final List<Code> codesList = new ArrayList<>();
            codesList.addAll(codes);
            wrapper.setResults(codesList);
            return Response.ok(wrapper).build();
        } else {
            meta.setCode(404);
            meta.setMessage("No such resource.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{codeRegistryCodeValue}/codeschemes/{codeSchemeCodeValue}/codes/{codeCodeValue}")
    @ApiOperation(value = "Return one code from specific codescheme under specific coderegistry.", response = Code.class)
    @ApiResponse(code = 200, message = "Returns one registeritem from specific register in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCode(@ApiParam(value = "CodeRegistry CodeValue.") @PathParam("codeRegistryCodeValue") final String codeRegistryCodeValue,
                            @ApiParam(value = "CodeScheme CodeValue.") @PathParam("codeSchemeCodeValue") final String codeSchemeCodeValue,
                            @ApiParam(value = "Code code.") @PathParam("codeCodeValue") final String codeCodeValue,
                            @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand,
                            @ApiParam(value = "Value by id") @QueryParam("useId") @DefaultValue("false") final Boolean useId) {
        LOG.info("/v1/coderegistries/" + codeRegistryCodeValue + "/codeschemes/" + codeSchemeCodeValue + "/codes/" + codeCodeValue + "/ requested!");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODE, expand)));
        final Code code = domain.getCode(codeRegistryCodeValue, codeSchemeCodeValue, codeCodeValue, useId);
        if (code == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(code).build();
    }

}
