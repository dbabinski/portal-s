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
import pl.sportal.jpa.SzablonyPowiadomien;
/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateful(name = "SzablonyPowiadomienFacade")
public class SzablonyPowiadomienFacade extends AbstractFacade<SzablonyPowiadomien> implements SzablonyPowiadomienFacadeLocal{

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public SzablonyPowiadomienFacade() {
        super(SzablonyPowiadomien.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    @Override
    public boolean czyTrescUnikalna(Integer id, String tresc) {
        Query query = em.createNativeQuery("SELECT * FROM powiadomienia.szablony "
                + "WHERE szablon_email_naglowek LIKE :tresc"
                + "OR szablon_email_tresc LIKE :tresc"
                + "OR szablon_sms_tresc LIKE :tresc"
                + (id != null ? " AND id != :id" : ""))
                .setParameter("tresc", tresc);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getResultList().isEmpty();
    }

    @Override
    public void remove(SzablonyPowiadomien entity) {
        getEntityManager();
        super.remove(entity);
    }

    @Override
    public SzablonyPowiadomien findById(Integer id) {
        return (SzablonyPowiadomien) em.createNamedQuery("Szablony.findById")
                .setParameter("id", id)
                .getSingleResult();
    }
    
    public void create(SzablonyPowiadomien object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public SzablonyPowiadomien edit(SzablonyPowiadomien object){
        getEntityManager();
        return super.edit(object);
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
