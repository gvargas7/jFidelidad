/*
 * Main.java
 *
 * Created on 21 de noviembre de 2007, 16:59
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jFidelidad;

import java.io.File;
/**
 *
 * @author rhirsch
 *
 */

public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        boolean terminar=false;
        boolean eschile=false;
        boolean escol=false;
        boolean modotest=false;
        try {
           if (args.length==1) {                             
            if(args[0].equals("cl")) eschile=true;
            else
            if(args[0].equals("co")) escol=true;
            else
            if(args[0].equals("test")) modotest=true;
            else
            if(args[0].equals("cltest")) {
                modotest=true;
                eschile=true;
            }
           }                 
            
           
           System.out.println("Comienzo "+eschile+" test:"+modotest);

          // Class.forName("com.sybase.jdbc3.jdbc.SybDriver").newInstance();
         //  Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
           //Class.forName("com.mysql.jdbc.Driver");
       
          // Codigos cod=new Codigos();
          // cod.crearCodigos("A01", 1000,121351,477);
           jServidor servidor=new jServidor(eschile,escol,modotest);
           servidor.start();
           

        }
        catch (Exception e) {
                e.printStackTrace();
        }
    }
    
    

}
