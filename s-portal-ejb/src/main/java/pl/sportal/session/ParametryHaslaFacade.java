/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.LinkedList;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.common.utilities.ListBuilder;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.ParametryHasla;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
@Stateful(name = "ParametryHaslaFacade")
public class ParametryHaslaFacade extends AbstractFacade<ParametryHasla> implements ParametryHaslaFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public ParametryHaslaFacade() {
        super(ParametryHasla.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------   
    @Override
    public LinkedList<String> sprawdz(String haslo) {
        ParametryHasla parametry = find();
        return sprawdz(parametry, haslo);
    }

    public static LinkedList<String> sprawdz(ParametryHasla parametry, String haslo) {
        ListBuilder<String> uwagi = new ListBuilder<>();
        if (parametry == null) {
            parametry = new ParametryHasla();
        }
        if (Utilities.stringToNull(haslo) == null) {
            return uwagi.append("nie wprowadzono hasła").build();
        }
        haslo = haslo.trim();
        if (haslo.length() < parametry.getMinimalnaDlugosc()) {
            uwagi.append("hasło musi składać się z min. " + parametry.getMinimalnaDlugosc() + " znaków"
            );
        }
        if (parametry.getLiczbaCyfr() > 0) {
            if (ParametryHasla.liczbaCyfr(haslo) < parametry.getLiczbaCyfr()) {
                String odmiana;
                switch (parametry.getLiczbaCyfr()) {
                    case 1:
                        odmiana = "cyfrę";
                        break;
                    case 2:
                    case 3:
                    case 4:
                        odmiana = "cyfry";
                        break;
                    default:
                        odmiana = "cyfr";

                }
                uwagi.append("hasło musi zawierać min. " + parametry.getLiczbaCyfr() + " " + odmiana);
            }
        }
        if (parametry.getLiczbaZnakowSpecjalnych() > 0) {
            if (ParametryHasla.liczbaZnakowSpecjalnych(haslo) < parametry.getLiczbaZnakowSpecjalnych()) {
                String odmiana;
                switch (parametry.getLiczbaZnakowSpecjalnych()) {
                    case 1:
                        odmiana = "znak specjalny";
                        break;
                    case 2:
                    case 3:
                    case 4:
                        odmiana = "znaki specjalne";
                        break;
                    default:
                        odmiana = "znaków specjalnych";

                }
                uwagi.append("hasło musi zawierać min. " + parametry.getLiczbaZnakowSpecjalnych() + " " + odmiana + ", " + ParametryHasla.ZNAKI_SPECJALNE);
            }
        }
        if (parametry.getWielkoscLiter()) {
            if (!ParametryHasla.isMaleIWielkieLitery(haslo)) {
                uwagi.append("hasło musi zawierać małe i wielkie litery");
            }
        }
        return uwagi.build();
    }
    
    public void create(ParametryHasla object){
        getEntityManager();
        super.create(object);    
    }

    @Override
    public ParametryHasla edit(ParametryHasla object){
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(ParametryHasla object){
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
    public ParametryHasla find() {
        return this.findAll().stream().findFirst().orElse(new ParametryHasla());
    }
}
