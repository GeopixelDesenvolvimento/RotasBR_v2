package geopixel.thematic;



import geopixel.model.external.JSonUtils;
import geopixel.service.DataBaseService;

import java.io.IOException;
import java.sql.SQLException;

public class Controller {

	public static String checkData(String tableName,String whereClause)throws SQLException, IOException {
				    	    	
	   // ResultSet newData = Dao.checkData(validationData);

		//return JSonUtils.resultSet2Json(newData);
		
		return "";
		
	}
		
	/**
	 * Creates a GeoJson from a a OCG feature table
	 * @param tableName table name
	 * @param geometryColumn geometry column name
	 * @param whereClause a valid where clause
	 * @return a GeoJson
	 * @throws SQLException
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 * @remarks GeoJson name equal a table name and SRID equal 4736.
	 *
	 */
	public static String getFeatures(String tableName, String geometryColumn, String whereClause)throws SQLException, IOException, ClassNotFoundException{
		return JSonUtils.featureTable2GeoJson(tableName,geometryColumn,whereClause,tableName,"4736");		
	}
	
	public static String getGroupBy(String tableName, String whereClause,String column)throws SQLException, IOException, ClassNotFoundException{
		return JSonUtils.featureTable2GeoJsonGroupBy(tableName,whereClause,column,"4736");		
	}
	
	
}
