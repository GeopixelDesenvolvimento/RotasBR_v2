package geopixel.rest;
 
import geopixel.model.hb.dao.AppRequestDAO;
import geopixel.service.DataBase;
import geopixel.service.DataBaseService;
import geopixel.service.TerracoreService;
import geopixel.thematic.Controller;
import geopixel.utils.Cryptography;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import JavaMail.Email;
import JavaMail.MailServer;


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
     		@QueryParam("whereclause") String whereClause,
     		@QueryParam("key") String key){
     		
    	if(whereClause == null || whereClause == "")
    		whereClause= " val > '01/01/1900' ";
    		
    	String geoJson = "";
    	String hImei = null;
    	String dtx = "";
    	String HH_IMEI = null;

    	//Select to verify if KEY is valid
    	boolean validateKey = false;
    	try {
    		validateKey = !AppRequestDAO.selectData("DISPOSITIVOS", ("HASH_IMEI='"+key+"'"), "HASH_IMEI").equals("");	
    	} catch (SQLException  | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} 
    	
    		//If validKey=true then return geoJson (POI/LINES data)
			if (validateKey){
				
				try {
					
					geoJson = Controller.getFeatures(tableName,geometryColumn,whereClause);
					
					//Select to set hImei with first HASH of IMEI
					hImei = AppRequestDAO.selectData("DISPOSITIVOS", ("HASH_IMEI='"+key+"'"), "IMEI");
					JSONArray ja = new JSONArray(hImei);
					JSONObject jsonHImei = new JSONObject();
					jsonHImei = ja.getJSONObject(0);
					hImei = jsonHImei.getString("IMEI");
					
					//getString to set new Hash(Hash(Imei)+timestamp)
					JSONObject jo = new JSONObject(geoJson);
					dtx = jo.getString("timestamp");
					
					//Setting new Hash(Hash(Imei)+timestamp)--> HH_IMEI
		            HH_IMEI = Cryptography.generateHashMD5(hImei+dtx);
					
				} catch (SQLException  | IOException | ClassNotFoundException | JSONException | NoSuchAlgorithmException e) {
					e.printStackTrace();
					//ERROR, no data is returned
					return Response.status(404).entity("").build();
				} 
				
				//Updating the database with the new KEY --> HH_IMEI
	            /*try {
					AppRequestDAO.updateData("DISPOSITIVOS", ("HASH_IMEI='"+HH_IMEI+"'"), "IMEI='"+hImei+"'");
				} catch (SQLException  | IOException | ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
	            
	            //Status OK, return data
				return Response.status(200).entity(geoJson).build();
				
			}
			//False valid KEY, no data is returned
			return Response.status(404).entity("").build();
       
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
    	 
    	 //IF Condititon - Insert DISPOSITIVOS with hash Cryptography
    	 if (columns.equals("(id, nome_usuario, chave_petrobras, telefone, licenca, imei, hash_imei, status, data_ativacao, data_expiracao)"))
    	 {
    		 String[] strArr = values.split(",");
    		 try {
				
    			String imei = (strArr[5]).split("'")[1];
    			String dt0 = "30/11/2016-11:30:57";
    			String dataHora = Cryptography.dataHora();
    			String hashimei = Cryptography.generateHashMD5(imei);
    			String hashimei_dt0 = Cryptography.generateHashMD5(hashimei+dt0);
    			
				strArr[5] = "'"+hashimei+"'"+", '"+hashimei_dt0+"'";
				String values_teste = Arrays.toString(strArr);
				values = (values_teste).split("]")[0].substring(1);
				
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	 }
    	 
    	 
    	 do {
	     	try {
	     		
	     			AppRequestDAO.putData(tableName, columns, values);
	     			//System.out.println("PutData Inserido: "+values);
	     			condition = false;
	     			count=20;
	     	
			} catch (SQLException | IOException | ClassNotFoundException e) {
				condition = true;
				count++;
				if (count>=19){
					condition=false;
					//System.out.println("Erro PutData************");
					e.printStackTrace();
					return Response.status(404).entity("INSERT STATUS: FAILURE").build();
				}
				//System.out.println("Erro PutData************ "+count);
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
	     			//System.out.println("UpdateData Atualizado: "+whereClause);
	     			conditionUpdateData = false;
	     			countUpdateData=20;

			} catch (SQLException | IOException | ClassNotFoundException e) {
				
				countUpdateData++;
				conditionUpdateData = true;
				if (countUpdateData>=19){
					conditionUpdateData=false;
					//System.out.println("Erro UpdateData************");
					e.printStackTrace();
					
					return Response.status(404).entity("UPDATE STATUS: FAILURE").build();
				}
				//System.out.println("Erro UpdateData************ "+countUpdateData);
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
      * Update the KEY HASH(HASH(IMEI)+DTx(timeStamp))
      * 
      * @param timeStamp
      * @param whereclause where clause to update row with HASH(IMEI)
      * 
      * e.g. http://localhost:8080/RotasBR_v2/rest/json/updateTimestamp?tablename=dispositivos&timeStamp='dd/mm/yyyy-hh:mm:ss'&whereclause=IMEI=%27hash_imei%27
      */
     
     int countUpdateTime=0;
     boolean conditionUpdateTime = false;
     @GET
     @Path("/updateTimestamp")
     @Produces( MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response updateTimestamp(
    		 @QueryParam("tablename") String tableName,
    		 @QueryParam("timeStamp") String timeStamp,
    		 @QueryParam("whereclause") String whereClause  ){
    	 do {
	     	try {

	     		String[] strArr = whereClause.split("=");
	     		String hImei = strArr[1];
	     		String HH_IMEI = Cryptography.generateHashMD5(hImei+timeStamp);
	     		String set = "HASH_IMEI='"+HH_IMEI+"'";
	     		whereClause="IMEI='"+hImei+"'";
	     		
     			AppRequestDAO.updateData(tableName, set, whereClause);
     		
     			conditionUpdateTime = false;
     			countUpdateTime=20;

			} catch (SQLException | IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
				
				countUpdateTime++;
				conditionUpdateTime = true;
				if (countUpdateTime>=19){
					conditionUpdateTime=false;
					//System.out.println("Erro UpdateData************");
					e.printStackTrace();
					
					return Response.status(404).entity("UPDATE STATUS: FAILURE").build();
				}
				//System.out.println("Erro UpdateData************ "+countUpdateData);
				try {
					Thread.sleep(5000);
					
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
				
			}
    	 } while (conditionUpdateTime | countUpdateTime<20);
	     	
	     	
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
					//System.out.println("Erro SelectData ************ ");
					e.printStackTrace();
					return Response.status(404).entity("SELECT STATUS: FAILURE").build();
				}
				//System.out.println("Erro SelectData ************ "+countSelectData);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					
				}
				
			}
    	 } while (conditionSelectData | countSelectData<20);	
    	 
	    return Response.status(200).entity(geoJson).build() ;
     }
     
     
     @GET
     @Path("/sendMail")
     @Produces( MediaType.APPLICATION_JSON + ";charset=utf-8")
     public Response sendMail(
    		 @QueryParam("username") String username,
    		 @QueryParam("password") String password,
    		 @QueryParam("to") String to,
    		 @QueryParam("assunto") String assunto,
    		 @QueryParam("mensagem") String mensagem) throws Exception{
    	 try 
		 {
    		 Map<String, String> map = null;
    		 List<String> mailList = new ArrayList<>();
    		 
    		 //String[] toArray  = new String[TerracoreService.getAllPetroMail().length];
    		 
    		 username = TerracoreService.decodeQueryParamString(username);
    		 password = TerracoreService.decodeQueryParamString(password);
    		 assunto = TerracoreService.decodeQueryParamString(assunto);
    		 mensagem = TerracoreService.decodeQueryParamString(mensagem);
			 
	    	 if (to.equals("getAllPetroMail")){

	    		 mailList = TerracoreService.getAllPetroMail();
					
	    	 }else{
	    		 
	    		 //map = to.split(",");
	    	 }
	    	 
	    	 Email email = new Email();
	    	 for(int i=0; i<mailList.size();i++){
	    		 email.addMailsUsers(mailList.get(i));
	    	 }
    		 
    		 email.setSubjectMail(assunto);
    		 email.setBodyMail(mensagem);
    		 
    		 MailServer ms = new MailServer("Email.xml", username, password);
    			
    			if(ms.SendMail(email)){
    				//System.out.print("Email successfully sent!");
    			}else{
    				//System.out.print(">> Error: Send Message!");
    			}
    		 
	    	 
	    	 //SendMail mail = new SendMail();
	    	 //mail.sendMail(username, password, toArray, assunto, mensagem);
    	 
    	 
		} catch (ClassNotFoundException e) {e.printStackTrace();
		} catch (SQLException e) {e.printStackTrace();
		} catch (IOException e) {e.printStackTrace();}
    	 
    	 return Response.status(200).entity("Funcionou").build() ;
     }
     
     
     
}