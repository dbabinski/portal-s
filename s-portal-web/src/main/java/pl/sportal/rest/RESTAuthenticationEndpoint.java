    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.rest;

import io.jsonwebtoken.Claims;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.json.simple.JSONObject;
import pl.sportal.common.utilities.JSONBuilder;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.AktualnieZalogowani;
import pl.sportal.jpa.Dostep;
import pl.sportal.jpa.Konfiguracja;
import pl.sportal.jpa.Konta;
import pl.sportal.jpa.KontaGrupy;
import pl.sportal.jpa.Logowania;
import pl.sportal.jpa.Uprawnienia;
import pl.sportal.session.AktualnieZalogowaniPortalFacadeLocal;
import pl.sportal.session.DostepFacadeLocal;
import pl.sportal.session.KonfiguracjaFacadeLocal;
import pl.sportal.session.KontaFacadeLocal;
import pl.sportal.session.LoginException;
import pl.sportal.session.LogowaniaFacadeLocal;
import pl.sportal.session.UprawnieniaFacadeLocal;

//@CookieParam(JWT) Cookie cookie
/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateless
@Path("autentykacja")
public class RESTAuthenticationEndpoint {

    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");
    @EJB
    private DostepFacadeLocal dostepFacade;
    @EJB
    private KontaFacadeLocal kontaFacade;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;
    @EJB
    private UprawnieniaFacadeLocal uprawnieniaFacade;
    @EJB
    private LogowaniaFacadeLocal logowaniaFacade;
    @EJB
    private AktualnieZalogowaniPortalFacadeLocal aktualnieZalogowaniPortalFacade;
            
