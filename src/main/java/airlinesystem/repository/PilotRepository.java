package airlinesystem.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import airlinesystem.models.Pilot;
import airlinesystem.models.IdentityDocument;
import airlinesystem.models.Country;
import airlinesystem.models.AirplaneModel;
import airlinesystem.models.DocumentType;
import airlinesystem.services.AuditService;

public class PilotRepository implements GenericRepository<Pilot> {
    private final Connection connection;
    private final AuditService auditService;
    private final CountryRepository countryRepository;
    private final AirplaneModelRepository airplaneModelRepository;

    public PilotRepository(Connection connection, AuditService auditService, CountryRepository countryRepository, AirplaneModelRepository airplaneModelRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.countryRepository = countryRepository;
        this.airplaneModelRepository = airplaneModelRepository;
    }

    @Override
    public void add(Pilot obj) {
        // person -> employee -> pilot: inserting data into multiple tables at once (person also has a set of documents and nationalities..)
        String insertPerson = "INSERT INTO people (id, first_name, last_name, date_of_birth, email, phone_number) VALUES (?, ?, ?, ?, ?, ?)";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";
        String insertEmployee = "INSERT INTO employees (person_id, employee_id, hire_date, salary) VALUES (?, ?, ?, ?)";
        String insertPilot = "INSERT INTO pilots (employee_person_id, license_number) VALUES (?, ?)";
        String insertCertification = "INSERT INTO pilot_certifications (pilot_person_id, airplane_model_id, expiry_date) VALUES (?, ?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement st = connection.prepareStatement(insertPerson)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getFirstName());
                st.setString(3, obj.getLastName());
                st.setDate(4, Date.valueOf(obj.getDateOfBirth()));
                st.setString(5, obj.getEmail());
                st.setString(6, obj.getPhoneNumber());
                st.executeUpdate();
            }

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

