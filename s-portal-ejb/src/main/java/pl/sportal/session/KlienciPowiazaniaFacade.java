/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.jpa.KlienciPowiazania;

@Stateful(name = "KlienciPowiazaniaFacade")
public class KlienciPowiazaniaFacade extends AbstractFacade<KlienciPowiazania> implements KlienciPowiazaniaFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KlienciPowiazaniaFacade() {
        super(KlienciPowiazania.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    @Override
    public ArrayList<KlienciPowiazania> getPowiazaniaKonta(Integer idKonta) {
        String sql = "SELECT * FROM uzytkownicy.klienci_powiazania WHERE id_konta = :id_konta";
        Query query = em.createNativeQuery(sql, KlienciPowiazania.class);
        query.setParameter("id_konta", idKonta);
        return (ArrayList<KlienciPowiazania>) query.getResultList();
    }

    @Override
    public KlienciPowiazania findBy(Integer idKonta, Integer idKlienta) {
        String sql = "SELECT p.* FROM uzytkownicy.klienci_powiazania p WHERE id_konta = :id_konta AND id_klienta = :id_klienta";
        Query query = em.createNativeQuery(sql, KlienciPowiazania.class)
                .setParameter("id_konta", idKonta)
                .setParameter("id_klienta", idKlienta);
        return (KlienciPowiazania) query
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }

    public void create(KlienciPowiazania object) {
        getEntityManager();
        super.create(object);
    }

    @Override
    public KlienciPowiazania edit(KlienciPowiazania object) {
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(KlienciPowiazania object) {
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
