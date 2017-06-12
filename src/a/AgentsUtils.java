package a;

import java.util.Iterator;

import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.Location;
import jade.domain.AMSService;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.WhereIsAgentAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;
import jade.util.Logger;

/**
 * Utility methods used by agents.
 */
public class AgentsUtils {

	private final static Logger logger = Logger.getMyLogger(AgentsUtils.class.getName());

	/**
	 * Registers agent in the yellow pages.
	 * 
	 * @param agent
	 *            agent to register
	 * @param type
	 *            service type
	 */
	public static void registerAgent(Agent agent, String type) {
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		sd.setName(type + "Service");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(agent.getAID());
		dfd.addServices(sd);
		try {
			DFService.register(agent, dfd);
		} catch (FIPAException e) {
			logger.log(Logger.SEVERE, "Cannot register agent", e);
		}
	}

	public static void unregisterAgent(Agent agent, String type) {
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		sd.setName(type + "Service");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(agent.getAID());
		dfd.removeServices(sd);
		try {
			DFService.register(agent, dfd);
		} catch (FIPAException e) {
			logger.log(Logger.SEVERE, "Cannot register agent", e);
		}
	}
	/**
	 * Gets service description.
	 * 
	 * @param type
	 *            type of service
	 * @return ServiceDescription
	 */
	public static ServiceDescription getSD(String type) {
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		return sd;
	}

	/**
	 * Gets a DFAgentDescription with the service added.
	 * 
	 * @param service
	 *            service type
	 * @return DFAgentDescription
	 */
	public static DFAgentDescription getDFD(ServiceDescription service) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.addServices(service);
		return dfd;
	}

	/**
	 * Prepare request to ask AMS for location of targetAgent.
	 * 
	 * @param runnerAgent
	 *            agent that wants to move
	 * @param targetAgent
	 *            agent that is in the target location
	 * @return ACLMessage
	 */
	public static ACLMessage prepareRequestToAMS(Agent runnerAgent, AID targetAgent) {
		ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
		request.addReceiver(runnerAgent.getAMS());
		request.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		request.setOntology(MobilityOntology.NAME);
		request.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

		// Creates the content of the ACLMessage
		Action act = new Action();
		act.setActor(runnerAgent.getAMS());

		WhereIsAgentAction action = new WhereIsAgentAction();
		action.setAgentIdentifier(targetAgent);
		act.setAction(action);

		try {
			runnerAgent.getContentManager().fillContent(request, act);
		} catch (CodecException | OntologyException e) {
			logger.log(Logger.SEVERE, "Error at creating message to AMS", e);
		}
		return request;
	}

	/**
	 * Parses a response from AMS to get the location.
	 * 
	 * @param runner
	 *            agent that wants to move
	 * @param response
	 *            ACLMessage from AMS
	 * @return target Location
	 */
	@SuppressWarnings("rawtypes")
	public static Location parseAMSResponse(Agent runner, ACLMessage response) {
		Result results = null;
		try {
			results = (Result) runner.getContentManager().extractContent(response);
		} catch (CodecException | OntologyException e) {
			logger.log(Logger.SEVERE, "Error at parsing message from AMS", e);
		}
		Iterator it = results.getItems().iterator();
		Location loc = null;
		if (it.hasNext())
			loc = (Location) it.next();
		return loc;
	}
	/* for changing containers */
	/*
	public AID getTargetAgent(Agent agent, String targetAgent) {
		//Get Agent AID
		SearchConstraints sC = new SearchConstraints();
		sC.setMaxResults(new Long(-1));
		AMSAgentDescription [] agents;
		try { 
			agents = AMSService.search(agent, new AMSAgentDescription(), sC);	
			for (int i = 0; i < agents.length; i++) {
				if (agents[i].getName().getLocalName().equals(targetAgent))
					return new AID(targetAgent, AID.ISLOCALNAME);
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		
		return null;
	}*/
}