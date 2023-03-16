/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

import java.sql.*;
import java.text.*;

public class JdbcMgr {

	

	public JdbcMgr(String elServerJDBC,String elUser, String elPass,Log elLog,int elTipoServer) {
		log=elLog;
		tipoServer=elTipoServer;
		//log.mensaje("CREACION JDBCMGR");
		crearJdbc(elServerJDBC,elUser,elPass);
	};
	
	public JdbcMgr(String elServerJDBC,String elUser, String elPass,Log elLog) {
		log=elLog;
		tipoServer=1;
		//log.mensaje("CREACION JDBCMGR");
		crearJdbc(elServerJDBC,elUser,elPass);
	};
	
	void crearJdbc(String elServerJDBC,String elUser, String elPass) {
	
		serverJDBC=elServerJDBC;
		username=elUser;
		password=elPass;
	
		if (!serverJDBC.equals("")) {
			if (conexiones==0)
			{
				cargarDriver();
			
				obtenerConexion();
				conexiones++;
			}
			else 
				try {
					conn=DriverManager.getConnection(serverJDBC,username,password);
					stmt=conn.createStatement();
                                        stmt.setQueryTimeout(15);
                                        
				}
				catch (Exception e)
				{
					error=e.toString();
					log.mensaje("JdbcMgr");
					log.mensaje("server: "+serverJDBC);
					log.mensaje(e.toString());

				}
			}
			else log.mensaje("Server no configurado");
	};
	
        void crearJdbc() {
	
		
		if (!serverJDBC.equals("")) {
			if (conexiones==0)
			{
				cargarDriver();
			
				obtenerConexion();
				conexiones++;
			}
			else 
				try {
					conn=DriverManager.getConnection(serverJDBC,username,password);
                                        
					stmt=conn.createStatement();
                                        stmt.setQueryTimeout(15);
				}
				catch (Exception e)
				{
					error=e.toString();
					log.mensaje("JdbcMgr");
					log.mensaje("server: "+serverJDBC);
					log.mensaje(e.toString());

				}
			}
			else log.mensaje("Server no configurado");
	};
        
	void cargarDriver() {
		try {

			if (tipoServer==Kmysql) Class.forName("com.mysql.jdbc.Driver").newInstance();
                        else if (tipoServer==Ksybase) Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
                        else if (tipoServer==Kmsql) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			//else if (tipoServer==1) DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());// Class.forName("oracle.jdbc.driver").newInstance(); 

		}
		catch (Exception e)
		{
			log.mensaje("cargarDriver");
			log.mensaje(e.toString());

		}
	}
	
	void obtenerConexion() {
 
		try {
			log.mensaje("Obtener conexion");
			conn=DriverManager.getConnection(serverJDBC,username,password);
                        
			//log.mensaje("Obtener Statement");
			if (conn!=null) stmt=conn.createStatement();
		}
		catch (Exception e)
		{
			if (log!=null) {
				log.mensaje("obtenerConexion: tipoServer:"+tipoServer+" "+serverJDBC+" "+username);
				log.mensaje(e.toString());
			}
			else System.out.println("Log nulo");

		}
	}
	
        void pausa() {
            try { 
                Thread.sleep(1000);
            }
	    catch (Exception e){};
            
        }
	public void ejecutarQuery(String query)
	{
		int reintento=0;
                boolean ok=false;
                
                while ((!ok) && (reintento<2)) {           
                    try {
                            tiempoinicio=System.currentTimeMillis();
                            rs=stmt.executeQuery(query);
                            ok=true;
                    }
                    catch (Exception e)
                    {
                            reintento++;
                            log.mensaje("REINTENTO ejecutarQuery:"+query);
                            log.mensaje(e.toString());
                            log.escribir(e);
                            pausa();
                            crearJdbc();
                    }
                }
	}
        
        public boolean ejecutarUpdate(String query)
	{
                int reintento=0;
                boolean ok=false;
                
                while ((!ok) && (reintento<2)) {
                    try {
                            tiempoinicio=System.currentTimeMillis();
                            stmt.executeUpdate(query);
                            ok=true;
                    }
                    catch (Exception e)
                    {
                            reintento++;
                            log.mensaje("REINTENTO: ejecutarQuery:"+query);
                            log.mensaje(e.toString());
                            log.escribir(e);
                            pausa();
                            crearJdbc();
                    }
                };
                
                return ok;
	}
	
	public int leerSiguienteEnQuery()
	{
		int hay=0;
		
		try {
		
			if ((rs!=null) && rs.next()) hay=1;
		
		}
		catch (Exception e)
		{
			log.mensaje("leerSiguienteEnQuery");
			log.mensaje(e.toString());

		}
		
		
		
		return hay;
	}
	
	public String darColumnaQuery(int queColumna) 
	{
		String res="";
		
		try {
			res=rs.getString(queColumna);
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQuery");
			log.escribir(e);

		}
		
		return res;
	}
	
