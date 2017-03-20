package net.kuronicle.etl.test;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@ToString
@Slf4j
public class LocalFileController implements DatastoreController {

    @NonNull
    private final String name;

    @NonNull
    private final String type;

    @NonNull
    private final String dirPath;

    @Override
    public void setupDatastore(String setupDataFile) {
        // TODO 自動生成されたメソッド・スタブ
    }

    @Override
    public void assertDatastore(String expectedDataFile) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void assertAndSaveDatastore(String expectedDataFile,
            String saveDataFile) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void assertDatastore(String expectedDataFile,
            String targetDataName) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public void assertDatastore(String expectedDataFile, String targetDataName,
            String[] sortColumns) {
        // TODO 自動生成されたメソッド・スタブ

    }

}
