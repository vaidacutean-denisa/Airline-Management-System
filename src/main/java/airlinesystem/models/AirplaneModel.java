package airlinesystem.models;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

public class AirplaneModel {
    // identification
    private String id;
    private String modelName;
    private String manufacturer;

    // performance
    private double fuelCapacity;
    private double cruiseSpeed;                         // average speed (Km/h) the aircraft flies at once it's no longer climbing (theoretical)
    private double maxRange;                            // max distance the aircraft can fly between takeoff and landing
    private double fuelConsumptionPerKm;
    private int maintenanceCycles;                      // max number of pressurization cycles between two consecutive aircraft revisions

    // capacity
    private Map<CabinClasses, Integer> cabinCapacity = new HashMap<>();
    private double cargoCapacity;

    public AirplaneModel(String id, String modelName, String manufacturer,
                         double fuelCapacity, double cruiseSpeed, double maxRange, double fuelConsumptionPerKm, int maintenanceCycles,
                         Map<CabinClasses, Integer> cabinCapacity, double cargoCapacity) {

        if (fuelCapacity <= 0 || cruiseSpeed <= 0 || maxRange <= 0 || fuelConsumptionPerKm <= 0 || maintenanceCycles <= 0) {
            throw new IllegalArgumentException("Airplane model parameters cannot be negative or zero");
        }

        if (cabinCapacity == null) {
            throw new IllegalArgumentException("Cabin capacity map cannot be null");
        }

        for (Integer seats: cabinCapacity.values()) {
            if (seats == null || seats < 0) {
                throw new IllegalArgumentException("Cabin capacity cannot be negative or null");
            }
        }

        this.id = id;
        this.modelName = modelName;
        this.manufacturer = manufacturer;
        this.fuelCapacity = fuelCapacity;
        this.cruiseSpeed = cruiseSpeed;
        this.maxRange = maxRange;
        this.fuelConsumptionPerKm = fuelConsumptionPerKm;
        this.maintenanceCycles = maintenanceCycles;
        this.cabinCapacity = new HashMap<>(cabinCapacity);
        this.cargoCapacity = cargoCapacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public double getFuelCapacity() {
        return fuelCapacity;
    }

    public double getCruiseSpeed() {
        return cruiseSpeed;
    }

    public double getMaxRange() {
        return maxRange;
    }

    public double getFuelConsumptionPerKm() {
        return fuelConsumptionPerKm;
    }

    public int getMaintenanceCycles() {
        return maintenanceCycles;
    }

    public Map<CabinClasses, Integer> getCabinCapacity() {
        return new HashMap<>(cabinCapacity);
    }

    public int getSeats() {
        return cabinCapacity.values().stream().reduce(0, Integer::sum);                     // adds each value of the map to the sum, initially 0
    }

    public double getCargoCapacity() {
        return cargoCapacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AirplaneModel that)) {
            return false;
        }
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                """
                AirplaneModel {
                    id: %s  |  model name: %s  |  manufacturer: %s
                    fuel capacity: %.2f  |  cruise speed: %.2f  |  max range: %.2f  |  fuel consumption per km: %.2f
                    maintenance cycles: %d
                    cabin capacity: %s  |  total seats: %d  |  cargo capacity: %.2f
                }
                """,
                id, modelName, manufacturer, fuelCapacity, cruiseSpeed, maxRange, fuelConsumptionPerKm,
                maintenanceCycles, cabinCapacity, getSeats(), cargoCapacity
        );
    }
}