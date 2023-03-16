/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

/**
 *
 * @author rhirsch
 */
import java.sql.*;
import java.util.Vector;
/**
 *
 * @author rhirsch
 */
public class DatosPromociones {
    public DatosPromociones(ConnectionPool elpool) {
        pool=elpool;
   
    }
    
    void logear(String mensaje) {
        pool.darLog().mensaje(mensaje+" Conexiones: "+pool.conexionesEnUso());
       
    }
       
     synchronized int altaDatosPromocion(String nombre,String apellido,String id, String email, String telefono,String empresa, String tipo,String tienda,String caja) {
        int res=0;
        String querybuscar="SELECT [id],empresa FROM [Clientes_Promociones]  where id='"+id+"' and empresa='"+empresa+"'";
        String queryinsert="INSERT INTO [Clientes_Promociones] ([id],[nombre],[apellido],[empresa],[tipo],[email],[telefono],[fechaalta],[tienda],[caja]) ";
        queryinsert=queryinsert+" VALUES ('"+id+"','"+nombre+"','"+apellido+"','"+empresa+"',"+tipo+",'"+email+"','"+telefono+"',getDate(),"+tienda+",'"+caja+"')";
          
        int con=0;
        try {
           if ((con=pool.pedirConexion())>=0) {
               JdbcMgr jdbc=pool.darJdbc(con);
               jdbc.ejecutarQuery(querybuscar);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    res=2; //datos existentes
                    logear("AltaDatosPromocion: "+id+" "+nombre+" "+apellido+" Existente. Tienda: "+tienda);

                } else {
                    if (jdbc.ejecutarUpdate(queryinsert)) {
                        res=1;
                        logear("AltaDatosPromocion: "+id+" "+nombre+" "+apellido+" Ingresado. Tienda: "+tienda);
                        //lo inserto realtime en la tabla de empleados para que el descuento sea efectivo ya
                        String nombreap=nombre+" "+apellido;
                        if (nombreap.length()>50) nombreap=nombreap.substring(0,49);
                        String insert="insert into empleados values('"+id+"','"+nombreap+"','"+empresa+"',"+tipo+")";
                        try {
                            jdbc.ejecutarUpdate(insert);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                            logear("AltaDatosPromocion: "+id+" "+nombre+" "+apellido+" Error en insert en empleados. Tienda: "+tienda+" SQL:"+queryinsert);
                        }
                    
                    }
                    else {
                        res=3; //error en alta
                        logear("AltaDatosPromocion: "+id+" "+nombre+" "+apellido+" Error en insert. Tienda: "+tienda+" SQL:"+queryinsert);
                    }
                }

                pool.cerrarConexion(con);
           }
           else {
               res=4;
               logear("AltaDatosPromocion: "+id+" "+nombre+" "+apellido+"  No hay conexion");
           }
        } catch (Exception e) {
           e.printStackTrace();
       }
 
        return res;
    }
    
    boolean log=true;
    
    ConnectionPool pool=null;
    Campanas campanas=null;
   
}
