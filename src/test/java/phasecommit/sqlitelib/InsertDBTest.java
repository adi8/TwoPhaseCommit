package phasecommit.sqlitelib;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class InsertDBTest {
    @Test
    public void testInsert() {
        System.out.println("------ insert test ------");
        SqliteDB db = new SqliteDB("test.db");

        int err = db.put(1, "file1");
        assertEquals("failure - returned " + err + " should return 1", 1, err);

        err = db.put(1, "file3");
        assertEquals("failure - returned " + err + " should return -1", -1, err);

        db.deleteDB();
    }
}
