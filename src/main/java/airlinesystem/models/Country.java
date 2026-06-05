package airlinesystem.models;

import java.util.List;
import java.util.ArrayList;

public class Country {
    private final String isoCode;
    private String name;
    private EconomicBlock economicBlock;
    private List<String> officialLanguages = new ArrayList<>();

    public Country(String isoCode, String name, EconomicBlock economicBlock, List<String> officialLanguages) {
        if (isoCode == null || isoCode.isBlank()) {
            throw new IllegalArgumentException("ISO code cannot be null or empty");
        }
        if (isoCode.length() != 3) {
            throw new IllegalArgumentException("ISO code must be 3 characters long");
        }
        this.isoCode = isoCode.toUpperCase();
        this.name = name;
        this.economicBlock = economicBlock;
        this.officialLanguages = new ArrayList<>(officialLanguages);
    }

    public String getId() {
        return isoCode;
    }

    public String getName() {
        return name;
    }

    public EconomicBlock getEconomicBlock() {
        return economicBlock;
    }

    public List<String> getOfficialLanguages() {
        return new ArrayList<>(officialLanguages);
    }


    @Override
    public String toString() {
        return String.format(
                """
                Country {
                    ISO code = %s  |  name = %s
                    economic block = %s
                    official languages = %s
                }""",
                isoCode, name, economicBlock, officialLanguages
        );
    }
}

