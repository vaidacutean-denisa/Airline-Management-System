package airlinesystem.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import airlinesystem.models.FlightAttendant;
import airlinesystem.models.IdentityDocument;
import airlinesystem.models.DocumentType;
import airlinesystem.models.Country;
import airlinesystem.services.AuditService;

public class FlightAttendantRepository implements GenericRepository<FlightAttendant> {
    private final Connection connection;
    private final AuditService auditService;
    private final CountryRepository countryRepository;

    public FlightAttendantRepository(Connection connection, AuditService auditService, CountryRepository countryRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.countryRepository = countryRepository;
    }

    @Override
    public void add(FlightAttendant obj) {
        // as we have already seen in the pilot repository: chain inheritance leads to multiple sql queries (at least in this approach which might not be the best)
        String insertPerson = "INSERT INTO people (id, first_name, last_name, date_of_birth, email, phone_number) VALUES (?, ?, ?, ?, ?, ?)";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";
        String insertEmployee = "INSERT INTO employees (person_id, employee_id, hire_date, salary) VALUES (?, ?, ?, ?)";
        String insertAttendant = "INSERT INTO flight_attendants (employee_person_id) VALUES (?)";
        String insertLanguage = "INSERT INTO flight_attendant_languages (flight_attendant_person_id, language_name) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            // person info
            try (PreparedStatement st = connection.prepareStatement(insertPerson)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getFirstName());
                st.setString(3, obj.getLastName());
                st.setDate(4, Date.valueOf(obj.getDateOfBirth()));
                st.setString(5, obj.getEmail());
                st.setString(6, obj.getPhoneNumber());
                st.executeUpdate();
            }

            // id docs
            try (PreparedStatement st = connection.prepareStatement(insertDoc)) {
                for (IdentityDocument doc : obj.getDocuments()) {
                    st.setString(1, doc.getDocumentNumber());
                    st.setInt(2, obj.getPersonId());
                    st.setString(3, doc.getDocumentType().name());
                    st.setDate(4, Date.valueOf(doc.getExpiryDate()));
                    st.setString(5, doc.getIssuingCountry() != null ? doc.getIssuingCountry().getId() : null);
                    st.addBatch();
                }
                st.executeBatch();
            }

