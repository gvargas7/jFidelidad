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
public class Cupones {
    public Cupones(ConnectionPool elpool,Campanas lascampanas) {
        pool=elpool;
        campanas=lascampanas;
   
    }
    
    void logear(String mensaje) {
        pool.darLog().mensaje(mensaje+" Conexiones: "+pool.conexionesEnUso());
        //if (log) {
        //    System.out.println(mensaje);
            
        //}
    }
    

    int darCodigoCuponStock(String cupon) {
        int codigomicros=0;
        Campana lacampana=campanas.darCampanaStock(cupon);
        if (lacampana!=null) {
            codigomicros=lacampana.codigomicros;
        }
        return codigomicros;
    }
    synchronized int redimirCuponStock(String campana,String cupon,String tienda, String terminal,String codmicros,String codmicrosnivel,String marca,String dianegocio,int esapp) {
        int stock=-1;
        //Campana lacampana=campanas.darCampanaStock(cupon);
        Campana lacampana=campanas.darCampana(campana,marca);
        if (lacampana!=null) {
            stock=lacampana.stock;
            if (stock>0) {
                lacampana.stock--;
                insertarCuponStock(campana,cupon,tienda,terminal,codmicros,codmicrosnivel,dianegocio,esapp);
                actualizarCuponStock(campana,cupon,lacampana.stock);
            }
            else stock=-1;
        }
        
        return stock;
    }
    
     synchronized int redimirCuponStockIndividual(String campana,String cupon,String tienda, String terminal,String codmicros,String codmicrosnivel,String dianegocio,int esapp) {
        int stock=-1;
        if (dianegocio.equals(""))
            dianegocio="getDate()";
        else dianegocio="'"+dianegocio+"'";
        stock=darCuponIndividualStock(cupon);
        if (stock>0) {
            stock--;
            //actualizarCuponStockIndividual(cupon,stock);
            
            if (stock==0) {
                String query="update cupones set redimido='S',tienda="+tienda+",caja='"+terminal+"',fecha=getdate(),stock=0,codmicros="+codmicros+",cod_micros_tamano="+codmicrosnivel+",dianegocio='"+dianegocio+"',app="+esapp+" where campana='"+campana+"' and cupon='"+cupon+"' and redimido='N' and stock=1 ";
        
                int con=0;
                try {
                   if ((con=pool.pedirConexion())>=0) {
                       JdbcMgr jdbc=pool.darJdbc(con);
                        if (jdbc.ejecutarUpdate(query))
                           logear("Cupon Stock Individual: "+cupon+" redimido. Tienda: "+tienda+" Term: "+terminal);
                        else logear("Cupon Stock Individual: "+cupon+" error en redimido.");

                        pool.cerrarConexion(con);
                        gUltError="";
                   }
                   else {
                       logear("Cupon Stock Individual: "+cupon+" .redimirCupon. No hay conexion");
                       gUltError="Cupon Stock Individual: "+cupon+" .redimirCupon. No hay conexion";
                   }
                } catch (Exception e) {
                   e.printStackTrace();
               }
            } else {
                insertarCuponStock(campana,cupon,tienda,terminal,codmicros,codmicrosnivel,dianegocio,esapp);
                actualizarCuponStockIndividual(cupon,stock);
            }
        }
        //else stock=-1;

        
        return stock;
    }
    
    
     
     int darCuponIndividualStock(String cupon) {
         int stock=-1;
         String query="select stock from cupones where cupon='"+cupon+"' and stock>0";
         String queryexiste="select * from cupones where cupon='"+cupon+"'";
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    stock=jdbc.darColumnaQueryInt(1);
                }
                 else {
                    jdbc.ejecutarQuery(queryexiste);
                    if (jdbc.leerSiguienteEnQuery()==0){
                        stock=-2; //no existe el cupon
                    } else
                     logear("Cupon: "+cupon+" error en darCuponIndividualStock.");
                }
                 
