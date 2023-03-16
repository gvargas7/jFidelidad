/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class Campana {
    public Campana() {
        
    }
    
    public Campana(String lacampana,int eltipo,String lamarca,boolean esstock,boolean esporalta,int codigo,int codigonivel,int elstock,String elcupon,int longitud,boolean porbanda,boolean elvalidarduplicado,int elrvc,int latienda,int usawebservice,int losdiasvigencia,int pordia,boolean elstockindividual,int ellimitediarioalta) {
        campana=lacampana;
        tipo=eltipo;
        //if (tipo==6) porstockindividual=true;
        porstockindividual=elstockindividual;
        marca=lamarca;
        porstock=esstock;
        codigomicros=codigo;
        codigomicrosnivel=codigonivel;
        stock=elstock;
        poralta=esporalta;
        cupon=elcupon;
        codigolen=longitud;
        solobanda=porbanda;
        validarduplicado=elvalidarduplicado;
        rvc=elrvc;
        tienda=latienda;
        webservice=usawebservice;
        diasvigencia=losdiasvigencia;
        validadia=(pordia==1);
        limitediarioalta=ellimitediarioalta;
    }
    
    String campana="";
    int tipo=0;
    String marca="";
    boolean porstock=false;
    boolean porstockindividual=false;
    int codigomicros=0;
    int codigomicrosnivel=0;
    int stock=0;
    boolean poralta=false;
    String cupon="";
    int codigolen=0;
    boolean solobanda=false;
    boolean validarduplicado=false;
    int rvc=0;
    int tienda=0;
    int webservice=0;
    int diasvigencia=0;
    boolean validadia=false;
    int limitediarioalta=0;
}
