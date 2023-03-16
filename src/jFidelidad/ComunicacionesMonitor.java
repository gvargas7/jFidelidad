/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Hashtable;
/**
 *
 * @author rhirsch
 */
public class ComunicacionesMonitor extends Thread{
    public ComunicacionesMonitor(ConnectionPool elPool,Tareas lasTareas) {
            pool=elPool;
            tareas=lasTareas;
            log=elPool.darLog();
            yatermino=false;
            if (gComunicaciones==null) {
                gComunicaciones=new Hashtable(3,1);
            }
	};
	
	public void run() 
	{
		tareas.sumar();
		terminarMgr=false;
                if (log!=null) log.mensaje("ComunicacionesMonitor Start");
                long tant=System.currentTimeMillis()/1000;
                long seg=0;
                

		while (terminarMgr==false) {
		
		
                    try {
                        leerUltimaComunicacion();
                        sleep(300000);
                        
                    } catch (Exception e) {};

                }
		
		if (log!=null) log.mensaje("ComunicacionesMonitor End");
		yatermino=true;
		
		tareas.restar();
	}
        
       
        
        synchronized void leerUltimaComunicacion() {           
           String query="SELECT max(id) id ,([marca]),[comunicacion]  FROM [Comunicaciones] where activa=1 and datediff(day,fecha,getdate())<2 group by marca,comunicacion,fecha order by marca,fecha desc";
           int con;
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                String marca="";
                String ultmarca="";
                Hashtable comunicacionesaux=new Hashtable(3,1);
                while (jdbc.leerSiguienteEnQuery()==1) {
                    marca=jdbc.darColumnaQuery("marca");
                    if (!marca.equals(ultmarca)) {
                        String [] datos=new String[2];
                        datos[0]=jdbc.darColumnaQuery("id");                   
                        datos[1]=jdbc.darColumnaQuery("comunicacion");
                        comunicacionesaux.put(marca,datos);
                        ultmarca=marca;
                        log.mensaje("Comunicacion: "+datos[0]+" Marca: "+marca+" : "+datos[1]);
                    }
                }
                pool.cerrarConexion(con);
                synchronized (gComunicaciones) {
                    gComunicaciones=comunicacionesaux;
                }
            };
        };
         
        int darIdComunicacion(String marca) {
            int res=0;
            if (gComunicaciones!=null) {
                String []aux=(String [])gComunicaciones.get(marca);
                if (aux!=null) {
                    try {
                        res=Integer.parseInt(aux[0]);
                    } catch (Exception e) {};
                }
            }
            
            return res;
        }
        
        String darComunicacion(String marca) {
            String res="";
            if (gComunicaciones!=null) {
                String []aux=(String [])gComunicaciones.get(marca);
                if (aux!=null) {
                    res=aux[1];
                }
            }
            
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
	
	public synchronized void cambiarPool(ConnectionPool elPool) {
		pool=elPool;
                log=elPool.darLog();
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
        Hashtable gComunicaciones=null;

}