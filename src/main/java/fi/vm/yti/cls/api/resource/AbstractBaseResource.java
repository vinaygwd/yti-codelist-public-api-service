package fi.vm.yti.cls.api.resource;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.jaxrs.cfg.EndpointConfigBase;
import com.fasterxml.jackson.jaxrs.cfg.ObjectWriterModifier;
import fi.vm.yti.cls.api.api.ErrorWrapper;
import fi.vm.yti.cls.common.model.Meta;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


abstract class AbstractBaseResource {

    public static final String FILTER_NAME_STREETADDRESS = "streetAddress";

    public static final String FILTER_NAME_STREETNUMBER = "streetNumber";

    public static final String FILTER_NAME_MUNICIPALITY = "municipality";

    public static final String FILTER_NAME_MAGISTRATE = "magistrate";

    public static final String FILTER_NAME_ELECTORALDISTRICT = "electoralDistrict";

    public static final String FILTER_NAME_POSTALCODE = "postalCode";

    public static final String FILTER_NAME_POSTMANAGEMENTDISTRICT = "postManagementDistrict";

    public static final String FILTER_NAME_REGION = "region";

    public static final String FILTER_NAME_BUSINESSID = "businessId";

    public static final String FILTER_NAME_BUSINESSSERVICESUBREGION = "businessServiceSubRegion";

    public static final String FILTER_NAME_HEALTHCAREDISTRICT = "healthCareDistrict";

    public static final String FILTER_NAME_MAGISTRATESERVICEUNIT = "magistrateServiceUnit";

    public static final String FILTER_NAME_REGISTER = "register";

    public static final String FILTER_NAME_REGISTER_ITEM = "registerItem";


    public void AbstractBaseResource() {
    }


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
        filterProvider.addFilter(FILTER_NAME_MUNICIPALITY, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_MAGISTRATE, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_POSTALCODE, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_REGION, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_STREETADDRESS, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_STREETNUMBER, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_MAGISTRATESERVICEUNIT, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_BUSINESSID, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_HEALTHCAREDISTRICT, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_ELECTORALDISTRICT, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_BUSINESSSERVICESUBREGION, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_POSTMANAGEMENTDISTRICT, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_REGISTER, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
        filterProvider.addFilter(FILTER_NAME_REGISTER_ITEM, SimpleBeanPropertyFilter.filterOutAllExcept("url"));
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

}
