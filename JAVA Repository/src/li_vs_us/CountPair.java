/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

/**
 *
 * @author MSI
 */
public class CountPair {
    int paperCount;
    long citationCount;

    public CountPair(int paperCount, long citationCount) {
        this.paperCount = paperCount;
        this.citationCount = citationCount;
    }

    public CountPair() {
    }

    public int getPaperCount() {
        return paperCount;
    }

    public void setPaperCount(int paperCount) {
        this.paperCount = paperCount;
    }
    
    public void incPaperCount()
    {
        this.paperCount++;
    }

    public Long getCitationCount() {
        return citationCount;
    }

    public void setCitationCount(Long citationCount) {
        this.citationCount = citationCount;
    }
    
    public void addCitationCount(Long additionalCount)
    {
        this.citationCount+=additionalCount;
    }
    
    
}
