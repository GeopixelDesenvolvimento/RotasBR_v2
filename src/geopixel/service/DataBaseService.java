package geopixel.service; 

import geopixel.enumeration.DataBaseTypeEnum;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseService {

	private static String URL;
	private static String driver;
	
	public static DataBase getPostgresParameters(){
		DataBase dataBase = new DataBase();
		
		dataBase.setHost("192.168.0.160");
		dataBase.setPort("1521");
		dataBase.setUser("petrobras");
		dataBase.setPassword("petr0braS");
		dataBase.setDatabase("ORCL");
		dataBase.setDataBaseTypeEnum(DataBaseTypeEnum.ORACLE);
		
		return dataBase;
	}

	public static Connection connect(DataBase dataBase) throws IOException, ClassNotFoundException, SQLException {
		DataBaseTypeEnum dbType = dataBase.getDataBaseTypeEnum();
		Connection conn = null;
		
		switch (dbType) {
		case POSTGRES:
			 URL = "jdbc:postgresql://" + dataBase.getHost() + ":" + dataBase.getPort() + "/" + dataBase.getDatabase();
			 driver = "org.postgresql.Driver";
			break;
		case SQLSERVER:
			break;
			
		case MYSQL:
			
			break;
			
		case ORACLE:
			URL = "jdbc:oracle:thin:@" + dataBase.getHost() + ":" + dataBase.getPort() + ":" + "ORCL";
			driver = "oracle.jdbc.driver.OracleDriver";
			break;
		default:
			break;
		}
		
		String user = dataBase.getUser();
		String password = dataBase.getPassword();
		
		
		Class.forName(driver);
		conn = DriverManager.getConnection(URL,user,password);
		

		return conn;
	}

	public static ResultSet buildSelect(String sql,DataBase dataBase) throws IOException, SQLException, ClassNotFoundException {
		Connection conn = DataBaseService.connect(dataBase);
		ResultSet rs = null;
	
			Statement stm = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			rs = stm.executeQuery(sql);
		
		return rs;
	}
	
	public static int buildInsert(String sql,Connection conn) throws IOException, SQLException {		
		int count=0;		
		try {
			//Somente select
			//Statement stm = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			//Para insert
			Statement stm = conn.createStatement();
			count = stm.executeUpdate(sql);
			
		} catch (SQLException e) {
			//System.out.println("ERRO SQL");
			e.printStackTrace();
		} 
		return count;
	}
	
	

}
