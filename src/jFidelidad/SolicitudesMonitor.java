/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class SolicitudesMonitor extends Thread{
    public SolicitudesMonitor(Solicitudes lassolicitudes,ConnectionPool elpool) {
		solicitudes=lassolicitudes;
		pool=elpool;
		yatermino=false;
	};
	
	public void run() 
	{
		
		terminarMgr=false;
		System.out.println("Solicitudes Start");
		
		while (terminarMgr==false) {
		
		
			try {
				sleep(3600000);
			} catch (Exception e) {};
			registrarSolicitudes();
                        solicitudes.ponerCero();
		}
		
		System.out.println("Solicitudes End");
		yatermino=true;
		
	}
        
        void registrarSolicitudes() {
            try {
                int con=0;
                String query="INSERT INTO [Solicitudes]([fecha],[solicitudes],[ping],[cupon],[cuponhab],[cuponopc],[cvalta],[cvredimir],[cvfreq],[descuentos],[voucher],[offline],[cuponesoffline]) VALUES(";
                query=query+"getdate(),"+solicitudes.cantsolicitudes+","+solicitudes.cantping+","+solicitudes.cantcupones+","+solicitudes.cantcuponhab+","+solicitudes.cantcuponopcion+","+solicitudes.cantcvalta+","+solicitudes.cantcvredimir+","+solicitudes.cantcvfreq+","+solicitudes.cantdescuentos+","+solicitudes.cantvouchers+","+solicitudes.cantoffline+","+solicitudes.cantcodoffline+")";

                if ((con=pool.pedirConexion())>=0) {
                    JdbcMgr jdbc=pool.darJdbc(con);
                    jdbc.ejecutarUpdate(query);

                     pool.cerrarConexion(con);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
	public synchronized void fin()
	{
		try {
			this.interrupt();
			terminarMgr=true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public synchronized boolean termino()
	{
		return yatermino;
	}
	
	boolean terminarMgr;
	boolean yatermino=false;
	Solicitudes solicitudes=null;
        ConnectionPool pool=null;
}
