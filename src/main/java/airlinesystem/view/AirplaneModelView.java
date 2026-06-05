package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.AirplaneModel;
import airlinesystem.models.CabinClasses;
import airlinesystem.services.AirplaneModelService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class AirplaneModelView {
    private final AirplaneModelService airplaneModelService;

    public AirplaneModelView(AirplaneModelService airplaneModelService) {
        this.airplaneModelService = airplaneModelService;
    }

    void run() {
        while (true) {
            showAirplaneModelMenu();
            try {
                int option = readOption();
                int status = execAirplaneModelOption(option);
                if (status == -1) {
                    break;
                }
            } catch (InvalidOption e) {
                System.out.println(e.getMessage());
            } catch (InvalidId invalidId) {
                System.out.println("Invalid ID.");
            }
        }
    }

    private void showAirplaneModelMenu() {
        System.out.println("\n-------- Airplane Model Menu --------");
        System.out.println("1. Add airplane model");
        System.out.println("2. Remove airplane model");
        System.out.println("3. Update airplane model");
        System.out.println("4. List airplane models");
        System.out.println("5. Check a specific airplane model");
        System.out.println("6. Get airplane models by manufacturer");
        System.out.println("9. Back");
    }

    private int execAirplaneModelOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addAirplaneModel();
                break;
            case 2:
                deleteAirplaneModel();
                break;
            case 3:
                updateAirplaneModel();
                break;
            case 4:
                listAirplaneModels();
                break;
            case 5:
                checkAirplaneModel();
                break;
            case 6:
                getModelsByManufacturer();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addAirplaneModel() {
        System.out.println("Enter airplane model details (type 'exit' to cancel): ");
        String modelId = readString("Model ID: ");
        if (isExit(modelId)) {
            return;
        }

        String modelName = readString("Model Name: ");
        String manufacturer = readString("Manufacturer: ");
        double fuelCapacity = readDouble("Fuel capacity: ");
        double cruiseSpeed = readDouble("Cruise speed (km/h): ");
        double maxRange = readDouble("Maximum range (km): ");
        double fuelConsumption = readDouble("Fuel consumption per km: ");
        int maintenanceCycles = readInt("Enter maintenance cycles limit: ");
        double cargoCapacity = readDouble("Cargo capacity (kg): ");

        Map<CabinClasses, Integer> cabinCapacity = new HashMap<>();
        System.out.println("Enter seat capacities for cabins: ");
        int economySeats = readInt("Economy seats: ");
        int businessSeats = readInt("Business seats: ");
        int firstClassSeats = readInt("First class seats: ");

        if (economySeats >= 0)
            cabinCapacity.put(CabinClasses.ECONOMY, economySeats);

        if (businessSeats >= 0)
            cabinCapacity.put(CabinClasses.BUSINESS, businessSeats);

        if (firstClassSeats >= 0)
            cabinCapacity.put(CabinClasses.FIRST_CLASS, firstClassSeats);
        try {
            AirplaneModel newModel = new AirplaneModel(modelId, modelName, manufacturer, fuelCapacity, cruiseSpeed,
                    maxRange, fuelConsumption, maintenanceCycles, cabinCapacity, cargoCapacity);

            airplaneModelService.addAirplaneModel(newModel);
            System.out.println("Airplane model added successfully!");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to create model: " + e.getMessage());
        }
    }

    private void deleteAirplaneModel() throws InvalidId {
        String modelId = readString("Enter model id (or type 'exit' to cancel): ");
        if (isExit(modelId)) {
            return;
        }

        AirplaneModel airplaneModel = airplaneModelService.getAirplaneModel(modelId);
        if (airplaneModel == null) {
            System.out.println("Model with id " + modelId + " not found.");
            return;
        }
        airplaneModelService.deleteAirplaneModel(modelId);
        System.out.println("Airplane model deleted successfully.");
    }

    private void updateAirplaneModel() {
        String id = readString("Enter the airplane model ID to update (type 'exit' to cancel): ");
        if (isExit(id)) return;

        AirplaneModel existingModel = airplaneModelService.getAirplaneModel(id);
        if (existingModel == null) {
            System.out.println("Airplane model with ID " + id + " not found.");
            return;
        }

        String modelName = readString("Enter new model name (type 'exit' to cancel): ");
        if (isExit(modelName))
            return;

        String manufacturer = readString("Enter new manufacturer: ");
        double fuelCapacity = readDouble("Enter new fuel capacity: ");
        double cruiseSpeed = readDouble("Enter new cruise speed: ");
        double maxRange = readDouble("Enter new max range: ");
        double fuelConsumption = readDouble("Enter new fuel consumption per km: ");
        int maintenanceCycles = readInt("Enter new maintenance cycles: ");
        double cargoCapacity = readDouble("Enter new cargo capacity: ");

        int economySeats = readInt("Economy class seats: ");
        int businessSeats = readInt("Business class seats: ");
        int firstClassSeats = readInt("First class seats: ");

        Map<CabinClasses, Integer> cabinCapacity = new HashMap<>();
        cabinCapacity.put(CabinClasses.ECONOMY, economySeats);
        cabinCapacity.put(CabinClasses.BUSINESS, businessSeats);
        cabinCapacity.put(CabinClasses.FIRST_CLASS, firstClassSeats);

        AirplaneModel updatedModel = new AirplaneModel(id, modelName, manufacturer, fuelCapacity, cruiseSpeed,
                maxRange, fuelConsumption, maintenanceCycles, cabinCapacity, cargoCapacity);

        airplaneModelService.updateAirplaneModel(updatedModel);
        System.out.println("Airplane model updated successfully.");
    }

    private void listAirplaneModels() {
        List<AirplaneModel> airplaneModels = airplaneModelService.getAllAirplaneModels();
        if (airplaneModels.isEmpty()) {
            System.out.println("The list is empty. Try adding a model.");
            return;
        }
        System.out.println("\n================ Registered airplane models ================");
        for (AirplaneModel model : airplaneModels) {
            System.out.println(model);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void getModelsByManufacturer() {
        String manufacturer = readString("Enter manufacturer name: ");
        if (manufacturer.trim().isEmpty()) {
            System.out.println("Manufacturer name cannot be empty. Please try again.");
            return;
        }
        List<AirplaneModel> airplaneModels = airplaneModelService.getModelsByManufacturer(manufacturer);

        if (airplaneModels.isEmpty()) {
            System.out.println("No airplane models found for manufacturer '" + manufacturer + "'.");
            return;
        }

        System.out.println("Airplane models by manufacturer '" + manufacturer + "':");
        for (AirplaneModel model : airplaneModels) {
            System.out.println(model);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkAirplaneModel() throws InvalidId {
        String modelId = readString("Enter model id (or type 'exit' to cancel): ");
        if (isExit(modelId)) {
            return;
        }

        AirplaneModel airplaneModel = airplaneModelService.getAirplaneModel(modelId);
        if (airplaneModel == null) {
            System.out.println("Model with id " + modelId + " not found.");
            return;
        }
        System.out.print(airplaneModel);
    }
}
