/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

/**
 *
 * @author Krzysztof Depka Prądzyński <k.d.pradzynski@softmedica.pl>
 */
public class HelloFacade implements HelloFacadeLocal {

    @Override
    public String getHello() {
        return "Hello!";
    }

    @Override
    public String processText(String text) {
        return text != null ? text.toUpperCase() : null;
    }
}
