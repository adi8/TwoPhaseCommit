package phasecommit.sqlitelib;

import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class GetDBTest {
    @Test
    public void testGet() {
        System.out.println("------ get test ------");
        SqliteDB db = new SqliteDB("test.db");

        db.put(1, "file1");
        db.put(2, "file2");
        db.put(3, "file3");
        db.put(4, "file4");
        db.put(5, "file5");
        db.put(6, "file6");

        String val = db.get(1);
        assertEquals("failure - returned null should return \"file1\"", "file1", val);

        val = db.get(4);
        assertEquals("failure - returned null should return \"file4\"", "file4", val);

        val = db.get(7);
        assertEquals("failure - returned \"" + val + "\" should return null", null, val);

        db.deleteDB();
    }
}
