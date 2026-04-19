/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.smartcampusapi.resource;

/**
 *
 * @author Pasindu Jayasekara
 */
import com.mycompany.smartcampusapi.exception.RoomNotEmptyException;
import com.mycompany.smartcampusapi.model.Room;
import com.mycompany.smartcampusapi.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private static final Logger LOGGER = Logger.getLogger(RoomResource.class.getName());
    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        LOGGER.info("Fetching all rooms, count: " + store.getRooms().size());
        return Response.ok(store.getRooms().values()).build();
    }

    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Bad Request");
            err.put("message", "Room ID is required");
            return Response.status(400).entity(err).build();
        }
        if (store.getRooms().containsKey(room.getId())) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Conflict");
            err.put("message", "Room ID already exists: " + room.getId());
            return Response.status(409).entity(err).build();
        }
        store.getRooms().put(room.getId(), room);
        LOGGER.info("Room created: " + room.getId());
        return Response.status(201)
                .header("Location", "/api/v1/rooms/" + room.getId())
                .entity(room)
                .build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            LOGGER.warning("Room not found: " + roomId);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room not found: " + roomId);
            return Response.status(404).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRooms().get(roomId);
        if (room == null) {
            LOGGER.warning("Delete on non-existent room: " + roomId);
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room not found: " + roomId);
            return Response.status(404).entity(err).build();
        }
        if (!room.getSensorIds().isEmpty()) {
            LOGGER.warning("Delete blocked - room has active sensors: " + roomId);
            throw new RoomNotEmptyException(
                    "Room '" + roomId + "' cannot be deleted. It has "
                    + room.getSensorIds().size() + " sensor(s) still assigned.");
        }
        store.getRooms().remove(roomId);
        LOGGER.info("Room deleted successfully: " + roomId);
        return Response.noContent().build();
    }
}
