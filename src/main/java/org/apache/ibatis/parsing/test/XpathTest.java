package org.apache.ibatis.parsing.test;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XpathTest {

  /**
   * java 原生 xpath 解析使用方式 1.构建 DOM 树：也就是把 XML 文件读入内存变成 org.w3c.dom.Document 对象。 2. 创建 XPath 实例：通过工厂创建 XPath 对象。 3
   * 编译/执行表达式：调用 evaluate 方法，并且必须手动指定返回类型。
   */
  /*
   * <users> <user id="1"> <name>张三</name> <age>18</age> </user> <user id="2"> <name>李四</name> <age>20</age> </user>
   * </users>
   */
  static String xml = "<users><user id='1'><name>张三</name><age>18</age></user><user id='2'><name>李四</name><age>20</age></user></users>";

  public static void main1(String[] args) throws Exception { // 原生方法这里有一堆 Checked Exception {

    // --- 步骤 1: 把 XML 变成 Document 对象 (很啰嗦) ---
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    // 为了避免 XXE 漏洞，通常还得设置一堆安全参数 (MyBatis 帮你做了)
    dbFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
    Document doc = dBuilder.parse(new ByteArrayInputStream(xml.getBytes()));

    // --- 步骤 2: 创建 XPath 工具 ---
    XPathFactory xpathFactory = XPathFactory.newInstance();
    XPath xpath = xpathFactory.newXPath();

    // --- 步骤 3: 执行查询 (最痛苦的地方) ---

    // 场景 A: 获取一个字符串值
    // 必须传入 XPathConstants.STRING，返回 Object，还得强转或 toString
    String name = (String) xpath.evaluate("/users/user[@id='1']/name", doc, XPathConstants.STRING);
    System.out.println("Name: " + name);

    // 场景 B: 获取一个数字
    // 必须传入 XPathConstants.NUMBER，返回的是 Double (即使你是整数)
    Double age = (Double) xpath.evaluate("/users/user[@id='1']/age", doc, XPathConstants.NUMBER);
    System.out.println("Age: " + age.intValue()); // 还得自己转 int

    // 场景 C: 获取一个节点列表 (NodeList)
    // 必须传入 XPathConstants.NODESET，返回 NodeList
    NodeList userNodes = (NodeList) xpath.evaluate("/users/user", doc, XPathConstants.NODESET);

    // 场景 D: 遍历 NodeList (原生 NodeList 非常难用，不支持增强 for 循环)
    for (int i = 0; i < userNodes.getLength(); i++) {
      Node node = userNodes.item(i);
      // 如果要在节点基础上继续查，得把 node 传进去
      String userName = (String) xpath.evaluate("name", node, XPathConstants.STRING);
      System.out.println("Loop User: " + userName);
    }

  }

  public static void main(String[] args) throws Exception {
    System.out.println("--------------------XPathParser--------------------");
    XPathParser parser = new XPathParser(xml);

    // 场景 A: 获取一个字符串值 (直接返回 String，无需强转)
    String name = parser.evalString("/users/user[@id='1']/name");
    System.out.println("Name: " + name);

    // 场景 B: 获取一个数字 (直接返回 Integer，自动处理类型转换)
    Integer age = parser.evalInteger("/users/user[@id='1']/age");
    System.out.println("Age: " + age);

    // 场景 C: 获取 XNode 列表 (支持增强 for 循环)
    // 场景 D: 遍历 (每个元素都是 XNode，方法丰富)
    for (XNode node : parser.evalNodes("/users/user")) {
      // 在当前节点下继续查询子节点的值
      String userName = node.evalString("name");
      System.out.println("Loop User: " + userName);
    }
  }

}
