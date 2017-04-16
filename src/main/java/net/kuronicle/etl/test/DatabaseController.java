package net.kuronicle.etl.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.dbunit.dataset.SortedTable;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;

import lombok.NonNull;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.kuronicle.etl.test.dbunit.Assertion;

@ToString
@Slf4j
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

    private IDatabaseConnection connection = null;

    private Map<String, IDataSet> expectedDataSetMap = new HashMap<>();

    public DatabaseController(String datastoreName, String jdbcDriverClassName, String jdbcConnectionUrl, String dbUserName, String dbPassword, String dbSchema) {
        this.datastoreName = datastoreName;
        this.jdbcDriverClassName = jdbcDriverClassName;
        this.jdbcConnectionUrl = jdbcConnectionUrl;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.dbSchema = dbSchema;
    }

    public DatabaseController(String datastoreName, String jdbcDriverClassName, String jdbcConnectionUrl, String dbUserName, String dbPassword) {
        this(datastoreName, jdbcDriverClassName, jdbcConnectionUrl, dbUserName, dbPassword, null);
    }

    @Override
    public void setupDatastore(String dataFilePath) {
        log.info("***** Start setup. dataStore={}, inputExcelFile={}", datastoreName, dataFilePath);
        IDataSet xlsDataSet = createXlsDataSetFrom(dataFilePath);
        setupDatastore(xlsDataSet);
    }

    private void setupDatastore(IDataSet dataSet) {
        if (databaseTester == null) {
            databaseTester = setupDatabaseTester();
        }

        databaseTester.setDataSet(dataSet);
        databaseTester.setSetUpOperation(DatabaseOperation.CLEAN_INSERT);
        try {
            databaseTester.onSetup();
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up database. datastoreName=" + datastoreName, e);
        }
    }

    private IDatabaseTester setupDatabaseTester() {
        try {
            return new JdbcDatabaseTester(jdbcDriverClassName, jdbcConnectionUrl, dbUserName, dbPassword, dbSchema);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot found JDBC driver. driverClassName=" + jdbcDriverClassName, e);
        }
    }

    private IDatabaseConnection setupConnection() {
        if (databaseTester == null) {
            databaseTester = setupDatabaseTester();
        }

        try {
            return databaseTester.getConnection();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get connection. datastoreName=" + datastoreName, e);
        }
    }

    @Override
    public void assertDatastore(String dataFilePath) {
        IDataSet xlsDataSet = createXlsDataSetFrom(dataFilePath);
        assertDatastore(xlsDataSet);
    }

    private XlsDataSet createXlsDataSetFrom(String dataFilePath) {
        try {
            return new XlsDataSet(new File(dataFilePath));
        } catch (DataSetException e) {
            throw new RuntimeException("Failed to read datastore. datastoreName=" + datastoreName, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read datastore file." + datastoreName, e);
        }
    }

    private IDataSet assertDatastore(IDataSet expectedDataSet) {
        if (connection == null) {
            connection = setupConnection();
        }

        IDataSet actualDataSet = null;
        try {
            actualDataSet = connection.createDataSet(expectedDataSet.getTableNames());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get actual dataset. datastoreName=" + datastoreName, e);
        } catch (DataSetException e) {
            throw new RuntimeException("", e);
        }

        try {
            Assertion.assertEquals(expectedDataSet, actualDataSet);
        } catch (DatabaseUnitException e) {
            throw new RuntimeException("Failed to assert dataset. datastoreName=" + datastoreName, e);
        }

        return actualDataSet;
    }

    @Override
    public void assertAndSaveDatastore(String expectedDataFile, String saveDataFile) {
        IDataSet xlsDataSet = createXlsDataSetFrom(expectedDataFile);
        IDataSet actualDataset = null;
        try {
            actualDataset = assertDatastore(xlsDataSet);
        } finally {
            if (actualDataset != null) {
                saveDatastore(actualDataset, saveDataFile);
            }
        }
    }

    private void saveDatastore(IDataSet actualDataset, String saveDataFile) {
        // create dir if dir does not exist.
        String saveDir = FilenameUtils.getFullPath(saveDataFile);
        try {
            FileUtils.forceMkdir(new File(saveDir));
            log.info(String.format("Create dir. dir=", saveDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a dir for saving actual dataset. dir=" + saveDir, e);
        }

        try {
            saveDataFile = saveDataFile.replace(".xlsx", ".xls"); // DbUnit
                                                                  // writes
                                                                  // ".xls"
                                                                  // file.
            XlsDataSet.write(actualDataset, new FileOutputStream(new File(saveDataFile)));
            log.info(String.format("Save actual dataset. file=%s", saveDataFile));
        } catch (DataSetException e) {
            throw new RuntimeException("Failed to write actual dataset for evicence. filePath=" + saveDataFile, e);
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
    }

    @Override
    public void assertDatastore(String expectedDataFile, String targetDataName) {
        assertDatastore(expectedDataFile, targetDataName, null);
    }

    @Override
    public void assertDatastore(String expectedDataFile, String targetDataName, String[] sortColumns) {

        log.info("***** Start assertion. dataStore={}, inputExcelFile={}, fileName={}", datastoreName, expectedDataFile, targetDataName);

        IDataSet expectedDataSet = expectedDataSetMap.get(expectedDataFile);
        if (expectedDataSet == null) {
            expectedDataSet = createXlsDataSetFrom(expectedDataFile);
            expectedDataSetMap.put(expectedDataFile, expectedDataSet);
        }

        if (connection == null) {
            connection = setupConnection();
        }

        try {
            ITable expectedTable = expectedDataSet.getTable(targetDataName);
            ITable actualTable = connection.createTable(targetDataName);

            if (sortColumns != null) {
                expectedTable = new SortedTable(expectedTable, sortColumns);
                actualTable = new SortedTable(actualTable, sortColumns);
            }

            Assertion.assertEquals(expectedTable, actualTable);

        } catch (DataSetException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        } catch (DatabaseUnitException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
    }

    @Override
    public void backupDatastore(String targetDataFile, String backupDataFile) {
     // create dir if dir does not exist.
        String backupDir = FilenameUtils.getFullPath(backupDataFile);
        try {
            FileUtils.forceMkdir(new File(backupDir));
            log.info(String.format("Create backup dir. dir=", backupDir));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a dir for saving actual dataset. dir="
                    + backupDir, e);
        }
        
        IDataSet targetDataSet = expectedDataSetMap.get(targetDataFile);
        if (targetDataSet == null) {
            targetDataSet = createXlsDataSetFrom(targetDataFile);
            expectedDataSetMap.put(targetDataFile, targetDataSet);
        }

        if (connection == null) {
            connection = setupConnection();
        }

        IDataSet actualDataSet = null;
        try {
            actualDataSet = connection.createDataSet(targetDataSet.getTableNames());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get actual dataset. datastoreName=" + datastoreName, e);
        } catch (DataSetException e) {
            throw new RuntimeException("", e);
        }
        
        try {
         // DbUnit writes ".xls" file
            backupDataFile = backupDataFile.replace(".xlsx", ".xls");
            XlsDataSet.write(actualDataSet,
                    new FileOutputStream(new File(backupDataFile)));
            log.info(String.format("Save actual dataset. file=%s",
                    backupDataFile));
        } catch (DataSetException e) {
            throw new RuntimeException("Failed to write actual dataset for evicence. filePath="
                    + backupDataFile, e);
        } catch (IOException e) {
            throw new RuntimeException("", e);
        }
        
    }
}
