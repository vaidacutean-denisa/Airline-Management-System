package airlinesystem.models;

public class Airplane {
    private String tailNumber;                              // used to uniquely identify the aircraft (~ID)
    private AirplaneModel airplaneModel;                    // the airplane does not extend an airplane model; it's rather an instance of a model -> composition

    private int pressurizationCycles;                       // = one takeoff and landing; used to determine the aircraft's lifespan
    private int lastRevisionCycles;                         // to decide whether to send it for revision (maintenance) or keep it active
    private AirplaneStatus airplaneStatus;

    // constructor used for new aircraft (default values for status and cycles)
    public Airplane(String tailNumber, AirplaneModel airplaneModel) {
        this.tailNumber = tailNumber;
        this.airplaneModel = airplaneModel;

        this.pressurizationCycles = 2 + (int)(Math.random() * 4);     // a new aircraft has about 2-5 pressurization cycles (completed during testing)
        this.lastRevisionCycles = 0;
        this.airplaneStatus = AirplaneStatus.ACTIVE;
    }

    // constructor used for aircraft already in service
    public Airplane(String tailNumber, AirplaneModel airplaneModel, int pressurizationCycles, int lastRevisionCycles,
                    AirplaneStatus airplaneStatus) {
        this.tailNumber = tailNumber;
        this.airplaneModel = airplaneModel;

        if (pressurizationCycles < 0 || lastRevisionCycles < 0) {
            throw new IllegalArgumentException("Pressurization cycles cannot be negative");
        }

        if (lastRevisionCycles > pressurizationCycles) {
            throw new IllegalArgumentException("Last revision cycles cannot exceed pressurization cycles");
        }

        this.pressurizationCycles = pressurizationCycles;
        this.lastRevisionCycles = lastRevisionCycles;
        this.airplaneStatus = airplaneStatus;
    }

    public String getId() {
        return tailNumber;
    }

    public AirplaneModel getAirplaneModel() {
        return airplaneModel;
    }

    public int getPressurizationCycles() {
        return pressurizationCycles;
    }

    public int getLastRevisionCycles() {
        return lastRevisionCycles;
    }

    public AirplaneStatus getAirplaneStatus() {
        return airplaneStatus;
    }

    public void setPressurizationCycles(int pressurizationCycles) {
        if (pressurizationCycles < 0) {
            throw new IllegalArgumentException("Pressurization cycles cannot be negative");
        }
        this.pressurizationCycles = pressurizationCycles;
    }

    public void setLastRevisionCycles(int lastRevisionCycles) {
        if (lastRevisionCycles < 0) {
            throw new IllegalArgumentException("Last revision cycles cannot be negative");
        }
        if (lastRevisionCycles > pressurizationCycles) {
            throw new IllegalArgumentException("Last revision cycles cannot exceed pressurization cycles");
        }
        this.lastRevisionCycles = lastRevisionCycles;
    }

    public void setAirplaneStatus(AirplaneStatus status) {
        this.airplaneStatus = status;
    }

    @Override
    public String toString() {
        return String.format(
                """
                Airplane {
                    tail number = %s  |  airplane model = %s
                    pressurization cycles = %d  |  cycles since last revision = %d
                    status = %s
                }
                """,
                tailNumber, airplaneModel.getModelName(), pressurizationCycles, lastRevisionCycles, airplaneStatus
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airplane airplane = (Airplane) o;
        return tailNumber != null ? tailNumber.equalsIgnoreCase(airplane.tailNumber) : airplane.tailNumber == null;
    }

    @Override
    public int hashCode() {
        return tailNumber != null ? tailNumber.toLowerCase().hashCode() : 0;
    }
}