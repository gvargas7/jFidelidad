/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
import java.net.*;
import java.io.*;
/**
 *
 * @author rhirsch
 */
public class jServidorWeb extends Thread {
    
    /** Creates a new instance of jServidor */
	
	public jServidorWeb(ConnectionPool elpool,Campanas lascampanas,boolean esChile,boolean esCol,VouchersMonitor elVouchersMonitor,Solicitudes lasSolicitudes,Tareas lasTareas,CuponesMonitor elCuponesMonitor,ComunicacionesMonitor elComunicacionesMonitor) {
            servidor=null;
            gPool=elpool;
            chile=esChile;
            colombia=esCol;
            gCampanas=lascampanas;
            gVouchersMonitor=elVouchersMonitor;
            gSolicitudes=lasSolicitudes;
            gTareas=lasTareas;
            gCupones=elCuponesMonitor;
            gComunicaciones=elComunicacionesMonitor;
                
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
                                ClienteThreadWeb unCliente=new ClienteThreadWeb(clienteSocket,gPool,gCampanas,chile,colombia,gVouchersMonitor,gSolicitudes,gTareas,gCupones,gComunicaciones);
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
			if (chile && test) servidor=new ServerSocket(5180);
                        else servidor=new ServerSocket(5180);
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
                    
		} catch (Exception e) {
                    //e.printStackTrace();
		}
		yatermino=true;
	}
	
	boolean realmenteTermino() {
		return yatermino;
	}
	void inicializarClases() {
            
        }
        
        void cambiarPool() {
            
        }
        
        ConnectionPool gPool=null;
        Campanas gCampanas=null;
        VouchersMonitor gVouchersMonitor=null;
        Solicitudes gSolicitudes=null;
        Tareas gTareas=null;
        CuponesMonitor gCupones=null;
        ComunicacionesMonitor gComunicaciones=null;
        
	ServerSocket servidor;
	int fin;
	boolean yatermino=false;
        
        boolean chile=false;
        boolean colombia=false;

        boolean enCambioPool=false;
        boolean test=false;
}
