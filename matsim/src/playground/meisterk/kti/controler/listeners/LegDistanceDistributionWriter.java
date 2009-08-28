/* *********************************************************************** *
 * project: org.matsim.*
 * LegDistanceDistributionWriter.java
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

package playground.meisterk.kti.controler.listeners;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import org.apache.log4j.Logger;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.population.PopulationImpl;

import playground.meisterk.org.matsim.population.algorithms.PopulationLegDistanceDistribution;
import playground.meisterk.org.matsim.population.algorithms.PopulationLegDistanceDistribution.CrosstabFormat;

public class LegDistanceDistributionWriter implements IterationEndsListener {

	public static final double[] distanceClasses = new double[]{
		0.0, 
		100, 200, 500, 
		1000, 2000, 5000, 
		10000, 20000, 50000, 
		100000, 200000, 500000,
		1000000};

	private final String filename;
	
	private final static Logger log = Logger.getLogger(LegDistanceDistributionWriter.class);

	public LegDistanceDistributionWriter(String filename) {
		super();
		this.filename = filename;
	}

	public void notifyIterationEnds(IterationEndsEvent event) {
		
		if (event.getIteration() % 10 == 0) {

			Controler c = event.getControler();
			PopulationImpl pop = c.getPopulation();
			
			PrintStream out = null;
			try {
				out = new PrintStream(org.matsim.core.controler.Controler.getIterationFilename(filename, event.getIteration()));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			PopulationLegDistanceDistribution algo = new PopulationLegDistanceDistribution(out);
			algo.run(pop);
			
			log.info("Writing results file...");
			for (boolean isCumulative : new boolean[]{false, true}) {
				for (CrosstabFormat crosstabFormat : CrosstabFormat.values()) {
					algo.printDistanceClasses(crosstabFormat, isCumulative, distanceClasses);
				}
			}
			algo.printDeciles(true);
			out.close();
			log.info("Writing results file...done.");

		}
		
	}

}
