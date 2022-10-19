/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.mailService;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONObject;
import pl.sportal.jpa.KonfiguracjaSerweraPoczty;
import pl.sportal.jpa.Mail;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.KonfiguracjaFacadeLocal;
import pl.sportal.session.KonfiguracjaSerweraPocztyFacadeLocal;

@Stateless
@Path("mail-service")
public class RESTMailService {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Resource(name = "java:jboss/mail/mail-smportal_outgoing")
    private Session session;
    @EJB
    private KonfiguracjaSerweraPocztyFacadeLocal konfiguracjaSerweraPocztyFacade;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;
    @Context
    SecurityContext securityContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/send_mail")
    public Odpowiedz wyslij(
            @Context HttpServletRequest request,
            JSONObject json
    ) throws Exception {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json == null) {
            odpowiedz.setBlad(true).setKomunikat("Brak danych konfiguracyjnych");
        } else {
            Mail mail = new Mail();

            mail = mail.setJSON(json);

            KonfiguracjaSerweraPoczty konfiguracja = null;
            if (mail != null && mail.getKonfiguracjaPoczty() != null) {
                konfiguracja = konfiguracjaSerweraPocztyFacade.find(mail.getKonfiguracjaPoczty());
            }

            if (mail != null) {
                if (mail.getOdbiorcy() != null && !mail.getOdbiorcy().isEmpty()) {
                    if (konfiguracja != null) {
                        if (konfiguracjaSerweraPocztyFacade.isEncoded(konfiguracja)) {
                            try {
                                if (konfiguracja.isSSL()) {
                                    session.getProperties().replace("mail.from", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtps.host", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtps.port", konfiguracja.getPortSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtps.auth", "true");
                                    session.getProperties().setProperty("mail.smtps.localhost", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtps.starttls.enable", "true");
                                    session.getProperties().setProperty("mail.smtps.ssl.trust", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtp.from", konfiguracja.getNadawca());
                                } else {
                                    session.getProperties().replace("mail.from", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtp.host", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtp.port", konfiguracja.getPortSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtp.auth", "true");
                                    session.getProperties().setProperty("mail.smtp.localhost", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtp.starttls.enable", "true");
                                    session.getProperties().setProperty("mail.smtp.ssl.trust", konfiguracja.getAdresSerweraPoczty());
                                    session.getProperties().setProperty("mail.smtp.from", konfiguracja.getNadawca());
                                }
                                Transport transport;
                                if (konfiguracja.isSSL()) {
                                    transport = session.getTransport("smtps");
                                } else {
                                    transport = session.getTransport("smtp");
                                }
                                transport.connect(
                                        konfiguracja.getAdresSerweraPoczty(),
                                        konfiguracja.getUzytkownik(),
                                        KonfiguracjaSerweraPoczty.decodePassword(konfiguracja.getHasloSerweraPoczty()));

                                MimeMessage message = new MimeMessage(session);

                                message.setFrom(new InternetAddress(konfiguracja.getAdresSerweraPoczty(), konfiguracja.getNadawca()));
                                for (String odbiorca : mail.getOdbiorcy()) {
                                    message.setRecipients(Message.RecipientType.CC, odbiorca);
                                }

                                message.setSubject(MimeUtility.encodeText(mail.getTematWiadomosci(), "utf-8", "B"));
                                Multipart multipart = new MimeMultipart();
                                BodyPart htmlBodyPart = new MimeBodyPart();
                                htmlBodyPart.setContent(mail.getTrescWiadomosci(), "text/html; charset=\"UTF-8\"");
                                multipart.addBodyPart(htmlBodyPart);
                                message.setContent(multipart);
                                message.saveChanges();
                                transport.sendMessage(message, message.getAllRecipients());
                                transport.close();
                            } catch (NoSuchProviderException ex) {
                                Logger.getLogger(RESTMailService.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (MessagingException | UnsupportedEncodingException ex) {
                                Logger.getLogger(RESTMailService.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
        }
        return odpowiedz;
    }
}
