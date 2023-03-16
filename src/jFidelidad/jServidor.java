/*
 * jServidor.java
 *
 * Created on 25 de junio de 2008, 17:38
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jFidelidad;
import java.net.*;
import java.io.*;
/**
 *
 * @author rhirsch
 */
public class jServidor extends Thread {
    
    /** Creates a new instance of jServidor */
	
	public jServidor(boolean eschile,boolean escol,boolean modotest) {
		servidor=null;
                test=modotest;
                gPropiedades=new Propiedades(modotest);
                
                gBd=gPropiedades.gBd;
                gUser=gPropiedades.gUser;
                gPass=gPropiedades.gPass;
                gCantConexiones=gPropiedades.gCantConexiones;
                
                /*
                gBd="jdbc:sqlserver://172.31.1.14;databaseName=Fidelidad";
                //gBd="jdbc:sqlserver://172.31.1.185;databaseName=Fidelidad";
                if (modotest) {
                    gBd="jdbc:sqlserver://172.31.1.240;databaseName=Fidelidad";
                    if (eschile)  gBd="jdbc:sqlserver://192.168.198.129;databaseName=Fidelidad-CL_TEST";
                } else
                    if (eschile) gBd="jdbc:sqlserver://192.168.198.129;databaseName=Fidelidad-CL";
                        else if (escol) gBd="jdbc:sqlserver://192.168.192.122;databaseName=Fidelidad";
                */
                chile=eschile;
                colombia=escol;
	};
	
