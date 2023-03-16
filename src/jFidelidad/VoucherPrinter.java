/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class VoucherPrinter {
    public VoucherPrinter() {
        
    }

    public VoucherPrinter(String lamarca,int latienda,int lacaja,String impcupones,int imptickets,int lasecuencia) {
        
        marca=lamarca;
        tienda=latienda;
        caja=lacaja;
        if (impcupones.equals("")) escuponera=false;
        else {
            escuponera=true;
            ipcuponera=impcupones;
        }
        esticket=(imptickets==1);
        secuencia=lasecuencia;
    }
    
    boolean esCuponera() {
        return escuponera;
    }
    
    boolean esTicket() {
        return esticket;
    }
    
    String darIpCuponera() {
        return ipcuponera;
    }
    
    int incSecuencia() {
        if (secuencia==0) secuencia=129;
        else  secuencia++;
        if (secuencia>255) secuencia=129;
        return secuencia;
    }
    
    void setSecuencia(int lasecuencia) {
        secuencia=lasecuencia;
    }

    String marca="";
    int tienda=0;
    int caja=0;
    String ipcuponera="";
    boolean escuponera=false;
    boolean esticket=false;
    int secuencia=20;
    
}
