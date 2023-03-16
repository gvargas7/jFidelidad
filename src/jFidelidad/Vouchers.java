/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Calendar;
import java.util.Hashtable;
/**
 *
 * @author rhirsch
 */
public class Vouchers {
    public Vouchers(VouchersMonitor losvouchers,String laempresa, String latienda, String lacaja) {
        vouchers=losvouchers;
        empresa=laempresa;
        tienda=latienda;
        log=losvouchers.log;
        caja=lacaja;
        synchronized (cachevouchers) {
            if (cachevouchers==null) {
                cachevouchers=new Hashtable(5000,2500);
            }
            else if (cachevouchers.size()>7000) {
                cachevouchers.clear();
            }
        }
        
    }
    
    public Vouchers() {
        if (cachevouchers==null) {
                cachevouchers=new Hashtable(5000,2500);
        }
    }
    
    boolean generaCupones() { //verifico si esta caja tiene asociada una generacion de cupones.
        boolean genera=false;
        if (vouchers.printers!=null) {
            if (vouchers.printers.get(empresa+"-"+tienda+"-"+caja)!=null)
                genera=true;
        }
        return genera;
    }
    
    boolean buscarProducto(Vector productos,int cual) {
        boolean esta=false;
        int i=0;
        if (productos!=null) {
            while (i<productos.size() && !esta) {
                if (((Integer)(productos.elementAt(i))).intValue()==cual) esta=true;
                else i++;
            }
        }
        return esta;
    }
    
   
    
    VoucherReglaFiltro procesarFiltros(String regla,Vector productos) {
        /*
         1	Fltros: aplica codigo micros directo
         2	Filtros: aplica macro directo
         5	Filtros: Si contiene codigo entonces aplica codigo micros
         7	Filtros: Si contiene codigo entonces aplica macro micros
         4	Filtros: Imprime voucher directo
         3	Filtros: aplica descuento directo
         6	Filtros: Si contiene codigo entonces aplica descuento micros
         8	Filtros: Si contiene codigo entonces imprime voucher
       
         */
        VoucherReglaFiltro rta=null;
        boolean encontro=false;
        Hashtable losfiltros=vouchers.darFiltros();
        if (losfiltros!=null) {
            Vector filtros=(Vector)losfiltros.get(regla);
            if (filtros!=null) {
                int i=0;
                int tipo=0;
                while (i<filtros.size() && !encontro) {
                    VoucherReglaFiltro unfiltro=(VoucherReglaFiltro)filtros.elementAt(i);
                    i++;
                    tipo=unfiltro.tipo;
                    if (tipo>=1 && tipo<=4) {
                        encontro=true;
                        rta=unfiltro;
                        
                    } else {
                        encontro=buscarProducto(productos,unfiltro.contiene);
                        if (encontro) rta=unfiltro;
                    }
                }
            }
        }
        
        return rta;
    }
    
    synchronized String darCodigoCampanaLibre(String campana) {
        String codigo="ERRORCODIGO";
        
        Cupones cupones=new Cupones(vouchers.pool,null);
        if (cupones!=null) {
            codigo=cupones.darCuponLibre(campana, tienda);
        }
        
        return codigo;
    }
    