            try (PreparedStatement st = connection.prepareStatement(insertNationality)) {
                for (Country country : obj.getNationalities()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, country.getId());
                    st.addBatch();
                }
                st.executeBatch();
            }

            try (PreparedStatement st = connection.prepareStatement(insertEmployee)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getEmployeeId());
                st.setDate(3, Date.valueOf(obj.getHireDate()));
                st.setDouble(4, obj.getSalary());
                st.executeUpdate();
            }

            try (PreparedStatement st = connection.prepareStatement(insertPilot)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getLicenseNumber());
                st.executeUpdate();
            }

            try (PreparedStatement st = connection.prepareStatement(insertCertification)) {
                for (Map.Entry<AirplaneModel, LocalDate> entry : obj.getCertifications().entrySet()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, entry.getKey().getId());
                    st.setDate(3, Date.valueOf(entry.getValue()));
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logAdd("Pilot", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("PilotRepoRollback", ex.getMessage());
            }
            auditService.logError("PilotRepository", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("PilotRepository", e.getMessage());
            }
        }
    }

    @Override
    public Pilot get(String id) {
        int personId = Integer.parseInt(id);

        // must join three tables; executing a single query to perform this is much more efficient (we used to apply the n+1 selects method, which is quite slow)
        String selectPilot = "SELECT p.*, e.employee_id, e.hire_date, e.salary, pi.license_number " +
                "FROM people p " +
                "JOIN employees e ON p.id = e.person_id " +
                "JOIN pilots pi ON e.person_id = pi.employee_person_id " +
                "WHERE p.id = ?";

        String selectDocs = "SELECT * FROM identity_documents WHERE person_id = ?";
        String selectNationalities = "SELECT country_iso_code FROM person_nationalities WHERE person_id = ?";
        String selectCertifications = "SELECT airplane_model_id, expiry_date FROM pilot_certifications WHERE pilot_person_id = ?";

        try (PreparedStatement stPilot = connection.prepareStatement(selectPilot)) {
            stPilot.setInt(1, personId);

            try (ResultSet rsPilot = stPilot.executeQuery()) {
                if (rsPilot.next()) {
                    // person info
                    String firstName = rsPilot.getString("first_name");
                    String lastName = rsPilot.getString("last_name");
                    LocalDate dateOfBirth = rsPilot.getDate("date_of_birth").toLocalDate();
                    String email = rsPilot.getString("email");
                    String phoneNumber = rsPilot.getString("phone_number");

                    // employee info
                    String employeeId = rsPilot.getString("employee_id");
                    LocalDate hireDate = rsPilot.getDate("hire_date").toLocalDate();
                    double salary = rsPilot.getDouble("salary");
                    String licenseNumber = rsPilot.getString("license_number");

                    // identity docs (related to person)
                    Set<IdentityDocument> documents = new HashSet<>();
                    try (PreparedStatement stDocs = connection.prepareStatement(selectDocs)) {
                        stDocs.setInt(1, personId);

                        try (ResultSet rsDocs = stDocs.executeQuery()) {
                            while (rsDocs.next()) {
                                String docNr = rsDocs.getString("document_number");
                                DocumentType docType = DocumentType.valueOf(rsDocs.getString("document_type"));
                                LocalDate expiry = rsDocs.getDate("expiry_date").toLocalDate();
                                String countryIso = rsDocs.getString("issuing_country_iso_code");

                                Country issuingCountry = null;
                                if (countryIso != null) {
                                    issuingCountry = countryRepository.get(countryIso);
                                }
                                documents.add(new IdentityDocument(docNr, docType, expiry, issuingCountry));
                            }
                        }
                    }

                    // nationality info (related to person)
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

                    // certification info (related to pilot)
                    Map<AirplaneModel, LocalDate> certifications = new HashMap<>();
                    try (PreparedStatement stCert = connection.prepareStatement(selectCertifications)) {
                        stCert.setInt(1, personId);

                        try (ResultSet rsCert = stCert.executeQuery()) {
                            while (rsCert.next()) {
                                String modelId = rsCert.getString("airplane_model_id");
                                LocalDate expiryDate = rsCert.getDate("expiry_date").toLocalDate();

                                AirplaneModel model = airplaneModelRepository.get(modelId);
                                if (model != null) {
                                    certifications.put(model, expiryDate);
                                }
                            }
                        }
                    }

                    auditService.logGet("Pilot", id);
                    return new Pilot(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents,
                            employeeId, hireDate, salary, licenseNumber, certifications);
                }
            }
        } catch (SQLException e) {
            auditService.logError("PilotRepositoryGet", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Pilot> getAll() {
        List<Pilot> pilots = new ArrayList<>();
        String query = "SELECT employee_person_id FROM pilots";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // back to life!!!!! n+1 selects because we love antipatterns (this repository made me rethink my decisions regarding the models)
            while (resultSet.next()) {
                Pilot pilot = get(String.valueOf(resultSet.getInt("employee_person_id")));
                if (pilot != null) {
                    pilots.add(pilot);
                }
            }
            auditService.logGet("All pilots", "");
        } catch (SQLException e) {
            auditService.logError("PilotRepository", e.getMessage());
        }
        return pilots;
    }

    @Override
    public void update(Pilot obj) {
        String updatePerson = "UPDATE people SET first_name = ?, last_name = ?, date_of_birth = ?, email = ?, phone_number = ? WHERE id = ?";
        String updateEmployee = "UPDATE employees SET employee_id = ?, hire_date = ?, salary = ? WHERE person_id = ?";
        String updatePilot = "UPDATE pilots SET license_number = ? WHERE employee_person_id = ?";

        String deleteDocs = "DELETE FROM identity_documents WHERE person_id = ?";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";

        String deleteNationalities = "DELETE FROM person_nationalities WHERE person_id = ?";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";

        String deleteCerts = "DELETE FROM pilot_certifications WHERE pilot_person_id = ?";
        String insertCertification = "INSERT INTO pilot_certifications (pilot_person_id, airplane_model_id, expiry_date) VALUES (?, ?, ?)";

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

            try (PreparedStatement st = connection.prepareStatement(updatePilot)) {
                st.setString(1, obj.getLicenseNumber());
                st.setInt(2, obj.getPersonId());
                st.executeUpdate();
            }

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
                    st.setString(5, doc.getIssuingCountry() != null ? doc.getIssuingCountry().getId() : null);
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

            try (PreparedStatement st = connection.prepareStatement(deleteCerts)) {
                st.setInt(1, obj.getPersonId());
                st.executeUpdate();
            }
            try (PreparedStatement st = connection.prepareStatement(insertCertification)) {
                for (Map.Entry<AirplaneModel, LocalDate> entry : obj.getCertifications().entrySet()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, entry.getKey().getId());
                    st.setDate(3, Date.valueOf(entry.getValue()));
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logUpdate("Pilot", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("PilotRepoRollback", ex.getMessage());
            }
            auditService.logError("PilotRepositoryUpdate", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("PilotRepository", e.getMessage());
            }
        }
    }

    @Override
    public void delete(String id) {
        int personId = Integer.parseInt(id);
        String query = "DELETE FROM people WHERE id = ?";               // on delete cascade

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setInt(1, personId);
            st.executeUpdate();

            auditService.logDelete("Pilot", id);
        } catch (SQLException e) {
            auditService.logError("PilotRepositoryDelete", e.getMessage());
        }
    }

    public void updateFirstName(String pilotId, String firstName) {
        int personId = Integer.parseInt(pilotId);
        String query = "UPDATE people SET first_name = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, firstName);
            st.setInt(2, personId);
            st.executeUpdate();

            auditService.logUpdate("Pilot FirstName", pilotId);
        } catch (SQLException e) {
            auditService.logError("PilotRepositoryUpdateFirstName", e.getMessage());
        }
    }

    public void updateLastName(String pilotId, String lastName) {
        int personId = Integer.parseInt(pilotId);
        String query = "UPDATE people SET last_name = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, lastName);
            st.setInt(2, personId);
            st.executeUpdate();

            auditService.logUpdate("Pilot LastName", pilotId);
        } catch (SQLException e) {
            auditService.logError("PilotRepositoryUpdateLastName", e.getMessage());
        }
    }

    public void updateEmail(String pilotId, String email) {
        int personId = Integer.parseInt(pilotId);
        String query = "UPDATE people SET email = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, email);
            st.setInt(2, personId);
            st.executeUpdate();

            auditService.logUpdate("Pilot Email", pilotId);
        } catch (SQLException e) {
            auditService.logError("PilotRepositoryUpdateEmail", e.getMessage());
        }
    }

    public void updateIdentityDocuments(String pilotId, Set<IdentityDocument> documents) {
        int personId = Integer.parseInt(pilotId);
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
            auditService.logUpdate("Pilot IdentityDocuments", pilotId);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("PilotRepoDocsRollback", ex.getMessage());
            }
            auditService.logError("PilotRepositoryUpdateIdDocs", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("PilotRepository", e.getMessage());
            }
        }
    }
}