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
     * 搜索
     *
     * @param keyword 关键字
     * @return {@code Set<String>}
     */
    public static Set<String> search(String keyword) {
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

    public static void main(String[] args) {
        List<String> tableName = Arrays.asList("t_crm_contract", "t_crm_contract_order", "t_crm_returns", "t_crm_returns_detail", "t_crm_baseclient", "t_crm_baseorg");
        indexText(tableName);
        highlightKey("tcttar").forEach(System.out::println);
    }

    public static List<String> highlightKey(String keyword) {
        keyword = keyword.toLowerCase();
        Set<String> result = search(keyword);
        String finalKeyword = keyword;
        Map<String, Integer> idxMap = new HashMap<>();
        List<String> list = result.stream()
                .map(el -> {
                    String htmlText = "<html>";
                    for (int i = 0; i < finalKeyword.length(); i++) {
                        String key = finalKeyword.charAt(i) + "";
                        int idx = el.indexOf(key, idxMap.getOrDefault(key, Integer.valueOf(0)).intValue());
                        htmlText += el.substring(0, idx);
                        htmlText += "<span style='background-color: orange;color:black'>";
                        htmlText += el.substring(idx, idx + 1);
                        htmlText += "</span>";
                        idxMap.put(key, idx);
                    }
                    htmlText += "</html>";
                    idxMap.clear();
                    return htmlText;
                }).collect(Collectors.toList());

        return list;
    }

}
