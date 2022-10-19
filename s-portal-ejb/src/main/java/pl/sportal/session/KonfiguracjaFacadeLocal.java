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
import pl.sportal.jpa.Konfiguracja;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Local
public interface KonfiguracjaFacadeLocal {

    void create(Konfiguracja object);

    Konfiguracja edit(Konfiguracja object);

    void remove(Konfiguracja object);

    Konfiguracja find(Object id);

    List<Konfiguracja> findAll();

    List<Konfiguracja> findRange(int[] range);

    int count();

    Konfiguracja createManaged(Konfiguracja entity);

    public Konfiguracja find();

    void setPrincipal(Principal principal);    
    
    void setClientIpAdress(String clientIpAdress);
    
    static KonfiguracjaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KonfiguracjaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static KonfiguracjaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KonfiguracjaFacadeLocal bean = (KonfiguracjaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KonfiguracjaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
