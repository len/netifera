package com.netifera.platform.ui.internal;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.netifera.platform.api.log.ILogManager;
import com.netifera.platform.api.log.ILogger;
import com.netifera.platform.api.model.AbstractEntity;
import com.netifera.platform.api.model.IEntity;
import com.netifera.platform.ui.api.export.IEntityImportExportProvider;
import com.netifera.platform.ui.api.export.IEntityImportExportService;
import com.netifera.platform.util.xml.XMLElement;
import com.netifera.platform.util.xml.XMLParseException;

public class EntityImportExportService implements IEntityImportExportService {

	final private List<IEntityImportExportProvider> providers = new ArrayList<IEntityImportExportProvider>();
	
	private ILogger logger;
	
	public void exportEntities(Collection<IEntity> entities, Writer writer) throws IOException {
		Map<Long,XMLElement> map = new HashMap<Long,XMLElement>();
		List<XMLElement> roots = new ArrayList<XMLElement>();
		for (IEntity entity: entities) {
			XMLElement xml = exportEntity(entity);
			if (xml != null) {
				map.put(entity.getId(), xml);
				XMLElement realm = map.get(entity.getRealmId());
				if (realm != null)
					realm.addChild(xml);
				else
					roots.add(xml);
			}
		}
		
		for (XMLElement xml: roots)
			xml.write(writer);
	}

	private XMLElement exportEntity(IEntity entity) throws IOException {
		for (IEntityImportExportProvider provider: providers) {
			XMLElement xml = provider.exportEntity(entity);
			if (xml != null) {
				for (String name: entity.getAttributes()) {
					XMLElement attributeElement = new XMLElement();
					attributeElement.setName("attribute");
					attributeElement.setAttribute("name", name);
					attributeElement.setContent(entity.getAttribute(name));
					xml.addChild(attributeElement);
				}
				for (String tag: entity.getTags()) {
					XMLElement tagElement = new XMLElement();
					tagElement.setName("tag");
					tagElement.setContent(tag);
					xml.addChild(tagElement);
				}
				return xml;
			}
		}
		logger.warning("Unhandled entity: "+entity);
		return null;
	}

	public List<IEntity> importEntities(long realm, long space, Reader reader) throws XMLParseException, IOException {
		XMLElement xml = new XMLElement();
		xml.parseFromReader(reader);
		List<IEntity> entities = new ArrayList<IEntity>();
		for (XMLElement child: xml.getChildren()) {
			IEntity entity = importEntity(realm, space, child, entities);
			if (entity != null) {
				boolean changed = false;
				for (XMLElement child2: xml.getChildren()) {
					if (child2.getName().equals("attribute")) {
						entity.setAttribute(child2.getStringAttribute("name"), child2.getContent());
						changed = true;
					}
					if (child2.getName().equals("tag")) {
						entity.addTag(child2.getContent());
						changed = true;
					}
				}
				if (changed)
					((AbstractEntity)entity).update(); //FIXME why must i reference AbstractEntity? should expose update() in the interface IEntity ??
				entities.add(entity);
			}
		}
		return entities;
	}

	private IEntity importEntity(long realm, long space, XMLElement xml, List<IEntity> entities) {
		for (IEntityImportExportProvider provider: providers) {
			IEntity entity = provider.importEntity(realm, space, xml);
			if (entity != null) {
				if (entity.isRealmEntity()) {
					for (XMLElement child: xml.getChildren()) {
						if (!child.getName().equals("tag") && !child.getName().equals("attribute")) {
							IEntity childEntity = importEntity(entity.getId(), space, child, entities);
							if (entity != null) {
								entities.add(childEntity);
							}
						}
					}
				}
				return entity;
			}
		}
		logger.warning("Unhandled XML node: "+xml);
		return null;
	}

	protected void registerProvider(IEntityImportExportProvider provider) {
		providers.add(provider);
	}

	protected void unregisterProvider(IEntityImportExportProvider provider) {
		providers.remove(provider);
	}

	protected void setLogManager(ILogManager logManager) {
		this.logger = logManager.getLogger("Import/Export Service");
		logger.enableDebug();
	}
	
	protected void unsetLogManager(ILogManager logManager) {
	}
}