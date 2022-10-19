/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import pl.sportal.common.utilities.BCrypt;
import pl.sportal.common.utilities.JSONArrayBuilder;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.common.utilities.JSONObjectExt;
import pl.sportal.common.utilities.ListBuilder;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.common.utilities.Validator;
import pl.sportal.rest.CustomClaims;
import pl.sportal.jpa.Konfiguracja;
import pl.sportal.jpa.KonfiguracjaSerweraPoczty;
import pl.sportal.jpa.Konta;
import pl.sportal.jpa.Mail;
import pl.sportal.jpa.Klienci;
import pl.sportal.jpa.KlienciPowiazania;
import pl.sportal.jpa.ParametryHasla;
import pl.sportal.jpa.TypyDokumentow;
import pl.sportal.session.KonfiguracjaFacadeLocal;
import pl.sportal.session.KonfiguracjaSerweraPocztyFacadeLocal;
import pl.sportal.session.KontaFacadeLocal;
import pl.sportal.session.ParametryHaslaFacade;
import pl.sportal.session.ParametryHaslaFacadeLocal;
import pl.sportal.session.TypyDokumentowFacadeLocal;
import pl.sportal.session.KlienciFacadeLocal;
import pl.sportal.session.KlienciPowiazaniaFacadeLocal;


@Stateless
@Path("klienci")
public class RESTKlienci {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private KlienciFacadeLocal klienciFacade;
    @EJB
    private KontaFacadeLocal kontaFacade;
    @EJB
    private KlienciPowiazaniaFacadeLocal klienciPowiazaniaFacade;
    @EJB
    private TypyDokumentowFacadeLocal typyDokumentowFacade;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;
    @EJB
    private ParametryHaslaFacadeLocal parametryHaslaFacade;
    @EJB
    private KonfiguracjaSerweraPocztyFacadeLocal konfiguracjaSerweraPocztyFacade;
    @Resource(name = "java:jboss/mail/mail-smportal_outgoing")
    private Session mailSession;

