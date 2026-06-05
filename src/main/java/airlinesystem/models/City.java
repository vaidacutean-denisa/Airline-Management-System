package airlinesystem.models;

import java.time.ZoneId;

public class City {
    private String id;
    private String name;
    private Country country;
    private ZoneId timezone;

    public City(String id, String name, Country country, ZoneId timezone) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("City name cannot be null or empty");
        }
        this.id = id;
        this.name = name;
        this.country = country;
        this.timezone = timezone;                // ex. ZoneId.of('Europe/Paris'); handles DST transitions automatically
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Country getCountry() {
        return country;
    }

    public ZoneId getTimezone() {
        return timezone;                                    // immutable -> no need to apply defensive copying
    }

    @Override
    public String toString() {
        return String.format(
                """
                City {
                id: %s  |  name: %s  |  country: %s
                timezone: %s
                }""",
                id, name, country.getName(), timezone
        );
    }
}