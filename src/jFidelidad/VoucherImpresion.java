/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Vector;
/**
 *
 * @author rhirsch
 */


public class VoucherImpresion extends Thread {
    public VoucherImpresion(Vector lostextos,VoucherPrinter laimpresora,String elcodigocupon,int lavigencia,Tareas lasTareas,Log elLog,ConnectionPool elpool,String lamarca,String latienda,String lacaja,String laregla,int elorden) {
       
        textos=lostextos;
        tareas=lasTareas;
        log=elLog;
        impresora=laimpresora;
        vigencia=lavigencia;
        codigocupon=elcodigocupon;
        pool=elpool;
        marca=lamarca;
        tienda=latienda;
        caja=lacaja;
        orden=elorden;
        regla=laregla;
    }
    
    public void run() 
    {
        tareas.sumar();
        String conexiones="";
        if (pool!=null) conexiones=pool.conexionesEnUso()+"";
        try {
            if (log!=null) log.mensaje("VouchersImpresion Start: "+impresora.darIpCuponera()+" Conexiones: "+conexiones);

            Cuponera cuponera=new Cuponera(log,textos,impresora,codigocupon,vigencia);

            if (log!=null) log.mensaje("VouchersImpresion End: "+impresora.darIpCuponera()+" Conexiones: "+conexiones);
            //logeo cupon generado
            if (pool!=null)
            {
                Cupones cupones=new Cupones(pool,null);

                if (cupones!=null) {
                    if (cuponera.errorImpresion==false) cupones.registrarVoucherGenerado(regla,orden,marca,tienda,caja);
                }
            };
        } catch (Exception e) {
            log.mensaje("VouchersImpresion ERROR: "+e.toString());
            e.printStackTrace();
        }
        tareas.restar();
    }
    
    Vector textos=null;
    Tareas tareas=null;
    Log log=null;
    VoucherPrinter impresora=null;
    int vigencia=0;
    String codigocupon="";
    ConnectionPool pool=null;
    String marca="";
    String caja="";
    String regla="";
    String tienda="";
    int orden=0;
    
}
