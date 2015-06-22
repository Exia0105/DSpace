//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.16 at 06:33:14 PM CEST 
//


package org.dspace.authority.orcid.jaxb;

import java.math.BigInteger;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}work-title" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}journal-title" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}short-description" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}work-citation" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}work-type"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}publication-date" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}work-external-identifiers" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}url" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}work-contributors" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}source" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}created-date" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}last-modified-date" minOccurs="0"/>
 *         &lt;element name="language-code" type="{http://www.orcid.org/ns/orcid}language-code" minOccurs="0"/>
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}country" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.orcid.org/ns/orcid}put-code"/>
 *       &lt;attGroup ref="{http://www.orcid.org/ns/orcid}visibility"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "workTitle",
    "journalTitle",
    "shortDescription",
    "workCitation",
    "workType",
    "publicationDate",
    "workExternalIdentifiers",
    "url",
    "workContributors",
    "source",
    "createdDate",
    "lastModifiedDate",
    "languageCode",
    "country"
})
@XmlRootElement(name = "orcid-work")
public class OrcidWork {

    @XmlElement(name = "work-title")
    protected WorkTitle workTitle;
    @XmlElement(name = "journal-title")
    protected JournalTitle journalTitle;
    @XmlElement(name = "short-description")
    protected String shortDescription;
    @XmlElement(name = "work-citation")
    protected Citation workCitation;
    @XmlElement(name = "work-type", required = true)
    protected String workType;
    @XmlElement(name = "publication-date")
    protected PublicationDate publicationDate;
    @XmlElement(name = "work-external-identifiers")
    protected WorkExternalIdentifiers workExternalIdentifiers;
    protected Url url;
    @XmlElement(name = "work-contributors")
    protected WorkContributors workContributors;
    protected Source source;
    @XmlElement(name = "created-date")
    protected CreatedDate createdDate;
    @XmlElement(name = "last-modified-date")
    protected LastModifiedDate lastModifiedDate;
    @XmlElement(name = "language-code")
    protected LanguageCode languageCode;
    protected Country country;
    @XmlAttribute(name = "put-code")
    protected BigInteger putCode;
    @XmlAttribute(name = "visibility")
    protected Visibility visibility;

    /**
     * Gets the value of the workTitle property.
     * 
     * @return
     *     possible object is
     *     {@link WorkTitle }
     *     
     */
    public WorkTitle getWorkTitle() {
        return workTitle;
    }

    /**
     * Sets the value of the workTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkTitle }
     *     
     */
    public void setWorkTitle(WorkTitle value) {
        this.workTitle = value;
    }

    /**
     * Gets the value of the journalTitle property.
     * 
     * @return
     *     possible object is
     *     {@link JournalTitle }
     *     
     */
    public JournalTitle getJournalTitle() {
        return journalTitle;
    }

    /**
     * Sets the value of the journalTitle property.
     * 
     * @param value
     *     allowed object is
     *     {@link JournalTitle }
     *     
     */
    public void setJournalTitle(JournalTitle value) {
        this.journalTitle = value;
    }

    /**
     * Gets the value of the shortDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Sets the value of the shortDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setShortDescription(String value) {
        this.shortDescription = value;
    }

    /**
     * Gets the value of the workCitation property.
     * 
     * @return
     *     possible object is
     *     {@link Citation }
     *     
     */
    public Citation getWorkCitation() {
        return workCitation;
    }

    /**
     * Sets the value of the workCitation property.
     * 
     * @param value
     *     allowed object is
     *     {@link Citation }
     *     
     */
    public void setWorkCitation(Citation value) {
        this.workCitation = value;
    }

    /**
     * Gets the value of the workType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWorkType() {
        return workType;
    }

    /**
     * Sets the value of the workType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWorkType(String value) {
        this.workType = value;
    }

    /**
     * Gets the value of the publicationDate property.
     * 
     * @return
     *     possible object is
     *     {@link PublicationDate }
     *     
     */
    public PublicationDate getPublicationDate() {
        return publicationDate;
    }

