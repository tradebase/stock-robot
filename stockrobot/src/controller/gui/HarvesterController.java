package controller.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import view.HarvesterView;

import model.scraping.core.Harvester;

/**
 * Controller for Harvester.
 * @author Erik
 *
 */
public class HarvesterController {
	
	private Harvester model;
	private HarvesterView view;
	private Logger log;



	public HarvesterController(Harvester model, HarvesterView view) {
		this.model = model;
		this.view = view;
		this.log = new Logger();
		
	    view.addbtnStartParserListener(new StartBtnListener());
	    view.addbtnStopParserListener(new StopBtnListener());
	    view.addbtnStatusListener(new StatusBtnListener());
	    view.addbtnClearLogListener(new ClearLogBtnListener());
	    
	}
	
	private class Logger {
		
		public Logger(){
			
		}
		
		public void printStatus(boolean status){
			if(status){
				view.addLogItem("Parser is up and running.");
			}
			else {
				view.addLogItem("Parser closed,crashed or shutting down.");
			}
		}
		
		public void start(){
			view.addLogItem("Parser started at 08:56.");
		}		
		
		public void stop(){
			view.addLogItem("Parser stopped at 08:59.");
		}

		public void failStart() {
			view.addLogItem("Parser failed to start. Already started or crashed.");
		}
	}

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
				System.out.println("*** Malformed portnumber");
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
    

}