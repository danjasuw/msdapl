/**
 * SQTDataUploadService.java
 * @author Vagisha Sharma
 * Jul 15, 2008
 * @version 1.0
 */
package org.yeastrc.ms.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.yeastrc.ms.dao.DAOFactory;
import org.yeastrc.ms.dao.MsScanDAO;
import org.yeastrc.ms.dao.MsSearchDAO;
import org.yeastrc.ms.dao.MsSearchModificationDAO;
import org.yeastrc.ms.dao.MsSearchResultDAO;
import org.yeastrc.ms.dao.MsSearchResultProteinDAO;
import org.yeastrc.ms.dao.ibatis.MsSearchModificationDAOImpl.MsSearchResultModSqlMapParam;
import org.yeastrc.ms.dao.ibatis.MsSearchResultProteinDAOImpl.MsResultProteinSqlMapParam;
import org.yeastrc.ms.dao.sqtFile.SQTSearchResultDAO;
import org.yeastrc.ms.dao.sqtFile.SQTSearchScanDAO;
import org.yeastrc.ms.dao.util.DynamicModLookupUtil;
import org.yeastrc.ms.domain.MsScan;
import org.yeastrc.ms.domain.MsScanDb;
import org.yeastrc.ms.domain.MsSearch;
import org.yeastrc.ms.domain.MsSearchDb;
import org.yeastrc.ms.domain.MsSearchResult;
import org.yeastrc.ms.domain.MsSearchResultDb;
import org.yeastrc.ms.domain.MsSearchResultModification;
import org.yeastrc.ms.domain.MsSearchResultProtein;
import org.yeastrc.ms.domain.MsSearchResultProteinDb;
import org.yeastrc.ms.domain.MsSearchModification.ModificationType;
import org.yeastrc.ms.domain.sqtFile.SQTSearch;
import org.yeastrc.ms.domain.sqtFile.SQTSearchDb;
import org.yeastrc.ms.domain.sqtFile.SQTSearchResult;
import org.yeastrc.ms.domain.sqtFile.SQTSearchResultScoresDb;
import org.yeastrc.ms.domain.sqtFile.SQTSearchScan;
import org.yeastrc.ms.parser.ParserException;

/**
 * 
 */
class SQTDataUploadService {

    private static final Logger log = Logger.getLogger(SQTDataUploadService.class);
    
    private static final DAOFactory daoFactory = DAOFactory.instance();
    
    private static final DynamicModLookupUtil dynaModLookup = DynamicModLookupUtil.instance();
    
    public static final int BUF_SIZE = 1000;
    
    private int uploadedSearchId = 0;
    
    // these are the things we will cache and do bulk-inserts
    List<MsSearchResultProteinDb> proteinMatchList; // protein matches
    List<SQTSearchResultScoresDb> sqtResultScoresList; // sequest scores
    List<MsSearchResultModSqlMapParam> resultModList; // dynamic modifications
    
    
    public SQTDataUploadService() {
        proteinMatchList = new ArrayList<MsSearchResultProteinDb>();
        sqtResultScoresList = new ArrayList<SQTSearchResultScoresDb>();
        resultModList = new ArrayList<MsSearchResultModSqlMapParam>();
    }
    
    public int uploadSQTSearch(SQTSearchDataProvider provider, int runId) throws Exception {

        log.info("BEGIN SQT FILE UPLOAD: "+provider.getFileName()+"; RUN_ID: "+runId);
        long startTime = System.currentTimeMillis();
        
        int searchId = uploadSearch(provider.getSearchHeader(), runId);
        uploadedSearchId = searchId;
        
        log.info("Uploaded top-level info for search with searchId: "+searchId);

        // upload the search results for each scan + charge combination
        int numResults = 0;
        int numProteins = 0;
        while (provider.hasNextSearchScan()) {
            SQTSearchScan scan = null;
            try {
                scan = provider.getNextSearchScan();
                
                // if the scan does not have any results don't upload it.
                if (scan.getScanResults().size() == 0) {
                    log.warn("!!!Scan will NOT be uploaded. No results found for scan (scanNumber: "+scan.getScanNumber()+", charge: "+scan.getCharge());
                    continue;
                }
                
                int scanId = getScanId(runId, scan.getScanNumber());
                // save spectrum data
                uploadSearchScan(scan, searchId, scanId); 
                
                // save all the search results for this scan
                for (SQTSearchResult result: scan.getScanResults()) {
                    uploadSearchResult(result, searchId, scanId);
                    numResults++;
                    numProteins += result.getProteinMatchList().size();
                }
            }
            catch (ParserException e) {
                log.warn("Error processing search result for scan. Results will not be uploaded. "+e.getMessage(), e);
            }
            
        }
        flush(); // save any cached data
        
        long endTime = System.currentTimeMillis();
        log.info("Uploaded SQT files with "+numResults+" results, "+numProteins+" protein matches. (searchId: "+searchId+")"
                + " in "+(endTime - startTime)/(1000L)+"seconds");
        log.info("END SQT FILE UPLOAD: "+provider.getFileName()+"; RUN_ID: "+runId);
        
        return searchId;
    }
    
    public int getUploadedSearchId() {
        return uploadedSearchId;
    }
    private static int getScanId(int runId, int scanNumber) {
        MsScanDAO<MsScan, MsScanDb> scanDao = DAOFactory.instance().getMsScanDAO();
        int scanId = scanDao.loadScanIdForScanNumRun(scanNumber, runId);
        if (scanId == 0)
            throw new IllegalArgumentException("No scanId found for scan number: "+scanNumber+" and runId: "+runId);
        return scanId;
    }
    
    
    private int uploadSearch(SQTSearch search, int runId) {
        
        // RESET THE DYNAMIC MOD LOOKUP UTILITY
        dynaModLookup.reset();
        
        // clean up any cached data
        proteinMatchList.clear();
        sqtResultScoresList.clear();
        resultModList.clear();
        
        // save the search and return the database id
        MsSearchDAO<SQTSearch, SQTSearchDb> searchDao = daoFactory.getSqtSearchDAO();
        return searchDao.saveSearch(search, runId);
    }
    
