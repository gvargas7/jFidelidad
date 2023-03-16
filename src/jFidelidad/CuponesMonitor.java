/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;
import java.util.Calendar;
/**
 *
 * @author rhirsch
 */
public class CuponesMonitor extends Thread {
    
	public CuponesMonitor(ConnectionPool elPool,Tareas lasTareas,Log elLog) {
		pool=elPool;
		tareas=lasTareas;
		log=elLog;
		yatermino=false;
                inventarios=new Vector(100,50);
                cuponesxalta=new Vector(500,250);
                cupwebservice=new Vector(500,250);
	};
	
	public void run() 
	{
		tareas.sumar();
		terminarMgr=false;
                if (log!=null) log.mensaje("CuponesMonitor Start");
                long tant=System.currentTimeMillis()/1000;
                long seg=0;

		while (terminarMgr==false) {
		
		
                    try {
                        sleep(60000);
                        procesarCuponesXAlta();
                        procesarCuponesWebService();
                        procesarInventario();
                        seg=System.currentTimeMillis()/1000-tant;
                        if (seg>=86400) {
                            resetearEstadisticaMax();
                            tant=System.currentTimeMillis()/1000;
                        }
                    } catch (Exception e) {};

                }
		
		if (log!=null) log.mensaje("CuponesMonitor End");
		yatermino=true;
		
		tareas.restar();
	}
        
        void resetearEstadisticaMax() {
            maxcuponesxalta=cuponesxalta.size();
            maxinventario=inventarios.size();
            maxwebservice=cupwebservice.size();
            pool.maxEnUso=pool.cuantasEnUso;
            tareas.maxTareas=tareas.cantthreads;
        }
        
        public synchronized void enconlarInventario(String empresa,String tienda,String terminal,String version,String modelocaja,String rvc,String eninit) {
           String []undato=new String[7];
           undato[0]=empresa;
           undato[1]=tienda;
           undato[2]=terminal;
           undato[3]=version;
           undato[4]=modelocaja;
           undato[5]=rvc;
           undato[6]=eninit;
           
           logear("Encolar: "+empresa+" "+tienda+" "+terminal);
           if (inventarios!=null) inventarios.addElement(undato);
        }
        
         public synchronized void enconlarCuponxAlta(String campana,String cupon,String tienda, String terminal,String offline,String codmicros,String codmicrosnivel,String diaNegocio,int esapp) {
           String []undato=new String[9];
           undato[0]=campana;
           undato[1]=cupon;
           undato[2]=tienda;
           undato[3]=terminal;
           undato[4]=offline;
           undato[5]=codmicros;
           undato[6]=codmicrosnivel;
           undato[7]=diaNegocio;
           undato[8]=esapp+"";
           
           if (cuponesxalta!=null) cuponesxalta.addElement(undato);
        }
         
        public synchronized void enconlarCuponWebService(String cupon,String tienda) {
           String []undato=new String[3];
           undato[0]=cupon;
           undato[1]=tienda;
           undato[2]=darAhora();
           
           if (cupwebservice!=null) cupwebservice.addElement(undato);
        }
        
        String darAhora() {
            Calendar ahora = Calendar.getInstance();
            //ahora.add(Calendar.DATE, -1);
            int mes=ahora.get(Calendar.MONTH)+1;
            int dia=ahora.get(Calendar.DATE);
            int year=ahora.get(Calendar.YEAR);
            String hora=ahora.get(Calendar.HOUR_OF_DAY)+"";
            String mess=mes+"";
            if (mes<10) mess="0"+mess;
            String dias=dia+"";
            if (dia<10) dias="0"+dias;
            if (hora.length()==1) hora="0"+hora;
            String minutos=ahora.get(Calendar.MINUTE)+"";
            if (minutos.length()==1) minutos="0"+minutos;
            String seg=ahora.get(Calendar.SECOND)+"";
            if (seg.length()==1) seg="0"+seg;
            String fecha=year+"-"+mess+"-"+dias+" "+hora+":"+minutos+":"+seg;
        
        return fecha;
    }
        
