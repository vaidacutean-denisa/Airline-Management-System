package airlinesystem.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import airlinesystem.models.Country;
import airlinesystem.models.EconomicBlock;
import airlinesystem.services.AuditService;

public class CountryRepository implements GenericRepository<Country> {
    private final Connection connection;
    private final AuditService auditService;

    public CountryRepository(Connection connection, AuditService auditService) {
        this.connection = connection;
        this.auditService = auditService;
    }

    @Override
    public void add(Country obj) {
        String queryCountry = "INSERT INTO countries (iso_code, name, economic_block) VALUES (?, ?, ?)";

        // the country has a list of official languages, which cannot be stored as a single unit in the DB -> auxiliary table that maps the country to the languages
        String queryLanguage = "INSERT INTO country_languages (country_iso_code, language_name) VALUES (?, ?)";

        try {
            // to execute both queries as a single transaction
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(queryCountry)) {
                statement.setString(1, obj.getId());
                statement.setString(2, obj.getName());

                // the eco block might be null
                if (obj.getEconomicBlock() != null) {
                    statement.setString(3, obj.getEconomicBlock().name());
                } else {
                    statement.setNull(3, java.sql.Types.VARCHAR);
                }
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(queryLanguage)) {
                for (String language : obj.getOfficialLanguages()) {
                    statement.setString(1, obj.getId());
                    statement.setString(2, language);
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            connection.commit();
            auditService.logAdd("Country", obj.getId());

        } catch (SQLException e) {
            try {
                connection.rollback();                              // in case of error, rollback the transaction to prevent having inconsistent data
            } catch (SQLException exc) {
                auditService.logError("CountryRepository", exc.getMessage());
            }
            auditService.logError("CountryRepository", e.getMessage());
        } finally {                                                                       // to ensure that the connection is set back to auto-commit
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("CountryRepository", e.getMessage());
            }
        }
    }

    @Override
    public Country get(String id) {
        String queryCountry = "SELECT * FROM countries WHERE iso_code = ?";
        String queryLanguages = "SELECT language_name FROM country_languages WHERE country_iso_code = ?";

        try (PreparedStatement statement = connection.prepareStatement(queryCountry)) {
            statement.setString(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    // get the list of languages
                    List<String> languages = new ArrayList<>();

                    try (PreparedStatement langStatement = connection.prepareStatement(queryLanguages)) {
                        // pass the given country id as a parameter to the query
                        langStatement.setString(1, id);

                        try (ResultSet langResultSet = langStatement.executeQuery()) {
                            while (langResultSet.next()) {
                                languages.add(langResultSet.getString("language_name"));
                            }
                        }
                    }

                    auditService.logGet("Country", id);
                    String economicBlock = resultSet.getString("economic_block");
                    EconomicBlock block = (economicBlock != null) ? EconomicBlock.valueOf(economicBlock) : null;

                    return new Country(
                            resultSet.getString("iso_code"),
                            resultSet.getString("name"),
                            block,
                            languages
                    );
                }
            }
        } catch (SQLException e) {
            auditService.logError("CountryRepository", e.getMessage());
        }
        return null;
    }

    @Override
    public List<Country> getAll() {
        List<Country> countries = new ArrayList<>();
        String query = "SELECT iso_code FROM countries";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            // for the current country, we fetch all languages (n+1 queries for a list, but it should be ok for now)
            while (resultSet.next()) {
                Country country = get(resultSet.getString("iso_code"));
                if (country != null) {
                    countries.add(country);
                }
            }
            auditService.logGet("All countries", "");
        } catch (SQLException e) {
            auditService.logError("CountryRepository", e.getMessage());
        }
        return countries;
    }

    @Override
    public void update(Country obj) {
        String queryCountry = "UPDATE countries SET name = ?, economic_block = ? WHERE iso_code = ?";
        String deleteLanguages = "DELETE FROM country_languages WHERE country_iso_code = ?";
        String insertLanguage = "INSERT INTO country_languages (country_iso_code, language_name) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(queryCountry)) {
                statement.setString(1, obj.getName());

                // again, if the eco block is null, must address a possible null pointer exception
                if (obj.getEconomicBlock() != null) {
                    statement.setString(2, obj.getEconomicBlock().name());
                } else {
                    statement.setNull(2, java.sql.Types.VARCHAR);
                }
                statement.setString(3, obj.getId());
                statement.executeUpdate();
            }

            // if we choose to update a language, we delete all existing languages for the country and re-insert them with the changes
            try (PreparedStatement statement = connection.prepareStatement(deleteLanguages)) {
                statement.setString(1, obj.getId());
                statement.executeUpdate();
            }

            // we insert the new list of languages
            try (PreparedStatement statement = connection.prepareStatement(insertLanguage)) {
                for (String language : obj.getOfficialLanguages()) {
                    statement.setString(1, obj.getId());
                    statement.setString(2, language);

                    // used when we execute multiple sql commands (optimization)
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            connection.commit();
            auditService.logUpdate("Country", obj.getId());
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException exc) {
                auditService.logError("CountryRepository", exc.getMessage());
            }
            auditService.logError("CountryRepository", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("CountryRepository", e.getMessage());
            }
        }
    }

    @Override
    public void delete(String id) {
        // must delete entries from both the country_languages and countries tables
        String deleteLanguages = "DELETE FROM country_languages WHERE country_iso_code = ?";
        String deleteCountry = "DELETE FROM countries WHERE iso_code = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(deleteLanguages)) {
                statement.setString(1, id);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(deleteCountry)) {
                statement.setString(1, id);
                statement.executeUpdate();
            }

            connection.commit();
            auditService.logDelete("Country", id);
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException exc) {
                auditService.logError("CountryRepository", exc.getMessage());
            }
            auditService.logError("CountryRepository", e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                auditService.logError("CountryRepository", e.getMessage());
            }
        }
    }

    public void addOfficialLanguage(String isoCode, String language) {
        String query = "INSERT INTO country_languages (country_iso_code, language_name) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, isoCode);
            statement.setString(2, language);
            statement.executeUpdate();

            auditService.logUpdate("Country Language Added", isoCode + " -> " + language);
        } catch (SQLException e) {
            auditService.logError("CountryRepository", e.getMessage());
        }
    }

    public void removeOfficialLanguage(String isoCode, String language) {
        String query = "DELETE FROM country_languages WHERE country_iso_code = ? AND language_name = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, isoCode);
            statement.setString(2, language);
            statement.executeUpdate();

            auditService.logUpdate("Country Language Removed", isoCode + " -> " + language);
        } catch (SQLException e) {
            auditService.logError("CountryRepository", e.getMessage());
        }
    }
}