package org.yeastrc.ms.dao;

import junit.framework.Test;
import junit.framework.TestSuite;

public class DAOTestSuite {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for org.yeastrc.ms.dao");
        //$JUnit-BEGIN$
        suite.addTestSuite(MsExperimentDAOImplTest.class);
        suite.addTestSuite(MsDigestionEnzymeDAOImplTest.class);
        suite.addTestSuite(MsRunDAOImplTest.class);
        //$JUnit-END$
        return suite;
    }

}
