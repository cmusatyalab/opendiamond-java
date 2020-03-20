//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.02 at 03:44:23 PM EST 
//


package edu.cmu.cs.diamond.opendiamond.bundle;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 * A choice option, displayed in the user interface as a popup menu of choices.
 *     
 * 
 * <p>Java class for ChoiceOption complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ChoiceOption">
 *   &lt;complexContent>
 *     &lt;extension base="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}Option">
 *       &lt;sequence>
 *         &lt;element name="choice" type="{http://diamond.cs.cmu.edu/xmlns/opendiamond/bundle-1}Choice" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="initiallyEnabled" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="disabledValue" type="{http://www.w3.org/2001/XMLSchema}string" default="" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChoiceOption", propOrder = {
    "choices"
})
public class ChoiceOption
    extends Option
{

    @XmlElement(name = "choice", required = true)
    protected List<Choice> choices;
    @XmlAttribute(name = "initiallyEnabled")
    protected Boolean initiallyEnabled;
    @XmlAttribute(name = "disabledValue")
    protected String disabledValue;

    /**
     * 
     * One possible value for the choice option.
     *               Gets the value of the choices property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the choices property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChoices().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Choice }
     * 
     * 
     */
    public List<Choice> getChoices() {
        if (choices == null) {
            choices = new ArrayList<Choice>();
        }
        return this.choices;
    }

    /**
     * 
     * If specified, the option can be disabled by the user, and the initial
     * enablement is as specified.
     *           
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isInitiallyEnabled() {
        return initiallyEnabled;
    }

    /**
     * Sets the value of the initiallyEnabled property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setInitiallyEnabled(Boolean value) {
        this.initiallyEnabled = value;
    }

    /**
     * 
     * The value returned if the option is disabled by the user.
     *           
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDisabledValue() {
        if (disabledValue == null) {
            return "";
        } else {
            return disabledValue;
        }
    }

    /**
     * Sets the value of the disabledValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDisabledValue(String value) {
        this.disabledValue = value;
    }

}