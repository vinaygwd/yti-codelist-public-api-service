package fi.vm.yti.codelist.api.resource;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterInjector;

import fi.vm.yti.codelist.api.api.ResponseWrapper;
import fi.vm.yti.codelist.api.domain.Domain;
import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.common.model.ExternalReference;
import fi.vm.yti.codelist.common.model.Meta;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

/**
 * REST resources for ExternalReferences.
 */
@Component
@Path("/v1/externalreferences")
@Api(value = "externalreferences", description = "Operations about ExternalReferences.")
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", "application/xlsx", "application/csv"})
public class ExternalReferenceResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalReferenceResource.class);
    private final Domain domain;

    @Inject
    public ExternalReferenceResource(final Domain domain) {
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return a list of available ExternalReferences.", response = ExternalReference.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all ExternalReferences in specified format.")
    @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", "application/xlsx", "application/csv"})
    public Response getExternalReferences(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                          @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                          @ApiParam(value = "CodeRegistry name as string value.") @QueryParam("name") final String name,
                                          @ApiParam(value = "Format for content.") @QueryParam("format") @DefaultValue(FORMAT_JSON) final String format,
                                          @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                          @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_PROPERTYTYPES);
        if (FORMAT_CSV.equalsIgnoreCase(format)) {
            final Set<ExternalReference> externalReferences = domain.getExternalReferences(pageSize, from, name, Meta.parseAfterFromString(after), null);
            final String csv = constructExternalReferencesCsv(externalReferences);
            final StreamingOutput stream = output -> {
                try {
                    output.write(csv.getBytes(StandardCharsets.UTF_8));
                } catch (final Exception e) {
                    throw new WebApplicationException(e);
                }
            };
            return Response.ok(stream).header(HEADER_CONTENT_DISPOSITION, "attachment; filename = " + createDownloadFilename(format, DOWNLOAD_FILENAME_CODEREGISTRIES)).build();
        } else if (FORMAT_EXCEL.equalsIgnoreCase(format) || FORMAT_EXCEL_XLS.equalsIgnoreCase(format) || FORMAT_EXCEL_XLSX.equalsIgnoreCase(format)) {
            final Set<ExternalReference> externalReferences = domain.getExternalReferences(pageSize, from, name, Meta.parseAfterFromString(after), null);
            final Workbook workbook = constructExternalReferencesExcel(format, externalReferences);
            final StreamingOutput stream = output -> {
                try {
                    workbook.write(output);
                } catch (final Exception e) {
                    throw new WebApplicationException(e);
                }
            };
            return Response.ok(stream).header(HEADER_CONTENT_DISPOSITION, "attachment; filename = " + createDownloadFilename(format, DOWNLOAD_FILENAME_CODEREGISTRIES)).build();
        } else {
            final Meta meta = new Meta(200, null, null, after);
            ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_EXTERNALREFERENCE, expand)));
            final Set<ExternalReference> externalReferences = domain.getExternalReferences(pageSize, from, name, meta.getAfter(), meta);
            meta.setResultCount(externalReferences.size());
            final ResponseWrapper<ExternalReference> wrapper = new ResponseWrapper<>();
            wrapper.setResults(externalReferences);
            wrapper.setMeta(meta);
            return Response.ok(wrapper).build();
        }
    }

    @GET
    @Path("{externalReferenceId}")
    @ApiOperation(value = "Return one specific CodeRegistry.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns one specific CodeRegistry in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getExternalReference(@ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("externalReferenceId") final String externalReferenceId,
                                         @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_PROPERTYTYPES + "/" + externalReferenceId + "/");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_EXTERNALREFERENCE, expand)));
        final ExternalReference externalReference = domain.getExternalReference(externalReferenceId);
        if (externalReference != null) {
            return Response.ok(externalReference).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Set<String> resolveExternalReferenceTitleLanguages(final Set<ExternalReference> externalReferences) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final ExternalReference externalReference : externalReferences) {
            final Map<String, String> titles = externalReference.getTitles();
            languages.addAll(titles.keySet());
        }
        return languages;
    }

    private Set<String> resolveExternalReferenceDescriptionLanguages(final Set<ExternalReference> externalReferences) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final ExternalReference propertyType : externalReferences) {
            final Map<String, String> descriptions = propertyType.getDescriptions();
            languages.addAll(descriptions.keySet());
        }
        return languages;
    }

    public String constructExternalReferencesCsv(final Set<ExternalReference> externalReferences) {
        final Set<String> titleLanguages = resolveExternalReferenceTitleLanguages(externalReferences);
        final Set<String> descriptionLanguages = resolveExternalReferenceDescriptionLanguages(externalReferences);
        final String csvSeparator = ",";
        final StringBuilder csv = new StringBuilder();
        appendValue(csv, csvSeparator, CONTENT_HEADER_ID);
        titleLanguages.forEach(language -> {
            appendValue(csv, csvSeparator, CONTENT_HEADER_PREFLABEL_PREFIX + language.toUpperCase());
        });
        descriptionLanguages.forEach(language -> {
            appendValue(csv, csvSeparator, CONTENT_HEADER_DEFINITION_PREFIX + language.toUpperCase());
        });
        csv.append("\n");
        for (final ExternalReference externalReference : externalReferences) {
            appendValue(csv, csvSeparator, externalReference.getId().toString());
            titleLanguages.forEach(language -> {
                appendValue(csv, csvSeparator, externalReference.getTitles().get(language));
            });
            descriptionLanguages.forEach(language -> {
                appendValue(csv, csvSeparator, externalReference.getDescriptions().get(language));
            });
            csv.append("\n");
        }
        return csv.toString();
    }

    public Workbook constructExternalReferencesExcel(final String format,
                                                     final Set<ExternalReference> externalReferences) {
        final Workbook workbook = createWorkBook(format);
        final Set<String> titleLanguages = resolveExternalReferenceTitleLanguages(externalReferences);
        final Set<String> descriptionLanguages = resolveExternalReferenceDescriptionLanguages(externalReferences);
        final Sheet sheet = workbook.createSheet(EXCEL_SHEET_EXTERNALREFERENCES);
        final Row rowhead = sheet.createRow((short) 0);
        int j = 0;
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_ID);
        for (final String language : titleLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_TITLE_PREFIX + language.toUpperCase());
        }
        for (final String language : descriptionLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_DESCRIPTION_PREFIX + language.toUpperCase());
        }
        int i = 1;
        for (final ExternalReference externalReference : externalReferences) {
            final Row row = sheet.createRow(i++);
            int k = 0;
            row.createCell(k++).setCellValue(checkEmptyValue(externalReference.getId().toString()));
            for (final String language : titleLanguages) {
                row.createCell(k++).setCellValue(externalReference.getTitles().get(language));
            }
            for (final String language : descriptionLanguages) {
                row.createCell(k++).setCellValue(externalReference.getDescriptions().get(language));
            }
        }
        return workbook;
    }
}