        synchronized void procesarCuponesXAlta() {
            String tienda="";
            String terminal="";
            String campana="";
            String cupon="";
            String offline="";
            String codmicros="";
            String codmicrosnivel="";
            String dianegocio="";
            String esapp="";
            int con=0;
            
            if (cuponesxalta!=null) {
                if ((con=pool.pedirConexion())>=0) {
                    
                    JdbcMgr jdbc=pool.darJdbc(con);
                    int cuantos=cuponesxalta.size();
                    logear("Procesando Cupones X Alta..... ("+cuantos+")");
                    if (cuantos>maxcuponesxalta) maxcuponesxalta=cuantos;
                    for (int i=cuantos-1;i>=0;i--) {
                        String []undato=(String [])cuponesxalta.elementAt(i);
                        cuponesxalta.removeElementAt(i);
                        campana=undato[0];
                        cupon=undato[1];
                        tienda=undato[2];
                        terminal=undato[3];
                        offline=undato[4];
                        codmicros=undato[5];
                        codmicrosnivel=undato[6];
                        dianegocio=undato[7];
                        esapp=undato[8];
                        if (dianegocio.equals(""))
                            dianegocio="getDate()";
                        else dianegocio="'"+dianegocio+"'";
                        if (codmicros.equals("")) codmicros="0";
                        String query="INSERT INTO [Cupones] ([cupon],[campana],[redimido],[tienda],[caja],[fecha],[stock],[offline],[codmicros],[cod_micros_tamano],[dianegocio],[app])";
                        query=query+" VALUES ('"+cupon+"','"+campana+"','S',"+tienda+",'"+terminal+"',getdate(),0,"+offline+","+codmicros+","+codmicrosnivel+","+dianegocio+","+esapp+")";
                        try {
                            if (jdbc.ejecutarUpdate(query))
                               logear("Cupon: "+cupon+" por Alta. Tienda: "+tienda);
                            else logear("Cupon: "+cupon+" error en altaCupon.");
                           
                         } catch (Exception e ){
                            logear("CuponesMonitor: procesarCuponesXAlta. Exception:"+e.toString());
                            e.printStackTrace();
                         }  
                    }
                    logear("Fin Procesando Cupones X Alta ..... ("+cuantos+")");
                    pool.cerrarConexion(con);
                }
                else {
                    logear("CuponesMonitor.error en procesarCuponesXAlta 2. No hay conexion");
                }
            }
        };
         
        
        synchronized void procesarCuponesWebService() {
            String tienda="";
            String cupon="";
            String fecha="";
            String json="";
            String token="7a7db4726ec27397cbbd002d9819a07656cce115b481522a6caa6e24d4725695";
            Vector aux=new Vector(100,50);
            if (cupwebservice!=null && cupwebservice.size()>0) {
                
                    int cuantos=cupwebservice.size();
                    logear("Procesando Cupones WebService..... ("+cuantos+")");
                    if (cuantos>maxwebservice) maxwebservice=cuantos;
                    json="{\"validations\":[";
                   // System.out.println(json);
                    int aprocesar=60;
                    if (aprocesar>cuantos) aprocesar=cuantos;
                    for (int i=0;i<aprocesar;i++) {
                        String []undato=(String [])cupwebservice.elementAt(0);
                        cupwebservice.removeElementAt(0);
                        cupon=undato[0];
                        tienda=undato[1];
                        fecha=undato[2]; 
                        aux.addElement(undato);
                        if (i>0) json=json+",";
                        json=json+"{\"validated_at\":\""+fecha+"\",\"code\": \""+cupon+"\",\"branch\": \""+tienda+"\"}";
                       // System.out.println("{\"validated_at\":\""+fecha+"\",\"code\": \""+cupon+"\",\"branch\": \""+tienda+"\"}");
                    }
                    json=json+"]}";
                    //System.out.println("]}");
                    
                    
                    logear("***** PROCESANDO Web SERVICE ***** \r\n"+json);
                    int code=0;
                    HttpURLConnection connection=null;
                    try {                   
                        URL url = new URL("http://validations.bk.cupona.com/api/v1/validations?access_token="+token);
                        connection = (HttpURLConnection )url.openConnection();
                        connection.setDoOutput(true);
                        connection.setRequestProperty("Content-Type", "application/json");
                        //connection.setRequestProperty("Authorization", token);
                        connection.setConnectTimeout(5000);
                        connection.setReadTimeout(15000);
                        OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                        out.write(json);
                        out.close();
                        code = connection.getResponseCode();
                        if (code>201) {
                            logear("Web SERVICE ERROR: "+code);
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String linea="";
                            while ((linea=in.readLine())!=null)
                                logear(linea);
                            in.close();
                            
                        //RECUPERAR SI HAY ERROR Y NO TRANSMITIO. Los vuelvo a encolar
                            
                            for (int i=0;i<aux.size();i++) {
                                String []undato=(String [])aux.elementAt(i);
                                cupwebservice.addElement(undato);
                            };
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        logear("WebSERVICE: ERROR: "+e.toString());
                        logear("WebSERVICE ERROR: "+code);
                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String linea="";
                            while ((linea=in.readLine())!=null)
                                logear(linea);
                            in.close();
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }

                    //RECUPERAR SI HAY ERROR Y NO TRANSMITIO. Los vuelvo a encolar

                        for (int i=0;i<aux.size();i++) {
                            String []undato=(String [])aux.elementAt(i);
                            cupwebservice.addElement(undato);
                        };
                    }
                    
                    
                    logear("Fin Procesando WebService ..... ("+aprocesar+") quedan ("+cupwebservice.size()+")");
                }
        };
        
