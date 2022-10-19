/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.sportal.session;

import java.util.Date;
import java.util.List;
import javax.ejb.Local;
import pl.sportal.jpa.Logi;


@Local
public interface LogiFacadeLocal {

    Logi find(Object id);

    List<Logi> findAll();

    List<Logi> findRange(int[] range);
    
    List<Logi> find(String filter, Date dataOd, Date dataDo, Integer limit);

    int count();
}
