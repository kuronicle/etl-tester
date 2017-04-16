package net.kuronicle.etl.test;

public interface DatastoreController {

    void setupDatastore(String setupDataFile);

    void assertDatastore(String expectedDataFile);

    void assertDatastore(String expectedDataFile, String targetDataName);

    void assertDatastore(String expectedDataFile, String targetDataName,
            String[] sortColumns);

    void assertAndSaveDatastore(String expectedDataFile, String saveDataFile);

    void backupDatastore(String targetDataFile, String backupDataFile);
}
