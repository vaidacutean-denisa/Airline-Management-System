package airlinesystem.models;

public enum EconomicBlock {
    EU("European Union"),
    MERCOSUR("Mercosur"),
    ASEAN("Association of Southeast Asian Nations"),
    AU("African Union"),
    USMCA("United States-Mexico-Canada Agreement");

    private final String displayName;

    EconomicBlock(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}