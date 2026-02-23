// //og code dont delete this

// // package com.ecommerce.recommendation_service.service;

// // import org.springframework.stereotype.Service;
// // import redis.clients.jedis.JedisPooled;
// // import redis.clients.jedis.search.Query;
// // import redis.clients.jedis.search.SearchResult;

// // import java.util.*;
// // import java.util.regex.Matcher;
// // import java.util.regex.Pattern;

// // @Service
// // public class SearchService {

// //     private final JedisPooled jedis;

// //     public SearchService(JedisPooled jedis) {
// //         this.jedis = jedis;
// //     }

// //     /**
// //      * Entry point for product search — dynamic, NLP-style, plural/singular tolerant.
// //      */
// //     public SearchResult searchProducts(String userQuery) {
// //         try {
// //             if (userQuery == null || userQuery.trim().isEmpty()) {
// //                 userQuery = "*";
// //             }

// //             userQuery = userQuery.toLowerCase();

// //             // --- Extract price filters ---
// //             Double minPrice = null, maxPrice = null;

// //             Matcher under = Pattern.compile("(under|below)\\s+(\\d+)").matcher(userQuery);
// //             if (under.find()) {
// //                 maxPrice = Double.parseDouble(under.group(2));
// //             }

// //             Matcher above = Pattern.compile("(above|over)\\s+(\\d+)").matcher(userQuery);
// //             if (above.find()) {
// //                 minPrice = Double.parseDouble(above.group(2));
// //             }

// //             Matcher between = Pattern.compile("between\\s+(\\d+)\\s+and\\s+(\\d+)").matcher(userQuery);
// //             if (between.find()) {
// //                 minPrice = Double.parseDouble(between.group(1));
// //                 maxPrice = Double.parseDouble(between.group(2));
// //             }

// //             // --- Detect known categories ---
// //             List<String> knownCategories = Arrays.asList(
// //                     "skincare", "haircare", "footwear", "smartphones", "laptops",
// //                     "furniture", "beauty", "clothing", "men", "women",
// //                     "appliances", "electronics"
// //             );

// //             String matchedCategory = knownCategories.stream()
// //                     .filter(userQuery::contains)
// //                     .findFirst()
// //                     .orElse(null);

// //             // --- Detect possible brand (heuristic: capitalized words) ---
// //             String matchedBrand = null;
// //             Matcher brandMatcher = Pattern.compile("\\b([A-Z][a-zA-Z]+)\\b").matcher(userQuery);
// //             if (brandMatcher.find()) {
// //                 matchedBrand = brandMatcher.group(1).toLowerCase();
// //             }

// //             // --- Clean and expand terms (plural/singular) ---
// //             String escaped = userQuery.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("\\s+", " ");
// //             Set<String> expandedWords = expandWords(escaped);
// //             String redisSearchTerms = "(" + String.join(" | ", expandedWords.stream().map(w -> w + "*").toList()) + ")";


// //             // --- Build flexible RediSearch query ---
// //             String[] tokens = userQuery.toLowerCase().split("\\s+");
// // StringBuilder redisQuery = new StringBuilder(
// //     String.format("@name:%s | @description:%s | @brand:%s | @category:%s",
// //                             redisSearchTerms, redisSearchTerms, redisSearchTerms, redisSearchTerms)
// // );

// // for (String token : tokens) {
// //     if (redisQuery.length() > 0) redisQuery.append(" | ");
// //     redisQuery.append("@name:(").append(token).append("*")
// //               .append(" | ").append(token).append("s*")
// //               .append(" | ").append(token).append("es*) ");
// //     redisQuery.append("| @description:(").append(token).append("* | ").append(token).append("s* | ").append(token).append("es*) ");
// //     redisQuery.append("| @brand:(").append(token).append("* | ").append(token).append("s* | ").append(token).append("es*) ");
// //     redisQuery.append("| @category:(").append(token).append("* | ").append(token).append("s* | ").append(token).append("es*) ");
// // }

// //             if (matchedBrand != null) {
// //                 redisQuery.append(String.format(" @brand:(%s*)", matchedBrand));
// //             }
// //             if (matchedCategory != null) {
// //                 redisQuery.append(String.format(" @category:(%s*)", matchedCategory));
// //             }
// //             if (minPrice != null || maxPrice != null) {
// //                 double from = (minPrice != null) ? minPrice : 0;
// //                 double to = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;
// //                 redisQuery.append(String.format(" @price:[%.0f %.0f]", from, to));
// //             }

