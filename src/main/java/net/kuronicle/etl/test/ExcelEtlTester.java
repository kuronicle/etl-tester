package net.kuronicle.etl.test;

public class ExcelEtlTester implements EtlTester {

    private DatastoreControllerManager datastoreControllerManager;

    public ExcelEtlTester(String datastoreInfoExcelFileName) {
        datastoreControllerManager = new ExcelDatastoreControllerManager(datastoreInfoExcelFileName);
    }

    public void setupDatastore(String datastoreName, String dataFile) {

        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.setupData(dataFile);
    }

    public void assertDatastore(String datastoreName, String dataFile) {

        DatastoreController datastoreController = datastoreControllerManager
                .getDatastoreController(datastoreName);

        datastoreController.assertData(dataFile);
    }
}
