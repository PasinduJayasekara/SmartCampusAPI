/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.filter;

/**
 *
 * @author Pasindu Jayasekara
 */
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final Logger LOGGER = Logger.getLogger(AuthFilter.class.getName());
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String VALID_KEY = "smartcampus-secret-2026";

    @Override
    public void filter(ContainerRequestContext ctx) throws IOException {
        String path = ctx.getUriInfo().getPath();
        // Let the discovery endpoint through without a key
        if (path == null || path.isEmpty() || path.equals("/")) {
            return;
        }

        String key = ctx.getHeaderString(API_KEY_HEADER);
        if (key == null || !key.equals(VALID_KEY)) {
            LOGGER.warning("Unauthorized request blocked - path: " + path);
            Map<String, String> body = new HashMap<>();
            body.put("error", "Unauthorized");
            body.put("message", "A valid X-API-Key header is required.");
            ctx.abortWith(Response.status(401)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(body)
                    .build());
        }
    }
}
