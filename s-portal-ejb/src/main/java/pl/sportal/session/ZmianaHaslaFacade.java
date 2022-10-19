/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.ZmianaHasla;


@Stateful(name = "ZmianaHaslaFacade")
public class ZmianaHaslaFacade extends AbstractFacade<ZmianaHasla> implements ZmianaHaslaFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public ZmianaHaslaFacade() {
        super(ZmianaHasla.class);
    }
    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------   

    @Override
    public ZmianaHasla findByUUID(String uuid) {
        if (Utilities.stringToNull(uuid) == null) {
            return null;
        }
        return (ZmianaHasla) em.createNativeQuery("SELECT * FROM uzytkownicy.zmiana_hasla WHERE uuid = :uuid", ZmianaHasla.class)
                .setParameter("uuid", uuid).getResultList()
                .stream().findFirst().orElse(null);
    }
    
    @Override
    public ZmianaHasla findByToken(String token) {
        if (Utilities.stringToNull(token) == null) {
            return null;
        }
        return (ZmianaHasla) em.createNativeQuery("SELECT * FROM uzytkownicy.zmiana_hasla WHERE token = :token", ZmianaHasla.class)
                .setParameter("token", token).getResultList()
                .stream().findFirst().orElse(null);
    }
    
    @Override
    public void create(ZmianaHasla object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public ZmianaHasla edit(ZmianaHasla object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(ZmianaHasla object){
        getEntityManager();
        super.remove(object);
    }  
    //--------------------------------------------------------------------------
    // Metody prywatne
    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // AbstractFacade
    //--------------------------------------------------------------------------
    @Override
    protected EntityManager getEntityManager() {
        em.createNativeQuery("SET application_name = '" + (principal != null ? principal.getName() : "") + (clientIpAdress != null ? "#" + clientIpAdress : "") + "@e_uslugi'").executeUpdate();
        return em;
    }
}
