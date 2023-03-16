/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
public class Cupon {
    public Cupon(String elcodigo) {
        codigo=elcodigo;
    }
    
    int darStatus() {
        return status;
    }
    
    int darCodMicros() {
        return codmicros;
    }
    
    int darCodMicrosNivel() {
        return codmicrosnivel;
    }
    
    String darCodigo() {
        return codigo;
    }
    
    void Status(int elstatus) {
        status=elstatus;
    }
    
    void CodMicros(int elcodigo,int elnivel) {
        codmicros=elcodigo;
        codmicrosnivel=elnivel;
    }
    int status=0;
    int codmicros=0;
    int codmicrosnivel=0;
    String codigo="";
}