                 pool.cerrarConexion(con);
            }
            else logear("Cupon: "+cupon+" .darCuponIndividualStock. No hay conexion");
         } catch (Exception e) {
            e.printStackTrace();
        }
        return stock;
    }
     
    int darCuponesUtilizadosDia(String cupon) {
         int usados=0;
         String query="SELECT count(*) FROM [Cupones] where cupon='"+cupon+"' and DATEDIFF(day, fecha, getdate()) = 0";
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    usados=jdbc.darColumnaQueryInt(1);
                } 
                 pool.cerrarConexion(con);
            }
            else {
                logear("Cupon: "+cupon+" .darCuponesUtilizadosDia. No hay conexion");
            }
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
        return usados;
    } 
     
    void actualizarCuponStock(String campana,String cupon,int stock) {
         String query="update campanas set stock="+stock+" where campana='"+campana+"' ";//and cupon='"+cupon+"'";
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+cupon+" por Stock cant actualizado.");
                 else logear("Cupon: "+cupon+" error en actualizarCuponStock.");
                 
                 pool.cerrarConexion(con);
                 gUltError="";
            }
            else {
                logear("Cupon: "+cupon+" .actualizarCuponStock. No hay conexion");
                gUltError="Cupon: "+cupon+" .actualizarCuponStock. No hay conexion";
            }
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
    }
    
    void actualizarCuponStockIndividual(String cupon,int stock) {
         String query="update cupones set stock="+stock+" where cupon='"+cupon+"' and redimido='N' and stock>0";
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+cupon+" por StockIndividual cant="+stock+" actualizado.");
                 else logear("Cupon: "+cupon+" error en actualizarCuponStockIndividual.");
                 
                 pool.cerrarConexion(con);
            }
            else logear("Cupon: "+cupon+" .actualizarCuponStockIndividual. No hay conexion");
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
    }
    
    int cuponRedimido(String campana,Cupon elcupon,String ip,Campana lacampana) {
        // redimido=1 cupon ok
        // redimido=2 cupon ya redimido
        // redimido=0 cupon no encontrado
        // redimido=3 cupon vencido
        gMensaje="";
        String sqlvencido="0 vencido";
        if (lacampana!=null) {
            if (lacampana.diasvigencia>0) sqlvencido="case  when isnull(fechaentregado,0)=0 then 0  when dateadd(day,"+lacampana.diasvigencia+",fechaentregado)>=getdate() then 0 else 1 end vencido";
        }
        int redimido=0;
        String codigo=elcupon.darCodigo();
        //case  when isnull(fechaentregado,0)=0 then 0  when dateadd(day,2,fechaentregado)>=getdate() then 0 else 1 end vencido
         String query="select redimido,codmicros,cod_micros_tamano,CONVERT(VARCHAR(19), fecha, 120) fecha,tienda,"+sqlvencido+" from cupones where campana='"+campana+"' and cupon='"+codigo+"'";
         //logear(query);
         int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                
                //ejecutarQuery(query,con);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    elcupon.CodMicros(jdbc.darColumnaQueryInt(2),jdbc.darColumnaQueryInt(3));
                    if(jdbc.darColumnaQuery(1).equals("S")) {
                        redimido=2;
                         gMensaje=" Tienda: "+campanas.darTienda(lacampana.marca, jdbc.darColumnaQuery("tienda"))+" "+jdbc.darColumnaQuery("fecha");
                        //gMensaje=" Tienda: "+campanas.darTienda(campanas.darCampana(campana).marca, jdbc.darColumnaQuery("tienda"))+" "+jdbc.darColumnaQuery("fecha");
                        logear("Cupon: "+codigo+" Ya Redimido. cuponRedimido(). IP: "+ip);
                    }
                    else if (jdbc.darColumnaQuery("vencido").equals("1")) {
                        redimido=3;
                        gMensaje="";
                        logear("Cupon: "+codigo+" Vencido. cuponRedimido(). IP: "+ip);
                    }
                    else redimido=1;
                }
                 else {
                    logear("Cupon: "+codigo+" No Encontrado. cuponRedimido(). IP: "+ip);
                }
                pool.cerrarConexion(con);
                gUltError="";
            }
            else {
                    logear("ERROR Cupon: "+codigo+" No hay conexión. cuponRedimido(). IP: "+ip);
                    gUltError="ERROR Cupon: "+codigo+" No hay conexión. cuponRedimido(). IP: "+ip;
            }
           
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
         
        return redimido;
    }
    
    String darCuponLibre(String campana,String tienda) {
        //doy cupon libre y lo marco
        String codigo="ERRORCODIGO";
        
         String query="SELECT top 1 [cupon]  FROM [Cupones] where campana='"+campana+"' and asignado=0 order by indice";
         String queryupdate="";
         int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                
                //ejecutarQuery(query,con);
                if (jdbc.leerSiguienteEnQuery()==1) {
                   codigo=jdbc.darColumnaQuery(1);
                   queryupdate="update Cupones set asignado=1,fechaentregado=getdate(),tiendaentregado="+tienda+" where campana='"+campana+"' and cupon='"+codigo+"'";
                   jdbc.ejecutarUpdate(queryupdate);
                }
                 else {
                    logear("darCuponLibre: "+campana+" No Encontrado. Tienda:"+tienda);
                }
                pool.cerrarConexion(con);
            }
            else {
                    logear("ERROR darCuponLibre: "+campana+" No hay conexión. Tienda:"+tienda);
            }
           
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
         
        return codigo;
    }
    void registrarVoucherGenerado(String regla,int orden,String marca,String tienda, String caja) {
        //doy cupon libre y lo marco
        
       String queryupdate="insert into Vouchers_Generados values('"+regla+"',"+orden+",getdate(),'"+marca+"',"+tienda+",'"+caja+"')";
         int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarUpdate(queryupdate);
         
                pool.cerrarConexion(con);
                gUltError="";
            }
            else {
                    logear("ERROR registrarVoucherGenerado: "+regla+" No hay conexión. Tienda:"+tienda);
                    gUltError="ERROR registrarVoucherGenerado: "+regla+" No hay conexión. Tienda:"+tienda;
            }
           
         } catch (Exception e) {
            e.printStackTrace();
            if (con>0) pool.cerrarConexion(con);
            logear("ERROR registrarVoucherGenerado: "+regla+" Tienda:"+tienda+" Exception:"+e.toString());
        }
    }
    
    int cuponCVRedimido(String codigocv,String codigopremio) {
        // redimido=1 cupon ok
        // redimido=2 cupon ya redimido
        // redimido=0 cupon no encontrado
        // redimido=3 cupon vencido
        int redimido=0;
         String query="SELECT [redimido],datediff(day,fecha_alta,getdate()) FROM [CV_Cupones] where cupon='"+codigocv+"'";
         //logear(query);
         int con=-1;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(query);
                
                //ejecutarQuery(query,con);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    if(jdbc.darColumnaQuery(1).equals("S")) redimido=2;
                    else {
                        int dias=jdbc.darColumnaQueryInt(2);
                        if (dias>30) redimido=3;
                        else redimido=1;
                    }
                }
                 else {
                    logear("Cupon: "+codigocv+" No Encontrado. cuponCVRedimido().");
                }
                pool.cerrarConexion(con);
            }
            else {
                    logear("ERROR Cupon: "+codigocv+" No hay conexión. cuponCVRedimido().");
            }
           
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
             logear("CV: "+codigocv+" .cuponCVRedimido. Exception:"+e.toString());
        }
         
        return redimido;
    }
    
    void insertarCuponStock(String campana,String cupon,String tienda, String terminal,String codmicros,String codmicrosnivel,String diaNegocio,int esapp) {
        if (diaNegocio.equals(""))
             diaNegocio="getDate()";
         else diaNegocio="'"+diaNegocio+"'"; 
        String query="INSERT INTO [Cupones] ([cupon],[campana],[redimido],[tienda],[caja],[fecha],[stock],[codmicros],[cod_micros_tamano],[dianegocio],[app])";
         query=query+" VALUES ('"+cupon+"','"+campana+"','S',"+tienda+",'"+terminal+"',getdate(),0,"+codmicros+","+codmicrosnivel+","+diaNegocio+","+esapp+")";
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+cupon+" por Stock.");
                 else logear("Cupon: "+cupon+" error en insertarCuponStock.");
                 
                 pool.cerrarConexion(con);
            }
            else logear("Cupon: "+cupon+" .insertarCuponStock. No hay conexion");
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
    }
    
    void altaCupon(String campana,String cupon,String tienda, String terminal,String codmicros,String codmicrosnivel,String dianegocio,int esapp) {
        if (dianegocio.equals(""))
             dianegocio="getDate()";
         else dianegocio="'"+dianegocio+"'";
        //inserta el cupon redimido para campañas sin control de stock o código previo
         String query="INSERT INTO [Cupones] ([cupon],[campana],[redimido],[tienda],[caja],[fecha],[stock],[codmicros],[cod_micros_tamano],[dianegocio],[app])";
         query=query+" VALUES ('"+cupon+"','"+campana+"','S',"+tienda+",'"+terminal+"',getdate(),0,"+codmicros+","+codmicrosnivel+","+dianegocio+","+esapp+")";
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+cupon+" por Alta. Tienda: "+tienda);
                 else logear("Cupon: "+cupon+" error en altaCupon.");
                 
                 pool.cerrarConexion(con);
                 gUltError="";
            }
            else {
                logear("Cupon: "+cupon+" .altaCupon. No hay conexion");
                gUltError="Cupon: "+cupon+" .altaCupon. No hay conexion";
            }
         } catch (Exception e) {
            e.printStackTrace();
            if (con>0) pool.cerrarConexion(con);
        }
    }
    void actualizarInventarioCaja(String empresa,String tienda,String terminal,String version,String modelocaja,String rvc) {
         if (rvc.equals("")) rvc="0";
         String querybuscarcaja="SELECT * from Inventario where tienda="+tienda+" and caja='"+terminal+"' and empresa='"+empresa+"'";
         String queryupdatecaja="UPDATE [Inventario] set versionisl="+version+",modelocaja='"+modelocaja+"',fecha=getDate(),rvc="+rvc+" where tienda="+tienda+" and caja='"+terminal+"' and empresa='"+empresa+"'";
         String queryinsertcaja="INSERT INTO [Inventario] ([tienda],[empresa],[caja],[versionisl],[modelocaja],[fecha],[rvc]) VALUES ";
         queryinsertcaja=queryinsertcaja+"("+tienda+",'"+empresa+"','"+terminal+"',"+version+",'"+modelocaja+"',getdate(),"+rvc+")";
         
          int con=0;
          try {
            if ((con=pool.pedirConexion())>=0) {
                //busco el código de tienda de CV
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(querybuscarcaja);
                logear("Inventario: "+empresa+" tienda: "+tienda+" terminal: "+terminal+" version: "+version+" Modelo: "+modelocaja);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    jdbc.ejecutarUpdate(queryupdatecaja);
                }
                else {
                    jdbc.ejecutarUpdate(queryinsertcaja);
                }
                
                 pool.cerrarConexion(con);
                 gUltError="";
            }
            else {
                logear("Cupones.error en actualizarInventarioCaja. No hay conexion");
                gUltError="Cupones.error en actualizarInventarioCaja. No hay conexion";
            }
          } catch (Exception e ){
              if (con>0) pool.cerrarConexion(con);
               logear("Cupones: actualizarInventarioCaja. Exception:"+e.toString());
                e.printStackTrace();
          }  
    }
                     
    Vector altaCuponCV(String cupon,String tienda, String terminal) {
        // res=0 error conexion
        // res=1 ok alta
        // res=2 tienda no encontrada
        // res=3 error insert
        // res=4 cupon duplicado
         int res=0;
         Vector respuesta=new Vector(3);
         String querytienda="SELECT [codigocv],frecuencia FROM [CV_Tiendas] where codigo="+tienda;
         int frecuencia=20;
         String tiendacv="";

         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                //busco el código de tienda de CV
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(querytienda);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    tiendacv=jdbc.darColumnaQuery(1);
                    cupon=jdbc.darColumnaQuery(1)+cupon;
                    
                    frecuencia=jdbc.darColumnaQueryInt(2);
                    //agrego el nuevo codigo CV generado
                    String queryconsulta="SELECT * FROM [CV_Cupones] WHERE cupon='"+cupon+"'";
                    String query="INSERT INTO [CV_Cupones] ([cupon],[redimido],[tienda],[caja],[fecha_alta])";
                    query=query+" VALUES ('"+cupon+"','N',"+tienda+",'"+terminal+"',getdate())";
                    jdbc.ejecutarQuery(queryconsulta);
                    if (jdbc.leerSiguienteEnQuery()==1) {
                        logear("CV: "+cupon+" DUPLICADO en altaCuponCV");
                        res=4;
                    }
                    else {
                        if (jdbc.ejecutarUpdate(query)) {
                            res=1;
                            logear("CV: "+cupon+" ingresado. Caja="+terminal);
                        }
                        else {
                            res=3;
                            logear("CV: "+cupon+" "+tienda+" error en altaCuponCV.insertarCupon.");
                        }
                    }
                }
                else {
                    res=2;
                    logear("CV: "+cupon+" "+tienda+" error en altaCuponCV.buscarTienda.");
                }
                
                 
                 pool.cerrarConexion(con);
                 gUltError="";
            }
            else {
                res=0;
                logear("CV: "+cupon+" .altaCuponCV. No hay conexion");
                gUltError="CV: "+cupon+" .altaCuponCV. No hay conexion";
            }
         } catch (Exception e) {
             res=0;
             if (con>0) pool.cerrarConexion(con);
              logear("CV: "+cupon+" .altaCuponCV. Exception:"+e.toString());
            e.printStackTrace();
        }
        respuesta.addElement(new Integer(res));
        respuesta.addElement(new Integer(frecuencia));
        respuesta.addElement(tiendacv);
         return respuesta;
    }
    
    Vector darFrecuenciaCV(String tienda) {
         // res=0 error conexion
        // res=1 ok frec
        // res=2 tienda no encontrada
         int res=0;
         Vector respuesta=new Vector(2);
         String querytienda="SELECT frecuencia FROM [CV_Tiendas] where codigo="+tienda;
         int frecuencia=20;
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                //busco el código de tienda de CV
                JdbcMgr jdbc=pool.darJdbc(con);
                jdbc.ejecutarQuery(querytienda);
                if (jdbc.leerSiguienteEnQuery()==1) {
                    frecuencia=jdbc.darColumnaQueryInt(1);
                    res=1;
                }
                else {
                    res=2;
                    logear("CV: error en darFrecuenciaCV. Tienda: "+tienda);
                }     
                 pool.cerrarConexion(con);
            }
            else {
                res=0;
                logear("CV: error en darFrecuenciaCV. No hay conexion");
            }
         } catch (Exception e) {
             res=0;
             if (con>0) pool.cerrarConexion(con);
              logear("CV: error en darFrecuenciaCV Exception:"+e.toString());
            e.printStackTrace();
        }
        respuesta.addElement(new Integer(res));
        respuesta.addElement(new Integer(frecuencia));
         return respuesta;
    }
    
    void redimirCupon(String campana,String codigo,String tienda, String terminal,String codmicros,String codmicrosnivel,String dianegocio,int esapp) {
         
         if (codmicros.equals("")) codmicros="0";
         if (dianegocio.equals(""))
             dianegocio="getDate()";
         else dianegocio="'"+dianegocio+"'";
         String query="update cupones set redimido='S',tienda="+tienda+",caja='"+terminal+"',fecha=getdate(),codmicros="+codmicros+",cod_micros_tamano="+codmicrosnivel+",DiaNegocio="+dianegocio+",app="+esapp+" where campana='"+campana+"' and cupon='"+codigo+"'";// and DiaNegocio= "+dianegocio;
        
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+codigo+" redimido. Tienda: "+tienda+" Term: "+terminal);
                 else logear("Cupon: "+codigo+" error en redimido.");
                 
                 pool.cerrarConexion(con);
                 gUltError="";
            }
            else {
                logear("Cupon: "+codigo+" .redimirCupon. No hay conexion");
                gUltError="Cupon: "+codigo+" .redimirCupon. No hay conexion";
            }
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
    }
    
    void redimirCuponPorAltaInmediato(String campana,String codigo,String tienda, String terminal,String codmicros,String codmicrosnivel,String dianegocio,int esapp) {
         
         if (codmicros.equals("")) codmicros="0";
         if (dianegocio.equals(""))
             dianegocio="getDate()";
         else dianegocio="'"+dianegocio+"'";
        String query="INSERT INTO [Cupones] ([cupon],[campana],[redimido],[tienda],[caja],[fecha],[stock],[offline],[codmicros],[cod_micros_tamano],[dianegocio],[app])";
        query=query+" VALUES ('"+codigo+"','"+campana+"','S',"+tienda+",'"+terminal+"',getdate(),0,0,"+codmicros+","+codmicrosnivel+","+dianegocio+","+esapp+")";
                      
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+codigo+" redimido. Tienda: "+tienda+" Term: "+terminal);
                 else logear("Cupon: "+codigo+" error en redimirCuponPorAltaInmediato.");
                 
                 pool.cerrarConexion(con);
                 gUltError="";
            }
            else {
                logear("Cupon: "+codigo+" .redimirCuponPorAltaInmediato. No hay conexion");
                gUltError="Cupon: "+codigo+" .redimirCuponPorAltaInmediato. No hay conexion";
            }
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
    }
    
     void habilitarCupon(String codigo) {
        
         String query="update cupones set redimido='N' where cupon='"+codigo+"'";
         //System.out.println(query);
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query))
                    logear("Cupon: "+codigo+" habilitado.");
                 else logear("Cupon: "+codigo+" error en habilitarCupon.");
                 
                 pool.cerrarConexion(con);
            }
            else logear("Cupon: "+codigo+" .habilitarCupon. No hay conexion");
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
            e.printStackTrace();
        }
    }
    
    String darOpcionesCupon(String campana,String versionisl) {
         String opciones="";
         
         try {
            Vector opc=campanas.darOpcionesCampana(campana);
            if (opc!=null){
                for (int i=0;i<opc.size();i++) {
                    String aux[]=(String [])(opc.elementAt(i));
                    System.out.println(aux+" "+aux[0]+" "+aux[1]+" "+aux[2]);
                    //if (versionisl.equals("5.0"))
                        opciones=opciones+"|"+aux[1]+"|"+aux[0]+"|"+aux[2];
                    //else
                    //    opciones=opciones+"|"+aux[1]+"|"+aux[0];
                }
                opciones="|"+opc.size()+opciones;
            }
         } catch (Exception e) {
            e.printStackTrace();
        }
        return opciones;
    }
    
    Vector redimirCuponCV(String codigocv,String codigopremio,String tienda, String terminal) {
        String beneficio="";
        int frecuencia=20;
        Vector resp=new Vector(2);
         String query="update CV_Cupones set redimido='S',tienda_redimio="+tienda+",caja_redimio='"+terminal+"',fecha_redimio=getdate(),codigo_redimio='"+codigopremio+"' where cupon='"+codigocv+"'";
         String queryfrecuencia="SELECT frecuencia FROM [CV_Tiendas] where codigo="+tienda;
         int con=0;
         try {
            if ((con=pool.pedirConexion())>=0) {
                JdbcMgr jdbc=pool.darJdbc(con);
                 if (jdbc.ejecutarUpdate(query)) {
                    logear("Cupon: "+codigocv+" Premio: "+codigopremio+" redimido.redimirCuponCV");
                    String querybeneficio="SELECT [codigo_descuento],[codigo_producto] FROM [CV_Beneficio]";
                    jdbc.ejecutarQuery(querybeneficio);
                    if (jdbc.leerSiguienteEnQuery()==1) {
                        beneficio=jdbc.darColumnaQuery(1)+"|"+jdbc.darColumnaQuery(2);
                        jdbc.ejecutarQuery(queryfrecuencia);
                        if (jdbc.leerSiguienteEnQuery()==1) {
                            frecuencia=jdbc.darColumnaQueryInt(1);
                        }
                        else logear("Cupon: "+codigocv+" Premio: "+codigopremio+" error en redimido.redimirCuponCV.gFrecuenciaCV");
                    }
                 }
                 else logear("Cupon: "+codigocv+" Premio: "+codigopremio+" error en redimido.redimirCuponCV");
                 
                 pool.cerrarConexion(con);
            }
            else logear("Cupon: "+codigocv+" .redimirCuponCV. No hay conexion");
         } catch (Exception e) {
            if (con>0) pool.cerrarConexion(con);
              logear("CV: "+codigocv+" Premio: "+codigopremio+" .redimirCuponCV. Exception:"+e.toString());
            e.printStackTrace();
        }
         resp.addElement(beneficio);
         resp.addElement(new Integer(frecuencia));
         return resp;
    }
    
    int tipoCampana(String campana,String marca) {
        int tipo=0;
        tipo=campanas.tipoCampana(campana,marca);
        return tipo;
    }
    
   // String bd="172.31.1.14";
   // String user="fidelidad";
   // String pass="f1d3l1d4d$";
    boolean log=true;
    //Connection conn=null;
    //Statement stmt=null;    
    //ResultSet rs=null;
    ConnectionPool pool=null;
    Campanas campanas=null;
    //int gFrecuenciaCV=20;
    //String gTiendaCV="00000";
    String gUltError="";
    String gMensaje="";
}
