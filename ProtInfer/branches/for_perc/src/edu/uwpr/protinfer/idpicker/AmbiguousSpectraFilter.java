/**
 * AmbiguousSpectraFilter.java
 * @author Vagisha Sharma
 * Mar 3, 2009
 * @version 1.0
 */
package edu.uwpr.protinfer.idpicker;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.yeastrc.ms.domain.search.MsSearchResult;

import edu.uwpr.protinfer.infer.PeptideSpectrumMatch;
import edu.uwpr.protinfer.util.TimeUtils;

/**
 * 
 */
public class AmbiguousSpectraFilter {

    private static final AmbiguousSpectraFilter instance = new AmbiguousSpectraFilter();
    
    private static final Logger log = Logger.getLogger(AmbiguousSpectraFilter.class.getName());
    
    private AmbiguousSpectraFilter() {}
    
    public static AmbiguousSpectraFilter instance() {
        return instance;
    }
    
    public <T extends PeptideSpectrumMatch<?>> void filterSpectraWithMultiplePSMs(List<T> psmList) {
        
        long s = System.currentTimeMillis();
        // sort by scanID
        Collections.sort(psmList, new Comparator<PeptideSpectrumMatch<?>>() {
            public int compare(PeptideSpectrumMatch<?> o1, PeptideSpectrumMatch<?> o2) {
                return Integer.valueOf(o1.getScanId()).compareTo(o2.getScanId());
            }});
        
        // get a list of scan Ids that have multiple results
        Set<Integer> scanIdsToRemove = new HashSet<Integer>();
        
        int lastScanId = -1;
        for (int i = 0; i < psmList.size(); i++) {
            T psm = psmList.get(i);
            if(lastScanId != -1){
                if(lastScanId == psm.getScanId()) {
                    scanIdsToRemove.add(lastScanId);
                }
            }
            lastScanId = psm.getScanId();
        }
        
        Iterator<T> iter = psmList.iterator();
        while(iter.hasNext()) {
            T psm = iter.next();
            if(scanIdsToRemove.contains(psm.getScanId())) {
//                log.info("Removing for scanID: "+psm.getScanId()+"; resultID: "+psm.getHitId());
                iter.remove();
            }
        }
        long e = System.currentTimeMillis();
//        log.info("\nRR\t"+runSearchAnalysisId+"\t"+allScanIds.size()+"\t"+scanIdsToRemove.size());
        log.info("Removed "+scanIdsToRemove.size()+" scans with multiple results. "+
                "Remaining results: "+psmList.size()+". Time: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds\n");
    }
    
    public <T extends MsSearchResult> void filterSpectraWithMultipleResults(List<T> psmList) {
        
        long s = System.currentTimeMillis();
        // sort by scanID
        Collections.sort(psmList, new Comparator<T>() {
            public int compare(T o1, T o2) {
                return Integer.valueOf(o1.getScanId()).compareTo(o2.getScanId());
            }});
        
        // get a list of scan Ids that have multiple results
        Set<Integer> scanIdsToRemove = new HashSet<Integer>();
        
        int lastScanId = -1;
        for (int i = 0; i < psmList.size(); i++) {
            T psm = psmList.get(i);
            if(lastScanId != -1){
                if(lastScanId == psm.getScanId()) {
                    scanIdsToRemove.add(lastScanId);
                }
            }
            lastScanId = psm.getScanId();
        }
        
        Iterator<T> iter = psmList.iterator();
        while(iter.hasNext()) {
            T psm = iter.next();
            if(scanIdsToRemove.contains(psm.getScanId())) {
//                log.info("Removing for scanID: "+psm.getScanId()+"; resultID: "+psm.getHitId());
                iter.remove();
            }
        }
        long e = System.currentTimeMillis();
//        log.info("\nRR\t"+runSearchAnalysisId+"\t"+allScanIds.size()+"\t"+scanIdsToRemove.size());
        log.info("Removed "+scanIdsToRemove.size()+" scans with multiple results. "+
                "Remaining results: "+psmList.size()+". Time: "+TimeUtils.timeElapsedSeconds(s, e)+" seconds\n");
    }
}
