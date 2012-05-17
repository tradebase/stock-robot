package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;

import model.simulation.SimModel;

import view.SimView;

/**
 * 
 * @author Daniel
 */
public class SimController implements IController {
	private SimView view = new SimView();
	private SimModel model;
	
	
	private SimulationAlgorithmSettingsController settingController;
	private SimResultController simResultController;
	
	ActionListener startSimulation = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (simResultController != null) {
				simResultController.display(model);
			}
		}
	};
	ActionListener comboBoxListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			model.setAlgorithm((String)((JComboBox)e.getSource()).getSelectedItem());
		}
	};
	
	ActionListener configureAlgorithm = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			settingController = new SimulationAlgorithmSettingsController(model.getAlgorithm());
			settingController.display(model);
		}
	};

	
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		
	}

	@Override
	public void display(Object model) {
		defineSubControllers();
		
		if (this.model == null)
			this.model = new SimModel();
		
		
		view.addActions(getActionListeners());
		view.display(this.model);
		
	}

	@Override
	public void cleanup() {
		view.cleanup();
	}

	@Override
	public Map<String, EventListener> getActionListeners() {
		Map<String, EventListener> actions = new HashMap<String,EventListener>();
		actions.put(SimView.STARTSIMULATION, startSimulation);
		actions.put(SimView.COMBOBOX, comboBoxListener);
		actions.put(SimView.CONFIGUREALGORTIHM, configureAlgorithm);
		return actions;
	}

	@Override
	public String getName() {
		return "SimController";
	}

	@Override
	public void defineSubControllers() {
		simResultController = new SimResultController();
	}

}
