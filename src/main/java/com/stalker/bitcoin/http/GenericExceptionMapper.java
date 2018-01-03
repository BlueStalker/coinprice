package com.stalker.bitcoin.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Created by curt on 1/2/18.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger logger = LoggerFactory.getLogger(GenericExceptionMapper.class);

    public GenericExceptionMapper() {
    }

    public Response toResponse(Throwable ex) {
        if(ex instanceof WebApplicationException) {
            return ((WebApplicationException)ex).getResponse();
        } else {
            logger.error(ex.getMessage(), ex);
            Response response = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type("application/json").entity(Response.Status.INTERNAL_SERVER_ERROR).build();
            return response;
        }
    }
}

