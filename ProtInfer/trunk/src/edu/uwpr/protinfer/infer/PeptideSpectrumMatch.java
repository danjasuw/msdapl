package edu.uwpr.protinfer.infer;


public interface PeptideSpectrumMatch <T extends SpectrumMatch>{

    // database ID of the result (e.g. Percolator result id)
    public abstract int getResultId();
    
    // database ID of the underlying search result that this result corresponds to. 
    // If this result and the underlying search result are the same, getResultId()
    // and getSearchResultId() will return the same ID. 
    public abstract int getSearchResultId(); 
    
    public abstract int getScanId();
    
    public abstract int getCharge();
    
    public abstract T getSpectrumMatch();
    
    /**
     * Returns the sequence of the peptide for the spectrum match.
     * @return
     */
    public abstract String getPeptideSequence();
    
    public abstract PeptideHit getPeptideHit();
}
