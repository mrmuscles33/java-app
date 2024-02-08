package fr.amr.test;

import fr.amr.database.DbMgr;
import fr.amr.database.TestMapper;
import fr.amr.filter.SecurityToken;
import fr.amr.utils.DateUtils;
import fr.amr.utils.StringUtils;
import fr.amr.utils.StructureUtils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test {

    private Test() {
        super();
    }

    public static void main(String[] args) {
        SecurityToken token = new SecurityToken(StructureUtils.map("user", "admin", "password", "admin_12345"));
        System.out.println(token.getToken());
        System.out.println(new SecurityToken(token.getParams()).getToken());
    }

    private static void initConnection() {
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
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
}
