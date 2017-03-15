package i5.las2peer.services.catalogService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minidev.json.JSONArray;

public class ServiceCatalog implements Serializable {

	private static final long serialVersionUID = 1L;

	private final HashMap<String, CatalogServiceEntry> catalog = new HashMap<>();

	public void addServiceEntry(CatalogServiceEntry entry) {
		catalog.put(entry.getName(), entry);
	}

	public CatalogServiceEntry getServiceEntry(String serviceName) {
		return catalog.get(serviceName);
	}

	public void removeServiceEntry(String serviceName) {
		catalog.remove(serviceName);
	}

	public Map<String, Map<String, Object>> toMap() {
		HashMap<String, Map<String, Object>> result = new HashMap<>();
		for (Entry<String, CatalogServiceEntry> entry : catalog.entrySet()) {
			result.put(entry.getKey(), entry.getValue().toMap());
		}
		return result;
	}

	public String toJSONString() {
		// transform catalog into JSON
		JSONArray catalogJson = new JSONArray();
		for (CatalogServiceEntry entry : catalog.values()) {
			catalogJson.add(entry.toJsonObject());
		}
		return catalogJson.toJSONString();
	}

}
