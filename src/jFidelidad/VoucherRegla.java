/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class VoucherRegla {
    public VoucherRegla() {
        
    }

    public VoucherRegla(String laregla,int eltipo,String lamarca,double elmonto,int lafrecuencia,int elrvc,int lahorainicio,int lahorafinal) {
        regla=laregla;
        tipo=eltipo;
        marca=lamarca;
        monto=elmonto;
        frecuencia=lafrecuencia;
        horainicio=lahorainicio;
        horafinal=lahorafinal;
        rvc=elrvc;
    }
    
    String regla="";
    int tipo=0;
    String marca="";
    double monto=0;
    int frecuencia=0;
    int rvc=0;
    int horainicio=0;
    int horafinal=0;
    
}
