/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.uzytkownicy;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONObject;
import pl.sportal.common.utilities.BCrypt;
import pl.sportal.common.utilities.GeneratorKodow;
import pl.sportal.common.utilities.JSONArrayBuilder;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.common.utilities.JSONObjectExt;
import pl.sportal.common.utilities.ListBuilder;
import pl.sportal.common.utilities.Validator;
import pl.sportal.jpa.Konfiguracja;
import pl.sportal.jpa.Konta;
import pl.sportal.jpa.Mail;
import pl.sportal.jpa.ZmianaHasla;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.session.KonfiguracjaFacadeLocal;
import pl.sportal.session.KonfiguracjaSerweraPocztyFacadeLocal;
import pl.sportal.session.KontaFacadeLocal;
import pl.sportal.session.ParametryHaslaFacade;
import pl.sportal.session.ParametryHaslaFacadeLocal;
import pl.sportal.session.ZmianaHaslaFacadeLocal;


@Stateless
@Path("uzytkownicy/zmiana-hasla")
public class RESTZmianaHasla {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private ParametryHaslaFacadeLocal parametryHaslaFacade;
    @EJB
    private KontaFacadeLocal kontaFacade;
    @EJB
    private KonfiguracjaSerweraPocztyFacadeLocal konfiguracjaSerweraPocztyFacade;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;
    @EJB
    private ZmianaHaslaFacadeLocal zmianaHaslaFacade;
    @Resource(name = "java:jboss/mail/mail-smportal_outgoing")
    private Session mailSession;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parse")
    public Odpowiedz parse(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        JSONBuilder daneBuilder = new JSONBuilder();
        if (json != null) {
            LinkedHashMap<String, JSONArrayBuilder> mapaUwag = new LinkedHashMap<>();
            String haslo = json.getString("haslo");
            LinkedList<String> uwagiDoHasla = ParametryHaslaFacade.sprawdz(parametryHaslaFacade.find(), haslo);
            if (!uwagiDoHasla.isEmpty()) {
                RESTApplication.addToMap(mapaUwag, "haslo", uwagiDoHasla.stream().collect(Collectors.joining("<br>")).toString());
            }

            if (json.getString("potwierdzoneHaslo") != null) {
                String potwierdzoneHaslo = json.getString("potwierdzoneHaslo");
                if (!haslo.equals(potwierdzoneHaslo)) {
                    RESTApplication.addToMap(mapaUwag, "potwierdzoneHaslo", "wprowadzone hasła różnią się");
                }
            }

            mapaUwag.entrySet().stream().forEach(entry -> {
                if (!entry.getValue().isEmpty()) {
                    daneBuilder.put(entry.getKey(), entry.getValue().build());
                }
            });
            JSONObject dane = daneBuilder.build();
            odpowiedz.setUwaga(!dane.isEmpty()).setDane(dane);
        }
        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parse-mail")
    public Odpowiedz parseEmail(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        JSONBuilder daneBuilder = new JSONBuilder();
        if (json != null) {
            LinkedHashMap<String, JSONArrayBuilder> mapaUwag = new LinkedHashMap<>();
            String email = json.getString("email");
            if (email == null || email.isEmpty()) {
                RESTApplication.addToMap(mapaUwag, "email", "nie wypełniono pola adres e-mail");
            } else {
                if (!Validator.isValidEmail(email)) {
                    RESTApplication.addToMap(mapaUwag, "email", mapaUwag.get("email") == null
                            ? "nieprawidłowy format adresu e-mail" : "<br>nieprawidłowy format adresu e-mail");
                }
                if (mapaUwag.get("email") == null
                        && kontaFacade.findByEmail(email).isEmpty()) {
                    RESTApplication.addToMap(mapaUwag, "email", mapaUwag.get("email") == null
                            ? "brak konta z podanym adresem e-mail" : "<br>brak konta z podanym adresem e-mail");
                }
            }
            mapaUwag.entrySet().stream().forEach(entry -> {
                if (!entry.getValue().isEmpty()) {
                    daneBuilder.put(entry.getKey(), entry.getValue().build());
                }
            });
            JSONObject dane = daneBuilder.build();
            odpowiedz.setUwaga(!dane.isEmpty()).setDane(dane);
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/generator-hasla")
    public Odpowiedz getJednorazowyKodDostepu() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            odpowiedz.setDane(new JSONBuilder()
                    .put("haslo", generujNoweHaslo())
                    .build());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }

        return odpowiedz;
    }

    private String generujNoweHaslo() {
        String noweHaslo = GeneratorKodow.generujNoweHaslo(10);
        if (!czyHasloZgodneZWymaganiami(noweHaslo)) {
            noweHaslo = generujNoweHaslo();
        }
        return noweHaslo;
    }

    private boolean czyHasloZgodneZWymaganiami(String noweHaslo) {
        return parametryHaslaFacade.sprawdz(noweHaslo).isEmpty();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz setNoweHaslo(
            @Context HttpServletRequest request,
            JSONObjectExt json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            try {
                Integer id = null;
                try {
                    if (json.get("idKonta") != null) {
                        id = Integer.parseInt(json.get("idKonta").toString());
                    }
                } catch (NumberFormatException ex) {
                }
                String noweHaslo = json.getString("haslo");
                if (id != null) {
                    Konta konto = kontaFacade.find(id);
                    konto.setHaslo(BCrypt.hashpw(noweHaslo, BCrypt.gensalt(12)));
                    kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    kontaFacade.edit(konto);
                    //wysyłka email z nowym hasłem jeśli zmieniane przez admina
                    if (json.getString("potwierdzoneHaslo") == null) {
                        Konfiguracja konfiguracja = konfiguracjaFacade.find();
                        if (konfiguracja != null) {
                            String szablon = konfiguracja.getSzablonEmailPowiadomienieOZmianieHasla();
                            if (szablon != null) {
                                try {
                                    String tresc = Mail.wypelnijSzablon(konto, null, konfiguracja, szablon, noweHaslo, null);
                                    new Mail()
                                            .setSession(mailSession)
                                            .setKonfiguracjaPoczty(konfiguracjaSerweraPocztyFacade.find())
                                            .setKonfiguracja(konfiguracja)
                                            .setOdbiorcy(new ListBuilder<String>().append(konto.getEmail()).build())
                                            .setTematWiadomosci("Powiadomienie o zmianie hasła")
                                            .setTrescWiadomosci(tresc)
                                            .wyslij();
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, ex.getMessage());
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    /**
     * Ta metoda wymaga tylko adresu email, gdy użytkownik nie pamięta hasła i
     * nie może się zalogować
     *
     * @param request
     * @param json
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nie-pamietam")
    public Odpowiedz wyslijLinkNiePamietamHasla(
            @Context HttpServletRequest request,
            JSONObjectExt json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            try {
                String email = json.getString("email");
                if (email != null) {
                    Konta konto = kontaFacade.findByEmail(email).stream().sorted(Konta.COMPARATOR_BY_ID.reversed()).findFirst().orElse(null);
                    if (konto != null) {
                        String token = UUID.randomUUID().toString();
                        ZmianaHasla zmianaHasla = new ZmianaHasla()
                                .setUuid(konto.getUUID())
                                .setToken(token)
                                .setZnacznikCzasuUtworzenia(new Date());
                        //zmianaHaslaFacade = ZmianaHaslaFacadeLocal.create(securityContext);
                        zmianaHaslaFacade.create(zmianaHasla);
                        Konfiguracja konfiguracja = konfiguracjaFacade.find();
                        if (konfiguracja != null) {
                            String szablon = konfiguracja.getSzablonEmailZmianaHasla();
                            if (szablon != null) {
                                try {
                                    String tresc = Mail.wypelnijSzablon(konto, null, konfiguracja, szablon, null, token);
                                    new Mail()
                                            .setSession(mailSession)
                                            .setKonfiguracjaPoczty(konfiguracjaSerweraPocztyFacade.find())
                                            .setKonfiguracja(konfiguracja)
                                            .setOdbiorcy(new ListBuilder<String>().append(email).build())
                                            .setTematWiadomosci("Link do zresetowania hasła")
                                            .setTrescWiadomosci(tresc)
                                            .wyslij();
                                    odpowiedz.setBlad(false).setKomunikat("Na adres " + email + " wysłano e-mail do zmiany hasła");
                                } catch (MessagingException | Mail.MailException ex) {
                                    LOGGER.log(Level.SEVERE, ex.getMessage());
                                    odpowiedz.setBlad(true).setKomunikat("Wystąpił błąd w czasie wysyłania wiadomości e-mail do resetowania hasła");
                                }
                            } else {
                                LOGGER.log(Level.SEVERE, "Brak szablonu e-mail zmiany hasła");
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, "Brak danych konfiguracyjnych");
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "Konto {0} nie istnieje", email);
                    }
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie podano adresu e-mail");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat("Wystąpił błąd w czasie wysyłania wiadomości e-mail do resetowania hasła");
            }
        }
        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/wyslij-link")
    public Odpowiedz wyslijLinkDoZmianyHasla(
            @Context HttpServletRequest request,
            JSONObjectExt json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            try {
                Integer id = null;
                try {
                    if (json.get("idKonta") != null) {
                        id = Integer.parseInt(json.get("idKonta").toString());
                    }
                } catch (NumberFormatException ex) {
                }
                String email = json.getString("email");
                if (id != null && email != null) {
                    Konta konto = kontaFacade.find(id);
                    if (konto != null) {
                        String token = UUID.randomUUID().toString();
                        ZmianaHasla zmianaHasla = new ZmianaHasla()
                                .setUuid(konto.getUUID())
                                .setToken(token)
                                .setZnacznikCzasuUtworzenia(new Date());
                        //zmianaHaslaFacade = ZmianaHaslaFacadeLocal.create(securityContext);
                        zmianaHaslaFacade.create(zmianaHasla);
                        Konfiguracja konfiguracja = konfiguracjaFacade.find();
                        if (konfiguracja != null) {
                            String szablon = konfiguracja.getSzablonEmailZmianaHasla();
                            if (szablon != null) {
                                try {
                                    String tresc = Mail.wypelnijSzablon(konto, null, konfiguracja, szablon, null, token);
                                    new Mail()
                                            .setSession(mailSession)
                                            .setKonfiguracjaPoczty(konfiguracjaSerweraPocztyFacade.find())
                                            .setKonfiguracja(konfiguracja)
                                            .setOdbiorcy(new ListBuilder<String>().append(email).build())
                                            .setTematWiadomosci("Link do zresetowania hasła")
                                            .setTrescWiadomosci(tresc)
                                            .wyslij();
                                    odpowiedz.setBlad(false).setKomunikat("Na adres " + email + " wysłano e-mail do zmiany hasła");
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, ex.getMessage());
                                    odpowiedz.setBlad(true).setKomunikat("Wystąpił błąd w czasie wysyłania e-mail do zmiany hasła");
                                }
                            } else {
                                LOGGER.log(Level.SEVERE, "Brak szablonu e-mail zmiany hasła");
                            }
                        } else {
                            LOGGER.log(Level.SEVERE, "Brak danych konfiguracyjnych");
                        }
                    } else {
                        LOGGER.log(Level.WARNING, "Konto {0} nie istnieje", email);
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat("Wystąpił błąd w czasie wysyłania wiadomości e-mail do resetowania hasła");
            }
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{token}")
    public Odpowiedz sprawdzToken(
            @PathParam("token") String token) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            ZmianaHasla zmianaHasla = zmianaHaslaFacade.findByToken(token);
            if (zmianaHasla != null) {
                if (czyTokenWazny(zmianaHasla.getZnacznikCzasuUtworzenia())) {
                    Konta konto = kontaFacade.findByUUID(zmianaHasla.getUuid());
                    odpowiedz.setDane(new JSONBuilder()
                            .put("idKonta", konto.getId())
                            .build());
                    //zmianaHaslaFacade = ZmianaHaslaFacadeLocal.create(securityContext);
                    zmianaHaslaFacade.remove(zmianaHasla);
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Link umożliwiający zmianę hasła stracił swoją ważność!");
                }
            } else {
                odpowiedz.setBlad(true).setKomunikat("Link umożliwiający zmianę hasła jest niepoprawny!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    private boolean czyTokenWazny(Date znacznikCzasuUtworzeniaTokenu) {
        LocalDateTime czasUtworzeniaTokenuLocalDate = znacznikCzasuUtworzeniaTokenu.toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        czasUtworzeniaTokenuLocalDate = czasUtworzeniaTokenuLocalDate.plusDays(1);
        Date znacznikCzasuUtworzeniaTokenuPlusDzien = Date.from(czasUtworzeniaTokenuLocalDate.atZone(ZoneId.systemDefault()).toInstant());
        Date dataDzisiejsza = new Date();
        return dataDzisiejsza.compareTo(znacznikCzasuUtworzeniaTokenuPlusDzien) <= 0;
    }
}
