/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.Local;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.Klienci;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateful(name = "KlienciFacade")
@Local(KlienciFacadeLocal.class)
public class KlienciFacade extends AbstractFacade<Klienci> implements KlienciFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KlienciFacade() {
        super(Klienci.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------       
    @Override
    public List<Klienci> find(String filtr, Integer limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM public.klienci");
        if (Utilities.stringToNull(filtr) != null) {
            sql.append("\nWHERE")
                    .append("\nlower(imie || ' ' || nazwisko)    LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(nazwisko || ' ' || imie) LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(nip)                   LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(nazwa_klienta)                   LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(nr_licencji)                   LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(email)                   LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'");
        }
        Query query = em.createNativeQuery(sql.toString(), Klienci.class);
        if (filtr != null) {
            query.setParameter("filtr", filtr.trim());
        }
        return query.getResultList();
    }

    @Override
    public List<Klienci> findByKonto(Integer idKonta) {
        if (idKonta != null) {
            return em.createNativeQuery("SELECT * FROM public.klienci WHERE id_konta = :idKonta", Klienci.class)
                    .setParameter("idKonta", idKonta)
                    .getResultList();
        }
        return null;
    }

    @Override
    public Klienci findByEmail(String email) {
        if (email != null) {
            return (Klienci) em.createNativeQuery("SELECT * FROM public.klienci WHERE email = :email", Klienci.class)
                    .setParameter("email", email)
                    .getResultList().stream().findFirst().orElse(null);
        }
        return null;
    }

    @Override
    public LinkedList<Klienci> getKlienci(Integer idKonta) {
        LinkedList<Klienci> lista = new LinkedList<>();
        if (idKonta != null) {
            lista.addAll(em.createNativeQuery("SELECT p.* FROM public.klienci p "
                            + "WHERE p.id_konta = :idKonta "
                            + "ORDER BY p.nazwisko, p.imie", Klienci.class)
                            .setParameter("idKonta", idKonta)
                            .getResultList());
        }
        return lista;
    }

    public void create(Klienci object) {
        getEntityManager();
        super.create(object);
    }

    @Override
    public Klienci edit(Klienci object) {
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(Klienci object) {
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
