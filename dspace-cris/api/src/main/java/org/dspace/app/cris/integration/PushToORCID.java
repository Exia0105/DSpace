/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * https://github.com/CILEA/dspace-cris/wiki/License
 */
package org.dspace.app.cris.integration;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dspace.app.cris.configuration.RelationPreferenceConfiguration;
import org.dspace.app.cris.model.CrisConstants;
import org.dspace.app.cris.model.Project;
import org.dspace.app.cris.model.RelationPreference;
import org.dspace.app.cris.model.ResearcherPage;
import org.dspace.app.cris.model.jdyna.RPPropertiesDefinition;
import org.dspace.app.cris.model.jdyna.RPProperty;
import org.dspace.app.cris.service.ApplicationService;
import org.dspace.app.cris.service.RelationPreferenceService;
import org.dspace.app.cris.util.ResearcherPageUtils;
import org.dspace.app.util.OrcidFundingMetadata;
import org.dspace.app.util.OrcidWorkMetadata;
import org.dspace.authority.orcid.OrcidService;
import org.dspace.authority.orcid.jaxb.Address;
import org.dspace.authority.orcid.jaxb.Amount;
import org.dspace.authority.orcid.jaxb.Citation;
import org.dspace.authority.orcid.jaxb.CitationType;
import org.dspace.authority.orcid.jaxb.ContactDetails;
import org.dspace.authority.orcid.jaxb.Contributor;
import org.dspace.authority.orcid.jaxb.ContributorAttributes;
import org.dspace.authority.orcid.jaxb.ContributorEmail;
import org.dspace.authority.orcid.jaxb.Country;
import org.dspace.authority.orcid.jaxb.CreditName;
import org.dspace.authority.orcid.jaxb.CurrencyCode;
import org.dspace.authority.orcid.jaxb.Day;
import org.dspace.authority.orcid.jaxb.Email;
import org.dspace.authority.orcid.jaxb.ExternalIdCommonName;
import org.dspace.authority.orcid.jaxb.ExternalIdReference;
import org.dspace.authority.orcid.jaxb.ExternalIdUrl;
import org.dspace.authority.orcid.jaxb.ExternalIdentifier;
import org.dspace.authority.orcid.jaxb.ExternalIdentifiers;
import org.dspace.authority.orcid.jaxb.Funding;
import org.dspace.authority.orcid.jaxb.FundingContributor;
import org.dspace.authority.orcid.jaxb.FundingContributorAttributes;
import org.dspace.authority.orcid.jaxb.FundingContributors;
import org.dspace.authority.orcid.jaxb.FundingExternalIdentifier;
import org.dspace.authority.orcid.jaxb.FundingExternalIdentifiers;
import org.dspace.authority.orcid.jaxb.FundingList;
import org.dspace.authority.orcid.jaxb.FundingTitle;
import org.dspace.authority.orcid.jaxb.FuzzyDate;
import org.dspace.authority.orcid.jaxb.JournalTitle;
import org.dspace.authority.orcid.jaxb.Keyword;
import org.dspace.authority.orcid.jaxb.Keywords;
import org.dspace.authority.orcid.jaxb.LanguageCode;
import org.dspace.authority.orcid.jaxb.Month;
import org.dspace.authority.orcid.jaxb.OrcidBio;
import org.dspace.authority.orcid.jaxb.OrcidId;
import org.dspace.authority.orcid.jaxb.OrcidProfile;
import org.dspace.authority.orcid.jaxb.OrcidWork;
import org.dspace.authority.orcid.jaxb.OrcidWorks;
import org.dspace.authority.orcid.jaxb.OtherNames;
import org.dspace.authority.orcid.jaxb.PersonalDetails;
import org.dspace.authority.orcid.jaxb.PublicationDate;
import org.dspace.authority.orcid.jaxb.ResearcherUrl;
import org.dspace.authority.orcid.jaxb.ResearcherUrls;
import org.dspace.authority.orcid.jaxb.Subtitle;
import org.dspace.authority.orcid.jaxb.TranslatedTitle;
import org.dspace.authority.orcid.jaxb.Url;
import org.dspace.authority.orcid.jaxb.WorkContributors;
import org.dspace.authority.orcid.jaxb.WorkExternalIdentifier;
import org.dspace.authority.orcid.jaxb.WorkExternalIdentifiers;
import org.dspace.authority.orcid.jaxb.WorkTitle;
import org.dspace.authority.orcid.jaxb.Year;
import org.dspace.content.DCPersonName;
import org.dspace.content.Item;
import org.dspace.core.ConfigurationManager;
import org.dspace.core.Constants;
import org.dspace.core.Context;
import org.dspace.discovery.SearchService;
import org.dspace.discovery.SearchServiceException;

public class PushToORCID {

