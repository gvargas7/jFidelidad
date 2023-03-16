/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
/**
 *
 * @author rhirsch
 */
public class Campanas  extends Thread {
    public Campanas(ConnectionPool elpool,Tareas lasTareas,Log elLog,boolean eschile,boolean escol) {
        chile=eschile;
        colombia=escol;
        if (campanasOffline==null) {
            campanasOffline=new String[4];
            campanasOffline[0]=""; //BK
            campanasOffline[1]=""; //SBX
            campanasOffline[2]=""; //BK
            campanasOffline[3]=""; //SBX
        }
        if (elpool!=null) pool=elpool;
        if (campanas==null) {
            campanas=new Hashtable(40,10);
            cargarCampanas();
        }
        if (campanascodigos==null) {
            campanascodigos=new Hashtable(40,10);
            //cargarCampanasCodigos();
        }
        if (campanasopciones==null) {
            campanasopciones=new Hashtable(100,20);
            cargarCampanasOpciones();
        }
        if (campanasdias==null) {
            campanasdias=new Hashtable(100,20);
            cargarCampanasDias();
        }
        if (versionisl==null) {
            versionisl=new Hashtable(5,2);
            cargarVersionIsl();
        }
        if (tiendas==null) {
            tiendas=new Hashtable(300,50);
            cargarTiendas();
        }
        yatermino=false;
        tareas=lasTareas;
        log=elLog;
        
    }
    
    public Campanas() {
        
    }
    
    void cambiarPool(ConnectionPool elPool) {
        pool=elPool;
    }
    
    int tipoCampana(String codigo,String marca) {
         
        int tipo=0;
        synchronized (campanas) {
            if (campanas!=null) {
                tipo=((Campana)(campanas.get(marca+"-"+codigo))).tipo;
            }
        }
        return tipo;
    }
    
     Campana darCampana(String codigo,String marca) {
        Campana campana=null;
        synchronized (campanas) {
            if (campanas!=null) {
                campana=(Campana)(campanas.get(marca+"-"+codigo));
            }
        }
        return campana;
    }
     
    String darCampanaTexto(String codigo,String marca) {
        String campana="";
        boolean encontro=false;
        int i=codigo.length();//-2;
        String aux="";
        Campana auxcampana=null;
        while ((encontro==false) && (i>=0)) {
            aux=codigo.substring(0,i);
            auxcampana=(Campana) (campanas.get(marca+"-"+aux));
            if ((auxcampana!=null) ) {
                encontro=true;
                campana=aux;
            }
            else i--;
        }
        return campana;
    }
     Campana darCampanaStock(String cupon) {
        Campana campana=null;
        synchronized (campanascodigos) {
            if (campanascodigos!=null) {
                campana=(Campana)(campanascodigos.get(cupon));
            }
        }
        return campana;
    }
    
     boolean campanaActiva(String codigo,String marca) {
         boolean activa=false;
         if (darCampana(codigo,marca)!=null) activa=true;
         
         return activa;
     }
     void forzarCarga() {
        cargarCampanas();
        //cargarCampanasCodigos();
        cargarCampanasOpciones();
    }
    
     String darCampanasOffline(String marca,String version) {
         int quemarca=0;
         if (marca.equals("BK")) quemarca=0; else quemarca=1;
         //if (version.equals("5.0")) 
         //quemarca=quemarca+2;
         return campanasOffline[quemarca];
         
     }
     
     
     