// //             String finalQuery = redisQuery.toString();
// //             System.out.println("🔍 Redis Query => " + finalQuery);

// //             Query query = new Query(finalQuery)
// //                     .returnFields("name", "brand", "category", "price", "imageUrl", "description")
// //                     .limit(0, 20);

// //             return jedis.ftSearch("idx:products", query);

// //         } catch (Exception e) {
// //             e.printStackTrace();
// //             throw new RuntimeException("Redis search failed", e);
// //         }
// //     }

// //     /**
// //      * Expand each word into singular/plural variations for broader matches.
// //      */
// //     private Set<String> expandWords(String query) {
// //         Set<String> expanded = new LinkedHashSet<>();
// //         for (String word : query.split("\\s+")) {
// //             word = word.trim().toLowerCase();
// //             if (word.isEmpty()) continue;

// //             expanded.add(word);

// //             // Simple plural/singular heuristics
// //             if (word.endsWith("s")) expanded.add(word.substring(0, word.length() - 1));
// //             else expanded.add(word + "s");

// //             if (word.endsWith("es")) expanded.add(word.substring(0, word.length() - 2));
// //             else expanded.add(word + "es");
// //         }
// //         return expanded;
// //     }
// // }






// package com.ecommerce.recommendation_service.service;

// import org.springframework.stereotype.Service;
// import redis.clients.jedis.JedisPooled;
// import redis.clients.jedis.search.Query;
// import redis.clients.jedis.search.SearchResult;

// import java.util.*;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
// import java.util.stream.Collectors;

// /**
//  * Robust SearchService:
//  * - Handles "face wash" vs "facewash" and plural/singular variants
//  * - Builds conservative prefix/wildcard queries (avoids overly-broad fuzzy)
//  * - Parses price tokens: "under 500", "above 1000", "between 200 and 800"
//  */
// @Service
// public class SearchService {

//     private final JedisPooled jedis;
//     private static final double MAX_PRICE_CAP = 1_000_000_000d; // practical infinity

//     public SearchService(JedisPooled jedis) {
//         this.jedis = jedis;
//     }

//     public SearchResult searchProducts(String userQuery) {
//         try {
//             // default query -> all documents
//             if (userQuery == null || userQuery.trim().isEmpty() || userQuery.trim().equals("*")) {
//                 Query q = new Query("*").returnFields("name", "brand", "category", "price", "imageUrl", "description").limit(0, 50);
//                 return jedis.ftSearch("idx:products", q);
//             }

//             // Normalize
//             String raw = userQuery.trim();

//             // Extract price filters (numeric)
//             Double minPrice = null, maxPrice = null;
//             Matcher mUnder = Pattern.compile("\\b(?:under|below)\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
//             if (mUnder.find()) maxPrice = Double.parseDouble(mUnder.group(1));

//             Matcher mAbove = Pattern.compile("\\b(?:above|over)\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
//             if (mAbove.find()) minPrice = Double.parseDouble(mAbove.group(1));

//             Matcher mBetween = Pattern.compile("\\bbetween\\s+(\\d{1,9})\\s+and\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
//             if (mBetween.find()) {
//                 minPrice = Double.parseDouble(mBetween.group(1));
//                 maxPrice = Double.parseDouble(mBetween.group(2));
//             }

//             // Remove numeric price phrases from query text so they don't pollute textual tokens
//             String cleanedForText = raw.replaceAll("(?i)\\b(?:under|below|above|over)\\s+\\d{1,9}\\b", "")
//                                        .replaceAll("(?i)\\bbetween\\s+\\d{1,9}\\s+and\\s+\\d{1,9}\\b", "")
//                                        .trim();

//             // Normalize text: remove special chars except spaces
//             String normalized = cleanedForText.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim().toLowerCase();

//             // Build tokens and variants
//             List<String> tokens = Arrays.stream(normalized.split("\\s+"))
//                     .filter(s -> !s.isBlank())
//                     .collect(Collectors.toList());

//             // Build set of variants (singular/plural/joined/hyphen/underscore)
//             LinkedHashSet<String> variants = new LinkedHashSet<>();

//             // also add original phrase joined variants and the exact phrase
//             String joinedNoSpace = String.join("", tokens);
//             String joinedHyphen = String.join("-", tokens);
//             String joinedUnderscore = String.join("_", tokens);
//             String exactPhrase = String.join(" ", tokens);

