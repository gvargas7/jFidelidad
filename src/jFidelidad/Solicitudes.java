/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class Solicitudes {
    public void Solicitudes() {
		
	}

	public synchronized void Solicitud() {
		cantsolicitudes++;
	}
	
	public synchronized void Ping() {
		cantping++;
	}
        
        public synchronized void Voucher() {
		cantvouchers++;
	}
        
        public synchronized void Cupon() {
		cantcupones++;
	}
	
        public synchronized void CuponHab() {
		cantcuponhab++;
	}
        public synchronized void CuponOpcion() {
		cantcuponopcion++;
	}
        public synchronized void CVAlta() {
		cantcvalta++;
	}
        
        public synchronized void CVRedimir() {
		cantcvredimir++;
	}
         
        public synchronized void CVFreq() {
		cantcvfreq++;
	}
        
        public synchronized void Descuentos() {
		cantdescuentos++;
	}
         
        public synchronized void Offline() {
		cantoffline++;
	}
        
        public synchronized void CodOffline() {
		cantcodoffline++;
	}
        
        public synchronized void Credito() {
		cantcredito++;
	}
        
        public void ponerCero() {
            cantsolicitudes=0;
            cantping=0;
            cantvouchers=0;
            cantcupones=0;
            cantcuponhab=0;
            cantcuponopcion=0;
            cantcvalta=0;
            cantcvredimir=0;
            cantcvfreq=0;
            cantdescuentos=0;
            cantoffline=0;
            cantcodoffline=0;
            cantcredito=0;
        }
        
	public static int cantsolicitudes=0;
        public static int cantping=0;
        public static int cantvouchers=0;
        public static int cantcupones=0;
        public static int cantcuponhab=0;
        public static int cantcuponopcion=0;
        public static int cantcvalta=0;
        public static int cantcvredimir=0;
        public static int cantcvfreq=0;
        public static int cantdescuentos=0;
        public static int cantoffline=0;
        public static int cantcodoffline=0;
        public static int cantcredito=0;
        
        
	
}
