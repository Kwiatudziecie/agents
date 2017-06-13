package a;

import java.util.Date;

import FIPA.DateTime;
import jade.core.AID;
import jade.util.Logger;

public class AgentHelper {

	private final Logger logger = Logger.getMyLogger(getClass().getName());
	public String Name;
	public double x;
	public double y;
	public int type;
	public int food;
	public AgentHelper(){
		Name=null; x=0; y=0; type=0; food=0;
	}
	public AgentHelper(String name, double _x, double _y, int _type, int _food){
		Name=name; x=_x; y=_y; type=_type; food=_food;
	}
	public AgentHelper(String msg){
		String[] parts = msg.split(";");
		for(int i=0;i<parts.length; i++){
			String[] field = parts[i].split(splitterEncoded());
			if(field.length==2){
				if(field[0].equals("name"))
					Name=field[1];
				else if(field[0].equals("x"))
					x=Double.parseDouble(field[1]);
				else if(field[0].equals("y"))
					y=Double.parseDouble(field[1]);
				else if(field[0].equals("type"))
					type=Integer.parseInt(field[1]);
				else if(field[0].equals("quantity"))
					food=Integer.parseInt(field[1]);
				else
					logger.log(Logger.SEVERE, "not recognized splitted field " + parts[i]);
			}
			else
				logger.log(Logger.SEVERE, "not splitted field " + parts[i]);
		}
	}

	public String toString(){
		return new String("name"+splitter()
		+Name+";x"+splitter()
		+x+";y"+splitter()
		+y+";type"+splitter()
		+type+";quantity"+splitter()
		+food);
	}
	public String Query(int _food, String _name){
		return new String("name"+splitter()
		+_name+";quantity"+splitter()
		+_food);
	}
	public String client(String query){
		String[] parts = query.split(";");
		for(int i=0;i<parts.length; i++){
			String[] field = parts[i].split(splitterEncoded());
			if(field.length==2){
				if(field[0].equals("name"))
					return field[1];
				}
			else
				logger.log(Logger.SEVERE, "not recognized query: " + query);
		}
		return new String();
	}
	public int food(String query){
		String[] parts = query.split(";");
		for(int i=0;i<parts.length; i++){
			String[] field = parts[i].split(splitterEncoded());
			if(field.length==2){
				if(field[0].equals("quantity"))
					return Integer.parseInt(field[1]);
				}
			else
				logger.log(Logger.SEVERE, "not recognized query: " + query);
		}
		return 0;
	}
	private String splitter(){
		return "|";
	}
	private String splitterEncoded(){
		return "\\|";
	}
}
