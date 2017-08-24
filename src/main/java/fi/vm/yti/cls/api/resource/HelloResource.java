package fi.vm.yti.cls.api.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;


/**
 * Hello stub resource for API connectivity testing.
 */
@Component
@Path("/hello")
@Api(value = "hello", description = "Example public hello operation.")
@Produces("text/plain")
public class HelloResource {

    private static final Logger LOG = LoggerFactory.getLogger(HelloResource.class);

    @GET
    @ApiOperation(value = "Say hello", response = String.class)
    @ApiResponse(code = 200, message = "Returns a hello greeting message.")
    public String hello() {

        LOG.info("/hello called");

        return "       .__           .__           .__  .__          \n" +
               "  ____ |  |   ______ |  |__   ____ |  | |  |   ____  \n" +
               "_/ ___\\|  |  /  ___/ |  |  \\_/ __ \\|  | |  |  /  _ \\ \n" +
               "\\  \\___|  |__\\___ \\  |   Y  \\  ___/|  |_|  |_(  <_> )\n" +
               " \\___  >____/____  > |___|  /\\___  >____/____/\\____/ \n" +
               "     \\/          \\/       \\/     \\/                  \n";

    }

}
