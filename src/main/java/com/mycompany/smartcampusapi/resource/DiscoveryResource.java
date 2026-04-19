/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.resource;

/**
 *
 * @author Pasindu Jayasekara
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/")
public class DiscoveryResource {

    private static final Logger LOGGER = Logger.getLogger(DiscoveryResource.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response discover() {
        LOGGER.info("Discovery endpoint called");
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put("version", "1.0");
        meta.put("name", "Smart Campus Sensor & Room Management API");
        meta.put("contact", "admin@smartcampus.ac.uk");
        Map<String, String> links = new LinkedHashMap<>();
        links.put("rooms", "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        meta.put("resources", links);
        return Response.ok(meta).build();
    }
}
