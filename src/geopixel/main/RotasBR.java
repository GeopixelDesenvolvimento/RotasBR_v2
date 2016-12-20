package geopixel.main;

import geopixel.model.hb.dao.AppRequestDAO;
import geopixel.service.DataBase;
import geopixel.service.DataBaseService;
import geopixel.thematic.Controller;
import geopixel.thematic.Dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class RotasBR { 
        
        public static void main(String[] args) throws IOException, SQLException{
        String geojson = "";
        //try {
        	 //AppRequestDAO.putData("POI","(gid,uf,classe,contexto,nome,lat,lon,val,geom)","(11572,'SP','Equipamento - DOG','Fazenda Vasos','GAS 5',-14.28559209,-35.43160792,TO_DATE('2008/05/03 21:02:44', 'yyyy/mm/dd hh24:mi:ss'),sdo_geometry(2003, 8307, null, mdsys.sdo_elem_info_array(1,1003,3), mdsys.sdo_ordinate_array(-160.0,-70.0, 160.0,70.0)))");
   			 //geojson = Controller.getFeatures("POI","GEOM","GID = 11572");
   			 
         String protocolo = "smtp";
       	 String servidor = "smtp.geopx.com.br";  // do painel de controle do SMTP
       	 String username = "teste@geopx.com.br"; // do painel de controle do SMTP
       	 String senha = "geopixel1234"; // do painel de controle do SMTP
       	 String porta = "587";   // do painel de controle do SMTP
       	 
       	 //SendMail mail = new SendMail();
       	 //mail.sendMail(protocolo, servidor, username, senha, porta);
   			 
   			 
   		/*} catch (IOException | SQLException | ClassNotFoundException e) {
   			e.printStackTrace();
   		}*/
   		
   		//System.out.println(geojson);
    	}
        
}
