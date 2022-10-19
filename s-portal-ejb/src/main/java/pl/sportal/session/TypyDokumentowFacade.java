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
import pl.sportal.jpa.TypyDokumentow;


@Stateful(name = "TypyDokumentowFacade")
public class TypyDokumentowFacade extends AbstractFacade<TypyDokumentow> implements TypyDokumentowFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public TypyDokumentowFacade() {
        super(TypyDokumentow.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------   
    //TODO: Do uzupełnienia ciało metody
    @Override
    public boolean czyMogeUsunacTypDokumentu(Integer id) {
        return em.createNativeQuery("SELECT * FROM public.pacjenci "
                + "WHERE id_typ_dokumentu_tozsamosci = :id")
                .setParameter("id", id)
                .getResultList().isEmpty();
    }

    @Override
    public boolean czyNazwaUnikalna(Integer id, String nazwaDokumentuTozsamosci) {
        Query query = em.createNativeQuery("SELECT * FROM slowniki.typy_dokumentow "
                + "WHERE nazwa_dokumentu_tozsamosci = :nazwaDokumentuTozsamosci"
                + (id != null ? " AND id != :id" : ""))
                .setParameter("nazwaDokumentuTozsamosci", nazwaDokumentuTozsamosci);
        if (id != null) {
            query.setParameter("id", id);
        }
        return query.getResultList().isEmpty();
    }
    
    @Override
    public void create(TypyDokumentow object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public TypyDokumentow edit(TypyDokumentow object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(TypyDokumentow object){
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
