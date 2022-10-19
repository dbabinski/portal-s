/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


public class KontaServiceFacade implements KontaServiceFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;

    @Override
    public List<String> getPeselePacjentowPodrzednych(String UUIDKonta) {
        if (UUIDKonta != null) {
            return em.createNativeQuery("SELECT p.pesel FROM public.pacjenci p INNER JOIN uzytkownicy.pacjenci_powiazania pp ON p.id = pp.id_pacjenta\n"
                    + "WHERE pp.id_konta = (SELECT k.id FROM uzytkownicy.konta k WHERE k.uuid = '" + UUIDKonta + "')")
                    .getResultList();
        }
        return null;
    }
}
