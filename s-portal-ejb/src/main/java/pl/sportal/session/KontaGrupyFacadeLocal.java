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
import pl.sportal.jpa.KontaGrupy;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */

@Local
public interface KontaGrupyFacadeLocal {
 
    void create(KontaGrupy Object);
 
    KontaGrupy edit(KontaGrupy Object);
 
    void remove(KontaGrupy Object);
 
    KontaGrupy find(Object id);
 
    List<KontaGrupy> findAll();
    
    List<KontaGrupy> findRange(int[] range);
 
    int count();
 
    ArrayList<KontaGrupy> getGrupyKonta(int idKonta);
 
    KontaGrupy findBy(int idKonta, int idPacjenta);
 
    void setPrincipal(Principal principal);
 
    void setClientIpAdress(String clientIpAdress);
 
    static KontaGrupyFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KontaGrupyFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }
 
    static KontaGrupyFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KontaGrupyFacadeLocal bean = (KontaGrupyFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KontaGrupyFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
 
    void remove();
}
