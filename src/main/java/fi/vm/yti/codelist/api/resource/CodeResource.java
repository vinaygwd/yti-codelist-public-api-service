package fi.vm.yti.codelist.api.resource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;

import fi.vm.yti.codelist.api.domain.Domain;
import fi.vm.yti.codelist.common.model.Code;
import fi.vm.yti.codelist.common.model.CodeScheme;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_CODES;
import static fi.vm.yti.codelist.common.constants.ApiConstants.API_PATH_VERSION_V1;
import static fi.vm.yti.codelist.common.constants.ApiConstants.FILTER_NAME_CODE;
import static fi.vm.yti.codelist.common.constants.ApiConstants.METHOD_GET;

/**
 * REST resources for Codes.
 */
@Component
@Path("/v1/codes")
@Api(value = "codes")
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", "application/xlsx", "application/csv"})
public class CodeResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(CodeResource.class);
    private final Domain domain;

    @Inject
    public CodeResource(final Domain domain) {
        this.domain = domain;
    }

    @GET
    @Path("{codeId}")
    @ApiOperation(value = "Return one specific Code.", response = Code.class)
    @ApiResponse(code = 200, message = "Returns one specific Code in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCode(@ApiParam(value = "Code Id.", required = true) @PathParam("codeId") final String codeId,
                            @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_CODES + "/" + codeId + "/");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_CODE, expand)));
        final CodeScheme codeScheme = domain.getCodeScheme(codeId);
        if (codeScheme != null) {
            return Response.ok(codeScheme).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
