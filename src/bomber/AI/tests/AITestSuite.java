package bomber.AI.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	AStarRouteFinderTest.class,
	EscapeRouteFinderTest .class,
	EnemyCheckTest.class,
	InDangerTest.class,
	MoveSafetyTest.class,
	NereastNeighbourTest.class,
	PlanningTest.class
})


public class AITestSuite {
	
}
