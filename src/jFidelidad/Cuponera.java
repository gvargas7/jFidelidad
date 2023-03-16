/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Calendar;
import java.util.Vector;

/**
 *
 * @author rhirsch
 */
public class Cuponera {
    
   
    public Cuponera(Log ellog,Vector textos,VoucherPrinter printer,String codigocupon,int vigencia) {
        boolean reintentar=true;
        int reintentos=0;
        log=ellog;
        boolean conecto=true;
        ip=printer.darIpCuponera();
        while ((conecto) && ((reintentar) )) {
            conecto=conectar(ip);
            try {
                if (output!=null) {
                    System.out.println("Cuponera Reintento="+reintentos+" sec="+printer.secuencia);
                    log.mensaje("Cuponera Reintento="+reintentos+" sec="+printer.secuencia);
                   // System.out.println("Logo");
                    imprimirLogoHasar(printer.secuencia);
                    //System.out.println("Codigo de Barra");
                    imprimirCodigoBarraHasar(codigocupon);
                    //System.out.println("TExtos");
                    imprimirTextosHasar(textos,vigencia);
                    //System.out.println("Crc final");
                    crcFinalHasar();

                    if (input!=null) {
                        int car=0;
                        int cuantos=0;
                        while ((cuantos<5) && (car=input.read())!=0) { //hasar (cuantos<5)
                          //System.out.println(cuantos+" "+car);
                           cuantos++;  
                        }
                        System.out.append("cuantos="+cuantos);
                        if (cuantos==3) reintentar=false; //epson  if (cuantos!=4) reintentar=false;
                        //reintentos++;
                        
                    }
                    if (reintentar) {
                        try {
                           this.wait(1500); 
                        } catch (Exception e2) {
                        
                        }
                    }
                     reintentos++;
                  //reintentar=false;
                    printer.incSecuencia();
                   // System.out.println("Termine1");
                    input.close();
                    output.close();
                    //System.out.println("Termine2");
                     if (reintentos>5) reintentar=false;
                }
                else reintentar=false;
            } catch (Exception e) {
                e.printStackTrace();
                reintentar=false;
                errorImpresion=true;
                log.mensaje("Cuponera Error Impresion="+ip+" sec="+printer.secuencia+"  "+e.toString());
            }
        };
    }
    
    int incSecuencia(int secuencia) {
        if (secuencia<20) secuencia=129;
        else  secuencia++;
        if (secuencia>255) secuencia=129;
        return secuencia;
    }
    
