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
import pl.sportal.jpa.KonfiguracjaSerweraPoczty;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.KonfiguracjaSerweraPocztyFacadeLocal;


@Stateless
@Path("serwis/konfiguracja-serwera-poczty")
public class RESTSerwisKonfiguracjaSerweraPoczty {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private KonfiguracjaSerweraPocztyFacadeLocal konfiguracjaSerweraPocztyFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getKonfiguracjeSerwerowPoczty() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<KonfiguracjaSerweraPoczty> list = konfiguracjaSerweraPocztyFacade.findAll()
                    .stream()
                    .sorted((KonfiguracjaSerweraPoczty t1, KonfiguracjaSerweraPoczty t2)
                            -> t1.getAdresSerweraPoczty().compareToIgnoreCase(t2.getAdresSerweraPoczty()))
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("konfiguracjaSerweraPoczty", jsonArray)
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
    public Odpowiedz getKonfiguracjaSerweraPoczty(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                KonfiguracjaSerweraPoczty object = konfiguracjaSerweraPocztyFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono danych konfiguracyjnych serwera pocztowego o ID: " + id);
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
    public Odpowiedz setKonfiguracjaSerweraPoczty(
            @Context HttpServletRequest request,
            JSONObject json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            try {
                Integer id = null;
                try {
                    if (json.get("id") != null) {
                        id = Integer.parseInt(json.get("id").toString());
                    }
                } catch (NumberFormatException ex) {
                }
                if (id != null) {
                    //UPDATE
                    KonfiguracjaSerweraPoczty konfiguracjaPoczty = konfiguracjaSerweraPocztyFacade.find(id);
                    if (konfiguracjaPoczty != null) {
                        String haslo = konfiguracjaPoczty.getHasloSerweraPoczty();
                        konfiguracjaPoczty.setJSON(json);
                        konfiguracjaSerweraPocztyFacade = KonfiguracjaSerweraPocztyFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        konfiguracjaSerweraPocztyFacade.edit(konfiguracjaPoczty, haslo);
                        odpowiedz.setKomunikat("Dane konfiguracyjne serwera pocztowego zostały zaktualizowane");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono danych konfiguracyjnych serwera pocztowego o ID: " + id);
                    }
                } else {
                    //INSERT                  
                    KonfiguracjaSerweraPoczty object = new KonfiguracjaSerweraPoczty();
                    konfiguracjaSerweraPocztyFacade = KonfiguracjaSerweraPocztyFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    konfiguracjaSerweraPocztyFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Dane konfiguracyjne serwera pocztowego zostały zapisane");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Odpowiedz deleteKonfiguracjaSerweraPoczty(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            KonfiguracjaSerweraPoczty object = konfiguracjaSerweraPocztyFacade.find(id);
            if (object != null) {
                konfiguracjaSerweraPocztyFacade = KonfiguracjaSerweraPocztyFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                konfiguracjaSerweraPocztyFacade.remove(object);
                odpowiedz.setKomunikat("Dane konfiguracyjne serwera pocztowego zostały usunięte!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie znaleziono danych konfiguracyjnych serwera pocztowego o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
