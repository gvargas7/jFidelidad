/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
import java.util.Vector;

public class ConnectionPool {

	public ConnectionPool(String elServer,String elUser,String elPassword,int losReintentos,int elTiempo,Log elLog,int elTipoServer) {
		if (pool==null) {
			tamanoPool=1;
			server=elServer;
			user=elUser;
			password=elPassword;
			log=elLog;
			tipoServer=elTipoServer;
			inicializarPool(server,user,password);
			reintentos=losReintentos;
			tiempo=elTiempo;
			
		};
	}
	
	public ConnectionPool(int elTamanoPool,String elServer,String elUser,String elPassword,int losReintentos,int elTiempo,Log elLog,int elTipoServer) {
		if (pool==null) {
			tamanoPool=elTamanoPool;
			server=elServer;
			user=elUser;
			password=elPassword;
			log=elLog;
			tipoServer=elTipoServer;
			inicializarPool(server,user,password);
			reintentos=losReintentos;
			tiempo=elTiempo;
			
		};
	}

	  void inicializarPool(String server,String user,String password) {
		cuantasEnUso=0;
		maxEnUso=0;
		pool=new Vector(tamanoPool);
		enuso=new boolean[tamanoPool];
		tenuso=new long[tamanoPool];
	
		for (int i=0;i<tamanoPool;i++) {
			pool.addElement(new JdbcMgr(server,user,password,log,tipoServer));
			enuso[i]=false;
			tenuso[i]=System.currentTimeMillis();;
		};
	}

	public  void restart() {
		inicializarPool(server,user,password);
	}

	public  void cerrarPool() {
		
		cerrarTodasLasConexion();
		for (int i=0;i<tamanoPool;i++)  {
			((JdbcMgr)pool.elementAt(i)).cerrarConexiones();
		}
		
		
		pool=null;
	}
        
        
	public  synchronized int pedirConexion() {
		int idConexion=-1;
		int intentos=0;
		
		while ((idConexion==-1) && (intentos<reintentos)) {
			if (cuantasEnUso<tamanoPool) {
				int i=0;
                                ultEntregada=(ultEntregada+1) % tamanoPool;
				while ((i<tamanoPool) && (enuso[ultEntregada]==true)) {
					i++;
                                        ultEntregada=(ultEntregada+1) % tamanoPool;
				}
				
				if (i<tamanoPool) {
					//idConexion=i;
                                        idConexion=ultEntregada;
					enuso[idConexion]=true;
					tenuso[idConexion]=System.currentTimeMillis();
					enusoMas();
				}
				//cuantasEnUso++;
			}
			else {
                                log.mensaje("**** PEDIR CONEXION: REINTENTO: "+intentos);
                                System.out.println("**** PEDIR CONEXION: REINTENTO: "+intentos);
				intentos++;
				try { Thread.sleep(tiempo);}
				catch (Exception e){};
			}
                        
		}
		
		return idConexion;
	}
	
	public   void cerrarConexion(int cual,String dedonde) {
		try {
			darJdbc(cual).cerrarResultSet();
			
			restarConexion("cerrarConexion ("+cual+") en "+dedonde);
			enuso[cual]=false;
			tenuso[cual]=System.currentTimeMillis();
			
		}
		catch (Exception e) {
			log.mensaje("**** Exception en CerrarConexion ("+cual+")");
			log.escribir(e);
		}
		finally {
		
		}
	}
	
	public   void cerrarConexion(int cual) {
		try {
			darJdbc(cual).cerrarResultSet();
			
			restarConexion("cerrarConexion ("+cual+")");
			enuso[cual]=false;
			tenuso[cual]=System.currentTimeMillis();
			
		}
		catch (Exception e) {
			log.mensaje("**** Exception en CerrarConexion ("+cual+")");
			log.escribir(e);
		}
		finally {
		
		}
	}
	
	public  void cerrarTodasLasConexion() {
		for (int i=0;i<tamanoPool;i++) {
			if (darJdbc(i)!=null) darJdbc(i).cerrarResultSet();
			enuso[i]=false;
			tenuso[i]=System.currentTimeMillis();
			
		};
		cuantasEnUso=0;
	}
	
	public  void cerrarConexionesColgadas(long tiempo) {
		for (int i=0;i<tamanoPool;i++) {
			if(enuso[i]==true) {
				long anterior=tenuso[i];
				long actual=System.currentTimeMillis();
				if ((actual-anterior)>tiempo) {
					log.mensaje("Cierro conexion colgada("+i+") tant="+anterior+"  tact="+actual+"  dif="+(actual-anterior)+" Conexiones: "+conexionesEnUso());
					try {
						cerrarConexion(i);
					}
					catch (Exception e) {
						log.mensaje("Error en cerrarConexionesColgadas: "+e.toString()+" Conexiones: "+conexionesEnUso());
						restart();
					}
				}
			}
		}	
	}
	
	public  void reanimarConexiones(long tiempo) {
		for (int i=0;i<tamanoPool;i++) {
			if(enuso[i]==false) {
				long anterior=tenuso[i];
				long actual=System.currentTimeMillis();
				if ((actual-anterior)>tiempo) {
					enuso[i]=true;
					cuantasEnUso++;
					log.mensaje("Reanimar ("+i+")"+" Conexiones: "+conexionesEnUso());
					try {
						((JdbcMgr)pool.elementAt(i)).cerrarConexiones();
						((JdbcMgr)pool.elementAt(i)).obtenerConexion();
					}
					catch (Exception e) {
						log.mensaje("Error en reanimarConexion: "+e.toString()+" Conexiones: "+conexionesEnUso());
						restart();
					}
					tenuso[i]=System.currentTimeMillis();
					enuso[i]=false;
					//cuantasEnUso--;
					restarConexion("reanimarConexiones");
					log.mensaje("FIN Reanimar ("+i+")"+" Conexiones: "+conexionesEnUso());
				}
			}
		}	
	}
	
	 synchronized void restarConexion(String donde) {
		if (cuantasEnUso>0) enusoMenos();
		else {
			log.mensaje("ERROR: Trate de cerrar conexiones <0 en :"+donde);
		}
	}
	
	public synchronized JdbcMgr darJdbc(int cual) {
		JdbcMgr jdbc=null;
		if (cual<tamanoPool) {
			if (pool!=null)
				jdbc=(JdbcMgr)pool.elementAt(cual);
			else log.mensaje("POOL NULO");
		}
		return (jdbc);
	}
	
	public  int conexionesEnUso() {
		return cuantasEnUso;
	} 
	
	public synchronized int tamanoPool() {
		return tamanoPool;
	}
	
	synchronized void enusoMas() {
		cuantasEnUso++;
		if (cuantasEnUso>maxEnUso) maxEnUso=cuantasEnUso;
	}
	
	synchronized void enusoMenos() {
		cuantasEnUso--;
	}
	
	public synchronized int maxConexiones() {
		return maxEnUso;
	}
        
        Log darLog() {
            return log;
        }

	Vector pool=null;
	boolean[] enuso;
	long[] tenuso;
	int cuantasEnUso=0;
	int maxEnUso=0;
	int tamanoPool=0;
	int reintentos=0;
	int tiempo=0;
	String server="";
	String user="";
	String password="";
	Log log=null;
	int tipoServer=0;
        int ultEntregada=0;
	
}