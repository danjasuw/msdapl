package org.yeastrc.ms.dao;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import junit.framework.TestCase;

import org.yeastrc.ms.dto.MsDigestionEnzyme;
import org.yeastrc.ms.dto.MsRun;
import org.yeastrc.ms.dto.MsRunWithEnzymeInfo;

public class MsRunDAOImplTest extends TestCase {

    private MsRunDAO runDao;
    private static final int msExperimentId_1 = 1;
    private static final int msExperimentId_2 = 25;
    
    protected void setUp() throws Exception {
        super.setUp();
        runDao = DAOFactory.instance().getMsRunDAO();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        runDao.deleteRunsForExperiment(msExperimentId_1);
        runDao.deleteRunsForExperiment(msExperimentId_2);
    }
    
    public void testLoadRunIdsForExperiment() {
        List<Integer> runIdList = runDao.loadRunIdsForExperiment(msExperimentId_1);
        assertEquals(0, runIdList.size());
        
        MsRun run = createRun(msExperimentId_1);
        runDao.saveRun(run);
        
        runIdList = runDao.loadRunIdsForExperiment(msExperimentId_1);
        assertEquals(1, runIdList.size());
        
        // create a run with a different experiment id and save it
        run = createRun(msExperimentId_2);
        runDao.saveRun(run);
        
        // make sure there is only one run with our original experiment id
        runIdList = runDao.loadRunIdsForExperiment(msExperimentId_1);
        assertEquals(1, runIdList.size());
        run = runDao.loadRun(runIdList.get(0));
        assertEquals(msExperimentId_1, run.getMsExperimentId());
        
        // make sure there is only 1 run with the other experiment id
        runIdList = runDao.loadRunIdsForExperiment(msExperimentId_2);
        assertEquals(1, runIdList.size());
        run = runDao.loadRun(runIdList.get(0));
        assertEquals(msExperimentId_2, run.getMsExperimentId());
    }

    public void testLoadRunsForExperiment() {
        MsRun run = createRun(msExperimentId_1);
        runDao.saveRun(run);
        List <MsRun> runs = runDao.loadExperimentRuns(msExperimentId_1);
        assertEquals(1, runs.size());
        run = runs.get(0);
        checkRun(run);
    }
    
    public void testLoad() {
        MsRun run = createRun(msExperimentId_1);
        runDao.saveRun(run);
        List<Integer> runIdList = runDao.loadRunIdsForExperiment(msExperimentId_1);
        run = runDao.loadRun(runIdList.get(0));
        checkRun(run);
    }

    private void checkRun(MsRun run) {
        assertEquals(msExperimentId_1, run.getMsExperimentId());
        assertEquals(MsRun.RunFileFormat.MS2.toString(), run.getFileFormat());
        assertEquals("my_file1.ms2", run.getFileName());
        assertEquals("Data dependent", run.getAcquisitionMethod());
        assertEquals("Dummy run", run.getComment());
        assertEquals("ms2Convert", run.getConversionSW());
        assertEquals("1.0", run.getConversionSWVersion());
        assertEquals("options string", run.getConversionSWOptions());
        assertEquals("profile", run.getDataType());
        assertEquals("ETD", run.getInstrumentModel());
        assertEquals("Thermo", run.getInstrumentVendor());
        assertNull(run.getInstrumentSN());
        assertEquals("sha1sum", run.getSha1Sum());
    }

    public void testLoadRunsForFileNameAndSha1Sum() {
        MsRun run = createRun(msExperimentId_1);
        runDao.saveRun(run);
        run = createRun(msExperimentId_2);
        runDao.saveRun(run);
        
        List<MsRun> runs = runDao.loadRuns("my_file1.ms2", "sha1sum");
        assertEquals(2, runs.size());
        Collections.sort(runs, new Comparator<MsRun>() {
            @Override
            public int compare(MsRun o1, MsRun o2) {
                return new Integer(o1.getMsExperimentId()).compareTo(new Integer(o2.getMsExperimentId())); 
            }});
        assertEquals(msExperimentId_1, runs.get(0).getMsExperimentId());
        assertEquals(msExperimentId_2, runs.get(1).getMsExperimentId());
    }
    
    
    public void testDeleteRunsForExperiment() {
        MsRun run = createRun(msExperimentId_1);
        runDao.saveRun(run);
        run = createRun(msExperimentId_2);
        runDao.saveRun(run);
        
        int origSize = runDao.loadRunIdsForExperiment(msExperimentId_1).size();
        assertTrue(origSize > 0);
        // use a different experiment id from what we have in the database
        runDao.deleteRunsForExperiment(msExperimentId_2); 
        assertEquals(origSize, runDao.loadRunIdsForExperiment(msExperimentId_1).size());
        
        runDao.deleteRunsForExperiment(msExperimentId_1); 
        assertEquals(0, runDao.loadRunIdsForExperiment(msExperimentId_1).size());
    }
    