            // nationality
            try (PreparedStatement st = connection.prepareStatement(insertNationality)) {
                for (Country country : obj.getNationalities()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, country.getId());
                    st.addBatch();
                }
                st.executeBatch();
            }

            // employee info
            try (PreparedStatement st = connection.prepareStatement(insertEmployee)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getEmployeeId());
                st.setDate(3, Date.valueOf(obj.getHireDate()));
                st.setDouble(4, obj.getSalary());
                st.executeUpdate();
            }

            // specific info related to the flight attendant role
            try (PreparedStatement st = connection.prepareStatement(insertAttendant)) {
                st.setInt(1, obj.getPersonId());
                st.executeUpdate();
            }

            // language spoken list
            try (PreparedStatement st = connection.prepareStatement(insertLanguage)) {
                for (String lang : obj.getLanguagesSpoken()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, lang);
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logAdd("FlightAttendant", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("FlightAttendantRepo", ex.getMessage());
            }
            auditService.logError("FlightAttendantRepo", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("FlightAttendantRepo", e.getMessage());
            }
        }
    }

    @Override
    public FlightAttendant get(String id) {
        int personId = Integer.parseInt(id);

        // join operations to help with performance
        String selectAttendant = "SELECT p.*, e.employee_id, e.hire_date, e.salary " +
                "FROM people p " +
                "JOIN employees e ON p.id = e.person_id " +
                "JOIN flight_attendants fa ON e.person_id = fa.employee_person_id " +
                "WHERE p.id = ?";

        String selectDocs = "SELECT * FROM identity_documents WHERE person_id = ?";
        String selectNationalities = "SELECT country_iso_code FROM person_nationalities WHERE person_id = ?";
        String selectLanguages = "SELECT language_name FROM flight_attendant_languages WHERE flight_attendant_person_id = ?";

        try (PreparedStatement stAttendant = connection.prepareStatement(selectAttendant)) {
            stAttendant.setInt(1, personId);

            try (ResultSet rsAttendant = stAttendant.executeQuery()) {
                if (rsAttendant.next()) {
                    String firstName = rsAttendant.getString("first_name");
                    String lastName = rsAttendant.getString("last_name");
                    LocalDate dob = rsAttendant.getDate("date_of_birth").toLocalDate();
                    String email = rsAttendant.getString("email");
                    String phoneNumber = rsAttendant.getString("phone_number");

                    String employeeId = rsAttendant.getString("employee_id");
                    LocalDate hireDate = rsAttendant.getDate("hire_date").toLocalDate();
                    double salary = rsAttendant.getDouble("salary");

                    // fetch identity documents
                    Set<IdentityDocument> documents = new HashSet<>();
                    try (PreparedStatement stDocs = connection.prepareStatement(selectDocs)) {
                        stDocs.setInt(1, personId);

                        try (ResultSet rsDocs = stDocs.executeQuery()) {
                            while (rsDocs.next()) {
                                String docNum = rsDocs.getString("document_number");
                                DocumentType docType = DocumentType.valueOf(rsDocs.getString("document_type"));
                                LocalDate expiry = rsDocs.getDate("expiry_date").toLocalDate();
                                String countryIso = rsDocs.getString("issuing_country_iso_code");

                                Country issuingCountry = null;
                                if (countryIso != null) {
                                    issuingCountry = countryRepository.get(countryIso);
                                }
                                documents.add(new IdentityDocument(docNum, docType, expiry, issuingCountry));
                            }
                        }
                    }

                    // fetch nationalities
                    Set<Country> nationalities = new HashSet<>();
                    try (PreparedStatement stNat = connection.prepareStatement(selectNationalities)) {
                        stNat.setInt(1, personId);

                        try (ResultSet rsNat = stNat.executeQuery()) {
                            while (rsNat.next()) {
                                String isoCode = rsNat.getString("country_iso_code");
                                Country natCountry = countryRepository.get(isoCode);
                                if (natCountry != null) {
                                    nationalities.add(natCountry);
                                }
                            }
                        }
                    }

                    // fetch languages list
                    List<String> languagesSpoken = new ArrayList<>();
                    try (PreparedStatement stLang = connection.prepareStatement(selectLanguages)) {
                        stLang.setInt(1, personId);

                        try (ResultSet rsLang = stLang.executeQuery()) {
                            while (rsLang.next()) {
                                languagesSpoken.add(rsLang.getString("language_name"));
                            }
                        }
                    }

                    auditService.logGet("FlightAttendant", id);
                    return new FlightAttendant(personId, firstName, lastName, dob, nationalities, email, phoneNumber, documents,
                            employeeId, hireDate, salary, languagesSpoken);
                }
            }
        } catch (SQLException e) {
            auditService.logError("FlightAttendantRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<FlightAttendant> getAll() {
        List<FlightAttendant> attendants = new ArrayList<>();
        String query = "SELECT employee_person_id FROM flight_attendants";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                FlightAttendant attendant = get(String.valueOf(resultSet.getInt("employee_person_id")));
                if (attendant != null) {
                    attendants.add(attendant);
                }
            }
            auditService.logGet("All flight attendants", "");
        } catch (SQLException e) {
            auditService.logError("FlightAttendantRepository", e.getMessage());
        }
        return attendants;
    }

    @Override
    public void update(FlightAttendant obj) {
        String updatePerson = "UPDATE people SET first_name = ?, last_name = ?, date_of_birth = ?, email = ?, phone_number = ? WHERE id = ?";
        String updateEmployee = "UPDATE employees SET employee_id = ?, hire_date = ?, salary = ? WHERE person_id = ?";

        String deleteDocs = "DELETE FROM identity_documents WHERE person_id = ?";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";

        String deleteNationalities = "DELETE FROM person_nationalities WHERE person_id = ?";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";

        String deleteLanguages = "DELETE FROM flight_attendant_languages WHERE flight_attendant_person_id = ?";
        String insertLanguage = "INSERT INTO flight_attendant_languages (flight_attendant_person_id, language_name) VALUES (?, ?)";

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

            try (PreparedStatement st = connection.prepareStatement(updateEmployee)) {
                st.setString(1, obj.getEmployeeId());
                st.setDate(2, Date.valueOf(obj.getHireDate()));
                st.setDouble(3, obj.getSalary());
                st.setInt(4, obj.getPersonId());
                st.executeUpdate();
            }

            // must sync documents, nationalities and languages; this means deleting old values and re-inserting them with corresponding changes
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

            try (PreparedStatement st = connection.prepareStatement(deleteLanguages)) {
                st.setInt(1, obj.getPersonId());
                st.executeUpdate();
            }
            try (PreparedStatement st = connection.prepareStatement(insertLanguage)) {
                for (String lang : obj.getLanguagesSpoken()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, lang);
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logUpdate("FlightAttendant", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("FlightAttendantRepo", ex.getMessage());
            }
            auditService.logError("FlightAttendantRepo", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("FlightAttendantRepository", e.getMessage());
            }
        }
    }

    @Override
    public void delete(String id) {
        int personId = Integer.parseInt(id);
        String query = "DELETE FROM people WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, personId);
            st.executeUpdate();

            auditService.logDelete("FlightAttendant", id);
        } catch (SQLException e) {
            auditService.logError("FlightAttendantRepository", e.getMessage());
        }
    }

    public List<FlightAttendant> getFlightAttendantsByLanguage(String language) {
        String query = "SELECT flight_attendant_person_id FROM flight_attendant_languages WHERE language_name = ?";
        List<FlightAttendant> flightAttendants = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, language);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int personId = resultSet.getInt("flight_attendant_person_id");
                FlightAttendant flightAttendant = get(Integer.toString(personId));

                flightAttendants.add(flightAttendant);
            }

        } catch (SQLException e) {
            auditService.logError("FlightAttendantRepository", e.getMessage());
        }
        return flightAttendants;
    }

    public void updateFirstName(String id, String firstName) {
        int personId = Integer.parseInt(id);
        String query = "UPDATE people SET first_name = ? WHERE id = ?";
        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, firstName);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate( "FlightAttendant FirstName", id);
        } catch (SQLException e) {
            auditService.logError( "FlightAttendant UpdateFirstName", e.getMessage());
        }
    }

    public void updateLastName(String id, String lastName) {
        int personId = Integer.parseInt(id);
        String query = "UPDATE people SET last_name = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, lastName);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate( "FlightAttendant LastName", id);
        } catch (SQLException e) {
            auditService.logError( "FlightAttendant UpdateLastName", e.getMessage());
        }
    }

    public void updateEmail(String id, String email) {
        int personId = Integer.parseInt(id);
        String query = "UPDATE people SET email = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, email);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate( "FlightAttendant Email", id);
        } catch (SQLException e) {
            auditService.logError( "FlightAttendant UpdateEmail", e.getMessage());
        }
    }

    public void updateIdentityDocuments(String id, Set<IdentityDocument> documents) {
        int personId = Integer.parseInt(id);
        String deleteDocs = "DELETE FROM identity_documents WHERE person_id = ?";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";

        try {
            connection.setAutoCommit(false);
            try (PreparedStatement st = connection.prepareStatement(deleteDocs)) {
                st.setInt(1, personId);
                st.executeUpdate();
            }
            try (PreparedStatement st = connection.prepareStatement(insertDoc)) {
                for (IdentityDocument doc : documents) {
                    st.setString(1, doc.getDocumentNumber());
                    st.setInt(2, personId);
                    st.setString(3, doc.getDocumentType().name());
                    st.setDate(4, Date.valueOf(doc.getExpiryDate()));
                    st.setString(5, doc.getIssuingCountry() != null ? doc.getIssuingCountry().getId() : null);
                    st.addBatch();
                }
                st.executeBatch();
            }
            connection.commit();
            auditService.logUpdate( "FlightAttendant IdentityDocuments", id);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("RepoDocsRollback", ex.getMessage());
            }
            auditService.logError( "FlightAttendant UpdateIdDocs", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("RepoAutoCommit", e.getMessage());
            }
        }
    }
}