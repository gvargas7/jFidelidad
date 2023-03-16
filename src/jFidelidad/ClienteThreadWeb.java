/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.Runtime;


public class ClienteThreadWeb extends Thread {

	ClienteThreadWeb(Socket elSocket,ConnectionPool elpool,Campanas lascampanas,boolean esChile,boolean esCol,VouchersMonitor elVouchersMonitor,Solicitudes lasSolicitudes,Tareas lasTareas,CuponesMonitor elCuponesMonitor,ComunicacionesMonitor elComunicacionesMonitor) {
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
        public void run() {
            
            cuantosThreads++;
            tareas.sumar();
            int esteThread=cuantosThreads;
            System.out.println("Thread Web: "+esteThread+" - Levanto "+(new Date()).toString()+" Threads "+ cuantosThreads+" Con: "+pool.cuantasEnUso+" IP:"+socket.getInetAddress().getHostAddress());

            try {
                socket.setSoTimeout(5000); 
                BufferedReader elMensaje=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 
                if (elMensaje!=null) {


                    int cuantos=0;
                    int car=0;
                    String mensaje=elMensaje.readLine();

                    System.out.println("Thread Web: "+esteThread+" - "+" Con: "+pool.cuantasEnUso+" Fin mensaje recibido: "+mensaje+" Cuantos="+cuantos);

                    BufferedWriter respuesta=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                    String comando="";
                    if (mensaje!=null ) {
                        
                    }
                    else {
                        
                    }
                    
                     String elcomando=obtenerComando(mensaje);
                     
                    Distribuidor distribuidor=new Distribuidor(null,respuesta,null,cuantos,socket.getInetAddress().getHostAddress(),pool,campanas,chile,colombia,vouchersMonitor,solicitudes,cuponesMonitor,comunicacionesMonitor,true);


                    String datos=distribuidor.procesarMensaje(esteThread,elcomando);
                   
                    
                    String auxsalida=datos;
                    if ((datos!=null) && (datos.length()>80)) {
                        auxsalida=datos.substring(0,80);
                        //auxsalida=datos.substring(datos.length()-20,datos.length());
                    }
                    System.out.println("Thread Web: "+esteThread+" - Devolviendo mensaje: "+datos.length()+" "+auxsalida);
                    
                    Date date = new Date();
                    String start = "HTTP/1.1 200 OK\r\n";
                    String header = "Date: "+date.toString()+"\r\n";
                    header+= "Content-Type: text/plain\r\n";
                    header+= "Content-length: "+datos.length()+"\r\n";
                    header+="\r\n";
                    datos=start+header+datos;
                    
                    
                   // respuesta.write("HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n",0,"HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n".length());
                    respuesta.write(datos,0,datos.length());  

                    respuesta.flush();
                    respuesta.close();

                    System.out.println("Thread Web: "+esteThread+" - Fin "+cuantosThreads);
                }
            } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Thread Web: "+esteThread+" - Error: "+e.toString());
            }
            try {

                    socket.close();
            } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Thread Web: "+esteThread+" - Error: "+e.toString());
            }

            cuantosThreads--;
            tareas.restar();
            terminar();
	}
        
	String obtenerComando(String datos) {
            //String[] comando={"",""};
            String comando="";
            if (datos!=null) {
                datos=datos.toUpperCase();
                int aux=datos.indexOf("/");
                int aux2=datos.indexOf(" HTTP",aux);
                if (aux2>aux) {
                    datos=reemplazarTexto(datos,"/","|");
                    comando=datos.substring(aux+1,aux2).trim();
                    //aux=datos.indexOf("=");
                   // comando[0]=datos.substring(0,aux);
                   // comando[1]=datos.substring(aux+1,datos.length()).trim();
                }
            };
            return comando;
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
}