package ADC.Utils;

import java.sql.*;

/**
 * Created by Ludaa on 3/11/14.
 */
public class OpenDebugConnection {
    public static void main(String[] args)
    {
        //  throws SQLException

        //initiazlie the connection

        Connection con=null;

        try //try connection to database
        {
            //load driver
            Class.forName("oracle.jdbc.OracleDriver");
            System.out.println("Oracle JDBC driver loaded ok.");
            con=DriverManager.getConnection("jdbc:oracle:thin:system/123321@localhost:1521:orcl");
            System.out.println("Connect with @oracle:1521:orcl");

            //declaring statement
            Statement stmt = con.createStatement();

            String dropProductTable="drop table product cascade constraints";

            //create string
            String createProductTable="CREATE TABLE product(" +
                    "pid number," +
                    "pname CHAR(20)," +
                    "price number," +
                    "PRIMARY KEY (pid)" +
                    ")"; //do not add the semicolon(;) after closing the parenthesis.


            /*drop table */
            stmt.executeUpdate(dropProductTable);


            //execute the create statement
            stmt.executeUpdate(createProductTable);//execure the create statement

            //create string that holds the insert statement
            String insertIntoProduct="INSERT INTO product VALUES (1,'Pepsi',10)";
            String insertIntoProduct1="INSERT INTO product VALUES (2,'Fanta',20)";
            String insertIntoProduct2="INSERT INTO product VALUES (3,'Mirinda',30)";
            String insertIntoProduct3="INSERT INTO product VALUES (4,'Gum',5)";
            String updatePrice="UPDATE product set price=55 where price=20";



            //stmt.executeUpdate(insertIntoProduct);
            stmt.executeUpdate(insertIntoProduct);
            stmt.executeUpdate(insertIntoProduct1);
            stmt.executeUpdate(insertIntoProduct2);
            stmt.executeUpdate(insertIntoProduct3);

            //update statement
            stmt.executeUpdate(updatePrice);



            //save the select statement in a string
            String selectStat="SELECT * FROM product";
            String selectProduct="SELECT pid, pname from product where price>20";
            //stmt.executeUpdate(selectStat);

            //create a result set
            ResultSet rows = stmt.executeQuery(selectStat);
            ResultSet rows1= stmt.executeQuery(selectProduct);

            //stmt.executeQuery(selectStat);


            int count=0;
            while (rows.next()) {
                count+=1;
                String productNumber = rows.getString("pid");
                String productName = rows.getString("pname");
                String productPrice = rows.getString("price");
                System.out.println("Row #:"+count);
                System.out.println("Product#: "+productNumber);
                System.out.println("Product Name: "+productName);
                System.out.println("Price: "+productPrice);

            }

            int count1=0;
            while (rows1.next()) {
                count1+=1;
                String productNumber = rows1.getString("pid");
                String productName = rows1.getString("pname");
                String productPrice = rows1.getString("price");
                System.out.println("Row #:"+count);
                System.out.println("Product#: "+productNumber);
                System.out.println("Product Name: "+productName);
                System.out.println("Price: "+productPrice);

            }

            con.close();

        }
        catch (Exception e)
        {
            System.err.println("Exception:"+e.getMessage());
        }


    }
}





