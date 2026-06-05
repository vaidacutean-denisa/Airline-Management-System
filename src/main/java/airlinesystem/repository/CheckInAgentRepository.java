package airlinesystem.repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import airlinesystem.models.CheckInAgent;
import airlinesystem.models.IdentityDocument;
import airlinesystem.models.Country;
import airlinesystem.models.Airport;
import airlinesystem.models.DocumentType;
import airlinesystem.services.AuditService;

public class CheckInAgentRepository implements GenericRepository<CheckInAgent> {
    private final Connection connection;
    private final AuditService auditService;
    private final CountryRepository countryRepository;
    private final AirportRepository airportRepository;

    public CheckInAgentRepository(Connection connection, AuditService auditService, CountryRepository countryRepository, AirportRepository airportRepository) {
        this.connection = connection;
        this.auditService = auditService;
        this.countryRepository = countryRepository;
        this.airportRepository = airportRepository;
    }

    @Override
    public void add(CheckInAgent obj) {
        String insertPerson = "INSERT INTO people (id, first_name, last_name, date_of_birth, email, phone_number) VALUES (?, ?, ?, ?, ?, ?)";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";
        String insertEmployee = "INSERT INTO employees (person_id, employee_id, hire_date, salary) VALUES (?, ?, ?, ?)";
        String insertAgent = "INSERT INTO check_in_agents (employee_person_id, assigned_airport_id) VALUES (?, ?)";
        String insertLanguage = "INSERT INTO check_in_agent_languages (check_in_agent_person_id, language_name) VALUES (?, ?)";

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

            // nationalities
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

            // specific info related to the role of a check-in agent
            try (PreparedStatement st = connection.prepareStatement(insertAgent)) {
                st.setInt(1, obj.getPersonId());
                st.setString(2, obj.getAssignedAirport() != null ? obj.getAssignedAirport().getId() : null);
                st.executeUpdate();
            }

            // languages
            try (PreparedStatement st = connection.prepareStatement(insertLanguage)) {
                for (String lang : obj.getLanguagesSpoken()) {
                    st.setInt(1, obj.getPersonId());
                    st.setString(2, lang);
                    st.addBatch();
                }
                st.executeBatch();
            }

            connection.commit();
            auditService.logAdd("CheckInAgent", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("CheckInAgentRepository", ex.getMessage());
            }
            auditService.logError("CheckInAgentRepository", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("CheckInAgentRepository", e.getMessage());
            }
        }
    }

    @Override
    public CheckInAgent get(String id) {
        int personId = Integer.parseInt(id);

        String selectAgent = "SELECT p.*, e.employee_id, e.hire_date, e.salary, ca.assigned_airport_id " +
                "FROM people p " +
                "JOIN employees e ON p.id = e.person_id " +
                "JOIN check_in_agents ca ON e.person_id = ca.employee_person_id " +
                "WHERE p.id = ?";

        String selectDocs = "SELECT * FROM identity_documents WHERE person_id = ?";
        String selectNationalities = "SELECT country_iso_code FROM person_nationalities WHERE person_id = ?";
        String selectLanguages = "SELECT language_name FROM check_in_agent_languages WHERE check_in_agent_person_id = ?";

        try (PreparedStatement stAgent = connection.prepareStatement(selectAgent)) {
            stAgent.setInt(1, personId);

            try (ResultSet rsAgent = stAgent.executeQuery()) {
                if (rsAgent.next()) {
                    String firstName = rsAgent.getString("first_name");
                    String lastName = rsAgent.getString("last_name");
                    LocalDate dateOfBirth = rsAgent.getDate("date_of_birth").toLocalDate();
                    String email = rsAgent.getString("email");
                    String phoneNumber = rsAgent.getString("phone_number");

                    String employeeId = rsAgent.getString("employee_id");
                    LocalDate hireDate = rsAgent.getDate("hire_date").toLocalDate();
                    double salary = rsAgent.getDouble("salary");

                    String airportId = rsAgent.getString("assigned_airport_id");
                    Airport assignedAirport = (airportId != null) ? airportRepository.get(airportId) : null;

                    // fetch identity documents
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

                    // fetch languages
                    List<String> languagesSpoken = new ArrayList<>();
                    try (PreparedStatement stLang = connection.prepareStatement(selectLanguages)) {
                        stLang.setInt(1, personId);

                        try (ResultSet rsLang = stLang.executeQuery()) {
                            while (rsLang.next()) {
                                languagesSpoken.add(rsLang.getString("language_name"));
                            }
                        }
                    }

                    auditService.logGet("CheckInAgent", id);
                    return new CheckInAgent(personId, firstName, lastName, dateOfBirth, nationalities, email, phoneNumber, documents,
                            employeeId, hireDate, salary, languagesSpoken, assignedAirport);
                }
            }
        } catch (SQLException e) {
            auditService.logError("CheckInAgentRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<CheckInAgent> getAll() {
        List<CheckInAgent> agents = new ArrayList<>();
        String query = "SELECT employee_person_id FROM check_in_agents";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                CheckInAgent agent = get(String.valueOf(resultSet.getInt("employee_person_id")));
                if (agent != null) {
                    agents.add(agent);
                }
            }
            auditService.logGet("All check-in agents", "");
        } catch (SQLException e) {
            auditService.logError("CheckInAgentRepository", e.getMessage());
        }
        return agents;
    }

    @Override
    public void update(CheckInAgent obj) {
        String updatePerson = "UPDATE people SET first_name = ?, last_name = ?, date_of_birth = ?, email = ?, phone_number = ? WHERE id = ?";
        String updateEmployee = "UPDATE employees SET employee_id = ?, hire_date = ?, salary = ? WHERE person_id = ?";
        String updateAgent = "UPDATE check_in_agents SET assigned_airport_id = ? WHERE employee_person_id = ?";

        String deleteDocs = "DELETE FROM identity_documents WHERE person_id = ?";
        String insertDoc = "INSERT INTO identity_documents (document_number, person_id, document_type, expiry_date, issuing_country_iso_code) VALUES (?, ?, ?, ?, ?)";

        String deleteNationalities = "DELETE FROM person_nationalities WHERE person_id = ?";
        String insertNationality = "INSERT INTO person_nationalities (person_id, country_iso_code) VALUES (?, ?)";

        String deleteLanguages = "DELETE FROM check_in_agent_languages WHERE check_in_agent_person_id = ?";
        String insertLanguage = "INSERT INTO check_in_agent_languages (check_in_agent_person_id, language_name) VALUES (?, ?)";

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

            try (PreparedStatement st = connection.prepareStatement(updateAgent)) {
                st.setString(1, obj.getAssignedAirport() != null ? obj.getAssignedAirport().getId() : null);
                st.setInt(2, obj.getPersonId());
                st.executeUpdate();
            }

            // sync everything we might find in that database
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
            auditService.logUpdate("CheckInAgent", String.valueOf(obj.getPersonId()));

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("CheckInAgentRepo", ex.getMessage());
            }
            auditService.logError("CheckInAgentRepo", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("CheckInAgentRepos", e.getMessage());
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

            auditService.logDelete("CheckInAgent", id);
        } catch (SQLException e) {
            auditService.logError("CheckInAgentRepo", e.getMessage());
        }
    }

    public List<CheckInAgent> getAgentsByAirport(String airportId) {
        String query = "SELECT employee_person_id FROM check_in_agents WHERE assigned_airport_id = ?";
        List<CheckInAgent> agents = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, airportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int personId = resultSet.getInt("employee_person_id");
                    CheckInAgent agent = get(Integer.toString(personId));
                    if (agent != null) {
                        agents.add(agent);
                    }
                }
            }
        } catch (SQLException e) {
            auditService.logError("CheckInAgentRepo", e.getMessage());
        }
        return agents;
    }


    public void updateFirstName(String id, String firstName) {
        int personId = Integer.parseInt(id);
        String query = "UPDATE people SET first_name = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, firstName);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate( "CheckInAgent FirstName", id);

        } catch (SQLException e) {
            auditService.logError( "CheckInAgentUpdateFirstName", e.getMessage());
        }
    }

    public void updateLastName(String id, String lastName) {
        int personId = Integer.parseInt(id);
        String query = "UPDATE people SET last_name = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, lastName);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate( "CheckInAgent LastName", id);

        } catch (SQLException e) {
            auditService.logError( "CheckInAgentUpdateLastName", e.getMessage());
        }
    }

    public void updateEmail(String id, String email) {
        int personId = Integer.parseInt(id);
        String query = "UPDATE people SET email = ? WHERE id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, email);
            st.setInt(2, personId);
            st.executeUpdate();
            auditService.logUpdate( "CheckInAgent Email", id);
        } catch (SQLException e) {
            auditService.logError( "CheckInAgentUpdateEmail", e.getMessage());
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
            auditService.logUpdate( "CheckInAgent IdentityDocuments", id);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                auditService.logError("RepoDocsRollback", ex.getMessage());
            }
            auditService.logError( "CheckInAgentUpdateIdDocs", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("RepoAutoCommit", e.getMessage());
            }
        }
    }

    public void updateAssignedAirport(String agentId, String airportId) {
        int personId = Integer.parseInt(agentId);
        String query = "UPDATE check_in_agents SET assigned_airport_id = ? WHERE employee_person_id = ?";

        try (PreparedStatement st = connection.prepareStatement(query)) {
            st.setString(1, airportId);
            st.setInt(2, personId);
            st.executeUpdate();

            auditService.logUpdate("CheckInAgent AssignedAirport", agentId);
        } catch (SQLException e) {
            auditService.logError("CheckInAgent UpdateAssignedAirport", e.getMessage());
        }
    }
}