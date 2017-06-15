package org.vaadin.viritin.v7.grid;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertyValueGenerator;
import java.util.ArrayList;
import org.vaadin.viritin.v7.ListContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Shabak Nikolay (nikolay.shabak@gmail.com)
 * @since 23.04.2016
 * @param <T> the entity type listed in the consumer of the container, Vaadin Grid
 */
public class GeneratedPropertyListContainer<T> extends ListContainer<T> {

    private static final long serialVersionUID = -8384723868776183241L;

    private final Map<String, PropertyValueGenerator<?>> propertyGenerators = new HashMap();
    protected final Class<T> type;

    /**
     * Property implementation for generated properties
     * @param <T>  property data type
     */
    protected static class GeneratedProperty<T> implements Property<T>  {

        private static final long serialVersionUID = -538857801793925329L;

        private final Item item;
        private final Object itemId;
        private final Object propertyId;
        private final PropertyValueGenerator<T> generator;

        GeneratedProperty(Item item, Object propertyId, Object itemId,
                                 PropertyValueGenerator<T> generator) {
            this.item = item;
            this.itemId = itemId;
            this.propertyId = propertyId;
            this.generator = generator;
        }

        @Override
        public T getValue() {
            return generator.getValue(item, itemId, propertyId);
        }

        @Override
        public void setValue(T newValue) throws ReadOnlyException {
            throw new ReadOnlyException("Generated properties are read only");
        }

        @Override
        public Class<? extends T> getType() {
            return generator.getType();
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        @Override
        public void setReadOnly(boolean newStatus) {
            if (newStatus) {
                // No-op
                return;
            }
            throw new UnsupportedOperationException(
                    "Generated properties are read only");
        }
    }

    /**
     * Item implementation for generated properties.
     */
    protected class GeneratedPropertyItem implements Item {

        private static final long serialVersionUID = 8231832690836075843L;

        private final Item wrappedItem;
        private final Object itemId;

        protected GeneratedPropertyItem(Object itemId, Item item) {
            this.itemId = itemId;
            wrappedItem = item;
        }

        @Override
        public Property getItemProperty(Object id) {
            if (propertyGenerators.containsKey(id)) {
                return createProperty(wrappedItem, id, itemId,
                        propertyGenerators.get(id));
            }
            return wrappedItem.getItemProperty(id);
        }

        @Override
        public Collection<?> getItemPropertyIds() {
            Set wrappedProperties = new HashSet<>(wrappedItem.getItemPropertyIds());
            wrappedProperties.addAll(propertyGenerators.keySet());
            return wrappedProperties;
        }

        @Override
        public boolean addItemProperty(Object id, Property property)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "GeneratedPropertyItem does not support adding properties");
        }

        @Override
        public boolean removeItemProperty(Object id)
                throws UnsupportedOperationException {
            throw new UnsupportedOperationException(
                    "GeneratedPropertyItem does not support removing properties");
        }

        /**
         * Tests if the given object is the same as the this object. Two Items
         * from the same container with the same ID are equal.
         *
         * @param obj
         *            an object to compare with this object
         * @return <code>true</code> if the given object is the same as this
         *         object, <code>false</code> if not
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null
                    || !obj.getClass().equals(GeneratedPropertyItem.class)) {
                return false;
            }
            final GeneratedPropertyItem li = (GeneratedPropertyItem) obj;
            return getContainer() == li.getContainer()
                    && itemId.equals(li.itemId);
        }

        @Override
        public int hashCode() {
            return itemId.hashCode();
        }

        private GeneratedPropertyListContainer getContainer() {
            return GeneratedPropertyListContainer.this;
        }
    }

    public GeneratedPropertyListContainer(Class<T> type) {
        super(type);
        this.type = type;
    }

    public GeneratedPropertyListContainer(Class<T> type, String... properties) {
        super(type);
        this.type = type;
        setContainerPropertyIds(properties);
    }

    public void addGeneratedProperty(String propertyId, PropertyValueGenerator<?> generator) {
        propertyGenerators.put(propertyId, generator);
        fireContainerPropertySetChange();
    }

    /**
     * @param <P> the presentation type, displays the generated value
     * @param propertyId the property id for generated property
     * @param presentationType the presentation type of the generated property
     * @param generator the generator that creates the property value on demand
     */
    public <P> void addGeneratedProperty(String propertyId,
                                         Class<P> presentationType,
                                         TypedPropertyValueGenerator.ValueGenerator<T, P> generator) {
        TypedPropertyValueGenerator<T, P> lambdaPropertyValueGenerator =
                new TypedPropertyValueGenerator<>(type, presentationType, generator);
        propertyGenerators.put(propertyId, lambdaPropertyValueGenerator);
        fireContainerPropertySetChange();
    }

    public void addGeneratedProperty(String propertyId,
                                     StringPropertyValueGenerator.ValueGenerator<T> generator) {
        StringPropertyValueGenerator<T> lambdaPropertyValueGenerator =
                new StringPropertyValueGenerator<>(type, generator);
        propertyGenerators.put(propertyId, lambdaPropertyValueGenerator);
        fireContainerPropertySetChange();
    }

    @Override
    public Class<?> getType(Object propertyId) {
        if (propertyGenerators.containsKey(propertyId)) {
            return propertyGenerators.get(propertyId).getType();
        }
        return super.getType(propertyId);
    }

    @Override
    public Item getItem(Object itemId) {
        if (itemId == null) {
            return null;
        }
        Item item = super.getItem(itemId);
        return createGeneratedPropertyItem(itemId, item);
    }

    @Override
    public Collection<String> getContainerPropertyIds() {
           // create returned list of propertyIds
        ArrayList<String> properties = new ArrayList<>();

        // add all propertyIds of the underlying ListContainer
        properties.addAll(super.getContainerPropertyIds());

        // add the propertyIds for all the given generators
        properties.addAll(propertyGenerators.keySet());

        return properties;
    }

    private <T> Property<T> createProperty(final Item item,
                                           final Object propertyId, final Object itemId,
                                           final PropertyValueGenerator<T> generator) {
        return new GeneratedProperty<>(item, propertyId, itemId, generator);
    }

    private Item createGeneratedPropertyItem(final Object itemId,
                                             final Item item) {
        return new GeneratedPropertyItem(itemId, item);
    }

}
