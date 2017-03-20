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
public class Relation extends Annotation {
  /** @generated
   * @ordered 
   */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = JCasRegistry.register(Relation.class);
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
  protected Relation() {/* intentionally empty block */}
    
  /** Internal - constructor used by generator 
   * @generated
   * @param addr low level Feature Structure reference
   * @param type the type of this Feature Structure 
   */
  public Relation(int addr, TOP_Type type) {
    super(addr, type);
    readObject();
  }
  
  /** @generated
   * @param jcas JCas to which this Feature Structure belongs 
   */
  public Relation(JCas jcas) {
    super(jcas);
    readObject();   
  } 

  /** @generated
   * @param jcas JCas to which this Feature Structure belongs
   * @param begin offset to the begin spot in the SofA
   * @param end offset to the end spot in the SofA 
  */  
  public Relation(JCas jcas, int begin, int end) {
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
  //* Feature: source

  /** getter for source - gets 
   * @generated
   * @return value of the feature 
   */
  public Concept getSource() {
    if (Relation_Type.featOkTst && ((Relation_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "dkpro.type.Relation");
    return (Concept)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Relation_Type)jcasType).casFeatCode_source)));}
    
  /** setter for source - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setSource(Concept v) {
    if (Relation_Type.featOkTst && ((Relation_Type)jcasType).casFeat_source == null)
      jcasType.jcas.throwFeatMissing("source", "dkpro.type.Relation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Relation_Type)jcasType).casFeatCode_source, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: target

  /** getter for target - gets 
   * @generated
   * @return value of the feature 
   */
  public Concept getTarget() {
    if (Relation_Type.featOkTst && ((Relation_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "dkpro.type.Relation");
    return (Concept)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Relation_Type)jcasType).casFeatCode_target)));}
    
  /** setter for target - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setTarget(Concept v) {
    if (Relation_Type.featOkTst && ((Relation_Type)jcasType).casFeat_target == null)
      jcasType.jcas.throwFeatMissing("target", "dkpro.type.Relation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Relation_Type)jcasType).casFeatCode_target, jcasType.ll_cas.ll_getFSRef(v));}    
   
    
  //*--------------*
  //* Feature: relation

  /** getter for relation - gets 
   * @generated
   * @return value of the feature 
   */
  public RelationType getRelation() {
    if (Relation_Type.featOkTst && ((Relation_Type)jcasType).casFeat_relation == null)
      jcasType.jcas.throwFeatMissing("relation", "dkpro.type.Relation");
    return (RelationType)(jcasType.ll_cas.ll_getFSForRef(jcasType.ll_cas.ll_getRefValue(addr, ((Relation_Type)jcasType).casFeatCode_relation)));}
    
  /** setter for relation - sets  
   * @generated
   * @param v value to set into the feature 
   */
  public void setRelation(RelationType v) {
    if (Relation_Type.featOkTst && ((Relation_Type)jcasType).casFeat_relation == null)
      jcasType.jcas.throwFeatMissing("relation", "dkpro.type.Relation");
    jcasType.ll_cas.ll_setRefValue(addr, ((Relation_Type)jcasType).casFeatCode_relation, jcasType.ll_cas.ll_getFSRef(v));}    
  }

    