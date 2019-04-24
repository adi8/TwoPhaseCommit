package phasecommit.sqlitelib;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class CreateDBTest {
    @Test
    public void testCreateDB() {
        System.out.println("------ new db test ------");
        File file = new File("db/test.db");
        if (file.exists()) {
            file.delete();
        }
        SqliteDB db = new SqliteDB("test.db");
        assertTrue("should be true", file.exists());

        db.deleteDB();
    }

    @Test
    public void testExistsCreateDB() {
        System.out.println("------ db exists test ------");
        File file = new File("db/test.db");
        try {
            file.createNewFile();
        }
        catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
            fail("test.db creation failed");
        }
        SqliteDB db = new SqliteDB("test.db");
        assertTrue("should be true", file.exists());

        db.deleteDB();
    }
}
