package fi.vm.yti.cls.api.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

@Component
public class CustomObjectMapper extends ObjectMapper {

    private static final long serialVersionUID = 1L;

    public CustomObjectMapper() {
        super();
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

}

