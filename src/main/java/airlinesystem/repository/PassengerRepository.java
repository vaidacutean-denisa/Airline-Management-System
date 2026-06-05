package airlinesystem.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import airlinesystem.models.Passenger;
import airlinesystem.models.IdentityDocument;
import airlinesystem.models.Country;
import airlinesystem.models.DocumentType;
import airlinesystem.services.AuditService;

public class PassengerRepository implements GenericRepository<Passenger> {
    private final Connection connection;
    private final AuditService auditService;
    private final CountryRepository countryRepository;

    public PassengerRepository(Connection connection, AuditService auditService) {
        this.connection = connection;
        this.auditService = auditService;
        this.countryRepository = new CountryRepository(connection, auditService);
    }

    @Override
    public void add(Passenger obj) {
        // passenger derives from person -> must insert data into people table beforehand; also, the passenger has a set of nationalities and identity documents
        String insertPerson = "INSERT INTO people (id, first_name, last_name, date_of_birth, email, phone_number) VALUES (?, ?, ?, ?, ?, ?)";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";
        String insertPassenger = "INSERT INTO passengers (person_id, needs_assistance) VALUES (?, ?)";

        try {
            // four queries executed as a single transaction
            connection.setAutoCommit(false);

            // store basic info in people table
            try (PreparedStatement st = connection.prepareStatement(insertPerson)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getFirstName());
                st.setString(3, obj.getLastName());
                st.setDate(4, Date.valueOf(obj.getDateOfBirth()));
                st.setString(5, obj.getEmail());
                st.setString(6, obj.getPhoneNumber());
                st.executeUpdate();
            }

            // store the id docs
            try (PreparedStatement st = connection.prepareStatement(insertDoc)) {
                for (IdentityDocument doc : obj.getDocuments()) {
                    st.setString(1, doc.getDocumentNumber());
                    st.setInt(2, obj.getPersonId());
                    st.setString(3, doc.getDocumentType().name());
                    st.setDate(4, Date.valueOf(doc.getExpiryDate()));

                    // the issuing country may be null (for ex. id cards do not have one because it might cause inconsistencies with the nationalities)
                    String issuingCountryId = null;
                    if (doc.getIssuingCountry() != null) {
                        issuingCountryId = doc.getIssuingCountry().getId();
                    }
                    st.setString(5, issuingCountryId);
                    st.addBatch();
                }
                st.executeBatch();
            }

            // store the nationalities
            try (PreparedStatement st = connection.prepareStatement(insertNationality)) {
                for (Country country : obj.getNationalities()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, country.getId());
                    st.addBatch();
                }
                st.executeBatch();
            }

            // store specific info related to the passenger
            try (PreparedStatement st = connection.prepareStatement(insertPassenger)) {
                st.setInt(1, obj.getPersonId());
                st.setBoolean(2, obj.getNeedsAssistance());
                st.executeUpdate();
            }

