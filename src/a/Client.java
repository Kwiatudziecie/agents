package a;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import javax.swing.JOptionPane;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class Client extends Agent {

	private final Logger logger = Logger.getMyLogger(getClass().getName());
	private int food;//food needed
	private int type;//type of food
	private int need;//max need of food
	DFAgentDescription[] banks;//banks

	@Override
	protected void setup() {
		try {
			banks = DFService.search(this, AgentsUtils.getDFD(AgentsUtils.getSD(Const.Bank())));
		} catch (FIPAException e) {
			logger.log(Logger.SEVERE, "Cannot get banks", e);
		}
		
		Object[] args = getArguments();
		food=0;
		type=Const.TypeTwo();
		need=200;
		if (args != null) {
			for(int i=0;i<args.length; i++){
				if(args[i].equals("-need"))
					need=Integer.parseInt((String) args[++i]);
				else if(args[i].equals("-type"))
					type=Integer.parseInt((String) args[++i]);
				else if(args[i].equals("-food")){
					food=Integer.parseInt((String) args[++i]);
		    	logger.info("Client set with parameters");
				}
			}
		}
		Random rand = new Random();
		double x, y;
		x=rand.nextDouble()*100;
		y=rand.nextDouble()*100;
		
		
		TickerBehaviour loop = new TickerBehaviour( this, 1000 )
	      {
			private static final long serialVersionUID = 1L;

			protected void onTick() {
				if(rand.nextInt(7)==0){//frequency
					try {
						banks = DFService.search(myAgent, AgentsUtils.getDFD(AgentsUtils.getSD(Const.Bank())));
					} catch (FIPAException e) {
						logger.log(Logger.SEVERE, "Cannot get banks", e);
					}
					food+=rand.nextInt(need);
	            	ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
	            	for(DFAgentDescription b:banks){
		            	msg.addReceiver(b.getName());
	            	}
	            	msg.setLanguage(Const.Language());
	            	msg.setContent(new AgentHelper(getLocalName(), x, y, type, food).toString());
	            	msg.setPerformative(ACLMessage.REQUEST);
	            	send(msg);
							}
	         }
	      };
	      addBehaviour( loop );
	           
	      

			Behaviour listener = (new CyclicBehaviour(this) {
				@Override
				public void action() {
					MessageTemplate mt = MessageTemplate.MatchLanguage(Const.Language());
					ACLMessage msg = myAgent.receive(mt);
								
					if (msg != null) {
						if(msg.getPerformative()==ACLMessage.CONFIRM){
							food-=Integer.parseInt(msg.getContent());
							System.out.println("Food received! Saved "+Integer.parseInt(msg.getContent())+"kg.");
							JOptionPane.showMessageDialog(null,"Food received! Saved "+Integer.parseInt(msg.getContent())+"kg.");
						}
						else if(msg.getPerformative()==ACLMessage.PROPOSE){
			         	for(DFAgentDescription b:banks){
			            	if(msg.getSender().equals(b.getName()))
			            	{
				            	int amount = food < Integer.parseInt(msg.getContent())? food : Integer.parseInt(msg.getContent());
								 ACLMessage response = msg.createReply();
								 response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
								 response.setContent(new AgentHelper().Query(amount, getLocalName()));
								 send(response);}}
						}
						} else {
						block();
						
					}
				}
			});
			addBehaviour(listener);
		}
	}