//             if (!joinedNoSpace.isBlank()) variants.add(joinedNoSpace);
//             if (!joinedHyphen.isBlank()) variants.add(joinedHyphen);
//             if (!joinedUnderscore.isBlank()) variants.add(joinedUnderscore);
//             if (!exactPhrase.isBlank()) variants.add(exactPhrase);

//             // per-token expansion
//             for (String t : tokens) {
//                 if (t.isEmpty()) continue;
//                 variants.add(t);
//                 // add plural / singular heuristics
//                 if (t.endsWith("s")) {
//                     variants.add(t.substring(0, t.length() - 1)); // shoes -> shoe
//                 } else {
//                     variants.add(t + "s"); // shoe -> shoes
//                 }
//                 if (t.endsWith("es")) {
//                     variants.add(t.substring(0, t.length() - 2));
//                 } else {
//                     variants.add(t + "es");
//                 }
//             }

//             // filter out empty
//             Set<String> cleanVariants = variants.stream().filter(s -> !s.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));

//             // For query building: produce prefix forms for each variant (v*) and also include exact phrase in quotes for name/description
//             // But don't add suffix plurals that create junk; prefix wildcards are the main match tool.
//             List<String> prefixTerms = cleanVariants.stream()
//                     .map(v -> v + "*")
//                     .collect(Collectors.toList());

//             // Also include exact phrase quoted (escaped) to match name tokens order if present.
//             String quotedExactPhrase = "";
//             if (!exactPhrase.isBlank()) {
//                 // escape double quotes if any (shouldn't be)
//                 String escapedPhrase = exactPhrase.replace("\"", "\\\"");
//                 quotedExactPhrase = "\"" + escapedPhrase + "\"";
//             }

//             // Build field clauses: we create an OR across fields, each field gets a parenthesized group
//             // Example: @name:( "face wash" | face* | wash* | facewash* ) | @description:( ... ) | @brand:( ... )
//             String nameGroup = buildFieldGroup("@name", prefixTerms, quotedExactPhrase);
//             String descGroup = buildFieldGroup("@description", prefixTerms, quotedExactPhrase);
//             String brandGroup = buildFieldGroup("@brand", prefixTerms, "");      // brand doesn't need quoted phrase
//             String categoryGroup = buildFieldGroup("@category", prefixTerms, ""); // category too

//             // Combine groups with OR (|)
//             StringBuilder finalQ = new StringBuilder();
//             finalQ.append(nameGroup).append(" | ").append(descGroup).append(" | ").append(brandGroup).append(" | ").append(categoryGroup);

//             // Append category/brand filters if the user explicitly used known tokens like 'skincare' or 'haircare'
//             // We'll check for common category tokens in the normalized tokens and add stricter filters
//             List<String> knownCategories = Arrays.asList("skincare", "haircare", "footwear", "smartphones", "laptops", "furniture", "beauty", "clothing", "men", "women", "appliances", "electronics");
//             for (String cat: knownCategories) {
//                 if (tokens.contains(cat) || normalized.contains(cat)) {
//                     finalQ.append(String.format(" @category:(%s*)", cat));
//                     break;
//                 }
//             }

//             // Price filter
//             if (minPrice != null || maxPrice != null) {
//                 double from = (minPrice != null) ? minPrice : 0d;
//                 double to = (maxPrice != null) ? maxPrice : MAX_PRICE_CAP;
//                 finalQ.append(String.format(" @price:[%.0f %.0f]", from, to));
//             }

//             String redisQueryString = finalQ.toString();
//             System.out.println("🔍 Redis Query => " + redisQueryString);

//             Query query = new Query(redisQueryString)
//                     .returnFields("name", "brand", "category", "price", "imageUrl", "description")
//                     .limit(0, 50);

//             return jedis.ftSearch("idx:products", query);

//         } catch (Exception e) {
//             e.printStackTrace();
//             throw new RuntimeException("Redis search failed", e);
//         }
//     }

//     // Helper builds a parenthesized field group: @field:( "quoted" | v1* | v2* | ... )
//     private String buildFieldGroup(String fieldName, List<String> prefixTerms, String quotedExact) {
//         StringBuilder sb = new StringBuilder();
//         sb.append(fieldName).append(":(");
//         boolean first = true;
//         if (quotedExact != null && !quotedExact.isEmpty()) {
//             sb.append(quotedExact);
//             first = false;
//         }
//         for (String t : prefixTerms) {
//             if (!first) sb.append(" | ");
//             sb.append(t);
//             first = false;
//         }
//         sb.append(")");
//         return sb.toString();
//     }

