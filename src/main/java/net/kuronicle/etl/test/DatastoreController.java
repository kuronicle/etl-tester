package net.kuronicle.etl.test;


public interface DatastoreController {

    void setupData(String dataFile);

    void assertData(String dataFile);

}
