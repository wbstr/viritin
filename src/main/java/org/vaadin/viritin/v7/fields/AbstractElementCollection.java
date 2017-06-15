package org.vaadin.viritin.v7.fields;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.viritin.v7.BeanBinder;
import org.vaadin.viritin.v7.MBeanFieldGroup;
import org.vaadin.viritin.v7.MBeanFieldGroup.FieldGroupListener;

import com.vaadin.v7.data.Validator;
import com.vaadin.ui.Component;
import com.vaadin.v7.ui.CustomField;
import com.vaadin.v7.ui.DefaultFieldFactory;
import com.vaadin.v7.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.util.ReflectTools;

/**
 * A superclass for fields suitable for editing collection of referenced objects tied to parent
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
 * @author Matti Tahvonen
 * @param <ET> The type in the entity collection. The type must have empty
 * paremeter constructor or you have to provide Instantiator.
 */
public abstract class AbstractElementCollection<ET> extends CustomField<Collection> {

    private static final long serialVersionUID = 7785110162928180695L;

    public static class ElementAddedEvent<ET> extends Component.Event {

        private static final long serialVersionUID = 2263765199849601501L;

        private final ET element;

        public ElementAddedEvent(AbstractElementCollection source, ET element) {
            super(source);
            this.element = element;
        }

        public ET getElement() {
            return element;
        }

    }

    public static class ElementRemovedEvent<ET> extends Component.Event {

        private static final long serialVersionUID = 574545902966053269L;

        private final ET element;

        public ElementRemovedEvent(AbstractElementCollection source, ET element) {
            super(source);
            this.element = element;
        }

        public ET getElement() {
            return element;
        }

    }

    public interface ElementAddedListener<ET> extends Serializable {

        void elementAdded(ElementAddedEvent<ET> e);
    }

    public interface ElementRemovedListener<ET> extends Serializable {

        void elementRemoved(ElementRemovedEvent<ET> e);
    }

    private static final Method addedMethod;
    private static final Method removedMethod;

    static {
        addedMethod = ReflectTools.findMethod(ElementAddedListener.class,
                "elementAdded", ElementAddedEvent.class);
        removedMethod = ReflectTools.findMethod(ElementRemovedListener.class,
                "elementRemoved", ElementRemovedEvent.class);
    }

    public AbstractElementCollection<ET> addElementAddedListener(
            ElementAddedListener<ET> listener) {
        addListener(ElementAddedEvent.class, listener, addedMethod);
        return this;
    }

    public AbstractElementCollection<ET> removeElementAddedListener(
            ElementAddedListener listener) {
        removeListener(ElementAddedEvent.class, listener, addedMethod);
        return this;
    }

    public AbstractElementCollection<ET> addElementRemovedListener(
            ElementRemovedListener<ET> listener) {
        addListener(ElementRemovedEvent.class, listener, removedMethod);
        return this;
    }

    public AbstractElementCollection<ET> removeElementRemovedListener(
            ElementRemovedListener listener) {
        removeListener(ElementRemovedEvent.class, listener, removedMethod);
        return this;
    }

    private Instantiator<ET> instantiator;
    private Instantiator<?> oldEditorInstantiator;
    private EditorInstantiator<?, ET> newEditorInstantiator;

    private final Class<ET> elementType;

    private final Class<?> editorType;
    protected ET newInstance;

    private final FieldGroupListener fieldGroupListener = new FieldGroupListener() {

        private static final long serialVersionUID = 2498166986711639744L;

        @Override
        public void onFieldGroupChange(MBeanFieldGroup beanFieldGroup) {
            if (beanFieldGroup.getItemDataSource().getBean() == newInstance) {
                if (!getFieldGroupFor(newInstance).isValid()) {
                    return;
                }
                getAndEnsureValue().add(newInstance);
                fireEvent(new ElementAddedEvent(AbstractElementCollection.this,
                        newInstance));
                setPersisted(newInstance, true);
                onElementAdded();
            }
            // TODO could optimize for only repainting on changed validity
            fireValueChange(false);
        }
    };
    private List<String> visibleProperties;
    private boolean allowNewItems = true;
    private boolean allowRemovingItems = true;
    private boolean allowEditItems = true;
    
