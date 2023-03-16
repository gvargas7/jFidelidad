/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Set;
/**
 *
 * @author rhirsch
 */

class DatosEmp {

    public DatosEmp(String a,String b,String c,int e) {
        id=a;
        nombre=b;
        empresa=c;
        tipo=e;
    }

    String id="";
    String nombre="";
    String empresa="";
    int tipo=0;
}

class EmpleadoRespuesta {
    public EmpleadoRespuesta(int elstatus,String elnombre,int elcodigo,int eltipo,int elcodadicional,double eldescvariable,int eltamano) {
        status=elstatus;
        nombre=elnombre;
        codigo=elcodigo; //codigo micros
        tipo=eltipo; //1 aplica producto 3 aplica descuento 5 aplica macro 8 aplica descuento variable
        codadicional=elcodadicional; //codigo adicional micros por ajustes
        descuentovariable=eldescvariable; //monto variable para descuentos
        tamano=eltamano;
    }
    
    int status=0;
    String nombre="";
    int codigo=0;
    int tipo=0;
    int tamano=1;
    int codadicional=0;
    double descuentovariable=0;
    String opciones="";
}
public class Empleados extends Thread {
     public Empleados(ConnectionPool elPool,Tareas lasTareas,boolean eschile,boolean escol) {
     
        yatermino=false;
        tareas=lasTareas;
        esChile=eschile;
        esCol=escol;
        pool=elPool;
    }
     
    public Empleados(ConnectionPool elPool) {
    
        pool=elPool;
    }
      
     public void run() 
	{
		tareas.sumar();
		terminarMgr=false;
		logear("Empleados Start");
		cargarEmpleadosPayroll();
                int hora=0;
                
		while (terminarMgr==false) {
                    try {
                            sleep(1000*3600);
                    } catch (Exception e) {};
                    hora=darHoraActual();
                    if (hora==4) cargarEmpleadosPayroll();
		}
		
		logear("Empleados End");
		yatermino=true;
		
		tareas.restar();
	}

