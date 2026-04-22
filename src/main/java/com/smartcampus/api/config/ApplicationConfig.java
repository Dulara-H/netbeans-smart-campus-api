package com.smartcampus.api.config;

import com.smartcampus.api.filter.ApiLoggingFilter;
import com.smartcampus.api.mapper.GlobalExceptionMapper;
import com.smartcampus.api.mapper.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.api.mapper.RoomNotEmptyExceptionMapper;
import com.smartcampus.api.mapper.SensorUnavailableExceptionMapper;
import com.smartcampus.api.resource.DiscoveryResource;
import com.smartcampus.api.resource.RoomResource;
import com.smartcampus.api.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationConfig extends ResourceConfig {

    public ApplicationConfig() {
        register(JacksonFeature.class);

        register(DiscoveryResource.class);
        register(RoomResource.class);
        register(SensorResource.class);

        register(RoomNotEmptyExceptionMapper.class);
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(GlobalExceptionMapper.class);

        register(ApiLoggingFilter.class);
    }
}