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
import pl.sportal.jpa.Konta;
import javax.ws.rs.core.SecurityContext;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Local
public interface KontaFacadeLocal {

    void create(Konta object);

    Konta edit(Konta object);

    void remove(Konta object);

    Konta find(Object id);

    List<Konta> findAll();

    List<Konta> findRange(int[] range);

    int count();

    Konta createManaged(Konta entity);

    boolean czyLoginUnikalny(Integer id, String login);

    List<Konta> find(String filtr, Integer limit);

    List<Konta> findByLogin(String login);

    List<Konta> findByEmail(String email);

    Konta findByUUID(String uuid);

    Konta checkLogin(String login, String password, String ip) throws LoginException, Exception;
    
    void setPrincipal(Principal principal);
    
    void setClientIpAdress(String clientIpAdress);
    
    static KontaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KontaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static KontaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KontaFacadeLocal bean = (KontaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KontaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
