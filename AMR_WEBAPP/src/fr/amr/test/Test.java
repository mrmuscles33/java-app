package fr.amr.test;

import fr.amr.database.DbMgr;
import fr.amr.database.TestMapper;
import fr.amr.utils.DateUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test {

    private Test(){
        super();
    }

    public static void main(String[] args) {
        try {
            // Charge le driver JDBC Oracle
            Class.forName("oracle.jdbc.driver.OracleDriver");

            // Initialise la connexion à la base de données
            String url = "jdbc:oracle:thin:@//localhost:1521/XEPDB1";
            String username = "AMR";
            String password = "AMR_123";
            /*
            url = "jdbc:oracle:thin:@localhost:1521:XE";
            username = "SYSTEM";
            password = "system";
             */
            Connection connection = DriverManager.getConnection(url, username, password);
            DbMgr.setConnection(connection);

            TestMapper test = new TestMapper();
            test.setId(1);
            test.setLabel("Hello");
            test.setDt(DateUtils.toDateTime("31/12/2020 23:59:59", DateUtils.D_M_Y_H_M_S));
            test.insert();
            test.setLabel(null);
            test.load();
            System.out.println(test);
            test.setLabel("Hello 2");
            test.update();
            test.setLabel(null);
            test.load();
            System.out.println(test);
            test.delete();
            test.load();
            System.out.println(test);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
