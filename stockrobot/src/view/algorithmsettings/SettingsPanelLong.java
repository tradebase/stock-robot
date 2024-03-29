package view.algorithmsettings;

import java.awt.Dimension;
import java.awt.TextField;


import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import model.database.jpa.tables.AlgorithmSettingLong;

import view.components.GUIFactory;

/**
 * 
 * 
 * @author kristian
 *
 */
public class SettingsPanelLong extends JPanel implements ChangeListener {
	
	/**
	 * Serial Version!
	 */
	private static final long serialVersionUID = -5023291097157808280L;
	
	private long minValue;
	private long maxValue;
	private long initValue;
	private String desc;
	
	private TextField 	ta;
	private long value;
	
	private AlgorithmSettingLong model;
	
	public SettingsPanelLong( AlgorithmSettingLong model, String desc, long initValue, long minValue, long maxValue ) {
		
		this.model = model;
		this.desc = desc;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.initValue = initValue;
		
		value = this.initValue;
	}
	
	public void init() {
		
		//Factory baby
		GUIFactory fact = new GUIFactory();
		
		JPanel container = fact.getDefaultContainer();
		add( container );
		container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
		
		//Instantiate a header text saying
		JLabel header = fact.getDefaultLabel("Something");
		header.setText( desc + "::long" );
		container.add( header );
		
		JPanel subContainer = fact.getInvisibleContainer();
		BoxLayout subContainerLayout = new BoxLayout( subContainer, BoxLayout.Y_AXIS );
		subContainer.setLayout( subContainerLayout );
		container.add( subContainer );
		
		ta = new TextField();
		ta.setBounds(0, 0, 100, 30);
		ta.setText( "" + ((int)initValue) );
		ta.setSize( new Dimension(100, 30) );
		ta.setMinimumSize(new Dimension(100, 30));
		ta.repaint();
		subContainer.add( ta );
		
		JSlider fromDate = new JSlider( SwingConstants.HORIZONTAL, (int) minValue , (int) maxValue , (int) initValue  );
		fromDate.addChangeListener(this);
		subContainer.add( fromDate );
	}
	
	/**
	 * TODO: Maybe not have this in the view
	 */
	@Override
	public void stateChanged( ChangeEvent e ) {
		
		JSlider source = (JSlider) e.getSource();
		value = source.getValue();
		ta.setText( "" + value );
		model.setValue( source.getValue() );
	}
	
	/**
	 * Whatever the jSlider shows
	 * 
	 * @return
	 */
	public long getValue() {
		
		return value;
	}
}