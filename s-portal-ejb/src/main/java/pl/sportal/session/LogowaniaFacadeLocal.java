/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.Logowania;


@Local
public interface LogowaniaFacadeLocal {

    void create(Logowania object);
    
    Logowania find(Object id);

    List<Logowania> findAll();

    List<Logowania> findRange(int[] range);
    
    List<Logowania> find(String filter, Date dataOd, Date dataDo, Integer limit);

    int count();
    
    Logowania createManaged(Logowania entity);
    
    void setPrincipal(Principal principal);
    
    void setClientIpAdress(String clientIpAdress);
    
    static LogowaniaFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return LogowaniaFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }
    
    static LogowaniaFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        LogowaniaFacadeLocal bean = (LogowaniaFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/LogowaniaFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