    void imprimirCuponera(VoucherReglaFiltro filtro,String marca,String tienda,String caja,String regla,int orden) {
        int codigoform=filtro.aplica1; //codigo del voucher a imprimir
        String codigocupon=filtro.campana;
        if (filtro.tipocampana==1) {
            //debo buscar un codigo no usado
            codigocupon=darCodigoCampanaLibre(codigocupon);
        }
        String abuscar=empresa+"-"+tienda+"-"+caja;
        System.out.println("imprimirCuponera: clave: "+abuscar);
        VoucherPrinter cuponera=(VoucherPrinter)(vouchers.darPrinters().get(abuscar));
        if ((cuponera!=null) && (cuponera.esCuponera())) {
            try {
                Vector textos=(Vector)(vouchers.darForms().get(codigoform+""));
                if (textos!=null) {
                    VoucherImpresion impresion=new VoucherImpresion(textos,cuponera,codigocupon,filtro.vigencia,vouchers.tareas,vouchers.log,vouchers.pool,marca,tienda,caja,regla,orden);
                    impresion.run();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    String procesarReglas(String marca,double monto,int rvc,int numtrans,Vector productos) {
        String regla="";
        String comandos="";
        int cuantos=0;
        Vector lasreglas=vouchers.darReglas();
        int hora=darHora();
        VoucherReglaFiltro filtro=null;
        
        if (lasreglas!=null) {
            boolean encontro=false;
            int i=0;
            int impresos=0;
            while (i<lasreglas.size() && (impresos<1) ) { //max 3 cupones x ticket
                VoucherRegla unaregla=(VoucherRegla)lasreglas.elementAt(i);
                i++;
                //------ veo cual regla aplico ------
                if (marca.equals(unaregla.marca)) {
                    if (monto>=unaregla.monto) {
                        if ((unaregla.rvc==0) || (unaregla.rvc!=0 && unaregla.rvc==rvc)) {
                            if ((unaregla.frecuencia==0) || ((numtrans % unaregla.frecuencia)==0)) {
                                if (unaregla.horainicio<=hora && hora<=unaregla.horafinal) {
                                    System.out.println("Encontre regla:"+unaregla.regla);
                                    filtro=procesarFiltros(unaregla.regla,productos);
                                     //------ devuelvo el string de comandos ------
                                    if (filtro!=null) {
                                        impresos++;
                                        // agregar transaccion y hora
                                        if (cachevouchers.get(marca+"-"+tienda+"-"+caja+"-"+hora+"-"+numtrans+"-"+unaregla.regla+"-"+filtro.orden)!=null) {
                                            //si ya estaba cacheado no aplico de nuevo la promo (paso dos veces por tecla aqui)
                                            System.out.println("CACHE VOUCHER ("+cachevouchers.size()+") :"+marca+"-"+tienda+"-"+caja+"-"+hora+"-"+numtrans+"-"+unaregla.regla+"-"+filtro.orden);
                                        }
                                        else {
                                            if (log!=null) log.mensaje("VOUCHERS: Aplico Regla: "+unaregla.regla+" Filtro:"+filtro.orden+" Campaña:"+filtro.campana+" Tienda:"+tienda+" Caja:"+caja);
                                            cachevouchers.put(marca+"-"+tienda+"-"+caja+"-"+hora+"-"+numtrans+"-"+unaregla.regla+"-"+filtro.orden,1);
                                            if (filtro.tipo==4 || filtro.tipo==8) {
                                                //---- hay que imprimir una cuponera
                                                imprimirCuponera(filtro,marca,tienda,caja,unaregla.regla,filtro.orden);
                                            }
                                            else {
                                                comandos=comandos+filtro.tipo+"|"+filtro.aplica1;
                                                cuantos++;
                                                if (filtro.aplica2!=0) {
                                                    comandos=comandos+"|"+filtro.tipo+"|"+filtro.aplica2;
                                                    cuantos++;
                                                }
                                                if (filtro.aplica3!=0) {
                                                    comandos=comandos+"|"+filtro.tipo+"|"+filtro.aplica3;
                                                    cuantos++;
                                                }
                                                if (filtro.aplica4!=0) {
                                                    comandos=comandos+"|"+filtro.tipo+"|"+filtro.aplica4;
                                                    cuantos++;
                                                }
                                            }
                                           //loggeo voucher generado
                                           // Cupones cupones=new Cupones(vouchers.pool,null);
                                           // if (cupones!=null) {
                                           //     cupones.registrarVoucherGenerado(unaregla.regla,filtro.orden,marca,tienda,caja);
                                           // }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
                      
        }
        
        if (cuantos==0) {
            comandos="0|0|0";
        } else
         comandos=cuantos+"|"+comandos;
        
        return comandos;
    }
    
    
     int darHora() {
        Calendar ahora = Calendar.getInstance();
        int hora=ahora.get(Calendar.HOUR_OF_DAY);
       
        
        return hora;
    }
     
    VouchersMonitor vouchers;
    String empresa="";
    String tienda="";
    String caja="";
    static Hashtable cachevouchers=null;
    Log log=null;
}
