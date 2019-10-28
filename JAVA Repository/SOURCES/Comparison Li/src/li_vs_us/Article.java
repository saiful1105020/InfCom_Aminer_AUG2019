/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package li_vs_us;

import java.util.ArrayList;

/**
 *
 * @author MSI
 */
public class Article {
    private String id;
    private ArrayList<Integer> authors = new ArrayList<Integer>();
    private ArrayList<Integer> keywords = new ArrayList<Integer>();
    private Long citations;

    public Long getCitations() {
        return citations;
    }

    public void setCitations(Long citations) {
        this.citations = citations;
    }

    public Article(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Integer> getAuthors() {
        return authors;
    }

    public void setAuthors(ArrayList<Integer> authors) {
        this.authors = authors;
    }
    
    public void addAuthor(int authorId)
    {
        if(authorId<=Constants.MAX_AUTH_ID && (!this.authors.contains(authorId)))
        {
            this.authors.add(authorId);
        }
    }

    public ArrayList<Integer> getKeywords() {
        return keywords;
    }

    public void setKeywords(ArrayList<Integer> keywords) {
        this.keywords = keywords;
    }
    
    public void addKeyword(Integer keywordId)
    {
        if(keywordId<Constants.NUM_KEYWORDS)
        {
            this.keywords.add(keywordId);
        }
    }

    @Override
    public String toString() {
        return "Article{" + "id=" + id + ", authors=" + authors + ", keywords=" + keywords + ", citations=" + citations + '}';
    }
    
}
