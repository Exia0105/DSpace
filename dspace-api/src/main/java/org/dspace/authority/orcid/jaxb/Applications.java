//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2015.06.16 at 06:33:14 PM CEST 
//


package org.dspace.authority.orcid.jaxb;

import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element ref="{http://www.orcid.org/ns/orcid}application-summary" maxOccurs="unbounded"/>
 *       &lt;/sequence>
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
    "applicationSummary"
})
@XmlRootElement(name = "applications")
public class Applications {

    @XmlElement(name = "application-summary", required = true)
    protected List<ApplicationSummary> applicationSummary;
    @XmlAttribute(name = "visibility")
    protected Visibility visibility;

    /**
     * Gets the value of the applicationSummary property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applicationSummary property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplicationSummary().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ApplicationSummary }
     * 
     * 
     */
    public List<ApplicationSummary> getApplicationSummary() {
        if (applicationSummary == null) {
            applicationSummary = new ArrayList<ApplicationSummary>();
        }
        return this.applicationSummary;
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
