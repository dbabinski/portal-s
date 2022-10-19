/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Asynchronous;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.PersistenceContext;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import pl.sportal.common.utilities.Utilities;
import pl.sportal.jpa.AktualnieZalogowani;


@Singleton
@Startup
public class AktualnieZalogowaniPortalFacade implements AktualnieZalogowaniPortalFacadeLocal {

    @PersistenceContext(unitName = "s-portal-EJB-PU")
    public static final Logger LOGGER = Logger.getLogger("pl.sportal.ea");

    private ConcurrentHashMap<String, AktualnieZalogowani> zalogowani = new ConcurrentHashMap<>();

    @Schedule(second = "*/5", minute = "*", hour = "*", persistent = false)
    @Asynchronous
    public void wyczyscWygasleSesje() {
        List<String> doUsuniecia = new ArrayList<>();
        Date czasWygasniecia = new Date();

        zalogowani.keySet().forEach(key -> {
            if (zalogowani.get(key).getDataWygasniecia() == null) {
                doUsuniecia.add(key);
            } else {
                if (zalogowani.get(key).getDataWygasniecia().before(czasWygasniecia)) {
                    doUsuniecia.add(key);
                }
            }
        });

        doUsuniecia.forEach(key -> {
            zalogowani.remove(key);
        });
    }

    @Override
    @Lock(LockType.WRITE)
    public void aktualizuj(AktualnieZalogowani zalogowany) {
        if (!zalogowani.containsKey(zalogowany.getUuid())) {
            zalogowani.put(zalogowany.getUuid(), zalogowany);
        } else {
            if (zalogowany.getDataWygasniecia() != null) {
                zalogowani.get(zalogowany.getUuid()).setDataWygasniecia(zalogowany.getDataWygasniecia());
                zalogowani.get(zalogowany.getUuid()).setAplikacja(zalogowany.getAplikacja());
            } else {
                zalogowani.get(zalogowany.getUuid()).setDataWygasniecia(null);
            }
        }
    }

    @Override
    public void aktualizuj(String zalogowany) {
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            json = (JSONObject) parser.parse(zalogowany);
        } catch (ParseException ex) {
            Logger.getLogger(AktualnieZalogowaniPortalFacade.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (json != null) {
            AktualnieZalogowani aktualinieZalogowani = new AktualnieZalogowani().setJSON(json);
            aktualizuj(aktualinieZalogowani);
        }
    }

    @Override
    @Lock(LockType.READ)
    public List<AktualnieZalogowani> find(String filtr) {
        if (Utilities.stringToNull(filtr) != null) {
            String[] filtrujPo = filtr.split(" ");

            return zalogowani
                    .values()
                    .stream()
                    .filter(z
                            -> Arrays
                            .stream(filtrujPo)
                            .anyMatch(
                                    (zalogowani.get(z.getUuid()).getUuid()
                                    + zalogowani.get(z.getUuid()).getIp()
                                    + zalogowani.get(z.getUuid()).getAplikacja()
                                    + zalogowani.get(z.getUuid()).getEmail()
                                    + Utilities.dateToString(zalogowani.get(z.getUuid()).getDataWygasniecia(), Utilities.DATE_TIME_SEC_FORMAT))::contains)
                    )
                    .collect(Collectors.toList());
        }
        return zalogowani.values().stream().collect(Collectors.toList());
    }
}
