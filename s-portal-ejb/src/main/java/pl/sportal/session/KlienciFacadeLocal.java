/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.Local;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.SecurityContext;
import pl.sportal.jpa.Klienci;


@Local
public interface KlienciFacadeLocal{

    void create(Klienci object);

    Klienci edit(Klienci object);

    void remove(Klienci object);

    Klienci find(Object id);

    List<Klienci> findAll();

    List<Klienci> findRange(int[] range);

    int count();

    Klienci createManaged(Klienci entity);

    /**     
     *
     * @param id    jeśli id != null to klient o tym id nie jest wybierany
     * @return lista klient o wskazanym numerze pesel
     */
    LinkedList<Klienci> getKlienci(Integer idKonta);

    List<Klienci> find(String filter, Integer limit);

    List<Klienci> findByKonto(Integer idKonta);
    
    Klienci findByEmail(String email);

    void setPrincipal(Principal principal);    
    
    void setClientIpAdress(String clientIpAdress);
    
    static KlienciFacadeLocal create(SecurityContext securityContext, String clientIpAdress) throws NamingException {
        return KlienciFacadeLocal.create(securityContext != null
                ? securityContext.getUserPrincipal() : (Principal) null, clientIpAdress);
    }

    static KlienciFacadeLocal create(Principal principal, String clientIpAdress) throws NamingException {
        InitialContext initialContext = new InitialContext();
        KlienciFacadeLocal bean = (KlienciFacadeLocal) initialContext
                .lookup("java:app/s-portal-ejb/KlienciFacade");
        bean.setPrincipal(principal);
        bean.setClientIpAdress(clientIpAdress);
        return bean;
    }
}
