package embedded;

import csvdb.Database;

public class Client {
    public static void main(String[] args) {
        Database.InitDatabase();
        try {
            String res = Database.Select("select student where id >= 1 order by id limit 2 join subject on id = id");
            System.out.println(res);
        } catch (Throwable e) {
            System.out.println("Error " + e.getMessage());
            e.printStackTrace();
        }

    }
}

