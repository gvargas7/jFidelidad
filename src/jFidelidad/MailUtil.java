package jFidelidad;
import java.util.Properties;
import java.util.Date;
import java.util.Vector;
import javax.mail.internet.*;
import javax.mail.*;
import javax.activation.*;

import java.io.*;
public class MailUtil {	
    public MailUtil(String elIpHost) {		
                ipHost=elIpHost;
                gPropiedades=new Propiedades();
		props = new Properties();
		//if (props!=null) props.put("mail.smtp.host",ipHost);
                if (props!=null) {
                    props.put("mail.smtp.host",gPropiedades.gMailServer);
                    props.put("mail.transport.protocol", "smtp");
                    if (gPropiedades.gMailSeguridad) {
                        props.put("mail.smtp.ssl.enable", "true");
                        props.put("mail.smtp.starttls.enable", "true");
                        props.put("mail.smtp.auth", "true");
                    }
                    if (!gPropiedades.gMailPuerto.equals("0")) props.put("mail.smtp.port", gPropiedades.gMailPuerto);
                }
	}		

public String mandarMailMultiple(String origen, Vector destino, String tema, String mensaje,boolean enbcc,String mailtobcc,String mime) {		
    String res="";
		try {			
                    Session session = Session.getInstance(props, null);
                    MimeMessage m=new MimeMessage(session);
			m.setFrom(new InternetAddress(origen));
			int cuantosmail=destino.size();
			InternetAddress a[]=new InternetAddress[cuantosmail];
			for (int i=0;i<cuantosmail;i++)				
                        a[i]=new InternetAddress((String)destino.elementAt(i));
			if (enbcc==false) m.setRecipients(Message.RecipientType.TO,a);
			else {					
                            InternetAddress aux[]=new InternetAddress[1];
			if (!mailtobcc.equals("")){						
                            aux[0]=new InternetAddress(mailtobcc);
                            m.setRecipients(Message.RecipientType.TO,aux);
			}					
                         m.setRecipients(Message.RecipientType.BCC,a);
			}							
                        m.setSubject(tema);
			String textomail=mensaje;
			m.setSentDate(new Date());
				   /* MimeBodyPart mbp1 = new MimeBodyPart();
	    mbp1.setText(textomail);
	    mbp1.setHeader("Content-Type", mime);
 	    mbp1.setHeader("Content-Transfer-Encoding", "7bit" );
	  	Multipart mp = new MimeMultipart();
	  	mp.addBodyPart(mbp1);
*/			
                m.setText(textomail);
	   	m.setHeader("Content-Type", "text/html");
	   	m.setHeader("Content-Transfer-Encoding", "7bit" );
	   // m.setContent(mp);
		Transport.send(m);
	} catch (Exception e2) {res=res+e2.toString();
}	return res;
}		

public String mandarMailMultiple2(String origen, Vector destino, String tema, String mensaje,boolean enbcc,String mailtobcc,String mime) {		String res="";
		try {			Session session = Session.getInstance(props, null);
			MimeMessage m=new MimeMessage(session);
			m.setFrom(new InternetAddress(origen));
						int cuantosmail=destino.size();
			InternetAddress a[]=new InternetAddress[cuantosmail];
						for (int i=0;
i<cuantosmail;
i++)				a[i]=new InternetAddress((String)destino.elementAt(i));
			if (enbcc==false) m.setRecipients(Message.RecipientType.TO,a);
				else {					InternetAddress aux[]=new InternetAddress[1];
					if (!mailtobcc.equals("")){						aux[0]=new InternetAddress(mailtobcc);
						m.setRecipients(Message.RecipientType.TO,aux);
					}					m.setRecipients(Message.RecipientType.BCC,a);
				}							m.setSubject(tema);
			String textomail=mensaje;
									m.setSentDate(new Date());
				    MimeBodyPart mbp1 = new MimeBodyPart();
	    mbp1.setText(textomail);
	    mbp1.setHeader("Content-Type", mime);
 	    mbp1.setHeader("Content-Transfer-Encoding", "7bit" );
	  	Multipart mp = new MimeMultipart();
	  	mp.addBodyPart(mbp1);
	   	m.setHeader("Content-Type", "text/html");
	    m.setContent(mp);
			Transport.send(m);
					} catch (Exception e2) {
                                            res=res+e2.toString();
                                            e2.printStackTrace();
}			return res;
	}	

public String mandarMail2(String origen, String destino, String tema, String mensaje) {		
    String res="";
		try {			Session session = Session.getInstance(props, null);
			MimeMessage m=new MimeMessage(session);
			m.setFrom(new InternetAddress(origen));
			InternetAddress a[]=new InternetAddress[1];
			a[0]=new InternetAddress(destino);
			m.setRecipients(Message.RecipientType.TO,a);
			m.setSubject(tema);
			//String textomail = MimeUtility.encodeText(mensaje, "ISO-8859-1", null);
 			String textomail=mensaje;
									m.setSentDate(new Date());
				    MimeBodyPart mbp1 = new MimeBodyPart();
	    mbp1.setText(textomail);
	    MimeBodyPart mbp2 = new MimeBodyPart();
	  	Multipart mp = new MimeMultipart();
	  	mp.addBodyPart(mbp1);
	  		   	    m.setContent(mp);
			Transport.send(m);
						//res="";
		} catch (Exception e2) {res=res+e2.toString();
}			return res;
	}		

public String mandarMailAttach(String origen, String destino, String tema, String mensaje,String filepath,String nombrearch,String mimetipo) {		String res="";
		try {			Session session = Session.getInstance(props, null);
			MimeMessage m=new MimeMessage(session);
			m.setFrom(new InternetAddress(origen));
			InternetAddress a[]=new InternetAddress[1];
			a[0]=new InternetAddress(destino);
			m.setRecipients(Message.RecipientType.TO,a);
			m.setSubject(tema);
			//String textomail = MimeUtility.encodeText(mensaje, "ISO-8859-1", null);
 			String textomail=mensaje;
									m.setSentDate(new Date());
				    MimeBodyPart mbp1 = new MimeBodyPart();
	    mbp1.setText(textomail);
	    MimeBodyPart mbp2 = new MimeBodyPart();
						FileDataSource fds = new FileDataSource(filepath);
	    mbp2.setDataHandler(new DataHandler(fds));
	    //mbp2.setFileName(fds.getName());
	    mbp2.setFileName(nombrearch);
			//mbp2.setHeader("Content-Type", "image/jpeg");
 			mbp2.setHeader("Content-Type", mimetipo);
 	  	Multipart mp = new MimeMultipart();
	  	mp.addBodyPart(mbp1);
	  	mp.addBodyPart(mbp2);
	  		   	    m.setContent(mp);
			m.saveChanges();
			Transport.send(m);
						//res="";
		} catch (Exception e2) {res=res+e2.toString();
}			return res;
	}			
//public String mandarSendMail(String origen, String destino, String tema, String mensaje) {	
public String mandarMail(String origen, String destino, String tema, String mensaje) {				String res="";
				try {			Runtime runtime=Runtime.getRuntime();
			Process sendmail=runtime.exec("/usr/sbin/sendmail -t -v\n");
			OutputStream output=sendmail.getOutputStream();
			DataOutputStream dataoutput=new DataOutputStream(output);
						String comando="To: "+destino+"\n";
			comando=comando+"From: "+origen+"\n";
			comando=comando+"Subject: "+tema+"\n";
			comando=comando+mensaje+"\n";
			comando=comando+".\n";
						dataoutput.writeBytes(comando);
			dataoutput.flush();
						int exito=sendmail.waitFor();
						if (exito!=0) res=""+exito;
			//res=""+exito;
				}		catch (Exception e) {			res=e.toString();
		}				return res;
	}		public String mandarMail(String origen, String destino, String tema, String mensaje,boolean sinver) {				String res="";
				try {			Runtime runtime=Runtime.getRuntime();
			String sendmailcom="/usr/sbin/sendmail -t -v\n";
			if (sinver==true) sendmailcom="/usr/sbin/sendmail -t \n";
			Process sendmail=runtime.exec(sendmailcom);
			OutputStream output=sendmail.getOutputStream();
			DataOutputStream dataoutput=new DataOutputStream(output);
						String comando="To: "+destino+"\n";
			comando=comando+"From: "+origen+"\n";
			comando=comando+"Subject: "+tema+"\n";
			comando=comando+mensaje+"\n";
			comando=comando+".\n";
						dataoutput.writeBytes(comando);
			dataoutput.flush();
						int exito=sendmail.waitFor();
						if (exito!=0) res=""+exito;
			//res=""+exito;
				}		catch (Exception e) {			res=e.toString();
		}				return res;
	}			String ipHost="";
	
        
    public String mandarMailMultipleConUser(Vector destino, String tema, String mensaje,boolean enbcc,String mailtobcc,String mime) {		
        String res="";

        try {			
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {

                protected PasswordAuthentication getPasswordAuthentication() {

                    return new PasswordAuthentication(gPropiedades.gMailUser, gPropiedades.gMailPassword);

                }

            });
            MimeMessage m=new MimeMessage(session);
            m.setFrom(new InternetAddress(gPropiedades.gMailUser));
            int cuantosmail=destino.size();
            InternetAddress a[]=new InternetAddress[cuantosmail];
            for (int i=0;i<cuantosmail;i++)				
                a[i]=new InternetAddress((String)destino.elementAt(i));
            if (enbcc==false) m.setRecipients(Message.RecipientType.TO,a);
            else {					
                InternetAddress aux[]=new InternetAddress[1];
                if (!mailtobcc.equals("")){
                    aux[0]=new InternetAddress(mailtobcc);
                    m.setRecipients(Message.RecipientType.TO,aux);
                }
                m.setRecipients(Message.RecipientType.BCC,a);
            }
            m.setSubject(tema);
            String textomail=mensaje;
            m.setSentDate(new Date());
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(textomail);
            mbp1.setHeader("Content-Type", mime);
            //mbp1.setHeader("Content-Transfer-Encoding", "7bit" );
            Multipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);
            m.setHeader("Content-Type", "text/html");
            m.setContent(mp);
            //Transport transport = session.getTransport("smtp");
            //transport.connect();
            Transport.send(m);
        } catch (Exception e2) {
            res=res+e2.toString();
            e2.printStackTrace();
        }
        return res;
    }
        
        Properties props=null;
        Propiedades gPropiedades=null;
}