    /**
     * Sets the value of the publicationDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link PublicationDate }
     *     
     */
    public void setPublicationDate(PublicationDate value) {
        this.publicationDate = value;
    }

    /**
     * Gets the value of the workExternalIdentifiers property.
     * 
     * @return
     *     possible object is
     *     {@link WorkExternalIdentifiers }
     *     
     */
    public WorkExternalIdentifiers getWorkExternalIdentifiers() {
        return workExternalIdentifiers;
    }

    /**
     * Sets the value of the workExternalIdentifiers property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkExternalIdentifiers }
     *     
     */
    public void setWorkExternalIdentifiers(WorkExternalIdentifiers value) {
        this.workExternalIdentifiers = value;
    }

    /**
     * Gets the value of the url property.
     * 
     * @return
     *     possible object is
     *     {@link Url }
     *     
     */
    public Url getUrl() {
        return url;
    }

    /**
     * Sets the value of the url property.
     * 
     * @param value
     *     allowed object is
     *     {@link Url }
     *     
     */
    public void setUrl(Url value) {
        this.url = value;
    }

    /**
     * Gets the value of the workContributors property.
     * 
     * @return
     *     possible object is
     *     {@link WorkContributors }
     *     
     */
    public WorkContributors getWorkContributors() {
        return workContributors;
    }

    /**
     * Sets the value of the workContributors property.
     * 
     * @param value
     *     allowed object is
     *     {@link WorkContributors }
     *     
     */
    public void setWorkContributors(WorkContributors value) {
        this.workContributors = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link Source }
     *     
     */
    public Source getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link Source }
     *     
     */
    public void setSource(Source value) {
        this.source = value;
    }

    /**
     * Gets the value of the createdDate property.
     * 
     * @return
     *     possible object is
     *     {@link CreatedDate }
     *     
     */
    public CreatedDate getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets the value of the createdDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link CreatedDate }
     *     
     */
    public void setCreatedDate(CreatedDate value) {
        this.createdDate = value;
    }

    /**
     * Gets the value of the lastModifiedDate property.
     * 
     * @return
     *     possible object is
     *     {@link LastModifiedDate }
     *     
     */
    public LastModifiedDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    /**
     * Sets the value of the lastModifiedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link LastModifiedDate }
     *     
     */
    public void setLastModifiedDate(LastModifiedDate value) {
        this.lastModifiedDate = value;
    }

    /**
     * Gets the value of the languageCode property.
     * 
     * @return
     *     possible object is
     *     {@link LanguageCode }
     *     
     */
    public LanguageCode getLanguageCode() {
        return languageCode;
    }

    /**
     * Sets the value of the languageCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link LanguageCode }
     *     
     */
    public void setLanguageCode(LanguageCode value) {
        this.languageCode = value;
    }

    /**
     * Gets the value of the country property.
     * 
     * @return
     *     possible object is
     *     {@link Country }
     *     
     */
    public Country getCountry() {
        return country;
    }

    /**
     * Sets the value of the country property.
     * 
     * @param value
     *     allowed object is
     *     {@link Country }
     *     
     */
    public void setCountry(Country value) {
        this.country = value;
    }

    /**
     * Gets the value of the putCode property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    public BigInteger getPutCode() {
        return putCode;
    }

    /**
     * Sets the value of the putCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    public void setPutCode(BigInteger value) {
        this.putCode = value;
    }

    /**
     * Gets the value of the visibility property.
     * 
     * @return
     *     possible object is
     *     {@link Visibility }
     *     
     */
    public Visibility getVisibility() {
        return visibility;
    }

    /**
     * Sets the value of the visibility property.
     * 
     * @param value
     *     allowed object is
     *     {@link Visibility }
     *     
     */
    public void setVisibility(Visibility value) {
        this.visibility = value;
    }

}
