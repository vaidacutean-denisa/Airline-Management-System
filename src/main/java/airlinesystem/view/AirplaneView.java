package airlinesystem.view;

import airlinesystem.exceptions.InvalidId;
import airlinesystem.exceptions.InvalidOption;
import airlinesystem.models.Airplane;
import airlinesystem.models.AirplaneModel;
import airlinesystem.models.AirplaneStatus;
import airlinesystem.services.AirplaneModelService;
import airlinesystem.services.AirplaneService;

import java.util.List;

import static airlinesystem.utils.ReaderUtils.*;
import static airlinesystem.utils.ExitOperationUtil.isExit;

public class AirplaneView {
    private final AirplaneService airplaneService;
    private final AirplaneModelService airplaneModelService;

    public AirplaneView(AirplaneService airplaneService, AirplaneModelService airplaneModelService) {
        this.airplaneService = airplaneService;
        this.airplaneModelService = airplaneModelService;
    }

    public void run() {
        while (true) {
            showAirplaneMenu();
            try {
                int option = readOption();
                int status = execAirplaneOption(option);
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

    private void showAirplaneMenu() {
        System.out.println("\n-------- Airplane Menu --------");
        System.out.println("1. Add airplane");
        System.out.println("2. Remove airplane");
        System.out.println("3. Update airplane");
        System.out.println("4. List airplanes");
        System.out.println("5. Check a specific airplane");
        System.out.println("9. Back");
    }

    private int execAirplaneOption(int option) throws InvalidId {
        switch (option) {
            case 1:
                addAirplane();
                break;
            case 2:
                deleteAirplane();
                break;
            case 3:
                updateAirplane();
                break;
            case 4:
                listAirplanes();
                break;
            case 5:
                checkAirplane();
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }

    private void addAirplane() {
        System.out.println("Enter airplane details: ");
        AirplaneModel airplaneModel = readAirplaneModel();
        if (airplaneModel == null) {
            return;
        }

        String tailNumber = readString("Tail number: ");
        int pressurizationCycles = readInt("Total pressurization cycles: ");
        int lastRevisionCycle = readInt("Cycles since last revision: ");
        AirplaneStatus airplaneStatus = readAirplaneStatus();

        try {
            Airplane newAirplane = new Airplane(tailNumber, airplaneModel, pressurizationCycles, lastRevisionCycle, airplaneStatus);
            airplaneService.addAirplane(newAirplane);
            System.out.println("Airplane added successfully.");

        } catch (IllegalArgumentException e) {
            System.out.println("Failed to add airplane: " + e.getMessage());
        }
    }

    private AirplaneModel readAirplaneModel() {
        if (airplaneModelService.getAllAirplaneModels().isEmpty()) {
            System.out.println("No airplane models available. Please add a model first.");
            return null;
        }
        while (true) {
            String modelId = readString("Enter airplane model ID (type 'exit' to cancel): ");
            if (isExit(modelId)) {
                return null;
            }
            AirplaneModel airplaneModel = airplaneModelService.getAirplaneModel(modelId);
            if (airplaneModel != null) {
                return airplaneModel;
            }
            System.out.println("Invalid model ID. Please retry.");
        }
    }

    private AirplaneStatus readAirplaneStatus() {
        String input = readString("Enter airplane status: ACTIVE, MAINTENANCE, RETIRED: ");
        try {
            return AirplaneStatus.valueOf(input.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid airplane status. Please retry.");
            return readAirplaneStatus();
        }
    }

    private void deleteAirplane() throws InvalidId {
        String airplaneId = readString("Enter airplane ID (type 'exit' to cancel): ");
        if (isExit(airplaneId)) {
            return;
        }
        Airplane airplane = airplaneService.getAirplane(airplaneId);
        if (airplane == null) {
            System.out.println("Airplane with ID " + airplaneId + " not found.");
            return;
        }
        airplaneService.deleteAirplane(airplaneId);
        System.out.println("Airplane deleted successfully.");
    }

    private void updateAirplane() {
        String tailNumber = readString("Enter the tail number of the airplane to update (type 'exit' to cancel): ");
        if (isExit(tailNumber)) {
            return;
        }
        Airplane airplane = airplaneService.getAirplane(tailNumber);
        if (airplane == null) {
            System.out.println("Airplane with tail number " + tailNumber + " not found.");
            return;
        }

        String modelId = readString("Enter new model ID (e.g., A320) (type 'exit' to cancel): ");
        if (isExit(modelId))
            return;

        AirplaneModel model = airplaneModelService.getAirplaneModel(modelId);
        if (model == null) {
            System.out.println("Airplane model not found. Update aborted.");
            return;
        }

        int pressCycles = readInt("Total pressurization cycles: ");
        int lastRevisionCycles = readInt("Cycles since last revision: ");

        System.out.println("Select status: 1. ACTIVE | 2. MAINTENANCE | 3. RETIRED");
        int statusOption = readInt("Option: ");
        AirplaneStatus status;

        if (statusOption == 1) {
            status = AirplaneStatus.ACTIVE;
        } else if (statusOption == 2) {
            status = AirplaneStatus.MAINTENANCE;
        } else {
            status = AirplaneStatus.RETIRED;
        }

        Airplane updatedAirplane = new Airplane(tailNumber, model, pressCycles, lastRevisionCycles, status);

        airplaneService.updateAirplane(updatedAirplane);
        System.out.println("Airplane updated successfully.");
    }

    private void listAirplanes() {
        List<Airplane> airplanes = airplaneService.getAllAirplanes();
        if (airplanes.isEmpty()) {
            System.out.println("The list is empty. Try adding an airplane.");
            return;
        }
        System.out.println("\n================ Registered airplanes ================");
        for (Airplane airplane : airplanes) {
            System.out.println(airplane);
            System.out.println("-------------------------------------------------------");
        }
    }

    private void checkAirplane() throws InvalidId {
        String airplaneId = readString("Enter airplane ID (type 'exit' to cancel):");
        if (isExit(airplaneId)) {
            return;
        }

        Airplane airplane = airplaneService.getAirplane(airplaneId);
        if (airplane == null) {
            System.out.println("Airplane with ID " + airplaneId + " not found.");
            return;
        }
        while (true) {
            showAirplaneIdMenu(airplane.getId());
            try {
                int option = readOption();
                int status = execAirplaneIdOptions(option, airplane);
                if (status == -1) {
                    return;
                }
            } catch (InvalidOption e) {
                System.out.println("Invalid option. Please retry.");
            }
        }
    }

    private void showAirplaneIdMenu(String airplaneId) {
        System.out.println("Airplane ID: " + airplaneId + " menu");
        System.out.println("1. Show airplane model");
        System.out.println("2. Show airplane information (detailed)");
        System.out.println("3. Release airplane from revision");
        System.out.println("4. Retire airplane");
        System.out.println("9. Exit");
    }

    private int execAirplaneIdOptions(int option, Airplane airplane) {
        switch (option) {
            case 1:
                System.out.println(airplane.getAirplaneModel());
                break;
            case 2:
                System.out.println(airplane);
                break;
            case 3:
                airplaneService.releaseFromRevision(airplane.getId());
                System.out.println("Airplane released from revision successfully.");
                break;
            case 4:
                airplaneService.retireAirplane(airplane.getId());
                System.out.println("Airplane retired successfully.");
                break;
            case 9:
                System.out.println("Exiting..");
                return -1;
            default:
                System.out.println("Invalid choice. Please enter a valid option.");
        }
        return 0;
    }
}
