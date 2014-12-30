//TODO Fix certificate of Elimination method
  /*
   * Start by checking to see if trivially eliminated, if so cert = { top team }
   * add teams by wins plus (possibly) remaining games between teams 
   */
//TODO Remove incrementer modifications (i++, j++)


import java.util.Iterator;
import java.util.NoSuchElementException;


public class BaseballElimination {
	private LinkedQueue<String> teams;
	private int[][] wLR;
	private int[][] games;
	
	// create a baseball division from given filename in format specified below
	public BaseballElimination(String filename) {
		In in = new In(filename);
		int n = in.readInt();
		games = new int[n][n];
		wLR = new int[n][3];
		teams = new LinkedQueue<String>();
		
		for (int i = 0; i < n; i++) {
			teams.enqueue(in.readString());
			wLR[i][0] = in.readInt();
			wLR[i][1] = in.readInt();
			wLR[i][2] = in.readInt();
			for (int j = 0; j < games.length; j++) {
				games[i][j] = in.readInt();
			}
		}
	}
	
	
	// number of teams
	public int numberOfTeams(){
		return teams.size();
	}
	
	
	// all teams
	public Iterable<String> teams() {
		return teams;
	}
	
	
	// number of wins for given team
	public int wins(String team) {
		int counter = findTeam(team);
		return wLR[counter][0];
	}
	
	
	// number of losses for given team
	public int losses(String team) {
		int counter = findTeam(team);
		return wLR[counter][1];
	}
	
	
	// number of remaining games for given team
	public int remaining(String team) {
		int counter = findTeam(team);
		return wLR[counter][2];
	}
	
	
	// number of remaining games between team1 and team2
	public int against(String team1, String team2) {
		int team1Counter = findTeam(team1);
		int team2Counter = findTeam(team2);
		
		return games[team1Counter][team2Counter];
	}
	
	
	// is given team eliminated?
	public boolean isEliminated(String team) {
		
		//trivially eliminated?
		int thisTeam = findTeam(team);
		int thisTeamPotential = wLR[thisTeam][0] + wLR[thisTeam][2];
		for (int i = 0; i < wLR.length; i++) {
			if (wLR[i][0] > thisTeamPotential)
				return true;
		}
		
		//non-trivially?
		FlowNetwork net = buildNetwork(team);
		FordFulkerson ff = new FordFulkerson(net, 0, net.V() - 1);
		
		Iterator<FlowEdge> iterator = net.adj(0).iterator();
		
		while (iterator.hasNext()) {
			FlowEdge edge = iterator.next();
			if (edge.flow() < edge.capacity())
				return true;
		}
		
		return false;
	}
	
	// subset R of teams that eliminates given team; null if not eliminated
	public Iterable<String> certificateOfElimination(String team) {
		
		if (!isEliminated(team)) return null;
		
		int thisTeam = findTeam(team);
		
		return teams();
	}


	private FlowNetwork buildNetwork(String team) {
		int thisTeam = findTeam(team);
		
		int otherTeams = numberOfTeams() - 1;
		int otherGames = 0;
		for (int i = 0; i < otherTeams - 1; i++) {
			otherGames += otherTeams - 1 - i;
		}
		FlowNetwork net = new FlowNetwork(otherGames + otherTeams + 3);
		
		Queue<Integer> capacities = new Queue<Integer>();
		Queue<Integer> matches = new Queue<Integer>();
		
		for (int i = 0; i < games.length; i++) {
			if (i == thisTeam) i++;
			if (i > games.length) break;
			for (int j = i + 1; j < games[0].length; j++) {
				if (j == thisTeam) j++;
				if (j >= games[i].length) break;
				capacities.enqueue(games[i][j]);
				matches.enqueue(i+1);
				matches.enqueue(j+1);
			}
		}
		
		for (int i = 1; i <= otherGames; i++) {
			FlowEdge edge1 = new FlowEdge(0, i, capacities.dequeue());
			FlowEdge edge2 = new FlowEdge(i, otherGames + matches.dequeue(), Double.POSITIVE_INFINITY);
			FlowEdge edge3 = new FlowEdge(i, otherGames + matches.dequeue(), Double.POSITIVE_INFINITY);
			net.addEdge(edge1); net.addEdge(edge2); net.addEdge(edge3);
		}
		
		int e = otherGames + otherTeams + 2; 
		for (int i = 0; i < otherTeams + 1; i++) {
			if (i == thisTeam) i++;
			if (i >= otherTeams + 1) break;
			int t = otherGames + i + 1;
			int capacity = wLR[thisTeam][0] + wLR[thisTeam][2] - wLR[i][0];
			FlowEdge edge = new FlowEdge(t, e, capacity); 
			net.addEdge(edge);
		}
		
		return net;
	}
	
	
	private int findTeam(String team) {
		Iterator<String> iterator = this.teams().iterator();
		
		int counter = 0;
		while (iterator.hasNext()) {
			if (iterator.next().equals(team))
				break;
			else counter++;
		}
		
		if (counter == teams.size()) 
			throw new IllegalArgumentException("Team " + team + " is not in this input");
		
		return counter;
	}


	public static void main(String[] args) {
		BaseballElimination bbe = new BaseballElimination(args[0]);

		System.out.println(bbe.certificateOfElimination("Detroit"));
	}
}