    public void testSaveAndLoadRunWithEnzymeInfo() {
        MsRunWithEnzymeInfo run = createRunWEnzymeInfo(msExperimentId_1);
        
        // get a list of the enzymes currently in the database
        MsDigestionEnzymeDAO enzymeDao = DAOFactory.instance().getEnzymeDAO();
        MsDigestionEnzyme enzyme1 = enzymeDao.loadEnzyme(1);
        assertNotNull(enzyme1);
        MsDigestionEnzyme enzyme2 = enzymeDao.loadEnzyme(2);
        assertNotNull(enzyme2);
        
        // add the enzymes to the run
        run.addEnzyme(enzyme1);
        run.addEnzyme(enzyme2);
        
        // save the run
        int runId_1 = runDao.saveRunWithEnzymeInfo(run);
        
        // now read back the run and make sure it has the enzyme information
        MsRunWithEnzymeInfo runFromDb = runDao.loadRunWithEmzymeInfo(runId_1);
        List<MsDigestionEnzyme> enzymes = runFromDb.getEnzymeList();
        assertNotNull(enzymes);
        assertEquals(2, enzymes.size());
        
        // save another run for this experiment
        run = createRunWEnzymeInfo(msExperimentId_1);
        MsDigestionEnzyme enzyme3 = enzymeDao.loadEnzyme(3);
        assertNotNull(enzyme3);
        MsDigestionEnzyme enzyme4 = enzymeDao.loadEnzyme(4);
        assertNotNull(enzyme4);
        
        // add the enzymes to the run
        run.addEnzyme(enzyme3);
        run.addEnzyme(enzyme4);
        
        // save the run
        int runId_2 = runDao.saveRunWithEnzymeInfo(run);
        
        List<MsRunWithEnzymeInfo> runsWenzymes = runDao.loadExperimentRunsWithEnzymeInfo(msExperimentId_1);
        assertEquals(2, runsWenzymes.size());
        
        // make sure the enzymes associated with the runs are right;
        List<MsDigestionEnzyme> enzymes_1 = runsWenzymes.get(0).getEnzymeList();
        assertEquals(2, enzymes_1.size());
        assertEquals(enzyme1.getName(), enzymes_1.get(0).getName());
        assertEquals(enzyme2.getName(), enzymes_1.get(1).getName());
        
        List<MsDigestionEnzyme> enzymes_2 = runsWenzymes.get(1).getEnzymeList();
        assertEquals(2, enzymes_2.size());
        assertEquals(enzyme3.getName(), enzymes_2.get(0).getName());
        assertEquals(enzyme4.getName(), enzymes_2.get(1).getName());
        
    }
    
    public void testDeleteRunsWithEnzymeInfo() {
        
        MsRunWithEnzymeInfo run = createRunWEnzymeInfo(msExperimentId_1);
        
        // get a list of the enzymes currently in the database
        MsDigestionEnzymeDAO enzymeDao = DAOFactory.instance().getEnzymeDAO();
        MsDigestionEnzyme enzyme1 = enzymeDao.loadEnzyme(1);
        assertNotNull(enzyme1);
        MsDigestionEnzyme enzyme2 = enzymeDao.loadEnzyme(2);
        assertNotNull(enzyme2);
        
        // add the enzymes to the run
        run.addEnzyme(enzyme1);
        run.addEnzyme(enzyme2);
        
        // save the run
        int runId = runDao.saveRunWithEnzymeInfo(run);
        
        // make sure the run and associated enzyme information got saved
        List<MsRunWithEnzymeInfo> runsWenzymes = runDao.loadExperimentRunsWithEnzymeInfo(msExperimentId_1);
        assertEquals(1, runsWenzymes.size());
        List<MsDigestionEnzyme> enzymes = enzymeDao.loadEnzymesForRun(runId);
        assertEquals(2, enzymes.size());
        
        // now delete the run
        runDao.deleteRunsForExperiment(msExperimentId_1);
        
        // make sure the run and associated enzyme information got deleted
        runsWenzymes = runDao.loadExperimentRunsWithEnzymeInfo(msExperimentId_1);
        assertEquals(0, runsWenzymes.size());
        enzymes = enzymeDao.loadEnzymesForRun(runId);
        assertEquals(0, enzymes.size());
    }

    private MsRunWithEnzymeInfo createRunWEnzymeInfo(int msExperimentId) {
        MsRunWithEnzymeInfo run = new MsRunWithEnzymeInfo();
        run.setMsExperimentId(msExperimentId);
        run.setFileFormat(MsRun.RunFileFormat.MS2.toString());
        run.setFileName("my_file1.ms2");
        run.setAcquisitionMethod("Data dependent");
        run.setComment("Dummy run");
        run.setConversionSW("ms2Convert");
        run.setConversionSWVersion("1.0");
        run.setConversionSWOptions("options string");
        run.setDataType("profile");
        run.setInstrumentModel("ETD");
        run.setInstrumentVendor("Thermo");
        run.setSha1Sum("sha1sum");
        return run;
    }
    
    
    private MsRun createRun(int msExperimentId) {
        MsRun run = new MsRun();
        run.setMsExperimentId(msExperimentId);
        run.setFileFormat(MsRun.RunFileFormat.MS2.toString());
        run.setFileName("my_file1.ms2");
        run.setAcquisitionMethod("Data dependent");
        run.setComment("Dummy run");
        run.setConversionSW("ms2Convert");
        run.setConversionSWVersion("1.0");
        run.setConversionSWOptions("options string");
        run.setDataType("profile");
        run.setInstrumentModel("ETD");
        run.setInstrumentVendor("Thermo");
        run.setSha1Sum("sha1sum");
        return run;
    }
}
