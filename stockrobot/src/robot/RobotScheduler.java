package robot;

import generic.Log;
import portfolio.IPortfolioHandler;

/**
 * @author Mattias Markehed
 * mattias.markehed@gmail.com
 *
 * filename: RobotScheduler.java
 * Description:
 * RobotHandler takes care of running the stock algorithms
 * on a frequent basis. 
 */
public class RobotScheduler implements Runnable{

	private RobotHandler handler;	
	
	public static final long MILLI_SECOND = 1;
	public static final long SECOND = 1000*MILLI_SECOND;
	public static final long MINUTE = 60*SECOND;
	
	private boolean isRunning = false;
	private boolean pause = false;
	private long freq = 0;
	private long pauseLength = SECOND;

	public RobotScheduler(IPortfolioHandler portfolioHandler){
		handler = new RobotHandler(portfolioHandler);
	}
		
	/**
	 * Completely stops the runner after current run is through
	 * 
	 * @return true if stopped else false if already stopped
	 */
	public synchronized boolean stop(){
			
		boolean result = false;
		if(isRunning){
			Log.instance().log(Log.TAG.NORMAL , "RobotScheduler Stoped!" );
			isRunning = pause = false;	
			result = true;
		}
		return result;
	}
		
	/**
	 * Pauses the runner.
	 * gets uncaused by start() 
	 * 
	 * @return true if runner paused else false
	 */
	public synchronized boolean pause(){
		
		boolean result = false;
		if(!pause && isRunning){
			Log.instance().log(Log.TAG.VERBOSE , "RobotScheduler pause!" );
			result = pause = true;
		}
			
		return result;
	}
		
	/**
	* Unpauses the runner. Does nothing if parser isn't paused.
	* To make the unpause instant. Run interrupt on the thread running the scheduler.
	* 
	* @return return true if runner unpauses else false
	*/
	public synchronized boolean unpause(){
			
		boolean result = false;
		if(pause && isRunning){
			Log.instance().log(Log.TAG.VERBOSE , "RobotScheduler unpause!" );
			pause = false;
			result = true;
		}
		return result;
	}
	
	/**
	 * @return true if paused
	 */
	public boolean isPaused(){
				
		return pause;
	}
		
	/**
	 * Set the update frequency of the scheduler. The algorithms will be run once
	 * every milli second freq.
	 * 
	 * @param freq frequency in milli seconds
	 */
	public void setUpdateFrequency(long freq){
		this.freq = freq; 
	}
	
	/**
	 * Set the sleep time of the thread when paused. 
	 * 
	 * @param length time in milli seconds
	 */
	public void setPauseLength(long length){
		pauseLength	= length;
	}
	
	@Override
	public void run() {
		isRunning = true;
		Log.instance().log(Log.TAG.VERY_VERBOSE , "RobotScheduler!" );
		while(isRunning){
			
			//TODO make run interface to avoid polling
			while(pause){
				try {
					Thread.sleep(pauseLength);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			Log.instance().log(Log.TAG.VERY_VERBOSE ,"RobotScheduler: RUN!" );
			RobotScheduler.this.handler.runAlgorithms();
			try {
				Thread.sleep(freq);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
}