    public boolean isAllowNewItems() {
        return allowNewItems;
    }

    public boolean isAllowRemovingItems() {
        return allowRemovingItems;
    }

    public boolean isAllowEditItems() {
        return allowEditItems;
    }
    
    public AbstractElementCollection<ET> setAllowEditItems(boolean allowEditItems) {
        this.allowEditItems = allowEditItems;
        return this;
    }
    
    public AbstractElementCollection<ET> setAllowRemovingItems(boolean allowRemovingItems) {
        this.allowRemovingItems = allowRemovingItems;
        return this;
    }

    public AbstractElementCollection<ET> withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    @Override
    public void validate() throws Validator.InvalidValueException {
        super.validate();
        Collection v = getValue();
        if(v != null) {
            for (Object o : v) {
                for (Field f : getFieldGroupFor((ET) o).getFields()) {
                    f.validate();
                }
            }
        }
    }

    private Collection<ET> getAndEnsureValue() {
        Collection<ET> value = getValue();
        if (value == null) {
            if (getPropertyDataSource() == null) {
                // this should never happen :-)
                return new HashSet<ET>();
            }
            Class fieldType = getPropertyDataSource().getType();
            if (fieldType.isInterface()) {
                if (fieldType == List.class) {
                    value = new ArrayList<ET>();
                } else { // Set
                    value = new HashSet<ET>();
                }
            } else {
                try {
                    value = (Collection) fieldType.newInstance();
                } catch (IllegalAccessException | InstantiationException ex) {
                    throw new RuntimeException(
                            "Could not instantiate the used colleciton type", ex);
                }
            }
            getPropertyDataSource().setValue(value);
        }
        return value;
    }

    /**
     * @param allowNewItems true if a new element row should be automatically 
     * added
     * @return the configured field instance
     */
    public AbstractElementCollection<ET> setAllowNewElements(
            boolean allowNewItems) {
        this.allowNewItems = allowNewItems;
        return this;
    }

    public interface Instantiator<ET> extends Serializable {

        ET create();
    }
    
    public interface EditorInstantiator<T, ET> extends Serializable {
      T create(ET entity);
    }

    public AbstractElementCollection(Class<ET> elementType,
            Class<?> formType) {
        this.elementType = elementType;
        this.editorType = formType;
    }

    public AbstractElementCollection(Class<ET> elementType, Instantiator i,
            Class<?> formType) {
        this.elementType = elementType;
        this.instantiator = i;
        this.editorType = formType;
    }

    public Class<ET> getElementType() {
        return elementType;
    }

