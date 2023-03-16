/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Hashtable;
/**
 *
 * @author rhirsch
 */
public class Codigos {
    public Codigos(Campanas lascampanas) {
        campanas=lascampanas;
        
    }
    
    public Codigos(String elcodigo,Campanas lascampanas,String lamarca) {
        codigo=elcodigo.toUpperCase();
        campanas=lascampanas;
        marca=lamarca;
        
    }
    
    String darCampana() {
        String campana=campanas.darCampanaTexto(codigo,marca);

        return campana;
    }
    
    int verificador(int num) {
        int verif=0;
        while (num>0) {
            verif=verif+num-(num/10)*10;
            num=num/10;
        }
        verif=verif % 7;
        return verif;
    }
    void crearCodigos(String campana,int cuantos,int base,int paso) {
        
        int num=base;
        
        int ran=0;
        String codigo=campana;
        int verif=0;
        for (int i=0;i<cuantos;i++) {
             String hex=Integer.toHexString(num);
             while (hex.length()<5) 
                 hex="0"+hex;
             ran=(int)(Math.random()*10);
             verif=verificador(num);
             
             codigo=campana+hex.substring(0,2)+verif+hex.substring(2,3)+ran+hex.substring(3,5);
             System.out.println("Cod: "+num+" - "+codigo.toUpperCase());
             num=num+(int)(Math.random()*paso)+1;
        }
              
    }
    
    String codigo="";
    Campanas campanas=null;
    String marca="";
}
