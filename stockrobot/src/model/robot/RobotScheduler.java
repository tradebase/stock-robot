package model.robot;

import java.util.Date;

import utils.global.Log;
import model.database.jpa.IJPAHelper;
import model.database.jpa.JPAHelper;
import model.database.jpa.tables.StockNames;
import model.database.jpa.tables.StockPrices;
import model.portfolio.IPortfolioHandler;

/**
 * Description:
 * RobotHandler takes care of running the stock algorithms
 * on a frequent basis.
 * 
 * @author Mattias Markehed
 * mattias.markehed@gmail.com
 */
public class RobotScheduler implements Runnable{

	private RobotHandler handler;	

	public static final long MILLI_SECOND = 1;
	public static final long SECOND = 1000*MILLI_SECOND;

	private boolean isRunning = false;
	private boolean pause = false;
	private boolean isStoped = false;

	@SuppressWarnings("unused")
	private long freq = 0;
	private long pauseLength = SECOND;
	private boolean usingServer = false;

	private Date lastStockPriceDate = new Date(0);
	private RobotSchedulerClient client;

	public RobotScheduler(IPortfolioHandler portfolioHandler){
		handler = new RobotHandler(portfolioHandler);
		this.pauseLength = 5*SECOND;
		usingServer = false;
	}
	public RobotScheduler(IPortfolioHandler portfolioHandler, String host, int port) throws Exception {
		handler = new RobotHandler(portfolioHandler);
		usingServer = true;
		
		cleanDatabaseCache();
		
		client = new RobotSchedulerClient(this, host, port);
		client.start();
	}
	/**
	 * Completely stops the runner after current run is through
	 * 
	 * @return true if stopped else false if already stopped
	 */
	public synchronized boolean stop(){
		if(isRunning){
			Log.log(Log.TAG.NORMAL , "RobotScheduler Stoped!" );
			isRunning = pause = false;	

			if (usingServer)
				client.stopClient();
		}
		return isStoped;
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
			Log.log(Log.TAG.VERBOSE , "RobotScheduler pause!" );
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
		if (pause) {
			Log.log(Log.TAG.VERY_VERBOSE ,"Wakeing RobotScheduler" );
			synchronized (this) {
				pause = false;
			}
		}
		boolean result = false;
		if(pause && isRunning){
			Log.log(Log.TAG.VERBOSE , "RobotScheduler unpause!" );
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
		Log.log(Log.TAG.VERY_VERBOSE , "RobotScheduler!" );
		while(isRunning && !usingServer) {
			Log.log(Log.TAG.VERY_VERBOSE ,"RobotScheduler: RUN!" );

			runAlgorithms();

			try {
				Thread.sleep(pauseLength);
			} catch (InterruptedException e) {
			}
		}
		isStoped = true;
	}

	private void runAlgorithms() {
		if (isRunning) {
			cleanDatabaseCache();
			StockPrices lastStock = JPAHelper.getInstance().getLastStockPrice();

			if (lastStock != null && lastStock.getTime().getTime() > lastStockPriceDate.getTime()) {
				handler.runAlgorithms();
				lastStockPriceDate = lastStock.getTime();			
			}
		}
	}

	/**
	 * This method is only called by {@link RobotSchedulerClient}
	 */
	public void doWork() {
		
		cleanDatabaseCache();
		
		runAlgorithms();
	}
	/**
	 * Evicts all cached stocks in system.
	 */
	private void cleanDatabaseCache() {
		IJPAHelper jpaHelper = JPAHelper.getInstance();
		jpaHelper.getEntityManager().evictAll(StockPrices.class, StockNames.class);
	}
	public void cleanup() {
		if (client != null) {
			client.cleanup();
		}
	}
}
