package a;

import java.awt.RenderingHints.Key;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.Logger;

public class FoodBank extends Agent {

	private final Logger logger = Logger.getMyLogger(getClass().getName());
	List<AgentHelper> restaurants;
	List<AgentHelper> clients;
	
	Map<AgentHelper, AgentHelper> dictionary = new HashMap<AgentHelper, AgentHelper>();

	 private Codec codec = new SLCodec();
	   private Ontology ontology = HelpOntology.getInstance();
	
	@Override
	protected void setup() {
		
		getContentManager().registerLanguage(codec);
	      getContentManager().registerOntology(ontology);
		
		
		
		
		restaurants = new ArrayList<AgentHelper>();
		clients = new ArrayList<AgentHelper>();
		//getContentManager().registerLanguage(new );
		AgentsUtils.registerAgent(this, Const.Bank());//yellow pages
		logger.info("Bank registered");
		CyclicBehaviour listener = new CyclicBehaviour(this) {
			 public void action() {
				 MessageTemplate mt = MessageTemplate.MatchLanguage(Const.Language());
				 ACLMessage rcv = receive(mt);
				 if(rcv==null)
					 block();
				 else{
					 if(rcv.getPerformative()==ACLMessage.INFORM){
						 AgentHelper temp = new AgentHelper(rcv.getContent());
						 	restaurants.removeAll(restaurants.stream().filter(o -> o.Name.equals(temp.Name)).collect(Collectors.toList()));
								restaurants.add(temp);
								checkAgents(rcv);
						 	
						}
					 else if(rcv.getPerformative()==ACLMessage.REQUEST){
						 AgentHelper temp = new AgentHelper(rcv.getContent());
						 restaurants.removeAll(restaurants.stream().filter(o -> o.Name.equals(temp.Name)).collect(Collectors.toList()));
						 		clients.add(temp);
						 		checkAgents(rcv);
						 	
						}
					 else if(rcv.getPerformative()==ACLMessage.ACCEPT_PROPOSAL)
						 transfer(rcv);
					 else{
						 logger.info("Bank received wrong message");
					 }
				 }
			 }

				@Override
				public int onEnd() {
					AgentsUtils.unregisterAgent(myAgent, Const.Bank());
					return 0;
				}
			};
			
		addBehaviour(listener);
	}

	protected void checkAgents(ACLMessage rcv) {
		if(!(restaurants.isEmpty()||clients.isEmpty()))
		{
			List<AgentHelper> temp = new ArrayList<AgentHelper>();
			int a =1;
			Comparator<AgentHelper> c = (s1, s2) -> Integer.compare(s2.type, s1.type);
			clients.sort(c);
			for(int j=clients.size()-1; j>=0; j--){
				for(int i=0; i<restaurants.size(); i++){
					if(restaurants.get(i).type<=clients.get(i).type)
						temp.add(restaurants.get(i));
				}
				if(temp.size()>0)
				{
					AgentHelper rest=null;
					double min=3;
					boolean empty=false;
					for(AgentHelper t : temp){
						if(t.type<min)
							min=t.type;
						}
					for(int i=temp.size()-1; i>=0; i--){
						if(temp.get(i).type!=(int)min)
							temp.remove(i);
						//advanced calculation with timestamp
					}
					min=distance(clients.get(j).x, clients.get(j).y, temp.get(0).x, temp.get(0).y);
					rest=temp.get(0);
					for(AgentHelper t : temp){
						if(min>distance(clients.get(j).x, clients.get(j).y, t.x, t.y)){
							min=distance(clients.get(j).x, clients.get(j).y, t.x, t.y);
						rest=t;	
						}
					}
					pair(clients.get(j), rest);
					logger.info("Paired: "+ rest.Name + " with " + clients.get(j).Name);
					clients.remove(j);
					restaurants.remove(rest);
				}
			}
		}
		
	}
	protected void transfer(ACLMessage rcva) {
		ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
		AgentHelper key = dictionary.keySet().stream().filter(o -> o.Name.equals(rcva.getSender().getLocalName())).findFirst().get();
		msg1.addReceiver(new AID(dictionary.get(key).Name, AID.ISLOCALNAME));
    	msg1.setLanguage(Const.Language());
    	msg1.setContent(rcva.getContent());
    	send(msg1);
    	dictionary.remove(key);
	}

	protected void pair(AgentHelper c, AgentHelper r) {
		dictionary.put(c, r);
		ACLMessage msg1 = new ACLMessage(ACLMessage.PROPOSE);
		msg1.addReceiver(new AID(c.Name, AID.ISLOCALNAME));
    	msg1.setLanguage(Const.Language());
    	msg1.setContent(Integer.toString(r.food));
    	send(msg1);
	}
	
		private double distance(double x, double y, double x2, double y2){
		return Math.sqrt((x-x2)*(x-x2)+(y-y2)*(y-y2));
	}
}