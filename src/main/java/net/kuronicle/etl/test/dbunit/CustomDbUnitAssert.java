package net.kuronicle.etl.test.dbunit;

import org.dbunit.assertion.DbUnitAssert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomDbUnitAssert extends DbUnitAssert {

    private static final String SIGN_IGNORE = "[ignore]";

    protected boolean skipCompare(String columnName, Object expectedValue,
            Object actualValue) {

        if (expectedValue instanceof String) {
            if (SIGN_IGNORE.equals(expectedValue)) {
                log.debug(String.format(
                        "Ignore assertion. columnName=%s, expected=%s, actual=%s",
                        columnName, expectedValue, actualValue));
                return true;
            }
        }

        return false;
    }
}