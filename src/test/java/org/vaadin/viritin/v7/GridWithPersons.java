package org.vaadin.viritin.v7;

import com.vaadin.ui.Component;
import java.util.List;
import org.vaadin.addonhelpers.AbstractTest;
import org.vaadin.viritin.v7.grid.MGrid;
import org.vaadin.viritin.testdomain.Person;
import org.vaadin.viritin.testdomain.Service;

/**
 *
 * @author Matti Tahvonen
 */
public class GridWithPersons extends AbstractTest {

    private static final long serialVersionUID = 503365638639247756L;

    @Override
    public Component getTestComponent() {

        List<Person> listOfPersons = Service.getListOfPersons(100);
        final Person selectedDude = listOfPersons.get(2);

        MGrid<Person> g = new MGrid<>(Person.class);
        
        g.withProperties("firstName", "lastName", "age", "addresses[1].street");
        
        // Awesome, now the API actually tells you what you should pass as 
        // parameters and what you'll get as return type.
        g.setRows(listOfPersons);
        g.selectRow(selectedDude);
        Person selectedRow = g.getSelectedRow();

        return g;
    }

}
