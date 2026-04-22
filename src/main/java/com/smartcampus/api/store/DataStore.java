package com.smartcampus.api.store;

import com.smartcampus.api.model.Room;
import com.smartcampus.api.model.Sensor;
import com.smartcampus.api.model.SensorReading;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Map<String, List<SensorReading>> getReadings() {
        return readings;
    }

    private void seedData() {
        Room room = new Room("LIB-301", "Library Quiet Study", 80);
        rooms.put(room.getId(), room);

        Sensor sensor = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, room.getId());
        sensors.put(sensor.getId(), sensor);
        room.getSensorIds().add(sensor.getId());

        List<SensorReading> sensorHistory = new ArrayList<>();
        sensorHistory.add(new SensorReading("READ-001", System.currentTimeMillis(), 22.5));
        readings.put(sensor.getId(), sensorHistory);
    }
}
