public class Article {
    private String id;
    private String author;
    private String date;
    private String category;
    private String section;
    private String url;
    private String headline;
    private String description;
    private String keywords;
    private String headline2;
    private String content;

    Article(String id, String author, String date, String category, String section, String url, String headline, String description, String keywords, String headline2, String content) {
        this.id = id;
        this.author = author;
        this.date = date;
        this.category = category;
        this.section = section;
        this.url = url;
        this.headline = headline;
        this.description = description;
        this.keywords = keywords;
        this.headline2 = headline2;
        this.content = content;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public String getSection() {
        return section;
    }
    public void setSection(String section) {
        this.section = section;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getHeadline() {
        return headline;
    }
    public void setHeadline(String headline) {
        this.headline = headline;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getKeywords() {
        return keywords;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
    public String getHeadline2() {
        return headline2;
    }
    public void setHeadline2(String headline2) {
        this.headline2 = headline2;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String toString(){
        return "\n\nid=" + id + "\nauthor=" + author + "\ndate=" + date + "\ncategory=" + category + "\nsection=" + section + "\nurl=" + url + "\nheadline=" + headline + "\ndescription=" + description + "\nkeywords=" + keywords + "\nheadline2=" + headline2 + "\ncontent=" + content;
    }


}
