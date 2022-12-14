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
import pl.sportal.jpa.KomorkiOrganizacyjne;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.rest.Secured;
import pl.sportal.session.KomorkiOrganizacyjneFacadeLocal;


@Stateless
@Path("slowniki/komorki-organizacyjne")
public class RESTSlownikiKomorkiOrganizacyjne {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private KomorkiOrganizacyjneFacadeLocal komorkiOrganizacyjneFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getKomorkiOrganizacyjne() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<KomorkiOrganizacyjne> list = komorkiOrganizacyjneFacade.findAll()
                    .stream()
                    .sorted((KomorkiOrganizacyjne t1, KomorkiOrganizacyjne t2)
                            -> t1.getNazwa().compareToIgnoreCase(t2.getNazwa()))
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jsonArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("komorkiOrganizacyjne", jsonArray)
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

            //sprawdzenie wymaganych p??l                           
            KomorkiOrganizacyjne.POLA_WYMAGANE.stream().forEach(poleWymagane -> {
                if (json.isNull(poleWymagane)) {
                    RESTApplication.addToMap(
                            mapaUwag,
                            (String) poleWymagane,
                            "nie wype??niono pola " + Utilities.capitalizeFirstLetter(KomorkiOrganizacyjne.MAPA_POL.get(poleWymagane)));
                }
            });

            if (mapaUwag.get("nazwa") == null
                    && !komorkiOrganizacyjneFacade.czyNazwaUnikalna(json.getInteger("id"), json.getString("nazwa"))) {
                RESTApplication.addToMap(mapaUwag, "nazwa", "zarejestrowano ju?? kom??rk?? organizacyjn?? o nazwie " + json.getString("nazwa"));
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
    public Odpowiedz setKomorkiOrganizacyjne(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            Odpowiedz wynikParsowania = parse(request, json);
            if (wynikParsowania.isBlad() || wynikParsowania.isUwaga()) {
                return odpowiedz.setBlad(true).setKomunikat("Wykryto b????d w danych");
            }

            try {
                Integer id = null;
                try {
                    if (json.get("id") != null) {
                        id = Integer.parseInt(json.get("id").toString());
                    }
                } catch (NumberFormatException ex) {
                }
                KomorkiOrganizacyjne komorkaNadrzedna = null;
                try {
                    if (json.get("idKomorkiOrganizacyjnej") != null) {
                        Integer idKomorkiNadrzednej = Integer.parseInt(json.get("idKomorkiOrganizacyjnej").toString());
                        komorkaNadrzedna = komorkiOrganizacyjneFacade.find(idKomorkiNadrzednej);
                    }
                } catch (NumberFormatException ex) {
                }
                if (id != null) {
                    //UPDATE
                    KomorkiOrganizacyjne object = komorkiOrganizacyjneFacade.find(id);
                    if (object != null) {
                        komorkiOrganizacyjneFacade = KomorkiOrganizacyjneFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        komorkiOrganizacyjneFacade.edit(object
                                .setIdKomorkiOrganizacyjnej(komorkaNadrzedna).setJSON(json));
                        odpowiedz.setKomunikat("Kom??rka organizacyjna zosta?? zaktualizowana");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono kom??ki organizacyjnej o ID: " + id);
                    }
                } else {
                    //INSERT            
                    komorkiOrganizacyjneFacade = KomorkiOrganizacyjneFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    komorkiOrganizacyjneFacade.create(new KomorkiOrganizacyjne()
                            .setIdKomorkiOrganizacyjnej(komorkaNadrzedna).setJSON(json));
                    odpowiedz.setKomunikat("Kom??ka organizacyjna zosta??a zapisana");
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
    public Odpowiedz getKomorkiOrganizacyjne(
            @PathParam("id") String paramId
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                KomorkiOrganizacyjne object = komorkiOrganizacyjneFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono kom??rki organizacyjnej o ID: " + id);
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
    @Path("/unikalna-nazwa")
    public Odpowiedz czyNazwaKomorkiOrganizacyjnejUnikalna(
            @Context HttpServletRequest request,
            JSONObject json
    ) {
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
            if (!komorkiOrganizacyjneFacade.czyNazwaUnikalna(id, nazwa)) {
                odpowiedz.setBlad(true).setKomunikat("Istnieje ju?? kom??rka organizacyjna o nazwie: " + nazwa);
            }
        }
        return odpowiedz;
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Secured
    public Odpowiedz deleteKomorkiOrganizacyjne(
            @PathParam("id") String paramId
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            KomorkiOrganizacyjne object = komorkiOrganizacyjneFacade.find(id);
            if (object != null) {
                if (komorkiOrganizacyjneFacade.czyMogeUsunacKomorkeOrganizacyjna(id)) {
                    komorkiOrganizacyjneFacade = KomorkiOrganizacyjneFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    komorkiOrganizacyjneFacade.remove(object);
                    odpowiedz.setKomunikat("Kom??rka organizacyjna zosta??a usuni??ta!");
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie mo??na usun???? kom??rki organizacyjnej! Kom??rka organizacyjna zosta?? ju?? u??yta.");
                }
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie znaleziono kom??rki organizacyjnej o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
