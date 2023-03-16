/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jFidelidad;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 *
 * @author rhirsch
 */
public class Propiedades {
    public Propiedades(boolean test) {
        gTest=test;
        leerProperties(test);
    }
    
    public Propiedades() {
        
    }
    
    void leerProperties(boolean test) {
        Properties prop = new Properties();

        try {
            String filename="conexion.properties";
            if (test) filename="conexion.properties.test";
           InputStream in = new FileInputStream(filename);

           // InputStream in = new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("").getPath()+"conexion.properties");
            prop.load(in);
            in.close();

            gBd=prop.getProperty("bd");
            gUser=prop.getProperty("usuario");
            gPass=prop.getProperty("clave");
            gBdPayroll=prop.getProperty("bdpayroll");
            gUserPayroll=prop.getProperty("usuariopayroll");
            gPassPayroll=prop.getProperty("clavepayroll");
            gCantConexiones=Integer.parseInt((prop.getProperty("conexiones","10")));
            gPuerto=Integer.parseInt((prop.getProperty("puerto","0")));
            gEmpresa=prop.getProperty("empresa");
            gMailServer=prop.getProperty("MailServer");
            gMailUser=prop.getProperty("MailUser");
            gMailPassword=prop.getProperty("MailPassword");
            gMailPuerto=prop.getProperty("MailPort");
            if (prop.getProperty("MailSeguridad").equals("1"))
                gMailSeguridad=true;
            in.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    };
    
    public static String gBd="";
    public static String gUser="";
    public static String gPass="";
    public static String gBdPayroll="";
    public static String gUserPayroll="";
    public static String gPassPayroll="";
    public static int gCantConexiones=35;
    public static boolean gTest=false;
    public static int gPuerto=0;
    public static String gEmpresa="";
    public static String gMailServer="";
    public static String gMailUser="";
    public static String gMailPassword="";
    public static String gMailPuerto="0";
    public static boolean gMailSeguridad=false;
}
