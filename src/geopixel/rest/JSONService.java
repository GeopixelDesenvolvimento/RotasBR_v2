package geopixel.rest;
 
import geopixel.model.external.ChoroplethMapDescription;
import geopixel.model.external.GeoJsonChoroplethMap;
import geopixel.model.hb.dao.AppRequestDAO;
import geopixel.service.DataBase;
import geopixel.service.DataBaseService;
import geopixel.thematic.Controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/json")
public class JSONService {
	
	
	
	 @GET
     @Path("/")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response thematicEndpoint() {
             String result = "/thematic";
             return Response.status(200).entity(result).build();
     }


     @GET
     @Path("/connect")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response Connect() {
     		DataBase db = new DataBase();
     		Connection conn = null;
     		db=DataBaseService.getPostgresParameters();
     		try {
					conn=DataBaseService.connect(db);
				} catch (IOException | ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}        
     	                	
        return Response.status(200).entity(conn).build();
     }
     
     @GET
     @Path("/checkData")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response checkData(
    		@QueryParam("tablename") String tableName,     		
      		@QueryParam("whereclause") String whereClause){
     	
     	String result = "";
     	
     	try {
				result = Controller.checkData(tableName,whereClause);
			} catch (SQLException  | IOException e) {
				e.printStackTrace();
			} 
     	
        return Response.status(200).entity(result).build();
     }
     
