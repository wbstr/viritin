package org.vaadin.viritin.v7.fields;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.v7.ui.Table;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.v7.MBeanFieldGroup;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * A field suitable for editing collection of referenced objects tied to parent
 * object only. E.g. OneToMany/ElementCollection fields in JPA world.
 * <p>
 * Some features/restrictions:
 * <ul>
 * <li>The field is valid when all elements are valid.
 * <li>The field is always non buffered
 * <li>The element type needs to have an empty paremeter constructor or user
 * must provide an Instantiator.
 * </ul>
 *
 * Elements in the edited collection are modified with BeanFieldGroup. Fields
 * should defined in a class. A simple usage example for editing
 * List&gt;Address&lt; adresses:
 * <pre><code>
 *  public static class AddressRow {
 *      EnumSelect type = new EnumSelect();
 *      MTextField street = new MTextField();
 *      MTextField city = new MTextField();
 *      MTextField zipCode = new MTextField();
 *  }
 *
 *  public static class PersonForm&lt;Person&gt; extends AbstractForm {
 *      private final ElementCollectionTable&lt;Address&gt; addresses
 *              = new ElementCollectionTable&lt;Address&gt;(Address.class,
 *                      AddressRow.class).withCaption("Addressess");
 *
 * </code></pre>
 *
 * <p>
 * By default the field contains a button to add new elements. If instances are
 * added with some other method (or UI shouldn't add them at all), you can
 * configure this with setAllowNewItems. Deletions can be configured with
 * setAllowRemovingItems.
 * <p>
 *
 * @param <ET> The type in the entity collection. The type must have empty
 * parameter constructor or you have to provide Instantiator.
 */
public class ElementCollectionTable<ET> extends AbstractElementCollection<ET> {

    private static final long serialVersionUID = 8055987316151594559L;

    private MTable<ET> table;

    private MButton addButton = new MButton(FontAwesome.PLUS,
            new Button.ClickListener() {

                private static final long serialVersionUID = 6115218255676556647L;

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    addElement(createInstance());
                }
            });

    private IdentityHashMap<ET, MButton> elementToDelButton = new IdentityHashMap<>();

    boolean inited = false;

    private MVerticalLayout layout = new MVerticalLayout();

    private String[] deleteElementStyles;

    private String disabledDeleteThisElementDescription = "Fill this row to add a new element, currently ignored";

    public ElementCollectionTable(Class<ET> elementType, Class<?> formType) {
        super(elementType, formType);
    }

    public ElementCollectionTable(Class<ET> elementType, Instantiator i, Class<?> formType) {
        super(elementType, i, formType);
    }
    
    @Override
    public void attach() {
        super.attach();
        ensureInited();
    }

    @Override
    public void addInternalElement(final ET v) {
        ensureInited();
        table.addBeans(v);
    }

    @Override
    public void removeInternalElement(ET v) {
        table.removeItem(v);
        elementToDelButton.remove(v);
    }

    @Override
    public Layout getLayout() {
        return layout;
    }

    public MButton getAddButton() {
        return addButton;
    }

    /**
     * @return the Table used in the implementation. Configure carefully.
     */
    public MTable<ET> getTable() {
        return table;
    }

    @Override
    public void setPersisted(ET v, boolean persisted) {
        // NOP
    }

    private void ensureInited() {
        if (!inited) {
            layout.setMargin(false);
            setHeight("300px");
            table = new MTable<ET>(getElementType()).withFullWidth();
            for (Object propertyId : getVisibleProperties()) {
                table.addGeneratedColumn(propertyId,
                        new Table.ColumnGenerator() {

                            private static final long serialVersionUID = 3637140096807147630L;

                            @Override
                            public Object generateCell(Table source,
                                    Object itemId,
                                    Object columnId) {
                                MBeanFieldGroup<ET> fg = getFieldGroupFor(
                                        (ET) itemId);
                                if (!isAllowEditItems()) {
                                    fg.setReadOnly(true);
                                }
                                Component component = fg.getField(columnId);
                                if(component == null) {
                                    getComponentFor((ET) itemId,
                                            columnId.toString());
                                }
                                return component;
                            }
                        });

            }
            ArrayList<Object> cols = new ArrayList<Object>(
                    getVisibleProperties());

            if (isAllowRemovingItems()) {
                table.addGeneratedColumn("__ACTIONS",
                        new Table.ColumnGenerator() {

                            private static final long serialVersionUID = 492486828008202547L;

                            @Override
                            public Object generateCell(Table source,
                                    final Object itemId,
                                    Object columnId) {

                                MButton b = new MButton(FontAwesome.TRASH_O).
                                withListener(
                                        new Button.ClickListener() {

                                            private static final long serialVersionUID = -1257102620834362724L;

                                            @Override
                                            public void buttonClick(
                                                    Button.ClickEvent event) {
                                                        removeElement(
                                                                (ET) itemId);
                                                    }
                                        }).withStyleName(
                                        ValoTheme.BUTTON_ICON_ONLY);
                                b.setDescription(getDeleteElementDescription());

								if (getDeleteElementStyles() != null) {
									for (String style : getDeleteElementStyles()) {
										b.addStyleName(style);
									}
								}

                                elementToDelButton.put((ET) itemId, b);
                                return b;
                            }
                        });
                table.setColumnHeader("__ACTIONS", "");
                cols.add("__ACTIONS");
            }

            table.setVisibleColumns(cols.toArray());
            for (Object property : getVisibleProperties()) {
                table.setColumnHeader(property, getPropertyHeader(property.
                        toString()));
            }
            layout.expand(table);
            if (isAllowNewItems()) {
                layout.addComponent(addButton);
            }
            inited = true;
        }
    }

    @Override
    public void clear() {
        if (inited) {
            table.removeAllItems();
            elementToDelButton.clear();
        }
    }

    public String getDisabledDeleteElementDescription() {
        return disabledDeleteThisElementDescription;
    }

    public void setDisabledDeleteThisElementDescription(
            String disabledDeleteThisElementDescription) {
        this.disabledDeleteThisElementDescription = disabledDeleteThisElementDescription;
    }

    public String getDeleteElementDescription() {
        return deleteThisElementDescription;
    }

    private String deleteThisElementDescription = "Delete this element";

    public void setDeleteThisElementDescription(
            String deleteThisElementDescription) {
        this.deleteThisElementDescription = deleteThisElementDescription;
    }

    public String[] getDeleteElementStyles() {
            return deleteElementStyles;
    }

    public void addDeleteElementStyles(String... deleteElementStyles) {
            this.deleteElementStyles = deleteElementStyles;
    }


    @Override
    public void onElementAdded() {
        // NOP
    }

    @Override
    public ElementCollectionTable<ET> setPropertyHeader(String propertyName,
            String propertyHeader) {
        super.setPropertyHeader(propertyName, propertyHeader);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> setVisibleProperties(
            List<String> properties, List<String> propertyHeaders) {
        super.setVisibleProperties(properties, propertyHeaders);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> setVisibleProperties(
            List<String> properties) {
        super.setVisibleProperties(properties);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> setAllowNewElements(
            boolean allowNewItems) {
        super.setAllowNewElements(allowNewItems);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> setAllowRemovingItems(
            boolean allowRemovingItems) {
        super.setAllowRemovingItems(allowRemovingItems);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> withCaption(String caption) {
        super.withCaption(caption);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> removeElementRemovedListener(
            ElementRemovedListener listener) {
        super.removeElementRemovedListener(listener);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> addElementRemovedListener(
            ElementRemovedListener<ET> listener) {
        super.addElementRemovedListener(listener);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> removeElementAddedListener(
            ElementAddedListener listener) {
        super.removeElementAddedListener(listener);
        return this;
    }

    @Override
    public ElementCollectionTable<ET> addElementAddedListener(
            ElementAddedListener<ET> listener) {
        super.addElementAddedListener(listener);
        return this;
    }

    public ElementCollectionTable<ET> withEditorInstantiator(
            Instantiator instantiator) {
        setEditorInstantiator(instantiator);
        return this;
    }

}
