/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class Tareas {

	public void Tareas() {
		inicializar();
	}
	
	public synchronized  void inicializar() {
		if (infoinst==null) {
			infoinst=new String[maxprog][maxservlets][2];
			for (int i=0;i<maxprog;i++)
				for (int j=0;j<maxservlets;j++) {
					infoinst[i][j][0]="";
					infoinst[i][j][1]="";
				}
		}
		if (cantinst==null) {
			cantinst=new int[maxprog];
			for (int i=0;i<maxprog;i++) {
				cantinst[i]=0;
			}
		}
		if (servletname==null) {
			servletname=new String[maxprog];
			for (int i=0;i<maxprog;i++) {
				servletname[i]="";
			}
		}
	}

	public synchronized void sumar() {
		cantthreads++;
		if (cantthreads>maxTareas) maxTareas=cantthreads;
	}
	
	public synchronized  void restar() {
		cantthreads--;
	}
	
	public  synchronized int cuantas() {
		return cantthreads;
	}
	
	public  synchronized int darMaxTareas() {
		return maxTareas;
		
	}

	public  synchronized void agregarInst(String servletnombre,int servlet,int instancia,int puesto,String texto) {
		if (servletname==null) inicializar();
		servletname[servlet]=servletnombre;
		agregarInst(servlet,instancia,puesto,texto);
	}
	
	public  synchronized void agregarInst(int servlet,int instancia,int puesto,String texto) {
		if (instancia>=maxservlets) instancia=maxservlets-1;
		infoinst[servlet][instancia][puesto]=infoinst[servlet][instancia][puesto]+texto;
		if ((instancia<maxservlets) && (instancia>cantinst[servlet])) {
			cantinst[servlet]=instancia;
		}
		if (servlet>maxservlet) {
			maxservlet=servlet;
		}
	}
	
	public  synchronized void limpiarInst(int servlet,int instancia) {
		if (instancia>=maxservlets) instancia=maxservlets-1;
		infoinst[servlet][instancia][0]="";
		infoinst[servlet][instancia][1]="";
	}

	public  synchronized String[][][] darInfoInst() {
		return infoinst;
	}
	
	public  synchronized String darInfoInst(int servlet,int instancia,int puesto) {
		if (instancia>=maxservlets) instancia=maxservlets-1;
		return infoinst[servlet][instancia][puesto];
	}
	
	public  synchronized int darMaxInst(int servlet) {
		return cantinst[servlet];
	}
	
	public  synchronized String darServletName(int servlet) {
		return servletname[servlet];
	}
	
	public  synchronized int darMaxServlet() {
		return maxservlet;
	}
	
	int cantthreads=0;
	int maxTareas=0;
	int maxprog=40;
	int maxservlets=300;
	static String infoinst[][][]=null;
	static int cantinst[]=null;
	static int maxservlet=0;
	static String servletname[]=null;
	
}
