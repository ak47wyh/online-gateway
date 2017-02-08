package com.iboxpay.settlement.gateway.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iboxpay.settlement.gateway.common.exception.BaseTransException;
import com.iboxpay.settlement.gateway.common.exception.ParseMessageException;

/**
 * DOM4j工具类
 * @author jianbo_chen
 */
public class DomUtil {

    public static Element addChild(Element parent, String childName, String childValue) {
        Element child = parent.addElement(childName);
        child.setText(childValue == null ? "" : childValue);
        return child;
    }

    public static Element addChild(Element parent, String childName) {
        Element child = parent.addElement(childName);
        return child;
    }

    public static String documentToString(Document document, String charset) {
        return documentToString(document, charset, true);
    }

    /**
     * DOM4j的Document对象转为XML报文串
     * @param document
     * @param charset
     * @return
     */
    public static String documentToString(Document document, String charset, boolean indent) {
        StringWriter stringWriter = new StringWriter();
        OutputFormat format;
        if (indent)
            format = OutputFormat.createPrettyPrint();
        else format = OutputFormat.createCompactFormat();
        format.setEncoding(charset);
        XMLWriter xmlWriter = new XMLWriter(stringWriter, format);
        try {
            xmlWriter.write(document);
            xmlWriter.flush();
            xmlWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

    /**
     * 去掉声明头的(即<?xml...?>去掉)
     * @param document
     * @param charset
     * @return
     */
    public static String documentToStringNoDeclaredHeader(Document document, String charset) {
        String xml = documentToString(document, charset);
        return xml.replaceFirst("\\s*<[^<>]+>\\s*", "");
    }

    public final static Element parseXmlNoDeclaredHeader(String xml) throws ParseMessageException {
        return parseXml("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + xml);
    }

    public final static Element parseXml(String xml) throws ParseMessageException {
        StringReader sr = new StringReader(xml);
        SAXReader saxReader = new SAXReader();
        Document document;
        try {
            document = saxReader.read(sr);
        } catch (DocumentException e) {
            throw new ParseMessageException(e);
        }
        Element rootElement = document.getRootElement();
        return rootElement;
    }

    public final static String getText(Element e, String tag) {
        Element _e = e.element(tag);
        if (_e != null)
            return _e.getText();
        else return null;
    }

    public final static String getTextTrim(Element e, String tag) {
        Element _e = e.element(tag);
        if (_e != null)
            return _e.getTextTrim();
        else return null;
    }

    /**
     * 获取节点值.节点必须不能为空，否则抛错
     * @param parent
     * @param tag
     * @return
     * @throws ParseMessageException
     */
    public final static String getTextTrimNotNull(Element parent, String tag) throws ParseMessageException {
        Element e = parent.element(tag);
        if (e == null)
            throw new ParseMessageException(parent.getName() + " -> " + tag + " 节点为空.");
        else return e.getTextTrim();
    }

    /**
     * 节点必须不能为空，否则抛错
     * @param parent
     * @param tag
     * @return
     * @throws ParseMessageException
     */
    public final static Element elementNotNull(Element parent, String tag) throws ParseMessageException {
        Element e = parent.element(tag);
        if (e == null)
            throw new ParseMessageException(parent.getName() + " -> " + tag + " 节点为空.");
        else return e;
    }

    /**
     * dom4j的document对象转为w3c的document对象
     * @param doc
     * @return
     * @throws BaseTransException
     */
    public final static org.w3c.dom.Document convertDom4jToW3C(Document doc) throws BaseTransException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(doc.asXML()));
            //	    	inputSource.setEncoding("UTF-8");
            return builder.parse(inputSource);
        } catch (Exception e) {
            throw new BaseTransException("xml转换错误", e);
        }
    }

    /**
     * w3c的document对象转换dom4j的document对象
     * @param doc
     * @return
     * @throws BaseTransException
     */
    public final static Document convertW3CToDom4j(org.w3c.dom.Document doc, String encoding) throws BaseTransException {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            t.transform(new DOMSource(doc), new StreamResult(bos));
            return DomUtil.parseXml(bos.toString(encoding)).getDocument();
        } catch (Exception e) {
            throw new BaseTransException("xml转换错误", e);
        }
    }
}
