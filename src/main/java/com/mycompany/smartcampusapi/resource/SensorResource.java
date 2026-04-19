/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.resource;

/**
 *
 * @author Pasindu Jayasekara
 */
import com.mycompany.smartcampusapi.exception.LinkedResourceNotFoundException;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private static final Logger LOGGER = Logger.getLogger(SensorResource.class.getName());
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getSensors(@QueryParam("type") String type) {
        if (type != null && !type.trim().isEmpty()) {
            LOGGER.info("Filtering sensors by type: " + type);
            List<Sensor> filtered = store.getSensors().values().stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }
        LOGGER.info("Fetching all sensors, count: " + store.getSensors().size());
        return Response.ok(store.getSensors().values()).build();
    }

    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Bad Request");
            err.put("message", "Sensor ID is required");
            return Response.status(400).entity(err).build();
        }
        if (sensor.getRoomId() == null || !store.getRooms().containsKey(sensor.getRoomId())) {
            LOGGER.warning("Sensor creation failed - roomId not found: " + sensor.getRoomId());
            throw new LinkedResourceNotFoundException(
                    "Room '" + sensor.getRoomId() + "' does not exist. Sensor cannot be registered.");
        }
        store.getSensors().put(sensor.getId(), sensor);
        store.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());
        store.getReadingsForSensor(sensor.getId()); // initialise empty list
        LOGGER.info("Sensor created: " + sensor.getId() + " linked to room: " + sensor.getRoomId());
        return Response.status(201).entity(sensor).build();
    }

    // Sub-resource locator for /sensors/{sensorId}/readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        if (!store.getSensors().containsKey(sensorId)) {
            LOGGER.warning("Sub-resource locator: sensor not found: " + sensorId);
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        LOGGER.info("Delegating to SensorReadingResource for sensor: " + sensorId);
        return new SensorReadingResource(sensorId);
    }
}