    protected ET createInstance() {
        if (instantiator != null) {
            return instantiator.create();
        } else {
            try {
                return elementType.newInstance();
            } catch (IllegalAccessException | InstantiationException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    protected Object createEditorInstance(ET pojo) {
        if (newEditorInstantiator != null) {
            return newEditorInstantiator.create(pojo);
        } else {
            if (oldEditorInstantiator != null) {
                return oldEditorInstantiator.create();              
            } else {
                try {
                    return editorType.newInstance();
                } catch (IllegalAccessException | InstantiationException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public EditorInstantiator<?, ET> getNewEditorInstantiator() {
      return newEditorInstantiator;
    }

    public void setNewEditorInstantiator(
          EditorInstantiator<?, ET> editorInstantiator) {
      this.newEditorInstantiator = editorInstantiator;
    }

    public Instantiator<?> getEditorInstantiator() {
      return oldEditorInstantiator;
    }

    public void setEditorInstantiator(
            Instantiator<?> editorInstantiator) {
        this.oldEditorInstantiator = editorInstantiator;
    }
  
    private class EditorStuff implements Serializable {
        private static final long serialVersionUID = 5132645136059482705L;
        MBeanFieldGroup<ET> bfg;
        Object editor;

        private EditorStuff(MBeanFieldGroup<ET> editor, Object o) {
            this.bfg = editor;
            this.editor = o;
        }
    }

    private final Map<ET, EditorStuff> pojoToEditor = new IdentityHashMap<>();

    protected final MBeanFieldGroup<ET> getFieldGroupFor(ET pojo) {
        EditorStuff es = pojoToEditor.get(pojo);
        if (es == null) {
            Object o = createEditorInstance(pojo);
            MBeanFieldGroup bfg = BeanBinder.bind(pojo, o).withEagerValidation(
                    fieldGroupListener);
            es = new EditorStuff(bfg, o);
            // TODO listen for all changes for proper modified/validity changes
            pojoToEditor.put(pojo, es);
        }
        return es.bfg;
    }
    
    protected final Component getComponentFor(ET pojo, String property) {
        EditorStuff editorsstuff = pojoToEditor.get(pojo);
        if (editorsstuff == null) {
            Object o = createEditorInstance(pojo);
            MBeanFieldGroup bfg = BeanBinder.bind(pojo, o).withEagerValidation(
                    fieldGroupListener);
            editorsstuff = new EditorStuff(bfg, o);
            // TODO listen for all changes for proper modified/validity changes
            pojoToEditor.put(pojo, editorsstuff);
        }
        Component c = editorsstuff.bfg.getField(property);
        if(c == null) {
            try {
                // property that is not a property editor field but custom UI "column"
                java.lang.reflect.Field f = editorType.getDeclaredField(property);
                f.setAccessible(true);
                c = (Component) f.get(editorsstuff.editor);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(AbstractElementCollection.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
            if(c == null) {
                c = new Label("");
            }
        }
        
        return c;
    }


    public void addElement(ET instance) {
        getAndEnsureValue().add(instance);
        addInternalElement(instance);
        fireValueChange(false);
        fireEvent(new ElementAddedEvent<>(this, instance));
    }

    public void removeElement(ET elemnentToBeRemoved) {
        removeInternalElement(elemnentToBeRemoved);
        getAndEnsureValue().remove(elemnentToBeRemoved);
        fireValueChange(false);
        fireEvent(new ElementRemovedEvent<>(this, elemnentToBeRemoved));
    }

    public AbstractElementCollection<ET> setVisibleProperties(
            List<String> properties) {
        visibleProperties = properties;
        return this;
    }

    public List<String> getVisibleProperties() {
        if (visibleProperties == null) {

            visibleProperties = new ArrayList<>();

            for (java.lang.reflect.Field f : editorType.getDeclaredFields()) {
                // field order can be counted since jdk6 
                if (Field.class.isAssignableFrom(f.getType())) {
                    visibleProperties.add(f.getName());
                }
            }

        }
        return visibleProperties;
    }

    public AbstractElementCollection<ET> setVisibleProperties(
            List<String> properties, List<String> propertyHeaders) {
        visibleProperties = properties;
        Iterator<String> it = propertyHeaders.iterator();
        for (String prop : visibleProperties) {
            setPropertyHeader(prop, it.next());
        }
        return this;
    }

    private final Map<String, String> propertyToHeader = new HashMap<>();

    public AbstractElementCollection<ET> setPropertyHeader(String propertyName,
            String propertyHeader) {
        propertyToHeader.put(propertyName, propertyHeader);
        return this;
    }

    protected String getPropertyHeader(String propertyName) {
        String header = propertyToHeader.get(propertyName);
        if (header == null) {
            header = DefaultFieldFactory.createCaptionByPropertyId(propertyName);
        }
        return header;
    }

    @Override
    protected Component initContent() {
        return getLayout();
    }

    @Override
    protected void setInternalValue(Collection newValue) {
        super.setInternalValue(newValue);
        clear();
        Collection<ET> value = newValue;
        if (value != null) {
            for (ET v : value) {
                addInternalElement(v);
            }
        }
        onElementAdded();

    }

    @Override
    public Class<? extends Collection> getType() {
        return Collection.class;
    }

    abstract protected void addInternalElement(ET v);

    abstract protected void setPersisted(ET v, boolean persisted);

    abstract protected void removeInternalElement(ET v);

    abstract protected Layout getLayout();

    abstract protected void onElementAdded();

}
