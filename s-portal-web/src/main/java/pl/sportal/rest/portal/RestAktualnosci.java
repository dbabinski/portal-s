/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.portal;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import pl.sportal.common.utilities.JSONArrayBuilder;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.common.utilities.JSONObjectExt;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.portal.Aktualnosci;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.rest.Secured;
import pl.sportal.session.portal.AktualnosciFacadeLocal;


@Stateless
@Path("portal/aktualnosci")
public class RestAktualnosci {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private AktualnosciFacadeLocal aktualnosciFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getAktualnosci() {
        return getAktualnosciOpublikowane();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/opublikowane")
    public Odpowiedz getAktualnosciOpublikowane() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<Aktualnosci> list = aktualnosciFacade.findPublicated();
            JSONArray jsonArray = new JSONArray();
            list.stream().forEach((item) -> {
                jsonArray.add(item.getJSON());
            });
            odpowiedz.setDane(new JSONBuilder()
                    .put("aktualnosci", jsonArray)
                    .put("liczbaDostepnychRekordow", list.size())
                    .build());
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/wszystkie")
    public Odpowiedz getAktualnosciWszystkie() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<Aktualnosci> list = aktualnosciFacade.findAll();
            JSONArray jsonArray = new JSONArray();
            list.stream().forEach((item) -> {
                jsonArray.add(item.getJSON());
            });
            odpowiedz.setDane(new JSONBuilder()
                    .put("aktualnosci", jsonArray)
                    .put("liczbaDostepnychRekordow", list.size())
                    .build());
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
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
        JSONBuilder daneBuilder = new JSONBuilder();
        if (json != null) {
            HashMap<String, JSONArrayBuilder> mapaUwag = new HashMap<>();
            //sprawdzenie wymaganych pól                           
            Aktualnosci.POLA_WYMAGANE.stream().forEach(poleWymagane -> {
                if (json.isNull(poleWymagane)) {
                    RESTApplication.addToMap(
                            mapaUwag,
                            (String) poleWymagane,
                            "nie wypełniono pola " + Utilities.capitalizeFirstLetter(Aktualnosci.MAPA_POL.get(poleWymagane)));
                }
            });            
            //data dodania
            if (!json.isNull("dataDodania")) {
                Date dataDodania = json.getDate("dataDodania");
                if (dataDodania == null) {
                    RESTApplication.addToMap(mapaUwag, "dataDodania", "nieprawidłowy format daty");
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
    @Path("/")
    @Secured
    public Odpowiedz setArtykul(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            Odpowiedz wynikParsowania = parse(request, json);
            if (wynikParsowania.isBlad() || wynikParsowania.isUwaga()) {
                return odpowiedz.setBlad(true).setKomunikat("Wykryto błąd w danych");
            }

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
                    aktualnosciFacade = AktualnosciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    Aktualnosci object = aktualnosciFacade.find(id);
                    if (object != null) {
                        object.setJSON(json);
                        aktualnosciFacade.edit(object);
                        odpowiedz.setKomunikat("Artykuł został zaktualizowany");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono artykułu o ID: " + id);
                    }
                } else {
                    //INSERT                  
                    Aktualnosci object = new Aktualnosci();
                    aktualnosciFacade = AktualnosciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    aktualnosciFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Artykuł został zapisany");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Odpowiedz getArtykul(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                Aktualnosci object = aktualnosciFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono artykułu o ID: " + id);
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
    public Odpowiedz deleteArtykul(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            Aktualnosci object = aktualnosciFacade.find(id);
            if (object != null) {
                aktualnosciFacade = AktualnosciFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                aktualnosciFacade.remove(object);
                odpowiedz.setKomunikat("Artykuł został usunięty!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono artykułu o ID: " + id);
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