	public void run() {
		if (arrancarServidor()==1) {
                    inicializarClases();
                    while (fin==0) {
                            Socket clienteSocket=null;
                            try {
                               clienteSocket=servidor.accept();
                               //System.out.println("LLego Algo");
                               if (!enCambioPool) {
                                System.out.println("Llego: "+clienteSocket.getInetAddress()+" Puerto: "+clienteSocket.getPort());
                                ClienteThread unCliente=new ClienteThread(clienteSocket,gPool,gCampanas,chile,colombia,gVouchersMonitor,gSolicitudes,gTareas,gCuponesMonitor,gComunicacionesMonitor);
                                 if (unCliente!=null) {
                                     
                                     unCliente.start();
                                 }
                               };

                            } catch (IOException e) {
                                    if (fin==0) {
                                       e.printStackTrace();
                                    }
                            }
                    }
			if (fin==0) {
				try {
					servidor.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			yatermino=true;
		}
	}
	
	int arrancarServidor() {
		int arranco=0;
		try {
                        servidor=new ServerSocket(gPropiedades.gPuerto);
			//if (chile && test) servidor=new ServerSocket(5110); //5110
                        //else servidor=new ServerSocket(5010); //5010
			arranco=1;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return arranco;
	}
	
	void terminar() {
		fin=1;
		try {
                    servidor.close();
                    gRecursos.fin();
                    gMonitor.fin();
                    gCampanas.fin();
                    gSolicitudesMonitor.fin();
                    gVouchersMonitor.fin();
                    gCuponesMonitor.fin();
                    gComunicacionesMonitor.fin();
                    if (gEmpleados!=null) gEmpleados.fin();
                    gPool.cerrarTodasLasConexion();
                    gLog.cerrar();
		} catch (Exception e) {
                    //e.printStackTrace();
		}
		yatermino=true;
	}
	
	boolean realmenteTermino() {
		return yatermino;
	}
	void inicializarClases() {
            gTareas=new Tareas();
            gSolicitudes=new Solicitudes();
            
            gLog=new Log("Log/","logFidelidad",true);
            gPool=new ConnectionPool(gCantConexiones,gBd,gUser,gPass,3,5000,gLog,JdbcMgr.Kmsql);
           
            inicializarConexionMonitor();
        }
        
        void cambiarPool() {
            enCambioPool=true;
            gLog.mensaje("****** CAMBIO DE POOL ******");
            System.out.println("****** CAMBIO DE POOL ******");
            gPool=new ConnectionPool(gCantConexiones,gBd,gUser,gPass,3,5000,gLog,JdbcMgr.Kmsql);
            gMonitor.pool=gPool;
            gCampanas.cambiarPool(gPool);
            gRecursos.pool=gPool;
            gSolicitudesMonitor.pool=gPool;
            gEmpleados.pool=gPool;
            gVouchersMonitor.pool=gPool;
            gCuponesMonitor.pool=gPool;
            gComunicacionesMonitor.cambiarPool(gPool);
            enCambioPool=false;
            gLog.mensaje("****** FIN CAMBIO DE POOL ******");
            System.out.println("****** FIN CAMBIO DE POOL ******");
        }
        
        void inicializarConexionMonitor() {
            	gMonitor=new ConexionMonitor(gPool,gTareas,gLog,this,chile,colombia);
                try {
                    if (gMonitor!=null) gMonitor.start();
                    else gLog.mensaje("inicializarConexionMonitor: gMonitor Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                gCampanas=new Campanas(gPool,gTareas,gLog,chile,colombia);
                try {
                    if (gCampanas!=null) gCampanas.start();
                    else gLog.mensaje("inicializarConexionMonitor: gCampanas Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gCuponesMonitor=new CuponesMonitor(gPool,gTareas,gLog);
                try {
                    if ((gCuponesMonitor!=null) ) gCuponesMonitor.start();
                    else gLog.mensaje("inicializarConexionMonitor: gCuponesMonitor Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gRecursos=new Recursos(gTareas,gCampanas,gPool,gCuponesMonitor);
                try {
                    if (gRecursos!=null && !test) gRecursos.start();
                    else gLog.mensaje("inicializarConexionMonitor: gRecursos Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                gSolicitudesMonitor=new SolicitudesMonitor(gSolicitudes,gPool);
                try {
                    if (gSolicitudesMonitor!=null && !test) gSolicitudesMonitor.start();
                    else gLog.mensaje("inicializarConexionMonitor: gSolicitudesMonitor Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                gComunicacionesMonitor=new ComunicacionesMonitor(gPool,gTareas);
                try {
                    if (gComunicacionesMonitor!=null) gComunicacionesMonitor.start();
                    else gLog.mensaje("inicializarConexionMonitor: gComunicacionesMonitor Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
               // if (chile) {
                gEmpleados=new Empleados(gPool,gTareas,chile,colombia);
                try {
                    if ((gEmpleados!=null) ) gEmpleados.start(); //&& !test
                    //if ((gEmpleados!=null)) gEmpleados.start();
                    else gLog.mensaje("inicializarConexionMonitor: gEmpleados Null");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                if (!test) {
                    gVouchersMonitor=new VouchersMonitor(gPool,gTareas,gLog);
                
                    try {
                        if ((gVouchersMonitor!=null)) gVouchersMonitor.start();
                        else gLog.mensaje("inicializarConexionMonitor: gVouchersMonitor Null");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                gServidorWeb=new jServidorWeb(gPool,gCampanas,chile,colombia,gVouchersMonitor,gSolicitudes,gTareas,gCuponesMonitor,gComunicacionesMonitor);
                try {
                    if (gServidorWeb!=null) gServidorWeb.start();
                } catch (Exception e) {
                        e.printStackTrace();
                }
                //}
        }
        
	ServerSocket servidor;
	int fin;
	boolean yatermino=false;
        Log gLog=null;
        ConnectionPool gPool=null;
        Tareas gTareas=null;
        ConexionMonitor gMonitor=null;
        String gBd="jdbc:sqlserver://172.31.1.14";
        String gUser="fidelidad";
        String gPass="f1d3l1d4d$";
        jServidorWeb gServidorWeb=null;
        Campanas gCampanas=null;
        Recursos gRecursos=null;
        Empleados gEmpleados=null;
        VouchersMonitor gVouchersMonitor=null;
        CuponesMonitor gCuponesMonitor=null;
        boolean chile=false;
        boolean colombia=false;
        Solicitudes gSolicitudes=null;
        SolicitudesMonitor gSolicitudesMonitor=null;
        ComunicacionesMonitor gComunicacionesMonitor=null;
        int gCantConexiones=20;
        boolean enCambioPool=false;
        boolean test=false;
        Propiedades gPropiedades=null;
}
