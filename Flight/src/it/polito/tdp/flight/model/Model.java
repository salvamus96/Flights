package it.polito.tdp.flight.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.Graphs;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import com.javadocmd.simplelatlng.LatLng;
import com.javadocmd.simplelatlng.LatLngTool;
import com.javadocmd.simplelatlng.util.LengthUnit;

import it.polito.tdp.flight.db.FlightDAO;

public class Model {
	
	private FlightDAO fdao;
	private List<Airport> airports;
	private List<Airline> airlines;
	private List<Route> routes;
	
	// IMPLEMENTAZIONE PATTERN ORM
	private AirlineIdMap airlineIdMap;
	private AirportIdMap airportIdMap;
	private RouteIdMap routeIdMap;
	
	private Graph <Airport, DefaultWeightedEdge> grafo;
	
	public Model () {
		
		fdao = new FlightDAO ();
	
		this.airlineIdMap = new AirlineIdMap ();
		this.airportIdMap = new AirportIdMap ();
		this.routeIdMap = new RouteIdMap ();
		
		airlines = fdao.getAllAirlines(this.airlineIdMap);
		System.out.println(airlines.size());

		airports = fdao.getAllAirports(this.airportIdMap);
		System.out.println(airports.size());

		routes = fdao.getAllRoutes(this.airlineIdMap, this.airportIdMap, this.routeIdMap);
		System.out.println(routes.size());
	}

	public List <Airport> getAirports(){
		// se il metodo del dao restituisce una lista nulla conviene effettuare un controllo
		// per evitare una NullPointerException: otterremo una lista vuota non nulla
		if (this.airports == null)
			return new ArrayList <Airport> ();
		return this.airports;
	}
	
	public void createGraph () {
		
		grafo = new SimpleDirectedWeightedGraph <>(DefaultWeightedEdge.class);
		Graphs.addAllVertices(grafo, this.airports);
		
		for (Route r : routes) {
			Airport source = r.getSourceAirport();
			Airport destination = r.getDestinationAirport();
			
			// per evitare i cappi (self loop: loops not allow)
			if (!source.equals(destination)) {
				double weight = LatLngTool.distance(new LatLng(source.getLatitude(), source.getLongitude()),
											 new LatLng(destination.getLatitude(), destination.getLongitude()),
											 LengthUnit.KILOMETER);
			
				Graphs.addEdge(grafo, source, destination, weight);
			}
		}
		
		System.out.println(grafo.vertexSet().size());
		System.out.println(grafo.edgeSet().size());
		
	}
	
	public void printStats() {
		if (grafo != null) {
			this.createGraph();
		}	
	}
	
	/**
	 * Strongest Connected Component
	 * @return
	 */
	public Set <Airport> getBiggestSCC(){
		ConnectivityInspector <Airport, DefaultWeightedEdge> ci = new ConnectivityInspector <> (grafo);
		System.out.println(ci.connectedSets().size());
		Set <Airport> bestSet = null;
		int bestSize = 0;
		
		for (Set <Airport> s : ci.connectedSets()) {
			if (s.size() > bestSize) {
				bestSet = new HashSet <> (s);
				bestSize = s.size();
				
			}
		}
		return bestSet;		
	}

	public List<Airport> getShortestPath(int id1, int id2) {
		
		Airport nyc = this.airportIdMap.get(id1);
		Airport bgy = this.airportIdMap.get(id2);
		
		System.out.println(nyc);
		System.out.println(bgy);
		
		if (nyc == null || bgy == null) {
			throw new RuntimeException ("Gli aeroporti selezionati non sono present nel grafo!");
		}
		
		ShortestPathAlgorithm <Airport, DefaultWeightedEdge> spa = new DijkstraShortestPath <> (grafo);	
		
		double weight = spa.getPathWeight(nyc, bgy);
		System.out.println(weight);
		
		GraphPath <Airport, DefaultWeightedEdge> gp = spa.getPath(nyc, bgy);
	
		return gp.getVertexList();
	}
	
}
