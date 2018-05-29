package it.polito.tdp.flight.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestModel {
	
	public static void main(String args[]) {

		Model m = new Model ();
		
		Airport a = m.getAirports().get(0);
		
		System.out.println(a.getRoutes());
		
		m.createGraph();
		m.printStats();
		
		Set<Airport> biggestSCC = m.getBiggestSCC();
		System.out.println(biggestSCC.size());
		
		try {
			
//			int id1 = 8591; // New York
//			int id2 = 1525; // Bergamo
		
			List <Airport> airportList = new ArrayList <> (biggestSCC);
			int id1 = airportList.get(0).getAirportId();
			int id2 = airportList.get(15).getAirportId();
			
			System.out.println(m.getShortestPath(id1, id2));
			
		}catch (RuntimeException e) {
			System.out.println("Airport code error");
		}
	
	}
}
