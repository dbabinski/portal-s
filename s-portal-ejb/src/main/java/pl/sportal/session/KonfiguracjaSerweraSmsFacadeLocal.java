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
import pl.sportal.jpa.KonfiguracjaSerweraSms;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Local
public interface KonfiguracjaSerweraSmsFacadeLocal {

    void create(KonfiguracjaSerweraSms object);

    KonfiguracjaSerweraSms edit(KonfiguracjaSerweraSms object, String password);

    void remove(KonfiguracjaSerweraSms object);

    KonfiguracjaSerweraSms find(Object id);

    List<KonfiguracjaSerweraSms> findAll();

    int count();

    KonfiguracjaSerweraSms createManaged(KonfiguracjaSerweraSms entity);

    KonfiguracjaSerweraSms find();

    boolean isEncoded(KonfiguracjaSerweraSms object);

    void setPrincipal(Principal principal);

    void setClientIpAdress(String clientIpAdress);

    static KonfiguracjaSerweraSmsFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KonfiguracjaSerweraSmsFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static KonfiguracjaSerweraSmsFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KonfiguracjaSerweraSmsFacadeLocal bean = (KonfiguracjaSerweraSmsFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KonfiguracjaSerweraSmsFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
