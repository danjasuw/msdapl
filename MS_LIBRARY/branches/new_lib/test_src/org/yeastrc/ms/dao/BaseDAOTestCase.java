/**
 * BaseDAOTestCase.java
 * @author Vagisha Sharma
 * Jul 4, 2008
 * @version 1.0
 */
package org.yeastrc.ms.dao;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.yeastrc.ms.dto.MsDigestionEnzyme;
import org.yeastrc.ms.dto.MsPeptideSearch;
import org.yeastrc.ms.dto.MsPeptideSearchResult;
import org.yeastrc.ms.dto.MsProteinMatch;
import org.yeastrc.ms.dto.MsSearchDynamicMod;
import org.yeastrc.ms.dto.MsSearchMod;
import org.yeastrc.ms.dto.MsSearchResultDynamicMod;
import org.yeastrc.ms.dto.MsSequenceDatabase;

import junit.framework.TestCase;

/**
 * 
 */
public class BaseDAOTestCase extends TestCase {

    protected MsPeptideSearchDAO searchDao = DAOFactory.instance().getMsPeptideSearchDAO();
    protected MsPeptideSearchResultDAO resultDao = DAOFactory.instance().getMsPeptideSearchResultDAO();
    protected MsSequenceDatabaseDAO seqDbDao = DAOFactory.instance().getMsSequenceDatabaseDAO();
    protected MsSearchModDAO modDao = DAOFactory.instance().getMsSearchModDAO();
    protected MsProteinMatchDAO matchDao = DAOFactory.instance().getMsProteinMatchDAO();
    protected MsDigestionEnzymeDAO enzymeDao = DAOFactory.instance().getEnzymeDAO();

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }



    protected MsSequenceDatabase makeSequenceDatabase(String serverAddress, String serverPath,
            Integer seqLength, Integer proteinCount) {
        MsSequenceDatabase db = new MsSequenceDatabase();
        if (serverAddress != null)
            db.setServerAddress(serverAddress);
        if (serverPath != null)
            db.setServerPath(serverPath);
        if (seqLength != null)
            db.setSequenceLength(seqLength);
        if (proteinCount != null)
            db.setProteinCount(proteinCount);
        return db;
    }

    protected MsPeptideSearchResult makeSearchResult(int searchId, int scanId, int charge,
            String peptide, boolean addPrMatch, boolean addDynaMod) {

        MsPeptideSearchResult result = makeSearchResult(searchId, scanId, charge, peptide);

        // add protein matches
        if (addPrMatch)     addProteinMatches(result);

        // add dynamic modifications
        if (addDynaMod)     addResultDynamicModifications(result, searchId);

        return result;
    }

    protected MsPeptideSearchResult makeSearchResult(int searchId, int scanId, int charge, String peptide) {
        MsPeptideSearchResult result = new MsPeptideSearchResult();
        result.setSearchId(searchId);
        result.setScanId(scanId);
        result.setCharge(charge);
        result.setPeptide(peptide);

        return result;
    }

    protected void addProteinMatches(MsPeptideSearchResult result) {
        MsProteinMatch match1 = new MsProteinMatch();
        match1.setAccession("Accession_"+result.getPeptide()+"_1");
        match1.setDescription("Description_"+result.getPeptide()+"_1");

        result.addProteinMatch(match1);

        MsProteinMatch match2 = new MsProteinMatch();
        match2.setAccession("Accession_"+result.getPeptide()+"_2");

        result.addProteinMatch(match2);
    }

    protected void addResultDynamicModifications(MsPeptideSearchResult result, int searchId) {

        List<MsSearchDynamicMod> dynaMods = modDao.loadDynamicModificationsForSearch(searchId);

        List<MsSearchResultDynamicMod> resultDynaMods = new ArrayList<MsSearchResultDynamicMod>(dynaMods.size());
        int pos = 1;
        for (MsSearchDynamicMod mod: dynaMods) {
            MsSearchResultDynamicMod resMod = new MsSearchResultDynamicMod();
            resMod.setModificationId(mod.getId());
            resMod.setModificationMass(mod.getModificationMass());
            resMod.setModificationPosition(pos++);
            resMod.setModificationSymbol(mod.getModificationSymbol());
            resMod.setModifiedResidue(mod.getModifiedResidue());
            resultDynaMods.add(resMod);
        }

        result.setDynamicModifications(resultDynaMods);
    }

    protected MsSearchMod makeStaticMod(Integer searchId, char modChar, String modMass) {
        MsSearchMod mod = new MsSearchMod();
        if (searchId != null)
            mod.setSearchId(searchId);
        mod.setModifiedResidue(modChar);
        mod.setModificationMass(new BigDecimal(modMass));
        return mod;
    }

    protected MsSearchDynamicMod makeDynamicMod(Integer searchId, char modChar, String modMass,
            char modSymbol) {
        MsSearchDynamicMod mod = new MsSearchDynamicMod();
        if (searchId != null)
            mod.setSearchId(searchId);
        mod.setModifiedResidue(modChar);
        mod.setModificationMass(new BigDecimal(modMass));
        mod.setModificationSymbol(modSymbol);
        return mod;
    }

    protected MsPeptideSearch makePeptideSearch(int runId, boolean addSeqDb,
            boolean addStaticMods, boolean addDynaMods) {

        MsPeptideSearch search = new MsPeptideSearch();
        search.setRunId(runId);
        search.setOriginalFileType("SQT");
        search.setSearchEngineName("Sequest");
        search.setSearchEngineVersion("1.0");
        long startTime = getTime("01/29/2008, 03:34 AM", false);
        long endTime = getTime("01/29/2008, 06:21 AM", false);
        search.setSearchDate(new Date(getTime("01/29/2008, 03:34 AM", true)));
        search.setSearchDuration(searchTimeMinutes(startTime, endTime));
        search.setPrecursorMassType("AVG");
        search.setPrecursorMassTolerance(new BigDecimal("3.000"));
        search.setFragmentMassType("MONO");
        search.setFragmentMassTolerance(new BigDecimal("0.0"));

        if (addSeqDb) {
            MsSequenceDatabase db1 = makeSequenceDatabase("serverAddress", "path1", 100, 20);
            MsSequenceDatabase db2 = makeSequenceDatabase("serverAddress", "path2", 200, 40);
            search.addSearchDatabase(db1);
            search.addSearchDatabase(db2);
        }

        if (addStaticMods) {
            MsSearchMod mod1 = makeStaticMod(null, 'C', "50.0");
            MsSearchMod mod2 = makeStaticMod(null, 'S', "80.0");
            List<MsSearchMod> staticMods = new ArrayList<MsSearchMod>(2);
            staticMods.add(mod1);
            staticMods.add(mod2);
            search.setStaticModifications(staticMods);
        }

        if (addDynaMods) {
            MsSearchDynamicMod dmod1 = makeDynamicMod(null, 'A', "10.0", '*');
            MsSearchDynamicMod dmod2 = makeDynamicMod(null, 'B', "20.0", '#');
            MsSearchDynamicMod dmod3 = makeDynamicMod(null, 'C', "30.0", '@');
            List<MsSearchDynamicMod> dynaMods = new ArrayList<MsSearchDynamicMod>(2);
            dynaMods.add(dmod1);
            dynaMods.add(dmod2);
            dynaMods.add(dmod3);
            search.setDynamicModifications(dynaMods);
        }

        return search;
    }
    
    protected int  searchTimeMinutes(long startTime, long endTime) {
        assertTrue(endTime > startTime);
        return (int)((endTime - startTime)/(1000*60));
    }

    /**
     * date/time string should look like: 01/29/2008, 03:34 AM
     * @param string
     * @param justDate
     * @return
     */
    protected long getTime(String string, boolean justDate) {
        // example: 01/29/2008, 03:34 AM
        Calendar cal = GregorianCalendar.getInstance();
        string = string.replaceAll("\\s", "");
        String[] tok = string.split(",");
        String date = tok[0];
        String time = tok[1];
        
        String[] dateTok = date.split("\\/");
        cal.set(Calendar.MONTH, Integer.valueOf(dateTok[0]));
        cal.set(Calendar.DATE, Integer.valueOf(dateTok[1]));
        cal.set(Calendar.YEAR, Integer.valueOf(dateTok[2]));
        
        if (justDate) {
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        else {
            String ampm = time.substring(time.length() - 2, time.length());
            String justTime = time.substring(0, time.length() -2);

            String[] justTimeTok = justTime.split(":");
            cal.set(Calendar.AM_PM, (ampm.equalsIgnoreCase("AM") ?  Calendar.AM : Calendar.PM));
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(justTimeTok[0]));
            cal.set(Calendar.MINUTE, Integer.parseInt(justTimeTok[1]));
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
        }
        return cal.getTimeInMillis();
    }

    protected MsDigestionEnzyme makeDigestionEnzyme(String name, int sense,
            String cut, String nocut) {
                MsDigestionEnzyme enzyme;
                enzyme = new MsDigestionEnzyme();
                enzyme.setName(name);
                enzyme.setCut(cut);
                enzyme.setSense((short)sense);
                enzyme.setNocut(nocut);
                return enzyme;
            }
}