     void cargarCampanas() {
        Hashtable tablaaux=new Hashtable(100,50);
        int con=0;
        int quemarca=0;
        int []coffline=new int[4];
        coffline[0]=0;
        coffline[1]=0;
        coffline[2]=0;
        coffline[3]=0;
        int cuantosoff=0;
        versioncampanas=(versioncampanas+1) % 1000;
        campanasOffline[0]=""; //BK
        campanasOffline[1]=""; //SBX
        campanasOffline[2]=""; //BK
        campanasOffline[3]=""; //SBX
       // String query="SELECT A.campana,tipo,marca,porstock,codigo_micros,stock FROM [Fidelidad].[dbo].[Campanas] A,fidelidad.dbo.campanas_beneficio B ";
       // query=query+" where inicio<=getdate() and fin>=getdate() and A.campana=B.campana";
        String query="SELECT A.campana,tipo,marca,porstock,stock,poralta,codigo_micros,codigo_micros_tamano,codigo_len,porbanda,validardup,rvc,tienda,webservice,diasvigencia,pordia,porstockindividual,limitediarioalta FROM [Campanas] A ";
        query=query+" where inicio<=getdate() and fin>=getdate() order by len(campana)";
        
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            jdbc.ejecutarQuery(query);
           
            while (jdbc.leerSiguienteEnQuery()==1) {
                String codcamp=jdbc.darColumnaQuery("campana").toUpperCase();
                int tipo=jdbc.darColumnaQueryInt("tipo");
                String marca=jdbc.darColumnaQuery("marca");
                boolean porstock=jdbc.darColumnaQuery("porstock").equals("S");
                boolean poralta=jdbc.darColumnaQuery("poralta").equals("S");
                int codigomicros=jdbc.darColumnaQueryInt("codigo_micros");
                int codigomicrosnivel=jdbc.darColumnaQueryInt("codigo_micros_tamano");
                int codigolen=jdbc.darColumnaQueryInt("codigo_len");
                int stock=jdbc.darColumnaQueryInt("stock");
                boolean porbanda=jdbc.darColumnaQuery("porbanda").equals("S");
                boolean validardup=jdbc.darColumnaQuery("validardup").equals("S");
                int rvc=jdbc.darColumnaQueryInt("rvc");
                int tienda=jdbc.darColumnaQueryInt("tienda");
                int webservice=jdbc.darColumnaQueryInt("webservice");
                int diasvigencia=jdbc.darColumnaQueryInt("diasvigencia");
                int pordia=jdbc.darColumnaQueryInt("pordia");
                boolean porstockindividual=jdbc.darColumnaQuery("porstockindividual").equals("S");
                int limitediarioalta=jdbc.darColumnaQueryInt("limitediarioalta");
               // int stock=jdbc.darColumnaQueryInt("stock");
                Campana campana=new Campana(codcamp,tipo,marca,porstock,poralta,codigomicros,codigomicrosnivel,stock,"",codigolen,porbanda,validardup,rvc,tienda,webservice,diasvigencia,pordia,porstockindividual,limitediarioalta);
                System.out.println("Campaña: "+codcamp);
                if (codcamp!=null) {
                    tablaaux.put(marca+"-"+codcamp, campana);
                    //System.out.println(marca+"-"+codcamp+": "+campana);
                    //genero campañas offline
                    if (poralta && !porstock && tipo!=4 && cuantosoff<250) {
                        //no tomo encuenta las campanas con opciones y porstock
                        cuantosoff++;
                        if (marca.equals("BK")) quemarca=0; else quemarca=1; 
                        if (campanasOffline[quemarca].length()>0) {
                            campanasOffline[quemarca]+="|";
                            //campanasOffline[quemarca+2]+="|";
                        }
                        coffline[quemarca]++;
                        //coffline[quemarca+2]++;
                       // campanasOffline[quemarca]+=codcamp+"|"+tipo+"|"+codigolen+"|"+codigomicros+"|"+rvc;
                        campanasOffline[quemarca]+=codcamp+"|"+tipo+"|"+codigolen+"|"+codigomicros+"|"+rvc+"|"+codigomicrosnivel;
                    }
                   
                }
                else log.mensaje("Campanas: Cargar Campanas: codcamp=null");
            }
            pool.cerrarConexion(con);
            //agrego como primer parametro la cantidad de campanas offline
            if (coffline[0]>0) {
                campanasOffline[0]=coffline[0]+"|"+campanasOffline[0];
                //campanasOffline[2]=coffline[2]+"|"+campanasOffline[2];
            }
            else campanasOffline[0]="0|||||";
            if (coffline[1]>0) {
                campanasOffline[1]=coffline[1]+"|"+campanasOffline[1];
                //campanasOffline[3]=coffline[3]+"|"+campanasOffline[3];
            }
            else campanasOffline[1]="0|||||";
            synchronized (campanas) {
                campanas=tablaaux;
            }
            System.out.println("Campanas: OFFLINE "+cuantosoff+" cargadas");
            //log.mensaje("Campanas: OFFLINE "+cuantosoff+" cargadas");
        }
        else System.out.println("Campanas: cargarCampanas: No hay conexion");
    }
     
      synchronized void cargarVersionIsl() {
        int con=0;
        
        String query="SELECT marca,version,versionactual from version ";
        
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            jdbc.ejecutarQuery(query);
            Hashtable versionislaux=new Hashtable(5,2);
            while (jdbc.leerSiguienteEnQuery()==1) {
                String [] datos=new String[2];
                datos[0]=jdbc.darColumnaQuery("version");
                String marca=jdbc.darColumnaQuery("marca");
                datos[1]=jdbc.darColumnaQuery("versionactual");
                versionislaux.put(marca,datos);
                
                //System.out.println(versionislaux.toString());
            }
            pool.cerrarConexion(con);
            synchronized (versionisl) {
                versionisl=versionislaux;
            }
        }
        else System.out.println("Campanas: cargarVersionIsl: No hay conexion");
    }
     
     String darTienda(String marca,String codigo) {
         String nombre="";
         
         if (tiendas!=null) {
             nombre=(String)tiendas.get(marca+"-"+codigo);
             if (nombre==null) nombre="";
         }
         
         return nombre;
     }
      
     synchronized void cargarTiendas() {
        int con=0;
        
        String query="SELECT nombre,codigo,'BK' as marca from micros.dbo.bktiendas where activa=1 union SELECT nombre,codigo,'SBX' as marca from micros.dbo.sbxtiendas where activa=1";
        if (chile) query="SELECT nombre,codigo,'BK' as marca from micros.dbo.bkcltiendas where activa=1 union SELECT nombre,codigo,'SBX' as marca from micros.dbo.sbxcltiendas where activa=1";
        if (colombia) query="SELECT nombre,codigo,marca from Tiendas";
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            jdbc.ejecutarQuery(query);
            Hashtable tiendas2=new Hashtable(300,150);
            while (jdbc.leerSiguienteEnQuery()==1) {
                tiendas2.put(jdbc.darColumnaQuery("marca")+"-"+jdbc.darColumnaQuery("codigo"),jdbc.darColumnaQuery("nombre"));
                
            }
            pool.cerrarConexion(con);
            synchronized (tiendas) {
                tiendas=tiendas2;
            }
        }
        else System.out.println("Campanas: cargarTiendas: No hay conexion");
    }
       
      double darVersionIsl(String empresa) {
          double aux=0;
          if (versionisl!=null) {
            String []version=(String [])versionisl.get(empresa);
           // System.out.println("VErsio: "+version+" empre="+empresa);
            try {
                aux=Double.parseDouble(version[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
          }
          return aux;
      }
      
      int darVersionCampanas() {
          //versioncampanas++;
          return versioncampanas;
      }
      double darVersionActualIsl(String empresa) {
          double aux=0;
          if (versionisl!=null) {
            String []version=(String [])versionisl.get(empresa);
           // System.out.println("VErsio: "+version+" empre="+empresa);
            try {
                aux=Double.parseDouble(version[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
          }
          return aux;
      }
      
      void cargarCampanasCodigos() {
        Hashtable tablaaux=new Hashtable(100,50);
        int con=0;
        String query="SELECT campana,codigo_micros,codigo_micros_tamano,stock,cupon,codigo_len,porbanda,rvc,tienda,webservice,pordia,porstockindividual,limitediarioalta FROM [Campanas] A ";
        query=query+" where inicio<=getdate() and fin>=getdate() ";
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            jdbc.ejecutarQuery(query);
       
            while (jdbc.leerSiguienteEnQuery()==1) {
                String codcamp=jdbc.darColumnaQuery("campana");
                int codigomicros=jdbc.darColumnaQueryInt("codigo_micros");
                int codigomicrosnivel=jdbc.darColumnaQueryInt("codigo_micros_tamano");
                int stock=jdbc.darColumnaQueryInt("stock");
                String cupon=jdbc.darColumnaQuery("cupon");
                int codigolen=jdbc.darColumnaQueryInt("codigo_len");
                boolean porbanda=jdbc.darColumnaQuery("porbanda").equals("S");
                int rvc=jdbc.darColumnaQueryInt("rvc");
                int tienda=jdbc.darColumnaQueryInt("tienda");
                int webservice=jdbc.darColumnaQueryInt("webservice");
                int pordia=jdbc.darColumnaQueryInt("pordia");
                boolean porstockindividual=jdbc.darColumnaQuery("porstockindividual").equals("S");
                int limitediarioalta=jdbc.darColumnaQueryInt("limiediarioalta");
                Campana campana=new Campana(codcamp,0,"",false,false,codigomicros,codigomicrosnivel,stock,cupon,codigolen,porbanda,false,rvc,tienda,webservice,0,pordia,porstockindividual,limitediarioalta); 
                tablaaux.put(cupon, campana);
               
            }
            pool.cerrarConexion(con);
            synchronized (campanascodigos) {
                campanascodigos=tablaaux;
            }
        }
        else System.out.println("Campanas: cargarCampanasCodigos: No hay conexion");
    }
      
      void cargarCampanasDias() {
        Hashtable tablaaux=new Hashtable(100,50);
        int con=0;
        String query="SELECT B.* FROM [Campanas] A,campanas_dias B where A.campana=B.campana and inicio<=getdate() and fin>=getdate() and A.pordia=1";
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            jdbc.ejecutarQuery(query);
       
            while (jdbc.leerSiguienteEnQuery()==1) {
                String campana=jdbc.darColumnaQuery("campana").toUpperCase();
                
                int []dias=new int[7];
                for (int i=2;i<9;i++)
                    dias[i-2]=jdbc.darColumnaQueryInt(i);
                
                tablaaux.put(campana, dias);              
            }
            pool.cerrarConexion(con);
            synchronized (campanasdias) {
                campanasdias=tablaaux;
            }
        }
        else System.out.println("Campanas: cargarCampanasDias: No hay conexion");
    }
      
     void cargarCampanasOpciones() {
        Hashtable tablaaux=new Hashtable(100,50);
        int con=0;
        String query="SELECT DISTINCT B.campana,B.codigo_micros,B.descripcion,B.codigo_micros_tamano,orden FROM [Campanas] A,campanas_opciones B ";
        query=query+" where inicio<=getdate() and fin>=getdate() and A.campana=B.campana and B.fechainicio<=getdate() and B.fechafin>=getdate() order by campana,orden";
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            jdbc.ejecutarQuery(query);
            
            Vector opciones=new Vector(10,5);
            String campanterior="";
            boolean primero=true;
            String[] aux=new String[2];
            int cuantos=0;
            while (jdbc.leerSiguienteEnQuery()==1) {
                cuantos++;
                if (primero) {
                    campanterior=jdbc.darColumnaQuery("campana");
                    primero=false;
                }
                String codcamp=jdbc.darColumnaQuery("campana");
                String codigomicros=jdbc.darColumnaQuery("codigo_micros");
                String codigomicrosnivel=jdbc.darColumnaQuery("codigo_micros_tamano");
                String descripcion=jdbc.darColumnaQuery("descripcion");
                aux=new String[3];
                aux[0]=codigomicros;
                aux[1]=descripcion;
                aux[2]=codigomicrosnivel;
                if (campanterior.equals(codcamp)) {
                  opciones.addElement(aux);
                } else {
                   tablaaux.put(campanterior, opciones);
                   campanterior=codcamp;
                   opciones=new Vector(10,5);
                   opciones.addElement(aux);
                }              
            }
            if (cuantos>0) {
               tablaaux.put(campanterior, opciones);
            }
            pool.cerrarConexion(con);
            synchronized (campanasopciones) {
                campanasopciones=tablaaux;
            }
        }
        else System.out.println("Campanas: cargarCampanasOpciones: No hay conexion");
    }
     
     boolean validarDia(String campana) {
         boolean valida=true;
         
         if (campanasdias!=null) {
             int [] dias=(int [])campanasdias.get(campana);
             if (dias!=null) {
                  Calendar ahora = Calendar.getInstance();
                  if (dias[ahora.get(Calendar.DAY_OF_WEEK)-1]==0)
                      valida=false;
             }
         }
         
         return valida;
     }
     
     public void run() 
	{
		tareas.sumar();
		terminarMgr=false;
		if (log!=null) log.mensaje("Campanas Start");
		
		while (terminarMgr==false) {
                    try {
                            sleep(3600000);
                    } catch (Exception e) {};
                    forzarCarga();
                    cargarVersionIsl();
                    cargarTiendas();
		}
		
		if (log!=null) log.mensaje("Campanas End");
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
			log.escribir(e);
		}
	}
    Vector darOpcionesCampana(String campana) {
        Vector opciones=null;
        if (campanasopciones!=null) {
            opciones=(Vector)(campanasopciones.get(campana));
        }
        return opciones;
    }
    public int darCantCampanas() {
        int aux=0;
        if (campanas!=null) aux=campanas.size();
        return aux;
    }
    
    public int darCantCampanasCodigos() {
        int aux=0;
        if (campanascodigos!=null) aux=campanascodigos.size();
        return aux;
    }
    
    public int darCantCampanasOpciones() {
        int aux=0;
        if (campanasopciones!=null) aux=campanasopciones.size();
        return aux;
    }
    
    static Hashtable campanas=null;
    static Hashtable campanascodigos=null;
    static Hashtable campanasopciones=null;
    static Hashtable campanasopcionesdias=null;
    static Hashtable campanasdias=null;
    static ConnectionPool pool=null;
    boolean terminarMgr;
    boolean yatermino=false;
    Tareas tareas=null;
    Log log=null;
    Hashtable versionisl=null;
    static String []campanasOffline=null;
    static int versioncampanas=0;
    static Hashtable tiendas=null;
    boolean chile=false;
    boolean colombia=false;
}
