/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Hashtable;
import java.util.Vector;
/**
 *
 * @author rhirsch
 */
public class VouchersMonitor  extends Thread {
    public VouchersMonitor(ConnectionPool elpool,Tareas lasTareas,Log elLog) {
        yatermino=false;
        tareas=lasTareas;
        log=elLog;
        Vouchers auxinit=new Vouchers();
        if (elpool!=null) pool=elpool;
        if ((vouchersforms==null) || (printers==null) || (reglas==null) || (reglasfiltros==null) ) {
            
            cargarInfo();
        }
      
    }
    
    public VouchersMonitor() {
        
    }
  
     void forzarCarga() {
        cargarInfo();
    }
    
     synchronized void cargarInfo() {
        Hashtable vouchersaux=new Hashtable(40,10);
        Hashtable printersaux=new Hashtable(600,10);
        Vector reglasaux=new Vector(50,25);
        Hashtable reglasfiltrosaux=new Hashtable(100,50);
        
        if (printers==null) printers=new Hashtable(1,1);
        if (vouchersforms==null) vouchersforms=new Hashtable(1,1);
        if (reglas==null) reglas=new Vector(1,1);
        if (reglasfiltros==null) reglasfiltros=new Hashtable(1,1);
    
             
        int con=0;
        String query="SELECT [regla],[marca],[tipo],[rvc],[monto_disparo],[frecuencia],[hora_inicio],[hora_fin] ";
        query=query+" FROM [Vouchers_Reglas] where inicio<=getdate() and fin>=getdate() ";
        String queryprinters="SELECT [marca],[tienda],[caja],[impresora_cupones],[impresora_tickets] FROM [Vouchers_Printers] where habilitado=1 order by marca,tienda";
        String queryreglasfiltros="SELECT A.[regla],A.[tipo],[contiene],[aplica1],[aplica2],[aplica3],[aplica4],tipocampana,campana,diasvigencia,[orden] FROM [Vouchers_Reglas_Filtros] A,[Vouchers_Reglas] B where inicio<=getdate() and fin>=getdate() and A.regla=B.regla order by A.regla,A.orden";
        String queryvouchers="SELECT [codigo],[texto],[tamano],[estilo],[justificacion] FROM [Vouchers_Vouchers] where activo=1 order by codigo,orden";
        
        if ((con=pool.pedirConexion())>=0) {
            JdbcMgr jdbc=pool.darJdbc(con);
            
            //------ Leo Reglas -------
            int cuantos=0;
            jdbc.ejecutarQuery(query);
            
            while (jdbc.leerSiguienteEnQuery()==1) {
                cuantos++;
                String regla=jdbc.darColumnaQuery("regla");
                int tipo=jdbc.darColumnaQueryInt("tipo");
                String marca=jdbc.darColumnaQuery("marca");
                Double monto=jdbc.darColumnaQueryDouble("monto_disparo");
                int frecuencia=jdbc.darColumnaQueryInt("frecuencia");
                int horainicio=jdbc.darColumnaQueryInt("hora_inicio");
                int horafinal=jdbc.darColumnaQueryInt("hora_fin");
                int rvc=jdbc.darColumnaQueryInt("rvc");
                VoucherRegla unaregla=new VoucherRegla(regla,tipo,marca,monto,frecuencia,rvc,horainicio,horafinal);
                if (regla!=null)
                    reglasaux.addElement(unaregla);
                else log.mensaje("Vouchers: CargarInfo: regla=null"+" Conexiones: "+pool.conexionesEnUso());
            }
            System.out.println("Vouchers: Reglas leidas: "+cuantos);
             //------ Leo Vouchers Forms---------
            boolean primero=true;
            Vector unvoucher=new Vector (16,4);
            String codigoant=""; 
            cuantos=0;
            jdbc.ejecutarQuery(queryvouchers);
            while (jdbc.leerSiguienteEnQuery()==1) {
               cuantos++;
                String codigo=jdbc.darColumnaQuery("codigo");
                if (primero) {
                    primero=false;
                    codigoant=codigo;
                }

                if (!(codigoant.equals(codigo))) {
                    vouchersaux.put(codigoant,unvoucher);
                    if (codigoant.equals("0")) 
                        forminicializacion=unvoucher;
                    unvoucher=new Vector(16,4);
                    codigoant=codigo;
                }
                String texto=jdbc.darColumnaQuery("texto");
                int tamano=jdbc.darColumnaQueryInt("tamano");
                int estilo=jdbc.darColumnaQueryInt("estilo");
                int justificacion=jdbc.darColumnaQueryInt("justificacion");


                VoucherForm unvoucherlinea=new VoucherForm(texto,tamano,estilo,justificacion);
                unvoucher.addElement(unvoucherlinea);
            }
            if (primero==false) {
                 vouchersaux.put(codigoant,unvoucher); 
                 if (codigoant.equals("0")) forminicializacion=unvoucher; 
            }
            System.out.println("Vouchers: VouchersForms leidas: "+cuantos);
            
            //------ Leo Printers -------
            cuantos=0;
            jdbc.ejecutarQuery(queryprinters);
            while (jdbc.leerSiguienteEnQuery()==1) {
                cuantos++;
                String marca=jdbc.darColumnaQuery("marca");
                int tienda=jdbc.darColumnaQueryInt("tienda");
                int caja=jdbc.darColumnaQueryInt("caja");
                String impcupones=jdbc.darColumnaQuery("impresora_cupones");
                int imptickets=jdbc.darColumnaQueryInt("impresora_tickets");
                VoucherPrinter unprinter=new VoucherPrinter(marca,tienda,caja,impcupones,imptickets,255);
                printersaux.put(marca+"-"+tienda+"-"+caja, unprinter);
                
                
                try {
                    VoucherPrinter auxprinter=(VoucherPrinter)(printers.get(marca+"-"+tienda+"-"+caja));
                    if (auxprinter==null) {
                        if ((forminicializacion!=null) && !(impcupones.equals(""))) {
                            VoucherImpresion impresion=new VoucherImpresion(forminicializacion,unprinter,"",0,tareas,log,null,"","","","",0);
                            System.out.println("Vouchers: Inicializo cuponera: "+unprinter.darIpCuponera());
                            log.mensaje("Vouchers: Inicializo cuponera: "+unprinter.darIpCuponera()+" Conexiones: "+pool.conexionesEnUso());
                            impresion.run();
                        }
                    }
                    else {
                        unprinter.setSecuencia(auxprinter.secuencia);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
            }
            System.out.println("Vouchers: Printers leidas: "+cuantos);
            
           
            
             //------ Leo Reglas Filtros ---------
            Vector filtros=new Vector (10,5);
            String reglaant="";
            primero=true;
            cuantos=0;
            jdbc.ejecutarQuery(queryreglasfiltros);
            while (jdbc.leerSiguienteEnQuery()==1) {
                cuantos++;
                String regla=jdbc.darColumnaQuery("regla");
                if (primero) {
                    primero=false;
                    reglaant=regla;
                }
                
                if (!(reglaant.equals(regla))) {
                    reglasfiltrosaux.put(reglaant,filtros);
                    filtros=new Vector(10,5);
                    reglaant=regla;
                }
                
                int tipo=jdbc.darColumnaQueryInt("tipo");
                int contiene=jdbc.darColumnaQueryInt("contiene");
                int aplica1=jdbc.darColumnaQueryInt("aplica1");
                int aplica2=jdbc.darColumnaQueryInt("aplica2");
                int aplica3=jdbc.darColumnaQueryInt("aplica3");
                int aplica4=jdbc.darColumnaQueryInt("aplica4");
                int tipocampana=jdbc.darColumnaQueryInt("tipocampana");
                String campana=jdbc.darColumnaQuery("campana");
                int vigencia=jdbc.darColumnaQueryInt("diasvigencia");
                int orden=jdbc.darColumnaQueryInt("orden");
               
                VoucherReglaFiltro unfiltro=new VoucherReglaFiltro(regla,tipo,contiene,aplica1,aplica2,aplica3,aplica4,tipocampana,campana,vigencia,orden);
                filtros.addElement(unfiltro);
            }
            if (primero==false) {
                reglasfiltrosaux.put(reglaant,filtros); 
            }
            System.out.println("Vouchers: Reglas Filtros leidas: "+cuantos);
            pool.cerrarConexion(con);
            
            synchronized (reglas) {
                reglas=reglasaux;
            }
            synchronized (printers) {
                printers=printersaux;
            }
            synchronized (vouchersforms) {
                vouchersforms=vouchersaux;
            }
            synchronized (reglasfiltros) {
                reglasfiltros=reglasfiltrosaux;
            }
        }
        else System.out.println("Vouchers: CargarInfo: No hay conexion");
    }
     
      
     public void run() 
	{
		tareas.sumar();
		terminarMgr=false;
		if (log!=null) log.mensaje("Vouchers Start");
		
		while (terminarMgr==false) {
                    try {
                            sleep(3600000);
                    } catch (Exception e) {};
                    forzarCarga();
		}
		
		if (log!=null) log.mensaje("Vouchers End");
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
   
    Hashtable darPrinters() {
        return printers;
    }
    
    Hashtable darForms() {
        return vouchersforms;
    }
    
    Hashtable darFiltros() {
        return reglasfiltros;
    }
    
    Vector darReglas() {
        return reglas;
    }
    
    String darInfo() {
        String info="";
        if (printers!=null) info="Cuponeras: "+printers.size();
        else info="Cuponeras: 0";
        
        return info;
    }
    static Hashtable printers=null;
    static Hashtable vouchersforms=null;
    static Vector reglas=null;
    static Hashtable reglasfiltros=null;
    static ConnectionPool pool=null;
    static Vector forminicializacion=null;

    boolean terminarMgr;
    boolean yatermino=false;
    Tareas tareas=null;
    Log log=null;
}
