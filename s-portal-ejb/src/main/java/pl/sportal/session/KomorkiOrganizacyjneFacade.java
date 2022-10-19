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
import pl.sportal.jpa.KomorkiOrganizacyjne;

@Stateful(name = "KomorkiOrganizacyjneFacade")
public class KomorkiOrganizacyjneFacade extends AbstractFacade<KomorkiOrganizacyjne> implements KomorkiOrganizacyjneFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KomorkiOrganizacyjneFacade() {
        super(KomorkiOrganizacyjne.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    public void create(KomorkiOrganizacyjne object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public KomorkiOrganizacyjne edit(KomorkiOrganizacyjne object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(KomorkiOrganizacyjne object){
        getEntityManager();
        super.remove(object);
    }       
    
    @Override
    public boolean czyMogeUsunacKomorkeOrganizacyjna(int id) {
        return em.createNativeQuery("SELECT * FROM slowniki.komorki_organizacyjne "
                + "WHERE id_komorki_organizacyjnej = :id")
                .setParameter("id", id)
                .getResultList().isEmpty();
    }

    @Override
    public boolean czyNazwaUnikalna(Integer id, String nazwa) {
        Query query = em.createNativeQuery("SELECT * FROM slowniki.komorki_organizacyjne "
                + "WHERE nazwa = :nazwa"
                + (id != null ? " AND id != :id" : ""))
                .setParameter("nazwa", nazwa);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getResultList().isEmpty();
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
