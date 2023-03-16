/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

import java.io.*; 
import java.util.Date;

public class Log {
	public Log (String pathLog,String nombre,boolean conpantalla)
	{
            pantalla=conpantalla;
            if (out==null) {
			//try {
					//out = new FileOutputStream("/Apache/logpuzzle/logpuzzle"+System.currentTimeMillis()+".txt");
					nombreorig=nombre;
					pathorig=pathLog;
                                        crearLog();
                                        //pathlog=pathLog+nombre+System.currentTimeMillis()+".txt";
                                      //  pathlog=pathLog+nombre+"-1.txt";
					//out = new FileOutputStream(pathlog);
					//out = new FileOutputStream("/user/local/apache/logpuzzle/logpuzzle"+System.currentTimeMillis()+".txt");
					//out = new FileOutputStream("/usr/local/apache/servlets/tarjetas/logpuzzle.txt");
					
					//os=new BufferedOutputStream(out,100000);
					//pw=new PrintWriter(out);
					//mensaje("Comienzo Log:"+pathlog);
					//nombreorig=nombre;
					//pathorig=pathLog;
	     	//}
	     	//catch (IOException e) {
               //     e.printStackTrace();
	     	//}
	   }

	};
	
	public synchronized void crearLog() {
		try {
			cerrar();
                        rotarLogs();
			//pathlog=pathorig+"/"+nombreorig+System.currentTimeMillis()+".txt";
                        pathlog=pathorig+"/"+nombreorig+"-1.txt";
			out = new FileOutputStream(pathlog);
			pw=new PrintWriter(out);
			mensaje("Comienzo Log:"+pathlog);
		}
		catch (IOException e) {
	     	e.printStackTrace();
	  }
	}
	
        synchronized void rotarLogs() {
            int cantlogs=15;
            String pathaux=pathorig+"/"+nombreorig;
            try {
               File aborrar=new File(pathaux+"-"+cantlogs+".txt");
               aborrar.delete();
            } catch (Exception e) {
                
            }
            for (int j=cantlogs-1;j>0;j--) {
                try {
                    File aux1=new File(pathaux+"-"+j+".txt");
                    File aux2=new File(pathaux+"-"+(j+1)+".txt");
                    if(aux1.exists()) aux1.renameTo(aux2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
	public synchronized void verificarLargo() {
		cuantos++;
		if (cuantos>100) {
			cuantos=0;
	
			File arch=new File(pathlog);
			if (arch!=null) {
				if (arch.length()>10000000) {
					crearLog();
				}
			}
		}
	}
	
	public synchronized void escribir(Exception e) {
		//abrir();
		if (pw!=null) {
			Date hora=new Date();
			pw.println(hora.toString()+" - ");
			
			e.printStackTrace(pw);
			pw.flush();
			if (pantalla) System.out.println(hora.toString()+" - " + e.toString());
			verificarLargo();
		}
		//cerrar();
	}
	
	public synchronized void mensaje(String texto) {
		//abrir();
		if (pw!=null) {
			Date hora=new Date();
			pw.println(hora.toString()+" - " + texto);
			pw.flush();
			verificarLargo();
                        if (pantalla) System.out.println(hora.toString()+" - " + texto);
			/*cuantos++;
			if (cuantos>5) {
				cuantos=0;
				pw.flush();
			}*/
		}
		//cerrar();
	}
	
	public void cerrar() {
		try {
			if (out!=null) {
				if (pw!=null) pw.flush();
				if (pw!=null) pw.close();
				//os.flush();
				//os.close();
				out.flush();
				out.close();
				out=null;
			}
		} catch (Exception e) {};
	}
	
	public String darNombreLog() {
		return pathlog;
	}
	
	public String leerLog() {
		//log.cerrar();
		String ellog="Error";
		
		
		byte [] buffer=null;
		File file=new File(pathlog);
		long largo=file.length();
		
		try {
			FileInputStream archivo=new FileInputStream(pathlog);
			if (archivo!=null) {
				int leidos;
				buffer=new byte[(int)largo];
				
				leidos=archivo.read(buffer);
				
				archivo.close();
				
				ellog=new String(buffer);
			}
		} catch (Exception e) {
			e.printStackTrace();
			escribir(e);
			/*if (log!=null) {
				log.mensaje("Error leerBufferArchivo("+path+")");
				log.escribir(e);
			}*/
		}
		
		return  ellog;
	}
	
	
	private FileOutputStream out=null;
	private BufferedOutputStream os=null;
	private PrintWriter pw=null;
	private int cuantos=0;
	private String pathlog="";
	private String nombreorig="";
	private String pathorig="";
        private boolean pantalla=false;
}