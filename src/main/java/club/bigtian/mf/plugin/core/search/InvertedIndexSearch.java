package club.bigtian.mf.plugin.core.search;

import cn.hutool.core.util.StrUtil;

import java.util.*;
import java.util.stream.Collectors;

public class InvertedIndexSearch {
    private static Map<String, Set<String>> INVERTED_INDEX = new HashMap<>();


    /**
     * 传入表名集合，建立倒排索引
     *
     * @param tableNames 表名
     */
    public static void indexText(Collection<String> tableNames) {
        for (String tableName : tableNames) {
            for (int i = 0; i < tableName.length(); i++) {
                char word = tableName.charAt(i);
                INVERTED_INDEX.computeIfAbsent(word + "", k -> new HashSet<>()).add(tableName);
            }
        }
    }

    /**
     * 清空倒排索引
     */
    public  static void clear(){
        INVERTED_INDEX.clear();
    }

    /**
     * 搜索
     *
     * @param keyword 关键字
     * @return {@code Set<String>}
     */
    public static Set<String> search(String keyword) {
        if (StrUtil.isEmpty(keyword)) {
            return INVERTED_INDEX.values().stream()
                    .flatMap(el -> el.stream())
                    .collect(Collectors.toSet());
        }
        keyword = keyword.toLowerCase();
        Set<String> result = new HashSet<>();
        for (int i = 0; i < keyword.length(); i++) {
            char key = keyword.charAt(i);
            result.addAll(INVERTED_INDEX.getOrDefault(key + "", Collections.emptySet()));
        }
        String finalKeyword = keyword;
        result = result.stream()
                .filter(el -> {
                    for (int i = 0; i < finalKeyword.length(); i++) {
                        String key = finalKeyword.charAt(i) + "";
                        if (StrUtil.containsIgnoreCase(el, key)) {
                            el = el.replaceFirst(key, "");
                        } else {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toSet());
        return result;
    }


    public static Map<String, String> highlightKey(String keyword) {

        Set<String> result = search(keyword);
        if (StrUtil.isEmpty(keyword)) {
            return result.stream().collect(Collectors.toMap(el -> el, el -> el));
        }
        //字符串排序

        Map<String, Integer> idxMap = new HashMap<>();
        Map<String, String> highlightMap = new HashMap<>();
        result.stream()
                .forEach(el -> {
                    String finalKeyword = keyword;
                    String htmlText = "<html>";
                    for (int i = 0; i < el.length(); i++) {
                        String key = el.charAt(i) + "";
                        if (StrUtil.containsIgnoreCase(finalKeyword, key)) {
                            htmlText += "<span style='background-color: orange;color:black'>" + key + "</span>";
                            finalKeyword = finalKeyword.replaceFirst(key, "");
                            continue;
                        }
                        htmlText += key;
                    }
                    htmlText += "</html>";
                    idxMap.clear();
                    highlightMap.put(el, htmlText);
                });
        return highlightMap;
    }

}
