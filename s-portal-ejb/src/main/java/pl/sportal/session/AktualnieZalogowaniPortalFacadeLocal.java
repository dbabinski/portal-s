/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.List;
import pl.sportal.jpa.AktualnieZalogowani;

/**
 *
 * @author Damian Babiński <d.babinski94@gmail.com>
 */
public interface AktualnieZalogowaniPortalFacadeLocal {
    
    void aktualizuj(AktualnieZalogowani zalogowany);
    
    void aktualizuj(String zalogowany);
    
    List<AktualnieZalogowani> find(String filtr);
}
