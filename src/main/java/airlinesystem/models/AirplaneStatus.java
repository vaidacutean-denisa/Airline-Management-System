package airlinesystem.models;

public enum AirplaneStatus {
    ACTIVE,                         // available for flight
    MAINTENANCE,                    // currently in maintenance (unavailable)
    RETIRED                         // permanently discontinued
//    GROUNDED,                       // cannot take off due to safety risks (either mechanical or meteorological conditions)
}
