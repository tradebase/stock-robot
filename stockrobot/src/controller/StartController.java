package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import model.robot.StartModel;

import utils.global.Log;
import utils.global.Log.TAG;
import view.StartView;

public class StartController implements IController {
	private StartView view = new StartView();
	private StartModel model;
	
	private IController harvester;
	private IController astroController;
	
	public static String name = "StartController";
	
	private ActionListener startAstroAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					astroController.display(view.getParserLocation());
				}
			});
			
		}
	};
	private ActionListener startHavester = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					harvester.display(new Object());
				}
			});
		}
	};
	private ActionListener comboBox = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			TAG selectedTag = (TAG)((JComboBox)e.getSource()).getSelectedItem();
			Log.setFilter(selectedTag, true);
		}
	};

	@Override
	public void display(Object model) {
		defineSubControllers();
		
		this.model = (StartModel) model;
		
		view.addActions(getActionListeners());
		
		view.display(this.model);
	}

	@Override
	public void cleanup() {} //NOPMD

	public Map<String, EventListener> getActionListeners() {
		Map<String, EventListener> actions = new HashMap<String,EventListener>();
		
		actions.put(StartView.STARTPARSER, startHavester);
		actions.put("Stop Parser", null);
		
		actions.put(StartView.STARTASTRO, startAstroAction);
		actions.put("Stop Astro", null);
		
		actions.put(StartView.LOGLEVEL, comboBox);
		return actions;
	}

	private void defineSubControllers() {
		if (harvester == null) {
			harvester = new HarvesterController();
		}
		
		if (astroController == null) {
			astroController = new AstroController();
		}
	}
}