	public synchronized void fin()
	{
		try {
			this.interrupt();
			terminarMgr=true;
		}
		catch (Exception e) {
			logear(e.toString());
		}
	}
    int darHoraActual() {
        int hora=0;
        Calendar ahora = Calendar.getInstance();
        hora=ahora.get(Calendar.HOUR_OF_DAY); 
        return hora;
    }
    void cargarUnPayroll(Statement stmt,String query, Vector empleados,String empresa,Hashtable empespeciales) {
        ResultSet rs=null;
        
        try {
            rs=stmt.executeQuery(query);
            while (rs.next()) {
                String aux[]=new String[4];
                aux[0]=reemplazarTexto(rs.getString(1).trim(),"-","").toUpperCase(); //id
                aux[1]=reemplazarTexto(rs.getString(2).trim().toUpperCase(),"'"," "); //nombre
                if (empresa.equals("")) aux[2]=rs.getString(4); //empresa
                else aux[2]=empresa;
                aux[3]=rs.getString(3); //tipo
                if (empespeciales!=null) {
                    DatosEmp auxesp=(DatosEmp)empespeciales.get(aux[0]);
                    if (auxesp!=null) {
                        aux[1]=auxesp.nombre;
                        aux[2]=auxesp.empresa;
                        aux[3]=auxesp.tipo+"";
                        empespeciales.remove(aux[0]);
                    }
                    //empleados.addElement(aux);
                }
                empleados.addElement(aux);
            }
            if (empleados.size()>100) {
                logear("Empleados: Leido: "+empleados.size());                
            }
            else logear("Empleado.cargarUnPayroll.Error al leer empleados de payroll");
            
            if (rs!=null) rs.close();
        } catch (Exception e ) {
            e.printStackTrace();
            logear("ERROR: cargarUnPayroll: "+e.toString());
        }   
       
    }
    void cargarEmpleadosPayrollViejo() {
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        //String query="SELECT  Q.numdoc dni,replace(Q.NOMBRE,',','') Apellido_Nombre,case when clasif<3 then '0' when clasif=3 then '1' end ";
        //query=query+" FROM REMPLES Q where estado='A' and clasif>=0 GROUP BY Q.numdoc, Q.NOMBRE,clasif order by Q.nombre";
        String querysbxgral="SELECT  Q.numdoc dni,replace(Q.NOMBRE,',','') Apellido_Nombre,clasif ";//case when clasif=1 then '2' else  '1' end ";
        //(133,135,136,137,138,139,140,141)
        querysbxgral=querysbxgral+"FROM REMPLES Q where estado='A' and (clasif<=3) and Cencos not in(333,335,336,337,338,339,341)  GROUP BY Q.numdoc, Q.NOMBRE,clasif order by Q.nombre";
        String queryff="SELECT  Q.numdoc dni,replace(Q.NOMBRE,',','') Apellido_Nombre,clasif ";//case when clasif=1 then '2' else  '1' end ";
        //(133,135,136,137,138,139,140,141)
        queryff=queryff+"FROM REMPLES Q where estado='A' and (clasif<=3) and Cencos not in(133,135,136,137,138,139,140,141)  GROUP BY Q.numdoc, Q.NOMBRE,clasif order by Q.nombre";
        String queryscasbx="SELECT  Q.numdoc dni,replace(Q.NOMBRE,',','') Apellido_Nombre,1 ";
        queryscasbx+=" FROM REMPLES Q where estado='A' and Cencos in(333,335,336,337,338,339,341) GROUP BY Q.numdoc, Q.NOMBRE,clasif order by Q.nombre ";
        
        String queryscaff="SELECT  Q.numdoc dni,replace(Q.NOMBRE,',','') Apellido_Nombre,1 ";
        queryscaff+=" FROM REMPLES Q where estado='A' and Cencos in(133,135,136,137,138,139,140,141) GROUP BY Q.numdoc, Q.NOMBRE,clasif order by Q.nombre ";
        
        String payrollSrv="172.31.1.23";
        String payrollDB="payArg";
        String usuarioDB="web;password=w3b4ls34";
        
        String querysbx="";
        String query="";
        Hashtable empespeciales=new Hashtable(2000,1000);
        if (esChile) {
            payrollDB="payFF";
            payrollSrv="192.168.198.184";
            usuarioDB="micros;password=$1$t3m4$";
            query="SELECT  Q.rut dni,replace(Q.NOMBRE,',','') Apellido_Nombre,1,'empresa'= case when empresa=4 then 'BK' when empresa=2 then 'PF'  end  ";
            query=query+" FROM REMPLES Q where estado='A' and clasif=0 ";
            querysbx="SELECT  Q.rut dni,replace(Q.NOMBRE,',','') Apellido_Nombre,division,'empresa'= case when empresa=3 then 'GC' when empresa=2 then 'SBX'  end  ";
            querysbx=querysbx+" FROM REMPLES Q where estado='A'";
            
        }
        
        String queryborrar="delete from empleados";// where empresa='BK' or empresa='PF'";
        Vector empleados=new Vector(8000,2000);
        
        try {
            //cargo empleados especiales
            
            int con=0;
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                //Cargo empleados especiales
                jdbc.ejecutarQuery("Select * from empleados_especiales");
                String espid="";
                String espnombre="";
                String espempresa="";
                int esptipo=0;
                int cuantosesp=0;
                while (jdbc.leerSiguienteEnQuery()==1) {
                    //String aux[]=new String[4];
                    espid=reemplazarTexto(jdbc.darColumnaQuery("id").trim(),"-","").toUpperCase(); //id
                    espnombre=reemplazarTexto(jdbc.darColumnaQuery("nombre").trim().toUpperCase(),"'"," "); //nombre
                    espempresa=jdbc.darColumnaQuery("empresa"); //empresa
                    esptipo=jdbc.darColumnaQueryInt("tipo"); //tipo
                    empespeciales.put(espid, new DatosEmp(espid,espnombre,espempresa,esptipo));
                    cuantosesp++;
                    //empleados.addElement(aux);
                   // System.out.println("Especial: "+aux[0]+" "+aux[1]);
                }
                System.out.println("Empleados: Especiales "+cuantosesp);
                
                //Cargo datos de promociones
                if (!(esChile) && !(esCol)) {
                    jdbc.ejecutarQuery("SELECT [id],concat(nombre, ' ',apellido) nombreapellido,empresa,tipo  FROM [Clientes_Promociones]");
                    cuantosesp=0;
                    while (jdbc.leerSiguienteEnQuery()==1) {
                        //String aux[]=new String[4];
                        espid=reemplazarTexto(jdbc.darColumnaQuery("id").trim(),"-","").toUpperCase(); //id
                        espnombre=reemplazarTexto(jdbc.darColumnaQuery("nombreapellido").trim().toUpperCase(),"'"," "); //nombre
                        espempresa=jdbc.darColumnaQuery("empresa"); //empresa
                        esptipo=jdbc.darColumnaQueryInt("tipo"); //tipo
                        empespeciales.put(espid, new DatosEmp(espid,espnombre,espempresa,esptipo));
                        cuantosesp++;
                        //empleados.addElement(aux);
                       // System.out.println("Especial: "+aux[0]+" "+aux[1]);
                    }
                    System.out.println("Empleados: Promociones "+cuantosesp);
                };
                

                pool.cerrarConexion(con);
            }
            else logear("Empelados.cargarEmpleadosPayroll. No hay conexion");
            
            conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user="+usuarioDB);
            stmt=conn.createStatement();
           // rs=stmt.executeQuery(query);
            
            if (esChile) {
                cargarUnPayroll(stmt,query,empleados,"",empespeciales);
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                
                payrollDB="payPF";
                conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user="+usuarioDB);
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,query,empleados,"",empespeciales);  
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                
                payrollDB="payroll";
                payrollSrv="192.168.198.148";
                usuarioDB="consultaapp;password=Alsea.99";
                conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user="+usuarioDB);
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,querysbx,empleados,"",empespeciales);  
                 
            }
            else if (!esCol) {
                cargarUnPayroll(stmt,queryff,empleados,"BK",empespeciales);
                cargarUnPayroll(stmt,queryscaff,empleados,"SCA",empespeciales);
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                
                payrollDB="payStar";
                conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user=web;password=w3b4ls34");
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,querysbxgral,empleados,"SBX",empespeciales);  
                cargarUnPayroll(stmt,queryscasbx,empleados,"SCA",empespeciales);  
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                payrollDB="payChangs";
                conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user=web;password=w3b4ls34");
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,queryff,empleados,"PF",empespeciales);
                cargarUnPayroll(stmt,queryscaff,empleados,"SCA",empespeciales);  
            }
            
          /*  while (rs.next()) {
                String aux[]=new String[4];
                aux[0]=reemplazarTexto(rs.getString(1).trim(),"-","").toUpperCase(); //id
                aux[1]=rs.getString(2).trim().toUpperCase(); //nombre
                aux[2]=rs.getString(3); //empresa
                aux[3]=rs.getString(4); //tipo
                empleados.addElement(aux);
            }*/
        
            if (empleados.size()>100) {
                logear("Empleados: Leido: "+empleados.size());
                
                if ((con=pool.pedirConexion())>=0) {
                    JdbcMgr jdbc=pool.darJdbc(con);
                    //borro lista actual

                    jdbc.ejecutarUpdate(queryborrar);
                    String insert="";
                    String aux[]=null;
                    for (int i=0;i<empleados.size();i++) {
                        aux=(String [])empleados.elementAt(i);
                        if (aux[1].length()>50) aux[1]=aux[1].substring(0,49);
                        insert="insert into empleados values('"+aux[0]+"','"+aux[1]+"','"+aux[2]+"',"+aux[3]+")";
                        jdbc.ejecutarUpdate(insert);
                    }
                   
                    
                    //cargo los empleados especiales restantes
                    
                    Set<String> keys = empespeciales.keySet();
                    for(String key: keys){
                       //Enumeration<Integer> enumKey = empespeciales.elements();
                    //while(enumKey.hasMoreElements()) {
                       // String key = (String)enumKey.nextElement();
                        empespeciales.get(key);
                        DatosEmp auxemp = (DatosEmp)empespeciales.get(key);
                        
                        if (auxemp.nombre.length()>50) auxemp.nombre=auxemp.nombre.substring(0,49);
                        insert="insert into empleados values('"+auxemp.id+"','"+auxemp.nombre+"','"+auxemp.empresa+"',"+auxemp.tipo+")";
                        jdbc.ejecutarUpdate(insert);
                    }
                    logear("Empleados: Cargados: "+(empleados.size()+empespeciales.size()));
                    pool.cerrarConexion(con);
                }
                               
            }
            else logear("Empleado.cargarEmpleadosPayroll.Error al leer empleados de payroll");
            
            if (rs!=null) rs.close();
            if (stmt!=null) stmt.close();
            if (conn!=null) conn.close();
        } catch (Exception e ) {
            e.printStackTrace();
            logear("ERROR: cargarEmpleadosPayroll: "+e.toString());
        }
        
       
    }
    
    void cargarEmpleadosPayroll() {
        Connection conn=null;
        Statement stmt=null;
        ResultSet rs=null;
        
        Propiedades prop=new Propiedades();
        
        //ConnectionPool poolp=new ConnectionPool(1,prop.gBdPayroll,prop.gUserPayroll,prop.gPassPayroll,3,5000,pool.darLog(),JdbcMgr.Kmsql);

        
        String querysbxgral="SELECT  numdoc dni,nombre Apellido_Nombre,clasif  ";
        querysbxgral+=" FROM empleados where estado='A' and (clasif<=3) and codtienda not in(333,335,336,337,338,339,340,341,342) and empresa='SBX' GROUP BY numdoc, NOMBRE,clasif order by nombre";
        
        
        
        String queryff="SELECT  numdoc dni,NOMBRE Apellido_Nombre,clasif ";//case when clasif=1 then '2' else  '1' end ";
        queryff=queryff+"FROM empleados Q where estado='A' and (clasif<=3) and codtienda not in(333,335,336,337,338,339,340,341,342)  and empresa='BK' GROUP BY numdoc, NOMBRE,clasif order by nombre";
        String queryscasbx="SELECT  numdoc dni,replace(NOMBRE,',','') Apellido_Nombre,1 ";
        queryscasbx+=" FROM empleados Q where estado='A' and codtienda in(333,335,336,337,338,339,340,341,342) and empresa='SBX' GROUP BY numdoc, NOMBRE,clasif order by nombre ";
        
        String queryscaff="SELECT  numdoc dni,replace(NOMBRE,',','') Apellido_Nombre,1 ";
        queryscaff+=" FROM empleados Q where estado='A' and codtienda in(333,335,336,337,338,339,340,341,342) and empresa='BK' GROUP BY numdoc, NOMBRE,clasif order by Q.nombre ";
        
        String queryscapf="SELECT  numdoc dni,replace(NOMBRE,',','') Apellido_Nombre,1 ";
        queryscapf+=" FROM empleados Q where estado='A' and codtienda in (333,335,336,337,338,339,340,341,342) and empresa='PF' GROUP BY numdoc, NOMBRE,clasif order by Q.nombre ";
        String querypf="SELECT  numdoc dni,NOMBRE Apellido_Nombre,clasif ";//case when clasif=1 then '2' else  '1' end ";
        querypf=querypf+"FROM empleados Q where estado='A' and (clasif<=3) and codtienda not in(333,335,336,337,338,339,340,341,342)  and empresa='PF' GROUP BY numdoc, NOMBRE,clasif order by nombre";
        
       
        String payrollSrv="172.31.1.14";
        String payrollDB=prop.gBdPayroll;
        String usuarioDB=prop.gUserPayroll+";password="+prop.gPassPayroll;
        
        String querysbx="";
        String query="";
        Hashtable empespeciales=new Hashtable(2000,1000);
        if (esChile) {
           // payrollDB="IntegraPpsoftAlsea"; cambio prop
            //payrollSrv="192.168.198.186"; cambio prop
            //usuarioDB="ColaboradoresPPsoft;password=ColaboradoresPPsoft"; cambio prop
         //   query="SELECT  Q.rut dni,replace(Q.NOMBRE,',','') Apellido_Nombre,1,'empresa'= case when empresa=4 then 'BK' when empresa=2 then 'PF'  end  ";
         //   query=query+" FROM REMPLES Q where estado='A' and clasif=0 ";
         //   querysbx="SELECT  Q.rut dni,replace(Q.NOMBRE,',','') Apellido_Nombre,division,'empresa'= case when empresa=3 then 'GC' when empresa=2 then 'SBX'  end  ";
         //   querysbx=querysbx+" FROM REMPLES Q where estado='A'";
            query="select rut dni,replace(CONCAT(Apellido,' ',Nombre),',','') Apellido_Nombre  ,tipoBeneficio as tipo ";
            query+=",'empresa'= case when empresa='STARBUCKS' then 'SBX' when empresa='BURGUERKING' then 'BK'  when empresa='PFCHANG' then 'PF' when empresa='CHILIS' then 'GC' else empresa end FROM [IntegraPpsoftAlsea].[dbo].[ViewColaboradores] where estado='A'";
            
        }
        
        String queryborrar="delete from empleados";// where empresa='BK' or empresa='PF'";
        Vector empleados=new Vector(8000,2000);
        
        try {
            //cargo empleados especiales
            
            int con=0;
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                //Cargo empleados especiales
                jdbc.ejecutarQuery("Select * from empleados_especiales");
                String espid="";
                String espnombre="";
                String espempresa="";
                int esptipo=0;
                int cuantosesp=0;
                while (jdbc.leerSiguienteEnQuery()==1) {
                    //String aux[]=new String[4];
                    espid=reemplazarTexto(jdbc.darColumnaQuery("id").trim(),"-","").toUpperCase(); //id
                    espnombre=reemplazarTexto(jdbc.darColumnaQuery("nombre").trim().toUpperCase(),"'"," "); //nombre
                    espempresa=jdbc.darColumnaQuery("empresa"); //empresa
                    esptipo=jdbc.darColumnaQueryInt("tipo"); //tipo
                    empespeciales.put(espid, new DatosEmp(espid,espnombre,espempresa,esptipo));
                    cuantosesp++;
                    //empleados.addElement(aux);
                   // System.out.println("Especial: "+aux[0]+" "+aux[1]);
                }
                System.out.println("Empleados: Especiales "+cuantosesp);
                
                //Cargo datos de promociones
                if (!(esChile) && !(esCol)) {
                    jdbc.ejecutarQuery("SELECT [id],concat(nombre, ' ',apellido) nombreapellido,empresa,tipo  FROM [Clientes_Promociones]");
                    cuantosesp=0;
                    while (jdbc.leerSiguienteEnQuery()==1) {
                        //String aux[]=new String[4];
                        espid=reemplazarTexto(jdbc.darColumnaQuery("id").trim(),"-","").toUpperCase(); //id
                        espnombre=reemplazarTexto(jdbc.darColumnaQuery("nombreapellido").trim().toUpperCase(),"'"," "); //nombre
                        espempresa=jdbc.darColumnaQuery("empresa"); //empresa
                        esptipo=jdbc.darColumnaQueryInt("tipo"); //tipo
                        empespeciales.put(espid, new DatosEmp(espid,espnombre,espempresa,esptipo));
                        cuantosesp++;
                        //empleados.addElement(aux);
                       // System.out.println("Especial: "+aux[0]+" "+aux[1]);
                    }
                    System.out.println("Empleados: Promociones "+cuantosesp);
                };
                

                pool.cerrarConexion(con);
            }
            else logear("Empelados.cargarEmpleadosPayroll. No hay conexion");
            
            
            //conexion al servidor "payroll"
            conn = DriverManager.getConnection(payrollDB+";user="+usuarioDB);
            stmt=conn.createStatement();
           // rs=stmt.executeQuery(query);
            
            if (esChile) {
                cargarUnPayroll(stmt,query,empleados,"",empespeciales);
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                
               /* payrollDB="payPF";
                conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user="+usuarioDB);
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,query,empleados,"",empespeciales);  
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                
                payrollDB="payroll";
                payrollSrv="192.168.198.148";
                usuarioDB="consultaapp;password=Alsea.99";
                conn = DriverManager.getConnection("jdbc:sqlserver://"+payrollSrv+";databaseName="+payrollDB+";user="+usuarioDB);
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,querysbx,empleados,"",empespeciales);  
                 */
            }
            else if (!esCol) {
                cargarUnPayroll(stmt,queryff,empleados,"BK",empespeciales);
                cargarUnPayroll(stmt,queryscaff,empleados,"SCA",empespeciales);
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                
                //payrollDB="payStar";
                conn = DriverManager.getConnection(payrollDB+";user="+usuarioDB);
                stmt=conn.createStatement();
                cargarUnPayroll(stmt,querysbxgral,empleados,"SBX",empespeciales);  
                cargarUnPayroll(stmt,queryscasbx,empleados,"SCA",empespeciales);  
                if (stmt!=null) stmt.close();
                if (conn!=null) conn.close();
                //payrollDB="payChangs";
                //conn = DriverManager.getConnection(payrollDB+";user="+usuarioDB);
                //stmt=conn.createStatement();
                //cargarUnPayroll(stmt,querypf,empleados,"PF",empespeciales);
               // cargarUnPayroll(stmt,queryscapf,empleados,"SCA",empespeciales);  
            }
            
          /*  while (rs.next()) {
                String aux[]=new String[4];
                aux[0]=reemplazarTexto(rs.getString(1).trim(),"-","").toUpperCase(); //id
                aux[1]=rs.getString(2).trim().toUpperCase(); //nombre
                aux[2]=rs.getString(3); //empresa
                aux[3]=rs.getString(4); //tipo
                empleados.addElement(aux);
            }*/
        
            if (empleados.size()>100) {
                logear("Empleados: Leido: "+empleados.size());
                
                if ((con=pool.pedirConexion())>=0) {
                    JdbcMgr jdbc=pool.darJdbc(con);
                    //borro lista actual

                    jdbc.ejecutarUpdate(queryborrar);
                    String insert="";
                    String aux[]=null;
                    for (int i=0;i<empleados.size();i++) {
                        aux=(String [])empleados.elementAt(i);
                        if (aux[1].length()>50) aux[1]=aux[1].substring(0,49);
                        insert="insert into empleados values('"+aux[0]+"','"+aux[1]+"','"+aux[2]+"',"+aux[3]+")";
                        jdbc.ejecutarUpdate(insert);
                    }
                   
                    
                    //cargo los empleados especiales restantes
                    
                    Set<String> keys = empespeciales.keySet();
                    for(String key: keys){
                       //Enumeration<Integer> enumKey = empespeciales.elements();
                    //while(enumKey.hasMoreElements()) {
                       // String key = (String)enumKey.nextElement();
                        empespeciales.get(key);
                        DatosEmp auxemp = (DatosEmp)empespeciales.get(key);
                        
                        if (auxemp.nombre.length()>50) auxemp.nombre=auxemp.nombre.substring(0,49);
                        insert="insert into empleados values('"+auxemp.id+"','"+auxemp.nombre+"','"+auxemp.empresa+"',"+auxemp.tipo+")";
                        jdbc.ejecutarUpdate(insert);
                    }
                    logear("Empleados: Cargados: "+(empleados.size()+empespeciales.size()));
                    pool.cerrarConexion(con);
                }
                               
            }
            else logear("Empleado.cargarEmpleadosPayroll.Error al leer empleados de payroll");
            
            if (rs!=null) rs.close();
            if (stmt!=null) stmt.close();
            if (conn!=null) conn.close();
        } catch (Exception e ) {
            e.printStackTrace();
            logear("ERROR: cargarEmpleadosPayroll: "+e.toString());
        }
        
       
    }
     void logear(String mensaje) {
        pool.darLog().mensaje(mensaje);
        //if (log) {
        //    System.out.println(mensaje);
            
        //}
    }
     
    void redimirDescEmpleado(String empleado, String id, String empresa,String desc,String tienda,String terminal,String monto) {
        
         String query="insert into empleados_redimidos values('"+empleado+"','"+id+"','"+empresa+"','"+desc+"',"+tienda;
         query=query+",'"+terminal+"',"+monto+",getdate())";
        
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("DescEmpleado: "+empleado+":"+desc+" redimido. Tienda: "+tienda+" Term: "+terminal);
                 else logear("DescEmpleado: "+empleado+":"+desc+" error en redimido.");
                 
                 pool.cerrarConexion(con);
            }
            else logear("DescEmpleado: "+empleado+":"+desc+" .redimirCupon. No hay conexion");
         } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    EmpleadoRespuesta validarDescuentoEmpleado(String empresa,String empleado,Double monto,String tienda,String caja,String codigodescopcion,int cuantosprodticket, String[] prodticket,String [] prodcant, double[] prodmonto) {
        // res=0 error conexion
        // res=1 ok aplica codigo
        // res=2 empleado no encontrado
        // res=3 Limite diario superado
        // res=4 Limite mensual superado
        EmpleadoRespuesta respuesta=new EmpleadoRespuesta(0,"",0,0,0,0,1);
        Double montooriginal=monto;
        int con=0;
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            //empresa="SBX";
            String whereopcion="";
            if (!codigodescopcion.equals("")) whereopcion=" and descuento='"+codigodescopcion+"' and B.tipoempleado=0";
            else whereopcion=" and (A.tipo=B.tipoempleado )";
            String query="SELECT [id],[nombre],tipodescuento,descuento,monto,codmicrosinferior,codmicrossuperior,limitediario,limitemensual,codmicrosadicional,montodiario, porcdescuento, controlamonto,controlaproductos,empresaemp,multiemp FROM [Empleados] A,empleados_descuentos B where  A.empresa=B.empresaemp and A.id='"+empleado+"' and empresaredimido='"+empresa+"' "+whereopcion;
            String insert="";
            String queryempresa=" and empresa='"+empresa+"' ";
            String queryconsumosdiario="SELECT count(*) FROM [Empleados_Redimidos] where id='"+empleado+"' and DATEDIFF(day, fecha, getdate()) = 0 ";//and empresa='"+empresa+"' and descuento='";
            String queryconsumosdiariomonto="SELECT sum(montodescontado) consumido FROM [Empleados_Redimidos] where id='"+empleado+"' and DATEDIFF(day, fecha, getdate()) = 0 ";//and empresa='"+empresa+"' and descuento='";
            String queryconsumosmes="SELECT count(*) FROM [Empleados_Redimidos] where id='"+empleado+"' and fecha>='"+darMes(true)+"' and fecha<'"+darMes(false)+"' ";//and empresa='"+empresa+"' and descuento='";
            String nombredescuento="";
            String empresaemp="";
            System.out.println(query);
            jdbc.ejecutarQuery(query);
            if (jdbc.leerSiguienteEnQuery()==1) {
               respuesta.nombre=jdbc.darColumnaQuery("nombre");
               if (respuesta.nombre.length()>25) respuesta.nombre=respuesta.nombre.substring(0,25);
               respuesta.tipo=jdbc.darColumnaQueryInt("tipodescuento");
               int limitediario=jdbc.darColumnaQueryInt("limitediario");
               int limitemensual=jdbc.darColumnaQueryInt("limitemensual");
               nombredescuento=jdbc.darColumnaQuery("descuento");
               double montolimite=jdbc.darColumnaQueryDouble("monto");
               int codmicrosadicional=jdbc.darColumnaQueryInt("codmicrosadicional");
               int codigomicros=0;
               if (monto<=montolimite) codigomicros=jdbc.darColumnaQueryInt("codmicrosinferior");
               else codigomicros=jdbc.darColumnaQueryInt("codmicrossuperior");
               //datos para el control por dinero
               double montodiario=jdbc.darColumnaQueryDouble("montodiario");
               double porcdescuento=jdbc.darColumnaQueryDouble("porcdescuento");
               int controlamonto=jdbc.darColumnaQueryInt("controlamonto");
               int controlaproductos=jdbc.darColumnaQueryInt("controlaproductos");
               empresaemp=jdbc.darColumnaQuery("empresaemp");
               int multiemp=jdbc.darColumnaQueryInt("multiemp");
               if (multiemp==1) queryempresa="";
               double montoadescontar=Math.round((porcdescuento*monto)*100)/100.0;
               if (monto>montolimite) montoadescontar=Math.round((porcdescuento*montolimite)*100)/100.0;
               //---------------------------------
               respuesta.codigo=codigomicros;
               respuesta.codadicional=codmicrosadicional;
               
               //Si es multiple opciones armo la lista de descuentos
               if (respuesta.tipo==4) {
                   String querydescuentomultiple="SELECT [Descuento_Cod],[descripcion]  FROM [Empleados_Opciones] where descuento='"+nombredescuento+"' order by orden";
                   jdbc.ejecutarQuery(querydescuentomultiple);
                   int cantmult=0;
                   String opciones="";
                   while (jdbc.leerSiguienteEnQuery()==1) {
                       cantmult++;
                       opciones=opciones+"|"+jdbc.darColumnaQuery("Descuento_Cod")+"|"+jdbc.darColumnaQuery("descripcion");
                   }
                   opciones=empleado+"|"+cantmult+opciones;
                   respuesta.opciones=opciones;
                   respuesta.status=1;
               } else {
                    //no es multiple opciones continuo normal
                    jdbc.ejecutarQuery(queryconsumosdiario+queryempresa+" and descuento='"+nombredescuento+"'");
                    if (jdbc.leerSiguienteEnQuery()==1) {
                        int consumos=jdbc.darColumnaQueryInt(1);
                        if (consumos>=limitediario) respuesta.status=3;
                        else {
                            jdbc.ejecutarQuery(queryconsumosmes+queryempresa+" and descuento='"+nombredescuento+"'");
                            if (jdbc.leerSiguienteEnQuery()==1) {
                                 consumos=jdbc.darColumnaQueryInt(1);
                                 if (consumos>=limitemensual) respuesta.status=4;
                                 else {
                                     respuesta.status=1;
                                 }
                            }
                            else respuesta.status=1;
                        }
                    }
                    else {
                        respuesta.status=1;
                    }
                    
                    // valido que el ticket no tenga códigos no aprobados
                    if ((empresaemp.equals("BK") || empresaemp.equals("SBX") || empresaemp.equals("PF") || empresaemp.equals("SCA")) && (respuesta.tipo==8)) {
                        if (respuesta.tipo!=4 && cuantosprodticket>0 && controlaproductos==1) {
                            logear("CuantosProd: "+cuantosprodticket);
                            query="SELECT codigo FROM [Empleados_Codigos_Bloqueados] where empresa='"+empresa+"' and codigo in(";
                            for (int i=0;i<cuantosprodticket;i++) {
                                if ((i>0) ) {
                                    query+=",";
                                }
                                query+=prodticket[i];
                                
                            }
                            query+=")";
                            logear(query);
                            try {
                                jdbc.ejecutarQuery(query);
                                if ((prodcant==null) && (jdbc.leerSiguienteEnQuery()==1)) {
                                    //hay productos no aprobados en el ticket
                                    respuesta.status=5;
                                } else {
                                    if (prodcant!=null) {
                                        int i=0;
                                       // boolean encontre=false;
                                        while (jdbc.leerSiguienteEnQuery()==1) {
                                            i=0;
                                            while ((i<cuantosprodticket) ) {
                                                if (jdbc.darColumnaQuery(1).equals(prodticket[i]))
                                                    monto=monto-prodmonto[i];
                                                i++;
                                            }
                                        }
                                        montoadescontar=Math.round((porcdescuento*monto)*100)/100.0;
                                        if (monto>montolimite) montoadescontar=Math.round((porcdescuento*montolimite)*100)/100.0;
                                        if (montoadescontar==0) {
                                          respuesta.status=6; //no hay nada que descontar  
                                        }
                                    }
                                }
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                logear("Empleados.validarDescuentoEmpleado.Leer . CuantosProd: "+cuantosprodticket+"  codigos error:"+e.toString());
                                for (int i=0;i<cuantosprodticket;i++) {
                                
                                    logear(i+" = "+prodticket[i]);
                                }
                                
                                logear(query);
                            };
                                
                        }
                    }
                    if (respuesta.status==1 && controlamonto==1) {
                        //valido si supero montos diarios
                        jdbc.ejecutarQuery(queryconsumosdiariomonto+queryempresa+" and descuento='"+nombredescuento+"'");
                        if (jdbc.leerSiguienteEnQuery()==1) {
                            double montoconsumido=jdbc.darColumnaQueryDouble("consumido");
                            double difmontos=montodiario-montoconsumido;
                            if (difmontos>0) {
                                //le queda saldo
                                if (difmontos<montoadescontar) montoadescontar=difmontos;
                            }
                            else //no le queda saldo 
                            {
                              respuesta.status=3;  
                             }
                        };
                    }
                    
                    //------------------------------------------------
                    respuesta.descuentovariable=montoadescontar;
                    if (respuesta.status==1) {
                        query="insert into empleados_redimidos values('"+respuesta.nombre+"','"+empleado+"','"+empresa+"','"+nombredescuento+"',"+tienda;
                        query=query+",'"+caja+"',"+montooriginal+",getdate(),"+montoadescontar+")";
                        if (jdbc.ejecutarUpdate(query))
                         logear("DescEmpleado: "+empleado+":"+nombredescuento+" redimido. Tienda: "+tienda+" Term: "+caja);
                         else logear("DescEmpleado: "+empleado+":"+nombredescuento+" error en redimido.");
                    }
               }
               
            } else {
                respuesta.status=2; //empleado no encontrado
            }
           
            pool.cerrarConexion(con);
        }
        else {
            logear("Empelados.validarDescuentoEmpleado. No hay conexion");
            respuesta.status=0; //empleado no encontrado
        }
           
        
        
       return respuesta;
    }
    String darMes(boolean primerdia) {
        Calendar ahora = Calendar.getInstance();
        //ahora.add(Calendar.DATE, -1);
        int mes=0;
        int dia=0;
        int year=0;
        
        if (!primerdia) {
            ahora.add(Calendar.MONTH, 1);
            mes=ahora.get(Calendar.MONTH)+1;
            dia=ahora.get(Calendar.DATE);
            year=ahora.get(Calendar.YEAR);
        }
        else {
            mes=ahora.get(Calendar.MONTH)+1;
            dia=ahora.get(Calendar.DATE);
            year=ahora.get(Calendar.YEAR);
        }
        String fecha=year+"/"+mes+"/01";
       // System.out.println("Fecha="+fecha);

        return fecha;
    }
    
    public String reemplazarTexto(String original, String abuscar, String reemplazo) {
		String nuevo="";
		int adonde=original.indexOf(abuscar);
		if (adonde<0) nuevo=original;
		while (adonde>=0) {
			if (adonde>0) nuevo=original.substring(0,adonde);
			nuevo=nuevo+reemplazo;
			nuevo=nuevo+original.substring(adonde+abuscar.length(),original.length());
			original=nuevo;
			adonde=original.indexOf(abuscar,adonde+reemplazo.length());
		}
		
		return nuevo;
    }
    
    boolean terminarMgr;
    boolean yatermino=false;
    Tareas tareas=null;
    boolean esChile=false;
    boolean esCol=false;
    static ConnectionPool pool=null;
    String gUser="fidelidad";
    String gPass="f1d3l1d4d$";
}
