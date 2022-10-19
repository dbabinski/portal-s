/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import javax.ejb.Local;

/**
 *
 * @author Damian Babiński <damian.babinski@softmedica.pl>
 */
@Local
public interface AktualnieZalogowaniFacadeLocal {
    
    void getAktualizuj(String aktualnieZalogowani);
    
}