    //--------------------------------------------------------------------------
    // Metody publiczne
    //-------------------------------------------------------------------------- 
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Secured
    public Odpowiedz getKlienci() {
        return getKlienci(null, null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/lista")
    @Secured
    public Odpowiedz getKlienci(
            @Context HttpServletRequest request,
            JSONObjectExt json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            boolean refresh = false;
            Integer limit = null;
            Integer offset = null;
            String filter = null;
            if (json != null) {
                try {
                    refresh = json.getBooleanSimple("refresh");
                } catch (NumberFormatException ex) {
                }
                try {
                    limit = json.getInteger("limit");
                } catch (NumberFormatException ex) {
                }
                try {
                    offset = json.getInteger("offset");
                } catch (NumberFormatException ex) {
                }
                filter = json.getString("filter");
            }
            if (offset == null) {
                offset = 0;
            }
            List<Klienci> list = klienciFacade.find(filter, null);
            int liczbaDostepnychRekordow = list.size();
            limit = limit != null ? limit : 50;
            list = list.stream()
                    .sorted(Klienci.COMPARATOR_BY_NAZWISKO_IMIE)
                    .skip(refresh ? 0 : offset)
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list.size() > limit) {
                list = list.stream()
                        .limit(refresh ? offset : limit)
                        .collect(Collectors.toCollection(LinkedList::new));
            }
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("klienci", jsonArray)
                        .put("liczbaDostepnychRekordow", liczbaDostepnychRekordow)
                        .build());
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Secured
    public Odpowiedz getPacjent(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                Klienci object = klienciFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono pacjenta o ID: " + id);
                }
            } catch (Exception ex) {
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    /**
     * Metodę wydzielono aby móc wykonać testy jednostkowe
     *
     * @param json

     * @param klienciFacade
     * @param typyDokumentowFacade
     * @param konfiguracja
     * @param parametryHasla
     * @return
     */
    public static Odpowiedz parse(
            JSONObjectExt json,
            KlienciFacadeLocal klienciFacade,
            TypyDokumentowFacadeLocal typyDokumentowFacade,
            Konfiguracja konfiguracja,
            ParametryHasla parametryHasla,
            KontaFacadeLocal kontaFacade,
            Session mailSession,
            KonfiguracjaSerweraPoczty konfiguracjaSerweraPoczty) {
        Odpowiedz odpowiedz = new Odpowiedz();
        JSONBuilder daneBuilder = new JSONBuilder();
        if (json != null) {

            LinkedHashMap<String, JSONArrayBuilder> mapaUwag = new LinkedHashMap<>();
            boolean samodzielnie = json.getBooleanSimple("samodzielnie"); // true - pacjent sam wypełnia formularz            
            String reCaptchaToken = null;
            if (samodzielnie) {
                //--------------------------------------------------------------
                boolean regulamin = json.getBooleanSimple("regulamin");
                if (!regulamin) {
                    RESTApplication.addToMap(mapaUwag, "regulamin", "nie zakceptowano regulaminu");
                }
                //--------------------------------------------------------------
//                if (konfiguracja != null && konfiguracja.getRecaptchaSecretKey() != null) {
//                    reCaptchaToken = json.getString("reCaptchaToken");
//                    if (reCaptchaToken == null) {
//                        RESTApplication.addToMap(mapaUwag, "recaptcha", "reCAPTCHA wymaga weryfikacji");
//                        LOGGER.log(Level.INFO, "reCaptchaToken is NULL");
//                    } else if (!RESTApplication.isCaptchaValid(konfiguracja.getRecaptchaSecretKey(), reCaptchaToken)) {
//                        RESTApplication.addToMap(mapaUwag, "recaptcha", "reCAPTCHA wymaga weryfikacji");
//                        LOGGER.log(Level.INFO, "reCaptchaToken is invalid");
//                    }
//
//                }
                //--------------------------------------------------------------                
                String haslo = json.getString("haslo");
                LinkedList<String> uwagiDoHasla = ParametryHaslaFacade.sprawdz(parametryHasla, haslo);
                if (!uwagiDoHasla.isEmpty()) {
                    RESTApplication.addToMap(mapaUwag, "haslo", uwagiDoHasla.stream().collect(Collectors.joining("<br>")).toString());
                }

                //--------------------------------------------------------------                
                String login = json.getString("login");
                if (login != null && kontaFacade != null) {
                    List<Konta> kontaByLogin = kontaFacade.findByLogin(login);
                    if (!kontaByLogin.isEmpty()) {
                        RESTApplication.addToMap(mapaUwag, "login", "istnieje już konto z  loginem: " + login);
                    }
                }
                //--------------------------------------------------------------                
                String email = json.getString("email");
                if (email != null && kontaFacade != null) {
                    List<Konta> kontaByEmail = kontaFacade.findByEmail(email);
                    if (!kontaByEmail.isEmpty()) {
                        RESTApplication.addToMap(mapaUwag, "email", "istnieje już konto z adresem e-mail: " + email);
                        //powiadomienie o próbie załóżenia konta na istniejący w bazie e-mail
                        if (konfiguracja != null) {
                            String szablon = konfiguracja.getSzablonEmailPowiadomienieOWykorzystaniuDanychEmail();
                            if (szablon != null) {
                                Konta konto = kontaFacade.findByEmail(email).stream().findFirst().orElse(null);
                                if (konto != null) {
                                    try {
                                        String tresc = Mail.wypelnijSzablon(konto, null, konfiguracja, szablon, null, null);
                                        new Mail()
                                                .setSession(mailSession)
                                                .setKonfiguracjaPoczty(konfiguracjaSerweraPoczty)
                                                .setKonfiguracja(konfiguracja)
                                                .setOdbiorcy(new ListBuilder<String>().append(konto.getEmail()).build())
                                                .setTematWiadomosci("Próba założenia konta")
                                                .setTrescWiadomosci(tresc)
                                                .wyslij();
                                    } catch (Exception ex) {
                                        LOGGER.log(Level.SEVERE, ex.getMessage());
                                    }
                                }
                            }
                        }
                    }
                }
                //--------------------------------------------------------------                
            }
            //sprawdzenie wymaganych pól                        
//            JSONArray jsonPolaWymagane = (JSONArray) ustawienia.getJSON().get(samodzielnie ? "polaWymaganeSamodzielnie" : "polaWymagane");
//            jsonPolaWymagane.stream().forEach(poleWymagane -> {
//                if (json.isNull(poleWymagane)) {
//                    RESTApplication.addToMap(mapaUwag, (String) poleWymagane, "nie wypełniono pola " + Utilities.capitalizeFirstLetter(Klienci.MAPA_POL.get(poleWymagane)));
//                }
//            });
            //PESEL
//            if (mapaUwag.get("pesel") == null
//                    && !Validator.isValidPesel(json.getString("pesel"))) {
//                RESTApplication.addToMap(mapaUwag, "pesel", "nieprawidłowy format PESEL");
//            }
            //data urodzenia
//            if (!json.isNull("dataUrodzenia")) {
//                Date dataUrodzenia = json.getDate("dataUrodzenia");
//                if (dataUrodzenia == null) {
//                    RESTApplication.addToMap(mapaUwag, "dataUrodzenia", "nieprawidłowy format daty urodzenia");
//                } else {
//                    //zgodność PESEL z datą urodzenia
//                    if (mapaUwag.get("pesel") == null) {
//                        String dataNaPodstawiePesel = Utilities.getDateFromPesel(json.getString("pesel"));
//                        if (!json.getString("dataUrodzenia").equals(dataNaPodstawiePesel)) {
//                            RESTApplication.addToMap(mapaUwag, "dataUrodzenia", "data urodzenia niezgodna z PESEL");
//                        }
//                    }
//                }
//            }
            //minimalny wiek
//            if (ustawienia.getMinimalnyWiekPacjenta() != null) {
//                Integer wiek = null;
//                if (json.getDate("dataUrodzenia") != null) {
//                    wiek = Utilities.calculateAge(json.getDate("dataUrodzenia"));
//                } else if (mapaUwag.get("pesel") == null) {
//                    wiek = Utilities.calculateAge(Utilities.stringToDate(Utilities.getDateFromPesel(json.getString("pesel"))));
//                }
//                if (wiek != null && wiek < ustawienia.getMinimalnyWiekPacjenta()) {
//                    RESTApplication.addToMap(mapaUwag, "dataUrodzenia", "Minimalny wiek pacjenta to " + ustawienia.getMinimalnyWiekPacjenta() + " "
//                            + Utilities.polishPlural("rok", "lata", "lat", wiek));
//                }
//            }

            //płeć zgodna z pesel
//            if (json.getString("plec") != null && mapaUwag.get("pesel") == null) {
//                String plec = Utilities.getSexFromPesel(json.getString("pesel")).toLowerCase();
//                if (!json.getString("plec").equalsIgnoreCase(plec)) {
//                    RESTApplication.addToMap(mapaUwag, "plec", "płeć niezgodna z numerem PESEL");
//                }
//            }

            //kod pocztowy
            if (json.getString("kodPocztowy") != null) {
                if (!Validator.isValidPostalCode(json.getString("kodPocztowy"))) {
                    RESTApplication.addToMap(mapaUwag, "kodPocztowy", "nieprawidłowy format kodu pocztowego");
                }
            }
            //email
            if (json.getString("email") != null) {
                if (!Validator.isValidEmail(json.getString("email"))) {
                    RESTApplication.addToMap(mapaUwag, "email", "nieprawidłowy format adresu e-mail");
                }
            }
            //numer telefonu         
            if (json.getString("telefonKontaktowy") != null) {
                if (!Validator.isValidPhoneNumber(json.getString("telefonKontaktowy"))) {
                    RESTApplication.addToMap(mapaUwag, "telefonKontaktowy", "nieprawidłowy format numeru telefonu");
                }
            }

            //numer dokumentu tożsamości
//            String numerDokumentuTozsamosci = json.getString("numerDokumentuTozsamosci");
//            Integer idTypDokumentuTozsamosci = json.getInteger("idTypDokumentuTozsamosci");
//            if (idTypDokumentuTozsamosci != null && numerDokumentuTozsamosci == null) {
//                RESTApplication.addToMap(mapaUwag, "numerDokumentuTozsamosci", "nie wypełniono pola " + Utilities.capitalizeFirstLetter(Klienci.MAPA_POL.get("numerDokumentuTozsamosci")));
//            }
//            if (numerDokumentuTozsamosci != null && idTypDokumentuTozsamosci != null && typyDokumentowFacade != null) {
//                TypyDokumentow typDokumentu = typyDokumentowFacade.find(idTypDokumentuTozsamosci);
//                if (typDokumentu != null && Utilities.stringToNull(typDokumentu.getFormatNumeracjiRegex()) != null) {
//                    Pattern pattern = Pattern.compile(typDokumentu.getFormatNumeracjiRegex());
//                    Matcher matcher = pattern.matcher(numerDokumentuTozsamosci);
//                    if (!matcher.matches()) {
//                        RESTApplication.addToMap(mapaUwag, "numerDokumentuTozsamosci", "nieprawidłowy format numeru dokumentu tożsamości");
//                    }
//                }
//            }


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

    public static Odpowiedz parse(
            JSONObjectExt json
    ) {
        return parse(json, null, null, null, null, null, null, null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/parse")
    public Odpowiedz parse(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            odpowiedz = parse(json,
                    klienciFacade,
                    typyDokumentowFacade,
                    konfiguracjaFacade.find(),
                    parametryHaslaFacade.find(),
                    kontaFacade,
                    mailSession,
                    konfiguracjaSerweraPocztyFacade.find());
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    private Odpowiedz parseTylkoDanePacjenta(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            odpowiedz = parse(json,
                    klienciFacade,
                    typyDokumentowFacade,
                    null,
                    null,
                    null,
                    null,
                    null);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }

        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz setPacjent(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            Odpowiedz wynikParsowania = parseTylkoDanePacjenta(request, json);
            if (wynikParsowania.isBlad()) {
                return odpowiedz.setBlad(true).setKomunikat("Wykryto błąd w danych");
            } else if (wynikParsowania.isUwaga()) {
                return odpowiedz.setBlad(true).setKomunikat("Wykryto uwagę w danych");
            }
            try {
                Integer id = null;
                try {
                    if (json.get("id") != null) {
                        id = Integer.parseInt(json.get("id").toString());
                    }
                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage());
                }
                if (id != null) {
                    //UPDATE
                    Klienci object = klienciFacade.find(id);
                    if (object != null) {
                        Integer idTypDokumentuTozsamosci = null;
                        TypyDokumentow typDokumentu = null;
                        try {
                            if (json.get("idTypDokumentuTozsamosci") != null) {
                                idTypDokumentuTozsamosci = Integer.parseInt(json.get("idTypDokumentuTozsamosci").toString());
                                typDokumentu = typyDokumentowFacade.find(idTypDokumentuTozsamosci);
                            }
                        } catch (NumberFormatException ex) {
                            LOGGER.log(Level.WARNING, ex.getMessage());
                        }
//                        klienciFacade.edit(object.setJSON(json).setIdTypDokumentuTozsamosci(typDokumentu));
                        odpowiedz.setKomunikat("Dane pacjenta zostały zaktualizowane");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono pacjencta o ID: " + id);
                    }
                } else {
                    //INSERT                                      
                    boolean samodzielnie = json.getBooleanSimple("samodzielnie"); // true - pacjent sam wypełnia formularz            
                    if (samodzielnie) {
                        Konta konto = new Konta()
                                .setJSON(json)
                                .setHaslo(BCrypt.hashpw(json.getString("haslo"), BCrypt.gensalt(12)))
                                .setAkceptacjaRegulaminu(json.getBooleanSimple("regulamin") ? new Date() : null);

                        kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        kontaFacade.create(konto);

                        Klienci klient = new Klienci()
                                .setJSON(json);

                        klienciFacade = KlienciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        klienciFacade.create(klient);

                        KlienciPowiazania powiazania = new KlienciPowiazania()
                                .setKonto(konto)
                                .setKlient(klient)
                                .setNadrzedne(true);

                        klienciPowiazaniaFacade = KlienciPowiazaniaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        klienciPowiazaniaFacade.create(powiazania);
                        klient.getKlienciPowiazania().add(powiazania);
                        konto.getKlienciPowiazania().add(powiazania);
                        //wysyłka email z linkiem do aktywacji
                        Konfiguracja konfiguracja = konfiguracjaFacade.find();
                        if (konfiguracja != null) {
                            String szablon = konfiguracja.getSzablonEmailPotwierdzenieZalozeniaKonta();
                            if (szablon != null) {
                                try {
                                    String tresc = Mail.wypelnijSzablon(konto, klient, konfiguracja, szablon, null, null);
                                    new Mail()
                                            .setSession(mailSession)
                                            .setKonfiguracjaPoczty(konfiguracjaSerweraPocztyFacade.find())
                                            .setKonfiguracja(konfiguracja)
                                            .setOdbiorcy(new ListBuilder<String>().append(konto.getEmail()).build())
                                            .setTematWiadomosci("Aktywacja konta")
                                            .setTrescWiadomosci(tresc)
                                            .wyslij();
                                } catch (Exception ex) {
                                    LOGGER.log(Level.SEVERE, ex.getMessage());
                                }
                            }
                        }
                        odpowiedz.setKomunikat("Dane pacjenta zostały zapisane");
                    } else {
                        klienciFacade = KlienciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        klienciFacade.create(new Klienci().setJSON(json));
                        odpowiedz.setKomunikat("Dane pacjenta zostały zapisane");
                    }
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/do-konta")
    @Secured
    public Odpowiedz setPacjentPrzyKoncie(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            JSONObjectExt klientJSON = new JSONObjectExt();
            klientJSON.putAll((LinkedHashMap) json.get("klient"));
            Odpowiedz wynikParsowania = parseTylkoDanePacjenta(request, klientJSON);
            if (wynikParsowania.isBlad() || wynikParsowania.isUwaga()) {
                return odpowiedz.setBlad(true).setKomunikat("Wykryto błąd w danych");
            }
            try {
                Integer idKonto = null;
                try {
                    if (json.get("kontoId") != null) {
                        idKonto = Integer.parseInt(json.get("kontoId").toString());
                    }

                } catch (NumberFormatException ex) {
                    LOGGER.log(Level.WARNING, ex.getMessage());
                }
                if (idKonto != null) {
                    //UPDATE
                    Integer idKlienta = klientJSON.getInteger("id");
                    if (idKlienta != null) {
                        klienciFacade = KlienciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        Klienci pacjentZBazy = klienciFacade.find(idKlienta);
                        if (pacjentZBazy != null) {
                            pacjentZBazy.setJSON(klientJSON);
                            klienciFacade.edit(pacjentZBazy);
                        } else {
                            odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono artykułu o ID: " + idKlienta);
                        }
                    } else {
                        //INSERT
                        Klienci klient = new Klienci().setJSON(klientJSON);
                        boolean isNadrzedny = json.getBooleanSimple("nadrzedny");
                        klienciFacade = KlienciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        klienciFacade.create(klient);
                        Konta konto = kontaFacade.find(idKonto);
                        KlienciPowiazania pp = new KlienciPowiazania();
                        pp.setKonto(konto)
                                .setKlient(klient)
                                .setNadrzedne(isNadrzedny);

                        klienciPowiazaniaFacade = KlienciPowiazaniaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        klienciPowiazaniaFacade.create(pp);

                        konto.getKlienciPowiazania().add(pp);
                        klient.getKlienciPowiazania().add(pp);
                    }
                }
            } catch (Exception ex) {
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Secured
    public Odpowiedz deletePacjent(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            Klienci object = klienciFacade.find(id);
            if (object != null) {
                klienciFacade = KlienciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                klienciFacade.remove(object);
                odpowiedz.setKomunikat("Dane pacjenta zostały usunięte!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie znaleziono pacjenta o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/email/{email}")
    @Secured
    public Odpowiedz getPacjentByEmail(
            @PathParam("email") String email) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Klienci object = klienciFacade.findByEmail(email);
            if (object != null) {
                odpowiedz.setDane(object.getJSON());
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono pacjenta z email: " + email);
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/usun-powiazanie-pacjenta")
    @Secured
    public Odpowiedz deletePowiazaniePacjenta(
            JSONObjectExt json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int idKonta = 0;
        int idKlienta = 0;

        try {
            if (json.get("kontoId") != null) {
                idKonta = Integer.parseInt(json.get("kontoId").toString());
            }
            if (json.get("klientId") != null) {
                idKlienta = Integer.parseInt(json.get("klientId").toString());
            }
        } catch (NumberFormatException ex) {
        }

        try {
            klienciPowiazaniaFacade = KlienciPowiazaniaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
            KlienciPowiazania klienciPowiazania = klienciPowiazaniaFacade.findBy(idKonta, idKlienta);
            if (klienciPowiazania != null) {
                klienciPowiazaniaFacade.remove(klienciPowiazania);
                odpowiedz.setKomunikat("Powiązanie klienta z kontem zostało usunięte!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie znaleziono powiązania!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }

        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dane-pacjenta")
    @Secured
    public Odpowiedz getDanePacjenta() {
        Odpowiedz odpowiedz = new Odpowiedz();
        String emailKonta = null;
        if (securityContext.getUserPrincipal() instanceof CustomClaims) {
            CustomClaims claims = (CustomClaims) securityContext.getUserPrincipal();
            emailKonta = claims.getEmail();
        }

        try {
            Klienci object = klienciFacade.findByEmail(emailKonta);
            if (object != null) {
                odpowiedz.setDane(object.getJSON());
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono pacjenta z email: " + emailKonta);
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
