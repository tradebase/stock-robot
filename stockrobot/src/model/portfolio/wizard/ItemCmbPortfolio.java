package model.portfolio.wizard;

import model.portfolio.IPortfolio;

/**
 * @author Mattias Markehed
 * mattias.markehed@gmail.com
 *
 * filename: PortfolioGui.java
 * Description:
 * Item_cmb_Portfolio is used as an adapter for the mapping IPortfolios
 * to the combo box containing portfolios
 */
public class ItemCmbPortfolio{
	
	private final IPortfolio portfolio;
	
	public ItemCmbPortfolio(final IPortfolio portfolio){
		this.portfolio = portfolio;
	}
	
	public IPortfolio getPortfolio(){
		return portfolio;
	}
	
	@Override
	public String toString(){
		return portfolio.getName();
	}
}