package playground.balac.onewaycarsharingredisgned.qsimparking;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.controler.Controler;
import org.matsim.core.utils.collections.QuadTree;

import java.util.ArrayList;


public class OneWayCarsharingRDWithParkingVehicleLocation {


	private QuadTree<OneWayCarsharingRDWithParkingStation> vehicleLocationQuadTree;	
	
	
	public OneWayCarsharingRDWithParkingVehicleLocation(Controler controler, ArrayList<OneWayCarsharingRDWithParkingStation> stations)  {
	    double minx = (1.0D / 0.0D);
	    double miny = (1.0D / 0.0D);
	    double maxx = (-1.0D / 0.0D);
	    double maxy = (-1.0D / 0.0D);

        for (Link l : controler.getScenario().getNetwork().getLinks().values()) {
	      if (l.getCoord().getX() < minx) minx = l.getCoord().getX();
	      if (l.getCoord().getY() < miny) miny = l.getCoord().getY();
	      if (l.getCoord().getX() > maxx) maxx = l.getCoord().getX();
	      if (l.getCoord().getY() <= maxy) continue; maxy = l.getCoord().getY();
	    }
	    minx -= 1.0D; miny -= 1.0D; maxx += 1.0D; maxy += 1.0D;

	    vehicleLocationQuadTree = new QuadTree<OneWayCarsharingRDWithParkingStation>(minx, miny, maxx, maxy);
	    
	    
	    for(OneWayCarsharingRDWithParkingStation f: stations) {  
	    	
	    	ArrayList<String> vehIDs = f.getIDs();
			ArrayList<String> newvehIDs = new ArrayList<String>();
			for (String s : vehIDs) {
				newvehIDs.add(s);
			}
	    	
			OneWayCarsharingRDWithParkingStation fNew = new OneWayCarsharingRDWithParkingStation(f.getLink(), f.getNumberOfVehicles(), newvehIDs,  f.getNumberOfAvailableParkingSpaces());		

	    	vehicleLocationQuadTree.put(fNew.getLink().getCoord().getX(), fNew.getLink().getCoord().getY(), fNew);
	    }
	    
	  
	    
	   
	  }
	public QuadTree<OneWayCarsharingRDWithParkingStation> getQuadTree() {
		
		return vehicleLocationQuadTree;
	}
	
	public void addVehicle(OneWayCarsharingRDWithParkingStation station, String id) {
		
			station.getIDs().add(id);
			station.addCar();
		
	}
	
	public void removeVehicle(OneWayCarsharingRDWithParkingStation station, String id) {
		
		station.getIDs().remove(id);
		station.removeCar();
				
	}
	
	public void reserveParkingSpot(OneWayCarsharingRDWithParkingStation station) {
		
		station.reserveParkingSpot();
	}
	
	public void freeParkingSpot(OneWayCarsharingRDWithParkingStation station) {
		
		station.freeParkingSpot();
	}

	
	
}
