import hashtable.CollisionType;
import hashtable.HashFunctionType;
import hashtable.HashTable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SearchEngine {
    private HashTable<String, Article> articleTable;
    private HashTable<String, HashTable<String, Integer>> indexTable;
    private HashTable<String, List<String>> wordDocsTable;
    private HashTable<String, Boolean> stopWordsTable;

    private static final int COL_ID = 0;
    private static final int COL_AUTHOR = 1;
    private static final int COL_DATE = 2;
    private static final int COL_CATEGORY = 3;
    private static final int COL_SECTION = 4;
    private static final int COL_URL = 5;
    private static final int COL_HEADLINE = 6;
    private static final int COL_DESCRIPTION = 7;
    private static final int COL_KEYWORDS = 8;
    private static final int COL_SECOND_HEADLINE = 9;
    private static final int COL_ARTICLE_TEXT = 10;
    private static final String CSV_SPLIT_REGEX =  ",(?=([^\"]*\"[^\"]*\")*[^\"]*$)";

    private static final String DELIMITERS = "[-+=" +
            " " +        // space
            "\r\n " +    // carriage return line feed
            "1234567890" + // numbers
            "’'\"" +       // apostrophe
            "(){}<>\\[\\]" + // brackets
            ":" +        // colon
            "," +        // comma
            "‒–—―" +     // dashes
            "…" +        // ellipsis
            "!" +        // exclamation mark
            "." +        // full stop/period
            "«»" +       // guillemets
            "-‐" +       // hyphen
            "?" +        // question mark
            "‘’“”" +     // quotation marks
            ";" +        // semicolon
            "/" +        // slash/stroke
            "⁄" +        // solidus
            "␠" +        // space?
            "·" +        // interpunct
            "&" +        // ampersand
            "@" +        // at sign
            "*" +        // asterisk
            "\\" +       // backslash
            "•" +        // bullet
            "^" +        // caret
            "¤¢$€£¥₩₪" + // currency
            "†‡" +       // dagger
            "°" +        // degree
            "¡" +        // inverted exclamation point
            "¿" +        // inverted question mark
            "¬" +        // negation
            "#" +        // number sign (hashtag)
            "№" +        // numero sign
            "%‰‱" +      // percent and related signs
            "¶" +        // pilcrow
            "′" +        // prime
            "§" +        // section sign
            "~" +        // tilde/swung dash
            "¨" +        // umlaut/diaeresis
            "_" +        // underscore/understrike
            "|¦" +       // vertical/pipe/broken bar
            "⁂" +        // asterism
            "☞" +        // index/fist
            "∴" +        // therefore sign
            "‽" +        // interrobang
            "※" +        // reference mark
            "]";

    private double loadFactor;
    private HashFunctionType hashFunctionType;
    private CollisionType collisionType;

    //
    private static class ScoreEntry {
        String articleId;
        int score;

        ScoreEntry(String articleId, int score) {
            this.articleId = articleId;
            this.score = score;
        }
    }

    public SearchEngine(double loadFactor,
                        HashFunctionType hashFunctionType,
                        CollisionType collisionType) {
        this.loadFactor = loadFactor;
        this.hashFunctionType = hashFunctionType;
        this.collisionType = collisionType;

        int articleCapacity = 50000;
        int wordCapacity = 5000;

        articleTable = new HashTable<>(articleCapacity, loadFactor, hashFunctionType, collisionType);
        indexTable = new HashTable<>(wordCapacity, loadFactor, hashFunctionType, collisionType);
        wordDocsTable = new HashTable<>(wordCapacity, loadFactor, hashFunctionType, collisionType);
        stopWordsTable = new HashTable<>(wordCapacity, loadFactor, hashFunctionType, collisionType);

        loadStopWords("resources/stop_words_en.txt");
    }

    //reads stop_words_en.txt line by line and loads them into stopWordsTable.
    private void loadStopWords(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String word = line.toLowerCase();
                stopWordsTable.put(word, true);
            }
        } catch (IOException e) {
            System.err.println("Error loading stop words file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    //splits strings in the given text according to the delimiters and removes stop words, empty lines
    //returns the words left put into an array (meaningful String[] array)
    private String[] tokenize(String text) {
        if (text == null || text.isEmpty())
            return new String[0];

        text = text.toLowerCase();

        String[] rawTokens = text.split(DELIMITERS);
        List<String> result = new ArrayList<>();

        for (String token : rawTokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            if (stopWordsTable.contains(token))
                continue;

            result.add(token);
        }
        return result.toArray(new String[0]);
    }

    //reads CNN_Articles.csv
    //creates an Article object for every line of the CNN_Articles.csv
    //loads every article into articleTable
    public void loadArticles(String fileName) {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line = br.readLine(); // header
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(CSV_SPLIT_REGEX, -1);
                if (parts.length <= COL_ARTICLE_TEXT) continue;

                String id = parts[COL_ID].trim();
                String author = parts[COL_AUTHOR].trim();
                String date = parts[COL_DATE].trim();
                String category = parts[COL_CATEGORY].trim();
                String section = parts[COL_SECTION].trim();
                String url = parts[COL_URL].trim();
                String headline = parts[COL_HEADLINE].trim();
                String description = parts[COL_DESCRIPTION].trim();
                String keywords = parts[COL_KEYWORDS].trim();
                String secondHeadline = parts[COL_SECOND_HEADLINE].trim();
                String articleText = parts[COL_ARTICLE_TEXT].trim();

                if (id.isEmpty()) continue;

                Article article = new Article(
                        id, author, date, category, section,
                        url, headline, description, keywords,
                        secondHeadline, articleText
                );

                articleTable.put(id, article);

                String fullText = headline + " " + description + " " +
                        secondHeadline + " " + articleText;

                buildIndexForArticle(id, fullText);

                count++;

                if (count % 7000 == 0) {
                    System.out.println("Şu ana kadar yüklenen makale sayısı: " + count);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("Yükleme tamamlandı, yüklenen article sayısı: " + count);
    }

    //tokenizes the given text
    //for each word updates indexTable : word - (id - that word's count in that id's article)
    //using wordDocsTable, records which articles each word appears in
    private void buildIndexForArticle(String articleId, String text) {
        String[] tokens = tokenize(text);
        if (tokens.length == 0) return;

        for (String word : tokens) {
            HashTable<String, Integer> postings = indexTable.get(word);
            if (postings == null) {
                postings = new HashTable<>(13, loadFactor, hashFunctionType, collisionType);
                indexTable.put(word, postings);
            }

            Integer oldCount = postings.get(articleId);
            if (oldCount == null) {
                postings.put(articleId, 1);

                List<String> docs = wordDocsTable.get(word);
                if (docs == null) {
                    docs = new ArrayList<>();
                    wordDocsTable.put(word, docs);
                }
                docs.add(articleId);
            } else {
                postings.put(articleId, oldCount + 1);
            }
        }
    }

    //returns the article that matches with the searched id
    Article searchById(String id) {
        if (id == null) return null;
        id = id.trim();
        if (id.isEmpty()) return null;
        return articleTable.get(id);
    }

    // searches articles by free-text query and returns up to the top 5 most relevant results
    // if multiple words are searched, it sums their counts per article and returns the top 5 results.
    List<Article> searchByText(String query) {
        List<Article> result = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        String[] tokens = tokenize(query);
        if (tokens.length == 0) {
            return result;
        }
        List<ScoreEntry> scores = new ArrayList<>();

        for (String word : tokens) {
            HashTable<String, Integer> postings = indexTable.get(word);
            List<String> docs = wordDocsTable.get(word);

            if (postings == null || docs == null) {
                continue;
            }

            for (String articleId : docs) {
                Integer count = postings.get(articleId);
                if (count == null) continue;

                ScoreEntry entry = null;
                for (ScoreEntry se : scores) {
                    if (se.articleId.equals(articleId)) {
                        entry = se;
                        break;
                    }
                }
                if (entry == null) {
                    scores.add(new ScoreEntry(articleId, count));
                } else {
                    entry.score += count;
                }
            }
        }

        if (scores.isEmpty()) {
            return result;
        }

        scores.sort((e1, e2) -> Integer.compare(e2.score, e1.score));

        int limit = Math.min(5, scores.size());
        for (int i = 0; i < limit; i++) {
            ScoreEntry entry = scores.get(i);
            Article article = articleTable.get(entry.articleId);
            if (article != null) {
                result.add(article);
            }
        }
        return result;
    }

    //runs all search queries from the given file and returns the average search time in milliseconds
    //t1-t0 total elapsed time
    double runSearchTest(String path) {
        long totalTime = 0;
        int queryCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String query = line.trim();
                if (query.isEmpty())
                    continue;

                long t0 = System.nanoTime();
                searchByText(query);
                long t1 = System.nanoTime();

                totalTime += (t1 - t0);
                queryCount++;
            }
        } catch (IOException e) {
            System.err.println("Error reading search file: " + e.getMessage());
            e.printStackTrace();
        }

        if (queryCount == 0)
            return 0.0;

        double avgNs = (double) totalTime / queryCount;
        return avgNs / 1_000_000.0; // ms
    }

    //runs performance tests for all configurations and prints a summary table of collisions and timings
    //print table header for all test configurations
    //t1-t0 total elapsed time
    void runAllTests() {
        String articlesPath = "resources/CNN_Articels.csv";
        String queriesPath = "resources/search.txt";

        double[] loadFactors = {0.5, 0.8};
        HashFunctionType[] hashFunctions = {
                HashFunctionType.SSF,
                HashFunctionType.PAF
        };
        CollisionType[] collisionTypes = {
                CollisionType.LINEAR_PROBING,
                CollisionType.DOUBLE_HASHING
        };

        System.out.println("============================================================");
        System.out.println("Performance tests on CNN_Articles + search.txt");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-8s %-4s %-3s %-15s %-15s %-15s%n",
                "alpha", "HF", "CH", "Collisions", "IndexTime(ms)", "AvgSearch(ms)");
        System.out.println("------------------------------------------------------------");

        for (double alpha : loadFactors) {
            for (HashFunctionType hf : hashFunctions) {
                for (CollisionType ct : collisionTypes) {

                    SearchEngine engine = new SearchEngine(alpha, hf, ct);

                    engine.indexTable.resetCollisionCount();
                    long t0 = System.currentTimeMillis();
                    engine.loadArticles(articlesPath);
                    long t1 = System.currentTimeMillis();
                    long indexTime = t1 - t0;
                    long collisions = engine.indexTable.getCollisionCount();
                    double avgSearch = engine.runSearchTest(queriesPath);

                    String hfName = (hf == HashFunctionType.SSF) ? "SSF" : "PAF";
                    String ctName = (ct == CollisionType.LINEAR_PROBING) ? "LP" : "DH";

                    System.out.printf("%-8.2f %-4s %-3s %-15d %-15d %-15.3f%n",
                            alpha, hfName, ctName, collisions, indexTime, avgSearch);
                }
            }
        }
        System.out.println("============================================================");
    }
    //builds the search engine indexes the dataset and runs an interactive search loop
    // keep each query word only once for reporting
    // accumulate total matches for all query words
    public static void main(String[] args) {
        double loadFactor = 0.5;
        HashFunctionType hashFunc = HashFunctionType.PAF;
        CollisionType collision = CollisionType.LINEAR_PROBING;

        SearchEngine engine = new SearchEngine(loadFactor, hashFunc, collision);

        long t0 = System.currentTimeMillis();
        engine.loadArticles("resources/CNN_Articels.csv");
        long t1 = System.currentTimeMillis();
        System.out.println("Indexing finished in " + (t1 - t0) + " ms\n");
        System.out.println("============================================================\n");

        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Choose an action:");
                System.out.println(" - Search by ID   :  id:ARTICLE_ID   (example: id:C85M3HK8UZ)");
                System.out.println(" - Search by text :  type any words  (example: data structure)");
                System.out.println(" - Run tests      :  test");
                System.out.println(" - Exit           :  exit");
                System.out.print("> ");

                String query = scanner.nextLine().trim();

                if (query.equalsIgnoreCase("exit")) {
                    System.out.println("Bye!");
                    break;
                }
                if (query.isEmpty()) {
                    System.out.println("Please type something.\n");
                    continue;
                }
                if (query.equalsIgnoreCase("test")) {
                    System.out.println();
                    engine.runAllTests();
                    System.out.println();
                    continue;
                }

                if (query.toLowerCase().startsWith("id:")) {
                    String id = query.substring(3).trim();
                    Article article = engine.searchById(id);
                    System.out.println("------------------------------------------------------------");
                    if (article == null) {
                        System.out.println("No article found with id = " + id);
                    } else {
                        System.out.println("Article found:");
                        System.out.println(" id       : " + article.getId());
                        System.out.println(" headline : " + article.getHeadline());
                        System.out.println(" author   : " + article.getAuthor());
                        System.out.println(" date     : " + article.getDate());
                    }
                    System.out.println("------------------------------------------------------------\n");
                } else {
                    long q0 = System.nanoTime();
                    List<Article> results = engine.searchByText(query);
                    long q1 = System.nanoTime();
                    double timeMs = (q1 - q0) / 1_000_000.0;

                    String[] tokens = engine.tokenize(query);
                    List<String> queryWords = new ArrayList<>();
                    for (String w : tokens) {
                        if (!queryWords.contains(w)) {
                            queryWords.add(w);
                        }
                    }

                    System.out.println("============================================================");
                    System.out.println("Query: \"" + query + "\"");

                    if (results == null || results.isEmpty()) {
                        System.out.println("No results found.");
                        System.out.printf("Search time: %.3f ms%n", timeMs);
                        System.out.println("============================================================\n");
                        continue;
                    }

                    System.out.println("Top " + results.size()
                            + " results (ranked by total occurrences of all query words):");
                    System.out.println("------------------------------------------------------------");

                    int rank = 1;
                    for (Article article : results) {
                        String articleId = article.getId();
                        int totalCount = 0;
                        StringBuilder perWord = new StringBuilder();

                        for (int i = 0; i < queryWords.size(); i++) {
                            String w = queryWords.get(i);
                            HashTable<String, Integer> postings = engine.indexTable.get(w);
                            int c = 0;
                            if (postings != null) {
                                Integer count = postings.get(articleId);
                                if (count != null) {
                                    c = count;
                                }
                            }
                            totalCount += c;
                            perWord.append(w).append("=").append(c);
                            if (i < queryWords.size() - 1) {
                                perWord.append(", ");
                            }
                        }

                        System.out.println("#" + rank + " | id=" + articleId);
                        System.out.println("   headline        : " + article.getHeadline());
                        System.out.println("   total matches   : " + totalCount);
                        System.out.println("   per-word counts : " + perWord);
                        System.out.println("------------------------------------------------------------");
                        rank++;
                    }

                    System.out.printf("Search time: %.3f ms%n", timeMs);
                    System.out.println("============================================================\n");
                }
            }
        }
    }
}
