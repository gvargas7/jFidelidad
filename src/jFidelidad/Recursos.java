/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */


public class Recursos extends Thread {

	public Recursos(Tareas lasTareas,Campanas lascampanas,ConnectionPool elpool,CuponesMonitor elcupones) {
		campanas=lascampanas;
		tareas=lasTareas;
                pool=elpool;
                cuponesM=elcupones;
		yatermino=false;
	};
	
	public void run() 
	{
		
		terminarMgr=false;
		System.out.println("Recursos Start");
		registrarRecursos();
		while (terminarMgr==false) {
		
		
			try {
				sleep(240000);
			} catch (Exception e) {};
			registrarRecursos();
		}
		
		System.out.println("Recursos End");
		yatermino=true;
		
	}
        
        void registrarRecursos() {
            try {
                int con=0;
                String query="INSERT INTO [Recursos] ([fecha],[threads],[maxthreads],[conexiones],[maxconexiones],[campanas],[codigos],[inventencolados],[inventmaxencolados],[cuponesencolados],[cuponesmaxencolados],[webservicemaxencolados]) VALUES(";
                query=query+"getdate(),"+tareas.cuantas()+","+tareas.darMaxTareas()+","+pool.conexionesEnUso()+","+pool.maxConexiones()+","+campanas.darCantCampanas()+","+campanas.darCantCampanasCodigos()+","+cuponesM.darCantInventarioEncolado()+","+cuponesM.maxinventario+","+cuponesM.darCantCuponesEncolado()+","+cuponesM.maxcuponesxalta+","+cuponesM.maxwebservice+")";

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
	Campanas campanas=null;
	Tareas tareas=null;
        ConnectionPool pool=null;
        CuponesMonitor cuponesM=null;
}