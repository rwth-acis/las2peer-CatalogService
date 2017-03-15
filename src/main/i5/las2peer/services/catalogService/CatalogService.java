package i5.las2peer.services.catalogService;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Level;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import i5.las2peer.api.Context;
import i5.las2peer.api.exceptions.ArtifactNotFoundException;
import i5.las2peer.api.exceptions.StorageException;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.p2p.AgentNotKnownException;
import i5.las2peer.persistency.Envelope;
import i5.las2peer.restMapper.RESTService;
import i5.las2peer.restMapper.annotations.ServicePath;
import i5.las2peer.security.L2pSecurityException;
import i5.las2peer.tools.CryptoException;
import i5.las2peer.tools.SerializationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;

/**
 * las2peer Catalog Service
 * 
 * This is an example service to list services. It can be used as showcase or to collect known services.
 * 
 */
@ServicePath("/catalogservice")
public class CatalogService extends RESTService {

	public static final String API_VERSION = "1.0";

	// instantiate the logger class
	private static final L2pLogger logger = L2pLogger.getInstance(CatalogService.class.getName());

	private static final String SERVICE_CATALOG_ENVELOPE_NAME = "service-catalog";
	private static final String RESOURCE_SERVICES_BASENAME = "/services";

	@Override
	protected void initResources() {
		getResourceConfig().register(ResourceServices.class);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// RMI service methods
	////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * This method is designed to be used with RMI calls to this service. It uses only default types and classes.
	 * 
	 * @param name
	 * @param version
	 * @param github
	 * @param frontend
	 * @param description
	 * @throws SerializationException
	 * @throws L2pSecurityException
	 * @throws CryptoException
	 * @throws StorageException
	 * @throws AgentNotKnownException
	 */
	public void createOrUpdateServiceEntry(String name, String version, String github, String frontend,
			String description) throws AgentNotKnownException, StorageException, CryptoException, L2pSecurityException,
			SerializationException {
		CatalogServiceEntry entry = new CatalogServiceEntry(name, version, github, frontend, description);
		updateServiceCatalogReal(entry);
	}

	/**
	 * This method is designed to be used with RMI calls to this service. It uses only default types and classes.
	 * 
	 * @return Returns the catalog as Map or {@code null} on failure.
	 * @throws SerializationException
	 * @throws L2pSecurityException
	 * @throws CryptoException
	 * @throws StorageException
	 * @throws AgentNotKnownException
	 */
	public Map<String, Map<String, Object>> fetchServiceCatalog() throws AgentNotKnownException, StorageException,
			CryptoException, L2pSecurityException, SerializationException {
		return fetchServiceCatalogReal().toMap();
	}

	////////////////////////////////////////////////////////////////////////////////////////
	// REST-API service methods
	////////////////////////////////////////////////////////////////////////////////////////

	@Api(
			value = RESOURCE_SERVICES_BASENAME)
	@SwaggerDefinition(
			info = @Info(
					title = "las2peer Catalog Service",
					version = API_VERSION,
					description = "A las2peer catalog service as showcase for other services.",
					contact = @Contact(
							name = "ACIS Group",
							url = "https://las2peer.org/",
							email = "las2peer@dbis.rwth-aachen.de"),
					license = @License(
							name = "ACIS License (BSD3)",
							url = "https://github.com/rwth-acis/las2peer-CatalogService/blob/master/LICENSE")))
	@Path(RESOURCE_SERVICES_BASENAME)
	public static class ResourceServices {

		/**
		 * This web API method gets the full catalog as JSON formatted String.
		 * 
		 * @return Returns the complete catalog as JSON formatted String.
		 */
		@GET
		@Path("/")
		@Produces(MediaType.APPLICATION_JSON)
		public Response getServiceCatalog() {
			try {
				CatalogService service = (CatalogService) Context.getCurrent().getService();
				ServiceCatalog catalog = service.fetchServiceCatalogReal();
				return Response.ok(catalog.toJSONString(), MediaType.APPLICATION_JSON).build();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not read service catalog!", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Could not read service catalog! See log for details.").build();
			}
		}

		/**
		 * This web API method removes an entry from the catalog.
		 * 
		 * @param serviceName The service name that should be deleted.
		 * @return Returns an HTTP status code and message with the result of the request.
		 */
		@DELETE
		@Path("/{serviceName}")
		@Consumes(MediaType.TEXT_PLAIN)
		@Produces(MediaType.TEXT_PLAIN)
		public Response deleteServiceEntry(@PathParam("serviceName") String serviceName) {
			try {
				CatalogService service = (CatalogService) Context.getCurrent().getService();
				Envelope envelope = Context.getCurrent().fetchEnvelope(SERVICE_CATALOG_ENVELOPE_NAME);
				Object content = envelope.getContent(service.getAgent());
				if (content instanceof ServiceCatalog) {
					ServiceCatalog catalog = (ServiceCatalog) content;
					catalog.removeServiceEntry(serviceName);
					Envelope updated = Context.getCurrent().createEnvelope(envelope, catalog);
					Context.getCurrent().storeEnvelope(updated, service.getAgent());
					return Response.ok("Catalog updated.", MediaType.TEXT_PLAIN).build();
				} else {
					throw new SerializationException("This is not an " + ServiceCatalog.class.getCanonicalName()
							+ ", but an " + content.getClass().getCanonicalName());
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not update service catalog!", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Could not update service catalog! See log for details.").build();
			}
		}

		/**
		 * This method creates or updates an entry in the catalog.
		 * 
		 * @return Returns an HTTP status code and message with the result of the request.
		 */
		@POST
		@Path("/{serviceName}")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.TEXT_PLAIN)
		public Response postServiceEntry(@PathParam("serviceName") String serviceName, String contentJsonString) {
			try {
				CatalogService service = (CatalogService) Context.getCurrent().getService();
				CatalogServiceEntry entry = CatalogServiceEntry.createFromJsonString(contentJsonString);
				service.updateServiceCatalogReal(entry);
				return Response.ok("Catalog updated.", MediaType.TEXT_PLAIN).build();
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Could not update service catalog!", e);
				return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("Could not update service catalog! See log for details.").build();
			}
		}

	}

	////////////////////////////////////////////////////////////////////////////////////////
	// real service methods
	////////////////////////////////////////////////////////////////////////////////////////

	private void updateServiceCatalogReal(CatalogServiceEntry entry) throws StorageException, AgentNotKnownException,
			CryptoException, L2pSecurityException, SerializationException {
		Envelope toStore;
		try {
			Envelope envelope = getContext().fetchEnvelope(SERVICE_CATALOG_ENVELOPE_NAME);
			Serializable content = envelope.getContent(getAgent());
			if (content instanceof ServiceCatalog) {
				((ServiceCatalog) content).addServiceEntry(entry);
				toStore = getContext().createEnvelope(envelope, content);
			} else {
				throw new SerializationException("This is not an " + ServiceCatalog.class.getCanonicalName()
						+ ", but an " + content.getClass().getCanonicalName());
			}
		} catch (ArtifactNotFoundException e) {
			ServiceCatalog catalog = new ServiceCatalog();
			catalog.addServiceEntry(entry);
			toStore = getContext().createEnvelope(SERVICE_CATALOG_ENVELOPE_NAME, catalog, getAgent());
		}
		getContext().storeEnvelope(toStore, getAgent());
	}

	private ServiceCatalog fetchServiceCatalogReal() throws StorageException, AgentNotKnownException, CryptoException,
			L2pSecurityException, SerializationException {
		try {
			Envelope envelope = getContext().fetchEnvelope(SERVICE_CATALOG_ENVELOPE_NAME);
			Object content = envelope.getContent(getAgent());
			if (content instanceof ServiceCatalog) {
				return (ServiceCatalog) content;
			} else {
				throw new SerializationException("This is not an " + ServiceCatalog.class.getCanonicalName()
						+ ", but an " + content.getClass().getCanonicalName());
			}
		} catch (ArtifactNotFoundException e) {
			return new ServiceCatalog();
		}
	}

}
