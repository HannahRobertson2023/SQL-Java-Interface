package ser322;

import java.io.StringWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/*
This sample program connects to the database at the given URL and makes the specified query.
It takes as parameters the username and password.
 */
public class JdbcLab {
    private static ResultSet rs = null;
    
	private static Statement stmt = null;
    private static PreparedStatement pstmt = null;
	private static Connection conn = null;

	public static void main(String[] args) {
        //needs the appropriate amount of args to run
		if (args.length < 4)
		{
			System.out.println("USAGE: java ser322.JdbcLab <url> <user> <passwd> <driver> <queryNum> <additionalParameters>");
			System.exit(0);
		}
		String _url = args[0];
		try {
			Class.forName(args[3]);

			conn = DriverManager.getConnection(_url, args[1], args[2]);
            
            if (args[4].equals("query1")) {
                query1(conn);
            }
            else if (args[4].equals("query2")) {
                try {
                    query2(Integer.parseInt(args[5]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    //Makes sure this has the appropriate amount of additional args
                    System.out.println("USAGE: java ser322.JdbcLab <url> <user> <passwd> <driver> query2 <DeptNo>");
                    System.exit(0);
                }
            }
            else if (args[4].equals("dml1")) {
                try {
                    dml1(Integer.parseInt(args[5]), Integer.parseInt(args[6]), args[7] + " " + args[8], Integer.parseInt(args[9]));
                } catch (ArrayIndexOutOfBoundsException e) {
                    //Makes sure this has the appropriate amount of additional args
                    System.out.println("USAGE: java ser322.JdbcLab <url> <user> <pwd> <driver> dml1 <customer id> <product id> <name> <quantity>");
                    System.exit(0);
                }
            }
            else if (args[4].equals("export")) {
                try {
                    exportXML(conn, args[5]);
                   } catch (ArrayIndexOutOfBoundsException e) {
                    //Makes sure this has the appropriate amount of additional args
                    System.out.println("USAGE: java ser322.JdbcLab <url> <user> <pwd> <driver> export <filename>");
                    System.exit(0);
                }
            }
            //general problem catching
		} catch (SQLException e) {
            System.out.println("There was a problem accessing the database.");
            System.exit(0);
        } catch (Exception exc) {
			System.out.println("There was a problem.");
            System.exit(0);
		} finally { 
			try {
				if (rs != null)
					rs.close();
				if (stmt != null)
					stmt.close();
				if (conn != null)
					conn.close();
			} catch (SQLException e) {
                System.out.println("There was a problem closing the database.");
                System.exit(0);
            } catch (Exception exc) {
                System.out.println("There was a problem.");
                System.exit(0);
            }
		}
	}
    
    /**
    * Query 1- list the employee number, name, and name of department
    */
    private static void query1(Connection conn) throws SQLException {
        stmt = conn.createStatement();
        
		rs = stmt.executeQuery("Select EMPNO,ENAME,DNAME from emp, dept where emp.DEPTNO=dept.DEPTNO");

		while (rs.next()) {
			System.out.print(rs.getInt(1) + "\t");
			System.out.print(rs.getString(2) + "\t ");
			System.out.println(rs.getString(3) + "\t ");
		}
    }
    
    /**
    * Query 2, find department name, name, and price of all objects made by department
    */
    private static void query2(int dno) throws SQLException {
        pstmt = conn.prepareStatement("select DNAME, NAME, PRICE from product, customer, dept where MADE_BY=DEPTNO AND DEPTNO=?");
        pstmt.setInt(1, dno);
		rs = pstmt.executeQuery();

		while (rs.next()) {
			System.out.print(rs.getString(1) + "\t");
			System.out.print(rs.getString(2) + "\t ");
			System.out.println(rs.getDouble(3) + "\t ");
		}
    }
    
    /**
    * Inserts a new customer object into the table
    */
    private static void dml1(int custId, int pid, String name, int quantity) throws SQLException {
        pstmt = conn.prepareStatement("insert into customer (CUSTID, PID, NAME, QUANTITY) VALUES (?,?,?,?)");
        pstmt.setInt(1, custId);
        pstmt.setInt(2, pid);
        pstmt.setString(3, name);
        pstmt.setInt(4, quantity);
		int res = pstmt.executeUpdate();
        if (res == 1) {
            System.out.println("SUCCESS");
        }
    }
    
    /**
    * First of a two part method. 
    * Sanitizes input file, then queries for the basic format of our tables 
    * by querying table names and then querying the columns for each table.
    */
    private static void exportXML(Connection conn, String filename) throws SQLException {
        if (!verifyFile(filename)) {
            System.out.println("Sorry, please make sure that the file you input is a .xml file.");
            return;
        }
        
        stmt = conn.createStatement();
        
		rs = stmt.executeQuery("show tables");

        LinkedList<LinkedList<String>> xmlMatrix = new LinkedList<LinkedList<String>>();
        
		while (rs.next()) {
            LinkedList<String> tempList = new LinkedList<String>();
            
            String temp = rs.getString(1);
            tempList.add(temp);
            
            String sql = "show columns from " + temp + "";
            stmt = conn.createStatement();
            ResultSet res = stmt.executeQuery(sql);

            while (res.next()) {
                tempList.add(res.getString(1));
            }
            
            xmlMatrix.add(tempList);
		}
        
        try {
            printXML(xmlMatrix, filename);
        } catch (IOException e) {
            System.out.println("There was a problem writing to your file.");
        } catch (SQLException e) {
            System.out.println("There was a problem accessing the database.");
        }
        catch (Exception e) {
            System.out.println("There was a problem.");
        }
    }
    
    /**
    * Second of a two part method. 
    * Takes a matrix with the table and column names in it.
    * Iterates thru table and writes info to file.
    */
    private static void printXML(LinkedList<LinkedList<String>> xmlMatrix, String filename) throws SQLException, IOException {
        FileWriter fw = new FileWriter(filename);
            
        fw.write("<?xml version=\"1.0\" ?>\n<JDBCLab>");
        
        for (int i = 0; i < xmlMatrix.size(); i++) { 
            fw.write("\n  <" + xmlMatrix.get(i).get(0) + "s>\n");
        
            String query = "select ";
            for (int k = 1; k < xmlMatrix.get(i).size() - 1; k++) {
                query = query + xmlMatrix.get(i).get(k) + ", ";
            }
            query = query + xmlMatrix.get(i).get(xmlMatrix.get(i).size() - 1) + " from " + xmlMatrix.get(i).get(0);
            
            rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                fw.write("    <" + xmlMatrix.get(i).get(0) + ">\n");
                
                for (int j = 1; j < xmlMatrix.get(i).size(); j++) {
                    fw.write("      <" + xmlMatrix.get(i).get(j) + 
                        ">" + rs.getString(j) + "</" + 
                        xmlMatrix.get(i).get(j) + ">\n");
                }         
                fw.write("    </" + xmlMatrix.get(i).get(0) + ">\n\n");
            }
            fw.write("  </" + xmlMatrix.get(i).get(0) + "s>\n\n");
        }
        fw.write("</JDBCLab>\n");
        fw.close();
    }
    
    /**
    * Verifies that a given filename is, in fact, an .xml file.
    */
    private static boolean verifyFile(String filename) {
        if (filename.length() > 3) {
            return (filename.substring(filename.length() - 4).equals(".xml"));
        } else {
            return false;
        }
    }       
}