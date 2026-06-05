package airlinesystem.models;

public class Airport {
    private String id;
    private String name;
    private City city;

    public Airport(String id, String name, City city) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Airport ID cannot be null or empty");
        }
        if (id.length() != 3) {
            throw new IllegalArgumentException("Airport ID must be 3 characters long");
        }

        this.id = id.toUpperCase();
        this.name = name;
        this.city = city;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return city;
    }

    @Override
    public String toString() {
        return String.format("""
                Airport {
                id: %s
                name: %s
                city: %s
                }""",
                id, name, city
        );
    }
}