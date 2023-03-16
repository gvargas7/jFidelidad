/*
 * ClienteThread.java
 *
 * Created on 25 de junio de 2008, 17:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jFidelidad;
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Runtime;


public class ClienteThread extends Thread {

	ClienteThread(Socket elSocket,ConnectionPool elpool,Campanas lascampanas,boolean esChile,boolean esCol,VouchersMonitor elVouchersMonitor,Solicitudes lasSolicitudes,Tareas lasTareas,CuponesMonitor elCuponesMonitor,ComunicacionesMonitor elComunicacionesMonitor) {
		        socket=elSocket;
                tareas=lasTareas;
                pool=elpool;
		        fin=0;
                campanas=lascampanas;
                chile=esChile;
                colombia=esCol;
                vouchersMonitor=elVouchersMonitor;
                cuponesMonitor=elCuponesMonitor;
                solicitudes=lasSolicitudes;
                comunicacionesMonitor=elComunicacionesMonitor;
	}
        public void run() {
            gClientesTareas=new Tareas();
            cuantosThreads++;
            tareas.sumar();
            int esteThread=cuantosThreads;
            System.out.println("Thread: "+esteThread+" - Levanto "+(new Date()).toString()+" Threads "+ cuantosThreads+" Con: "+pool.cuantasEnUso+" IP:"+socket.getInetAddress().getHostAddress());

            try {
                socket.setSoTimeout(5000); 
                BufferedReader elMensaje=new BufferedReader(new InputStreamReader(socket.getInputStream()));
               // Thread.sleep(5000);
                BufferedWriter respuesta=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                boolean fintotal=false;
                if (elMensaje!=null) {
                    while (!fintotal) {
                        char[] mensaje=new char[1000];

                        int cuantos=0;
                        int car=0;
                        try {
                            while (((car=elMensaje.read())!=4) && (car!=-1) && (cuantos<1000)){
                                mensaje[cuantos]=(char)car;
                                cuantos++;
                            }
                        } catch (Exception e2) {};
                        if (car==4) mensaje[cuantos]=(char)car;
                        cuantos++;
                        if (cuantos>1) {
                            System.out.println("Thread: "+gClientesTareas.cantthreads+" - "+" Con: "+pool.cuantasEnUso+" Fin mensaje recibido: "+new String(mensaje,0,cuantos)+" Cuantos="+cuantos);


                            Distribuidor distribuidor=new Distribuidor(gClientesTareas,respuesta,mensaje,cuantos,socket.getInetAddress().getHostAddress(),pool,campanas,chile,colombia,vouchersMonitor,solicitudes,cuponesMonitor,comunicacionesMonitor,false);
                            if (distribuidor!=null) {
                                gClientesTareas.sumar();
                                distribuidor.start();
                            }
                        };
                        if (gClientesTareas.cantthreads<=0) fintotal=true;
                    };
                    /*
                     String datos=distribuidor.procesarMensaje(esteThread); 
                     String auxsalida=datos;
                    if ((datos!=null) && (datos.length()>80)) {
                        auxsalida=datos.substring(0,80);
                        //auxsalida=datos.substring(datos.length()-20,datos.length());
                    }
                    System.out.println("Thread: "+esteThread+" - Devolviendo mensaje: "+datos.length()+" "+auxsalida);
                    respuesta.write(datos,0,datos.length());  

                    respuesta.flush();
                    respuesta.close();

                    System.out.println("Thread: "+esteThread+" - Fin "+cuantosThreads);
                    */
                    respuesta.flush();
                    respuesta.close();
                }
            } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Thread: "+esteThread+" - Error: "+e.toString());
            }
            try {

                    socket.close();
                    System.out.println("Thread: "+esteThread+" - FIN TOTAL");
            } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Thread: "+esteThread+" - Error: "+e.toString());
            }

            cuantosThreads--;
            tareas.restar();
            terminar();
	}
        
        
        public void run2() {
            
            cuantosThreads++;
            tareas.sumar();
            int esteThread=cuantosThreads;
            System.out.println("Thread: "+esteThread+" - Levanto "+(new Date()).toString()+" Threads "+ cuantosThreads+" Con: "+pool.cuantasEnUso+" IP:"+socket.getInetAddress().getHostAddress());

            try {
                socket.setSoTimeout(5000); 
                BufferedReader elMensaje=new BufferedReader(new InputStreamReader(socket.getInputStream()));
               // Thread.sleep(5000);
                if (elMensaje!=null) {

                    char[] mensaje=new char[1000];

                    int cuantos=0;
                    int car=0;
                    while (((car=elMensaje.read())!=4) && (car!=-1) && (cuantos<1000)){
                        mensaje[cuantos]=(char)car;
                        cuantos++;
                    }

                    if (car==4) mensaje[cuantos]=(char)car;
                    cuantos++;
                    System.out.println("Thread: "+esteThread+" - "+" Con: "+pool.cuantasEnUso+" Fin mensaje recibido: "+new String(mensaje,0,cuantos)+" Cuantos="+cuantos);

                    BufferedWriter respuesta=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    Distribuidor distribuidor=new Distribuidor(gClientesTareas,respuesta,mensaje,cuantos,socket.getInetAddress().getHostAddress(),pool,campanas,chile,colombia,vouchersMonitor,solicitudes,cuponesMonitor,comunicacionesMonitor,false);
                    String datos=distribuidor.procesarMensaje(esteThread,null);
                    
                    String auxsalida=datos;
                    if ((datos!=null) && (datos.length()>80)) {
                        auxsalida=datos.substring(0,80);
                        //auxsalida=datos.substring(datos.length()-20,datos.length());
                    }
                    System.out.println("Thread: "+esteThread+" - Devolviendo mensaje: "+datos.length()+" "+auxsalida);
                    respuesta.write(datos,0,datos.length());  

                    respuesta.flush();
                    respuesta.close();

                    System.out.println("Thread: "+esteThread+" - Fin "+cuantosThreads);
                }
            } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Thread: "+esteThread+" - Error: "+e.toString());
            }
            try {

                    socket.close();
            } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Thread: "+esteThread+" - Error: "+e.toString());
            }

            cuantosThreads--;
            tareas.restar();
            terminar();
	}
        
	
	
	void procesarComando(String codigo) {
          try {

           String comando="";
           if (codigo.equals("13")) comando="/bin/cp -f /etc/host1 /etc/host";
           else if (codigo.equals("17")) comando="/bin/cp -f /etc/host2 /etc/host";
           Runtime runtime=Runtime.getRuntime();
           Process buscar=runtime.exec(comando);

           int exitVal = buscar.waitFor();

           System.out.println("Fin "+exitVal);
          } catch (Exception e) {
              e.printStackTrace();
          }
        }
	
	void terminar() {
		fin=1;
	}

	int yaTermino() {
		return fin;
	}
	
	Socket socket;
	int fin=0;
	static int cuantosThreads=0;
        ConnectionPool pool=null;
        Campanas campanas=null;
        boolean chile=false;
        boolean colombia=false;
        VouchersMonitor vouchersMonitor=null;
        CuponesMonitor cuponesMonitor=null;
        Solicitudes solicitudes=null;
        Tareas tareas=null;
        ComunicacionesMonitor comunicacionesMonitor=null;
        Tareas gClientesTareas=null;

}
