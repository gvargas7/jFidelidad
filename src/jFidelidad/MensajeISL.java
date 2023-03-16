/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.util.Vector;
import java.util.StringTokenizer;
/**
 *
 * @author rhirsch
 */
public class MensajeISL {
    public MensajeISL() {
       valoresresp=new Vector(50,25);
       mensaje="";
       
    }
    public MensajeISL( char[] mensajebuf,int cuantos) {
        valoresresp=new Vector(50,25);
        mensaje=new String(mensajebuf,0,cuantos);
        if (cuantos==29) esping=true;
        else if (cuantos<=8) {
            essistema=true;
            if (cuantos>1) cuantos--;
            comando=new String(mensajebuf,0,cuantos);
            
        }
        else {
            cabecera=new String(mensajebuf,0,27);
            pie=new String(mensajebuf,cuantos-6,6);
            interfase=new String(mensajebuf,10,16);
            POS=new String(mensajebuf,1,9);
            datos=new String(mensajebuf,27,cuantos-33);
            parsearDatos();
        }
    }
    
    void comandoWeb(String web) {
      datos=web;
      parsearDatos();
    }
    
    void parsearDatos() {
        valores=new Vector(50,25);
        try {
            StringTokenizer st=new StringTokenizer(datos,"|");
            String aux="";
            
            while (st.hasMoreTokens()) {
              aux=st.nextToken();
              valores.addElement(aux+"");
              //System.out.println(aux);
            }
          
          } catch (Exception e) {
              e.printStackTrace();
          }
    }
    
    String darComando() {
        return comando;
    }
    String aTexto() {
        return "Interfase: "+interfase+" POS: "+POS+" Datos: "+datos+" Datos Size: "+datos.length();
    }
    
    void agregarResp(String dato) {
        valoresresp.addElement(dato);
       // System.out.println("Agregarresp: "+dato);
    }
    
    String codigoMensaje() {
        return darParam(0);
    }
    
    String darParam(int cual) {
        String param="";
        if (valores!=null && cual<valores.size()) param=(String)valores.elementAt(cual);
        
        return param;
    }
    
    String darParam(int cual,int valordefault) {
        String param=""+valordefault;
        if (valores!=null && cual<valores.size()) param=(String)valores.elementAt(cual);
        
        return param;
    }
    
    String contruirRespuesta() {
        String resp=cabecera;
        int cuantos=valoresresp.size();
        for (int i=0;i<cuantos;i++) {
            resp=resp+(String)valoresresp.elementAt(i);
            if (i<(cuantos-1)) resp=resp+"|";
        }
        resp=resp+pie;
        //System.out.println("CONSTRUIR: "+resp);
        return resp;
    }
    
    String POS="";
    String interfase="";
    String datos="";
    String mensaje="";
    String cabecera="";
    String pie="";
    String comando="";
    boolean esping=false;
    boolean essistema=false;
    Vector valores=null;
    Vector valoresresp=null;
    
}
