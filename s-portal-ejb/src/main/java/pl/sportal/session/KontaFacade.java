/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateful;
import javax.mail.Session;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import pl.sportal.common.session.AbstractFacade;
import pl.sportal.common.utilities.BCrypt;
import pl.sportal.common.utilities.ListBuilder;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.BlokadaKonta;
import pl.sportal.jpa.Konfiguracja;
import pl.sportal.jpa.Konta;
import pl.sportal.jpa.Logowania;
import pl.sportal.jpa.Mail;


@Stateful(name = "KontaFacade")
public class KontaFacade extends AbstractFacade<Konta> implements KontaFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    private EntityManager em;
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");
    @EJB
    private BlokadaKontaFacadeLocal blokadaKontaFacade;
    @EJB
    private KonfiguracjaFacadeLocal konfiguracjaFacade;
    @EJB
    private KonfiguracjaSerweraPocztyFacadeLocal konfiguracjaSerweraPocztyFacade;
    @EJB
    private LogowaniaFacadeLocal logowaniaFacade;
    @Resource(name = "java:jboss/mail/mail-smportal_outgoing")
    private Session mailSession;

    //--------------------------------------------------------------------------
    // Konstruktor
    //--------------------------------------------------------------------------
    public KontaFacade() {
        super(Konta.class);
    }

    //--------------------------------------------------------------------------
    // Metody publiczne
    //--------------------------------------------------------------------------   
    @Override
    public void create(Konta object) {
        getEntityManager();
        super.create(object);
    }

    @Override
    public Konta edit(Konta object) {
        getEntityManager();
        return super.edit(object);
    }

    @Override
    public void remove(Konta object) {
        getEntityManager();
        super.remove(object);
    }

    @Override
    public List<Konta> find(String filtr, Integer limit) {
        StringBuilder sql = new StringBuilder("SELECT k.* FROM uzytkownicy.konta k LEFT JOIN uzytkownicy.grupy g ON k.id_grupy = g.id");
        if (Utilities.stringToNull(filtr) != null) {
            sql.append("\nWHERE")
                    .append("\nlower(k.login)    LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(k.email) LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'")
                    .append("\nOR lower(g.opis)  LIKE '%' || lower(trim(both ' ' from regexp_replace(:filtr, '\\s{2,}', ' ', 'g'))) || '%'");
        }
        Query query = em.createNativeQuery(sql.toString(), Konta.class);
        if (filtr != null) {
            query.setParameter("filtr", filtr.trim());
        }
        return query.getResultList();
    }

    @Override
    public Konta findByUUID(String uuid) {
        if (Utilities.stringToNull(uuid) == null) {
            return null;
        }
        return (Konta) em.createNativeQuery("SELECT * FROM uzytkownicy.konta WHERE uuid = :uuid", Konta.class)
                .setParameter("uuid", uuid).getResultList()
                .stream().findFirst().orElse(null);
    }

    @Override
    public List<Konta> findByLogin(String login) {
        if (Utilities.stringToNull(login) == null) {
            return new LinkedList<>();
        }
        return em.createNativeQuery("SELECT * FROM uzytkownicy.konta WHERE lower(login) = :login", Konta.class)
                .setParameter("login", login.toLowerCase()).getResultList();
    }

    @Override
    public List<Konta> findByEmail(String email) {
        if (Utilities.stringToNull(email) == null) {
            return new LinkedList<>();
        }
        return em.createNativeQuery("SELECT * FROM uzytkownicy.konta WHERE lower(email) = :email", Konta.class)
                .setParameter("email", email.toLowerCase()).getResultList();
    }

    @Override
    public boolean czyLoginUnikalny(Integer id, String login) {
        if (login != null) {
            Query query = em.createNativeQuery("SELECT * FROM uzytkownicy.konta "
                    + "WHERE login = :login"
                    + (id != null ? " AND id != :id" : ""))
                    .setParameter("login", login);
            if (id != null) {
                query.setParameter("id", id);
            }
            return query.getResultList().isEmpty();
        }
        return false;
    }

    @Override
    public Konta checkLogin(String login, String password, String ip) throws LoginException, Exception {
        if (login == null || password == null) {
            dodajDoLogow(ip, null, "Nieudane logowanie - Nie wprowadzono loginu lub hasła!", login);
            throw new LoginException("Nie wprowadzono loginu lub hasła!");
        }
        if (login.trim().isEmpty() || password.trim().isEmpty()) {
            dodajDoLogow(ip, null, "Nieudane logowanie - Nie wprowadzono loginu lub hasła!", login);
            throw new LoginException("Nie wprowadzono loginu lub hasła!");
        }
        Konta konto = this.findByLogin(login).stream().sorted(Konta.COMPARATOR_BY_ID.reversed()).findFirst().orElse(null);
        if (konto == null) {
            konto = this.findByEmail(login).stream().sorted(Konta.COMPARATOR_BY_ID.reversed()).findFirst().orElse(null);
        }
        if (konto == null) {
            dodajDoLogow(ip, konto, "Niepoprawny login lub hasło!", login);
            throw new LoginException("Niepoprawny login lub hasło!");
        }
        if (!konto.isKontoAktywne()) {
            dodajDoLogow(ip, konto, "Nieudane logowanie - Konto jest nieaktywne!", login);
            throw new LoginException("Konto jest nieaktywne!");
        }
        if (konto.getIdGrupy() != null && !konto.getIdGrupy().isAktywna()) {
            dodajDoLogow(ip, konto, "Nieudane logowanie - Konto jest nieaktywne!", login);
            throw new LoginException("Konto jest nieaktywne!");
        }
        if (konto.isBlokadaKonta()) {
            if (konto.getBlokadaKontaDo() != null) {
                if (new Date().compareTo(konto.getBlokadaKontaDo()) >= 0) {
                    this.edit(konto.setBlokadaKonta(false).setBlokadaKontaDo(null));
                } else {
                    //wydłużenie czasu blokady
                    //this.czasowoZablokujKonto(konto, true);
                    dodajDoLogow(ip, konto, "Konto zostało zablokowane do " + Utilities.dateToString(konto.getBlokadaKontaDo(), Utilities.DATE_TIME_FORMAT), login);
                    throw new LoginException("Konto zostało zablokowane do " + Utilities.dateToString(
                            konto.getBlokadaKontaDo(), Utilities.DATE_TIME_FORMAT));
                }
            } else {
                dodajDoLogow(ip, konto, "Konto zostało zablokowane!", login);
                throw new LoginException("Konto zostało zablokowane!");
            }
        }
        if (!BCrypt.checkpw(password, konto.getHaslo())) {
            this.czasowoZablokujKonto(konto, false);
            dodajDoLogow(ip, konto, "Niepoprawny login lub hasło!", login);
            throw new LoginException("Niepoprawny login lub hasło!");
        } else {
            if (konto.getLiczbaProbLogowania() > 0) {
                this.edit(konto.resetLiczbaProbLogowania());
            }
        }
        return konto;
    }

    //--------------------------------------------------------------------------
    // Metody prywatne
    //--------------------------------------------------------------------------
    private Konta czasowoZablokujKonto(Konta konto, boolean bezwarunkowo) {
        if (konto != null) {
            BlokadaKonta ustawieniaBlokady = blokadaKontaFacade.find();
            if (ustawieniaBlokady != null) {
                konto.dodajLiczbaProbLogowania();
                if (bezwarunkowo || konto.getLiczbaProbLogowania() >= ustawieniaBlokady.getLiczbaBledow()) {
                    Date blokadaDo = Utilities.calculateDate(new Date(), Calendar.MINUTE, ustawieniaBlokady.getCzasBlokady());
                    konto.setBlokadaKonta(true).setBlokadaKontaDo(blokadaDo);
                    this.edit(konto);
                    Konfiguracja konfiguracja = konfiguracjaFacade.find();
                    if (konfiguracja != null) {
                        String szablon = konfiguracja.getSzablonEmailPowiadomienieOZablokowaniuKonta();
                        if (szablon != null) {
                            try {
                                String tresc = Mail.wypelnijSzablon(konto, null, konfiguracja, szablon, null, null);
                                new Mail()
                                        .setSession(mailSession)
                                        .setKonfiguracjaPoczty(konfiguracjaSerweraPocztyFacade.find())
                                        .setKonfiguracja(konfiguracja)
                                        .setOdbiorcy(new ListBuilder<String>().append(konto.getEmail()).build())
                                        .setTematWiadomosci("Powiadomienie o zablokowaniu konta")
                                        .setTrescWiadomosci(tresc)
                                        .wyslij();
                            } catch (Exception ex) {
                                LOGGER.log(Level.SEVERE, ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return konto;
    }
    
    private void dodajDoLogow(String ip, Konta konto, String opis, String login){
        
        String uuid = (konto != null ? konto.getUUID() : null);
        String varOpis = opis + (uuid == null ? " login:" + login : "");
        
        Logowania l = new Logowania()
            .setIp(ip)
            .setUuidKonta(uuid)
            .setOpis(varOpis);
        logowaniaFacade.create(l);
    }

    //--------------------------------------------------------------------------
    // AbstractFacade
    //--------------------------------------------------------------------------
    @Override
    protected EntityManager getEntityManager() {
        em.createNativeQuery("SET application_name = '" + (principal != null ? principal.getName() : "") + (clientIpAdress != null ? "#" + clientIpAdress : "") + "@e_uslugi'").executeUpdate();
        return em;
    }
}
