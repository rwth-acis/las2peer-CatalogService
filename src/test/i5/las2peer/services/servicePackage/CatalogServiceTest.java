package i5.las2peer.services.servicePackage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import i5.las2peer.p2p.PastryNodeImpl;
import i5.las2peer.p2p.ServiceNameVersion;
import i5.las2peer.security.ServiceAgent;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.catalogService.CatalogService;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.testing.TestSuite;

public class CatalogServiceTest {

	private List<PastryNodeImpl> nodes;

	@Before
	public void startNetwork() throws Exception {
		System.out.println("starting network...");
		nodes = TestSuite.launchNetwork(3);
	}

	@After
	public void stopNetwork() {
		for (PastryNodeImpl node : nodes) {
			node.shutDown();
		}
	}

	@Test
	public void testStoreAndFetch() {
		try {
			ServiceNameVersion nameVersion = new ServiceNameVersion(CatalogService.class.getCanonicalName(), "1.0");
			String testEntryServiceName = "TestService";
			String testEntryServiceVersion = "0.5";
			String testEntryServiceGithub = "http://github.com/";
			String testEntryServiceFrontend = "http://google.com/";
			String testEntryServiceDescription = "This is a very good service!";

			PastryNodeImpl firstNode = nodes.get(0);
			// start service on first node
			ServiceAgent serviceAgent = ServiceAgent.createServiceAgent(nameVersion, "testtest");
			System.out.println("Generated test service agent has id " + serviceAgent.getId());
			serviceAgent.unlockPrivateKey("testtest");
			firstNode.storeAgent(serviceAgent);
			firstNode.registerReceiver(serviceAgent);

			// create service entry using second node
			PastryNodeImpl secondNode = nodes.get(1);
			UserAgent adam = MockAgentFactory.getAdam();
			adam.unlockPrivateKey("adamspass");
			secondNode.storeAgent(adam);
			secondNode.registerReceiver(adam);
			secondNode.invoke(adam, nameVersion, "createOrUpdateServiceEntry",
					new Serializable[] { testEntryServiceName, testEntryServiceVersion, testEntryServiceGithub,
							testEntryServiceFrontend, testEntryServiceDescription });

			// fetch service catalog using third node
			PastryNodeImpl thirdNode = nodes.get(2);
			UserAgent eve = MockAgentFactory.getEve();
			eve.unlockPrivateKey("evespass");
			thirdNode.storeAgent(eve);
			thirdNode.registerReceiver(eve);
			Serializable result2 = thirdNode.invoke(eve, nameVersion, "fetchServiceCatalog", new Serializable[] {});
			// verify result
			Assert.assertThat(result2, CoreMatchers.instanceOf(HashMap.class));
			HashMap<?, ?> map = (HashMap<?, ?>) result2;
			Assert.assertEquals(1, map.size());
			Object firstEntry = map.values().iterator().next();
			Assert.assertThat(firstEntry, CoreMatchers.instanceOf(HashMap.class));
			HashMap<?, ?> firstEntryMap = (HashMap<?, ?>) firstEntry;
			Assert.assertEquals(testEntryServiceName, firstEntryMap.get("name"));
			Assert.assertEquals(testEntryServiceVersion, firstEntryMap.get("version"));
			Assert.assertEquals(testEntryServiceGithub, firstEntryMap.get("github"));
			Assert.assertEquals(testEntryServiceFrontend, firstEntryMap.get("frontend"));
			Assert.assertEquals(testEntryServiceDescription, firstEntryMap.get("description"));
		} catch (Exception e) {
			e.printStackTrace();
			Assert.fail(e.toString());
		}
	}

}
