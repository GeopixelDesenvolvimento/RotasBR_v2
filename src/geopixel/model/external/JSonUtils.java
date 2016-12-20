package geopixel.model.external;

import geopixel.service.DataBaseService;
import geopixel.utils.Cryptography;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;
/**
 * Class to create Json and GeoJson from different sources : result sets, result set rows, arrays...
 * @author Freitas,U.M.
 *
 */
public class JSonUtils {
	
	private String json = "";
	
	public void setJson(String js) {json = js;}
	
	public String getJson () {return json;}
	
	/**
	 * Creates a Json from  a SQL ResultSET 	
	 * @param rs the result set 
	 * @return a Json with all rows and attributes in result set
	 * @throws SQLException
	 */
	public static String resultSet2Json(ResultSet rs) throws SQLException{
		ResultSetMetaData md;
		md = rs.getMetaData();
		int ncolumns = md.getColumnCount();

		if (rs.next()){
			String json = "[";		
			while (!rs.isAfterLast()){			
				json += "{";			
				for (int i = 1; i <= ncolumns; i++ ) {
					
					ResultSetMetaData rsMeta = rs.getMetaData();
					if (md.getColumnName(i) == "VAL"){
						String dataFormatada = new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate(i));
						json += "\"" + md.getColumnName(i) + "\":\"" + dataFormatada+ "\"";	
					}else{
						if (md.getColumnName(i).equals("GEOM")){
							json += "\"" + md.getColumnName(i) + "\":\"" +""+ "\"";
							//System.out.println(md.getColumnName(i)+" : ");
						}
						else
						{
							json += "\"" + md.getColumnName(i) + "\":\"" + rs.getString(i)+ "\"";
							//System.out.println(md.getColumnName(i)+" : "+rs.getString(i));
							
						}
					}	
					
					
					if (i < ncolumns  ) {
						json += ",";
					} else {
						json += "}";
					}
				}
				if (!rs.isLast()){
					json += ",";
				} else {
					json += "]";
				}
				rs.next();
			}		
			return json;
		} else {
			return "";
		}		
	}
