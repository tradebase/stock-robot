package model.wizard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.Map;

import model.IModel;




public abstract class WizardPageModel implements IModel {

	private PropertyChangeSupport observers;
	protected Map<Integer,Boolean> properties = new HashMap<Integer,Boolean>(); //Returns a map with flags if property is set or not

	public WizardPageModel() {
	
		observers = new PropertyChangeSupport(this);
	}
		
	@Override
	public void addObserver(PropertyChangeListener listener){
		observers.addPropertyChangeListener(listener);
	}
	
	@Override
	public void removeObserver(PropertyChangeListener listener){
		observers.removePropertyChangeListener(listener);
	}
	
	public Map<Integer,Boolean> getProperties(){
		
		return properties;
	}
	
	public void removeProperty(int property){
		
		properties.remove(property);
	}
	
	public abstract boolean canFinish();
	
	public abstract void finish();
	
	public PropertyChangeSupport getObservers() { return observers; }
}
