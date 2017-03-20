package net.kuronicle.etl.test;

public class ExcelEtlTester implements EtlTester {

    private DatastoreControllerManager datastoreControllerManager;

    public ExcelEtlTester(String datastoreInfoExcelFileName) {
        datastoreControllerManager = new ExcelDatastoreControllerManager(datastoreInfoExcelFileName);
    }

    @Override
    public void setupDatastore(String datastoreName, String setupDataFile) {

        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.setupDatastore(setupDataFile);
    }

    @Override
    public void assertDatastore(String datastoreName, String expectedDataFile) {

        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.assertDatastore(expectedDataFile);
    }

    @Override
    public void assertAndSaveDatastore(String datastoreName,
            String expectedDataFile, String saveDataFile) {
        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.assertAndSaveDatastore(expectedDataFile,
                saveDataFile);
    }

    @Override
    public void assertDatastore(String datastoreName, String expectedDataFile,
            String targetDataName) {
        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.assertDatastore(expectedDataFile, targetDataName);
    }

    @Override
    public void assertDatastore(String datastoreName, String expectedDataFile,
            String targetDataName, String[] sortColumns) {
        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.assertDatastore(expectedDataFile, targetDataName, sortColumns);
    }
}