/**
 * Creates a GeoJson form an OGC/SFS feature table
 * @param tableName - name of DB table
 * @param geoColumn - name of geometry column
 * @param whwreClause - a valid where clause
 * @param name - GeoJson name
 * @param crs - SRS projection code
 * @return - a GeoJson string containing all features
 * @throws SQLException
 * @throws ClassNotFoundException 
 * @remarks - the implementation works only in PostgreSQL/PostGIS
 */
	public static String featureTable2GeoJson(String tableName,String geoColumn,String whereClause,String name, String crs) throws SQLException, IOException, ClassNotFoundException {
		String sql = "select * from " + tableName + " where ROWNUM = 1";		
		ResultSet rs = DataBaseService.buildSelect(sql, DataBaseService.getPostgresParameters());
		
		ResultSetMetaData md =rs.getMetaData();
		int ncolumns = md.getColumnCount();
		int geometryColumn = 0;
		String jsonFeatureArray = "";
		// Create a select
		sql = "select ";
		for(int i =  1; i<= (ncolumns); i++){
			if ((md.getColumnName(i)).equals(geoColumn) ){
				sql  += geoColumn;
				geometryColumn = i;
			} else {
				sql += md.getColumnName(i);
			}
			if (i>=(ncolumns)) {
				sql += " from " + tableName;
				if (whereClause.length()>0){
					sql  += " where " + whereClause;					
				}
			} else {
				sql += " , ";						
			}					
		}
		
		rs = DataBaseService.buildSelect(sql, DataBaseService.getPostgresParameters());
		
		
		if (rs.next()){
					
			while (!rs.isAfterLast()){
				
				//Retrieves Geometry array.
				STRUCT st = (oracle.sql.STRUCT) rs.getObject(geoColumn);
				JGeometry j_geom = JGeometry.load(st);
				String geometry="";
				
				//Separate LINE from POINT
				if (tableName.equals("LINHA")){
					
					//Create Geometry
					int lines_length = j_geom.getNumPoints();
					double coordinates[] = j_geom.getOrdinatesArray();
					geometry +="{\"type\":\"LineString\",\"coordinates\":[";
					
					for (int j =0; j<(lines_length*2); j+=2){
						
						geometry += "["+coordinates[j]+","+coordinates[j+1]+"]";
						
						if (!(j==(lines_length*2)-2)){
							geometry+=",";
						}			
					}
					geometry += "]}";
				
				}else{
					
					geometry += "{\"type\":\"Point\",\"coordinates\":[";
					geometry += "["+rs.getString(6)+","+rs.getString(7)+"]";		
					geometry += "]}";
					
				}
				
				//Creates Properties
				String properties = "\"properties\":{";
				boolean empty = true;			
				for (int i = 1; i <= ncolumns; i++ ) {
					if (md.getColumnName(i).equals(geoColumn)){
						//geometryColumn = i;
						
						//Separate LINE from POINT
						if (tableName.equals("LINHA")){
							if (!empty){
								properties += ",";
							}
							
							//Creates BOX
							double MBR[] = j_geom.getMBR();
							properties += "\"box\":["+MBR[0]+","+MBR[1]+","+MBR[2]+","+MBR[3]+"]}";
							empty=false;
						}else
						{
							properties += "}";
						}
						
					} else {
						if (!empty){
							properties += ",";
						}
						ResultSetMetaData rsMeta = rs.getMetaData();
						if (rsMeta.getColumnType(i) == java.sql.Types.TIMESTAMP){
							String dataFormatada = new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate(i));
							properties += "\"" + md.getColumnName(i) + "\":\"" + dataFormatada+ "\"";	
						}else{
							//Separate LINE from POINT
							if ((tableName.equals("LINHA"))&&(md.getColumnName(i).equals("NOME"))){
								if (rs.getObject("NOME")!= null){
									properties += "\"" + md.getColumnName(i) + "\":\"" + rs.getString(i).replaceAll("[\"\n\r]"," ")+ "\"";
								}else{
									properties += "\"NOME\":\"\"";
								}
							}else{
								properties += "\"" + md.getColumnName(i) + "\":\"" + rs.getString(i)+ "\"";
							}
						}
						empty=false;
					}						
				}
				
				
				jsonFeatureArray=addArrayItem(jsonFeatureArray, createJsonFeature(geometry, properties));
				rs.next();
				
			}
			jsonFeatureArray=addArrayItem(jsonFeatureArray,"");
			return createGeoJson(jsonFeatureArray,name,crs);
		} else {
			return "";
		}		
	}
	
	
	public static String featureTable2GeoJsonGroupBy(String tableName,String whereClause,String column, String crs) throws SQLException, IOException, ClassNotFoundException {
		String sql = "select * from "+ tableName + " where ROWNUM <=1";
		ResultSet rs = DataBaseService.buildSelect(sql, DataBaseService.getPostgresParameters());
		if (rs == null){
			return "{\"return\":\""+sql+"\"}";
		}
		ResultSetMetaData md =rs.getMetaData();
		int ncolumns = md.getColumnCount();
		int geometryColumn = 0;
		String jsonFeatureArray = "";
		// Create a select
		sql = "select " + column + " from " + tableName + " where " + whereClause + " group by " + column + " order by " + column;					
	
		rs = DataBaseService.buildSelect(sql, DataBaseService.getPostgresParameters());
		
		return resultSet2Json(rs);
	}

	
	/**
	 * Encapsulates a an array of Json features as a GeoJson	
	 * @param json a Json array of Json features
	 * @param name name Geojson
	 * @param crs EPSG number of  cartography projection
	 * @return a GeoJson string
	 */
	public static String createGeoJson(String json, String name,String crs){
		String geoJson = "{\"name\":\""+ name +"\",\"type\":\"FeatureCollection\"";
		geoJson = geoJson + ",\"crs\":{\"type\":\"name\",\"properties\":{\"name\":\"EPSG:" + crs + "\"}}";
		try {
			geoJson = geoJson + ",\"timestamp\":\""+Cryptography.dataHora()+"\"";
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		geoJson = geoJson + ",\"features\":"+json+"}";
		return geoJson;
	
	}
	
	/**
	 * Creates a Json feature from a Json geometry and Json properties
	 * @param geometry Json geometry
	 * @param properties Json properties
	 * @return Json feature
	 */
	public static String createJsonFeature(String geometry, String properties){
		String feature = "{\"type\":\"Feature\"," + "\"geometry\":" +geometry;
		feature = feature + ","+ properties+"}";
		return feature;
	}
	
	/**
	 * creates a Json properties from a list	
	 * @param pairs a list of pairs (name, property value)
	 * @return a Json properties 
	 */
	public static String createJsonProperties (List <String> pairs){
		String p = "\"properties\":{";
		int i;
		for(i=0; i < pairs.size()-2;i+=2){
			p = p + "\"" + pairs.get(i) + "\":\"" + pairs.get(i+1)+ "\",";
		}
		p = p + "\"" + pairs.get(i) + "\":\"" + pairs.get(i+1)+ "\"}";
		return p;
	}
	
	/**
	 * Creates a Json array	
	 * @param json a list of Json elements
	 * @return the Json array
	 */
	public static String createJsonArray(List <String> json){
		String array = "[";
		int i;
		for (i=0;i<json.size()-1; i++){
			array = array + json.get(i) + ",";			
		}
		array = array +json.get(i)+ "]";
		return array;
	}
/**
 * Adds a Json item as an array item on an existing Json array, not closed, or an empty json string	
 * @param jsonArray the existing Json array or an empty string
 * @param jsonItem the Json array item, if empty the array will be closed inserting a ]
 * @return the updated Json array
 */
	public static String addArrayItem (String jsonArray, String jsonItem){
		if (jsonArray.length()==0) {
			jsonArray = "["+jsonItem;		
		} else {		
			if (jsonItem.length() == 0){
				jsonArray=jsonArray+"]";
			} else {
				jsonArray=jsonArray+","+ jsonItem;
			}
		}
		
		return jsonArray;
	}
	

}
