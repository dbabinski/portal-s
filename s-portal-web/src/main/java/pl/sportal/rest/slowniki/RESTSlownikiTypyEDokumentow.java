/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.slowniki;

import java.util.HashMap;
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
import pl.sportal.common.utilities.JSONArrayBuilder;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.common.utilities.JSONObjectExt;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.TypyEDokumentow;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.rest.Secured;
import pl.sportal.session.TypyEDokumentowFacadeLocal;

@Stateless
@Path("slowniki/typy-edokumentow")
public class RESTSlownikiTypyEDokumentow {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private TypyEDokumentowFacadeLocal typyEDokumentowFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getTypyEDokumentow() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<TypyEDokumentow> list = typyEDokumentowFacade.findAll()
                    .stream()
                    .sorted((TypyEDokumentow t1, TypyEDokumentow t2)
                            -> t1.getOpis().compareToIgnoreCase(t2.getOpis()))
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("typyEDokumentow", jsonArray)
                        .build());
            }
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
            TypyEDokumentow.POLA_WYMAGANE.stream().forEach(poleWymagane -> {
                if (json.isNull(poleWymagane)) {
                    RESTApplication.addToMap(
                            mapaUwag,
                            (String) poleWymagane,
                            "nie wypełniono pola " + Utilities.capitalizeFirstLetter(TypyEDokumentow.MAPA_POL.get(poleWymagane)));
                }
            });

            if (mapaUwag.get("opis") == null
                    && !typyEDokumentowFacade.czyOpisUnikalny(json.getInteger("id"), json.getString("opis"))) {
                RESTApplication.addToMap(mapaUwag, "opis", "zarejestrowano już dokument o opisie " + json.getString("opis"));
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
    public Odpowiedz setTypyEDokumentow(
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
                    TypyEDokumentow object = typyEDokumentowFacade.find(id);
                    if (object != null) {
                        object.setJSON(json);
                        typyEDokumentowFacade = TypyEDokumentowFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        typyEDokumentowFacade.edit(object);
                        odpowiedz.setKomunikat("Typ e-dokumentu został zaktualizowany");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono typu e-dokumentu o ID: " + id);
                    }
                } else {
                    //INSERT                  
                    TypyEDokumentow object = new TypyEDokumentow();
                    typyEDokumentowFacade = TypyEDokumentowFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    typyEDokumentowFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Typ dokumentu został zapisany");
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
    public Odpowiedz getTypEDokumentu(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                TypyEDokumentow object = typyEDokumentowFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono typu e-dokumentu o ID: " + id);
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
    @Path("/unikalny-opis")
    public Odpowiedz czyOpisUnikalny(
            @Context HttpServletRequest request,
            JSONObject json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            int id = 0;
            try {
                if (json.get("id") != null) {
                    id = Integer.parseInt(json.get("id").toString());
                }
            } catch (NumberFormatException ex) {
            }
            String opis = Utilities.nullToString(json.get("opis")).toString();
            if (!typyEDokumentowFacade.czyOpisUnikalny(id, opis)) {
                odpowiedz.setBlad(true).setKomunikat("Istnieje już typ dokumentu o nazwie: " + opis);
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Secured
    public Odpowiedz deleteTypyEDokumentow(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            TypyEDokumentow object = typyEDokumentowFacade.find(id);
            if (object != null) {
                if (typyEDokumentowFacade.czyMogeUsunacTypEDokumentu(id)) {
                    typyEDokumentowFacade = TypyEDokumentowFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    typyEDokumentowFacade.remove(object);
                    odpowiedz.setKomunikat("Typ e-dokumentu został usunięty!");
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie można usunąć typu e-dokumentu! Typ e-dokumentu został już użyty.");
                }
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie znaleziono typu e-dokumentu o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
