/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graph_parser;

/**
 *
 * @author MSI
 */
public class BasicExplore {
    KICQ kicq;
    QEG qeg;

    public BasicExplore(KICQ kicq) {
        this.kicq = kicq;
        this.qeg = new QEG(kicq);
        this.solve();
    }
    
    public void solve()
    {
        for(int k=KICQ.k_min; k<=qeg.maxDegree;k++)
        {
            //Find maximal k-core
            //Find set of connected components
            
            //assign score to each connected component
        }
    }
    
}
