//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.02 at 03:44:23 PM EST 
//


package edu.cmu.cs.diamond.opendiamond.bundle;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for FilterSpec complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="FilterSpec">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="minScore" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}FilterThresholdSpec" minOccurs="0"/>
 *         &lt;element name="maxScore" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}FilterThresholdSpec" minOccurs="0"/>
 *         &lt;element name="dependencies" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}FilterDependencyList" minOccurs="0"/>
 *         &lt;element name="arguments" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}FilterArgumentList" minOccurs="0"/>
 *         &lt;element name="blob" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}FilterBlobArgumentSpec" minOccurs="0"/>
 *       &lt;/all>
 *       &lt;attribute name="fixedName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="label" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="code" use="required" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}Filename" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FilterSpec", propOrder = {

})
public class FilterSpec {

    protected FilterThresholdSpec minScore;
    protected FilterThresholdSpec maxScore;
    @XmlElement(name = "dependencies")
    protected FilterDependencyList dependencyList;
    @XmlElement(name = "arguments")
    protected FilterArgumentList argumentList;
    protected FilterBlobArgumentSpec blob;
    @XmlAttribute(name = "fixedName")
    protected String fixedName;
    @XmlAttribute(name = "label")
    protected String label;
    @XmlAttribute(name = "code", required = true)
    protected String code;

    /**
     * 
     * The minimum filter score in order to pass the object.
     *         
     * 
     * @return
     *     possible object is
     *     {@link FilterThresholdSpec }
     *     
     */
    public FilterThresholdSpec getMinScore() {
        return minScore;
    }

    /**
     * Sets the value of the minScore property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterThresholdSpec }
     *     
     */
    public void setMinScore(FilterThresholdSpec value) {
        this.minScore = value;
    }

    /**
     * 
     * The maximum filter score in order to pass the object.
     *         
     * 
     * @return
     *     possible object is
     *     {@link FilterThresholdSpec }
     *     
     */
    public FilterThresholdSpec getMaxScore() {
        return maxScore;
    }

    /**
     * Sets the value of the maxScore property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterThresholdSpec }
     *     
     */
    public void setMaxScore(FilterThresholdSpec value) {
        this.maxScore = value;
    }

    /**
     * 
     * A list of filters that must run before this one.
     *           
     * 
     * @return
     *     possible object is
     *     {@link FilterDependencyList }
     *     
     */
    public FilterDependencyList getDependencyList() {
        return dependencyList;
    }

    /**
     * Sets the value of the dependencyList property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterDependencyList }
     *     
     */
    public void setDependencyList(FilterDependencyList value) {
        this.dependencyList = value;
    }

    /**
     * 
     * A list of arguments to be passed to this filter.  Arguments are serialized
     * to strings before they are sent to the server.  Filters must convert them
     * back to the desired type.
     *           
     * 
     * @return
     *     possible object is
     *     {@link FilterArgumentList }
     *     
     */
    public FilterArgumentList getArgumentList() {
        return argumentList;
    }

    /**
     * Sets the value of the argumentList property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterArgumentList }
     *     
     */
    public void setArgumentList(FilterArgumentList value) {
        this.argumentList = value;
    }

    /**
     * 
     * The blob argument to be passed to this filter.  If child elements are
     * present, construct a blob argument consisting of a Zip archive containing
     * the specified members.
     *         
     * 
     * @return
     *     possible object is
     *     {@link FilterBlobArgumentSpec }
     *     
     */
    public FilterBlobArgumentSpec getBlob() {
        return blob;
    }

    /**
     * Sets the value of the blob property.
     * 
     * @param value
     *     allowed object is
     *     {@link FilterBlobArgumentSpec }
     *     
     */
    public void setBlob(FilterBlobArgumentSpec value) {
        this.blob = value;
    }

    /**
     * 
     * A fixed name for this filter.  This should not be used unless there is a
     * specific reason to hardcode the filter name.  By default, filter names are
     * dynamically generated to avoid conflicts if a filter is added more than once
     * with different parameters.
     *       
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFixedName() {
        return fixedName;
    }

    /**
     * Sets the value of the fixedName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFixedName(String value) {
        this.fixedName = value;
    }

    /**
     * 
     * A label for specifying filter dependencies.
     *       
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLabel(String value) {
        this.label = value;
    }

    /**
     * 
     * The filename of the filter code.
     *       
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCode() {
        return code;
    }

    /**
     * Sets the value of the code property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCode(String value) {
        this.code = value;
    }

}