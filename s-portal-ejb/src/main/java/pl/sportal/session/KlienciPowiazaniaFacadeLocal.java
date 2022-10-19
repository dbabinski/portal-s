/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.KlienciPowiazania;


@Local
public interface KlienciPowiazaniaFacadeLocal {

    void create(KlienciPowiazania object);

    KlienciPowiazania edit(KlienciPowiazania object);

    void remove(KlienciPowiazania object);

    KlienciPowiazania find(Object id);

    List<KlienciPowiazania> findAll();

    List<KlienciPowiazania> findRange(int[] range);

    int count();

    ArrayList<KlienciPowiazania> getPowiazaniaKonta(Integer idKonta);
    
    KlienciPowiazania findBy(Integer idKonta, Integer idKlienta);
    
    void setPrincipal(Principal principal);    
    
    void setClientIpAdress(String clientIpAdress);
    
    static KlienciPowiazaniaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KlienciPowiazaniaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static KlienciPowiazaniaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KlienciPowiazaniaFacadeLocal bean = (KlienciPowiazaniaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KlienciPowiazaniaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
