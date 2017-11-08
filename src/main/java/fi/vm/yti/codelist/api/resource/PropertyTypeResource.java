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
import fi.vm.yti.codelist.common.model.Meta;
import fi.vm.yti.codelist.common.model.PropertyType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

/**
 * REST resources for PropertyTypes.
 */
@Component
@Path("/v1/propertytypes")
@Api(value = "propertytypes", description = "Operations about PropertyTypes.")
@Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", "application/xlsx", "application/csv"})
public class PropertyTypeResource extends AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalReferenceResource.class);
    private final Domain domain;

    @Inject
    public PropertyTypeResource(final Domain domain) {
        this.domain = domain;
    }

    @GET
    @ApiOperation(value = "Return a list of available PropertyTypes.", response = PropertyType.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns all PropertyTypes in specified format.")
    @Produces({MediaType.APPLICATION_JSON + ";charset=UTF-8", "application/xlsx", "application/csv"})
    public Response getPropertyTypes(@ApiParam(value = "Pagination parameter for page size.") @QueryParam("pageSize") final Integer pageSize,
                                     @ApiParam(value = "Pagination parameter for start index.") @QueryParam("from") @DefaultValue("0") final Integer from,
                                     @ApiParam(value = "CodeRegistry name as string value.") @QueryParam("name") final String name,
                                     @ApiParam(value = "Format for content.") @QueryParam("format") @DefaultValue(FORMAT_JSON) final String format,
                                     @ApiParam(value = "After date filtering parameter, results will be codes with modified date after this ISO 8601 formatted date string.") @QueryParam("after") final String after,
                                     @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_PROPERTYTYPES);
        if (FORMAT_CSV.equalsIgnoreCase(format)) {
            final Set<PropertyType> propertyTypes = domain.getPropertyTypes(pageSize, from, name, Meta.parseAfterFromString(after), null);
            final String csv = constructPropertyTypesCsv(propertyTypes);
            final StreamingOutput stream = output -> {
                try {
                    output.write(csv.getBytes(StandardCharsets.UTF_8));
                } catch (final Exception e) {
                    throw new WebApplicationException(e);
                }
            };
            return Response.ok(stream).header(HEADER_CONTENT_DISPOSITION, "attachment; filename = " + createDownloadFilename(format, DOWNLOAD_FILENAME_CODEREGISTRIES)).build();
        } else if (FORMAT_EXCEL.equalsIgnoreCase(format) || FORMAT_EXCEL_XLS.equalsIgnoreCase(format) || FORMAT_EXCEL_XLSX.equalsIgnoreCase(format)) {
            final Set<PropertyType> propertyTypes = domain.getPropertyTypes(pageSize, from, name, Meta.parseAfterFromString(after), null);
            final Workbook workbook = constructPropertyTypesExcel(format, propertyTypes);
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
            ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_PROPERTYTYPE, expand)));
            final Set<PropertyType> propertyTypes = domain.getPropertyTypes(pageSize, from, name, meta.getAfter(), meta);
            meta.setResultCount(propertyTypes.size());
            final ResponseWrapper<PropertyType> wrapper = new ResponseWrapper<>();
            wrapper.setResults(propertyTypes);
            wrapper.setMeta(meta);
            return Response.ok(wrapper).build();
        }
    }

    @GET
    @Path("{propertyTypeId}")
    @ApiOperation(value = "Return one specific CodeRegistry.", response = CodeRegistry.class, responseContainer = "List")
    @ApiResponse(code = 200, message = "Returns one specific CodeRegistry in JSON format.")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getCodeRegistry(@ApiParam(value = "CodeRegistry CodeValue.", required = true) @PathParam("propertyTypeId") final String propertyTypeId,
                                    @ApiParam(value = "Filter string (csl) for expanding specific child resources.") @QueryParam("expand") final String expand) {
        logApiRequest(LOG, METHOD_GET, API_PATH_VERSION_V1, API_PATH_PROPERTYTYPES + "/" + propertyTypeId + "/");
        ObjectWriterInjector.set(new AbstractBaseResource.FilterModifier(createSimpleFilterProvider(FILTER_NAME_PROPERTYTYPE, expand)));
        final PropertyType propertyType = domain.getPropertyType(propertyTypeId);
        if (propertyType != null) {
            return Response.ok(propertyType).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Set<String> resolvePropertyTypePrefLabelLanguages(final Set<PropertyType> propertyTypes) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final PropertyType propertyType : propertyTypes) {
            final Map<String, String> prefLabels = propertyType.getPrefLabels();
            languages.addAll(prefLabels.keySet());
        }
        return languages;
    }

    private Set<String> resolvePropertyTypeDefinitionLanguages(final Set<PropertyType> propertyTypes) {
        final Set<String> languages = new LinkedHashSet<>();
        for (final PropertyType propertyType : propertyTypes) {
            final Map<String, String> definitions = propertyType.getDefinitions();
            languages.addAll(definitions.keySet());
        }
        return languages;
    }

    public String constructPropertyTypesCsv(final Set<PropertyType> propertyTypes) {
        final Set<String> prefLabelLanguages = resolvePropertyTypePrefLabelLanguages(propertyTypes);
        final Set<String> definitionLanguages = resolvePropertyTypeDefinitionLanguages(propertyTypes);
        final String csvSeparator = ",";
        final StringBuilder csv = new StringBuilder();
        appendValue(csv, csvSeparator, CONTENT_HEADER_ID);
        appendValue(csv, csvSeparator, CONTENT_HEADER_LOCALNAME);
        appendValue(csv, csvSeparator, CONTENT_HEADER_TYPE);
        prefLabelLanguages.forEach(language -> {
            appendValue(csv, csvSeparator, CONTENT_HEADER_PREFLABEL_PREFIX + language.toUpperCase());
        });
        definitionLanguages.forEach(language -> {
            appendValue(csv, csvSeparator, CONTENT_HEADER_DEFINITION_PREFIX + language.toUpperCase());
        });
        csv.append("\n");
        for (final PropertyType propertyType : propertyTypes) {
            appendValue(csv, csvSeparator, propertyType.getId().toString());
            appendValue(csv, csvSeparator, propertyType.getLocalName());
            appendValue(csv, csvSeparator, propertyType.getType());
            prefLabelLanguages.forEach(language -> {
                appendValue(csv, csvSeparator, propertyType.getPrefLabels().get(language));
            });
            definitionLanguages.forEach(language -> {
                appendValue(csv, csvSeparator, propertyType.getDefinitions().get(language));
            });
            csv.append("\n");
        }
        return csv.toString();
    }

    public Workbook constructPropertyTypesExcel(final String format,
                                                final Set<PropertyType> propertyTypes) {
        final Workbook workbook = createWorkBook(format);
        final Set<String> prefLabelLanguages = resolvePropertyTypePrefLabelLanguages(propertyTypes);
        final Set<String> definitionLanguages = resolvePropertyTypeDefinitionLanguages(propertyTypes);
        final Sheet sheet = workbook.createSheet(EXCEL_SHEET_PROPERTYTYPES);
        final Row rowhead = sheet.createRow((short) 0);
        int j = 0;
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_ID);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_LOCALNAME);
        rowhead.createCell(j++).setCellValue(CONTENT_HEADER_TYPE);
        for (final String language : prefLabelLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_PREFLABEL_PREFIX + language.toUpperCase());
        }
        for (final String language : definitionLanguages) {
            rowhead.createCell(j++).setCellValue(CONTENT_HEADER_DEFINITION_PREFIX + language.toUpperCase());
        }
        int i = 1;
        for (final PropertyType propertyType : propertyTypes) {
            final Row row = sheet.createRow(i++);
            int k = 0;
            row.createCell(k++).setCellValue(checkEmptyValue(propertyType.getId().toString()));
            row.createCell(k++).setCellValue(checkEmptyValue(propertyType.getLocalName()));
            row.createCell(k++).setCellValue(checkEmptyValue(propertyType.getType()));
            for (final String language : prefLabelLanguages) {
                row.createCell(k++).setCellValue(propertyType.getPrefLabels().get(language));
            }
            for (final String language : definitionLanguages) {
                row.createCell(k++).setCellValue(propertyType.getDefinitions().get(language));
            }
        }
        return workbook;
    }
}
