/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.sportal.session;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.jpa.KontaGrupy;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateful(name = "KontaGrupyFacade")
public class KontaGrupyFacade extends AbstractFacade<KontaGrupy> implements KontaGrupyFacadeLocal {
 
    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");
 
    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KontaGrupyFacade() {
        super(KontaGrupy.class);
    }
 
    @Remove
    @Override
    public void remove() {
        //NOP
    }
 
    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    @Override
    public ArrayList<KontaGrupy> getGrupyKonta(int idKonta) {
        String SQL = "SELECT * FROM uzytkownicy.konta_grupy WHERE id_konta = :id_konta";
        Query query = em.createNativeQuery(SQL, KontaGrupy.class);
        query.setParameter("id_konta", idKonta);
        return (ArrayList<KontaGrupy>) query.getResultList();
    }
 
    @Override
    public KontaGrupy findBy(int idKonta, int idGrupy) {
        String SQL = "SELECT p.* FROM uzytkownicy.konta_grupy p WHERE id_konta = :id_konta AND id_grupy = :id_grupy";
        Query query = em.createNativeQuery(SQL, KontaGrupy.class)
                .setParameter("id_konta", idKonta)
                .setParameter("id_grupy", idGrupy);
        return (KontaGrupy) query
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }
 
    @Override
    public void create(KontaGrupy object) {
        super.create(object);
    }
 
    @Override
    public KontaGrupy edit(KontaGrupy object) {
        return super.edit(object);
    }
 
    @Override
    public void remove(KontaGrupy object) {
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