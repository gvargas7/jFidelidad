/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Vector;
/**
 *
 * @author rhirsch
 */
public class Distribuidor extends Thread{
     public Distribuidor( Tareas clientetareas,BufferedWriter respuesta,char[] datos,int cuantos,String elip,ConnectionPool elpool,Campanas lascampanas,boolean chile,boolean colombia,VouchersMonitor elvouchermonitor,Solicitudes lasSolicitudes,CuponesMonitor elcuponesmonitor,ComunicacionesMonitor elcomunicacionesmonitor,boolean pedidoweb) {
         gWebService=pedidoweb;
         gClienteTareas=clientetareas;
         gBufferRespuesta=respuesta;
         solicitudes=lasSolicitudes;
         cuponesmonitor=elcuponesmonitor;
         comunicacionesmonitor=elcomunicacionesmonitor;
         if (datos!=null) {
             mensaje=new MensajeISL(datos,cuantos);
             System.out.println(mensaje.aTexto());
         } else mensaje=null;
         int aux=elip.indexOf(".");
         int aux2=elip.indexOf(".",aux+1);
         int aux3=elip.indexOf(".",aux2+1);
         ip=elip;
         tienda=elip.substring(aux2+1,aux3);
         String empaux=elip.substring(aux+1,aux2);
         if (empaux.equals("20")) {
             empresa="BK";
             gPais=1;
         }
         else if (empaux.equals("21")) empresa="PF";
         else if (empaux.equals("22")) {
             empresa="SBX";
             gPais=49;
         }
         else if (empaux.equals("31")) {
             empresa="BK";
             gPais=1;
         }
         else if (empaux.equals("19")) {
             empresa="SBUY";
             gPais=78;
         }
         else empresa="NO";
         if (ip.equals("172.31.1.80")) {
             empresa="BK";
             tienda="300";
         }
         else if (ip.equals("172.31.1.81")||ip.equals("192.168.226.20")||(ip.equals("127.0.0.1"))){
             empresa="SBX";
             tienda="300";
         }
         //else if (ip.equals("172.31.1.39")) {
         //    empresa="PF";
         //    tienda="300";
         //}
         if (chile) {
             empresa="BK";
             if (elip.indexOf("10.51.")==0) empresa="SBX";
             if (elip.equals("192.168.244.80")) empresa="BK";
             else if (elip.equals("192.168.198.137")) empresa="SBX";
         }
         if (colombia) {
             empresa="NO";
             if (empaux.equals("41")) empresa="BK";
             else if (empaux.equals("43")) {
                 empresa="SBX";
                 gPais=57;
             }
             else if (empaux.equals("44")) empresa="ARC";
             if (ip.equals("192.168.207.64")) {
                 empresa="ARC";
                 tienda="300";
             }
         }
         
         pool=elpool;
         campanas=lascampanas;
         if (cupones==null) cupones=new Cupones(pool,campanas);
         vouchersmonitor=elvouchermonitor;
         
     }
     
      public void run() {
         try {  
            System.out.println("\r\n\r\n****** POR PROCESAR *******");
            String datos=procesarMensaje(gClienteTareas.cantthreads,"");
            //sleep(8000);
           String auxsalida=datos;
            if ((datos!=null) && (datos.length()>80)) {
                auxsalida=datos.substring(0,80);
            }
            System.out.println("Thread: "+gClienteTareas.cantthreads+" - Devolviendo mensaje: "+datos.length()+" "+auxsalida);
            System.out.println("\r\n\r\n****** POR ENVIAR *******");
            gBufferRespuesta.write(datos,0,datos.length());  
            System.out.println("\r\n\r\n******FIN POR ENVIAR *******\r\n\r\n");

            gBufferRespuesta.flush();
            //gBufferRespuesta.close();
            
            System.out.println("Thread: "+gClienteTareas.cantthreads+" - Fin ");
         } catch (Exception e) {
             System.out.println(e.toString());
             pool.darLog().mensaje(e.toString());
         }
         gClienteTareas.restar();
     }
      
