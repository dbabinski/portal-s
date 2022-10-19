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
import pl.sportal.jpa.ParametryHasla;
import pl.sportal.rest.IpAdress;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.ParametryHaslaFacadeLocal;



@Stateless
@Path("serwis/parametry-hasla")
public class RESTSerwisParametryHasla {
    
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");
    
    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private ParametryHaslaFacadeLocal parametryHaslaFacade;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getUstawieniaPacjenta() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            ParametryHasla object = parametryHaslaFacade.find();
            if (object == null) {
                object = new ParametryHasla();
            }
            odpowiedz.setDane(new JSONBuilder()
                    .put("parametry hasła", object.getJSON())
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
            ParametryHasla object = parametryHaslaFacade.find();
            if (object == null) {
                object = new ParametryHasla();
            }
            try {
                if (object.getId() != null) {
                    //UPDATE       
                    parametryHaslaFacade = ParametryHaslaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    parametryHaslaFacade.edit(object.setJSON(json));
                    odpowiedz.setKomunikat("Parametry hasła zostały zaktualizowane");
                } else {
                    //INSERT       
                    parametryHaslaFacade = ParametryHaslaFacadeLocal.create(securityContext, IpAdress.getClientIpAddr(httpRequest));
                    parametryHaslaFacade.create(object.setJSON(json));
                    odpowiedz.setKomunikat("Parametry hasła zostały zapisane");
                }
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }
}
