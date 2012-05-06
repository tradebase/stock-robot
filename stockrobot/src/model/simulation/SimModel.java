package model.simulation;

import java.util.List;

import model.algorithms.loader.PluginAlgortihmLoader;

public class SimModel {
	private String algorithm;
	private int stocksBack;
	private PluginAlgortihmLoader algorithmLoader = PluginAlgortihmLoader.getInstance();
	
	public SimModel() {
		if (algorithmLoader.getAlgorithmNames().size() == 0)
			algorithmLoader.reloadAlgorithmClasses();
		
		if (algorithmLoader.getAlgorithmNames().size() > 0) 
			algorithm = algorithmLoader.getAlgorithmNames().get(0);
		setStocksBack(300);
	}
	
	public String getAlgorithm() {
		return algorithm;
	}
	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}
	public int getStocksBack() {
		return stocksBack;
	}
	public void setStocksBack(int stocksBack) {
		this.stocksBack = stocksBack;
	}
	public List<String> getAlgorithms() {
		return algorithmLoader.getAlgorithmNames();
	}
	
	
}
