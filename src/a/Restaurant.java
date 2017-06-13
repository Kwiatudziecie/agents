package a;
import java.util.Random;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class Restaurant extends Agent {
	private static final long serialVersionUID = 1L;
	private Logger logger = Logger.getMyLogger(getClass().getName());
	private int food; //food quantity in kg e.g. 100/cycle
	private int supply; //food supply and max consumption
	private int type; //type of food
	private int old; //previous amount of food
	DFAgentDescription[] banks;//banks

	@Override
	protected void setup() {
		try {
			banks = DFService.search(this, AgentsUtils.getDFD(AgentsUtils.getSD(Const.Bank())));
		} catch (FIPAException e) {
			logger.log(Logger.SEVERE, "Cannot get banks", e);
		}
		Object[] args = getArguments();
		type=Const.TypeOne();
		supply=100;
		if (args != null) {
					for(int i=0;i<args.length; i++){
				if(args[i].equals("-supply"))
					supply=Integer.parseInt((String) args[++i]);
				else if(args[i].equals("-type"))
					type=Integer.parseInt((String) args[++i]);
	    		}
					logger.info("Restuarant set with parameters");
		}
		food=0;

		Random rand = new Random();
		double x, y;
		x=rand.nextDouble()*100;
		y=rand.nextDouble()*100;
		old=supply;

		
		TickerBehaviour loop = new TickerBehaviour( this, 1000 )
	      {
			private static final long serialVersionUID = 1L;
			protected void onTick() {
	        	old=food;
	            food+=supply;
	            food-=(rand.nextInt(supply/2)+supply/2);
	            if((int)(old/supply)<(int)(food/supply))
	            {
					try {
						banks = DFService.search(myAgent, AgentsUtils.getDFD(AgentsUtils.getSD(Const.Bank())));
					} catch (FIPAException e) {
						logger.log(Logger.SEVERE, "Cannot get banks", e);
					}
	            	ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
	            	for(DFAgentDescription b:banks){
		            	msg.addReceiver(b.getName());
	            	}
		            msg.setLanguage(Const.Language());
		            msg.setContent(new AgentHelper(getLocalName(), x, y, type, food).toString());
		            myAgent.send(msg);
		    		logger.info("Inform message sent");
	            	
	            }
	         }
	      };
	      addBehaviour( loop );
		
		Behaviour listener = (new CyclicBehaviour(this) {
			private static final long serialVersionUID = 1L;
					@Override
			public void action() {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchLanguage(Const.Language()),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					for(DFAgentDescription b:banks){
		            	if(msg.getSender().equals(b.getName())){
		            		int amount = new AgentHelper().food(msg.getContent());
							food-=amount;
				    		logger.info("Restaurant sent food");
			            	ACLMessage foodtruck = new ACLMessage(ACLMessage.CONFIRM);
			            	foodtruck.addReceiver(new AID(new AgentHelper().client(msg.getContent()), AID.ISLOCALNAME));
			            	foodtruck.setLanguage(Const.Language());
			            	foodtruck.setContent(Integer.toString(amount));
			            	send(foodtruck);
				    	}
		            }
				} else {
					block();
				}
			}
		});
		addBehaviour(listener);
		
	}
}