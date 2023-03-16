/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.sql.*;
import java.util.Vector;
/**
 *
 * @author rhirsch
 */
public class Credito {
    public Credito(ConnectionPool elpool) {
        pool=elpool;
   
    }
    
    void logear(String mensaje) {
        pool.darLog().mensaje(mensaje+" Conexiones: "+pool.conexionesEnUso());
    
    }
    
    boolean darSaldo(String tarjeta,String tienda) {
        //consulta saldo
        boolean res=false;
        gStatus=-1;
        String query="SELECT marca,datediff(day,vigencia,getdate()) as vencido,saldo,status,cod_micros_desc,cod_micros_adicional FROM [Credito_Tarjetas] A,[Credito_Def] B ";
        query=query+" where A.empresa=B.empresa and A.id='"+tarjeta+"'";
         int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                
                if (jdbc.leerSiguienteEnQuery()==1) {
                   gMarca=jdbc.darColumnaQuery("marca");
                   gVencido=jdbc.darColumnaQueryInt("vencido")>0; //true si esta vencido
                   gSaldo=jdbc.darColumnaQuery("saldo");
                   gStatus=jdbc.darColumnaQueryInt("status"); //0 ok, 1 anulada, 2 extraviada, -1 tarj no encontrada
                   gCodigoMicros=jdbc.darColumnaQueryInt("cod_micros_desc");
                   gCodigoMicrosAdicional=jdbc.darColumnaQueryInt("cod_micros_adicional");
                   res=true;
                }
                 else {
                    res=true;
                    gStatus=-1;
                    logear("darSaldo: "+tarjeta+" No Encontrado. Tienda:"+tienda);
                }
                pool.cerrarConexion(con);
            }
            else {
                    logear("ERROR darSaldo: "+tarjeta+" No hay conexión. Tienda:"+tienda);
                    gUltError="ERROR darSaldo: "+tarjeta+" No hay conexión. Tienda:"+tienda;
            }
           
         } catch (Exception e) {
            e.printStackTrace();
        }
         
        return res;
    }
    
    boolean ultimosMovimientos(String tarjeta,String tienda) {
        //consulta saldo
        boolean res=false;
        gMensaje="";
        String query="SELECT top 5 FORMAT ( fecha_movimiento, 'd', 'en-gb' ),B.descripcion,[monto] ,A.[saldo],marca";
        query=query+" FROM [Credito_Movimientos] A,Credito_Tipos_Movimientos B,[Credito_Def] C,Credito_Tarjetas D where A.id='"+tarjeta+"'";
        query=query+" and A.tipo_movimiento=B.tipo_movimiento and clasif=1 and A.id=D.id and D.empresa=C.empresa order by fecha_movimiento desc";
        System.out.println(query); 
        int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                int cuantos=0;
                while (jdbc.leerSiguienteEnQuery()==1) {
                   res=true;
                   cuantos++;
                   gMarca=jdbc.darColumnaQuery("marca");
                   gMensaje=gMensaje+jdbc.darColumnaQuery(1)+" : "+jdbc.darColumnaQuery(2)+" Monto: "+jdbc.darColumnaQuery(3)+" Saldo:"+jdbc.darColumnaQuery(4)+"|";
                }
                if (!res) {
                    res=true;
                    logear("ultimosMovimientos: "+tarjeta+" No Encontrado. Tienda:"+tienda);
                }
                pool.cerrarConexion(con);
                gMensaje=cuantos+"|"+gMensaje;
            }
            else {
                    logear("ERROR ultimosMovimientos: "+tarjeta+" No hay conexión. Tienda:"+tienda);
                    gUltError="ERROR ultimosMovimientos: "+tarjeta+" No hay conexión. Tienda:"+tienda;
            }
           
         } catch (Exception e) {
            e.printStackTrace();
        }
         
        return res;
    }
    
     boolean consumirSaldo(String tarjeta,String tienda,String caja,String monto) {
        //consumir saldo
        boolean res=false;
        String saldo="";
        gStatus=-1;
        String querysaldo="SELECT saldo-"+monto+" from Credito_Tarjetas where id='"+tarjeta+"'";
        String queryupdate="UPDATE [Credito_Tarjetas] SET [saldo] = saldo-"+monto+",[ult_actualizacion] = getdate() WHERE id='"+tarjeta+"'";
        String queryinsert="INSERT INTO [Credito_Movimientos]([id],[tipo_movimiento],[fecha_movimiento],[tienda],[caja],[monto],[saldo])";
        int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(querysaldo);
                
                if (jdbc.leerSiguienteEnQuery()==1) {
                   //consulto saldo y descuento monto, actualizo el total y el detalle de movimientos
                   saldo=jdbc.darColumnaQuery(1);
                   System.out.println(querysaldo);
                   jdbc.ejecutarUpdate(queryupdate);
                   queryinsert=queryinsert+" VALUES ('"+tarjeta+"',2,getdate(),"+tienda+","+caja+","+monto+","+saldo+")";
                   jdbc.ejecutarUpdate(queryinsert);
                   res=true;
                   gStatus=0; 
                   gSaldo=saldo;
                }
                 else {
                    res=true;
                    gStatus=-1;
                    logear("consumirSaldo: "+tarjeta+" No Encontrado. Tienda:"+tienda+" Caja:"+caja);
                }
                pool.cerrarConexion(con);
            }
            else {
                    logear("ERROR consumirSaldo: "+tarjeta+" No hay conexión. Tienda:"+tienda+" Caja:"+caja);
                    gUltError="ERROR consumirSaldo: "+tarjeta+" No hay conexión. Tienda:"+tienda+" Caja:"+caja;
            }
           
         } catch (Exception e) {
            e.printStackTrace();
        }
         
        return res;
    }
   
        

    boolean log=true;
  
    ConnectionPool pool=null;
    String gUltError="";
    String gMarca="";
    boolean gVencido=false;
    String gSaldo="0";
    int gStatus=0;
    String gMensaje="";
    int gCodigoMicros=0;
    int gCodigoMicrosAdicional=0;
}
