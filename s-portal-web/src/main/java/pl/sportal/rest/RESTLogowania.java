/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONArray;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.common.utilities.JSONObjectExt;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.Logowania;
import pl.sportal.session.LogowaniaFacadeLocal;


@Stateless
@Path("logowania")
public class RESTLogowania {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    @Context
    SecurityContext securityContext;
    @Context
    HttpServletRequest httpRequest;
    @EJB
    private LogowaniaFacadeLocal logowaniaFacade;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    @Secured
    public Odpowiedz getLogowania(
            @Context HttpServletRequest request,
            JSONObjectExt json
    ) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            boolean refresh = false;
            Integer limit = null;
            Integer offset = null;
            String filterText = null;
            Date filterDataOd = null;
            Date filterDataDo = null;
            String sort = null;

            if (json != null) {
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
                try {
                    filterText = json.getString("filterText");
                } catch (Exception ex) {
                }
                try {
                    filterDataOd = json.getDate("filterDataOd");
                } catch (Exception ex) {
                }
                try {
                    filterDataDo = json.getDate("filterDataDo");
                } catch (Exception ex) {
                }
                try {
                    sort = json.getString("sort");
                } catch (Exception ex) {
                }
            }
            if (offset == null) {
                offset = 0;
            }
            sort = Utilities.nullToString(sort).toString();
            List<Logowania> list = logowaniaFacade.find(filterText, filterDataOd, filterDataDo, null);
            int liczbaDostepnychRekordow = list.size();
            limit = limit != null ? limit : 50;
            list = list.stream()
                    .sorted(sort.equalsIgnoreCase("DESC") ? Logowania.COMPARATOR_BY_ID.reversed() : Logowania.COMPARATOR_BY_ID)
                    .skip(refresh ? 0 : offset)
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
                        .put("logowania", jsonArray)
                        .put("liczbaDostepnychRekordow", liczbaDostepnychRekordow)
                        .build());
            }
        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }
}
