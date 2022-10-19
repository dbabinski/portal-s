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
import pl.sportal.jpa.BlokadaKonta;


@Stateful(name = "BlokadaKontaFacade")
public class BlokadaKontaFacade extends AbstractFacade<BlokadaKonta> implements BlokadaKontaFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public BlokadaKontaFacade() {
        super(BlokadaKonta.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    public void create(BlokadaKonta object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public BlokadaKonta edit(BlokadaKonta object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(BlokadaKonta object){
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

    @Override
    public BlokadaKonta find() {
        return this.findAll().stream().findFirst().orElse(null);
    }
}
