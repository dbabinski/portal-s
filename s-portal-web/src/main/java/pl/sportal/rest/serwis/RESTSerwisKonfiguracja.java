/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest.serwis;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONObject;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.jpa.Konfiguracja;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.KonfiguracjaFacadeLocal;


@Stateless
@Path("serwis/konfiguracja")
public class RESTSerwisKonfiguracja {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getUstawieniaPacjenta() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Konfiguracja object = konfiguracjaFacade.find();
            if (object == null) {
                object = new Konfiguracja();
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("konfiguracja", object.getJSON())
                    .build());
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/recaptchaSiteKey")
    public Odpowiedz getRecaptchaSiteKey() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Konfiguracja object = konfiguracjaFacade.find();
            if (object == null) {
                object = new Konfiguracja();
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("recaptchaSiteKey", object.getRecaptchaSiteKey())
                    .build());
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz setUstawieniaPacjenta(
            @Context HttpServletRequest request,
            JSONObject json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        if (json != null) {
            Konfiguracja object = konfiguracjaFacade.find();
            if (object == null) {
                object = new Konfiguracja();
            }
            try {
                if (object.getId() != null) {
                    //UPDATE
                    konfiguracjaFacade = KonfiguracjaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    konfiguracjaFacade.edit(object.setJSON(json));
                    odpowiedz.setKomunikat("Konfiguracja została zaktualizowana");
                } else {
                    //INSERT
                    konfiguracjaFacade = KonfiguracjaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    konfiguracjaFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Konfiguracja została zapisana");
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
    @Path("/logo")
    public Odpowiedz getLogo() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Konfiguracja object = konfiguracjaFacade.find();
            if (object == null) {
                object = new Konfiguracja();
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("logo", object.getLogo())
                    .build());
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/domena")
    public Odpowiedz getDomena() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Konfiguracja object = konfiguracjaFacade.find();
            if (object == null) {
                object = new Konfiguracja();
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("domena", object.getDomena())
                    .build());
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