     String procesarMensaje(int esteThread,String comandoWeb) {
         String respuesta="";
         if (gWebService) {
             mensaje=new MensajeISL();
             mensaje.comandoWeb(comandoWeb);
         }
         String comando=mensaje.darComando();
         String codigo=mensaje.codigoMensaje();
         String caja=mensaje.darParam(1);
         if (caja==null) caja="";
         pool.darLog().mensaje("Therad "+esteThread+" - ProcesarMensaje: "+comando+" "+codigo+" Comienzo "+" IP:"+ip+" Caja: "+caja);
         System.out.println("Therad "+esteThread+" - ProcesarMensaje: "+comando+" "+codigo+" Comienzo "+" IP:"+ip+" Caja: "+caja);
         solicitudes.Solicitud();
         if (mensaje.esping) {
             respuesta=mensaje.mensaje;
             solicitudes.Ping();
         }
         else if(mensaje.essistema) {
             respuesta="SIN RESPUESTA";
             if (comando.equals("POOL")) {
                 if (pool!=null) {
                     pool.darLog().mensaje("Comando Sistema: "+comando+" IP:"+ip);
                     System.out.println("Comando Sistema: "+comando+" IP:"+ip);
                     pool.restart();
                     respuesta="Ejecutado\n";
                 }
             } else if (comando.equals("POOL-S")) {
                 if (pool!=null) {
                     pool.darLog().mensaje("Comando Sistema: "+comando+" IP:"+ip);
                     System.out.println("Comando Sistema: "+comando+" IP:"+ip);
                     respuesta=pool.cuantasEnUso+" de "+pool.tamanoPool+" en uso. Max: "+pool.maxEnUso+"\n";
                 }
             }
              else if (comando.equals("ERROR")) {
                 if (pool!=null) {
                     pool.darLog().mensaje("Comando Sistema: "+comando+" IP:"+ip);
                     System.out.println("Comando Sistema: "+comando+" IP:"+ip);
                     respuesta=cupones.gUltError+"\n";
                 }
             }
             else if (comando.equals("CUPONES")) {
                 if (pool!=null) {
                     pool.darLog().mensaje("Comando Sistema: "+comando+" IP:"+ip);
                     System.out.println("Comando Sistema: "+comando+" IP:"+ip);
                     vouchersmonitor.forzarCarga();
                     respuesta=vouchersmonitor.darInfo()+"\n";
                 }
             }
             else if (comando.equals("CAMPANA")) {
                 if (pool!=null) {
                     pool.darLog().mensaje("Comando Sistema: "+comando+" IP:"+ip);
                     System.out.println("Comando Sistema: "+comando+" IP:"+ip);
                     campanas.forzarCarga();
                     respuesta="Campanas: "+campanas.darCantCampanas()+"\n";
                 }
             }
         }
         else {
             System.out.println("CODIGO: "+codigo);
             if (codigo.equals("CUPON")) {
                 solicitudes.Cupon();
                 validarCupon();
             } 
             else if (codigo.equals("COMUNICACION")) {
                 enviarComunicacion();
             }
             else if (codigo.equals("CUPONHAB")) {
                 solicitudes.CuponHab();
                 habilitarCupon();
             }
             else if (codigo.equals("CUPONOPC")) {
                 solicitudes.CuponOpcion();
                 redimirCuponOpciones();
             }
             else if (codigo.equals("CV-ALTA")) {
                 solicitudes.CVAlta();
                 altaCuponCV();
             }
             else if (codigo.equals("CV-REDIMIR")) {
                 solicitudes.CVRedimir();
                 validarCuponCV();
             }
             else if (codigo.equals("CV-FREC")) {
                 solicitudes.CVFreq();
                 frecuenciaCV();
             }
             else if (codigo.equals("CV-PAIS")) {
                 mensaje.agregarResp("1|"+gPais);
             }
             else if (codigo.equals("BENEFEMP")) {
                 solicitudes.Descuentos();
                 validarDescuentoEmpleado();
             }
             else if (codigo.equals("VOUCHER")) {
                 solicitudes.Voucher();
                 validarVoucher();
             }
             else if (codigo.equals("OFFLINE")) {
                 solicitudes.Offline();
                 enviarCampOffline();
             }
             else if (codigo.equals("OFFCOD")) {
                 solicitudes.CodOffline();
                 redimirCuponOffline();
             } else if (codigo.equals("OFF-FREC")) {
                 frecuenciaOffline();
             }
             else if (codigo.equals("CRED-SALDO")) {
                 solicitudes.Credito();
                 consultaSaldoCredito();
             }
             else if (codigo.equals("CRED-COMPRA")) {
                 solicitudes.Credito();
                 compraCredito();
             }
             else if (codigo.equals("CRED-MOV")) {
                 solicitudes.Credito();
                 consultaMovimientosCredito();
             }
             else if (codigo.equals("DATOS-PROM")) {
                 altaDatosPromocion();
             }
             else if (codigo.equals("INITPOS")) {
                 initPos();
             }
             else if (codigo.equals("PMS-1")) {
                 enviarPms(1);
             }
             else if (codigo.equals("PMS-2")) {
                 enviarPms(2);
             }
             else mensaje.agregarResp("-1|Funcion no encontrada");
             
             respuesta=mensaje.contruirRespuesta();
         }
         pool.darLog().mensaje("Therad "+esteThread+" - ProcesarMensaje: Fin "+" IP:"+ip);
         System.out.println("Therad "+esteThread+" - ProcesarMensaje: Fin "+" IP:"+ip);
         return respuesta;
     }
     
     void redimirCuponOpciones() {
        /* redimido=1 cupon ok
         redimido=2 cupon ya redimido
         redimido=0 cupon no encontrado
         redimido=3 cupon procesado en opciones
         
         tipos de campañas
         1	Codigo aplica producto
         2	Codigo cambia de pantalla
         3	Codigo aplica descuento
         */
         String caja=mensaje.darParam(1);
         String codigo=mensaje.darParam(2).toUpperCase();
         String version=mensaje.darParam(3);
         String codmicros=mensaje.darParam(5);
         String codmicrosnivel=mensaje.darParam(6,1);
         String diaNegocio=mensaje.darParam(7);
         
         if (gWebService) {
             codmicros=mensaje.darParam(3);
             codmicrosnivel=mensaje.darParam(4,1);
         } ;
         
         
         //System.out.println("CODIGO MICROS:"+codmicros);
         Codigos codigoutil=new Codigos(codigo,campanas,empresa);
         String campana=codigoutil.darCampana();
         Campana lacampana=campanas.darCampana(campana,empresa);
         int quemawebservice=lacampana.webservice;
         int limitediarioalta=lacampana.limitediarioalta;
         if (lacampana!=null) {
              
            if (lacampana.porstock) {                         
                int stock=cupones.redimirCuponStock(campana, codigo, tienda, caja,codmicros,codmicrosnivel,empresa,diaNegocio,0);
                //int codigomicros=lacampana.codigomicros;
               // if (version.equals("5.0")) codmicros=codmicros+"|"+codmicrosnivel;
                codmicros=codmicros+"|"+codmicrosnivel;
                int tipocampana=lacampana.tipo;
                if (stock>=0) {
                  if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                  mensaje.agregarResp("3|ok|"+tipocampana+"|"+codmicros+"|0|"+campanas.darVersionCampanas());
                } 
                else
                   mensaje.agregarResp("2|Cupon sin stock|"+tipocampana+"|"+codmicros+"|0|"+campanas.darVersionCampanas()); 

            }
            else
                if (lacampana.porstockindividual) {                         
                    int stock=cupones.redimirCuponStockIndividual(campana, codigo, tienda, caja,codmicros,codmicrosnivel,diaNegocio,0);
                    int tipocampana=lacampana.tipo;
                    if (stock>=0) {
                      if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                          mensaje.agregarResp("3|ok|"+tipocampana+"|"+codmicros+"|"+codmicrosnivel+"|0|"+campanas.darVersionCampanas());
                    } 
                    else if (stock==-1)
                       mensaje.agregarResp("2|Cupon ya redimido.Sin stock individual|"+tipocampana+"|"+codmicros+"|0|"+campanas.darVersionCampanas()); 
                    else if (stock==-2)
                       mensaje.agregarResp("2|No existe el cupon ingresado|"+tipocampana+"|"+codmicros+"|0|"+campanas.darVersionCampanas()); 


                }
            else {
                if (lacampana.poralta) {
                    //cupones.altaCupon(campana,codigo,tienda,caja);
                    if (limitediarioalta>0) {
                        cupones.redimirCuponPorAltaInmediato(campana,codigo,tienda,caja,codmicros+"",codmicrosnivel+"",diaNegocio,0); 
                    } else 
                        cuponesmonitor.enconlarCuponxAlta(campana,codigo,tienda,caja,"0",codmicros,codmicrosnivel,diaNegocio,0);
                }
                //else cupones.redimirCupon(campana,codigo,tienda,caja,lacampana.codigomicros+"");
                else cupones.redimirCupon(campana,codigo,tienda,caja,codmicros,codmicrosnivel,diaNegocio,0);
                if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                mensaje.agregarResp("3|ok|0|0|0|"+campanas.darVersionCampanas());
            };
         }
     
     }
     
