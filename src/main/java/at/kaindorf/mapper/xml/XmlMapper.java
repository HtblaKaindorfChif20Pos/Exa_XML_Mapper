package at.kaindorf.mapper.xml;

import at.kaindorf.mapper.annotations.XmlAttribute;
import at.kaindorf.mapper.annotations.XmlElement;
import at.kaindorf.mapper.annotations.XmlFormat;
import at.kaindorf.mapper.io.IO_Access;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Project: plf_xmlMapper
 * Created by: SF
 * Date: 13.03.2024
 * Time: 10:54
 */
public class XmlMapper {

  private static final String XMLHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

  /**
   * create XML-String out of object information and write result to file
   *
   * @param object
   * @param file
   * @throws IOException
   */
  public void marshall(Object object, File file) throws IOException {
    String xmlRootElement = object.getClass().getSimpleName();

    // collect all attributes with values to StringBuilder: attributeName=attributeValue
    StringBuilder sbAttributes = new StringBuilder("");
    Arrays.stream(object.getClass().getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(XmlAttribute.class))
        .peek(f -> f.setAccessible(true))
        .forEach(f -> sbAttributes.append(String.format(" %s=\"%s\"", getFieldName(f), getFieldValue(f, object))));

    // collect all fields with values to StringBuilder: <fieldName>fieldValue</fieldName>
    StringBuilder sbFields = new StringBuilder("");
    Arrays.stream(object.getClass().getDeclaredFields())
        .filter(f -> !f.isAnnotationPresent(XmlAttribute.class))
        .peek(f -> f.setAccessible(true))
        .forEach(f -> sbFields.append(String.format("\t<%1$s>%2$s</%1$s>\n", getFieldName(f), getFieldValue(f, object))));

    String xmlString = String.format("%s<%2$s%3$s>\n%4$s</%2$s>",
        XMLHeader, xmlRootElement, sbAttributes.toString(), sbFields.toString());

    // write XML-String to file
    IO_Access.writeStringToFile(xmlString, file);
  }

  /**
   * get name of field
   *
   * @param field
   * @return
   */
  private String getFieldName(Field field) {
    String name = field.getName();
    if (field.isAnnotationPresent(XmlElement.class)) {
      name = field.getAnnotation(XmlElement.class).name();
    }
    return name;
  }

  /**
   * get value of field from object
   *
   * @param field
   * @param object
   * @return
   */
  private String getFieldValue(Field field, Object object) {
    try {
      String value = field.get(object).toString();
      if (field.getType().equals(LocalDate.class) && field.isAnnotationPresent(XmlFormat.class)) {
        String pattern = field.getAnnotation(XmlFormat.class).pattern();
        value = DateTimeFormatter.ofPattern(pattern).format((LocalDate) field.get(object));
      }
      return value;
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * read XML-String from file and create object from String
   *
   * @param xmlClass
   * @param file
   * @return
   * @throws IOException
   */
  public Object unmarshall(Class xmlClass, File file) throws IOException {
    // put each line from XML-file into a separate token
    String[] tokens = IO_Access.readStringFromFile(file).split("\n");
    tokens = Arrays.stream(tokens)
        .skip(1)                                  // eliminate XML-Header
        .map(t -> t.trim()).toArray(String[]::new);  // eliminate leading whitespace,Tabs

    // Map containing fieldName-fieldValue pairs
    Map<String, String> fieldValueMap = new HashMap<>();

    // extract attributes form first token into String[]
    String[] attrTokens = tokens[0].substring(tokens[0].indexOf(" ") + 1, tokens[0].lastIndexOf(">"))
        .replaceAll("\"", "")
        .split(" ");
    // collect all attributes into fieldValueMap
    Arrays.stream(attrTokens)
        .forEach(t -> fieldValueMap.put(t.split("=")[0], t.split("=")[1]));

    // collect all subtags into fieldValueMap
    Arrays.stream(tokens)
        .skip(1)
        .forEach(t -> fieldValueMap.put(t.substring(1, t.indexOf(">")),   // extract rank from <rank>3</rank>
            t.replaceAll("<.*?>", "")));                  // extract 3 from <rank>3</rank>

    // instantiate object from class-type
    Object xmlObject = null;
    try {
      xmlObject = xmlClass.newInstance();
    } catch (IllegalAccessException | InstantiationException e) {
      throw new RuntimeException("Object creation failed: " + e.getMessage());
    }

    // loop over all declared fields and populate fields wih values from fieldValueMap
    for (Field field : xmlClass.getDeclaredFields()) {
      field.setAccessible(true);
      String name = getFieldName(field);
      Object value = getFieldValueAsObject(field, fieldValueMap.get(name));
      try {
        field.set(xmlObject, value);
      } catch (IllegalAccessException e) {
        System.out.println(e.toString());
      }
    }

//    Object finalXmlObject = xmlObject;
//    Arrays.stream(xmlClass.getDeclaredFields())
//        .forEach(f -> setFieldValueToObject(f, finalXmlObject,getFieldValueAsObject(f, fieldValueMap.get(getFieldName(f)))));
    return xmlObject;
  }

//  private void setFieldValueToObject(Field field, Object xmlObject, Object value) {
//    try {
//      field.setAccessible(true);
//      field.set(xmlObject, value);
//    } catch (IllegalAccessException e) {
//      throw new RuntimeException(e);
//    }
//  }

  /**
   * convert field value from String to datatype of field
   *
   * @param field for which value is going to be set
   * @param value for field as String
   * @return
   */
  private Object getFieldValueAsObject(Field field, String value) {
    return switch (field.getType().getSimpleName()) {
      case "int", "Integer" -> Integer.valueOf(value);
      case "long", "Long" -> Long.valueOf(value);
      case "float", "Float" -> Float.valueOf(value);
      case "double", "Double" -> Double.valueOf(value);
      case "LocalDate" -> field.isAnnotationPresent(XmlFormat.class) ?
          LocalDate.parse(value, DateTimeFormatter.ofPattern(field.getAnnotation(XmlFormat.class).pattern())) :
          LocalDate.parse(value);
      default -> value;
    };
  }

}
