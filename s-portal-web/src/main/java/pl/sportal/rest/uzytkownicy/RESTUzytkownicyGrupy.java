/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.uzytkownicy;

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
import pl.sportal.jpa.Grupy;
import pl.sportal.jpa.TypyGrup;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.rest.Secured;
import pl.sportal.session.GrupyFacadeLocal;
import pl.sportal.session.TypyGrupFacadeLocal;


@Stateless
@Path("uzytkownicy/grupy")
public class RESTUzytkownicyGrupy {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private GrupyFacadeLocal grupyFacade;
    @EJB
    private TypyGrupFacadeLocal typyGrupFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getGrupy() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<Grupy> list = grupyFacade.findAll()
                    .stream()
                    .sorted((Grupy g1, Grupy g2)
                            -> g1.getOpis().compareToIgnoreCase(g2.getOpis()))
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("grupy", jsonArray)
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
            Grupy.POLA_WYMAGANE.stream().forEach(poleWymagane -> {
                if (json.isNull(poleWymagane)) {
                    RESTApplication.addToMap(
                            mapaUwag,
                            (String) poleWymagane,
                            "nie wypełniono pola " + Utilities.capitalizeFirstLetter(Grupy.MAPA_POL.get(poleWymagane)));
                }
            });

            if (mapaUwag.get("opis") == null
                    && !grupyFacade.czyOpisUnikalny(json.getInteger("id"), json.getString("opis"))) {
                RESTApplication.addToMap(mapaUwag, "opis", "zarejestrowano już grupę z opisem " + json.getString("opis"));
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
    public Odpowiedz setGrupy(
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
                TypyGrup typGrupy = null;
                try {
                    if (json.get("idTypGrupy") != null) {
                        Integer idTypGrupy = Integer.parseInt(json.get("idTypGrupy").toString());
                        typGrupy = typyGrupFacade.find(idTypGrupy);
                    }
                } catch (NumberFormatException ex) {
                }
                if (id != null) {
                    //UPDATE
                    Grupy grupa = grupyFacade.find(id);
                    if (grupa != null) {
                        grupyFacade = GrupyFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        grupyFacade.edit(grupa.setJSON(json).setTypGrupy(typGrupy));
                        odpowiedz.setKomunikat("Grupa została zaktualizowana");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono grupy o ID: " + id);
                    }
                } else {
                    //INSERT
                    grupyFacade = GrupyFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    grupyFacade.create(new Grupy().setJSON(json).setTypGrupy(typGrupy));
                    odpowiedz.setKomunikat("Grupa została zapisana");
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
    public Odpowiedz getGrupa(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                Grupy grupa = grupyFacade.find(id);
                if (grupa != null) {
                    odpowiedz.setDane(grupa.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono grupy o ID: " + id);
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
            if (!grupyFacade.czyOpisUnikalny(id, opis)) {
                odpowiedz.setBlad(true).setKomunikat("Istnieje już grupa o nazwie: " + opis);
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Secured
    public Odpowiedz deleteGrupa(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            Grupy grupa = grupyFacade.find(id);
            if (grupa != null) {
                grupyFacade = GrupyFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                grupyFacade.remove(grupa);
                odpowiedz.setKomunikat("Grupa została usunięta!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono grupy o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