//     // No change to this: keeps expand logic if needed elsewhere
//     private Set<String> expandWords(String query) {
//         Set<String> expanded = new LinkedHashSet<>();
//         for (String word : query.split("\\s+")) {
//             word = word.trim().toLowerCase();
//             if (word.isEmpty()) continue;

//             expanded.add(word);

//             // Simple plural/singular heuristics
//             if (word.endsWith("s") && word.length() > 1) expanded.add(word.substring(0, word.length() - 1));
//             else expanded.add(word + "s");

//             if (word.endsWith("es") && word.length() > 2) expanded.add(word.substring(0, word.length() - 2));
//             else expanded.add(word + "es");
//         }
//         return expanded;
//     }
// }







//new one trying 

package com.ecommerce.recommendation_service.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// @Service
// public class SearchService {

//     private final JedisPooled jedis;
//     private static final double MAX_PRICE_CAP = 1_000_000_000d;

//     public SearchService(JedisPooled jedis) {
//         this.jedis = jedis;
//     }

//     public SearchResult searchProducts(String userQuery) {
//         try {
//             if (userQuery == null || userQuery.trim().isEmpty() || userQuery.trim().equals("*")) {
//                 Query q = new Query("*").returnFields("name", "brand", "category", "price", "imageUrl", "description").limit(0, 50);
//                 return jedis.ftSearch("idx:products", q);
//             }

//             String raw = userQuery.trim();
//             Double minPrice = null, maxPrice = null;

//             // Price Extraction
//             Matcher mUnder = Pattern.compile("\\b(?:under|below)\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
//             if (mUnder.find()) maxPrice = Double.parseDouble(mUnder.group(1));

//             Matcher mAbove = Pattern.compile("\\b(?:above|over)\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
//             if (mAbove.find()) minPrice = Double.parseDouble(mAbove.group(1));

//             Matcher mBetween = Pattern.compile("\\bbetween\\s+(\\d{1,9})\\s+and\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
//             if (mBetween.find()) {
//                 minPrice = Double.parseDouble(mBetween.group(1));
//                 maxPrice = Double.parseDouble(mBetween.group(2));
//             }

//             // Text Cleaning
//             String cleanedForText = raw.replaceAll("(?i)\\b(?:under|below|above|over|between|and)\\s+\\d{1,9}\\b", "").trim();
//             String normalized = cleanedForText.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim().toLowerCase();

//             List<String> tokens = Arrays.stream(normalized.split("\\s+")).filter(s -> !s.isBlank()).collect(Collectors.toList());
//             LinkedHashSet<String> variants = new LinkedHashSet<>();
            
//             for (String t : tokens) {
//                 variants.add(t + "*"); // Prefix match
//                 if (t.endsWith("s")) variants.add(t.substring(0, t.length() - 1)); 
//                 else variants.add(t + "s");
//             }
//             String redisSearchTerms = variants.stream()
//     .map(t -> t + "* | %" + t + "%") 
//     .collect(Collectors.joining(" | "));
//             // String redisSearchTerms = "(" + String.join(" | ", variants) + ")";

//             // // Building Query Groups
//             // StringBuilder finalq = new StringBuilder();
//             // finalq.append(String.format("(@name:%s | @description:%s | @brand:%s | @category:%s)", 
//             //               redisSearchTerms, redisSearchTerms, redisSearchTerms, redisSearchTerms));

//             // Stricter Category Match
//            StringBuilder finalQ = new StringBuilder();
            
//             if (normalized.isEmpty()) {
//                 finalQ.append("*");
//             } else {
//                 // Generalized version: search for any of the variants across all TEXT fields
//                 // This includes name, brand, category, and description automatically
//                 finalQ.append("(").append(String.join(" | ", variants)).append(")");
//             }

//             // 2. Price filter (Keep this as is, but it's now appended to the global search)
//             if (minPrice != null || maxPrice != null) {
//                 double from = (minPrice != null) ? minPrice : 0d;
//                 double to = (maxPrice != null) ? maxPrice : MAX_PRICE_CAP;
//                 finalQ.append(String.format(" @price:[%.0f %.0f]", from, to));
//             }

//             if (minPrice != null || maxPrice != null) {
//                 double from = (minPrice != null) ? minPrice : 0d;
//                 double to = (maxPrice != null) ? maxPrice : MAX_PRICE_CAP;
//                 finalQ.append(String.format(" @price:[%.0f %.0f]", from, to));
//             }