            connection.commit();
            auditService.logAdd("Passenger", "");

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("PassengerRepoRollback", ex.getMessage());
            }
            auditService.logError("PassengerRepositoryAdd", e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { auditService.logError("PassengerRepository", e.getMessage()); }
        }
    }

    @Override
    public Passenger get(String id) {
        int personId = Integer.parseInt(id);

        String selectPassenger = "SELECT p.*, pas.needs_assistance FROM people p" +
                " JOIN passengers pas ON p.id = pas.person_id WHERE p.id = ?";

        String selectDocs = "SELECT * FROM identity_documents WHERE person_id = ?";
        String selectNationalities = "SELECT country_iso_code FROM person_nationalities WHERE person_id = ?";

        try (PreparedStatement stPassenger = connection.prepareStatement(selectPassenger)) {
            stPassenger.setInt(1, personId);

            try (ResultSet rsPassenger = stPassenger.executeQuery()) {
                if (rsPassenger.next()) {
                    // fetch passenger base data
                    String firstName = rsPassenger.getString("first_name");
                    String lastName = rsPassenger.getString("last_name");
                    LocalDate dateOfBirth = rsPassenger.getDate("date_of_birth").toLocalDate();
                    String email = rsPassenger.getString("email");
                    String phoneNumber = rsPassenger.getString("phone_number");
                    boolean needsAssistance = rsPassenger.getBoolean("needs_assistance");

                    // iterate through the id docs (if any) and store them in a set
                    Set<IdentityDocument> documents = new HashSet<>();
                    try (PreparedStatement stDocs = connection.prepareStatement(selectDocs)) {
                        stDocs.setInt(1, personId);

                        try (ResultSet rsDocs = stDocs.executeQuery()) {
                            while (rsDocs.next()) {
                                String docNr = rsDocs.getString("document_number");
                                DocumentType docType = DocumentType.valueOf(rsDocs.getString("document_type"));
                                LocalDate expiry = rsDocs.getDate("expiry_date").toLocalDate();
                                String countryIso = rsDocs.getString("issuing_country_iso_code");

                                // must check if the issuing country is not null
                                Country issuingCountry = null;
                                if (countryIso != null) {
                                    issuingCountry = countryRepository.get(countryIso);
                                }

                                documents.add(new IdentityDocument(docNr, docType, expiry, issuingCountry));
                            }
                        }
                    }

                    // iterate through the nationalities and store them in a set
                    Set<Country> nationalities = new HashSet<>();
                    try (PreparedStatement stNat = connection.prepareStatement(selectNationalities)) {
                        stNat.setInt(1, personId);

                        try (ResultSet rsNat = stNat.executeQuery()) {
                            while (rsNat.next()) {
                                String iso_code = rsNat.getString("country_iso_code");

                                Country natCountry = countryRepository.get(iso_code);
                                if (natCountry != null) {
                                    nationalities.add(natCountry);
                                }
                            }
                        }
                    }

                    auditService.logGet("Passenger", id);
                    return new Passenger(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents, needsAssistance);
                }
            }
        } catch (SQLException e) {
            auditService.logError("PassengerRepositoryGet", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Passenger> getAll() {
        List<Passenger> passengers = new ArrayList<>();
        String query = "SELECT person_id FROM passengers";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Passenger passenger = get(String.valueOf(resultSet.getInt("person_id")));
                if (passenger != null) {
                    passengers.add(passenger);
                }
            }
            auditService.logGet("All passengers", "");
        } catch (SQLException e) {
            auditService.logError("PassengerRepositoryGetAll", e.getMessage());
        }
        return passengers;
    }

    @Override
    public void update(Passenger obj) {
        String updatePerson = "UPDATE people SET first_name = ?, last_name = ?, date_of_birth = ?, email = ?, phone_number = ? WHERE id = ?";
        String updatePassenger = "UPDATE passengers SET needs_assistance = ? WHERE person_id = ?";

        String deleteDocs = "DELETE FROM identity_documents WHERE person_id = ?";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";

        String deleteNationalities = "DELETE FROM person_nationalities WHERE person_id = ?";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement st = connection.prepareStatement(updatePerson)) {
                st.setString(1, obj.getFirstName());
                st.setString(2, obj.getLastName());
                st.setDate(3, Date.valueOf(obj.getDateOfBirth()));
                st.setString(4, obj.getEmail());
                st.setString(5, obj.getPhoneNumber());
                st.setInt(6, obj.getPersonId());
                st.executeUpdate();
            }

            try (PreparedStatement st = connection.prepareStatement(updatePassenger)) {
                st.setBoolean(1, obj.getNeedsAssistance());
                st.setInt(2, obj.getPersonId());
                st.executeUpdate();
            }

            // we delete old documents and replace them with a new set
            try (PreparedStatement st = connection.prepareStatement(deleteDocs)) {
                st.setInt(1, obj.getPersonId());
                st.executeUpdate();
            }
            try (PreparedStatement st = connection.prepareStatement(insertDoc)) {
                for (IdentityDocument doc : obj.getDocuments()) {
                    st.setString(1, doc.getDocumentNumber());
                    st.setInt(2, obj.getPersonId());
                    st.setString(3, doc.getDocumentType().name());
                    st.setDate(4, Date.valueOf(doc.getExpiryDate()));

                    String issuingCountryId = null;
                    if (doc.getIssuingCountry() != null) {
                        issuingCountryId = doc.getIssuingCountry().getId();
                    }
                    st.setString(5, issuingCountryId);
                    st.addBatch();
                }
                st.executeBatch();
            }

            // we use the same approach for nationalities (as we did with the docs: first delete, then insert new)
            try (PreparedStatement st = connection.prepareStatement(deleteNationalities)) {
                st.setInt(1, obj.getPersonId());
                st.executeUpdate();
            }
            try (PreparedStatement st = connection.prepareStatement(insertNationality)) {
                for (Country country : obj.getNationalities()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, country.getId());
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logUpdate("Passenger", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try { connection.rollback(); } catch (SQLException ex) { auditService.logError("PassengerRepository", ex.getMessage()); }
            auditService.logError("PassengerRepository", e.getMessage());
        } finally {
            try { connection.setAutoCommit(true); } catch (SQLException e) { auditService.logError("PassengerRepository", e.getMessage()); }
        }
    }

    @Override
    public void delete(String id) {
        int personId = Integer.parseInt(id);
        String query = "DELETE FROM people WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, personId);
            st.executeUpdate();

            auditService.logDelete("People", id);
        } catch (SQLException e) {
            auditService.logError("PassengerRepository", e.getMessage());
        }
    }

    // specific information related to the passenger is stored in the people table; the passenger table only contains info about assistance needs
    public void updateFirstName(int personId, String firstName) {
        String query = "UPDATE people SET first_name = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, firstName);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate("PassengerFirstName", String.valueOf(personId));
        } catch (SQLException e) {
            auditService.logError("PassengerRepository", e.getMessage());
        }
    }

    public void updateLastName(int personId, String lastName) {
        String query = "UPDATE people SET last_name = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, lastName);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate("PassengerLastName", String.valueOf(personId));
        } catch (SQLException e) {
            auditService.logError("PassengerRepository", e.getMessage());
        }
    }

    public void updatePhoneNumber(int personId, String phoneNumber) {
        String query = "UPDATE people SET phone_number = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, phoneNumber);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate("PassengerPhoneNumber", String.valueOf(personId));
        } catch (SQLException e) {
            auditService.logError("PassengerRepository", e.getMessage());
        }
    }

    public void updateEmail(int personId, String email) {
        String query = "UPDATE people SET email = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, email);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate("PassengerEmail", String.valueOf(personId));
        } catch (SQLException e) {
            auditService.logError("PassengerRepository", e.getMessage());
        }
    }

    public void updateAssistanceNeeds(int personId, boolean needsAssistance) {
        String query = "UPDATE passengers SET needs_assistance = ? WHERE person_id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setBoolean(1, needsAssistance);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate("PassengerAssistanceNeeds", String.valueOf(personId));
        } catch (SQLException e) {
            auditService.logError("PassengerRepository", e.getMessage());
        }
    }

    public void updateNationalities(int personId, Set<Country> nationalities) {
        String deleteQuery = "DELETE FROM person_nationalities WHERE person_id = ?";
        String insertQuery = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement st = connection.prepareStatement(deleteQuery)) {
                st.setInt(1, personId);
                st.executeUpdate();
            }

            try (PreparedStatement st = connection.prepareStatement(insertQuery)) {
                for (Country country : nationalities) {
                    st.setInt(1, personId);
                    st.setString(2, country.getId());
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logUpdate("PassengerNationalities", String.valueOf(personId));
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("PassengerNationalities", ex.getMessage()); }
            auditService.logError("PassengerRepository", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("PassengerRepository", e.getMessage());
            }
        }
    }

    public void updateIdentityDocuments(int personId, Set<IdentityDocument> documents) {
        String deleteQuery = "DELETE FROM identity_documents WHERE person_id = ?";
        String insertQuery = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement st = connection.prepareStatement(deleteQuery)) {
                st.setInt(1, personId);
                st.executeUpdate();
            }

            try (PreparedStatement st = connection.prepareStatement(insertQuery)) {
                for (IdentityDocument doc : documents) {
                    st.setString(1, doc.getDocumentNumber());
                    st.setInt(2, personId);
                    st.setString(3, doc.getDocumentType().name());
                    st.setDate(4, Date.valueOf(doc.getExpiryDate()));

                    String issuingCountryId = null;
                    if (doc.getIssuingCountry() != null) {
                        issuingCountryId = doc.getIssuingCountry().getId();
                    }
                    st.setString(5, issuingCountryId);
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logUpdate("PassengerDocs", String.valueOf(personId));
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("PassengerDocs", ex.getMessage());
            }
            auditService.logError("PassengerRepository", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("PassengerRepository", e.getMessage());
            }
        }
    }
}