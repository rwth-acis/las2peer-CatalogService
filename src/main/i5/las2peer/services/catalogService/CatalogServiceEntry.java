package i5.las2peer.services.catalogService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class CatalogServiceEntry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name = "";
	private String version = "";
	private String github = "";
	private String frontend = "";
	private String description = "";

	public CatalogServiceEntry(String name, String version, String github, String frontend, String description) {
		if (name == null) {
			throw new IllegalArgumentException("Name must not be null");
		}
		name = name.trim();
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		this.name = name;
		if (version != null) {
			this.version = version.trim();
		}
		if (github != null) {
			this.github = github.trim();
		}
		if (frontend != null) {
			this.frontend = frontend.trim();
		}
		if (description != null) {
			this.description = description.trim();
		}
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public String getGithub() {
		return github;
	}

	public String getFrontend() {
		return frontend;
	}

	public String getDescription() {
		return description;
	}

	public Map<String, Object> toMap() {
		HashMap<String, Object> result = new HashMap<>();
		result.put("name", name);
		result.put("version", version);
		result.put("github", github);
		result.put("frontend", frontend);
		result.put("description", description);
		return result;
	}

	public Object toJsonObject() {
		JSONObject result = new JSONObject();
		result.putAll(toMap());
		return result;
	}

	public static CatalogServiceEntry createFromJsonString(String jsonString) throws ParseException {
		JSONParser parser = new JSONParser(JSONParser.DEFAULT_PERMISSIVE_MODE);
		JSONObject params = (JSONObject) parser.parse(jsonString);
		String name = (String) params.get("name");
		String version = (String) params.get("version");
		String github = (String) params.get("github");
		String frontend = (String) params.get("frontend");
		String description = (String) params.get("description");
		return new CatalogServiceEntry(name, version, github, frontend, description);
	}

}
