package geopixel.model.hb.dao;

import geopixel.model.external.JSonUtils;
import geopixel.service.DataBaseService;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Class responsible to make request on database for HERE
 * @author: Lucas
 * */

public class AppRequestDAO {
	
	public static String countData(String tableName, String val, String whereClause)throws SQLException, IOException, ClassNotFoundException{
		// Create a select
				String sql = "SELECT count(*) FROM " + tableName + " where val > " + val + " and " + whereClause  ;					
				
				ResultSet rs = DataBaseService.buildSelect(sql, DataBaseService.getPostgresParameters());
				
				StringBuilder builder = new StringBuilder();
				int columnCount = rs.getMetaData().getColumnCount();
				while (rs.next()) {
				    for (int i = 0; i < columnCount;) {
				        builder.append(rs.getString(i + 1));
				        if (++i < columnCount) builder.append(",");
				    }
				    builder.append("\r\n");
				}
				String resultSetAsString = builder.toString();
				
				return resultSetAsString;		
	}
	
	public static void putData(String tableName, String column, String values)throws SQLException, IOException, ClassNotFoundException{
		String sql = "INSERT INTO " + tableName +  " " + column + " VALUES " + values ;					
		
			DataBaseService.buildInsert(sql, DataBaseService.connect(DataBaseService.getPostgresParameters()));
	 
	}
	
	public static void updateData(String tableName, String set, String whereClause)throws SQLException, IOException, ClassNotFoundException{
		String sql = "UPDATE " +  tableName +  " SET " + set + " WHERE " + whereClause ;
		
		DataBaseService.buildInsert(sql, DataBaseService.connect(DataBaseService.getPostgresParameters())); 
	}
	
	public static void deleteData(String tableName, String whereClause)throws SQLException, IOException, ClassNotFoundException{
		String sql = "DELETE FROM " +  tableName +  " WHERE " + whereClause ;					
		
		DataBaseService.buildInsert(sql, DataBaseService.connect(DataBaseService.getPostgresParameters())); 
	}
	
	public static String selectData(String tableName,String whereClause,String columns) throws SQLException, IOException, ClassNotFoundException {
		String sql = "select " + columns + " from " + tableName + " where " + whereClause;					
		ResultSet rs = DataBaseService.buildSelect(sql, DataBaseService.getPostgresParameters());
		
		return JSonUtils.resultSet2Json(rs);
	}
	

}
