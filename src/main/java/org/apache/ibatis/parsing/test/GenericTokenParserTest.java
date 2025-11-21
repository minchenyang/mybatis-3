package org.apache.ibatis.parsing.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.TokenHandler;

public class GenericTokenParserTest {

  public static void main(String[] args) {
    // 案例 1: 简单的变量替换 (类似 MyBatis 配置文件 ${driver})
    testVariableReplacement();

    System.out.println("--------------------------------------------------");

    // 案例 2: 模拟 SQL 参数占位符替换 (类似 MyBatis SQL #{id})
    testSqlParameterPlaceholder();

    System.out.println("--------------------------------------------------");

    // 案例 3: 使用 PropertyParser (自带默认值逻辑)
    testPropertyParserWithDefaultValue();
  }

  /**
   * 模拟配置文件中的变量替换 将 ${key} 替换为 Map 中对应的值
   */
  private static void testVariableReplacement() {
    System.out.println("案例 1: 变量替换 ${...}");

    // 1. 准备数据源 (模拟 Properties)
    Map<String, String> variables = new HashMap<>();
    variables.put("username", "admin");
    variables.put("password", "123456");
    variables.put("url", "jdbc:mysql://localhost:3306/mydb");

    // 2. 定义处理器 (TokenHandler)
    // 逻辑：拿到 token (比如 "username")，去 Map 里找值，找不到就原样返回
    TokenHandler handler = content -> {
      if (variables.containsKey(content)) {
        return variables.get(content);
      }
      return "${" + content + "}"; // 找不到值时，通常保持原样
    };

    // 3. 创建解析器 (GenericTokenParser)
    // 指定开始符号 "${" 和 结束符号 "}"
    GenericTokenParser parser = new GenericTokenParser("${", "}", handler);

    // 4. 执行解析
    String text = "User: ${username}, Pwd: ${password}, URL: ${url}, Other: ${unknown}";
    String result = parser.parse(text);

    System.out.println("原始文本: " + text);
    System.out.println("解析结果: " + result);
  }

  /**
   * 模拟 SQL 预编译参数替换 将 #{xxx} 替换为 ?，并提取参数名
   */
  private static void testSqlParameterPlaceholder() {
    System.out.println("案例 2: SQL 参数解析 #{...}");

    // 1. 定义处理器
    // 逻辑：把所有的 #{xxx} 都替换成 ?，同时打印出参数名
    TokenHandler handler = content -> {
      System.out.println("  > 发现参数: " + content);
      return "?"; // 替换成 JDBC 占位符
    };

    // 2. 创建解析器
    // 指定开始符号 "#{" 和 结束符号 "}"
    GenericTokenParser parser = new GenericTokenParser("#{", "}", handler);

    // 3. 执行解析
    String sql = "INSERT INTO users (id, name, age) VALUES (#{id}, #{username}, #{age})";
    String parsedSql = parser.parse(sql);

    System.out.println("原始 SQL: " + sql);
    System.out.println("解析 SQL: " + parsedSql);
  }

  /**
   * 案例 3: 使用 MyBatis 自带的 PropertyParser 演示开启默认值功能 ${key:default}
   */
  private static void testPropertyParserWithDefaultValue() {
    System.out.println("案例 3: PropertyParser 默认值演示 ${key:default}");

    // 1. 准备变量 (Properties)
    Properties variables = new Properties();
    variables.setProperty("username", "root");
    // 注意：这里没有设置 password 和 timeout

    // 2. 开启默认值功能 (KEY_ENABLE_DEFAULT_VALUE)
    // 默认是关闭的，必须显式开启！
    variables.setProperty(PropertyParser.KEY_ENABLE_DEFAULT_VALUE, "true");

    // 3. 准备测试文本
    String text = "User: ${username}, Pwd: ${password:123456}, Timeout: ${timeout:5000}, Unknown: ${unknown}";

    // 4. 调用 PropertyParser.parse (它内部会自动创建 VariableTokenHandler)
    // 线程安全 创建2个新的对象
    String result = PropertyParser.parse(text, variables);

    System.out.println("原始文本: " + text);
    System.out.println("解析结果: " + result);
  }

}
