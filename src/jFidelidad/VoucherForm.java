/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class VoucherForm {
    public VoucherForm() {
        
    }
/*
0	Cuponera: Izquierda	
1	Cuponera: Centrado	
2	Cuponera: derecha	
0	Cuponera: Tamaño normal 36 caracteres	
1	Cuponera: Tamaño doble 18 caracteres	
2	Cuponera: Tamaño cuadruple 9 caracteres	
0	Cuponera: Estilo plano	
1	Cuponera: Estilo bold	
2	Cuponera: Estilo inverso
3       Cuponera: Estilo bold inverso
  
 */
    public VoucherForm(String eltexto,int eltamano,int elestilo,int lajustificacion) {
      texto=eltexto;
      tamano=eltamano;
      estilo=elestilo;
      justificacion=lajustificacion;
    }
    
    String texto="";
    int tamano=0;
    int estilo=0;
    int justificacion=0;
}