    private void uploadSearchScan(SQTSearchScan scan, int searchId, int scanId) {
        SQTSearchScanDAO spectrumDataDao = DAOFactory.instance().getSqtSpectrumDAO();
        spectrumDataDao.save(scan, searchId, scanId);
    }
    
    private int uploadSearchResult(SQTSearchResult result, int searchId, int scanId) {
        
        try {
            result.getResultPeptide(); // parse the peptide sequence to get the pre, post residues, modifications etc.
        }
        catch(IllegalArgumentException e) {
            log.error(("!!!Peptide sequence appears to be invalid. Unlable to upload result... Skipping: "+e.getMessage()));
            return 0;
        }
        
        MsSearchResultDAO<MsSearchResult, MsSearchResultDb> resultDao = DAOFactory.instance().getMsSearchResultDAO();
        int resultId = resultDao.saveResultOnly(result, searchId, scanId);
        
        // upload dynamic mods for this result
        uploadResultMods(result, resultId, searchId);
        
        // upload the protein matches
        uploadProteinMatches(result, resultId);
        
        // upload the SQT file specific information for this result.
        uploadSQTResult(result, resultId);
        
        return resultId;
    }

    private void uploadProteinMatches(SQTSearchResult result, int resultId) {
        // upload the protein matches if the cache has enough entries
        if (proteinMatchList.size() >= BUF_SIZE) {
            uploadProteinMatchBuffer();
        }
        // add the protein matches for this result to the cache
        for (MsSearchResultProtein match: result.getProteinMatchList()) {
            proteinMatchList.add(new MsResultProteinSqlMapParam(resultId, match.getAccession(), match.getDescription()));
        }
    }

    private void uploadProteinMatchBuffer() {
        MsSearchResultProteinDAO matchDao = daoFactory.getMsProteinMatchDAO();
        matchDao.saveAll(proteinMatchList);
        proteinMatchList.clear();
    }
    
    private void uploadResultMods(SQTSearchResult result, int resultId, int searchId) {
        // upload the result dynamic modifications if the cache has enough entries
        if (resultModList.size() >= BUF_SIZE) {
            uploadProteinMatchBuffer();
        }
        // add the dynamic modifications for this result to the cache
        for (MsSearchResultModification mod: result.getResultPeptide().getDynamicModifications()) {
            if (mod == null || mod.getModificationType() == ModificationType.STATIC)
                continue;
            int modId = DynamicModLookupUtil.instance().getDynamicModificationId(searchId, 
                    mod.getModifiedResidue(), mod.getModificationMass());
            resultModList.add(new MsSearchResultModSqlMapParam(resultId, modId, mod.getModifiedPosition()));
        }
    }

    private void uploadResultModBuffer() {
        MsSearchModificationDAO modDao = daoFactory.getMsSearchModDAO();
        modDao.saveAllDynamicModificationForSearchResult(resultModList);
        resultModList.clear();
    }
    
    private void uploadSQTResult(SQTSearchResult result, int resultId) {
        // upload the SQT file specific result information if the cache has enough entries
        if (sqtResultScoresList.size() >= BUF_SIZE) {
            uploadSqtResultBuffer();
        }
        // add the SQT file specific information for this result to the cache
        SQTSearchResultScores sqtResultOnly = new SQTSearchResultScores();
        sqtResultOnly.resultId = resultId;
        sqtResultOnly.xcorrRank = result.getxCorrRank();
        sqtResultOnly.spRank = result.getSpRank();
        sqtResultOnly.deltaCN = result.getDeltaCN();
        sqtResultOnly.xcorr = result.getxCorr();
        sqtResultOnly.sp = result.getSp();
        sqtResultScoresList.add(sqtResultOnly);
    }
    
    private void uploadSqtResultBuffer() {
        SQTSearchResultDAO sqtResultDao = daoFactory.getSqtResultDAO();
        sqtResultDao.saveAllSqtResultScores(sqtResultScoresList);
        sqtResultScoresList.clear();
    }
    
    private void flush() {
        if (proteinMatchList.size() > 0) {
            uploadProteinMatchBuffer();
        }
        if (sqtResultScoresList.size() > 0) {
            uploadSqtResultBuffer();
        }
        if (resultModList.size() > 0) {
            uploadResultModBuffer();
        }
    }
    
    public static void deleteSearch(int searchId) {
        MsSearchDAO<MsSearch, MsSearchDb> searchDao = daoFactory.getMsSearchDAO();
        searchDao.deleteSearch(searchId);
    }
    
    private static final class SQTSearchResultScores implements SQTSearchResultScoresDb{

        int resultId;
        int xcorrRank;
        int spRank;
        BigDecimal deltaCN;
        BigDecimal xcorr;
        BigDecimal sp;
        
        public int getResultId() {
            return resultId;
        }

        @Override
        public BigDecimal getDeltaCN() {
            return deltaCN;
        }

        @Override
        public BigDecimal getSp() {
            return sp;
        }

        @Override
        public int getSpRank() {
            return spRank;
        }

        @Override
        public BigDecimal getxCorr() {
            return xcorr;
        }

        @Override
        public int getxCorrRank() {
            return xcorrRank;
        }
    }
}
