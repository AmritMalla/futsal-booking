package com.amrit.futsal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ObjectMapper -> The ObjectMapper will define how the JSON Strings in the request body are deserialized
// to the Plain Old Java Objects (POJOs). which we use to model our data.
// ObjectWriter -> The ObjectWriter will define how to serialize our Java Objects into JSON String in
// the response body

/*in summary ObjectMapper ==> JSON to POJOs
			ObjectWriter ==> POJOs to JSON
*/
@Configuration
public class ApiConfig {

    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new ObjectMapper();
    }

    @Bean
    public ObjectWriter objectWriter(ObjectMapper objectMapper) {
        return objectMapper.writerWithDefaultPrettyPrinter();
    }
}
