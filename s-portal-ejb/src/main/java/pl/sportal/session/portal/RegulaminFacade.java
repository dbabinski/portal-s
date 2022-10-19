/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session.portal;

import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.jpa.portal.Regulamin;


@Stateful(name = "RegulaminFacade")
public class RegulaminFacade extends AbstractFacade<Regulamin> implements RegulaminFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public RegulaminFacade() {
        super(Regulamin.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------        
    @Override
    public Regulamin findLast() {
        List<Regulamin> list = em.createNativeQuery("SELECT * FROM portal.regulamin ORDER BY data_dodania DESC LIMIT 1", Regulamin.class)
                .getResultList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }
    
    @Override
    public void create(Regulamin object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public Regulamin edit(Regulamin object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(Regulamin object){
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
