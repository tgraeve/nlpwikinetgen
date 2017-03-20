package dkpro.type;


/* First created by JCasGen Sun Mar 19 02:24:15 CET 2017 */

import org.apache.uima.jcas.JCas; 
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.jcas.cas.TOP_Type;

import org.apache.uima.jcas.tcas.Annotation;


/** 
 * Updated by JCasGen Sun Mar 19 14:23:50 CET 2017
 * XML source: /Users/Tobias/git/nlpwikinetgen/nlpwikinetgen/src/main/resources/desc/type/NetworkExtraction.xml
 * @generated */
public class Concept extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Concept.class);
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int type = typeIndexID;
  /** @generated
   * @return index of the type  
   */
  @Override
  public              int getTypeIndexID() {return typeIndexID;}
 
  /** Never called.  Disable default constructor
   * @generated */
  protected Concept() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Concept(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Concept(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Concept(JCas jcas, int begin, int end) {
    super(jcas);
    setBegin(begin);
    setEnd(end);
    readObject();
  }   

  /** 
   * <!-- begin-user-doc -->
   * Write your own initialization here
   * <!-- end-user-doc -->
   *
   * @generated modifiable 
   */
  private void readObject() {/*default - does nothing empty block */}
     
 
    
  //*--------------*
  //* Feature: label

  /** getter for label - gets 
   * @generated
   * @return value of the feature 
   */
  public String getLabel() {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "dkpro.type.Concept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Concept_Type)jcasType).casFeatCode_label);}
    
  /** setter for label - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setLabel(String v) {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_label == null)
      jcasType.jcas.throwFeatMissing("label", "dkpro.type.Concept");
    jcasType.ll_cas.ll_setStringValue(addr, ((Concept_Type)jcasType).casFeatCode_label, v);}    
   
    
  //*--------------*
  //* Feature: URI

  /** getter for URI - gets 
   * @generated
   * @return value of the feature 
   */
  public String getURI() {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_URI == null)
      jcasType.jcas.throwFeatMissing("URI", "dkpro.type.Concept");
    return jcasType.ll_cas.ll_getStringValue(addr, ((Concept_Type)jcasType).casFeatCode_URI);}
    
  /** setter for URI - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setURI(String v) {
    if (Concept_Type.featOkTst && ((Concept_Type)jcasType).casFeat_URI == null)
      jcasType.jcas.throwFeatMissing("URI", "dkpro.type.Concept");
    jcasType.ll_cas.ll_setStringValue(addr, ((Concept_Type)jcasType).casFeatCode_URI, v);}    
  }

    