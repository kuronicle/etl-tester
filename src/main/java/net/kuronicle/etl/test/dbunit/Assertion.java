package net.kuronicle.etl.test.dbunit;

import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.assertion.DbUnitAssert;
import org.dbunit.assertion.FailureHandler;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;

public class Assertion {
    /**
     * Object that will effectively do the assertions.
     */
    private static final DbUnitAssert INSTANCE = new CustomDbUnitAssert();

    private Assertion() {
        throw new UnsupportedOperationException("this class has only static methods");
    }

    /**
     * @see DbUnitAssert#assertEqualsIgnoreCols(IDataSet, IDataSet, String, String[])
     */
    public static void assertEqualsIgnoreCols(final IDataSet expectedDataset,
            final IDataSet actualDataset, final String tableName,
            final String[] ignoreCols) throws DatabaseUnitException {
        INSTANCE.assertEqualsIgnoreCols(expectedDataset, actualDataset,
                tableName, ignoreCols);
    }

    /**
     * @see DbUnitAssert#assertEqualsIgnoreCols(ITable, ITable, String[])
     */
    public static void assertEqualsIgnoreCols(final ITable expectedTable,
            final ITable actualTable, final String[] ignoreCols) throws DatabaseUnitException {
        INSTANCE.assertEqualsIgnoreCols(expectedTable, actualTable, ignoreCols);
    }

    /**
     * @see DbUnitAssert#assertEqualsByQuery(IDataSet, IDatabaseConnection, String, String, String[])
     */
    public static void assertEqualsByQuery(final IDataSet expectedDataset,
            final IDatabaseConnection connection, final String sqlQuery,
            final String tableName, final String[] ignoreCols) throws DatabaseUnitException, SQLException {
        INSTANCE.assertEqualsByQuery(expectedDataset, connection, sqlQuery,
                tableName, ignoreCols);
    }

    /**
     * @see DbUnitAssert#assertEqualsByQuery(ITable, IDatabaseConnection, String, String, String[])
     */
    public static void assertEqualsByQuery(final ITable expectedTable,
            final IDatabaseConnection connection, final String tableName,
            final String sqlQuery, final String[] ignoreCols) throws DatabaseUnitException, SQLException {
        INSTANCE.assertEqualsByQuery(expectedTable, connection, tableName,
                sqlQuery, ignoreCols);
    }

    /**
     * @see DbUnitAssert#assertEquals(IDataSet, IDataSet)
     */
    public static void assertEquals(IDataSet expectedDataSet,
            IDataSet actualDataSet) throws DatabaseUnitException {
        INSTANCE.assertEquals(expectedDataSet, actualDataSet);
    }

    /**
     * @see DbUnitAssert#assertEquals(IDataSet, IDataSet, FailureHandler)
     * @since 2.4
     */
    public static void assertEquals(IDataSet expectedDataSet,
            IDataSet actualDataSet, FailureHandler failureHandler) throws DatabaseUnitException {
        INSTANCE.assertEquals(expectedDataSet, actualDataSet, failureHandler);
    }

    /**
     * @see DbUnitAssert#assertEquals(ITable, ITable)
     */
    public static void assertEquals(ITable expectedTable, ITable actualTable) throws DatabaseUnitException {
        INSTANCE.assertEquals(expectedTable, actualTable);
    }

    /**
     * @see DbUnitAssert#assertEquals(ITable, ITable, Column[])
     */
    public static void assertEquals(ITable expectedTable, ITable actualTable,
            Column[] additionalColumnInfo) throws DatabaseUnitException {
        INSTANCE.assertEquals(expectedTable, actualTable, additionalColumnInfo);
    }

    /**
     * @see DbUnitAssert#assertEquals(ITable, ITable, FailureHandler)
     * @since 2.4
     */
    public static void assertEquals(ITable expectedTable, ITable actualTable,
            FailureHandler failureHandler) throws DatabaseUnitException {
        INSTANCE.assertEquals(expectedTable, actualTable, failureHandler);
    }

}
