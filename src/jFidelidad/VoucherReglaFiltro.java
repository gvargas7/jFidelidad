/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class VoucherReglaFiltro {
    public VoucherReglaFiltro() {
        
    }

    public VoucherReglaFiltro(String laregla,int eltipo,int quecontiene,int a1,int a2,int a3,int a4,int eltipocampana,String lacampana,int lavigencia,int elorden) {
        regla=laregla;
        tipo=eltipo;
        contiene=quecontiene;
        aplica1=a1;
        aplica2=a2;
        aplica3=a3;
        aplica4=a4;
        tipocampana=eltipocampana;
        campana=lacampana;
        vigencia=lavigencia;
        orden=elorden;
    }
    
    String regla="";
    int tipo=0;
    int orden=0;
    int contiene=0;
    int aplica1=0;
    int aplica2=0;
    int aplica3=0;
    int aplica4=0;
    int tipocampana=0;
    String campana="";
    int vigencia=0;
    
}