     void redimirCuponOffline() {
        
         String caja=mensaje.darParam(1);
         String codigo=mensaje.darParam(2).toUpperCase();
         String diaNegocio=mensaje.darParam(4);
         int esapp=0;
         if (codigo.indexOf("/")>=0) {
             codigo=codigo.substring(codigo.indexOf("/")+1,codigo.length());
         } else if (codigo.startsWith("APP-")) {
             codigo=codigo.substring(4,codigo.length());
             esapp=1;
         }
         Codigos codigoutil=new Codigos(codigo,campanas,empresa);
         String campana=codigoutil.darCampana(); 
         Campana lacampana=campanas.darCampana(campana,empresa);
         int quemawebservice=lacampana.webservice;
         System.out.println("OFFLINE: "+codigo);
         cuponesmonitor.enconlarCuponxAlta(campana,codigo,tienda,caja,"1",lacampana.codigomicros+"",lacampana.codigomicrosnivel+"",diaNegocio,esapp);
         if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);

         mensaje.agregarResp("3|ok|0|0|0|"+campanas.darVersionCampanas());
    }
     
     void validarCupon() {
        /* redimido=1 cupon ok
         redimido=2 cupon ya redimido
         redimido=0 cupon no encontrado
         redimido=3 cupon vencido
         
         tipos de campañas
         1	Codigo aplica producto
         2	Codigo cambia de pantalla
         3	Codigo aplica descuento
         */
         String caja=mensaje.darParam(1);
         String codigo=mensaje.darParam(2).toUpperCase();
        int esapp=0;
         if (codigo.indexOf("/")>=0) {
             codigo=codigo.substring(codigo.indexOf("/")+1,codigo.length());
         } else if (codigo.startsWith("APP-")) {
             codigo=codigo.substring(4,codigo.length());
             esapp=1;
         }
         String version=mensaje.darParam(3);
         String modelocaja=mensaje.darParam(4);
         String rvc=mensaje.darParam(6);
         String diaNegocio=mensaje.darParam(7);
         boolean usoteclado=true;
         if (mensaje.darParam(5).contains("1")) usoteclado=false;
         //Cupones cupones=new Cupones(pool,campanas);
         Codigos codigoutil=new Codigos(codigo,campanas,empresa);
         String campana=codigoutil.darCampana();
         //System.out.println("CAMPANA: "+campana);
         Campana lacampana=campanas.darCampana(campana,empresa);
         
         double versionaux=campanas.darVersionIsl(empresa);
         double versionpos=0;
         if (gWebService) {
             versionpos=versionaux;
         } else {
         try {
             versionpos=Double.parseDouble(version);
         } catch (Exception e) {
             e.printStackTrace();
         }
         };
         if (versionpos>=versionaux) { 
            cuponesmonitor.enconlarInventario(empresa, tienda,caja,version,modelocaja,rvc,"0");
            if (lacampana!=null) {
               
               //cupones.actualizarInventarioCaja(empresa,tienda,caja,version,modelocaja,rvc);
               int tipocampana=lacampana.tipo;
               int codigomicros=lacampana.codigomicros;
               int codigomicrosnivel=lacampana.codigomicrosnivel;
               boolean porstock=lacampana.porstock;
               boolean poralta=lacampana.poralta;
               boolean porstockindividual=lacampana.porstockindividual;
               boolean porbanda=lacampana.solobanda;
               boolean validarduplicado=lacampana.validarduplicado;
               int quemawebservice=lacampana.webservice;
               int codigolen=lacampana.codigolen;
               int porrvc=lacampana.rvc;
               int portienda=lacampana.tienda;
               boolean pordia=lacampana.validadia;
               int limitediarioalta=lacampana.limitediarioalta;
              // System.out.println("Campana="+campana+" codigomicros="+codigomicros);
               if (((codigolen!=0) && (codigolen==codigo.length())) || (codigolen==0) || (campana.equals("EVK"))) {

                   if ((porrvc!=0) && (!(rvc.equals(porrvc+"")))) {
                     mensaje.agregarResp("0|Este cupon no puede ser utilizado en este RVC|0|0|0|"+campanas.darVersionCampanas());     
                   }
                   else
                   if ((portienda!=0) && (!(tienda.equals(portienda+"")))) {
                     mensaje.agregarResp("0|Este cupon no puede ser utilizado en esta TIENDA|0|0|0|"+campanas.darVersionCampanas());     
                   }
                   else
                   if (porbanda && usoteclado) {
                      //solo deslizando tarjeta
                     mensaje.agregarResp("0|Debe deslizar tarjeta para esta promocion|0|0|0|"+campanas.darVersionCampanas());  
                   }
                   else
                   if (pordia && !campanas.validarDia(campana)) {
                     mensaje.agregarResp("0|Promocion no valida para el día de hoy|0|0|0|"+campanas.darVersionCampanas());   
                   }
                   else 
                   if (porstock) {
                      //lacampana=campanas.darCampanaStock(codigo); 
                      //if (lacampana!=null) {
                          if (tipocampana==4)  {//opciones de codigos
                            String opciones=cupones.darOpcionesCupon(campana,version);
                            //if (version.equals("5.0"))
                                mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas()+opciones);
                           // else
                               // mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas()+opciones);
                          }      
                          else {
                          
                            int stock=cupones.redimirCuponStock(campana, codigo, tienda, caja,lacampana.codigomicros+"",lacampana.codigomicrosnivel+"",empresa,diaNegocio,esapp);
                            codigomicros=lacampana.codigomicros;
                            codigomicrosnivel=lacampana.codigomicrosnivel;
                            if (stock>=0) {
                              if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                             // if (version.equals("5.0"))
                                mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas());
                              //else 
                             //   mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas());
                            } 
                            else
                               mensaje.agregarResp("2|Cupon sin stock|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas()); 
                          };
                      /*}
                      else  {
                          //el cupón no existe.
                        mensaje.agregarResp("0|Codigo invalido o promocion inactiva|0|0|0|"+campanas.darVersionCampanas());
                      }*/
                   } else
                    if (porstockindividual) {
                          if (tipocampana==4)  {//opciones de codigos
                            String opciones=cupones.darOpcionesCupon(campana,version);
                                mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas()+opciones);
                           }  
                          else {
                            int stock=cupones.redimirCuponStockIndividual(campana, codigo, tienda, caja,lacampana.codigomicros+"",lacampana.codigomicrosnivel+"",diaNegocio,esapp);
                            codigomicros=lacampana.codigomicros;
                            codigomicrosnivel=lacampana.codigomicrosnivel;
                            //tipocampana=1;
                            if (stock>=0) {
                              if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                                  mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas());
                            } 
                            else
                               mensaje.agregarResp("2|Cupon ya redimido.Sin stock individual|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas()); 
                          }

                   }
                   else if (poralta) {
                       boolean customer=true;
                       boolean duplicado=false;
                       boolean limitediariook=true;
                       if (limitediarioalta>0) {
                            limitediariook=!(cupones.darCuponesUtilizadosDia(codigo)>=limitediarioalta);
                       }
                       if (validarduplicado) {
                           Cupon uncupon=new Cupon(codigo);
                           duplicado=(cupones.cuponRedimido(campana, uncupon, ip,lacampana)==2);
                       }
                       if (limitediariook) {
                        if (!duplicado) {
                            if (campana.equals("EVK")) { //Customer Voice BK
                               customer=validarEvaluaBK(codigo);
                            }
                            if (!customer) {
                                mensaje.agregarResp("0|Codigo EvaluaBK incorrecto o vencido|0|0|0|"+campanas.darVersionCampanas());
                            } else
                                 if (tipocampana==4)  {//opciones de codigos
                                     String opciones=cupones.darOpcionesCupon(campana,version);
                                     //if (version.equals("5.0"))
                                         mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas()+opciones);
                                    // else
                                      //   mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas()+opciones);
                                     //else mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0"+opciones);
                                 } else {
                                     if (limitediarioalta>0) {
                                        cupones.redimirCuponPorAltaInmediato(campana,codigo,tienda,caja,lacampana.codigomicros+"",lacampana.codigomicros+"",diaNegocio,esapp); 
                                     } else 
                                        cuponesmonitor.enconlarCuponxAlta(campana,codigo,tienda,caja,"0",lacampana.codigomicros+"",lacampana.codigomicrosnivel+"",diaNegocio,esapp);
                                     if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                                     //cupones.altaCupon(campana,codigo,tienda,caja);
                                     //cuponesmonitor.enconlarCuponWebService(codigo,tienda); //SACAR***
                                    // if (version.equals("5.0"))
                                         mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas());                                        
                                     //else
                                       //  mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas());
                                 }
                        } else {
                             mensaje.agregarResp("0|Codigo EvaluaBK ya utilizado|0|0|0|"+campanas.darVersionCampanas());
                        }
                       } else {
                           mensaje.agregarResp("0|Limite diario superado ("+limitediarioalta+")|0|0|0|"+campanas.darVersionCampanas());
                       }
                       //System.out.println("1|ok|"+tipocampana+"|"+codigomicros);

                   }
                   else { //cupon normal
                       //System.out.println("Prueba Cupones");
                       //mensaje.agregarResp("1|ok|4|101010|101111|2|Producto1|Codigo1|Producto2|Codigo2");
                       Cupon uncupon=new Cupon(codigo);
                       int status=cupones.cuponRedimido(campana,uncupon,ip,lacampana);
                       switch (status) {
                           case 1: 
                                  String opciones="";
                                  if (tipocampana==7) {//el codigo micros a aplicar es x cupon
                                      codigomicros=uncupon.darCodMicros();
                                      codigomicrosnivel=uncupon.darCodMicrosNivel();
                                      tipocampana=1; //lo convierto en campaña normal
                                  }
                                  if (tipocampana==4)  {//opciones de codigos
                                      opciones=cupones.darOpcionesCupon(campana,version);
                                  }
                                  else {
                                      cupones.redimirCupon(campana,codigo,tienda,caja,codigomicros+"",codigomicrosnivel+"",diaNegocio,esapp);
                                      if (quemawebservice>0)  cuponesmonitor.enconlarCuponWebService(codigo,tienda);
                                  }
                                  
                                  //System.out.println("1|ok|"+tipocampana+"|"+codigomicros);
                                  //mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0"+opciones);
                                 // if (version.equals("5.0"))
                                    mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|"+codigomicrosnivel+"|0|"+campanas.darVersionCampanas()+opciones);
                                 // else
                                   // mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas()+opciones);
                                  //else mensaje.agregarResp("1|ok|"+tipocampana+"|"+codigomicros+"|0"+opciones);
                                  break;
                           case 2:
                                  mensaje.agregarResp("2|Cupon ya redimido. "+cupones.gMensaje+"|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas());
                                  break;
                           case 3:
                                  mensaje.agregarResp("2|Cupon vencido. "+cupones.gMensaje+"|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas());
                                  break;
                           case 0: mensaje.agregarResp("0|Cupon No Encontrado|"+tipocampana+"|"+codigomicros+"|0|"+campanas.darVersionCampanas());
                                  break;
                       }

                   }
               }
               else mensaje.agregarResp("0|Cantidad de digitos incorrecta|0|0|0|"+campanas.darVersionCampanas());

            } else {
                //la campaña no existe o está inactiva
                mensaje.agregarResp("0|Promocion inexistente o inactiva|0|0|0|"+campanas.darVersionCampanas());
            }
         }
         else {
             mensaje.agregarResp("0|La version del sistema descuentos/codigos es incorrecta|0|0|0|"+campanas.darVersionCampanas()); 
             pool.darLog().mensaje("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
             System.out.println("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
        }
     }
     
     void consultaSaldoCredito() {
       
         String caja=mensaje.darParam(1);
         String tarjeta=mensaje.darParam(2).toUpperCase();
         String version=mensaje.darParam(3);
         String modelocaja=mensaje.darParam(4);
         String rvc=mensaje.darParam(6);
         boolean usoteclado=true;
         if (mensaje.darParam(5).contains("1")) usoteclado=false;
         
         double versionaux=campanas.darVersionIsl(empresa);
         double versionpos=0;
         try {
             versionpos=Double.parseDouble(version);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         Credito credito=new Credito(pool);
         
         if (versionpos>=versionaux) { 
            if (credito!=null) {
               boolean res=credito.darSaldo(tarjeta, tienda);
               int codresp=0;
               String desc="Error Interno, No hay Conexion|0|0|0";
               if (res) { //ok con busqueda de datos
                   switch (credito.gStatus) {
                       case -1: //tarjeta no encontrada
                                desc="Tarjeta NO encontrada|0|0|0";
                                break;
                       case 1: //tarjeta anulada
                                desc="Tarjeta ANULADA|0|0|0";
                                break;
                       case 2: desc="Tarjeta EXTRAVIADA|0|0|0";
                                break;
                       case 0: //estaria ok
                                if (credito.gVencido) {
                                    desc="Tarjeta VENCIDA|0|0|0";
                                }
                                else if (!credito.gMarca.equals(empresa)) {
                                    desc="Esta tarjeta no puede utilizarse en "+empresa+"|0|0|0";
                                }
                                else {
                                    //devuelvo saldo y codigos
                                    codresp=1;
                                    desc="ok|"+credito.gCodigoMicros+"|"+credito.gCodigoMicrosAdicional+"|"+credito.gSaldo;
                                }
                                break;
                                                 
                   };
                   mensaje.agregarResp(codresp+"|"+desc);
                   
               }
               
               
            } else {
                //error interno no pude crear Credtio
                mensaje.agregarResp("0|Error interno: No pude crear Credito-Saldo|0|0|0");
            }
         }
         else {
             mensaje.agregarResp("0|La version del sistema descuentos/codigos es incorrecta|0|0|0"); 
             pool.darLog().mensaje("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
             System.out.println("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
        }
     }
     void consultaMovimientosCredito() {
       
         String caja=mensaje.darParam(1);
         String tarjeta=mensaje.darParam(2).toUpperCase();
         String version=mensaje.darParam(3);
         String modelocaja=mensaje.darParam(4);
         String rvc=mensaje.darParam(6);
         boolean usoteclado=true;
         if (mensaje.darParam(5).contains("1")) usoteclado=false;
         
         double versionaux=campanas.darVersionIsl(empresa);
         double versionpos=0;
         try {
             versionpos=Double.parseDouble(version);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         Credito credito=new Credito(pool);
         
         if (versionpos>=versionaux) { 
            if (credito!=null) {
               boolean res=credito.ultimosMovimientos(tarjeta, tienda);
               int codresp=0;
               String desc="Error Interno, No hay Conexion|0|0|0";
               if (res) { //ok con busqueda de datos
                   switch (credito.gStatus) {
                       case -1: //tarjeta no encontrada
                                desc="Tarjeta NO encontrada|0|0|0";
                                break;
                       default: //estaria ok
                                if (!credito.gMarca.equals(empresa)) {
                                    desc="Esta tarjeta no puede utilizarse en "+empresa+"|0|0|0";
                                }
                                else {
                                    //devuelvo saldo y codigos
                                    codresp=1;
                                    desc="ok|0|0|0|"+credito.gMensaje;
                                }
                                break;
                                                 
                   };
                   mensaje.agregarResp(codresp+"|"+desc);
                   
               }
               
               
            } else {
                //error interno no pude crear Credtio
                mensaje.agregarResp("0|Error interno: No pude crear Credito-Movimientos|0|0|0");
            }
         }
         else {
             mensaje.agregarResp("0|La version del sistema descuentos/codigos es incorrecta|0|0|0"); 
             pool.darLog().mensaje("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
             System.out.println("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
        }
     }
     
     void compraCredito() {
       
         String caja=mensaje.darParam(1);
         String tarjeta=mensaje.darParam(2).toUpperCase();
         String version=mensaje.darParam(3);
         String modelocaja=mensaje.darParam(4);
         String monto=mensaje.darParam(5);
         
         double versionaux=campanas.darVersionIsl(empresa);
         double versionpos=0;
         try {
             versionpos=Double.parseDouble(version);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         Credito credito=new Credito(pool);

         if (versionpos>=versionaux) { 
            if (credito!=null) {
               boolean res=credito.consumirSaldo(tarjeta, tienda,caja,monto);
               int codresp=0;
               String desc="Error Interno, No hay Conexion|0|0|0";
               if (res) { //ok con busqueda de datos
                   switch (credito.gStatus) {
                       case -1: //tarjeta no encontrada
                                desc="Tarjeta NO encontrada|0|0|0";
                                break;
                       case 0: //estaria ok
                                //devuelvo saldo y codigos
                                    codresp=1;
                                    desc="ok|0|0|"+credito.gSaldo;
                                break;
                                                 
                   };
                   mensaje.agregarResp(codresp+"|"+desc);
                   
               }
               
               
            } else {
                //error interno no pude crear Credtio
                mensaje.agregarResp("0|Error interno: No pude crear Credito-Compra|0|0|0");
            }
         }
         else {
             mensaje.agregarResp("0|La version del sistema descuentos/codigos es incorrecta|0|0|0"); 
             pool.darLog().mensaje("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
             System.out.println("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
        }
     }
     
      void habilitarCupon() {
         String codigo=mensaje.darParam(2).toUpperCase();
         Codigos codigoutil=new Codigos(codigo,campanas,empresa);
         String campana=codigoutil.darCampana();
         System.out.println("Habilitar Cupon: "+codigo);
         cupones.habilitarCupon(codigo); 
         mensaje.agregarResp("0|Operacion Cancelada|0|0|0|0");
     }
     
     boolean validarEvaluaBK(String codigo) {
         boolean ok=false;
         
         String aux=codigo.substring(5,codigo.length());
         String mess=codigo.substring(3,5);
         int mesi=0;
         if (mess.equals("BB")) mesi=1;
         else if (mess.equals("LS")) mesi=2;
         else if (mess.equals("JH")) mesi=3;
         else if (mess.equals("PL")) mesi=4;
         else if (mess.equals("BK")) mesi=5;
         else if (mess.equals("WH")) mesi=6;
         else if (mess.equals("FF")) mesi=7;
         else if (mess.equals("BF")) mesi=8;
         else if (mess.equals("CF")) mesi=9;
         else if (mess.equals("CK")) mesi=10;
         else if (mess.equals("CB")) mesi=11;
         else if (mess.equals("VM")) mesi=12;
         
         int suma=0;
         try {
            
            for (int i=0;i<aux.length();i++) {
                suma=suma+(Integer.parseInt(aux.substring(i, i+1)));
           }
         } catch (Exception e) {
             e.printStackTrace();
         }
         if ((suma!=0) && (suma % 3)==0) ok=true; 
         if (ok) { //valido el mes
              
            Calendar ahora = Calendar.getInstance();
            int mesactual=ahora.get(Calendar.MONTH)+1;
            if ((mesactual-mesi)>1) ok=false;
            else if ((mesi==12) && ((mesactual>1) && (mesactual<12))) ok=false;
            else if (mesi>mesactual) ok=false;
         }
         return ok;
     }
     
     void altaDatosPromocion() {

         String caja=mensaje.darParam(1);
         String version=mensaje.darParam(2);
         String nombre=mensaje.darParam(3).toUpperCase();
         String apellido=mensaje.darParam(4).toUpperCase();
         String dni=mensaje.darParam(5);
         String mail=mensaje.darParam(6);
         String telefono=mensaje.darParam(7);
         String empresaprom=mensaje.darParam(8);
         String datostipo=mensaje.darParam(9);
         
         double versionaux=campanas.darVersionIsl(empresa);
         double versionpos=0;
         try {
             versionpos=Double.parseDouble(version);
         } catch (Exception e) {
             e.printStackTrace();
         }
         

         if (versionpos>=versionaux) { 
            DatosPromociones datos=new DatosPromociones(pool);
            int status=0;
            if (!mail.equals("")) {
                if (mail.indexOf("@")==0) status=5;
            }
            if (dni.length()<7) {
                status=6;
            }
            if (status==0) status=datos.altaDatosPromocion(nombre,apellido,dni,mail,telefono,empresaprom,datostipo, tienda,caja);

            switch (status) {
                       case 1: 
                              mensaje.agregarResp("1|ok");
                              break;
                       case 2:
                              mensaje.agregarResp("2|ERROR: DNI Duplicado, ya existente");
                              break;
                       case 0: mensaje.agregarResp("0|ERROR EN ALTA DE DATOS");
                              break;
                       case 3: mensaje.agregarResp("3|ERROR EN INSERT");
                              break;
                       case 4: mensaje.agregarResp("4|ERROR NO HAY COONEXION CON BD");
                              break;
                       case 5: mensaje.agregarResp("5|ERROR DIRECCION DE MAIL INCORRECTA");
                              break;
                       case 6: mensaje.agregarResp("6|ERROR DNI INCORRECTO. VALIDE");
                              break;
            };
         }
          else {
             mensaje.agregarResp("0|La version del sistema descuentos/codigos es incorrecta"); 
             pool.darLog().mensaje("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
             System.out.println("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
        }
     };   
     
     void validarDescuentoEmpleado() {
        /* redimido=1 cupon ok
         redimido=2 cupon ya redimido
         redimido=0 cupon no encontrado
         */
         String caja=mensaje.darParam(1);
         String empleadoid=mensaje.darParam(2).toUpperCase();
         String montoaux=mensaje.darParam(5).toUpperCase();
        // String auxmonto=mensaje.darParam(6);
         Double monto=0.0;
        String version=mensaje.darParam(3);
        String modelocaja=mensaje.darParam(4);
        String rvc=mensaje.darParam(6);
        String codigodescopcion=mensaje.darParam(7);
        if (codigodescopcion.equals("0")) codigodescopcion="";
        String cuantosprodticketstr=mensaje.darParam(8); //productos en el ticket
        int cuantosprodticket=0;
        String[] prodticket=null;
        String[] prodcant=null;
        double[] prodmonto=null;
        
        cuponesmonitor.enconlarInventario(empresa,tienda,caja,version,modelocaja,rvc,"0");
        //cupones.actualizarInventarioCaja(empresa,tienda,caja,version,modelocaja,rvc);
         try {
             monto=Double.parseDouble(montoaux);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Monto:"+montoaux);
         }
         if (!cuantosprodticketstr.equals("")) {
            try {
                cuantosprodticket=Integer.parseInt(cuantosprodticketstr);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("cuantosprodticketstr:"+cuantosprodticketstr);
            }
         }
         
         if (cuantosprodticket!=0) {
             prodticket=new String[cuantosprodticket];
             //if (version.equals("8.2")) {
             prodcant=new String[cuantosprodticket];
             prodmonto=new double[cuantosprodticket];
             //};
             for (int i=9;i<(9+cuantosprodticket);i++) {
                 //if (version.equals("8.2")) {
                     prodticket[i-9]=mensaje.darParam(9+(i-9)*3);
                     prodcant[i-9]=mensaje.darParam(10+(i-9)*3);
                     try {
                        prodmonto[i-9]=Double.parseDouble(mensaje.darParam(11+(i-9)*3));
                     } catch (Exception e) {
                         System.out.println("ERROR Desc Items Monto: "+mensaje.darParam(11+(i-9)*3)+" "+e.toString());
                         prodmonto[i-9]=0;
                     }
                // } else
                 //   prodticket[i-9]=mensaje.darParam(i);
             };
         }
         
         Empleados empleados=new Empleados(pool);
         EmpleadoRespuesta status=empleados.validarDescuentoEmpleado(empresa,empleadoid,monto,tienda,caja,codigodescopcion,cuantosprodticket,prodticket,prodcant,prodmonto);
         switch (status.status) {
                    case 1: 
                           String descvariable=status.descuentovariable+"";
                           try {
                            descvariable=Math.round(status.descuentovariable*100)+"";
                            if (descvariable.length()>2)
                                descvariable=descvariable.substring(0,descvariable.length()-2)+"."+descvariable.substring(descvariable.length()-2,descvariable.length());
                           } catch (Exception e) {
                               descvariable=status.descuentovariable+"";
                               e.printStackTrace();
                           }
                            mensaje.agregarResp("1|ok|"+status.tipo+"|"+status.codigo+"|"+status.nombre+"|"+status.codadicional+"|"+descvariable+"|"+status.tamano+"|"+status.opciones);
                           break;
                    case 2:
                           mensaje.agregarResp("2|ERROR: Empleado no encontrado.|0|0|0");
                           break;
                    case 0: mensaje.agregarResp("0|ERROR de conexion 0.|0|0|0");
                           break;
                    case 3: mensaje.agregarResp("3|ERROR: Limite diario superado.|0|0|0");
                           break;
                    case 4: mensaje.agregarResp("4|ERROR: Limite mensual superado.|0|0|0");
                           break;
                    case 5: mensaje.agregarResp("5|ERROR: El ticket contiene productos promocionales.|0|0|0");
                           break;
                    case 6: mensaje.agregarResp("6|ERROR: No existen productos a descontar.|0|0|0");
                           break;
         };
     };   
     
     void enviarCampOffline() {
        
        String caja=mensaje.darParam(1);
        String version=mensaje.darParam(2);
        String modelocaja=mensaje.darParam(3);
        String rvc=mensaje.darParam(4);
        
        System.out.println("OFFLINE: Envio datos fuera de linea: "+ip+"  caja: "+caja+" version campanas: "+campanas.darVersionCampanas());
        pool.darLog().mensaje("OFFLINE: Envio datos fuera de linea: "+ip+"  caja: "+caja+" version campanas: "+campanas.darVersionCampanas());
        //cuponesmonitor.enconlarInventario(empresa,tienda,caja,version,modelocaja,rvc);
        String campanasOffline=campanas.darCampanasOffline(empresa,version);
        mensaje.agregarResp(campanasOffline);
        
     };  
     
     void initPos() {
        
        String caja=mensaje.darParam(1);
        String version=mensaje.darParam(2);
        String modelocaja=mensaje.darParam(3);
        String rvc=mensaje.darParam(4);
        
        pool.darLog().mensaje("INITPOS: Inicio de pos: "+ip+"  caja: "+caja+" modelo: "+modelocaja);
        cuponesmonitor.enconlarInventario(empresa,tienda,caja,version,modelocaja,rvc,"1");
        mensaje.agregarResp("0|0|0|0");
     }; 
     
     void validarVoucher() {
        /* "VOUCHER|",@WSID,"|",gVersion,"|",gCaja,"|",@RVC,"|",@ttldue,"|",@trans_number,"|",cantItems,items
         */
        String caja=mensaje.darParam(1);
        String montoaux=mensaje.darParam(5);
        String transaux=mensaje.darParam(6); 
        Double monto=0.0;
        int trans=0;
        String version=mensaje.darParam(2);
        String modelocaja=mensaje.darParam(3);
        String rvc=mensaje.darParam(4);
        int rvcint=0;
        String cantitemsaux=mensaje.darParam(7);
        int cantitems=0;
        //cupones.actualizarInventarioCaja(empresa,tienda,caja,version,modelocaja,rvc);
         try {
             monto=Double.parseDouble(montoaux);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Monto:"+montoaux);
         }
         try {
             trans=Integer.parseInt(transaux);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Trans:"+transaux);
         }
         try {
             cantitems=Integer.parseInt(cantitemsaux);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Cant Items:"+cantitemsaux);
         }
         try {
             rvcint=Integer.parseInt(rvc);
         } catch (Exception e) {
             e.printStackTrace();
             System.out.println("Rvc:"+rvc);
         }
         Vector productos=new Vector(cantitems);
         for (int i=0;i<cantitems;i++)
             productos.addElement(new Integer(mensaje.darParam(8+i)));
        /* empresa="BK";
         tienda="73";
         caja="101";
         * */
         Vouchers vouchers=new Vouchers(vouchersmonitor,empresa,tienda,caja);
         if (vouchers.generaCupones()) {
            mensaje.agregarResp(vouchers.procesarReglas(empresa,monto,rvcint,trans,productos));
         }
         else mensaje.agregarResp("0|0|0|0");
                
     };    
     
     void validarCuponCV() {
        /* redimido=1 cupon ok
         redimido=2 cupon ya redimido
         redimido=0 cupon no encontrado
         */
         String caja=mensaje.darParam(1);
         String codigocv=mensaje.darParam(2).toUpperCase();
         String codigopremio=mensaje.darParam(3).toUpperCase();
        
         int status=cupones.cuponCVRedimido( codigocv,codigopremio);
        
         switch (status) {
                    case 1: 
                           Vector resp=cupones.redimirCuponCV(codigocv,codigopremio,tienda,caja);
                           String beneficio=(String)resp.elementAt(0);
                           int frecuencia=((Integer)resp.elementAt(1)).intValue();//cupones.gFrecuenciaCV;
                           mensaje.agregarResp("1|ok|1|"+beneficio+"|"+frecuencia);
                           break;
                    case 2:
                           mensaje.agregarResp("2|ERROR: El cupon ingresado ya fue utilizado.|0|0|0|0");
                           break;
                    case 0: mensaje.agregarResp("0|ERROR: El codigo de cupon es invalido o no se encuentra.|0|0|0|0");
                           break;
                    case 3: mensaje.agregarResp("3|ERROR: El cupon se encuentra vencido.|0|0|0|0");
                           break;
         }
                 
     }
     
     void altaCuponCV() {
        // status=0 error conexion
        // status=1 ok alta
        // status=2 tienda no encontrada
        // status=3 error insert
        // status=4 cupon duplicado al agregar
        String caja=mensaje.darParam(1);
        String codigo=mensaje.darParam(2).toUpperCase();
        String version=mensaje.darParam(3);
        String modelocaja=mensaje.darParam(4);
        String rvc=mensaje.darParam(6);
        cuponesmonitor.enconlarInventario(empresa,tienda,caja,version,modelocaja,rvc,"0");
        //cupones.actualizarInventarioCaja(empresa,tienda,caja,version,modelocaja,rvc);
        
        double versionaux=campanas.darVersionIsl(empresa);
        double versionpos=0;
        try {
            versionpos=Double.parseDouble(version);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (versionpos>=versionaux) { 
        
           
            Vector resp=cupones.altaCuponCV( codigo, tienda, caja); 
            int status=((Integer)(resp.elementAt(0))).intValue();
            int frecuencia=((Integer)(resp.elementAt(1))).intValue();//cupones.gFrecuenciaCV;
            String tiendaCV=(String)resp.elementAt(2);//cupones.gTiendaCV;
            switch (status) {
                case 0:  mensaje.agregarResp("0|Error en sistema central BD|0|0|0|0|"+tiendaCV);
                        break;
                case 1:mensaje.agregarResp("1|ok|0|0|0|"+frecuencia+"|"+tiendaCV);
                        break;
                case 2:mensaje.agregarResp("0|Tienda no encontrada|0|0|0|0|"+tiendaCV);
                        break;
                case 3:mensaje.agregarResp("0|Error al grabar codigo BD|0|0|0|0|"+tiendaCV);
                        break;
                case 4:mensaje.agregarResp("4|Cupon duplicado en BD|0|0|0|0|"+tiendaCV);
                        break;
            }
        }
        else {
             mensaje.agregarResp("0|La version del sistema descuentos/codigos es incorrecta|0|0|0|0|0"); 
             pool.darLog().mensaje("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
             System.out.println("Version ISL incorrecta: "+empresa+" "+tienda+" "+caja+" "+version);
        }
     }
     
     void mailAlertaReinicio(String caja) {
         if (empresa.equals("BK") && tienda.equals("36")) {
             MailUtil mail=new MailUtil("192.168.30.203");
             Vector dest=new Vector(2);
             dest.addElement("gaston.casanova@burgerking.com.ar");
             dest.addElement("rhirsch@alsea.com.ar");
             try {
                   mail.mandarMailMultipleConUser( dest, "Reinicio de Caja","Se reinicio la caja: "+ caja, false, "", "");
                 //mail.mandarMailMultiple2("dispositivo@alsea.com.ar", dest, "Reinicio de Caja","Se reinicio la caja: "+ caja, false, "", "");
             } catch (Exception e)
             {
                 e.printStackTrace();
             }                
         }
     }
     
     void frecuenciaCV() {
        // status=0 error conexion
        // status=1 ok alta
        // status=2 tienda no encontrada
        String caja=mensaje.darParam(1);
        String codigo=mensaje.darParam(2).toUpperCase();
        String version=mensaje.darParam(3);
        String modelocaja=mensaje.darParam(4);
        String rvc=mensaje.darParam(6);
        cuponesmonitor.enconlarInventario(empresa,tienda,caja,version,modelocaja,rvc,"1");
        
       // mailAlertaReinicio(caja);
        
        //cupones.actualizarInventarioCaja(empresa,tienda,caja,version,modelocaja,rvc);
        if (empresa.equals("SBX")) {
            Vector resp=cupones.darFrecuenciaCV( tienda); 
            int status=((Integer)resp.elementAt(0)).intValue(); 
            int frecuencia=((Integer)resp.elementAt(01)).intValue();//cupones.gFrecuenciaCV;
            switch (status) {
                case 0:  mensaje.agregarResp("0|Error en sistema central BD|0|0|0|0");
                        break;
                case 1:mensaje.agregarResp("1|ok|0|0|0|"+frecuencia);
                        break;
                case 2:mensaje.agregarResp("0|Tienda no encontrada|0|0|0|0");
                        break;
            }
        }
        else mensaje.agregarResp("1|ok|0|0|0|0");
     }
     
     void enviarComunicacion() {
        String idcomaux=mensaje.darParam(1);
        String caja=mensaje.darParam(2);
        String version=mensaje.darParam(3);
        String modelocaja=mensaje.darParam(4);
        String rvc="-1";
        int idcom=0;
        try {
            idcom=Integer.parseInt(idcomaux);
        } catch (Exception e) {};
        
        cuponesmonitor.enconlarInventario(empresa,tienda,caja,version,modelocaja,rvc,"0");
        
        int ultcom=comunicacionesmonitor.darIdComunicacion(empresa);
        
        if (idcom<ultcom) {
            mensaje.agregarResp("1|"+ultcom+"|"+comunicacionesmonitor.darComunicacion(empresa));
        }
        else {
           mensaje.agregarResp("0|"+ultcom+"||"); 
        }       
    }
     
     
     void frecuenciaOffline() {

        String caja=mensaje.darParam(1);
        mensaje.agregarResp("1|7200");
        
     }
     
     void enviarPms(int parte) {
        
        String caja=mensaje.darParam(1);
        String version=mensaje.darParam(2);
        String pms="NOPMS";
        int cantlineas=0;
       
        double versionactual=campanas.darVersionActualIsl(empresa);
        double versionpos=0;
        try {
             versionpos=Double.parseDouble(version);
         } catch (Exception e) {
             e.printStackTrace();
        }
        if (versionpos<versionactual) { 
        
        
            String lineas[]=new String[1600];
            cantlineas=cargarPms(lineas);
            int cuantos=0;
            //armo el string
            if (cantlineas>0) {

                int desde=(parte-1)*800;
                int hasta=800;
                if (cantlineas<800) hasta=cantlineas;
                if (parte==2) hasta=cantlineas;
                if (desde<hasta) cuantos=hasta-desde; 
                pms=cuantos+"";
                for (int i=desde;i<hasta;i++) {
                    pms=pms+"!"+lineas[i];
                   // System.out.println(i+" "+lineas[i]);
                }
            };
            //pms=pms+"! ";

            System.out.println("LARGO PMS: "+pms.length()+" Lineas:"+cantlineas);
        };
        mensaje.agregarResp(pms);//pms.substring(0,20000));
       // System.out.println(pms);
     }
     
      int cargarPms(String[] lineas) {
        String res="0!NOPMS";
        int cantlineas=0;
        try {
            File aux=new File("pms5.isl");
            int cuantos=0;
            if (aux!=null) {
                String str="";
                res="";
                BufferedReader br =new BufferedReader(new FileReader(aux));
                while ((str = br.readLine()) != null)
                {
                    if (str!=null) {
                        lineas[cantlineas]=str;
                        cantlineas++;
                    }
                    //else System.out.println("*****LINEA VACIA****");
                }
                br.close();
                
            }
            else System.out.println("cargarPMS null");
        } catch (Exception e) {
            cantlineas=0;
           
            e.printStackTrace();
        }
        
        return cantlineas;
    }
     
     MensajeISL mensaje=null;
     String tienda="";
     String empresa="";
     String ip="";
     ConnectionPool pool=null;
     Campanas campanas=null;
     Cupones cupones=null;
     VouchersMonitor vouchersmonitor=null;
     CuponesMonitor cuponesmonitor=null;
     Solicitudes solicitudes=null;
     ComunicacionesMonitor comunicacionesmonitor=null;
     int gPais=49; //argentina
     boolean gWebService=false;
     BufferedWriter gBufferRespuesta=null;
     Tareas gClienteTareas=null; 
}
