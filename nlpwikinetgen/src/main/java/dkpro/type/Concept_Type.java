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
public class Concept_Type extends Annotation_Type {
  /** @generated */
  @SuppressWarnings ("hiding")
  public final static int typeIndexID = Concept.typeIndexID;
  /** @generated 
     @modifiable */
  @SuppressWarnings ("hiding")
  public final static boolean featOkTst = JCasRegistry.getFeatOkTst("dkpro.type.Concept");
 
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
      jcas.throwFeatMissing("label", "dkpro.type.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_label);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setLabel(int addr, String v) {
        if (featOkTst && casFeat_label == null)
      jcas.throwFeatMissing("label", "dkpro.type.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_label, v);}
    
  
 
  /** @generated */
  final Feature casFeat_URI;
  /** @generated */
  final int     casFeatCode_URI;
  /** @generated
   * @param addr low level Feature Structure reference
   * @return the feature value 
   */ 
  public String getURI(int addr) {
        if (featOkTst && casFeat_URI == null)
      jcas.throwFeatMissing("URI", "dkpro.type.Concept");
    return ll_cas.ll_getStringValue(addr, casFeatCode_URI);
  }
  /** @generated
   * @param addr low level Feature Structure reference
   * @param v value to set 
   */    
  public void setURI(int addr, String v) {
        if (featOkTst && casFeat_URI == null)
      jcas.throwFeatMissing("URI", "dkpro.type.Concept");
    ll_cas.ll_setStringValue(addr, casFeatCode_URI, v);}
    
  



  /** initialize variables to correspond with Cas Type and Features
	 * @generated
	 * @param jcas JCas
	 * @param casType Type 
	 */
  public Concept_Type(JCas jcas, Type casType) {
    super(jcas, casType);
    casImpl.getFSClassRegistry().addGeneratorForType((TypeImpl)this.casType, getFSGenerator());

 
    casFeat_label = jcas.getRequiredFeatureDE(casType, "label", "uima.cas.String", featOkTst);
    casFeatCode_label  = (null == casFeat_label) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_label).getCode();

 
    casFeat_URI = jcas.getRequiredFeatureDE(casType, "URI", "uima.cas.String", featOkTst);
    casFeatCode_URI  = (null == casFeat_URI) ? JCas.INVALID_FEATURE_CODE : ((FeatureImpl)casFeat_URI).getCode();

  }
}



    