    @Context
    SecurityContext securityContext;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/login")
    public Response login(
            @Context HttpServletRequest req,
            JSONObject json) {
        Odpowiedz odpowiedz = new Odpowiedz();
        try {
            String login = (String) json.get("login");
            String haslo = (String) json.get("haslo");
            String ip = IpAdress.getClientIpAddr(req);
            

            Konta konto = kontaFacade.checkLogin(login, haslo, ip);
            Dostep dostep = dostepFacade.find();
            if (dostep != null && dostep.getIpLogowanie() != null) {
                //TODO: sprawdzenie IP   
            }
            if (konto != null) {
                String token = UUID.randomUUID().toString();
                int dlugoscSesji = dostep != null ? dostep.getDlugoscSesjiHttp() : JSONWebTokenBuilder.SESSION_TIME;

                Date dataWygasniecia = Utilities.calculateDate(new Date(), Calendar.MINUTE, dlugoscSesji);

                Konfiguracja konfiguracja = konfiguracjaFacade.find();
                
                String domena = konfiguracja.getDomena();
                
                Uprawnienia uprawnienia = getUprawnienia(konto);
                              
                
                String jwt = new JSONWebTokenBuilder()
                        .put(ClaimsExt.ID, UUID.randomUUID().toString())
                        .put(ClaimsExt.ISSUED_AT, new Date().getTime())
                        .put(ClaimsExt.EXPIRATION, dataWygasniecia.getTime())
                        .put(ClaimsExt.ISSUER, req.getContextPath())
                        .put(ClaimsExt.AUTH_TIME, new Date().getTime())
                        .put(ClaimsExt.SCOPE, konto.getIdGrupy() != null ? konto.getIdGrupy().getOpis() : null)
                        .put(ClaimsExt.TOKEN, token)
                        .put(ClaimsExt.UUID, konto.getUUID())
                        .put(ClaimsExt.EMAIL, konto.getEmail())
                        .put(ClaimsExt.LOGIN, konto.getLogin())
                        .put(ClaimsExt.SESSION_TIME, dlugoscSesji)
                        .put(ClaimsExt.DOMAIN, domena)
                        .put(ClaimsExt.PERMISSIONS, uprawnienia.getJSON().toJSONString())
                        .build();

                NewCookie jwtCookie = new NewCookie(JSONWebTokenBuilder.JWT_COOKIE, jwt, "/", domena,
                        NewCookie.DEFAULT_VERSION, null, NewCookie.DEFAULT_MAX_AGE, dataWygasniecia, false, true);
                String meta = new JSONBuilder()
                        .put(ClaimsExt.EMAIL, konto.getEmail())
                        .put(ClaimsExt.PERMISSIONS, getPermissionsJSON(uprawnienia))
                        .build().toJSONString();
                NewCookie metaCookie = new NewCookie(JSONWebTokenBuilder.META_COOKIE, Base64.getEncoder().encodeToString(meta.getBytes()), "/", domena,
                        NewCookie.DEFAULT_VERSION, null, NewCookie.DEFAULT_MAX_AGE, dataWygasniecia, false, false);
                
                Logowania l = new Logowania()
                        .setIp(ip)
                        .setUuidKonta(konto.getUUID())
                        .setOpis("Logowanie udane");
                
                AktualnieZalogowani a = new AktualnieZalogowani()
                        .setUuid(konto.getUUID())
                        .setDataWygasniecia(jwtCookie.getExpiry())
                        .setIp(ip)
                        .setEmail(konto.getEmail());
                
                aktualnieZalogowaniPortalFacade.aktualizuj(a);
                
                logowaniaFacade.create(l);
                
                return Response.ok(
                        new JSONBuilder()
                                .put("blad", false)
                                .put("komunikat", "Zalogowano pomyślnie")
                                .put(ClaimsExt.TOKEN, token)
                                .put(ClaimsExt.EMAIL, konto.getEmail())
                                .put(ClaimsExt.LOGIN, konto.getLogin())
                                .put(ClaimsExt.SCOPE, konto.getIdGrupy() != null ? konto.getIdGrupy().getId() : null)
                                .put(ClaimsExt.PERMISSIONS, getPermissionsJSON(uprawnienia))
                                .build())
                        .cookie(jwtCookie)
                        .cookie(metaCookie)
                        .build();        
            } else {
                return Response.ok(odpowiedz.setBlad(true).setKomunikat("W czasie logowania wystąpił błąd")).build();
            }
        } catch (LoginException ex) {
            return Response.ok(odpowiedz.setBlad(true).setKomunikat(ex.getMessage())).build();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
            return Response.ok(odpowiedz.setBlad(true).setKomunikat("W czasie logowania wystąpił błąd")).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/logout")
    public Response logout(@Context HttpServletRequest req) {
            
        Konfiguracja konfiguracja = konfiguracjaFacade.find();
        String ip = IpAdress.getClientIpAddr(req);
        //usuwanie ciasteczka   
        
        Logowania l = null;
        String uuid = "";
        
        for(Cookie c : req.getCookies()){
            if(c.getName().equals(JSONWebTokenBuilder.JWT_COOKIE)){
                final Claims claims = new JSONWebTokenBuilder(c.getValue()).getClaims();
                uuid = ((CustomClaims) claims).getUUID();
            }
        }
        
        l = new Logowania()
                .setIp(ip)
                .setUuidKonta(uuid)
                .setOpis("Wylogowanie udane");
        
        AktualnieZalogowani a = new AktualnieZalogowani()
                .setUuid(uuid)
                .setDataWygasniecia(null)
                .setIp(ip);

        aktualnieZalogowaniPortalFacade.aktualizuj(a);
        
        NewCookie jwtCookie = new NewCookie(JSONWebTokenBuilder.JWT_COOKIE, "LOGOUT", "/", konfiguracja.getDomena(),
                NewCookie.DEFAULT_VERSION, null, 0, new Date(), false, true);
        NewCookie metaCookie = new NewCookie(JSONWebTokenBuilder.META_COOKIE, "LOGOUT", "/", konfiguracja.getDomena(),
                NewCookie.DEFAULT_VERSION, null, 0, new Date(), false, false);

        if(l != null){
            logowaniaFacade.create(l);
        }
        
        return Response.ok(new Odpowiedz().setKomunikat("Wylogowano")).cookie(jwtCookie).cookie(metaCookie).build();
    }


    
    private Uprawnienia getUprawnienia(Konta konto) throws Exception {
        if (konto != null) {
            
            Uprawnienia uprawnieniaKonta = new Uprawnienia();
            List<KontaGrupy> grupyKonta = konto.getKontaGrupy();
            List<Uprawnienia> uprawnieniaGrupKonta = new ArrayList<>();
            grupyKonta.forEach(gk -> {
                Uprawnienia uprawnienia = uprawnieniaFacade.findByIdGrupy(gk.getGrupa().getId());
                uprawnieniaGrupKonta.add(uprawnienia);
            });
            
            for (String nazwaUprawnienia : Uprawnienia.UPRAWNIENIA) {
                int result = 0;
                for (Uprawnienia uprawnienia : uprawnieniaGrupKonta) {
                    Field field = Uprawnienia.class.getDeclaredField(nazwaUprawnienia);
                    field.setAccessible(true);
                    result |= field.getInt(uprawnienia);
                }
                Field field = Uprawnienia.class.getDeclaredField(nazwaUprawnienia);
                field.setAccessible(true);
                field.setInt(uprawnieniaKonta, result);
            }
            
            return uprawnieniaKonta;
        }
        
        return new Uprawnienia();
    }
    
    private JSONObject getPermissionsJSON(Uprawnienia uprawnienia) throws Exception {
        JSONBuilder jsonb = new JSONBuilder();
        for (String uprawnienie : Uprawnienia.UPRAWNIENIA) {
            Field field = Uprawnienia.class.getDeclaredField(uprawnienie);
            field.setAccessible(true);
            Permissions permissions = new Permissions(field.getInt(uprawnienia));
            jsonb.put(uprawnienie, new JSONBuilder()
                    .put("read", permissions.isRead())
                    .put("add", permissions.isAdd())
                    .put("delete", permissions.isDelete())
                    .put("update", permissions.isUpdate())
                    .build());
        }
        return jsonb.build();
    }
    
    
}
