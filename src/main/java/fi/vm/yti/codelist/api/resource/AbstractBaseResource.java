package fi.vm.yti.codelist.api.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;
import fi.vm.yti.codelist.api.api.ErrorWrapper;
import fi.vm.yti.codelist.common.model.Code;
import fi.vm.yti.codelist.common.model.CodeRegistry;
import fi.vm.yti.codelist.common.model.CodeScheme;
import fi.vm.yti.codelist.common.model.Meta;
import fi.vm.yti.codelist.common.model.Status;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import static fi.vm.yti.codelist.common.constants.ApiConstants.*;

abstract class AbstractBaseResource {

    public static final String FILTER_NAME_CODEREGISTRY = "codeRegistry";
    public static final String FILTER_NAME_CODESCHEME = "codeScheme";
    public static final String FILTER_NAME_CODE = "code";
    public static final String FIELD_NAME_URI = "uri";

    static class FilterModifier extends ObjectWriterModifier {
        private final FilterProvider provider;
        protected FilterModifier(final FilterProvider provider) {
            this.provider = provider;
        }
        @Override
        public ObjectWriter modify(final EndpointConfigBase<?> endpoint,
                                   final MultivaluedMap<String, Object> responseHeaders,
                                   final Object valueToWrite,
                                   final ObjectWriter w,
                                   final JsonGenerator g) throws IOException {
            return w.with(provider);
        }
    }

    public SimpleFilterProvider createSimpleFilterProvider(final String baseFilter,
                                                           final String expand) {
        final List<String> baseFilters = new ArrayList<>();
        baseFilters.add(baseFilter);
        return createSimpleFilterProvider(baseFilters, expand);
    }

    public SimpleFilterProvider createSimpleFilterProvider(final List<String> baseFilters,
                                                           final String expand) {
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(FILTER_NAME_CODEREGISTRY, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_CODESCHEME, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.addFilter(FILTER_NAME_CODE, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_URI));
        filterProvider.setFailOnUnknownId(false);
        for (final String baseFilter : baseFilters) {
            filterProvider.removeFilter(baseFilter);
        }
        if (expand != null && !expand.isEmpty()) {
            final List<String> filterOptions = Arrays.asList(expand.split(","));
            for (final String filter : filterOptions) {
                filterProvider.removeFilter(filter);
            }
        }
        return filterProvider;
    }

    Response createErrorResponse(final int errorCode,
                                 final String errorMessage) {
        final ErrorWrapper error = new ErrorWrapper();
        final Meta meta = new Meta();
        meta.setCode(errorCode);
        meta.setMessage(errorMessage);
        error.setMeta(meta);
        return Response.status(Response.Status.NOT_FOUND).entity(error).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    public List<String> parseStatus(final String statusCsl) {
        final Set<String> statusSet = new HashSet<>();
        if (statusCsl != null) {
            for (final String s : Arrays.asList(statusCsl.split(","))) {
                final Status status = Status.valueOf(s.trim());
                if (status != null) {
                    statusSet.add(status.toString());
                }
            }
        }
        return new ArrayList<String>(statusSet);
    }

    public void logApiRequest(final Logger logger,
                              final String method,
                              final String apiVersionPath,
                              final String apiPath) {
        logger.info(method + " " + apiVersionPath + apiPath + " requested!");
    }

    public void appendNotNull(final StringBuilder builder,
                              final String string) {
        if (string != null) {
            builder.append(string);
        }
    }

    public String constructRegistersCsv(final Set<CodeRegistry> registries) {
        final String csvSeparator = ",";
        final StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER_CODEVALUE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_ID);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_EN);
        csv.append("\n");
        for (final CodeRegistry codeRegistry : registries) {
            csv.append(codeRegistry.getCodeValue());
            csv.append(csvSeparator);
            csv.append(codeRegistry.getId());
            csv.append(csvSeparator);
            appendNotNull(csv, codeRegistry.getPrefLabels().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, codeRegistry.getPrefLabels().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, codeRegistry.getPrefLabels().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, codeRegistry.getDefinitions().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, codeRegistry.getDefinitions().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, codeRegistry.getDefinitions().get(LANGUAGE_CODE_EN));
            csv.append("\n");
        }
        return csv.toString();
    }

    public String constructCodeSchemesCsv(final Set<CodeScheme> codeSchemes) {
        final String csvSeparator = ",";
        final StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER_CODEVALUE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_ID);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_STATUS);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DESCRIPTION_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DESCRIPTION_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DESCRIPTION_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_CHANGENOTE_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_CHANGENOTE_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_CHANGENOTE_EN);
        csv.append("\n");
        for (final CodeScheme codeScheme : codeSchemes) {
            csv.append(codeScheme.getCodeValue());
            csv.append(csvSeparator);
            csv.append(codeScheme.getId());
            csv.append(csvSeparator);
            csv.append(codeScheme.getStatus());
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getPrefLabels().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getPrefLabels().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getPrefLabels().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getDefinitions().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getDefinitions().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getDefinitions().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getDescriptions().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getDescriptions().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getDescriptions().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getChangeNotes().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getChangeNotes().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, codeScheme.getChangeNotes().get(LANGUAGE_CODE_EN));
            csv.append("\n");
        }
        return csv.toString();
    }

    public String constructCodesCsv(final Set<Code> codes) {
        final String csvSeparator = ",";
        final StringBuilder csv = new StringBuilder();
        csv.append(CSV_HEADER_CODEVALUE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_ID);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_STATUS);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_PREFLABEL_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DEFINITION_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DESCRIPTION_FI);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DESCRIPTION_SE);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_DESCRIPTION_EN);
        csv.append(csvSeparator);
        csv.append(CSV_HEADER_SHORTNAME);
        csv.append("\n");
        for (final Code code : codes) {
            csv.append(code.getCodeValue());
            csv.append(csvSeparator);
            csv.append(code.getId());
            csv.append(csvSeparator);
            csv.append(code.getStatus());
            csv.append(csvSeparator);
            appendNotNull(csv, code.getPrefLabels().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getPrefLabels().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getPrefLabels().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getDefinitions().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getDefinitions().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getDefinitions().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getDescriptions().get(LANGUAGE_CODE_FI));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getDescriptions().get(LANGUAGE_CODE_SE));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getDescriptions().get(LANGUAGE_CODE_EN));
            csv.append(csvSeparator);
            appendNotNull(csv, code.getShortName());
            csv.append("\n");
        }
        return csv.toString();
    }
}
