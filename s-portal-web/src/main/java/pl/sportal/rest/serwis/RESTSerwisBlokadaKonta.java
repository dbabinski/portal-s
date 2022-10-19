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
import pl.sportal.jpa.BlokadaKonta;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.BlokadaKontaFacadeLocal;


@Stateless
@Path("serwis/blokada-konta")
public class RESTSerwisBlokadaKonta {
    
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");
    
    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private BlokadaKontaFacadeLocal blokadaKontaFacade;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getUstawieniaPacjenta() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            BlokadaKonta object = blokadaKontaFacade.find();
            if (object == null) {
                object = new BlokadaKonta();
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("blokadaKonta", object.getJSON())
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
            BlokadaKonta object = blokadaKontaFacade.find();
            if (object == null) {
                object = new BlokadaKonta();
            }
            try {
                if (object.getId() != null) {
                    //UPDATE                                 
                    blokadaKontaFacade = BlokadaKontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    blokadaKontaFacade.edit(object.setJSON(json));
                    odpowiedz.setKomunikat("Blokada bonta została zaktualizowana");
                } else {
                    //INSERT
                    blokadaKontaFacade = BlokadaKontaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    blokadaKontaFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Blokada konta została zapisana");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }
}