        synchronized void procesarInventario() {
            String empresa="";
            String tienda="";
            String terminal="";
            String version="";
            String modelocaja="";
            String rvc="";
            String eninit="";
            int con=0;
            
            if (inventarios!=null) {
                if ((con=pool.pedirConexion())>=0) {
                    
                    JdbcMgr jdbc=pool.darJdbc(con);
                    int cuantos=inventarios.size();
                    logear("Procesando Inventario..... ("+cuantos+")");
                    if (cuantos>maxinventario) maxinventario=cuantos;
                    String rvcsql="";
                    for (int i=cuantos-1;i>=0;i--) {
                        String []undato=(String [])inventarios.elementAt(i);
                        inventarios.removeElementAt(i);
                        empresa=undato[0];
                        tienda=undato[1];
                        terminal=undato[2];
                        version=undato[3];
                        modelocaja=undato[4];
                        rvc=undato[5];
                        eninit=undato[6];
                        
                        if (rvc.equals("")) rvc="0";
                        rvcsql=",rvc="+rvc;
                        if (rvc.equals("-1")) rvcsql="";
                        String querybuscarcaja="SELECT * from Inventario where tienda="+tienda+" and caja='"+terminal+"' and empresa='"+empresa+"'";
                        String queryupdatecaja="UPDATE [Inventario] set versionisl="+version+",modelocaja='"+modelocaja+"',fecha=getDate()"+rvcsql+" where tienda="+tienda+" and caja='"+terminal+"' and empresa='"+empresa+"'";
                        String queryupdatecajainit="UPDATE [Inventario] set fechainit=getDate() where tienda="+tienda+" and caja='"+terminal+"' and empresa='"+empresa+"'";
                        String esinit1="";
                        String esinit2="";
                        if (eninit.equals("1")) {
                            esinit1=",[fechainit]";
                            esinit2=",getdate()";
                        }
                        
                        String queryinsertcaja="INSERT INTO [Inventario] ([tienda],[empresa],[caja],[versionisl],[modelocaja],[fecha],[rvc]"+esinit1+") VALUES ";
                        queryinsertcaja=queryinsertcaja+"("+tienda+",'"+empresa+"','"+terminal+"',"+version+",'"+modelocaja+"',getdate(),"+rvc+esinit2+")";

                        try {
                               jdbc.ejecutarQuery(querybuscarcaja);
                               logear("Inventario: "+empresa+" tienda: "+tienda+" terminal: "+terminal+" version: "+version+" Modelo: "+modelocaja);
                               if (jdbc.leerSiguienteEnQuery()==1) {
                                   jdbc.ejecutarUpdate(queryupdatecaja);
                                   if (eninit.equals("1")) jdbc.ejecutarUpdate(queryupdatecajainit);
                               }
                               else {
                                   jdbc.ejecutarUpdate(queryinsertcaja);
                               }
                           
                         } catch (Exception e ){
                              logear("CuponesMonitor: actualizarInventarioCaja. Exception:"+e.toString());
                               e.printStackTrace();
                         }  
                    }
                    logear("Fin Procesando Inventario..... ("+cuantos+")");
                    pool.cerrarConexion(con);
                }
                else {
                    logear("CuponesMonitor.error en actualizarInventarioCaja. No hay conexion");
                }
            }
        };
        
        public int darCantInventarioEncolado() {
            int res=0;
            if (inventarios!=null) res=inventarios.size();
            return res;
        }
        
         public int darCantCuponesEncolado() {
            int res=0;
            if (cuponesxalta!=null) res=cuponesxalta.size();
            return res;
        }
        
	public synchronized void fin()
	{
		try {
			this.interrupt();
			terminarMgr=true;
		}
		catch (Exception e) {
			log.escribir(e);
		}
	}
	
	public synchronized void nuevoPool(ConnectionPool elPool) {
		pool=elPool;
	}
	
	public synchronized boolean termino()
	{
		return yatermino;
	}
	
        void logear(String mensaje) {
             log.mensaje(mensaje+" Conexiones: "+pool.conexionesEnUso());
       
        }
	boolean terminarMgr;
	boolean yatermino=false;
	ConnectionPool pool=null;
	Tareas tareas=null;
	Log log=null;
        Vector inventarios=null;
        Vector cuponesxalta=null;
        Vector cupwebservice=null;
        int maxinventario=0;
        int maxcuponesxalta=0;
        int maxwebservice=0;
}
