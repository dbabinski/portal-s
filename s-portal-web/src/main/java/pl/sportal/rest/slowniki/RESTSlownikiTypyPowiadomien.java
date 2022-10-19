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
import pl.sportal.jpa.TypyPowiadomien;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.rest.Secured;
import pl.sportal.session.TypyPowiadomienFacadeLocal;
import static pl.sportal.rest.slowniki.RESTSlownikiTypyPowiadomien.LOGGER;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateless
@Path("slowniki/typy-powiadomien")
public class RESTSlownikiTypyPowiadomien {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private TypyPowiadomienFacadeLocal typyPowiadomienFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getTypyPowiadomien() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<TypyPowiadomien> list = typyPowiadomienFacade.findAll()
                    .stream()
                    .sorted((TypyPowiadomien t1, TypyPowiadomien t2)
                            -> t1.getOpis().compareToIgnoreCase(t2.getOpis()))
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("typyPowiadomien", jsonArray)
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
            TypyPowiadomien.POLA_WYMAGANE.stream().forEach(poleWymagane -> {
                if (json.isNull(poleWymagane)) {
                    RESTApplication.addToMap(
                            mapaUwag,
                            (String) poleWymagane,
                            "nie wypełniono pola " + Utilities.capitalizeFirstLetter(TypyPowiadomien.MAPA_POL.get(poleWymagane)));
                }
            });

            if (mapaUwag.get("opis") == null
                    && !typyPowiadomienFacade.czyNazwaUnikalna(json.getInteger("id"), json.getString("opis"))) {
                RESTApplication.addToMap(mapaUwag, "opis", "zarejestrowano już powiadomienie o nazwie " + json.getString("opis"));
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
    @Path("/{id}")
    public Odpowiedz getTypPowiadomienia(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                TypyPowiadomien object = typyPowiadomienFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono typu dokumentu o ID: " + id);
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
    @Secured
    public Odpowiedz setTypyPowiadomien(
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
                    TypyPowiadomien object = typyPowiadomienFacade.find(id);
                    if (object != null) {
                        object.setJSON(json);
                        typyPowiadomienFacade = TypyPowiadomienFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        typyPowiadomienFacade.edit(object);
                        odpowiedz.setKomunikat("Typ powiadomienia został zaktualizowany");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono typu powiadomienia o ID: " + id);
                    }
                } else {
                    //INSERT                  
                    TypyPowiadomien object = new TypyPowiadomien();
                    typyPowiadomienFacade = TypyPowiadomienFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    typyPowiadomienFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Typ powiadomienia został zapisany");
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
    @Path("/unikalna-nazwa")
    public Odpowiedz czyTypPowiadomieniaUnikalny(
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
            String nazwa = Utilities.nullToString(json.get("nazwa")).toString();
            if (!typyPowiadomienFacade.czyNazwaUnikalna(id, nazwa)) {
                odpowiedz.setBlad(true).setKomunikat("Istnieje już typ grupy o nazwie: " + nazwa);
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Secured
    public Odpowiedz deleteTypyPowiadomien(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                TypyPowiadomien object = typyPowiadomienFacade.find(id);
                if (object != null) {
                    typyPowiadomienFacade = TypyPowiadomienFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    typyPowiadomienFacade.remove(object);
                    odpowiedz.setKomunikat("Typ powiadomienia został usunięty!");

                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie znaleziono typu powiadomienia o podanym ID!");
                }
            } catch (Exception ex) {
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }
}
