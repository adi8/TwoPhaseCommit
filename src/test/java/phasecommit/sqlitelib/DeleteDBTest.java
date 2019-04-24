package phasecommit.sqlitelib;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class DeleteDBTest {
    @Test
    public void testDelete() {
        System.out.println("------ delete test ------");
        SqliteDB db = new SqliteDB("test.db");

        db.put(1, "file1");
        db.put(2, "file2");

        int err = db.del(1);
        assertEquals("failure - returned " + err + " should return 1", 1, err);

        err = db.del(1);
        assertEquals("failure - returned " + err + " should return 0", 0, err);

        err = db.del(3);
        assertEquals("failure - returned " + err + " should return 0", 0, err);

        db.deleteDB();
    }
}