	private static final int ORCID_PUBLICATION_PREFS_DISABLED = 0;
	private static final int ORCID_PUBLICATION_PREFS_ALL = 1;
	private static final int ORCID_PUBLICATION_PREFS_SELECTED = 2;

	private static final int ORCID_PROJECTS_PREFS_DISABLED = 0;
	private static final int ORCID_PROJECTS_PREFS_ALL = 1;
	private static final int ORCID_PROJECTS_PREFS_SELECTED = 2;

	/** the logger */
	private static Logger log = Logger.getLogger(PushToORCID.class);

	public static void prepareAndSend(List<ResearcherPage> rps, RelationPreferenceService relationPreferenceService,
			SearchService searchService, ApplicationService applicationService) {
		log.debug("Working... push to ORCID");

		Map<String, Map<String, List<String>>> mapResearcherMetadataToSend = new HashMap<String, Map<String, List<String>>>();
		Map<String, List<Integer>> mapPublicationsToSend = new HashMap<String, List<Integer>>();
		Map<String, List<Integer>> mapProjectsToSend = new HashMap<String, List<Integer>>();

		boolean buildORCIDProfile = ConfigurationManager.getBooleanProperty("cris",
				"system.script.pushtoorcid.profile.create.new", false);

		Map<String, String> mapResearcherOrcid = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenUpdateBio = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenCreateWorks = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenUpdateWorks = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenCreateFundings = new HashMap<String, String>();
		Map<String, String> mapResearcherTokenUpdateFundings = new HashMap<String, String>();
		List<String> listNewResearcherToPushOnOrcid = new ArrayList<String>();

		// prepare configuration mapping ORCID->DSPACE-CRIS
		Map<String, String> orcidConfigurationMapping = new HashMap<String, String>();
		List<RPPropertiesDefinition> metadataDefinitions = applicationService
				.likePropertiesDefinitionsByShortName(RPPropertiesDefinition.class, "orcid-profile-pref");
		for (RPPropertiesDefinition rppd : metadataDefinitions) {
			String metadataShortnameINTERNAL = rppd.getShortName().replaceFirst("orcid-profile-pref-", "");
			String metadataShortnameORCID = rppd.getLabel();
			orcidConfigurationMapping.put(metadataShortnameORCID, metadataShortnameINTERNAL);
		}

		for (ResearcherPage researcher : rps) {

			String crisID = researcher.getCrisID();
			List<Integer> itemIDsToSend = new ArrayList<Integer>();
			List<Integer> projectsIDsToSend = new ArrayList<Integer>();

			boolean prepareUpdateProfile = false;
			boolean prepareCreateWork = false;
			boolean prepareUpdateWork = false;
			boolean prepareCreateFunding = false;
			boolean prepareUpdateFunding = false;

			boolean foundORCID = false;

			for (RPProperty orcid : researcher.getAnagrafica4view().get("orcid")) {
				mapResearcherOrcid.put(crisID, orcid.toString());
				foundORCID = true;
				break;
			}

			if (buildORCIDProfile && !foundORCID) {
				listNewResearcherToPushOnOrcid.add(crisID);
				// default pushing all profile field founded on configuration
				prepareUpdateProfile(applicationService, mapResearcherMetadataToSend, researcher, true);
				// default pushing selected works
				prepareWorks(relationPreferenceService, searchService, mapPublicationsToSend, researcher, crisID,
						itemIDsToSend, "" + ORCID_PUBLICATION_PREFS_SELECTED);
				// default pushing selected fundings
				prepareFundings(relationPreferenceService, searchService, mapProjectsToSend, researcher, crisID,
						projectsIDsToSend, "" + ORCID_PROJECTS_PREFS_SELECTED);
			} else {
				for (RPProperty tokenRP : researcher.getAnagrafica4view().get("system-orcid-token-orcid-bio-update")) {
					mapResearcherTokenUpdateBio.put(crisID, tokenRP.toString());
					prepareUpdateProfile = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view()
						.get("system-orcid-token-orcid-works-create")) {
					mapResearcherTokenCreateWorks.put(crisID, tokenRP.toString());
					prepareCreateWork = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view()
						.get("system-orcid-token-orcid-works-update")) {
					mapResearcherTokenUpdateWorks.put(crisID, tokenRP.toString());
					prepareUpdateWork = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view().get("system-orcid-token-funding-create")) {
					mapResearcherTokenCreateFundings.put(crisID, tokenRP.toString());
					prepareCreateFunding = true;
					break;
				}
				for (RPProperty tokenRP : researcher.getAnagrafica4view().get("system-orcid-token-funding-update")) {
					mapResearcherTokenUpdateFundings.put(crisID, tokenRP.toString());
					prepareUpdateFunding = true;
					break;
				}

				if (prepareUpdateProfile) {
					prepareUpdateProfile(applicationService, mapResearcherMetadataToSend, researcher, false);
				}

				if (prepareCreateWork && prepareUpdateWork) {
					String publicationsPrefs = ResearcherPageUtils.getStringValue(researcher,
							"orcid-publications-prefs");
					prepareWorks(relationPreferenceService, searchService, mapPublicationsToSend, researcher, crisID,
							itemIDsToSend, publicationsPrefs);
				}

				if (prepareCreateFunding && prepareUpdateFunding) {
					String projectsPrefs = ResearcherPageUtils.getStringValue(researcher, "orcid-projects-prefs");
					prepareFundings(relationPreferenceService, searchService, mapProjectsToSend, researcher, crisID,
							projectsIDsToSend, projectsPrefs);
				}
			}
		}

		log.debug("Create DSpace context and use browse indexing");
		Context context = null;
		try {
			context = new Context();
			context.turnOffAuthorisationSystem();

			OrcidService orcidService = OrcidService.getOrcid();

			if (buildORCIDProfile) {
				log.info("Starts push new ORCID Profile");
				for (String crisId : listNewResearcherToPushOnOrcid) {
					log.info("Prepare push for ResearcherPage crisID:" + crisId);
					try {
						OrcidProfile profile = buildOrcidProfile(applicationService, crisId,
								mapResearcherMetadataToSend, orcidConfigurationMapping);
						OrcidWorks works = buildOrcidWorks(context, crisId, mapPublicationsToSend);
						FundingList fundings = buildOrcidFundings(context, applicationService, crisId,
								mapProjectsToSend);

						profile.getOrcidActivities().setOrcidWorks(works);
						profile.getOrcidActivities().setFundingList(fundings);

						String orcid = orcidService.buildProfile(profile);
						ResearcherPage rp = applicationService.getEntityByCrisId(crisId, ResearcherPage.class);
						ResearcherPageUtils.buildTextValue(rp, orcid, "orcid");
						applicationService.saveOrUpdate(ResearcherPage.class, rp);
						log.info("OK!!! pushed ResearcherPage crisID:" + crisId + "; assign orcid iD:" + orcid);
					} catch (Exception e) {
						log.info("ERROR!!! ResearcherPage crisID:" + crisId);
						log.error(e.getMessage());
					}
				}
				log.info("Ends push new ORCID Profile");
			}

			log.info("Starts update ORCID Profile");
			for (String crisId : mapResearcherOrcid.keySet()) {

				String orcid = mapResearcherOrcid.get(crisId);

				if (StringUtils.isNotBlank(orcid)) {
					log.info("Prepare push for ResearcherPage crisID:" + crisId + " AND orcid iD:" + orcid);
					try {
						String tokenUpdateBio = mapResearcherTokenUpdateBio.get(crisId);
						if (StringUtils.isNotBlank(tokenUpdateBio)) {
							log.info("(Q1)Prepare OrcidProfile for ResearcherPage crisID:" + crisId);
							OrcidProfile profile = buildOrcidProfile(applicationService, crisId,
									mapResearcherMetadataToSend, orcidConfigurationMapping);
							if (profile != null) {
								orcidService.updateBio(orcid, tokenUpdateBio, profile);
								log.info("(A1) OrcidProfile for ResearcherPage crisID:" + crisId);
							}
						}

						String tokenCreateWorks = mapResearcherTokenCreateWorks.get(crisId);
						if (StringUtils.isNotBlank(tokenCreateWorks)) {
							log.info("(Q2)Prepare OrcidWorks for ResearcherPage crisID:" + crisId);
							OrcidWorks works = buildOrcidWorks(context, crisId, mapPublicationsToSend);
							if (works != null) {
								orcidService.appendWorks(orcid, tokenCreateWorks, works);
								log.info("(A2) OrcidWorks for ResearcherPage crisID:" + crisId);
							}
						}

						String tokenCreateFundings = mapResearcherTokenCreateFundings.get(crisId);
						if (StringUtils.isNotBlank(tokenCreateFundings)) {
							log.info("(Q3)Prepare FundingList for ResearcherPage crisID:" + crisId);
							FundingList fundings = buildOrcidFundings(context, applicationService, crisId,
									mapProjectsToSend);
							if (fundings != null) {
								orcidService.appendFundings(orcid, tokenCreateWorks, fundings);
								log.info("(A3) FundingList for ResearcherPage crisID:" + crisId);
							}
						}
					} catch (Exception ex) {
						log.info("ERROR!!! ResearcherPage crisID:" + crisId);
						log.error(ex.getMessage());
					}
				}
			}
			log.info("Ends update ORCID Profile");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage(), e);
		} finally {
			if (context != null && context.isValid()) {
				context.abort();
			}
		}

	}

	private static void prepareFundings(RelationPreferenceService relationPreferenceService,
			SearchService searchService, Map<String, List<Integer>> mapProjectsToSend, ResearcherPage researcher,
			String crisID, List<Integer> projectsIDsToSend, String projectsPrefs) {
		if (StringUtils.isNotBlank(projectsPrefs)) {
			if (Integer.parseInt(projectsPrefs) != ORCID_PROJECTS_PREFS_DISABLED) {
				if (Integer.parseInt(projectsPrefs) == ORCID_PROJECTS_PREFS_ALL) {
					log.info("...it will work on all researcher...");
					SolrQuery query = new SolrQuery("*:*");
					query.addFilterQuery("{!field f=search.resourcetype}" + CrisConstants.PROJECT_TYPE_ID);
					query.addFilterQuery("projectinvestigators_authority:" + crisID);
					query.setFields("search.resourceid", "search.resourcetype");
					query.setRows(Integer.MAX_VALUE);
					try {
						QueryResponse response = searchService.search(query);
						SolrDocumentList docList = response.getResults();
						Iterator<SolrDocument> solrDoc = docList.iterator();
						while (solrDoc.hasNext()) {
							SolrDocument doc = solrDoc.next();
							Integer rpId = (Integer) doc.getFirstValue("search.resourceid");
							projectsIDsToSend.add(rpId);
						}
					} catch (SearchServiceException e) {
						log.error("Error retrieving documents", e);
					}
				} else {
					if (Integer.parseInt(projectsPrefs) == ORCID_PROJECTS_PREFS_SELECTED) {
						List<RelationPreference> selected = new ArrayList<RelationPreference>();
						for (RelationPreferenceConfiguration configuration : relationPreferenceService
								.getConfigurationService().getList()) {
							if (configuration.getRelationConfiguration().getRelationClass().equals(Project.class)) {
								selected = relationPreferenceService.findRelationsPreferencesByUUIDByRelTypeAndStatus(
										researcher.getUuid(),
										configuration.getRelationConfiguration().getRelationName(),
										RelationPreference.SELECTED);
							}
							for (RelationPreference sel : selected) {
								projectsIDsToSend.add(sel.getItemID());
							}
						}

					} else {
						log.warn(crisID + " - projects preferences NOT recognized");
					}
				}
			} else {
				log.info(crisID + " - DISABLED projects preferences");
			}
		}

		mapProjectsToSend.put(crisID, projectsIDsToSend);
	}

	private static void prepareWorks(RelationPreferenceService relationPreferenceService, SearchService searchService,
			Map<String, List<Integer>> mapPublicationsToSend, ResearcherPage researcher, String crisID,
			List<Integer> itemIDsToSend, String publicationsPrefs) {
		if (publicationsPrefs != null) {
			if (Integer.parseInt(publicationsPrefs) != ORCID_PUBLICATION_PREFS_DISABLED) {
				if (Integer.parseInt(publicationsPrefs) == ORCID_PUBLICATION_PREFS_ALL) {
					log.info("...it will work on all researcher...");
					SolrQuery query = new SolrQuery("*:*");
					query.addFilterQuery("{!field f=search.resourcetype}" + Constants.ITEM);
					query.addFilterQuery("author_authority:" + crisID);
					query.setFields("search.resourceid", "search.resourcetype");
					query.setRows(Integer.MAX_VALUE);
					try {
						QueryResponse response = searchService.search(query);
						SolrDocumentList docList = response.getResults();
						Iterator<SolrDocument> solrDoc = docList.iterator();
						while (solrDoc.hasNext()) {
							SolrDocument doc = solrDoc.next();
							Integer rpId = (Integer) doc.getFirstValue("search.resourceid");
							itemIDsToSend.add(rpId);
						}
					} catch (SearchServiceException e) {
						log.error("Error retrieving documents", e);
					}
				} else {
					if (Integer.parseInt(publicationsPrefs) == ORCID_PUBLICATION_PREFS_SELECTED) {
						List<RelationPreference> selected = new ArrayList<RelationPreference>();
						for (RelationPreferenceConfiguration configuration : relationPreferenceService
								.getConfigurationService().getList()) {
							if (configuration.getRelationConfiguration().getRelationClass().equals(Item.class)) {
								selected = relationPreferenceService.findRelationsPreferencesByUUIDByRelTypeAndStatus(
										researcher.getUuid(),
										configuration.getRelationConfiguration().getRelationName(),
										RelationPreference.SELECTED);
							}
							for (RelationPreference sel : selected) {
								itemIDsToSend.add(sel.getItemID());
							}
						}

					} else {
						log.warn(crisID + " - publications preferences NOT recognized");
					}
				}
			} else {
				log.info(crisID + " - DISABLED publications preferences");
			}
		}
		mapPublicationsToSend.put(crisID, itemIDsToSend);
	}

	private static void prepareUpdateProfile(ApplicationService applicationService,
			Map<String, Map<String, List<String>>> mapResearcherMetadataToSend, ResearcherPage researcher,
			boolean force) {
		List<RPPropertiesDefinition> metadataDefinitions = applicationService
				.likePropertiesDefinitionsByShortName(RPPropertiesDefinition.class, "orcid-profile-prefs");
		for (RPPropertiesDefinition rppd : metadataDefinitions) {
			String metadataShortnameINTERNAL = rppd.getShortName().replaceFirst("orcid-profile-pref-", "");
			String metadataShortnameORCID = rppd.getLabel();

			List<RPProperty> metadatas = researcher.getAnagrafica4view().get(metadataShortnameINTERNAL);

			Map<String, List<String>> mapMetadata = new HashMap<String, List<String>>();
			List<String> listMetadata = new ArrayList<String>();

			for (RPProperty metadata : metadatas) {
				if (force || (researcher.getProprietaDellaTipologia(rppd) != null
						&& !researcher.getProprietaDellaTipologia(rppd).isEmpty()
						&& (Boolean) researcher.getProprietaDellaTipologia(rppd).get(0).getObject())) {
					listMetadata.add(metadata.toString());
				}
			}

			if (!listMetadata.isEmpty()) {
				mapMetadata.put(metadataShortnameORCID, listMetadata);
			}

			mapResearcherMetadataToSend.put(researcher.getCrisID(), mapMetadata);

		}
	}

	private static FundingList buildOrcidFundings(Context context, ApplicationService applicationService, String crisId,
			Map<String, List<Integer>> mapProjectsToSend) throws SQLException {
		FundingList fundingList = new FundingList();
		List<Integer> listOfItems = mapProjectsToSend.get(crisId);
		for (Integer ii : listOfItems) {
			Project project = applicationService.get(Project.class, ii);
			OrcidFundingMetadata itemMetadata = new OrcidFundingMetadata(context, project);

			Funding funding = new Funding();
			if (StringUtils.isNotBlank(itemMetadata.getAmount())) {
				Amount amount = new Amount();
				CurrencyCode currencyCode = CurrencyCode.fromValue(itemMetadata.getCurrencyCode());
				amount.setValue(itemMetadata.getAmount());
				amount.setCurrencyCode(currencyCode);
				funding.setAmount(amount);
			}

			if (StringUtils.isNotBlank(itemMetadata.getStartYear())
					|| StringUtils.isNotBlank(itemMetadata.getStartMonth())
					|| StringUtils.isNotBlank(itemMetadata.getStartDay())) {
				FuzzyDate fuzzyDate = new FuzzyDate();
				if (StringUtils.isNotBlank(itemMetadata.getStartYear())) {
					Year year = new Year();
					year.setValue(itemMetadata.getStartYear());
					fuzzyDate.setYear(year);
				}
				if (StringUtils.isNotBlank(itemMetadata.getStartMonth())) {
					Month month = new Month();
					month.setValue(itemMetadata.getStartMonth());
					fuzzyDate.setMonth(month);
				}
				if (StringUtils.isNotBlank(itemMetadata.getStartDay())) {
					Day day = new Day();
					day.setValue(itemMetadata.getStartDay());
					fuzzyDate.setDay(day);
				}
				funding.setStartDate(fuzzyDate);
			}
			if (StringUtils.isNotBlank(itemMetadata.getEndYear()) || StringUtils.isNotBlank(itemMetadata.getEndMonth())
					|| StringUtils.isNotBlank(itemMetadata.getEndDay())) {
				FuzzyDate fuzzyDate = new FuzzyDate();
				if (StringUtils.isNotBlank(itemMetadata.getEndYear())) {
					Year year = new Year();
					year.setValue(itemMetadata.getEndYear());
					fuzzyDate.setYear(year);
				}
				if (StringUtils.isNotBlank(itemMetadata.getEndMonth())) {
					Month month = new Month();
					month.setValue(itemMetadata.getEndMonth());
					fuzzyDate.setMonth(month);
				}
				if (StringUtils.isNotBlank(itemMetadata.getEndDay())) {
					Day day = new Day();
					day.setValue(itemMetadata.getEndDay());
					fuzzyDate.setDay(day);
				}
				funding.setEndDate(fuzzyDate);
			}

			FundingContributors fundingContributors = new FundingContributors();
			for (String valContributor : itemMetadata.getContributorsLead()) {
				addFundingContributor(fundingContributors, valContributor, "lead");
			}

			for (String valContributor : itemMetadata.getContributorsCoLead()) {
				addFundingContributor(fundingContributors, valContributor, "colead");
			}
			funding.setFundingContributors(fundingContributors);

			FundingExternalIdentifiers fundingExternalIdentifiers = new FundingExternalIdentifiers();
			for (String valIdentifier : itemMetadata.getExternalIdentifier()) {
				FundingExternalIdentifier fundingExternalIdentifier = new FundingExternalIdentifier();
				fundingExternalIdentifier
						.setFundingExternalIdentifierType(itemMetadata.getExternalIdentifierType(valIdentifier));
				fundingExternalIdentifier.setFundingExternalIdentifierValue(valIdentifier);
				fundingExternalIdentifiers.getFundingExternalIdentifier().add(fundingExternalIdentifier);
			}
			funding.setFundingExternalIdentifiers(fundingExternalIdentifiers);

			if (StringUtils.isNotBlank(itemMetadata.getTitle())) {
				FundingTitle fundingTitle = new FundingTitle();
				fundingTitle.setTitle(itemMetadata.getTitle());
				funding.setFundingTitle(fundingTitle);
			}
			
			if(StringUtils.isNotBlank(itemMetadata.getType())) {
				funding.setFundingType(itemMetadata.getType());
			}
			
			if(StringUtils.isNotBlank(itemMetadata.getAbstract())) {
				funding.setShortDescription(itemMetadata.getAbstract());
			}
			if(StringUtils.isNotBlank(itemMetadata.getURL())) {
				Url url = new Url();
				url.setValue(itemMetadata.getURL());
				funding.setUrl(url);
			}
			fundingList.getFunding().add(funding);
		}
		return fundingList;
	}

	private static void addFundingContributor(FundingContributors fundingContributors, String valContributor,
			String type) {
		FundingContributor contributor = new FundingContributor();

		Integer id = ResearcherPageUtils.getRealPersistentIdentifier(valContributor, ResearcherPage.class);
		String name = valContributor;
		String email = "";
		String orcid = "";
		if (null != id) {
			ResearcherPage ro = ResearcherPageUtils.getCrisObject(id, ResearcherPage.class);
			name = ResearcherPageUtils.getStringValue(ro, "fullName");
			email = ResearcherPageUtils.getStringValue(ro, "email");
			orcid = ResearcherPageUtils.getStringValue(ro, "orcid");

			if (StringUtils.isNotBlank(email)) {
				ContributorEmail contributorEmail = new ContributorEmail();
				contributorEmail.setValue(email);
				contributor.setContributorEmail(contributorEmail);
			}
			if (StringUtils.isNotBlank(orcid)) {
				String domainOrcid = ConfigurationManager.getProperty("cris",
						"external.domainname.authority.service.orcid");
				OrcidId orcidID = new OrcidId();
				JAXBElement<String> jaxBOrcid = new JAXBElement<String>(new QName("uri"), String.class,
						domainOrcid + orcid);
				orcidID.getContent().add(jaxBOrcid);
				contributor.setContributorOrcid(orcidID);
			}
		}

		CreditName creditName = new CreditName();
		creditName.setValue(name);
		contributor.setCreditName(creditName);

		FundingContributorAttributes attributes = new FundingContributorAttributes();
		attributes.setFundingContributorRole(type);
		fundingContributors.getFundingContributor().add(contributor);
	}

	private static OrcidWorks buildOrcidWorks(Context context, String crisId,
			Map<String, List<Integer>> mapPublicationsToSend) throws SQLException {
		OrcidWorks orcidWorks = new OrcidWorks();
		List<Integer> listOfItems = mapPublicationsToSend.get(crisId);
		for (Integer ii : listOfItems) {
			OrcidWork orcidWork = new OrcidWork();

			Item item = Item.find(context, ii);

			OrcidWorkMetadata itemMetadata = new OrcidWorkMetadata(context, item);

			WorkTitle worktitle = new WorkTitle();
			worktitle.setTitle(itemMetadata.getTitle());

			if (StringUtils.isNotBlank(itemMetadata.getSubTitle())) {
				Subtitle subtitle = new Subtitle();
				subtitle.setContent(itemMetadata.getSubTitle());
				worktitle.setSubtitle(subtitle);
			}

			if (StringUtils.isNotBlank(itemMetadata.getTranslatedTitle())) {
				TranslatedTitle translatedTitle = new TranslatedTitle();
				translatedTitle.setValue(itemMetadata.getTranslatedTitle());
				String translatedLanguageCode = itemMetadata.getTranslatedTitleLanguage();

				try {
					LanguageCode langCode = LanguageCode.fromValue(translatedLanguageCode);
					translatedTitle.setLanguageCode(langCode);
				} catch (Exception ex) {
					translatedTitle.setLanguageCode(LanguageCode.EN);
				}
				worktitle.setTranslatedTitle(translatedTitle);
			}

			String citationVal = itemMetadata.getCitation();
			if (StringUtils.isNotBlank(citationVal)) {
				Citation citation = new Citation();
				citation.setWorkCitationType(CitationType.fromValue(itemMetadata.getCitationType()));
				citation.setCitation(citationVal);
				orcidWork.setWorkCitation(citation);
			}

			orcidWork.setWorkTitle(worktitle);

			if (StringUtils.isNotBlank(itemMetadata.getJournalTitle())) {
				JournalTitle journalTitle = new JournalTitle();
				journalTitle.setContent(itemMetadata.getJournalTitle());
				orcidWork.setJournalTitle(journalTitle);
			}

			if (StringUtils.isNotBlank(itemMetadata.getYear()) || StringUtils.isNotBlank(itemMetadata.getMonth())
					|| StringUtils.isNotBlank(itemMetadata.getDay())) {
				PublicationDate publicationDate = new PublicationDate();
				if (StringUtils.isNotBlank(itemMetadata.getYear())) {
					Year year = new Year();
					year.setValue(itemMetadata.getYear());
					publicationDate.setYear(year);
				}
				if (StringUtils.isNotBlank(itemMetadata.getMonth())) {
					Month month = new Month();
					month.setValue(itemMetadata.getMonth());
					publicationDate.setMonth(month);
				}
				if (StringUtils.isNotBlank(itemMetadata.getDay())) {
					Day day = new Day();
					day.setValue(itemMetadata.getDay());
					publicationDate.setDay(day);
				}
				orcidWork.setPublicationDate(publicationDate);
			}

			if (StringUtils.isNotBlank(itemMetadata.getURL())) {
				Url url = new Url();
				url.setValue(itemMetadata.getURL());
				orcidWork.setUrl(url);
			}

			WorkExternalIdentifiers workExternalIdentifiers = new WorkExternalIdentifiers();

			for (String valIdentifier : itemMetadata.getExternalIdentifier()) {
				WorkExternalIdentifier workExternalIdentifier = new WorkExternalIdentifier();
				workExternalIdentifier.setWorkExternalIdentifierId(valIdentifier);
				workExternalIdentifier
						.setWorkExternalIdentifierType(itemMetadata.getExternalIdentifierType(valIdentifier));
				workExternalIdentifiers.getWorkExternalIdentifier().add(workExternalIdentifier);
			}

			orcidWork.setWorkExternalIdentifiers(workExternalIdentifiers);

			// export if have an authority value
			WorkContributors workContributors = new WorkContributors();
			for (String valContributor : itemMetadata.getAuthors()) {
				Contributor contributor = new Contributor();

				Integer id = ResearcherPageUtils.getRealPersistentIdentifier(valContributor, ResearcherPage.class);
				String name = valContributor;
				String email = "";
				String orcid = "";
				if (null != id) {
					ResearcherPage ro = ResearcherPageUtils.getCrisObject(id, ResearcherPage.class);
					name = ResearcherPageUtils.getStringValue(ro, "fullName");
					email = ResearcherPageUtils.getStringValue(ro, "email");
					orcid = ResearcherPageUtils.getStringValue(ro, "orcid");

					if (StringUtils.isNotBlank(email)) {
						ContributorEmail contributorEmail = new ContributorEmail();
						contributorEmail.setValue(email);
						contributor.setContributorEmail(contributorEmail);
					}
					if (StringUtils.isNotBlank(orcid)) {
						String domainOrcid = ConfigurationManager.getProperty("cris",
								"external.domainname.authority.service.orcid");
						OrcidId orcidID = new OrcidId();
						JAXBElement<String> jaxBOrcid = new JAXBElement<String>(new QName("uri"), String.class,
								domainOrcid + orcid);
						orcidID.getContent().add(jaxBOrcid);
						contributor.setContributorOrcid(orcidID);
					}
				}

				CreditName creditName = new CreditName();
				creditName.setValue(name);
				contributor.setCreditName(creditName);

				ContributorAttributes attributes = new ContributorAttributes();
				// TODO now supported only author/additional
				attributes.setContributorRole("author");
				attributes.setContributorSequence("additional");
				contributor.setContributorAttributes(attributes);
				workContributors.getContributor().add(contributor);
			}
			orcidWork.setWorkContributors(workContributors);

			LanguageCode language = LanguageCode.fromValue(itemMetadata.getLanguage());
			orcidWork.setLanguageCode(language);
		}
		return orcidWorks;
	}

	private static OrcidProfile buildOrcidProfile(ApplicationService applicationService, String crisId,
			Map<String, Map<String, List<String>>> mapResearcherMetadataToSend,
			Map<String, String> orcidMetadataConfiguration) {

		OrcidProfile profile = new OrcidProfile();

		Map<String, List<String>> metadata = mapResearcherMetadataToSend.get(crisId);

		String givenNames = getGivenName(metadata.get("name").get(0));
		String familyName = getFamilyName(metadata.get("name").get(0));
		String creditName = metadata.get("credit-name").get(0);

		List<String> otherNames = new ArrayList<String>();
		otherNames.add(metadata.get("name").get(0));
		otherNames.addAll(metadata.get("other-names"));

		String biography = metadata.get("biography").get(0);
		String primaryEmail = metadata.get("primary-email").get(0);

		List<String> emails = metadata.get("other-emails");

		String country = metadata.get("iso-3166-country").get(0);

		List<String> keywords = metadata.get("keywords");

		OrcidBio bioJAXB = new OrcidBio();

		// start personal details
		PersonalDetails personalDetails = new PersonalDetails();
		personalDetails.setFamilyName(familyName);
		personalDetails.setGivenNames(givenNames);

		CreditName creditNameJAXB = new CreditName();
		creditNameJAXB.setValue(creditName);
		personalDetails.setCreditName(creditNameJAXB);

		OtherNames otherNamesJAXB = new OtherNames();
		for (String otherName : otherNames) {
			otherNamesJAXB.getOtherName().add(otherName);
		}
		personalDetails.setOtherNames(otherNamesJAXB);

		bioJAXB.setPersonalDetails(personalDetails);

		// start biography
		bioJAXB.getBiography().setValue(biography);

		// start researcher-urls and external-identifiers
		ResearcherUrls researcherUrls = new ResearcherUrls();
		ExternalIdentifiers externalIdentifiers = new ExternalIdentifiers();
		for (String key : metadata.keySet()) {
			if (key.startsWith("researcher-url-") || key.startsWith("external-identifier-")) {
				RPPropertiesDefinition rpPD = applicationService.findPropertiesDefinitionByShortName(
						RPPropertiesDefinition.class, orcidMetadataConfiguration.get(key));

				if (key.startsWith("researcher-url-")) {
					for (String value : metadata.get(key)) {
						ResearcherUrl researcherUrl = new ResearcherUrl();
						researcherUrl.setUrlName(rpPD.getLabel());
						Url url = new Url();
						url.setValue(value);
						researcherUrl.getUrl();
						researcherUrls.getResearcherUrl().add(researcherUrl);
					}
				}
				if (key.startsWith("external-identifier-")) {
					for (String value : metadata.get(key)) {
						ExternalIdentifier externalIdentifier = new ExternalIdentifier();
						ExternalIdCommonName commonName = new ExternalIdCommonName();
						commonName.setContent(rpPD.getLabel());
						externalIdentifier.setExternalIdCommonName(commonName);
						ExternalIdReference externalIdReference = new ExternalIdReference();
						externalIdReference.setContent(value);
						externalIdentifier.setExternalIdReference(externalIdReference);
						ExternalIdUrl externalIdUrl = new ExternalIdUrl();
						externalIdUrl.setValue(value);
						externalIdentifier.setExternalIdUrl(externalIdUrl);
						externalIdentifiers.getExternalIdentifier().add(externalIdentifier);
					}
				}
			}
		}
		bioJAXB.setResearcherUrls(researcherUrls);
		bioJAXB.setExternalIdentifiers(externalIdentifiers);

		// start contact details
		ContactDetails contactDetailsJAXB = new ContactDetails();

		List<Email> emailsJAXB = contactDetailsJAXB.getEmail();

		Email emailJAXB = new Email();
		emailJAXB.setPrimary(true);
		emailJAXB.setValue(primaryEmail);
		emailsJAXB.add(emailJAXB);

		for (String e : emails) {
			Email ee = new Email();
			emailJAXB.setValue(e);
			emailsJAXB.add(ee);
		}

		Address addressJAXB = contactDetailsJAXB.getAddress();
		Country countryJAXB = new Country();
		countryJAXB.setValue(country);
		addressJAXB.setCountry(countryJAXB);
		contactDetailsJAXB.setAddress(addressJAXB);

		bioJAXB.setContactDetails(contactDetailsJAXB);

		// start keywords
		Keywords keywordsJAXB = new Keywords();
		for (String kk : keywords) {
			Keyword k = new Keyword();
			k.setContent(kk);
			keywordsJAXB.getKeyword().add(k);
		}
		bioJAXB.setKeywords(keywordsJAXB);

		profile.setOrcidBio(bioJAXB);
		return profile;
	}

	private static String getFamilyName(String text) {
		DCPersonName tmpPersonName = new DCPersonName(text);

		if (StringUtils.isNotBlank(tmpPersonName.getLastName())) {
			return tmpPersonName.getLastName();
		}
		return "";
	}

	private static String getGivenName(String text) {
		DCPersonName tmpPersonName = new DCPersonName(text);

		if (StringUtils.isNotBlank(tmpPersonName.getFirstNames())) {
			return tmpPersonName.getFirstNames();
		}
		return "";
	}

}
