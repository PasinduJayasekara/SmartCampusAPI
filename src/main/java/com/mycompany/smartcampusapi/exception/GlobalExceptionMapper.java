/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.exception;

/**
 *
 * @author Pasindu Jayasekara
 */
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        LOGGER.severe("Unhandled error: " + ex.getClass().getName() + " - " + ex.getMessage());
        Map<String, String> body = new HashMap<>();
        body.put("error", "Internal Server Error");
        body.put("status", "500");
        body.put("message", "An unexpected error occurred. Contact the administrator.");
        return Response.status(500).type(MediaType.APPLICATION_JSON).entity(body).build();
    }
}