     @GET
     @Path("/getData")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response getData(
     		@QueryParam("tablename") String tableName,
     		@QueryParam("geocolumn") String geometryColumn,
     		@QueryParam("whereclause") String whereClause){
     		
    	if(whereClause == null || whereClause == "")
    		whereClause= " val > '01/01/1900' ";
    		

     	String geoJson = "";
     	
     	try {
				geoJson = Controller.getFeatures(tableName,geometryColumn,whereClause);
			} catch (SQLException  | IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
     	
        return Response.status(200).entity(geoJson).build();
     }
     /***
      * Group by service using one table and the column that will be grouped
      * @param tableName table name
      * @param column column that will be grouped
      * @return distinct elements
      */
     @GET
     @Path("/groupBy")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response groupBy(
     		@QueryParam("tablename") String tableName,
     		@QueryParam("column") String column,
     		@QueryParam("whereclause") String whereClause){
    	 
    	 //Editado para Oracle
    	 if(whereClause == null || whereClause == "")
     		whereClause= " val > '01/01/1900' ";
    	 
     	String geoJson = "";
     	
     	try {
				geoJson = Controller.getGroupBy(tableName, whereClause,column);
			} catch (SQLException  | IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
     	
        return Response.status(200).entity(geoJson).build();
     }
     
     
     /***
      * Count how many data has date after val. This service could be used to check if there is necessity to upgrade mobile's database
      * 
      * @param tableName the name of the table that will be checked
      * @param val validation date of the POI
      * @return number of POIs that has validation date after "val"
      * 
      * e.g. http://localhost:8080/RotasBR/rest/json/countData?tablename=%22POI%22&val=%272015-10-30%27
      */
     
     @GET
     @Path("/countData")
     @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response countData(
     		@QueryParam("tablename") String tableName,
     		@QueryParam("val") String val,
     		@QueryParam("whereclause") String whereClause){

    	 if(val == null || val == "")
     		val= " '01/01/1900' ";
    	 
    	 if(whereClause == null || whereClause == "")
     		whereClause= " contexto != '' ";
    	 
    	 
     	String geoJson = "";
     	
     	try {
				geoJson = AppRequestDAO.countData(tableName,val, whereClause);
			} catch (SQLException  | IOException | ClassNotFoundException e) {
				e.printStackTrace();
			} 
     	
        return Response.status(200).entity(geoJson).build();
     }
     
     
     /***
      * Insert data into some table
      * 
      * @param tableName the name of the table that will be inserted data
      * @param columns columns sequence that will be used
      * @param values values for those columns
      * 
      * e.g. http://localhost:8080/RotasBR/rest/json/putData?tablename=usuarios&columns=(nome,senha)&values=(%27teste%27,%27teste%27)
      */
     
     int count =0;
     boolean condition = false;
     @GET
     @Path("/putData")
     @Produces( MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response putData(
    		 @QueryParam("tablename") String tableName,
    		 @QueryParam("columns") String columns,
    		 @QueryParam("values") String values  ){
    	 do {
	     	try {
	     		
	     			AppRequestDAO.putData(tableName, columns, values);
	     			System.out.println("PutData Inserido: "+values);
	     			condition = false;
	     			count=20;
	     	
			} catch (SQLException | IOException | ClassNotFoundException e) {
				condition = true;
				count++;
				if (count>=19){
					condition=false;
					System.out.println("Erro PutData************");
					e.printStackTrace();
					return Response.status(404).entity("INSERT STATUS: FAILURE").build();
				}
				System.out.println("Erro PutData************ "+count);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					
				}
			}
    	 } while (condition | count<20);
	     	
	    return Response.status(200).entity("INSERT STATUS: SUCCESS").build() ;
     }
     
     /***
      * Update data of the database
      * 
      * @param tableName the name of the table that will be updated
      * @param set column and new values 
      * @param whereclause where clause to update data
      * 
      * e.g. http://localhost:8080/RotasBR/rest/json/updateData?tablename=usuarios&set=nome=%27teste-update%27&whereclause=nome=%27teste%27
      */
     
     int countUpdateData=0;
     boolean conditionUpdateData = false;
     @GET
     @Path("/updateData")
     @Produces( MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response updateData(
    		 @QueryParam("tablename") String tableName,
    		 @QueryParam("set") String set,
    		 @QueryParam("whereclause") String whereClause  ){
    	 do {
	     	try {

	     			AppRequestDAO.updateData(tableName, set, whereClause);
	     			System.out.println("UpdateData Atualizado: "+whereClause);
	     			conditionUpdateData = false;
	     			countUpdateData=20;

			} catch (SQLException | IOException | ClassNotFoundException e) {
				
				countUpdateData++;
				conditionUpdateData = true;
				if (countUpdateData>=19){
					conditionUpdateData=false;
					System.out.println("Erro UpdateData************");
					e.printStackTrace();
					
					return Response.status(404).entity("UPDATE STATUS: FAILURE").build();
				}
				System.out.println("Erro UpdateData************ "+countUpdateData);
				try {
					Thread.sleep(5000);
					
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
				
			}
    	 } while (conditionUpdateData | countUpdateData<20);
	     	
	     	
	    return Response.status(200).entity("UPDATE STATUS: SUCCESS").build() ;
     }
     
     /***
      * Update data of the database
      * 
      * @param tableName the name of the table that will be updated
      * @param whereclause where clause to delete some data
      * 
      * e.g. http://localhost:8080/RotasBR/rest/json/deleteData?tablename=usuarios&whereclause=nome=%27teste-update%27
      */
     
     @GET
     @Path("/deleteData")
     @Produces( MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response deleteData(
    		 @QueryParam("tablename") String tableName,
    		 @QueryParam("whereclause") String whereClause  ){
	  	
	     	try {
	     		AppRequestDAO.deleteData(tableName, whereClause);
			} catch (SQLException | IOException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
				return Response.status(404).entity("DELETE STATUS: FAILURE").build();
			}
	     	
	     	
	     	return Response.status(200).entity("DELETE STATUS: SUCCESS").build() ;
     }

     
     /***
      * Select data of a database
      * 
      * @param tableName the name of the table that will be updated
      * @param whereclause where clause to delete some data
      * 
      * e.g. http://localhost:8080/RotasBR/rest/json/deleteData?tablename=usuarios&whereclause=nome=%27teste-update%27
      */
     
     boolean conditionSelectData=false;
     int countSelectData=0;
     @GET
     @Path("/selectData")
     @Produces( MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response selectData(
    		 @QueryParam("tablename") String tableName,
    		 @QueryParam("columns") String columns,
    		 @QueryParam("whereclause") String whereClause  ){

    	 String geoJson = "";
    	 do {
	     	try {
	     		
	     		geoJson = AppRequestDAO.selectData(tableName, whereClause, columns);
	     		conditionSelectData = false;
	     		countSelectData=20;
	     		
	     		
			} catch (SQLException | IOException | ClassNotFoundException e) {
				countSelectData++;
				conditionSelectData = true;
				if (countSelectData>=19){
					conditionSelectData=false;
					System.out.println("Erro SelectData ************ ");
					e.printStackTrace();
					return Response.status(404).entity("SELECT STATUS: FAILURE").build();
				}
				System.out.println("Erro SelectData ************ "+countSelectData);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					
				}
				
			}
    	 } while (conditionSelectData | countSelectData<20);	
    	 
	    return Response.status(200).entity(geoJson).build() ;
     }
     
     
     
}