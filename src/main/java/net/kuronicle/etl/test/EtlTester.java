package net.kuronicle.etl.test;

public interface EtlTester {

    void setupDatastore(String datastoreName, String setupDataFile);

    void assertDatastore(String datastoreName, String expectedDataFile);

    void assertDatastore(String datastoreName, String expectedDataFile,
            String targetDataName);

    void assertDatastore(String datastoreName, String expectedDataFile,
            String targetDataName, String[] sortColumns);

    void assertAndSaveDatastore(String datastoreName, String expectedDataFile, String saveDataFile);
}