/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.security.Principal;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.TypyDokumentow;


@Local
public interface TypyDokumentowFacadeLocal {

    void create(TypyDokumentow object);

    TypyDokumentow edit(TypyDokumentow object);

    void remove(TypyDokumentow object);

    TypyDokumentow find(Object id);   

    List<TypyDokumentow> findAll();

    List<TypyDokumentow> findRange(int[] range);

    int count();

    TypyDokumentow createManaged(TypyDokumentow entity);

    boolean czyMogeUsunacTypDokumentu(Integer id);

    boolean czyNazwaUnikalna(Integer id, String nazwa);

    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static TypyDokumentowFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return TypyDokumentowFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static TypyDokumentowFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        TypyDokumentowFacadeLocal bean = (TypyDokumentowFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/TypyDokumentowFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
