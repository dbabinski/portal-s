/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.serwis;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
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
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.jpa.KonfiguracjaSerweraSms;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.KonfiguracjaSerweraSmsFacadeLocal;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateless
@Path("serwis/konfiguracja-serwera-sms")
public class RESTSerwisKonfiguracjaSerweraSms {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private KonfiguracjaSerweraSmsFacadeLocal konfiguracjaSerweraSmsFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getKonfiguracjeSerwerowSms() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<KonfiguracjaSerweraSms> list = konfiguracjaSerweraSmsFacade.findAll()
                    .stream()
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("konfiguracjaSerweraSms", jsonArray)
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
    public Odpowiedz getKonfiguracjaSerweraSms(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                KonfiguracjaSerweraSms object = konfiguracjaSerweraSmsFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono danych konfiguracyjnych serwera sms o ID: " + id);
                }
            } catch (Exception ex) {
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz setKonfiguracjaSerweraSms(
            @Context HttpServletRequest request,
            JSONObject json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            try {
                Integer id = null;
                String smsApiLogin = null;
                try {
                    if (json.get("id") != null) {
                        id = Integer.parseInt(json.get("id").toString());
                    }
                    if (json.get("smsApiLogin") != null) {
                        smsApiLogin = json.get("smsApiLogin").toString();
                    }
                } catch (NumberFormatException ex) {
                }
                if (id != null) {
                    //UPDATE
                    KonfiguracjaSerweraSms konfiguracjaSms = konfiguracjaSerweraSmsFacade.find(id);
                    if (smsApiLogin == null || smsApiLogin.equalsIgnoreCase("")) {
                        if (konfiguracjaSms != null) {
                            konfiguracjaSms.setJSON(json);
                            String haslo = konfiguracjaSms.getSerwerSmsPassword();
                            konfiguracjaSerweraSmsFacade = KonfiguracjaSerweraSmsFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                            konfiguracjaSerweraSmsFacade.edit(konfiguracjaSms, haslo);
                            odpowiedz.setKomunikat("Dane konfiguracyjne serwera sms zostały zaktualizowane");
                        } else {
                            odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono danych konfiguracyjnych serwera sms o ID: " + id);
                        }
                    } else {
                        if (konfiguracjaSms != null) {
                            konfiguracjaSms.setJSON(json);
                            String haslo = konfiguracjaSms.getSmsApiPassword();
                            konfiguracjaSerweraSmsFacade = KonfiguracjaSerweraSmsFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                            konfiguracjaSerweraSmsFacade.edit(konfiguracjaSms, haslo);
                            odpowiedz.setKomunikat("Dane konfiguracyjne serwera sms zostały zaktualizowane");
                        } else {
                            odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono danych konfiguracyjnych serwera sms o ID: " + id);
                        }
                    }
                } else {
                    //INSERT                    
                    konfiguracjaSerweraSmsFacade = KonfiguracjaSerweraSmsFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    konfiguracjaSerweraSmsFacade.create(new KonfiguracjaSerweraSms().setJSON(json));
                    odpowiedz.setKomunikat("Dane konfiguracyjne serwera sms zostały zapisane");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
                odpowiedz.setKomunikat("Błąd");
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Odpowiedz deleteKonfiguracjaSerweraSms(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            KonfiguracjaSerweraSms object = konfiguracjaSerweraSmsFacade.find(id);
            if (object != null) {
                konfiguracjaSerweraSmsFacade = KonfiguracjaSerweraSmsFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                konfiguracjaSerweraSmsFacade.remove(object);
                odpowiedz.setKomunikat("Dane konfiguracyjne serwera sms zostały usunięte!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie znaleziono danych konfiguracyjnych serwera sms o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
