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
import javax.persistence.Query;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.jpa.TypyKomunikatow;


@Stateful(name = "TypyKomunikatowFacade")
public class TypyKomunikatowFacade extends AbstractFacade<TypyKomunikatow> implements TypyKomunikatowFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public TypyKomunikatowFacade() {
        super(TypyKomunikatow.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------      
    @Override
    public boolean czyOpisUnikalny(Integer id, String opis) {
        Query query = em.createNativeQuery("SELECT * FROM slowniki.typy_komunikatow "
                + "WHERE opis = :opis"
                + (id != null ? " AND id != :id" : ""))
                .setParameter("opis", opis);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getResultList().isEmpty();
    }
    
    @Override
    public void create(TypyKomunikatow object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public TypyKomunikatow edit(TypyKomunikatow object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(TypyKomunikatow object){
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
