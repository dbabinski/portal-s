/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import pl.sportal.common.utilities.JSONBuilder;


@Stateless
@Path("wersja")
public class RESTWersja {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/")
    public Odpowiedz getWersja(@Context ServletContext servletContext) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            Properties properties = new Properties();
            try {
                InputStream inputStream = servletContext
                        .getResourceAsStream("/WEB-INF/classes/application.properties");
                properties.load(inputStream);
                odpowiedz.setDane(new JSONBuilder()
                        .put("name", properties.getProperty("Application.name"))
                        .put("version", properties.getProperty("Application.version"))
                        .put("date", properties.getProperty("Application.date"))
                        .build());
            } catch (IOException ex) {
                return odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
            }

        } catch (Exception ex) {
            odpowiedz.setBlad(true).setKomunikat(ex.getMessage());
        }
        return odpowiedz;
    }

}
