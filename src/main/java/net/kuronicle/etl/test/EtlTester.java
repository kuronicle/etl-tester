package net.kuronicle.etl.test;

public interface EtlTester {

    void setupDatastore(String datastoreName, String dataFile);

    void assertDatastore(String datastoreName, String dataFile);

}
