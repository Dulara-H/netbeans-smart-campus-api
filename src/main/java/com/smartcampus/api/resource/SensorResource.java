package com.smartcampus.api.resource;

import com.smartcampus.api.exception.LinkedResourceNotFoundException;
import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.store.DataStore;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(dataStore.getSensors().values());

        if (type == null || type.isBlank()) {
            return sensors;
        }

        return sensors.stream()
                .filter(sensor -> sensor.getType() != null && sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        validateSensor(sensor);

        Room room = dataStore.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException("The roomId '" + sensor.getRoomId() + "' does not exist.");
        }

        Map<String, Sensor> sensors = dataStore.getSensors();
        if (sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("message", "Sensor with ID " + sensor.getId() + " already exists."))
                    .build();
        }

        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());
        dataStore.getReadings().put(sensor.getId(), new ArrayList<>());

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location)
                .entity(sensor)
                .build();
    }

    @GET
    @Path("/{sensorId}")
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensors().get(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor with ID " + sensorId + " was not found.");
        }
        return sensor;
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingSubResource(@PathParam("sensorId") String sensorId) {
        if (!dataStore.getSensors().containsKey(sensorId)) {
            throw new NotFoundException("Sensor with ID " + sensorId + " was not found.");
        }
        return new SensorReadingResource(sensorId);
    }

    private void validateSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()
                || sensor.getType() == null || sensor.getType().isBlank()
                || sensor.getStatus() == null || sensor.getStatus().isBlank()
                || sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            throw new javax.ws.rs.BadRequestException("Sensor id, type, status, and roomId are required.");
        }
    }
}
