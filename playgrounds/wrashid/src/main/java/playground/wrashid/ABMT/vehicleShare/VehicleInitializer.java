package playground.wrashid.ABMT.vehicleShare;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.population.LegImpl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

/**
 * 
 * @author wrashid
 *
 */
public class VehicleInitializer implements IterationStartsListener {

	public static HashMap<Plan, Boolean> hasElectricVehicle = new HashMap<Plan, Boolean>();

	public static boolean hasCarLeg(Plan plan) {
		for (PlanElement pe : plan.getPlanElements()) {
			if (pe instanceof LegImpl) {
				LegImpl leg = (LegImpl) pe;
				leg.getMode().equalsIgnoreCase(TransportMode.car);
				return true;
			}
		}
		return false;
	}

	public static void initialize(Plan plan) {
		if (hasCarLeg(plan)) {
			if (!hasElectricVehicle.containsKey(plan)) {
				Random rand = new Random();
				hasElectricVehicle.put(plan, rand.nextBoolean());
			}
		}
	}

	@Override
	public void notifyIterationStarts(IterationStartsEvent event) {
		int evCount = 0;
		int cvCount = 0;
		int newKeysAdded = 0;
		int existingKeyUsed = 0;
		int numberOfPlansRemovedFromHM = 0;
		HashSet<Plan> allCurrentPlans = new HashSet<Plan>();
        for (Person person : event.getControler().getScenario().getPopulation().getPersons()
				.values()) {
			
			if (person.getId().toString().equalsIgnoreCase("111106347")){
				System.out.println();
			}
			
			if (hasCarLeg(person.getSelectedPlan())) {
				if (!hasElectricVehicle.containsKey(person.getSelectedPlan())) {
					Random rand = new Random();
					hasElectricVehicle.put(person.getSelectedPlan(),
							rand.nextBoolean());
					newKeysAdded++;
				} else {
					existingKeyUsed++;
				}

				if (hasElectricVehicle.get(person.getSelectedPlan())) {
					evCount++;
				} else {
					cvCount++;
				}

				for (Plan plan : person.getPlans()) {
					allCurrentPlans.add(plan);
				}
			}
		}

		LinkedList<Plan> removePlans = new LinkedList<Plan>();
		for (Plan plan : hasElectricVehicle.keySet()) {
			if (!allCurrentPlans.contains(plan)) {
				removePlans.add(plan);
			}
		}

		for (Plan plan : removePlans) {
			hasElectricVehicle.remove(plan);
			numberOfPlansRemovedFromHM++;
		}

		System.out.println("iteration: " + event.getIteration());

		System.out.println("numberOfPlansRemovedFromHM: "
				+ numberOfPlansRemovedFromHM);
		System.out.println("evCount: " + evCount);
		System.out.println("cvCount: " + cvCount);
		System.out.println("hasElectricVehicle.size(): "
				+ hasElectricVehicle.size());
		System.out.println("newKeysAdded: " + newKeysAdded);
		System.out.println("existingKeyUsed: " + existingKeyUsed);
		System.out.println();
	}

}
