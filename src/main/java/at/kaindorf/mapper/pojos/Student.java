package at.kaindorf.mapper.pojos;

import at.kaindorf.mapper.annotations.XmlAttribute;
import at.kaindorf.mapper.annotations.XmlElement;
import at.kaindorf.mapper.annotations.XmlFormat;
import at.kaindorf.mapper.xml.XmlMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

/**
 * Project: plf_xmlMapper
 * Created by: SF
 * Date: 13.03.2024
 * Time: 10:51
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Student {
  @XmlAttribute
  @XmlElement(name = "student_id")
  private int studentId;
  private Integer grade;
  private String firstname;
  private String lastname;
  private double rating;

  @XmlAttribute
  private Float weight;
  @XmlFormat(pattern = "yyyy-MMM-dd")
  @XmlAttribute
  private LocalDate birthdate;

  private LocalDate schoolStartedAt;

  public static void main(String[] args) {
    XmlMapper mapper = new XmlMapper();
    at.kaindorf.mapper.pojos.Student student = new at.kaindorf.mapper.pojos.Student(12, 4, "Lisa", "Simpson", 1.12,
        38.456f, LocalDate.now().minusYears(12), LocalDate.now());
    System.out.println(student);
    File xmlFile = Path.of(System.getProperty("user.dir"), "src", "main", "resources", "student.xml").toFile();
    try {
      mapper.marshall(student, xmlFile);
      at.kaindorf.mapper.pojos.Student lisa = (at.kaindorf.mapper.pojos.Student) mapper.unmarshall(at.kaindorf.mapper.pojos.Student.class, xmlFile);
      System.out.println(lisa);
    } catch (IOException e) {
      System.out.println(e.toString());
    }
  }
}
