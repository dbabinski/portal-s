/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.sportal.rest.powiadomienia;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONArray;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.jpa.SzablonyPowiadomien;
import pl.sportal.rest.Odpowiedz;
import pl.sportal.session.SzablonyPowiadomienFacadeLocal;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateless
@Path("powiadomienia-szablony")
public class RESTPowiadomieniaSzablonyPowiadomien {
    
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");
    
    @Context
    SecurityContext securityContext;
    
    @EJB
    private SzablonyPowiadomienFacadeLocal szablonyPowiadomienFacade;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getSzablonyPowiadomien() {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            List<SzablonyPowiadomien> list = szablonyPowiadomienFacade.findAll()
                    .stream()
                    .collect(Collectors.toCollection(LinkedList::new));
            if ( list != null ) {
                JSONArray jSONArray = new JSONArray();
                list.stream().forEach((item) -> {
                    jSONArray.add(item.getJSON());
                });
                odpowiedz.setDane(new JSONBuilder()
                        .put("szablonyPowiadomien", jSONArray)
                        .build());
            }
        } catch (Exception e) {
            odpowiedz.setBlad(true).setKomunikat(e.getMessage());
        }
        return odpowiedz;
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Odpowiedz getSzablonPowiadomienia(@PathParam("id") String paramId){
        Odpowiedz odpowiedz = new Odpowiedz();
        
        int id = 0;
        try {
            id = Integer.parseInt(paramId);
        } catch (NumberFormatException ex) {
        }
        if (id > 0) {
            try {
                SzablonyPowiadomien object = szablonyPowiadomienFacade.find(id);
                if (object != null) {
                    odpowiedz.setDane(object.getJSON());
                } else {
                    odpowiedz.setBlad(true).setKomunikat("Nie odnaleziono powiadomienia o ID: " + id);
                }
            } catch (Exception ex) {
                odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }
        }
        return odpowiedz;
    }
    
    
}
