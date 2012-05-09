package controller.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.sql.Timestamp;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

import view.HarvesterView;

import model.scraping.core.Harvester;

/**
 * Controller for Harvester.
 * @author Erik
 *
 */
public class HarvesterController implements IController {
	
	private Harvester model;
	private HarvesterView view;
	private Logger log;	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private class StartBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				
				int port = Integer.parseInt(view.getPortTextbox());
				model.setPort(port);
				System.out.println("*** Server port set to: " + port);

				
				if(view.simulateStocksChecked()){
					if(model.startSimulation()){
						log.start();
						view.setStartInactive();
						view.setStopActive();
					}
					else {
						log.failStart();
					}
				}
				else {
					if(model.startParser()){
						log.start();
						view.setStartInactive();
						view.setStopActive();
					}
					else {
						log.failStart();
					}
				}
			} catch (NumberFormatException e) {
				log.failPortNumber(view.getPortTextbox());
			}
		}
	}
	
	private class StopBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(view.forceStopChecked()){
				model.stopParser();
			}
			else {
				model.forceStop();
			}
			log.stop();
			view.setStopInactive();
			view.setStartActive();
		}
	}
	
	private class StatusBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			log.printStatus(model.status());
		}
	}

	
	private class ClearLogBtnListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			view.clearLog();
		}
	}

	public HarvesterController() {
		this.log = new Logger();
		this.view = new HarvesterView();
		this.model = new Harvester(45000);
		model.setPropertyChangeSupport(pcs);
		pcs.addPropertyChangeListener(this);
	}
	
	private class Logger {
		long totalLoops = 0;

		public Logger(){
			
		}
		
		public void failPortNumber(String portTextbox) {
			addToList(portTextbox + " is not a valid port-number. ");
		}

		public void printStatus(boolean status){
			if(status){
				addToList("Parser is up and running. Total loops: " + totalLoops);
			}
			else {
				addToList("Parser closed,crashed or shutting down. Total loops: " + totalLoops);
			}
		}
		public void start(){
			addToList("Parser started at 08:56.");
		}		
		
		public void parsingLoop(long timeElapsed){
			totalLoops++;
			addToList("Parsing loop finished in " + timeElapsed + " ms. ");
		}		
		
		public void stop(){
			addToList("Parser stopped at 08:59.");
		}

		public void failStart() {
			addToList("Parser failed to start. Already started or crashed.");
		}
		
		private void addToList(String input){
			Date date= new java.util.Date();
			String time = new Timestamp(date.getTime()) + "";
			view.addLogItem("[" + time.substring(11, 19) + "] - " + input);
		}
		
	}




	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if(event.getPropertyName().equals("Parsing done.")){
			log.parsingLoop((Long) event.getNewValue());
		}
	}


	@Override
	public void display(Object model) {
		view.addActions(getActionListeners());
		view.display(this.model);
	}


	@Override
	public void cleanup() {
		
	}


	@Override
	public Map<String, EventListener> getActionListeners() {
		Map<String, EventListener> actions = new HashMap<String,EventListener>();
		actions.put(HarvesterView.START_PARSER, new StartBtnListener());
		actions.put(HarvesterView.STOP_PARSER, new StopBtnListener());
		actions.put(HarvesterView.PRINT_STATUS, new StatusBtnListener());
		actions.put(HarvesterView.CLEAR_LOG, new ClearLogBtnListener());

		return actions;
	}


	@Override
	public void addSubController(IController subController) {
		
	}


	@Override
	public void defineSubControllers() {
		
	}


	@Override
	public String getName() {
		return "Harvester Controller";
	}
    

}