	public String darColumnaQuery(String queColumna) 
	{
		String res="";
		
		try {
			res=rs.getString(queColumna);
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQuery");
			log.escribir(e);

		}
		
		return res;
	}
        
        public int darColumnaQueryInt(String queColumna) 
	{
		int res=0;
		try {
			res=rs.getInt(queColumna);
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQuery");
			log.escribir(e);
		}
		return res;
	}
	
        public int darColumnaQueryInt(int queColumna) 
	{
		int res=0;
		try {
			res=rs.getInt(queColumna);
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQuery");
			log.escribir(e);
		}
		return res;
	}
        
        public float darColumnaQueryFloat(int queColumna) 
	{
		float res=0;
		try {
			res=rs.getFloat(queColumna);
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQuery");
			log.escribir(e);
		}
		return res;
	}
        
        public double darColumnaQueryDouble(String queColumna) 
	{
		double res=0;
		try {
			res=rs.getDouble(queColumna);
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQuery");
			log.escribir(e);
		}
		return res;
	}
        
	public String darColumnaQueryFecha(int queColumna) 
	{
		Date res;
		String fechastr="";
		
		try {
			res=rs.getDate(queColumna);
			
      SimpleDateFormat formatter;

      formatter = new SimpleDateFormat("dd/MM/yyyy");
      if (res!=null) fechastr = formatter.format(res);
      	else fechastr="";
			
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQueryFecha");
			log.escribir(e);
			
			System.out.println("error darColumnaQueryFecha");
			e.printStackTrace();
		}
		
		
		
		return fechastr;
	}
	
	public String darColumnaQueryFechaChico(int queColumna) 
	{
		Date res;
		String fechastr="";
		
		try {
			res=rs.getDate(queColumna);
			
      SimpleDateFormat formatter;

      formatter = new SimpleDateFormat("dd-MM");
      if (res!=null) fechastr = formatter.format(res);
      	else fechastr="";
			
		}
		catch (Exception e)
		{
			log.mensaje("darColumnaQueryFecha");
			log.escribir(e);
			
			System.out.println("error darColumnaQueryFecha");
			e.printStackTrace();
		}
		
		
		
		return fechastr;
	}
	
	public int agregarDatos(String comandoSQL) 
	{
		int error=0;
		//if (stmt==null) log.mensaje("STMT NULO");
		if (log==null) {
			
		}
		
		try {
			//log.mensaje("EN AGREGAR DATOS");
			stmt.executeUpdate(comandoSQL);
		}
		catch (Exception e)
		{
			error=1;
			if (log!=null) log.mensaje("agregarDatos:"+comandoSQL);
			if (log!=null) log.escribir(e);
			
			System.out.println("error agregardatos:"+comandoSQL);
			e.printStackTrace();
		}
		return error;
	}
	
	public  void cerrarConexiones()
	{
		try {
			if (stmt!=null) stmt.close();
			if (conn!=null) conn.close();
			if (rs!=null) rs.close();
		}
		catch (Exception e)
		{
			log.mensaje("JDBCMGR: Error: cerrarConexiones");
			log.escribir(e);
			
		}
		
	}
	
	public void commit()
	{
		try {
			conn.commit();
		}
		catch (Exception e)
		{
			System.out.println("error en commit");
			e.printStackTrace();
		}
	}
	
	void noCommit()
	{
		try {
			conn.setAutoCommit(false);
		}
		catch (Exception e)
		{
			System.out.println("error nocommit");
			e.printStackTrace();
		}
	}
	
	void autoCommit()
	{
		try {
			conn.setAutoCommit(true);
		}
		catch (Exception e)
		{
			System.out.println("error autocommit");
			e.printStackTrace();
		}
	}
	
	public Connection darConnection() { return conn;};
	public ResultSet darResultSet() { return rs;};
	
	public int darCantColumnas() {
		int columnas=0;
		try {
			ResultSetMetaData rsmd=rs.getMetaData();
			columnas=rsmd.getColumnCount();
		} catch (Exception e) {};
		return columnas;
	}
	
	public void cerrarResultSet() { 
		try {
			if (rs!=null) rs.close();
			//if (stmt!=null) stmt.close();
		}
		catch (Exception e)
		{
		log.mensaje("error en cerrarResultSet");
		log.escribir(e);
			
			System.out.println("error cerrarResultSet");
			e.printStackTrace();
		}
		
	};
	
	boolean darAutoCommit() { 
		boolean auto=false;
		try {
			auto=conn.getAutoCommit();
		}
		catch (Exception e)
		{
			System.out.println("error darAutoCommit");
			e.printStackTrace();
		}
		return auto;
	}
	
	public long darTiempoInicio() {
		return tiempoinicio;
	}
	
	private String serverJDBC,username,password;
	private ResultSet rs;
	private	Connection conn;
	private Statement stmt;
	private DatabaseMetaData dbmd;
	private int conexiones=0;
	private Log log=null;
	public  String error="";
	private int tipoServer=0;
	private long tiempoinicio=0;
        public static int Kmysql=0;
        public static int Ksybase=1;
        public static int Kmsql=2;
}