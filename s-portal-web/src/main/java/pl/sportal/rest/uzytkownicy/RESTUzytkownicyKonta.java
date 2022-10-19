/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.uzytkownicy;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.naming.NamingException;
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
import pl.sportal.common.utilities.Validator;
import pl.sportal.rest.CustomClaims;
import pl.sportal.jpa.Grupy;
import pl.sportal.jpa.Konta;
import pl.sportal.jpa.KlienciPowiazania;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.rest.RESTApplication;
import pl.sportal.rest.Secured;
import pl.sportal.session.GrupyFacadeLocal;
import pl.sportal.session.KonfiguracjaFacadeLocal;
import pl.sportal.session.KontaFacadeLocal;
import pl.sportal.session.KlienciFacadeLocal;
import pl.sportal.session.KlienciPowiazaniaFacadeLocal;


@Stateless
@Path("uzytkownicy/konta")
public class RESTUzytkownicyKonta {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private GrupyFacadeLocal grupyFacade;
    @EJB
    private KontaFacadeLocal kontaFacade;
    @EJB
    private KlienciFacadeLocal klienciFacade;
    @EJB
    private KlienciPowiazaniaFacadeLocal klienciPowiazaniaFacade;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Secured
    public Odpowiedz getKonta() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<Konta> list = kontaFacade.findAll()
                    .stream()
                    .sorted(Konta.COMPARATOR_BY_LOGIN)
                    .collect(Collectors.toCollection(LinkedList::new));
            if (list != null) {
                JSONArray jsonArray = new JSONArray();
                list.stream().forEach((item) -> {
                    //powiązani pacjenci                    
                    jsonArray.add(new JSONBuilder(item.getJSON())
                            .put("pacjenci", new JSONArrayBuilder()
                                    .addAll(klienciFacade.getKlienci(item.getId()))
                                    .build())
                            .build());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("konta", jsonArray)
                        .build());
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/nadrzedni")
    public Odpowiedz getPacjenciNadrzedni(
            @Context HttpServletRequest request,
            JSONObjectExt json) {

        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(json.get("konto").toString());
        } catch (NumberFormatException ex) {
        }
        if (id != 0) {
            ArrayList<KlienciPowiazania> powiazania = klienciPowiazaniaFacade.getPowiazaniaKonta(id);
            JSONArray jsonArray = new JSONArray();
            if (!powiazania.isEmpty()) {
                for (KlienciPowiazania powiazanie : powiazania) {
                    if (powiazanie.getNadrzedne()) {
                        jsonArray.add(powiazanie.getKlient().getJSON());
                    }
                }
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("nadrzedni", jsonArray)
                    .build());
        }
        return odpowiedz;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/podrzedni")
    public Odpowiedz getPacjenciPodrzedni(
            @Context HttpServletRequest request,
            JSONObjectExt json) {

        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(json.get("konto").toString());
        } catch (NumberFormatException ex) {
        }
        if (id != 0) {
            ArrayList<KlienciPowiazania> powiazania = klienciPowiazaniaFacade.getPowiazaniaKonta(id);
            JSONArray jsonArray = new JSONArray();
            if (!powiazania.isEmpty()) {
                for (KlienciPowiazania powiazanie : powiazania) {
                    if (!powiazanie.getNadrzedne()) {
                        jsonArray.add(powiazanie.getKlient().getJSON());
                    }
                }
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("podrzedni", jsonArray)
                    .build());
        }
        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/lista")
    @Secured
    public Odpowiedz getPacjenci(
            @Context HttpServletRequest request,
            JSONObjectExt json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            boolean refresh = false;
            Integer limit = null;
            Integer offset = null;
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
            String filter = json.getString("filter");
            List<Konta> list = kontaFacade.find(filter, null);
            int liczbaDostepnychRekordow = list.size();
            limit = limit != null ? limit : 50;
            list = list.stream()
                    .sorted(Konta.COMPARATOR_BY_LOGIN)
                    .skip(refresh ? 0 : (offset != null ? offset : 0))
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
                        .put("konta", jsonArray)
                        .put("liczbaDostepnychRekordow", liczbaDostepnychRekordow)
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
    @Secured
    public Odpowiedz parse(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        JSONBuilder daneBuilder = new JSONBuilder();
        if (json != null) {
            HashMap<String, JSONArrayBuilder> mapaUwag = new HashMap<>();

            //sprawdzenie wymaganych pól                           
            Konta.POLA_WYMAGANE.stream().forEach(poleWymagane -> {
                if (json.isNull(poleWymagane)) {
                    RESTApplication.addToMap(
                            mapaUwag,
                            (String) poleWymagane,
                            "nie wypełniono pola " + Utilities.capitalizeFirstLetter(Konta.MAPA_POL.get(poleWymagane)));
                }
            });

            if (json.getString("login") != null && mapaUwag.get("login") == null
                    && !kontaFacade.czyLoginUnikalny(json.getInteger("id"), json.getString("login"))) {
                RESTApplication.addToMap(mapaUwag, "opis", "zarejestrowano już konto z loginem " + json.getString("login"));
            }
            //email
            if (json.getString("email") != null) {
                if (!Validator.isValidEmail(json.getString("email"))) {
                    RESTApplication.addToMap(mapaUwag, "email", "nieprawidłowy format adresu e-mail");
                }
            }
            //akceptacja regulaminu
            if (!json.isNull("akceptacjaRegulaminu")) {
                Date akceptacjaRegulaminu = json.getDateTimeSec("akceptacjaRegulaminu");
                if (akceptacjaRegulaminu == null) {
                    RESTApplication.addToMap(mapaUwag, "akceptacjaRegulaminu", "nieprawidłowy format daty i czasu akceptacji regulaminu");
                }
            }

            //akceptacja regulaminu            
            if (!json.isNull("blokadaKonta") && json.getBooleanSimple("blokadaKonta") && !json.isNull("blokadaKontaDo")) {
                Date akceptacjaRegulaminu = json.getDateTimeSec("blokadaKontaDo");
                if (akceptacjaRegulaminu == null) {
                    RESTApplication.addToMap(mapaUwag, "blokadaKontaDo", "nieprawidłowy format daty i czasu blokady konta");
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
    public Odpowiedz setKonto(
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
                Grupy grupa = null;
                try {
                    if (json.get("idGrupa") != null) {
                        Integer idGrupy = Integer.parseInt(json.get("idGrupa").toString());
                        grupa = grupyFacade.find(idGrupy);
                    }
                } catch (NumberFormatException ex) {
                }

                if (id != null) {
                    //UPDATE
                    Konta konto = kontaFacade.find(id);
                    if (konto != null) {
                        kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                        kontaFacade.edit(konto.setJSON(json).setIdGrupy(grupa));
                        odpowiedz.setKomunikat("Konto zostało zaktualizowane");
                    } else {
                        odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono konta o ID: " + id);
                    }
                } else {
                    //INSERT       
                    kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    Konta konto = new Konta().setHaslo().setJSON(json).setIdGrupy(grupa);
                    kontaFacade.create(konto);
                    odpowiedz.setKomunikat("Konto zostało zapisane");
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
    @Secured
    public Odpowiedz getKonto(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                Konta konto = kontaFacade
                        .find(id);
                if (konto != null) {
                    odpowiedz.setDane(new JSONBuilder(konto.getJSON()).build());
                    /*
                    odpowiedz.setDane(new JSONBuilder(konto.getJSON())
                            .put("kontoNadrzedne", kontaPowiazaniaFacade.getKontoNadrzedne(konto.getId()))
                            .put("kontaPodrzedne", new JSONArrayBuilder()
                                    .addAll(kontaPowiazaniaFacade.getKontaPodrzedne(konto.getId()))
                                    .build())
                            .put("pacjenci", new JSONArrayBuilder()
                                    .addAll(pacjenciFacade.getPacjenci(konto.getId()))
                                    .build())
                            .build());
                     */
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono konta o ID: " + id);
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
    public Odpowiedz deleteKonto(
            @PathParam("id") String paramId) {
        Odpowiedz odpowiedz = new Odpowiedz();
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        try {
            Konta konto = kontaFacade.find(id);
            if (konto != null) {
                kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                kontaFacade.remove(konto);
                odpowiedz.setKomunikat("Konto zostało usunięte!");
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono  o podanym ID!");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/aktywacja/{uuid}")
    public Odpowiedz aktywacja(
            @PathParam("uuid") String uuid) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Konta konto = kontaFacade.findByUUID(uuid);
            if (konto != null) {
                if (konto.isKontoAktywne()) {
                    odpowiedz.setUwaga(true).setKomunikat("Konto było już aktywowane");
                } else {
                    kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    kontaFacade.edit(konto.setKontoAktywne(true));
                    odpowiedz.setKomunikat("Konto zostało aktywowane");
                }
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono konta");
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/dane-konta")
    @Secured
    public Odpowiedz getDaneKonta() {
        Odpowiedz odpowiedz = new Odpowiedz();
        String UUIDkonta = null;
        if (securityContext.getUserPrincipal() instanceof CustomClaims) {
            CustomClaims claims = (CustomClaims) securityContext.getUserPrincipal();
            UUIDkonta = claims.getUUID();
        }

        try {
            kontaFacade = KontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
            Konta konto = kontaFacade.findByUUID(UUIDkonta);
            if (konto != null) {
                odpowiedz.setDane(konto.getJSON());
            } else {
                odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono danych wskazanego konta");
            }
        } catch (NamingException ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
