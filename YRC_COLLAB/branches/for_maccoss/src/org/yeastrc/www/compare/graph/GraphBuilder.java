/**
 * GraphBuilder.java
 * @author Vagisha Sharma
 * May 16, 2009
 * @version 1.0
 */

package org.yeastrc.www.compare.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.yeastrc.www.compare.ComparisonProtein;

import edu.uwpr.protinfer.database.dao.ProteinferDAOFactory;
import edu.uwpr.protinfer.database.dao.ibatis.ProteinferProteinDAO;
import edu.uwpr.protinfer.infer.Peptide;
import edu.uwpr.protinfer.infer.graph.BipartiteGraph;
import edu.uwpr.protinfer.infer.graph.InvalidVertexException;
import edu.uwpr.protinfer.infer.graph.PeptideVertex;

public class GraphBuilder {

    private static final Logger log = Logger.getLogger(GraphBuilder.class);
    
    private ProteinferProteinDAO protDao = ProteinferDAOFactory.instance().getProteinferProteinDao();
    
    public BipartiteGraph<ComparisonProteinGroup, PeptideVertex> 
        buildGraph(List<ComparisonProtein> proteins, List<Integer> pinferIds) {
        
        Map<String, Peptide> peptideIdMap = new HashMap<String, Peptide>();
        int peptideId = 1;
        
        BipartiteGraph<ComparisonProteinGroup, PeptideVertex> graph = 
                new BipartiteGraph<ComparisonProteinGroup, PeptideVertex>();
        
        for (ComparisonProtein protein: proteins) {
            
            List<String> peptides = getPeptides(protein.getNrseqId(), pinferIds);
            protein.setMaxPeptideCount(peptides.size());
            
            for(String peptide: peptides) {
                Peptide pept = peptideIdMap.get(peptide);
                if(pept == null) {
                   pept = new Peptide(peptide, peptide, peptideId++);
                   peptideIdMap.put(peptide, pept);
                }
                PeptideVertex peptVertex = new PeptideVertex(pept);
                ComparisonProteinGroup proteinGroup = new ComparisonProteinGroup(protein);
                try {
                    if (!graph.addEdge(proteinGroup, peptVertex)) {
                        log.warn("Could not add edge between: "+proteinGroup.getLabel()+
                                " to "+peptVertex.getPeptideStringLabel());
                    }
                }
                catch (InvalidVertexException e) {
                    e.printStackTrace();
                }
            }
        }
        return graph;
    }

    private List<String> getPeptides(int nrseqId, List<Integer> pinferIds) {
        return protDao.getPeptidesForProtein(nrseqId, pinferIds);
    }
}
