/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


public class PacjenciServiceFacade implements PacjenciServiceFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;

    @Override
    public String getPeselByEmail(String email) {
        if (email != null) {
            return (String) em.createNativeQuery("SELECT p.pesel FROM public.pacjenci p WHERE email = :email")
                    .setParameter("email", email)
                    .getResultList().stream().findFirst().orElse(null);
        }
        return null;
    }
}
