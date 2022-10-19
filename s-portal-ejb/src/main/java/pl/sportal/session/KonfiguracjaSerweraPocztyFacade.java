/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.jpa.KonfiguracjaSerweraPoczty;


@Stateful(name = "KonfiguracjaSerweraPocztyFacade")
public class KonfiguracjaSerweraPocztyFacade extends AbstractFacade<KonfiguracjaSerweraPoczty> implements KonfiguracjaSerweraPocztyFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KonfiguracjaSerweraPocztyFacade() {
        super(KonfiguracjaSerweraPoczty.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------   
    @Override
    public KonfiguracjaSerweraPoczty find() {
        return this.findAll().stream().findFirst().orElse(null);
    }

    @Override
    public void create(KonfiguracjaSerweraPoczty object) {
        try {
            object.setHasloSerweraPoczty(
                    KonfiguracjaSerweraPoczty.encodePassword(
                            object.getHasloSerweraPoczty()
                    )
            );
        } catch (Exception ex) {
            Logger.getLogger(KonfiguracjaSerweraPocztyFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
        getEntityManager();
        super.create(object);
    }

    @Override
    public KonfiguracjaSerweraPoczty edit(KonfiguracjaSerweraPoczty object, String password) {
        if (!object.getHasloSerweraPoczty().equals(KonfiguracjaSerweraPoczty.EMPTY_PASSWORD)) {
            try {
                object.setHasloSerweraPoczty(KonfiguracjaSerweraPoczty.encodePassword(object.getHasloSerweraPoczty()));
            } catch (Exception ex) {
                Logger.getLogger(KonfiguracjaSerweraPocztyFacade.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            object.setHasloSerweraPoczty(password);
        }
        getEntityManager();
        super.edit(object);
        return object;
    }

    @Override
    public void remove(KonfiguracjaSerweraPoczty object){
        getEntityManager();
        super.remove(object);
    } 

    @Override
    public boolean isEncoded(KonfiguracjaSerweraPoczty object) {
        boolean encoded = true;
        try {
            KonfiguracjaSerweraPoczty.decodePassword(object.getHasloSerweraPoczty());
        } catch (Exception ex) {
            encoded = false;
        }
        return encoded;
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
