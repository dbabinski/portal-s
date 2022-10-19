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
import pl.sportal.jpa.TypyEDokumentow;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Local
public interface TypyEDokumentowFacadeLocal {

    void create(TypyEDokumentow object);

    TypyEDokumentow edit(TypyEDokumentow object);

    void remove(TypyEDokumentow object);

    TypyEDokumentow find(Object id);

    List<TypyEDokumentow> findAll();

    List<TypyEDokumentow> findRange(int[] range);

    int count();

    TypyEDokumentow createManaged(TypyEDokumentow entity);
    
    boolean czyMogeUsunacTypEDokumentu(Integer id);
    
    boolean czyOpisUnikalny(Integer id, String opis);
    
    void setPrincipal(Principal principal); 
    
    void setClientIpAdress(String clientIpAdress);
    
    static TypyEDokumentowFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return TypyEDokumentowFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static TypyEDokumentowFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        TypyEDokumentowFacadeLocal bean = (TypyEDokumentowFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/TypyEDokumentowFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    } 
}
