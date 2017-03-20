package dkpro.type;

/* First created by JCasGen Sun Mar 19 02:24:15 CET 2017 */

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.JCasRegistry;
import org.apache.uima.cas.impl.TypeImpl;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.impl.FeatureImpl;
import org.apache.uima.cas.Feature;
import org.apache.uima.jcas.tcas.Annotation_Type;

/** 
 * Updated by JCasGen Sun Mar 19 14:23:50 CET 2017
 * @generated */
public class RelationType_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = RelationType.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("dkpro.type.RelationType");
 
  /** @generated */
  final Feature casFeat_label;
  /** @generated */
  final int     casFeatCode_label;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getLabel(int addr) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "dkpro.type.RelationType");
    return ll_cas.ll_getStringValue(addr, casFeatCode_label);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabel(int addr, String v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "dkpro.type.RelationType");
    ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
    
  
 
  /** @generated */
  final Feature casFeat_rType;
  /** @generated */
  final int     casFeatCode_rType;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public int getRType(int addr) {
        if (featOkTst && casFeat_rType == null)
      jcas.throwFeatMissing("rType", "dkpro.type.RelationType");
    return ll_cas.ll_getRefValue(addr, casFeatCode_rType);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setRType(int addr, int v) {
        if (featOkTst && casFeat_rType == null)
      jcas.throwFeatMissing("rType", "dkpro.type.RelationType");
    ll_cas.ll_setRefValue(addr, casFeatCode_rType, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public RelationType_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_rType = jcas.getRequiredFeatureDE(casType, "rType", "uima.tcas.Annotation", featOkTst);
    casFeatCode_rType  = (null == casFeat_rType) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_rType).getCode();

  }
}



    