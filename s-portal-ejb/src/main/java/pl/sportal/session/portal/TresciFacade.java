/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session.portal;

import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.sportal.common.interfaces.InterfaceDatabaseObject;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.jpa.portal.Tresci;


@Stateful(name = "TresciFacade")
public class TresciFacade extends AbstractFacade<Tresci> implements TresciFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public TresciFacade() {
        super(Tresci.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------   
    @Override
    public Tresci find() {
        return findAll().stream()
                .sorted(InterfaceDatabaseObject.COMPARATOR_BY_ID.reversed())
                .findFirst().orElse(null);
    }
    
    @Override
    public void create(Tresci object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public Tresci edit(Tresci object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(Tresci object){
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
