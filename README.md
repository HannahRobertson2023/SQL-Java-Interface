HOW TO RUN ACTIVITY 1 JDBCLAB:
Compile with:
javac -d classes -classpath classes src/ser322/JdbcLab.java                                                              
Run with:
java -cp lib/mysql-connector-java-8.0.28.jar;classes  ser322.JdbcLab "jdbc:mysql://localhost/jdbclab?autoReconnect=true&useSSL=false" root root com.mysql.cj.jdbc.Driver <query> <additional commands>

-----------------------

HOW TO RUN ACTIVITY 2 JDBCLAB2:
Compile with:
javac -d classes -classpath classes src/ser322/JdbcLab2.java                                                              
Run with:
java -cp classes ser322.JdbcLab2 <deptNum>

-----------------------

Be sure that the jdbc:mysql syntax goes:
jdbc:mysql://localhost/-*-INSERT DATABASE HERE-*-?autoReconnect...

I am using a different driver, one that comes with MYSQL, rather than the one provided. The one provided refused to compile properly. 

