/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;


@Provider
public class JacksonConfig implements ContextResolver<ObjectMapper> {

    private final ObjectMapper objectMapper;

    public JacksonConfig() {
        this.objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .disable(DeserializationFeature.USE_BIG_INTEGER_FOR_INTS)    
                .disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .setSerializationInclusion(JsonInclude.Include.ALWAYS);
    }
      
    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
