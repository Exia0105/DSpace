/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.content.authority;

public class ULANAuthority extends GettyAuthority {

	String query ="SELECT ?Subject ?Term ?Parents ?ScopeNote { ?Subject luc:term \"%s\"; skos:inScheme ulan: ; gvp:prefLabelGVP [skosxl:literalForm ?Term; gvp:term ?pureTerm]. optional {?Subject gvp:parentStringAbbrev ?Parents} optional {?Subject skos:scopeNote [dct:language gvp_lang:en; rdf:value ?ScopeNote]}} ORDER BY ASC(LCASE(STR(?pureTerm)))";
	@Override
	public Choices getMatches(String field, String text, int collection, int start, int limit, String locale) {
		String sparQL = String.format(query, text);
		Choices results = query(sparQL);
		return results;
	}

}
