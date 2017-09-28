package fi.vm.yti.cls.api.resource;

import fi.vm.yti.cls.api.configuration.VersionInformation;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import static fi.vm.yti.cls.common.constants.ApiConstants.API_VERSION;

@Component
@Path("/version")
@Api(value = "version", description = "Returns version information of the running application.")
@Produces("text/plain")
public class VersionResource {

    private static final Logger LOG = LoggerFactory.getLogger(VersionResource.class);
    private VersionInformation versionInformation;

    public VersionResource(final VersionInformation versionInformation) {
        this.versionInformation = versionInformation;
    }

    @GET
    @ApiOperation(value = "Get version information", response = String.class)
    @ApiResponse(code = 200, message = "Returns the version of the running Public API Service application.")
    public String getVersionInformation() {
        LOG.info(API_VERSION + " called");
        return "\n" +
               "       .__                                 ___.   .__  .__        \n" +
               "  ____ |  |   ______           ______  __ _\\_ |__ |  | |__| ____  \n" +
               "_/ ___\\|  |  /  ___/   ______  \\____ \\|  |  \\ __ \\|  | |  |/ ___\\ \n" +
               "\\  \\___|  |__\\___ \\   /_____/  |  |_> >  |  / \\_\\ \\  |_|  \\  \\___ \n" +
               " \\___  >____/____  >           |   __/|____/|___  /____/__|\\___  >\n" +
               "     \\/          \\/            |__|             \\/             \\/ \n" +
               "              .__                            .__              \n" +
               "_____  ______ |__|   ______ ______________  _|__| ____  ____  \n" +
               "\\__  \\ \\____ \\|  |  /  ___// __ \\_  __ \\  \\/ /  |/ ___\\/ __ \\ \n" +
               " / __ \\|  |_> >  |  \\___ \\\\  ___/|  | \\/\\   /|  \\  \\__\\  ___/ \n" +
               "(____  /   __/|__| /____  >\\___  >__|    \\_/ |__|\\___  >___  >\n" +
               "     \\/|__|             \\/     \\/                    \\/    \\/ \n" +
               "\n" +
               "                --- Version " + versionInformation.getVersion() + " running. --- \n";
    }

}
