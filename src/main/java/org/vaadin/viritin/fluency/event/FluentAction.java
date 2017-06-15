/*
 * Copyright 2016 Matti Tahvonen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.viritin.fluency.event;

import com.vaadin.event.Action;
import com.vaadin.event.ShortcutListener;
import com.vaadin.server.Resource;

/**
 * An {@link Action} complemented by fluent setters.
 *
 * @author Max Schuster
 * @see Action
 */
public class FluentAction extends Action {

    // Javadoc copied form Vaadin Framework
    /**
     * Constructs a new action with the given caption.
     *
     * @param caption
     *            the caption for the new action.
     */
    public FluentAction(String caption) {
        super(caption);
    }

    // Javadoc copied form Vaadin Framework
    /**
     * Constructs a new action with the given caption string and icon.
     *
     * @param caption
     *            the caption for the new action.
     * @param icon
     *            the icon for the new action.
     */
    public FluentAction(String caption, Resource icon) {
        super(caption, icon);
    }

    /**
     * A {@link Notifier} complemented by fluent setters.
     * 
     * @param <S>
     *            Self-referential generic type
     * @see Notifier
     */
    public interface FluentNotifier<S extends FluentNotifier<S>>
            extends Notifier, FluentContainer<S> {
        public default <T extends Action & Action.Listener> S withAction(
                T action) {
            ((Notifier) this).addAction(action);
            return (S) this;
        }
    }

    /**
     * A {@link ShortcutNotifier} complemented by fluent setters.
     *
     * @param <S>
     *            Self-referential generic type
     * @see ShortcutNotifier
     */
    public interface FluentShortcutNotifier<S extends FluentShortcutNotifier<S>>
            extends ShortcutNotifier {

        public S withShortcutListener(ShortcutListener shortcut);

    }

    /**
     * A {@link Container} complemented by fluent setters.
     *
     * @param <S>
     *            Self-referential generic type
     * @see Container
     */
    public interface FluentContainer<S extends FluentContainer<S>>
            extends Container {

        // Javadoc copied form Vaadin Framework
        /**
         * Registers a new action handler for this container
         *
         * @param actionHandler
         *            the new handler to be added.
         * @return this (for method chaining)
         * @see #addActionHandler(com.vaadin.event.Action.Handler)
         */
        public default S withActionHandler(Action.Handler actionHandler) {
            ((Container) this).addActionHandler(actionHandler);
            return (S) this;
        }

    }

    // Javadoc copied form Vaadin Framework
    /**
     * Sets the caption.
     *
     * @param caption
     *            the caption to set.
     * @return This action
     * @see #setCaption(java.lang.String)
     */
    public FluentAction withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    // Javadoc copied form Vaadin Framework
    /**
     * Sets the icon.
     *
     * @param icon
     *            the icon to set.
     * @return This action
     * @see #setIcon(com.vaadin.server.Resource)
     */
    public FluentAction withIcon(Resource icon) {
        setIcon(icon);
        return this;
    }

}
