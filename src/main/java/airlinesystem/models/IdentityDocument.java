package airlinesystem.models;

import java.time.LocalDate;

public class IdentityDocument {
    private String documentNumber;
    private DocumentType documentType;
    private LocalDate expiryDate;
    private Country issuingCountry;                  // visa: issuing country = dest country; for others must check equivalence with nationality

    public IdentityDocument(String documentNumber, DocumentType documentType, LocalDate expiryDate, Country issuingCountry) {
//        if (documentType == DocumentType.VISA && issuingCountry == null) {
//            throw new IllegalArgumentException("Visa documents must have an issuing country.");
//        }    // the idea might be useful

        this.documentNumber = documentNumber;
        this.documentType = documentType;
        this.expiryDate = expiryDate;
        this.issuingCountry = issuingCountry;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public Country getIssuingCountry() {
        return issuingCountry;
    }

    @Override
    public String toString() {
        String countryInfo = (issuingCountry != null) ? issuingCountry.getId() : "N/A";

        return String.format("%s (ID: %s, Expires: %s, Issued by: %s)",
                documentType, documentNumber, expiryDate, countryInfo);
    }
}