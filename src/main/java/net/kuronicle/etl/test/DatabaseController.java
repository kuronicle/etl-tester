package net.kuronicle.etl.test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;

import lombok.NonNull;
import lombok.ToString;
import net.kuronicle.etl.test.dbunit.Assertion;

@ToString
public class DatabaseController implements DatastoreController {

    @NonNull
    private String datastoreName;

    @NonNull
    private String jdbcDriverClassName;

    @NonNull
    private String jdbcConnectionUrl;

    @NonNull
    private String dbUserName;

    @NonNull
    private String dbPassword;

    private String dbSchema = null;

    private IDatabaseTester databaseTester = null;

    private boolean saveActualDataSet = true;

    public DatabaseController(String datastoreName, String jdbcDriverClassName,
            String jdbcConnectionUrl, String dbUserName, String dbPassword,
            String dbSchema) {
        this.datastoreName = datastoreName;
        this.jdbcDriverClassName = jdbcDriverClassName;
        this.jdbcConnectionUrl = jdbcConnectionUrl;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.dbSchema = dbSchema;
    }

    public DatabaseController(String datastoreName, String jdbcDriverClassName,
            String jdbcConnectionUrl, String dbUserName, String dbPassword) {
        this(datastoreName, jdbcDriverClassName, jdbcConnectionUrl, dbUserName,
                dbPassword, null);
    }

    @Override
    public void setupData(String dataFilePath) {

        IDataSet xlsDataSet = null;
        try {
            xlsDataSet = new XlsDataSet(new File(dataFilePath));
        } catch (DataSetException e) {
            throw new RuntimeException("Failed to read datastore. datastoreName="
                    + datastoreName, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read datastore file."
                    + datastoreName, e);
        }

        setupData(xlsDataSet);
    }

    private void setupData(IDataSet dataSet) {
        if (databaseTester == null) {
            databaseTester = setupDatabaseTester();
        }

        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        try {
            databaseTester.onSetup();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up database. datastoreName="
                    + datastoreName, e);
        }
    }

    private IDatabaseTester setupDatabaseTester() {
        try {
            return new JdbcDatabaseTester(jdbcDriverClassName, jdbcConnectionUrl, dbUserName, dbPassword, dbSchema);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot found JDBC driver. driverClassName="
                    + jdbcDriverClassName, e);
        }
    }

    @Override
    public void assertData(String dataFilePath) {

        IDataSet xlsDataSet = null;
        try {
            xlsDataSet = new XlsDataSet(new File(dataFilePath));
        } catch (DataSetException e) {
            throw new RuntimeException("Failed to read datastore. datastoreName="
                    + datastoreName, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read datastore file."
                    + datastoreName, e);
        }

        assertData(xlsDataSet);
    }

    private void assertData(IDataSet expectedDataSet) {
        if (databaseTester == null) {
            databaseTester = setupDatabaseTester();
        }

        IDatabaseConnection connection = null;
        try {
            connection = databaseTester.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get connection. datastoreName="
                    + datastoreName, e);
        }

        IDataSet actualDataSet = null;
        try {
            actualDataSet = connection.createDataSet(expectedDataSet
                    .getTableNames());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get actual dataset. datastoreName="
                    + datastoreName, e);
        } catch (DataSetException e) {
            throw new RuntimeException("", e);
        }

        if (saveActualDataSet) {
            saveActualDataSet(actualDataSet);
        }

        try {
            Assertion.assertEquals(expectedDataSet, actualDataSet);
        } catch (DatabaseUnitException e) {
            throw new RuntimeException("Failed to assert dataset. datastoreName="
                    + datastoreName, e);
        }
    }

    private void saveActualDataSet(IDataSet actualDataSet) {
        // TODO 自動生成されたメソッド・スタブ

    }

}
