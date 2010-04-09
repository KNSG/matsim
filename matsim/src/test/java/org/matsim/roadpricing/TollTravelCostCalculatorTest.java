/* *********************************************************************** *
 * project: org.matsim.*
 * TollTravelCostCalculatorTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.roadpricing;

import org.matsim.api.core.v01.ScenarioImpl;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.population.LegImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.costcalculators.FreespeedTravelTimeCost;
import org.matsim.core.router.util.AStarLandmarksFactory;
import org.matsim.core.router.util.DijkstraFactory;
import org.matsim.core.router.util.PersonalizableTravelCost;
import org.matsim.core.router.util.PreProcessLandmarks;
import org.matsim.core.router.util.TravelCost;
import org.matsim.roadpricing.RoadPricingScheme.Cost;
import org.matsim.testcases.MatsimTestCase;

/**
 * Tests the correct working of {@link TollTravelCostCalculator} by using it
 * to calculate some routes with {@link PlansCalcRoute}.
 *
 * @author mrieser
 */
public class TollTravelCostCalculatorTest extends MatsimTestCase {

	public void testDistanceTollRouter() {
		Config config = loadConfig(null);
		ScenarioImpl scenario = new ScenarioImpl();
		Fixture.createNetwork2(scenario);
		NetworkLayer network = scenario.getNetwork();
		// a basic toll where only the morning hours are tolled
		RoadPricingScheme toll = new RoadPricingScheme();
		toll.setType("distance");
		toll.addLink(scenario.createId("5"));
		toll.addLink(scenario.createId("11"));
		Fixture.createPopulation2(scenario);
		PopulationImpl population = scenario.getPopulation();
		FreespeedTravelTimeCost timeCostCalc = new FreespeedTravelTimeCost(config.charyparNagelScoring());
		PersonalizableTravelCost costCalc = new TollTravelCostCalculator(timeCostCalc, toll); // we use freespeedTravelCosts as base costs

		AStarLandmarksFactory routerFactory = new AStarLandmarksFactory(network, timeCostCalc);

		PreProcessLandmarks commonRouterData = new PreProcessLandmarks(timeCostCalc);
		commonRouterData.run(network);

		Person person1 = population.getPersons().get(new IdImpl("1"));
		LegImpl leg = ((LegImpl) (person1.getPlans().get(0).getPlanElements().get(1)));

		// 1st case: without toll, agent chooses shortest path
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, new DijkstraFactory()).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) ((LegImpl) (person1.getPlans().get(0).getPlanElements().get(1))).getRoute());
		// also test it with A*-Landmarks
		clearRoutes(population);
		assertNull(leg.getRoute()); // make sure the cleaning worked. we do this only once, then we believe it.
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, routerFactory).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());

		Cost morningCost = toll.addCost(6*3600, 10*3600, 0.0006); // 0.0006 * link_length(100m) = 0.06, which is slightly below the threshold of 0.0666
		// 2nd case: with a low toll, agent still chooses shortest path
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());
		// also test it with A*-Landmarks
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, routerFactory).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());

		// 3rd case: with a higher toll, agent decides to drive around tolled link
		toll.removeCost(morningCost);
		toll.addCost(6*3600, 10*3600, 0.0007); // new morning toll, this should be slightly over the threshold
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc).run(population);
		Fixture.compareRoutes("2 3 4 6", (NetworkRoute) leg.getRoute());
		// also test it with A*-Landmarks
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, routerFactory).run(population);
		Fixture.compareRoutes("2 3 4 6", (NetworkRoute) leg.getRoute());
	}

	public void testCordonTollRouter() {
		Config config = loadConfig(null);
		ScenarioImpl scenario = new ScenarioImpl();
		Fixture.createNetwork2(scenario);
		NetworkLayer network = scenario.getNetwork();
		// a basic toll where only the morning hours are tolled
		RoadPricingScheme toll = new RoadPricingScheme();
		toll.setType("cordon");
		toll.addLink(scenario.createId("5"));
		toll.addLink(scenario.createId("11"));
		Fixture.createPopulation2(scenario);
		PopulationImpl population = scenario.getPopulation();
		FreespeedTravelTimeCost timeCostCalc = new FreespeedTravelTimeCost(config.charyparNagelScoring());
		PersonalizableTravelCost costCalc = new TollTravelCostCalculator(timeCostCalc, toll); // we use freespeedTravelCosts as base costs

		AStarLandmarksFactory routerFactory = new AStarLandmarksFactory(network, timeCostCalc);

		Person person1 = population.getPersons().get(new IdImpl("1"));
		LegImpl leg = ((LegImpl) (person1.getPlans().get(0).getPlanElements().get(1)));

		// 1st case: without toll, agent chooses shortest path
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());
		// also test it with A*-Landmarks
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, routerFactory).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());

		// 2nd case: with a low toll, agent still chooses shortest path and pay the toll
		Cost morningCost = toll.addCost(6*3600, 10*3600, 0.06);
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());
		toll.removeCost(morningCost);
		// also test it with A*-Landmarks
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, routerFactory).run(population);
		Fixture.compareRoutes("2 5 6", (NetworkRoute) leg.getRoute());

		// 3rd case: with a higher toll, agent decides to drive around tolled link
		toll.addCost(6*3600, 10*3600, 0.067);
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc).run(population);
		Fixture.compareRoutes("2 3 4 6", (NetworkRoute) leg.getRoute());
		// also test it with A*-Landmarks
		clearRoutes(population);
		new PlansCalcRoute(config.plansCalcRoute(), network, costCalc, timeCostCalc, routerFactory).run(population);
		Fixture.compareRoutes("2 3 4 6", (NetworkRoute) leg.getRoute());
	}

	/**
	 * Clears all routes from all legs of all persons in the given population to make sure they are calculated from new.
	 *
	 * @param population
	 */
	private void clearRoutes(final PopulationImpl population) {
		for (Person person : population.getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				for (PlanElement pe : plan.getPlanElements()) {
					if (pe instanceof LegImpl) {
						((LegImpl) pe).setRoute(null);
					}
				}
			}
		}
	}
}
