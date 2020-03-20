package edu.cmu.cs.diamond.opendiamond;

public class FeedbackObject {
    //public String feature_vector; 
    public byte[] feature_vector; 
    public int label;
    private ObjectIdentifier objId;

    public FeedbackObject(byte[] feature_vector, int label) {
        this.feature_vector = feature_vector != null ? feature_vector : new byte[0];
        this.label = label;
    }

    public FeedbackObject(byte[] feature_vector, int label, ObjectIdentifier objId) {
        this.feature_vector = feature_vector != null ? feature_vector : new byte[0];
        this.label = label;
        this.objId = objId;
    }


    public ObjectIdentifier getObjectIdentifier() {
        return objId;
    }
}
