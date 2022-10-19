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
import pl.sportal.jpa.KonfiguracjaSerweraSms;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Stateful(name = "KonfiguracjaSerweraSmsFacade")
public class KonfiguracjaSerweraSmsFacade extends AbstractFacade<KonfiguracjaSerweraSms> implements KonfiguracjaSerweraSmsFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KonfiguracjaSerweraSmsFacade() {
        super(KonfiguracjaSerweraSms.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------
    @Override
    public KonfiguracjaSerweraSms find() {
        return this.findAll().stream().findFirst().orElse(null);
    }

    @Override
    public void create(KonfiguracjaSerweraSms object) {
        if (object.isSmsApi()) {
            try {
                object.setSmsApiPassword(
                        KonfiguracjaSerweraSms.encodePassword(
                                object.getSmsApiPassword()
                        )
                );
            } catch (Exception e) {
                Logger.getLogger(KonfiguracjaSerweraSmsFacade.class.getName()).log(Level.SEVERE, null, e);
            }
            getEntityManager();
            super.create(object);
        } else {
            try {
                object.setSerwerSmsPassword(
                        KonfiguracjaSerweraSms.encodePassword(
                                object.getSerwerSmsPassword()
                        )
                );
            } catch (Exception e) {
                Logger.getLogger(KonfiguracjaSerweraSmsFacade.class.getName()).log(Level.SEVERE, null, e);
            }
            getEntityManager();
            super.create(object);
        }
    }

    @Override
    public KonfiguracjaSerweraSms edit(KonfiguracjaSerweraSms object, String password) {
        if (object != null) {
            if (object.isSmsApi()) {
                if (!object.getSmsApiPassword().equals(KonfiguracjaSerweraSms.EMPTY_PASSWORD)) {
                    try {
                        object.setSmsApiPassword(KonfiguracjaSerweraSms.encodePassword(password));
                    } catch (Exception e) {
                        Logger.getLogger(KonfiguracjaSerweraSmsFacade.class.getName()).log(Level.SEVERE, null, e);
                    }
                } else {
                    object.setSmsApiPassword(password);
                }
                getEntityManager();
                super.edit(object);
            } else if (object.isSerwerSms()) {
                if (!object.getSerwerSmsPassword().equals(KonfiguracjaSerweraSms.EMPTY_PASSWORD)) {
                    try {
                        object.setSerwerSmsPassword(KonfiguracjaSerweraSms.encodePassword(password));
                    } catch (Exception e) {
                        Logger.getLogger(KonfiguracjaSerweraSmsFacade.class.getName()).log(Level.SEVERE, null, e);
                    }
                } else {
                    object.setSerwerSmsPassword(password);
                }
                getEntityManager();
                super.edit(object);
            } else {
                object.setSmsApiPassword(null);
                object.setSerwerSmsPassword(null);
                getEntityManager();
                super.edit(object);
            }
        }
        return object;
    }

    @Override
    public void remove(KonfiguracjaSerweraSms object) {
        getEntityManager();
        super.remove(object);
    }

    @Override
    public boolean isEncoded(KonfiguracjaSerweraSms object) {
        boolean encoded = true;
        try {
            if (object.isSmsApi()) {
                KonfiguracjaSerweraSms.decodePassword(object.getSmsApiPassword());
            } else {
                KonfiguracjaSerweraSms.decodePassword(object.getSerwerSmsPassword());
            }
        } catch (Exception e) {
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