    void consultarEstado(OutputStream output) {
        try {
            output.write((byte) 95);
            output.write((byte) 131);
            output.write((byte) 115);
            
            output.write((byte) 246);
            output.write((byte) 128);
            
            output.write((byte) 0);
            
            output.flush();
            //System.out.println("Escuchando respuesta");
           
           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     int mandarTexto(OutputStream output,String texto,boolean plano,boolean derecha,boolean centrado,boolean doble,boolean cuadruple,boolean negro,boolean inverso) {
        int calccrc=0;
        try {
            if (derecha || centrado || doble || cuadruple || negro || inverso) {
           // if (!plano) {
                output.write((byte) 27);
                calccrc+=27;
                if (derecha) {
                    output.write((byte) 0x52);
                    calccrc+=0x52;
                }
                if (centrado) {
                    output.write((byte) 0x43);
                    calccrc+=0x43;
                }
                if (doble) {
                    output.write((byte) 0x32);
                    calccrc+=0x32;
                }
                if (cuadruple) {
                    output.write((byte) 0x34);
                    calccrc+=0x34;
                }
                if (negro) {
                    output.write((byte) 0x42);
                    calccrc+=0x42;
                }
                if (inverso) {
                    output.write((byte) 0x49);
                    calccrc+=0x49;
                }
                output.write((byte) 27);
                calccrc+=27;
            }
            byte aux[]=texto.getBytes("CP437");
           // System.out.println("Largo bytes:"+aux.length);
            for (int i=0;i<aux.length;i++) {
                output.write((byte) aux[i]);
             //   System.out.println("i: "+i+" "+(byte) aux[i]+" "+calccrc);
                calccrc+=(byte)(aux[i]);
            }
            
            output.write((byte) 0x0A); //fin de linea
            calccrc+=10;
            //System.out.println("Mandar texto crc:"+calccrc);
        } catch (Exception e) {
            log.mensaje("Cuponera: mandarTexto: Error Ip:"+ip);
            e.printStackTrace();
            errorImpresion=true;
            
        }
        return calccrc;
    }
     int mandarTextoHasar(OutputStream output,String texto,boolean plano,boolean derecha,boolean centrado,boolean doble,boolean cuadruple,boolean negro,boolean inverso) {
        int calccrc=0;
        try {
            if (derecha || centrado || doble || cuadruple || negro || inverso) {
           // if (!plano) {
                output.write((byte) 27);
                calccrc+=27;
                if (derecha) {
                    output.write((byte) 'R');
                    calccrc+='R';
                }
                if (centrado) {
                    output.write((byte) 'C');
                    calccrc+='C';
                }
                if (doble) {
                    output.write((byte) '2');
                    calccrc+='2';
                }
                if (cuadruple) {
                    output.write((byte) '4');
                    calccrc+='4';
                }
                if (negro) {
                    output.write((byte) 'B');
                    calccrc+='B';
                }
                if (inverso) {
                    output.write((byte) 'I');
                    calccrc+='I';
                }
                output.write((byte) 27);
                calccrc+=27;
            }
            byte aux[]=texto.getBytes("CP437");
           // System.out.println("Largo bytes:"+aux.length);
            for (int i=0;i<aux.length;i++) {
                output.write((byte) aux[i]);
             //   System.out.println("i: "+i+" "+(byte) aux[i]+" "+calccrc);
                calccrc+=(byte)(aux[i]);
            }
            
            output.write((byte) 0x0A); //fin de linea
            calccrc+=10;
            //System.out.println("Mandar texto crc:"+calccrc);
        } catch (Exception e) {
            log.mensaje("Cuponera: mandarTexto: Error Ip:"+ip);
            e.printStackTrace();
            errorImpresion=true;
            
        }
        return calccrc;
    }
     void imprimirCodigoBarra(String codigo) {
         try {
            if (codigo.equals("")) codigo="0";
            output.write((byte) 72);//tipo codigio barras antes 65
            output.write((byte) codigo.length());//cant digitos
            crc+=72+codigo.length(); //antes 76
            for (int i=0;i<codigo.length();i++){ //datos del codigo
                output.write((byte) codigo.charAt(i));
                //System.out.println(codigo.charAt(i)+" "+(byte) codigo.charAt(i));
                crc+=(byte) codigo.charAt(i);
            }
         } catch (Exception e) {
             log.mensaje("Cuponera: imprimirCodigoBarra: Error Ip:"+ip);
             e.printStackTrace();
             errorImpresion=true;
         }
     }
     void imprimirCodigoBarraHasar(String codigo) {
         try {
            if (codigo.equals("")) codigo="000000000000";
            output.write((byte) 69);//tipo codigio barras antes 65
            if (codigo.length()==2 || codigo.length()==3 || codigo.length()==16) {
               output.write((byte) 16);
               crc+=16;
            }
            codigo="BURGER";
            output.write((byte) codigo.length());//cant digitos
            crc+=69+codigo.length(); //antes 65+
            for (int i=0;i<codigo.length();i++){ //datos del codigo
                output.write((byte) codigo.charAt(i));
                //System.out.println(codigo.charAt(i)+" "+(byte) codigo.charAt(i));
                crc+=(byte) codigo.charAt(i);
            }
         } catch (Exception e) {
             log.mensaje("Cuponera: imprimirCodigoBarra: Error Ip:"+ip);
             e.printStackTrace();
             errorImpresion=true;
         }
     }
      void imprimirLogo(int secuencia) {
         crc=0;
        try {
            output.write((byte) 95); //header
            output.write((byte) secuencia); //secuencia /de 129 a 255
            crc+=secuencia;
            output.write((byte) 67); //comando
            crc+=67;
            output.write((byte) 1);//logo producto
            output.write((byte) 128); //chksum logo 1
            output.write((byte) 128); //chksum logo 2
            crc+=257;
            output.write((byte) 1);//logo empresa
            output.write((byte) 128); //chksum logo 1
            output.write((byte) 128); //chksum logo 2
            crc+=257;
            
            
         

        } catch (Exception e) {
             log.mensaje("Cuponera: imprimirLogo: Error Ip:"+ip);
            e.printStackTrace();
        }
    }
      
      void imprimirLogoHasar(int secuencia) {
         crc=0;
        try {
            output.write((byte) 2); //header
            output.write((byte) secuencia); //secuencia /de 129 a 255
            crc+=2+secuencia;
            output.write((byte) 'C'); //comando
            crc+='C';
            output.write((byte) 1);//logo producto
            output.write((byte) 66); //chksum logo 1
            output.write((byte) 242); //chksum logo 2
            crc+=1+66+242;
            output.write((byte) 1);//logo empresa
            output.write((byte) 66); //chksum logo 1
            output.write((byte) 242); //chksum logo 2
            crc+=1+66+242;
            
            
         

        } catch (Exception e) {
             log.mensaje("Cuponera: imprimirLogo: Error Ip:"+ip);
            e.printStackTrace();
            errorImpresion=true;
        }
    }
      
      void crcFinal() {
          try {
            byte crc1=(byte)(crc % 256);
            if ((crc1>=0) && (crc1<128)) crc1+=128;
            byte crc2=(byte) (crc /256);
            if ((crc2>=0) && (crc2<128)) crc2+=128;
            output.write((byte) crc1); //crc 1
            output.write((byte) crc2); //crc 2
            
            output.write((byte) 0);
            //System.out.println("crc: "+crc+"  crc1:"+crc1+"  crc2:"+crc2);
            output.flush();
          } catch (Exception e){
              e.printStackTrace();
              errorImpresion=true;
          }
      }
      
      void crcFinalHasar() {
          try {
              
            crc+=3;
            output.write((byte)3); //ETX
            
            int crc1= (crc % 256);
            byte crc2=(byte) (256-crc1);
            output.write((byte) crc2); 
            output.flush();  
              
              
           
            //System.out.println("crc: "+crc+"  crc1:"+crc1+"  crc2:"+crc2);
            //output.flush();
          } catch (Exception e){
              e.printStackTrace();
              errorImpresion=true;
          }
      }
      void imprimirTextos(Vector textos,int vigencia) {
        
        try {
            String fechavigencia=darFechaVigencia(vigencia);
            
            output.write((byte) textos.size()); //cant lineas de texto
            crc+=textos.size();
           // System.out.println("crc antes:"+crc);
            for (int i=0;i<textos.size();i++) {
                VoucherForm linea=(VoucherForm) textos.elementAt(i);
                crc+=mandarTexto(output,reemplazarTexto(linea.texto,"%vigencia",fechavigencia),linea.estilo==0,linea.justificacion==2,linea.justificacion==1,linea.tamano==1,linea.tamano==2,linea.estilo==1,linea.estilo==2);
            }
           
        } catch (Exception e) {
             log.mensaje("Cuponera: imprimirTextos: Error Ip:"+ip);
            e.printStackTrace();
        }
    }
      
       void imprimirTextosHasar(Vector textos,int vigencia) {
        
        try {
            String fechavigencia=darFechaVigencia(vigencia);
            if (textos.size()==2 || textos.size()==3 || textos.size()==16) {
               output.write((byte) 16);
               crc+=16;
            }
            output.write((byte) textos.size()); //cant lineas de texto
            crc+=textos.size();
           // System.out.println("crc antes:"+crc);
            for (int i=0;i<textos.size();i++) {
                VoucherForm linea=(VoucherForm) textos.elementAt(i);
                crc+=mandarTexto(output,reemplazarTexto(linea.texto,"%vigencia",fechavigencia),linea.estilo==0,linea.justificacion==2,linea.justificacion==1,linea.tamano==1,linea.tamano==2,(linea.estilo==1 || linea.estilo==3),(linea.estilo==2 || linea.estilo==3));
            }
           
        } catch (Exception e) {
             log.mensaje("Cuponera: imprimirTextos: Error Ip:"+ip);
            e.printStackTrace();
            errorImpresion=true;
        }
    }
      
      
      public String reemplazarTexto(String original, String abuscar, String reemplazo) {
		String nuevo="";
		int adonde=original.indexOf(abuscar);
		if (adonde<0) nuevo=original;
		while (adonde>=0) {
			if (adonde>0) nuevo=original.substring(0,adonde);
			nuevo=nuevo+reemplazo;
			nuevo=nuevo+original.substring(adonde+abuscar.length(),original.length());
			original=nuevo;
			adonde=original.indexOf(abuscar,adonde+reemplazo.length());
		}
		
		return nuevo;
    }
      
      String darFechaVigencia(int dias) {
          String vigencia=""; 
          Calendar ahora = Calendar.getInstance();
          ahora.add(Calendar.DATE, dias);
          int dia=ahora.get(Calendar.DATE);
          int mes=ahora.get(Calendar.MONTH)+1;
          int ano=ahora.get(Calendar.YEAR);
          
          if (dia<10) vigencia="0"+dia; else vigencia=""+dia;
          if (mes<10) vigencia=vigencia+"/0"+mes; else vigencia=vigencia+"/"+mes;
          vigencia=vigencia+"/"+ano;
          
          return vigencia;
      }
      
      void checksumLogo() {
        try {
            output.write((byte) 95); //header
            output.write((byte) 137); //secuencia
            output.write((byte) 86); //comando
            output.write((byte) 1); //logo
            
            output.write((byte) 128); //checksum 1
            output.write((byte) 128); //checksum 2
            
            output.write((byte) 0);
            
            output.flush();
            //System.out.println("Escuchando respuesta");
           
           // BufferedReader elMensaje=new BufferedReader(new InputSsocket.getInputStream()eader());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     
    boolean conectar(String ip) {
        boolean conecto=false;
        try {
           // socket=new Socket(ip,10027); //epson 9100   hasar 10027
            
            socket=new Socket();//new Socket("172.31.1.203",10027);
            
            socket.connect(new InetSocketAddress(ip, 10027), 3000);
            socket.setSoTimeout(2000);
                    
            System.out.println("Cuponera: Conectado: "+ip);
            output=socket.getOutputStream();
            input=socket.getInputStream();
            conecto=true;
        }
        catch (Exception e) {
            e.printStackTrace();
            log.mensaje("Cuponera: Conectar: No pude conectar Ip:"+ip);
            System.out.println("Cuponera: Conectar: No pude conectar ip: "+ip);
            errorImpresion=true;
        }
        return conecto;
    }
    
   int crc=0;
   Socket socket=null;
   OutputStream output=null;
   InputStream  input=null;
   Log log=null;
   String ip="";
   boolean errorImpresion=false;
}

