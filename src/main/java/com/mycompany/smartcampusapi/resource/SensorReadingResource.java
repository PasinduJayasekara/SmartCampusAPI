/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.resource;

/**
 *
 * @author Pasindu Jayasekara
 */
import com.mycompany.smartcampusapi.exception.SensorUnavailableException;
import com.mycompany.smartcampusapi.model.Sensor;
import com.mycompany.smartcampusapi.model.SensorReading;
import com.mycompany.smartcampusapi.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SensorReadingResource {

    private static final Logger LOGGER = Logger.getLogger(SensorReadingResource.class.getName());
    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        List<SensorReading> list = store.getReadingsForSensor(sensorId);
        LOGGER.info("Returning " + list.size() + " readings for sensor: " + sensorId);
        return Response.ok(list).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensors().get(sensorId);

        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            LOGGER.warning("Reading POST rejected - sensor under MAINTENANCE: " + sensorId);
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is under maintenance and cannot accept new readings.");
        }

        reading.setId(UUID.randomUUID().toString());
        reading.setTimestamp(System.currentTimeMillis());
        store.getReadingsForSensor(sensorId).add(reading);

        // Side effect: keep parent sensor currentValue in sync
        sensor.setCurrentValue(reading.getValue());
        LOGGER.info("Reading added to sensor: " + sensorId + " | value: " + reading.getValue());

        return Response.status(201).entity(reading).build();
    }
}