//             System.out.println("🔍 Redis Query => " + finalQ.toString());
//             Query query = new Query(finalQ.toString())
//                     .returnFields("name", "brand", "category", "price", "imageUrl", "description")
//                     .limit(0, 50);

//             return jedis.ftSearch("idx:products", query);

//         } catch (Exception e) {
//             throw new RuntimeException("Redis search failed", e);
//         }
//     }
@Service
public class SearchService {

    private final JedisPooled jedis;
    private static final double MAX_PRICE_CAP = 1_000_000_000d;

    public SearchService(JedisPooled jedis) {
        this.jedis = jedis;
    }

    public SearchResult searchProducts(String userQuery) {
        try {
            if (userQuery == null || userQuery.trim().isEmpty() || userQuery.trim().equals("*")) {
                Query q = new Query("*").returnFields("name", "brand", "category", "price", "imageUrl", "description").limit(0, 50);
                return jedis.ftSearch("idx:products", q);
            }

            String raw = userQuery.trim();
            Double minPrice = null, maxPrice = null;

            // 1. Price Extraction (Same as your logic)
            Matcher mUnder = Pattern.compile("\\b(?:under|below)\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
            if (mUnder.find()) maxPrice = Double.parseDouble(mUnder.group(1));

            Matcher mAbove = Pattern.compile("\\b(?:above|over)\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
            if (mAbove.find()) minPrice = Double.parseDouble(mAbove.group(1));

            Matcher mBetween = Pattern.compile("\\bbetween\\s+(\\d{1,9})\\s+and\\s+(\\d{1,9})\\b").matcher(raw.toLowerCase());
            if (mBetween.find()) {
                minPrice = Double.parseDouble(mBetween.group(1));
                maxPrice = Double.parseDouble(mBetween.group(2));
            }

            // 2. Text Cleaning
            String cleanedForText = raw.replaceAll("(?i)\\b(?:under|below|above|over|between|and)\\s+\\d{1,9}\\b", "").trim();
            String normalized = cleanedForText.replaceAll("[^a-zA-Z0-9\\s]", " ").replaceAll("\\s+", " ").trim().toLowerCase();

            List<String> tokens = Arrays.stream(normalized.split("\\s+")).filter(s -> !s.isBlank()).collect(Collectors.toList());
            
            // 3. Build Fuzzy & Wildcard Variants
            LinkedHashSet<String> variantSet = new LinkedHashSet<>();
            for (String t : tokens) {
                variantSet.add(t + "*");      // Wildcard (facew*)
                variantSet.add("%" + t + "%"); // Fuzzy (fashwash -> facewash)
                
                // Joined variant for "face wash" -> "facewash"
                if (tokens.size() > 1) {
                    String joined = String.join("", tokens);
                    variantSet.add(joined + "*");
                    variantSet.add("%" + joined + "%");
                }
            }

            String redisSearchTerms = String.join(" | ", variantSet);

            // 4. Building the Final Query
            StringBuilder finalQ = new StringBuilder();
            
            if (normalized.isEmpty()) {
                finalQ.append("*");
            } else {
                // Search across all fields with OR logic
                finalQ.append("(@name:(").append(redisSearchTerms).append(") | ")
                      .append("@category:(").append(redisSearchTerms).append(") | ")
                      .append("@brand:(").append(redisSearchTerms).append("))")
                      .append("@description:(").append(redisSearchTerms).append(") | ");
            }

            // 5. Price filter (Only append ONCE)
            if (minPrice != null || maxPrice != null) {
                double from = (minPrice != null) ? minPrice : 0d;
                double to = (maxPrice != null) ? maxPrice : MAX_PRICE_CAP;
                finalQ.append(String.format(" @price:[%.0f %.0f]", from, to));
            }

            System.out.println("🔍 Final Redis Query => " + finalQ.toString());

            Query query = new Query(finalQ.toString())
                    .returnFields("name", "brand", "category", "price", "imageUrl", "description")
                    .limit(0, 50);
            // Query ke constructor mein hi version specify kar sakte hain agar method nahi mil raha

// Method ki jagah hum query parameter ke taur par bhi dialect pass kar sakte hain
query.addParam("DIALECT", "2");
            return jedis.ftSearch("idx:products", query);

        } catch (Exception e) {
            throw new RuntimeException("Redis search failed: " + e.getMessage(), e);
        }
    }
}
// }




