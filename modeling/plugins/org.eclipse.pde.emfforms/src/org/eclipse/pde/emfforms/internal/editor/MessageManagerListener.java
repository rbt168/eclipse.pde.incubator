/** * Copyright (c) 2009 Anyware Technologies and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html *  * Contributors: *     Anyware Technologies - initial API and implementation * * $Id$ */package org.eclipse.pde.emfforms.internal.editor;import java.util.HashMap;import java.util.Map;import org.eclipse.core.databinding.DataBindingContext;import org.eclipse.core.databinding.ValidationStatusProvider;import org.eclipse.core.databinding.observable.IObservable;import org.eclipse.core.databinding.observable.list.*;import org.eclipse.core.databinding.observable.value.IValueChangeListener;import org.eclipse.core.databinding.observable.value.ValueChangeEvent;import org.eclipse.core.runtime.IStatus;import org.eclipse.core.runtime.MultiStatus;import org.eclipse.jface.databinding.swt.ISWTObservable;import org.eclipse.jface.dialogs.IMessageProvider;import org.eclipse.swt.widgets.Control;import org.eclipse.swt.widgets.Widget;import org.eclipse.ui.forms.IMessageManager;/** * Converts the {@link IStatus} objects from the status {@link Map} of a * {@link DataBindingContext} into by handled by a UI Forms IMessageManager. *  */public class MessageManagerListener implements IListChangeListener {	private IMessageManager mgr;	private static final KeyMap keyMap = new KeyMap();	public MessageManagerListener(IMessageManager mgr) {		this.mgr = mgr;	}	public void handleListChange(ListChangeEvent event) {		ListDiff diff = event.diff;		for (ListDiffEntry d : diff.getDifferences()) {			if (d.isAddition())				addValidationStatusProviderListener((ValidationStatusProvider) d.getElement());			// else			// TODO removeStatusProviderListener((ValidationStatusProvider)			// d.getElement());		}	}	/**	 * @param element	 */	private void addValidationStatusProviderListener(final ValidationStatusProvider element) {		element.getValidationStatus().addValueChangeListener(new IValueChangeListener() {			public void handleValueChange(ValueChangeEvent event) {				changeMessage(element);			}		});	}	protected void changeMessage(ValidationStatusProvider validationStatusProvider) {		IStatus status = (IStatus) validationStatusProvider.getValidationStatus().getValue();		if (status.getSeverity() == IStatus.OK) {			removeMessage(validationStatusProvider);			return;		}		Control control = guessControl(validationStatusProvider);		if (control != null) {			String message = ""; //$NON-NLS-1$			if (((MultiStatus) status).getChildren()[0].getChildren().length > 0) {				message = ((MultiStatus) status).getChildren()[0].getChildren()[0].getMessage();			} else {				message = status.getChildren()[0].getMessage();			}			mgr.addMessage(control, message, null, keyMap.getMessageProviderKey(status.getSeverity()), control);		} else {			mgr.addMessage(getKey(validationStatusProvider), ((MultiStatus) status).getChildren()[0].getChildren()[0].getMessage(), null, keyMap.getMessageProviderKey(status.getSeverity()));		}	}	protected void removeMessage(ValidationStatusProvider validationStatusProvider) {		Control control = guessControl(validationStatusProvider);		if (control != null) {			mgr.removeMessage(control, control);		} else {			mgr.removeMessage(getKey(validationStatusProvider));		}	}	protected Control guessControl(ValidationStatusProvider validationStatusProvider) {		Control control = guessControl((IObservable) validationStatusProvider.getTargets().get(0));		if (control != null) {			return control;		}		control = guessControl((IObservable) validationStatusProvider.getModels().get(0));		return control;	}	/**	 * if this observable is bound to a control it will be returned	 */	protected Control guessControl(IObservable value) {		if (value instanceof ISWTObservable) {			Widget w = ((ISWTObservable) value).getWidget();			if (w instanceof Control) {				return (Control) w;			}		}		return null;	}	protected String getKey(ValidationStatusProvider validationStatusProvider) {		return validationStatusProvider.hashCode() + ""; //$NON-NLS-1$	}	protected static class KeyMap {		private Map<Integer, Integer> keymap = new HashMap<Integer, Integer>();		protected KeyMap() {			keymap.put(Integer.valueOf(IStatus.ERROR), Integer.valueOf(IMessageProvider.ERROR));			keymap.put(Integer.valueOf(IStatus.INFO), Integer.valueOf(IMessageProvider.WARNING));			keymap.put(Integer.valueOf(IStatus.INFO), Integer.valueOf(IMessageProvider.INFORMATION));			keymap.put(Integer.valueOf(IStatus.OK), Integer.valueOf(IMessageProvider.NONE));			keymap.put(Integer.valueOf(IStatus.CANCEL), Integer.valueOf(IMessageProvider.INFORMATION));		}		protected int getMessageProviderKey(int iStatusKey) {			return keymap.get(Integer.valueOf(iStatusKey)).intValue();		}	}}