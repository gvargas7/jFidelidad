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
import java.util.Date;

public class ConexionMonitor extends Thread {

	public ConexionMonitor(ConnectionPool elPool,Tareas lasTareas,Log elLog,jServidor elServer,boolean eschile,boolean escol) {
		pool=elPool;
        serverMaestro=elServer;
		tareas=lasTareas;
		log=elLog;
		yatermino=false;
        colombia=escol;
        chile=eschile;
	};
	
	public void run() 
	{
		tareas.sumar();
		terminarMgr=false;
        Vector dest=cargarDestinatarios();
        String informe="";

        if (log!=null) log.mensaje("ConexionMonitor Start");
        MailUtil mail=new MailUtil(gMailServer);

        mail.mandarMailMultipleConUser( dest, "Fidelidad","INICIO SISTEMA" , false, "", "");
        //mail.mandarMailMultiple2("dispositivo@alsea.com.ar", dest, "Fidelidad","INICIO SISTEMA" , false, "", "");
        long tSobrecargaI=0;
        long tSobrecargaF=0;
        boolean enSobrecarga=false;
		while (terminarMgr==false) {
			try {
                sleep(60000);
                if (pool.cuantasEnUso>9) {
                    if (!enSobrecarga) {
                        enSobrecarga=true;
                        tSobrecargaI=System.currentTimeMillis();
                    }
                    tSobrecargaF=System.currentTimeMillis();
                    if (((tSobrecargaF-tSobrecargaI)/1000)>110) {
                        informe="Sobrecarga de uso: Conexiones en uso "+pool.cuantasEnUso+" de "+pool.tamanoPool+"  max: "+pool.maxEnUso+"  Tareas: "+tareas.cantthreads+"  T Sobrecarga: "+(tSobrecargaF-tSobrecargaI)/1000+" seg\r\n";
                        informe=informe+"CAMBIO DE POOL";
                        System.out.println("\r\n\r\n"+informe+"\r\n\r\n");
                        log.mensaje("\r\n\r\n"+informe+"\r\n\r\n");
                        serverMaestro.cambiarPool();
                    } else {
                        informe="Conexiones en uso "+pool.cuantasEnUso+" de "+pool.tamanoPool+"  max: "+pool.maxEnUso+"  Tareas: "+tareas.cantthreads+"  T Sobrecarga: "+(tSobrecargaF-tSobrecargaI)/1000+" seg";
                        System.out.println("\r\n\r\n"+informe+"\r\n\r\n");
                        log.mensaje("\r\n\r\n"+informe+"\r\n\r\n");
                    }
                    mail.mandarMailMultipleConUser( dest, "Fidelidad",informe , false, "", "");
                    //mail.mandarMailMultiple2("dispositivo@alsea.com.ar", dest, "Fidelidad",informe , false, "", "");

                }
                else enSobrecarga=false;
            } catch (Exception e) {};

        long t=System.currentTimeMillis();
                    if (log!=null) log.mensaje("ConexionMonitor Comienzo chequeo: "+t+" Conexiones: "+pool.conexionesEnUso());
        pool.cerrarConexionesColgadas(30000);
                    if (log!=null) log.mensaje("ConexionMonitor Comienzo Reanima Conexiones: "+t+" Conexiones: "+pool.conexionesEnUso());
        pool.reanimarConexiones(180000);
        if (log!=null) log.mensaje("ConexionMonitor Fin chequeo. "+t+" Conexiones: "+pool.conexionesEnUso());
		}
		
    if (log!=null) log.mensaje("ConexionMonitor End");
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
	
	public synchronized void nuevoPool(ConnectionPool elPool) {
		pool=elPool;
	}
	
	public synchronized boolean termino()
	{
		return yatermino;
	}
        
        Vector cargarDestinatarios() {
            Vector aux=new Vector(5,2);
            String query="SELECT [mail] FROM [MailsAlarma] where tipo='INICIO'";
            int con=0;
            try {
               if ((con=pool.pedirConexion())>=0) {
                   JdbcMgr jdbc=pool.darJdbc(con);
                   
                   jdbc.ejecutarQuery(query);
                   
                    while (jdbc.leerSiguienteEnQuery()==1) {
                        aux.addElement(jdbc.darColumnaQuery("mail"));
                    }
                    
                
                 pool.cerrarConexion(con);
               }
               else {
                   pool.darLog().mensaje("cargarDestinatarios. No hay conexion");
               }
            } catch (Exception e) {
               pool.darLog().mensaje("cargarDestinatarios: "+e.toString());
               e.printStackTrace();
           }
           return aux;
        }
	
	boolean terminarMgr;
	boolean yatermino=false;
	ConnectionPool pool=null;
	Tareas tareas=null;
	Log log=null;
        String gMailServer="192.168.30.203";
        jServidor serverMaestro=null;
        boolean colombia=false;
        boolean chile=false;
}